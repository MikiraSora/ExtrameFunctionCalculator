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
        executor.InitFromFile(input_file);
        if(executor==null)
            throw new Exception("cant load script "+input_file+" file by unknown cause.");
        //ScriptMap.put(executor.GetPackageName(),executor);
        if(ScriptMap.containsKey(executor.GetPackageName())){
            Log.Debug(String.format("the package %s is exsited already.",executor.GetPackageName()));
        }
        else
            ReferenceAdd(executor.GetPackageName(),executor);
        return executor.GetPackageName();
    }

    public void LoadScript(Executor executor){
        if (executor==null)
            return;
        ReferenceAdd(executor.GetPackageName(),executor);
    }

    public void UnloadScript(String package_name){
        if(!ScriptMap.containsKey(package_name))
            return;
        Executor executor=ScriptMap.get(package_name);
        executor.Drop();
        if(!executor.IsNonReferenced())
            return;

        if(ableCacheReferenceFunction){
            for(Parser.Statement.Function function:executor.parser.FunctionTable.values()){
                if(!CacheFunctionMap.containsKey(function.GetFunctionName()))
                    continue;
                if(CacheFunctionMap.get(function.GetFunctionName()).reference_parser.GetExecutor().GetPackageName().equals(executor.GetPackageName()))
                    CacheFunctionMap.remove(function.GetFunctionName());
            }
        }

        for(Executor executor1:executor.recordIncludeExecutor){
            UnloadScript(executor1.GetPackageName());
        }
    }

    public Executor GetExecutor(String package_name){
        return ScriptMap.get(package_name);
    }

    private void ReferenceAdd(String package_name,Executor executor){
        if(!ScriptMap.containsKey(package_name))
                    ScriptMap.put(package_name, executor);
        ScriptMap.get(package_name).Link();
        if(ableCacheReferenceFunction)
            for(Parser.Statement.Function function:executor.parser.FunctionTable.values())
                if (!CacheFunctionMap.containsKey(function.GetFunctionName()))
                    CacheFunctionMap.put(function.GetFunctionName(),function);
    }

    public Calculator.ScriptFunction RequestFunction(String function_name){
        if(ableCacheReferenceFunction){
            if(CacheFunctionMap.containsKey(function_name))
            {
                Parser.Statement.Function function=CacheFunctionMap.get(function_name);
                return new Calculator.ScriptFunction(function_name,function.reference_parser.GetExecutor(),GetCalculator());
            }

        }
        for(Executor executor:ScriptMap.values())
            if(executor.parser.FunctionTable.containsKey(function_name))
                return new Calculator.ScriptFunction(function_name,executor,GetCalculator());
        return null;
    }

    HashMap<String, Parser.Statement.Function> CacheFunctionMap=null;

    private boolean ableCacheReferenceFunction=false;
    public void SetCacheReferenceFunction(boolean sw) {
        ableCacheReferenceFunction = sw;
        if (sw) {
            if (CacheFunctionMap == null) {
                CacheFunctionMap = new HashMap<>();
                //init cache
                for (Executor executor : ScriptMap.values())
                    for (Parser.Statement.Function function : executor.parser.FunctionTable.values())
                        if (!CacheFunctionMap.containsKey(function.GetFunctionName()))
                            CacheFunctionMap.put(function.GetFunctionName(), function);
            }
        } else {
            CacheFunctionMap = null;
        }
    }

    public boolean ContainScript(String package_name){return ScriptMap.containsKey(package_name);}

    public boolean ContainFunction(String function_name){
        if(ableCacheReferenceFunction){
            if(CacheFunctionMap.containsKey(function_name))
            {
                Parser.Statement.Function function=CacheFunctionMap.get(function_name);
                return true;
            }

        }
        for(Executor executor:ScriptMap.values())
            if(executor.parser.FunctionTable.containsKey(function_name))
                return true;
        return false;
    }

    public Calculator.Variable RequestVariable(String name){
        Calculator.Variable variable=null;
        for(Executor executor : ScriptMap.values()){
            variable=executor.GetVariable(name);
            if(variable!=null)
                return variable;
        }
        return null;
    }
}
