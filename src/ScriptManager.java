import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by MikiraSora on 2016/11/3.
 */
public class ScriptManager {
    Calculator calculator=null;
    Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    private ScriptManager(){}
    ScriptManager(Calculator calculator){this.calculator=calculator;}

    private HashMap<String,Executor> ScriptMap=new HashMap<>();

    public int GetLoadedSricptCount(){return ScriptMap.size();}

    public String LoadScript(String input_file)throws Exception{
        Executor executor=new Executor(GetCalculator());
        if(executor==null)
            throw new Exception("cant load script "+input_file+" file by unknown cause.");
        ScriptMap.put(executor.GetPackageName(),executor);
        return null;//// TODO: 2016/11/3
    }

    public Executor GetExecutor(String package_name){
        return ScriptMap.get(package_name);
    }
}
