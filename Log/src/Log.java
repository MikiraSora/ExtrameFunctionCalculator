import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by MikiraSora on 2016/10/10.
 */
class LogTest{

    public static void main(String[] args) {
        System.out.print("hello log!");
        String message=null;
        Scanner scanner = new Scanner(System.in);

        Log.SetPort(2857);
        Log.SetAddress("127.0.0.1");

        try {
            Log.Connect();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                message = scanner.nextLine();
                Log.Debug(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

public class Log {

    private static final String COLOR_RESET = "\u001B[0m";
    private static final String COLOR_RED = "\u001B[31m";
    private static final String COLOR_GREEN = "\u001B[32m";
    private static final String COLOR_YELLOW = "\u001B[33m";
    private static final String COLOR_WHITE = "\u001B[37m";

    static class Message{
        enum Type{
            Debug,
            Warning,
            Exception
        }

        private Type message_type=Type.Debug;

        private long time_strip=0;

        private String message=null;

        private static long GetCurrentTime(){return System.currentTimeMillis();}

        public Message(){
            time_strip=GetCurrentTime();
        }

        public Message(Type type,long time,String message){
            message_type=type;
            time_strip=time;
            this.message=message;
        }

        public Message(Type type,String message){this(type,GetCurrentTime(),message);}

        @Override
        public String toString() {
            long min=time_strip/1000/60;
            long sec=(time_strip-min*1000*60)/1000;
            long mill_sec=time_strip-min*60*1000-sec*1000;
            return String.format("[%d:%d:%d %s]%s",min,sec,mill_sec,message_type.toString(),message);
        }
    }

    private static Socket socket;

    private static int port=2857;
    public static void SetPort(int port){Log.port=port;}

    private static String address="127.0.0.1";
    public static void SetAddress(String address){Log.address=address;}

    private static ArrayList<Message> messages_history=new ArrayList<>();

    private static int histroy_size=10;
    public static void SetHistorySize(int history_size){Log.histroy_size=history_size;}

    private static void PushHistory(Message message){
        if(messages_history.size()>=histroy_size)
            messages_history.remove(0);
        messages_history.add(message);
    }

    private static boolean IsConnecting(){
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
        byte strbuffer[]=text.getBytes();
        GetOutputStream().write(strbuffer,0,strbuffer.length);
        GetOutputStream().flush();
    }

    public static void LogWrite(Message message)throws Exception{
        if(!IsConnecting())
            Connect();
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
