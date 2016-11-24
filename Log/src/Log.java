import com.sun.javaws.Main;
import com.sun.org.apache.xml.internal.security.Init;
import sun.awt.Mutex;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import static java.lang.Thread.currentThread;

/**
 * Created by MikiraSora on 2016/10/10.
 */
class LogTest{

    public static void main(String[] args) {
        String message=null;
        Scanner scanner = new Scanner(System.in);
        Log.InitRecordTime();

        Log.SetPort(2857);
        Log.SetAddress("127.0.0.1");

        System.out.println("this is log client and try to connect "+Log.GetAddress()+":"+Log.GetPort());
        Log.SetIsThreadCommitLog(true);

        while(true) {
            try {
                Log.Connect();
                break;
            } catch (Exception e) {
                System.out.println("Connected error.retry connecting.");
                continue;
            }
        }

        if(Log.IsConnecting())
            System.out.println("Connected!");
        else
            System.out.println("Not Connected!");
        while (true) {
            try {
                message = scanner.nextLine();
                Log.Debug(message);
                System.out.println("Socket Send:"+message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

public class Log {

    static class Message{
        enum Type{
            User,
            Debug,
            Warning,
            Exception
        }

        private Type message_type=Type.Debug;

        String callerMethodName=null;

        private long time_strip=0;

        private String message=null;

        private static long recordTime=0;

        private static void InitRecordTime(){recordTime=System.currentTimeMillis();}

        private static long GetCurrentTime(){return System.currentTimeMillis()-recordTime;}

        public Message(){
            time_strip=GetCurrentTime();
        }

        public Message(Type type,long time,String message,String methodname){
            callerMethodName=methodname;
            message_type=type;
            time_strip=time;
            this.message=message;
        }

        public Message(Type type,long time,String message){
            message_type=type;
            time_strip=time;
            this.message=message;
        }

        public Message(Type type,String message){this(type,GetCurrentTime(),message);}
        public Message(Type type,String message,String methodname){this(type,GetCurrentTime(),message,methodname);}

        private static final String COLOR_RESET = "\u001B[0m";
        private static final String COLOR_EXCEPTION = "\u001B[31m";
        private static final String COLOR_DEBUG = "\u001B[32m";
        private static final String COLOR_WARNING = "\u001B[33m";
        private static final String COLOR_NORMAL = "\u001B[37m";

        private static String GetColorWithType(Type type){
            switch (type){
                case Debug:return COLOR_DEBUG;
                case Warning:return COLOR_WARNING;
                case Exception:return COLOR_EXCEPTION;
                default: return COLOR_NORMAL;
            }
        }

        @Override
        public String toString() {
            long min=time_strip/1000/60;
            long sec=(time_strip-min*1000*60)/1000;
            long mill_sec=time_strip-min*60*1000-sec*1000;
            if(Log.IsShowCallerMethod())
                return String.format("%c[%2d:%2d:%2d %s]%s():%s",message_type.toString().charAt(0),min, sec, mill_sec, message_type.toString(), callerMethodName != null ? callerMethodName : "unknown_method", message);
            return String.format("%c[%2d:%2d:%2d %s]%s",message_type.toString().charAt(0), min, sec, mill_sec, message_type.toString(),message);
        }
    }

    private static boolean IsShowCallerMethod=true;
    public static void SetShowCallerMethod(boolean isShowCallerMethod){IsThreadCommitLog=isShowCallerMethod;}
    public static boolean IsShowCallerMethod(){return IsShowCallerMethod;}

    private static Socket socket;

    public static void InitRecordTime(){Message.InitRecordTime();}

    public static Thread thread=null;

    public static boolean EnableLog=true;
    public static void EnableLog(boolean sw){EnableLog=sw;}
    public static boolean GetEnableLog(){return EnableLog;}

    private static class MaintenanceRunnable implements Runnable{
        private static class HighLevelMaintenanceRunnable implements Runnable{
            //private HighLevelMaintenanceRunnable(){}
            //HighLevelMaintenanceRunnable(MaintenanceRunnable maintenanceRunnable){fatherMaintenanceThread=maintenanceRunnable;}

            private volatile boolean isLock=true;
            private volatile boolean isExit=false;

            private MaintenanceRunnable fatherMaintenanceThread=null;

            private long flushInv=30;
            private long recuseTime=3000;
                private long totalTime=0;

                private StringBuilder stringBuilder;
                private static String SegmentationMarker="#@#seg_marker#%#";

                private int CombinCount=40;
            private ArrayList<Message> commitArrayList=new ArrayList<>();

            public void PostMessageSend(Message message){
                commitArrayList.add(message);
            }

            @Override
            public void run() {
                Message message=null;
                isExit=false;
                isLock=false;
                while (!isExit){
                    while (isLock){
                        try {
                            currentThread().sleep(flushInv);
                            if(!commitArrayList.isEmpty())
                                break;
                            totalTime+=flushInv;
                            if(totalTime>=recuseTime){
                                isExit=true;
                                isLock=false;
                                maintenanceRunnable.isHighLevelCommit=false;
                                totalTime=0;
                                return;
                            }
                        }catch (Exception e){}
                    }
                    totalTime=0;
                    while(!commitArrayList.isEmpty()) {
                        stringBuilder=new StringBuilder();
                        for(int index=0;index<commitArrayList.size();index++){
                            if(index>CombinCount)
                                break;
                            message=commitArrayList.remove(0);
                            if(message==null)
                                continue;
                            stringBuilder.append(message.toString()).append(SegmentationMarker);
                        }
                        try {
                            SocketWrite(stringBuilder.toString());
                        } catch (Exception e) {}
                    }
                    isLock=true;
                }
            }

            boolean isExit(){return isExit;}
            boolean isLock(){return isLock;}
        }

        private HighLevelMaintenanceRunnable highLevelMaintenanceRunnable=new HighLevelMaintenanceRunnable();
        private Thread highlevelThread=null;

        private volatile boolean isLock=true;
        private volatile boolean isExit=false;
        public volatile boolean isHighLevelCommit=false;

        private long flushInv=30;

        private long limitCount=1000;

        public boolean IsExit(){return isExit;}

        public void Exit(){isExit=true;}
        public void Unlock(){isLock=false;}

        private ArrayList<Message> CommitLogQueue=new ArrayList<>();
        public void CommitLog(Message message){
            CommitLogQueue.add(message);
            Unlock();
        }

        @Override
        public void run() {
            Message message=null;
            isExit=false;
            while (!isExit){
                while (isLock){
                    try {
                        currentThread().sleep(flushInv);
                    }catch (Exception e){}
                }
                /*
                while(!CommitLogQueue.isEmpty()) {
                    message = CommitLogQueue.remove(0);
                    if (CommitLogQueue.size() > limitCount) {
                        //触发规则
                        if(!isHighLevelCommit){
                            //启动
                            isHighLevelCommit=true;
                            if(highlevelThread==null){
                                highlevelThread=new Thread(highLevelMaintenanceRunnable);
                                highlevelThread.start();
                            }else{
                                if(highLevelMaintenanceRunnable.isExit()){
                                    highlevelThread.start();
                                }
                            }
                        }
                    }
                    if(isHighLevelCommit){
                        highLevelMaintenanceRunnable.PostMessageSend(message);
                        continue;
                    }*/
                    message=CommitLogQueue.isEmpty()?null:CommitLogQueue.remove(0);
                    if (message == null)
                        continue;
                    try {
                        SocketWrite(message.toString());
                    } catch (Exception e) {}
                }
                isLock=true;
            }

    }

    private static MaintenanceRunnable maintenanceRunnable=new MaintenanceRunnable();

    private static boolean IsThreadCommitLog=false;
    public static void SetIsThreadCommitLog(Boolean isThreadCommitLog){
        IsThreadCommitLog=isThreadCommitLog;
        if(IsThreadCommitLog){
            if(thread==null){
                thread=new Thread(maintenanceRunnable);
                thread.setName("Log_Commit thread");
                thread.start();
            }
            if(maintenanceRunnable.IsExit())
                thread.start();
        }else{
            if(thread==null)
                return;
            if(!maintenanceRunnable.IsExit())
                maintenanceRunnable.Exit();
        }
    }

    private static int port=2857;
    public static void SetPort(int port){Log.port=port;}
    public static int GetPort(){return port;}

    private static String address="127.0.0.1";
    public static void SetAddress(String address){Log.address=address;}
    public static String GetAddress(){return address;}

    private static ArrayList<Message> messages_history=new ArrayList<>();

    private static int histroy_size=10;
    public static void SetHistorySize(int history_size){Log.histroy_size=history_size;}

    private static void PushHistory(Message message){
        if(messages_history.size()>=histroy_size)
            messages_history.remove(0);
        messages_history.add(message);
    }

    public static boolean IsConnecting(){
        if(socket==null)
            return false;
        return socket.isConnected();
    }

    public static void Connect()throws Exception{
        if(IsConnecting())
            socket.close();
        if(socket!=null)
            socket.close();
        socket=new Socket(address,port);
    }

    private static OutputStream GetOutputStream()throws Exception{
        return socket.getOutputStream();
    }

    private static void SocketWrite(String text)throws Exception{
        if(!IsConnecting())
            Connect();
        PrintWriter writer=new PrintWriter(new OutputStreamWriter(GetOutputStream(), StandardCharsets.UTF_8));
        writer.println(text);
        writer.flush();
    }

    public static void LogWrite(Message message)throws Exception{
        if(!EnableLog)
            return;
        if(IsThreadCommitLog){
            maintenanceRunnable.CommitLog(message);
            return;
        }
        try {
            SocketWrite(message.toString());
        }catch (Exception e){
            return;
        }
        PushHistory(message);
    }

    public static String GetCallerMethodName(){
        StackTraceElement[] stackTraceElements= Thread.currentThread().getStackTrace();
        int caller_method=3;
        StackTraceElement caller=stackTraceElements[caller_method];
        return caller.getMethodName();
    }

    public static void Error(String message){
        Message msg = new Message(Message.Type.Exception,message,GetCallerMethodName());
        try {
            LogWrite(msg);
        }catch (Exception e){return;}
        Exception exception=new Exception(message);
        exception.setStackTrace(currentThread().getStackTrace());
    }

    public static void ExceptionError(Exception e)throws Exception{
        Message msg=IsShowCallerMethod?new Message(Message.Type.Exception, e.getMessage(),GetCallerMethodName()):new Message(Message.Type.Exception,e.getMessage());
        LogWrite(msg);
        throw e;
    }

    public static void Debug(String message){
        try {
            Message msg =IsShowCallerMethod?new Message(Message.Type.Debug, message,GetCallerMethodName()):new Message(Message.Type.Debug, message);
            LogWrite(msg);
        }catch (Exception e){}
    }

    public static void Warning(String message){
        try {
            Message msg =IsShowCallerMethod?new Message(Message.Type.Warning, message,GetCallerMethodName()):new Message(Message.Type.Warning, message);
            LogWrite(msg);
        }catch (Exception e){}
    }

    public static void User(String message){
        try {
            Message msg =IsShowCallerMethod?new Message(Message.Type.User, message,GetCallerMethodName()):new Message(Message.Type.User, message);
            LogWrite(msg);
        }catch (Exception e){}

    }

    public static void DisConnect()throws Exception{
        socket.close();
    }

    @Override
    protected void finalize() throws Throwable {
        DisConnect();
        super.finalize();
    }
}
