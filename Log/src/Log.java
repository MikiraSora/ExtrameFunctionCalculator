import com.sun.org.apache.xml.internal.security.Init;
import sun.awt.Mutex;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Created by MikiraSora on 2016/10/10.
 */
class LogTest{

    public static void main(String[] args) {
        String message=null;
        Scanner scanner = new Scanner(System.in);

        Log.SetPort(2857);
        Log.SetAddress("127.0.0.1");

        //Lock lock=new Mutex();

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
    static {
        InitRecordTime();
    }

    static class Message{
        enum Type{
            Debug,
            Warning,
            Exception
        }

        private Type message_type=Type.Debug;

        private long time_strip=0;

        private String message=null;

        private static long recordTime=0;

        private static void InitRecordTime(){recordTime=System.currentTimeMillis();}

        private static long GetCurrentTime(){return System.currentTimeMillis()-recordTime;}

        public Message(){
            time_strip=GetCurrentTime();
        }

        public Message(Type type,long time,String message){
            message_type=type;
            time_strip=time;
            this.message=message;
        }

        public Message(Type type,String message){this(type,GetCurrentTime(),message);}

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
            return String.format("[%2d:%2d:%2d %s]%s",min,sec,mill_sec,message_type.toString(),message);
            //return String.format("%s[%d:%d:%d %s]%s %s",GetColorWithType(message_type),min,sec,mill_sec,message_type.toString(),message,COLOR_RESET);
        }
    }

    private static Socket socket;

    public static void InitRecordTime(){Message.InitRecordTime();}

    public static Thread thread=null;

    private static class MaintenanceRunnable implements Runnable{
        private volatile boolean isLock=true;
        private volatile boolean isExit=false;
        private long flushInv=30;

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
                        Thread.currentThread().sleep(flushInv);
                    }catch (Exception e){}
                }
                /*
                for(int index=0;index<CommitLogQueue.size();index++){
                    message=CommitLogQueue.remove(0);
                    if(message==null)
                        continue;
                    try {
                        LogWrite(message);
                    }catch (Exception e){}
                }
                */
                while(!CommitLogQueue.isEmpty()){
                    message=CommitLogQueue.remove(0);
                    if(message==null)
                        continue;
                    try {
                        SocketWrite(message.toString());
                    }catch (Exception e){}
                }
                isLock=true;
            }
        }
    }

    private static MaintenanceRunnable maintenanceRunnable=new MaintenanceRunnable();

    private static boolean IsThreadCommitLog=false;
    public static void SetIsThreadCommitLog(Boolean isThreadCommitLog){
        IsThreadCommitLog=isThreadCommitLog;
        if(IsThreadCommitLog){
            if(thread==null)
                thread=new Thread(maintenanceRunnable);
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

    public static void Error(String message)throws Exception{
        Message msg=new Message(Message.Type.Exception,message);
        LogWrite(msg);
    }

    public static void Debug(String message)throws Exception{
        Message msg=new Message(Message.Type.Debug,message);
        LogWrite(msg);
    }

    public static void Warning(String message)throws Exception{
        Message msg=new Message(Message.Type.Warning,message);
        LogWrite(msg);
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
