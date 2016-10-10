import java.net.Socket;

/**
 * Created by MikiraSora on 2016/10/10.
 */

class LogServerTest{

}

public class LogServer {

    private int port=2857;
    public void SetPort(int port){this.port=port;}

    public static abstract class OnReceiveMessage{
        public void onReceiveMessage(String message){}
    }

    OnReceiveMessage trigger_onReceiveMessage=null;
    public void SetOnReceiveMessage(OnReceiveMessage onReceiveMessage){trigger_onReceiveMessage=onReceiveMessage;}

    private volatile boolean Looping=true;
    public void StopLoop(){Looping=false;}

    public boolean LoopRun(){
        Looping=true;
    }
}
