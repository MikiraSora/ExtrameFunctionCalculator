import java.net.Socket;

/**
 * Created by MikiraSora on 2016/10/10.
 */

class LogServerTest{

}

public class LogServer {

    private Socket socket=null;

    private static String address="127.0.0.1";

    private int port=2857;
    public void SetPort(int port){this.port=port;}

    public static abstract class OnReceiveMessage{
        public void onReceiveMessage(String message){}
    }

    public static abstract class OnChangeNetStatus{
        public void onWaitConnecting(){}
        public void onConnected(){}
        public void onConnectedLost(){}
    }

    OnReceiveMessage trigger_onReceiveMessage=null;
    public void SetOnReceiveMessage(OnReceiveMessage onReceiveMessage){trigger_onReceiveMessage=onReceiveMessage;}

    OnChangeNetStatus trigger_onChangeNetStatus=null;
    public void SetOnChangeNetStatus(OnChangeNetStatus onChangeNetStatus){trigger_onChangeNetStatus=onChangeNetStatus;}

    private volatile boolean Looping=true;
    public void StopLoop(){Looping=false;}

    public boolean IsConnected(){
        if(socket==null)
            return false;
        return socket.isConnected();
    }

    public void WaitForConnecting(){
        if(IsConnected())
            return;
        socket=new Socket()
    }

    public void LoopRun(){
        Looping=true;
        while (Looping){

        }
    }
}
