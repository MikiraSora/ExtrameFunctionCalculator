import sun.rmi.runtime.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by MikiraSora on 2016/10/10.
 */

class LogServerTest{
    public static void main(String[] args){
        LogServer logServer=new LogServer(2857);

        logServer.SetOnReceiveMessage(new LogServer.OnReceiveMessage() {
            @Override
            public void onReceiveMessage(String message) {
                System.out.println(message);
            }
        });

        logServer.SetOnChangeNetStatus(new LogServer.OnChangeNetStatus() {
            @Override
            public void onWaitConnecting() {
                System.out.println("waiting for clients in port "+logServer.GetPort());
            }
        });

        logServer.LoopRun();

    }
}

public class LogServer {

    //private ServerSocket server_socket=null;
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

    /*
    public boolean IsConnected(){
        if(server_socket==null)
            return false;
        return !server_socket.isClosed();
    }

    /*public void WaitForConnecting()throws Exception{
        if(IsConnected())
            return;
        server_socket=new ServerSocket(port);
    }
*/
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
            inputStream=socket.getInputStream();
            bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            while (Looping){
                message=bufferedReader.readLine();
                if(trigger_onReceiveMessage!=null)
                    trigger_onReceiveMessage.onReceiveMessage(message);
            }
            if(trigger_onChangeNetStatus!=null)
                trigger_onChangeNetStatus.onConnectedLost();
        }catch (Exception e){
            if(trigger_onChangeNetStatus!=null)
                if(trigger_onChangeNetStatus.onConnectError(e))
                    return;
            LoopRun();
        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (bufferedReader != null)
                    bufferedReader.close();
            }catch (Exception e){

            }
        }
    }
}
