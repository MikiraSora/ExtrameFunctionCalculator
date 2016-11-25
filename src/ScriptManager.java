import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by MikiraSora on 2016/11/3.
 */
public class ScriptManager {
    Calculator calculator=null;
    Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    private ScriptManager(){}
    ScriptManager(Calculator calculator){this.calculator=calculator;}

    private HashMap<String,Executor> ScriptMap=new HashMap<>();

    private Stack<Executor> ExecutingExecutorStack=new Stack<>();

    public int GetLoadedSricptCount(){return ScriptMap.size();}

    /**
     *从磁盘文件中读取加载脚本
     * @param input_file 脚本文件路径
     * @return 脚本名
     * */
    public String LoadScript(String input_file)throws Exception{
        Executor executor=new Executor(GetCalculator());
        executor.InitFromFile(input_file);
        if(executor==null)
            throw new Exception("cant load script "+input_file+" file by unknown cause.");//// TODO: 2016/11/23 可以弄成IOException
        if(ScriptMap.containsKey(executor.GetPackageName())){
            Log.Debug(String.format("the package %s had exsited already.",executor.GetPackageName()));
        }
        ReferenceAdd(executor.GetPackageName(),executor);
        /*
        else
            ReferenceAdd(executor.GetPackageName(),executor);*/
        return executor.GetPackageName();
    }

    /**
     * 被引用加载脚本,被加载的脚本仅仅做引用计数增加
     * @param executor 被引用的执行器
     * */
    public void LoadScript(Executor executor){
        if (executor==null)
            return;
        ReferenceAdd(executor.GetPackageName(),executor);
        Log.Debug(String.format("%s is add reference count,now is %d",executor.GetPackageName(),executor.GetReferenceCount()));
    }

    /**
     * 解除一次引用，如果对应执行器引用为0，意味着并不会有其他执行器会引用，那么才会真正卸下并释放
     * @param package_name 脚本名
     * */
    public void UnloadScript(String package_name){
        if(!ScriptMap.containsKey(package_name))
            return;
        Executor executor=ScriptMap.get(package_name);
        executor.Drop();
        Log.Debug(String.format("%s decrease reference count.now is %d",executor.GetPackageName(),executor.GetReferenceCount()));
        if(!executor.IsNonReferenced())
            return;

        Log.Debug(String.format("unloaded %s ",executor.GetPackageName()));

        if(ableCacheReferenceFunction){
            for(Parser.Statement.Function function:executor.parser.FunctionTable.values()){
                if(!CacheFunctionMap.containsKey(function.GetFunctionName()))
                    continue;
                if(CacheFunctionMap.get(function.GetFunctionName()).reference_parser.GetExecutor().GetPackageName().equals(executor.GetPackageName())){
                    CacheFunctionMap.remove(function.GetFunctionName());
                    Log.Debug(String.format("%s::%s() was removed from cache",executor.GetPackageName(),function.GetFunctionName()));
                }
            }
        }

        for(Executor executor1:executor.recordIncludeExecutor){
            UnloadScript(executor1.GetPackageName());
        }

        ScriptMap.remove(package_name);
    }

    /**
     * 通过脚本名获取执行器
     * @param package_name 脚本名
     * @return 对应的执行器
     * */
    public Executor GetExecutor(String package_name){
        return ScriptMap.get(package_name);
    }

    /**
     * 绑定脚本
     * @param package_name 脚本名,通常是executor.GetPackageName()获得
     * @param executor 要被绑定脚本的执行器
     * */
    private void ReferenceAdd(String package_name,Executor executor){
        if(!ScriptMap.containsKey(package_name)){
            ScriptMap.put(package_name, executor);
            Log.Debug(String.format("%s is new script ,load to ScriptMap",package_name));
        }
        executor.Link();
        if(ableCacheReferenceFunction)
            for(Parser.Statement.Function function:executor.parser.FunctionTable.values())
                if (!CacheFunctionMap.containsKey(function.GetFunctionName()))
                    CacheFunctionMap.put(function.GetFunctionName(),function);
    }

    /**
     * 获取一个脚本函数
     * @param function_name 函数名
     * @return 对应的脚本函数，如果返回null意味着并没有这个函数
     * */
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

    /**
     * 脚本函数引用缓存开关<br>如果打开开关，那么各个脚本文件及执行器将会先枚举所有函数并放到一个缓存表中，获取函数时候将优先从此表查找.
     * @param sw 功能开关
     * */
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

    /**
     * 判断是否含有某脚本
     * @param package_name 脚本名
     * @return 如果存在返回true,反之false
     * */
    public boolean ContainScript(String package_name){return ScriptMap.containsKey(package_name);}

    /**
     * 判断是否含有某脚本函数,会递归查询各个脚本是否存在函数
     * @param function_name 函数名
     * @return 如果存在返回true,反之false
     * */
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

    /**
     * 获取一个脚本变量
     * @param name 变量名
     * @param good_executor 执行器,如果不为null则优先查询此执行器是否有变量，之后再遍历所有执行器。
     * @return 对应的脚本变量，如果返回null意味着并没有这个变量
     * */
    public Calculator.Variable RequestVariable(String name,Executor good_executor){
        Calculator.Variable variable=null;
        if(good_executor!=null) {
            variable = good_executor.GetVariable(name);
            if (variable != null){
                //Log.Debug(String.format("got script variable <%s> from paramester executor <%s>.",name,good_executor.GetPackageName()));
                return variable;
            }
        }
        if(!ExecutingExecutorStack.empty()){
            variable = GetCurrentExecutor().GetVariable(name);
            if (variable != null){
                //Log.Debug(String.format("got script variable <%s> from current executor <%s>.",name,GetCurrentExecutor().GetPackageName()));
                return variable;
            }
        }
        for(Executor executor : ScriptMap.values()){
            variable=executor.GetVariable(name);
            if(variable!=null){
                //Log.Debug(String.format("got script variable <%s> from one executor <%s> in all executons collection .",name,executor.GetPackageName()));
                return variable;
            }
        }
        return null;
    }

    public void RecordExecutingExecutor(Executor executor){
        ExecutingExecutorStack.push(executor);
        //Log.Debug(String.format("the executor <%s> was pushed into current caller executor stack",executor.GetPackageName()));
    }

    public void RecoveredExecutingExecutor(){
        if(ExecutingExecutorStack.empty())
            return;
        //Log.Debug(String.format("the executor <%s> was popped into current caller executor stack",GetCurrentExecutor().GetPackageName()));
        ExecutingExecutorStack.pop();
    }

    public Executor GetCurrentExecutor(){return ExecutingExecutorStack.peek();}

}
