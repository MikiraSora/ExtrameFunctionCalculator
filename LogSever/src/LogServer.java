import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import print.Printer;
import print.color.Ansi;
import print.color.ColoredPrinter;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by MikiraSora on 2016/10/10.
 */

class LogServerTest{

    private static String SegmentationMarker="#@#seg_marker#%#";

    static ColoredPrinter debug_cp,exception_cp,warning_cp,user_cp,default_cp;

    static {
        try {
            default_cp=new ColoredPrinter.Builder(0,false).foreground(Ansi.FColor.NONE).build();
            debug_cp = new ColoredPrinter.Builder(0,false).foreground(Ansi.FColor.WHITE).build();
            exception_cp = new ColoredPrinter.Builder(0,false).foreground(Ansi.FColor.YELLOW).background(Ansi.BColor.RED).attribute(Ansi.Attribute.BOLD).build();
            warning_cp = new ColoredPrinter.Builder(0,false).foreground(Ansi.FColor.WHITE).background(Ansi.BColor.YELLOW).build();
            user_cp=new ColoredPrinter.Builder(0,false).foreground(Ansi.FColor.GREEN).attribute(Ansi.Attribute.BOLD).build();
        }catch (Exception e){
            //nothing to do
        }
    }

    static void PrintMessage(String message){
        String context=message.substring(1);
        switch (message.charAt(0)){
            case 'U':{user_cp.println(context);break;}
            case 'D':{debug_cp.println(context);break;}
            case 'W':{warning_cp.println(context);break;}
            case 'E':{exception_cp.println(context);break;}
            default:{default_cp.println(context);break;}
        }
    }


    public static void main(String[] args){
/*
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLUE)   //setting format
                .build();
        cp.clear();
        cp.print(cp.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
*/
        LogServer logServer=new LogServer(2857);

        logServer.SetOnReceiveMessage(new LogServer.OnReceiveMessage() {
            @Override
            public void onReceiveMessage(String message) {
                String[] messageList=message.split(SegmentationMarker);
                for(String str : messageList)
                {
                    PrintMessage(str);
                }
            }
        });

        logServer.SetOnChangeNetStatus(new LogServer.OnChangeNetStatus() {
            @Override
            public void onWaitConnecting() {
                PrintMessage(" "+"waiting for clients in port "+logServer.GetPort());
            }

            @Override
            public void onConnected() {
                PrintMessage(" "+"Connected!");
            }

            @Override
            public void onConnectedLost() {
                PrintMessage(" "+"Connecting Lost!");
            }

            @Override
            public boolean onConnectError(Exception e) {
                PrintMessage(" "+"Connected Error!now reset server and waiting new connection.");
                return false;
            }
        });

        while (true)
            logServer.LoopRun();

        //return;
    }
}

public class LogServer {

    public LogServer(int port){this.port=port;}

    private int port=2857;
    public void SetPort(int port){this.port=port;}
    public int GetPort(){return port;}

    public static abstract class OnReceiveMessage{
        public void onReceiveMessage(String message){}
    }

    public static abstract class OnChangeNetStatus{
        public void onWaitConnecting(){}
        public void onConnected(){}
        public boolean onConnectError(Exception e){return true;}
        public void onConnectedLost(){}
    }

    OnReceiveMessage trigger_onReceiveMessage=null;
    public void SetOnReceiveMessage(OnReceiveMessage onReceiveMessage){trigger_onReceiveMessage=onReceiveMessage;}

    OnChangeNetStatus trigger_onChangeNetStatus=null;
    public void SetOnChangeNetStatus(OnChangeNetStatus onChangeNetStatus){trigger_onChangeNetStatus=onChangeNetStatus;}

    private volatile boolean Looping=true;
    public void StopLoop(){Looping=false;}

    public void LoopRun(){
        Looping=true;
        ServerSocket server_socket=null;
        Socket socket=null;
        InputStream inputStream=null;
        BufferedReader bufferedReader=null;

        String message=null;
        try {
            if(trigger_onChangeNetStatus!=null)
                trigger_onChangeNetStatus.onWaitConnecting();
            server_socket=new ServerSocket(port);
            socket=server_socket.accept();
            if(trigger_onChangeNetStatus!=null)
                trigger_onChangeNetStatus.onConnected();
            bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            while (socket.isConnected()){
                message=bufferedReader.readLine();
                port=port;
                if(trigger_onReceiveMessage!=null)
                    trigger_onReceiveMessage.onReceiveMessage(message);
            }
            socket.close();
            if(trigger_onChangeNetStatus!=null)
                trigger_onChangeNetStatus.onConnectedLost();
        }catch (Exception e){
            if(trigger_onChangeNetStatus!=null){
                trigger_onChangeNetStatus.onConnectError(e);
            }

        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (bufferedReader != null)
                    bufferedReader.close();
                socket.close();
                server_socket.close();
            }catch (Exception e){
                return;
            }
        }
    }
}
