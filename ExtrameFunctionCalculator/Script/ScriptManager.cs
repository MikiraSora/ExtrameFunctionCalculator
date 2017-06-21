using ExtrameFunctionCalculator.Script.Types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script
{
    public class ScriptManager
    {
        Calculator calculator = null;
        private Dictionary<string, Executor> script_map = new Dictionary<string, Executor>();
        private Stack<Executor> executing_executor_stack = new Stack<Executor>();
        bool is_cache_reference_function = false;
        Dictionary<String, Function> cache_function_map = null;

        private ScriptManager() { }
        public ScriptManager(Calculator calculator) { this.calculator = calculator; }

        Calculator GetCalculator() { return calculator == null ? calculator = new Calculator() : calculator; }

        #region Load/Unload Script

        public string LoadScript(string input_file)
        {
            Executor executor = new Executor(GetCalculator());
            executor.InitFromFile(input_file);
            if (executor == null)
                throw new Exception("cant load script " + input_file + " file by unknown cause.");//// TODO: 2016/11/23 可以弄成IOException
            if (script_map.ContainsKey(executor.GetPackageName))
            {
                Log.Debug(string.Format("the package {0} had exsited already.", executor.GetPackageName));
            }
            ReferenceAdd(executor.GetPackageName, executor);
            return executor.GetPackageName;
        }

        public void LoadScript(Executor executor)
        {
            if (executor == null)
                return;
            ReferenceAdd(executor.GetPackageName, executor);
            Log.Debug(String.Format("{0} is add reference count,now is {1}", executor.GetPackageName, executor.ReferenceCount));
        }

        public void UnloadScript(string package_name)
        {
            if (!script_map.ContainsKey(package_name))
                return;
            Executor executor = script_map[(package_name)];
            executor.Drop();
            Log.Debug(String.Format("{0} decrease reference count.now is {1}", executor.GetPackageName, executor.ReferenceCount));
            if (!executor.IsNonReferenced())
                return;

            Log.Debug(String.Format("unloaded {0} ", executor.GetPackageName));

            if (is_cache_reference_function)
            {
                foreach (Function function in executor.RefParser.function_table.Values)
                {
                    if (!cache_function_map.ContainsKey(function.FunctionName))
                        continue;
                    if (cache_function_map[(function.FunctionName)].RefParser.RefExecutor.GetPackageName==(executor.GetPackageName))
                    {
                        cache_function_map.Remove(function.FunctionName);
                        Log.Debug(String.Format("{0}::{1}() was removed from cache", executor.GetPackageName, function.FunctionName));
                    }
                }
            }

            foreach (Executor executor1 in executor.RecordIncludeExecutorList)
            {
                UnloadScript(executor1.GetPackageName);
            }

            script_map.Remove(package_name);
        }
        #endregion

        #region Request Variable/Function
        public ExtrameFunctionCalculator.Types.ScriptFunction RequestFunction(string function_name)
        {
            if (is_cache_reference_function)
            {
                if (cache_function_map.ContainsKey(function_name))
                {
                    Function function = cache_function_map[(function_name)];
                    return new ExtrameFunctionCalculator.Types.ScriptFunction(function_name, function.RefParser.RefExecutor, GetCalculator());
                }

            }
            foreach (Executor executor in script_map.Values)
                if (executor.RefParser.function_table.ContainsKey(function_name))
                    return new ExtrameFunctionCalculator.Types.ScriptFunction(function_name, executor, GetCalculator());
            return null;
        }

        public ExtrameFunctionCalculator.Types.Variable RequestVariable(string name, Executor good_executor)
        {
            ExtrameFunctionCalculator.Types.Variable variable = null;
            if (good_executor != null)
            {
                variable = good_executor.GetVariable(name);
                if (variable != null)
                {
                    //Log.Debug(String.format("got script variable <%s> from paramester executor <%s>.",name,good_executor.GetPackageName()));
                    return variable;
                }
            }
            if (!(executing_executor_stack.Count == 0))
            {
                variable = GetCurrentExecutor().GetVariable(name);
                if (variable != null)
                {
                    //Log.Debug(String.format("got script variable <%s> from current executor <%s>.",name,GetCurrentExecutor().GetPackageName()));
                    return variable;
                }
            }
            foreach (Executor executor in script_map.Values)
            {
                variable = executor.GetVariable(name);
                if (variable != null)
                {
                    //Log.Debug(String.format("got script variable <%s> from one executor <%s> in all executons collection .",name,executor.GetPackageName()));
                    return variable;
                }
            }
            return null;
        }

        public bool ContainFunction(string function_name)
        {
            if (is_cache_reference_function)
            {
                if (cache_function_map.ContainsKey(function_name))
                {
                    //Parser.Statement.Function function=CacheFunctionMap.get(function_name);
                    return true;
                }

            }
            foreach (Executor executor in script_map.Values)
                if (executor.RefParser.function_table.ContainsKey(function_name))
                    return true;
            return false;
        }

        public void SetCacheReferenceFunction(bool sw)
        {
            is_cache_reference_function = sw;
            if (sw)
            {
                if (cache_function_map == null)
                {
                    cache_function_map = new Dictionary<string, Function>();
                    //init cache
                    foreach (Executor executor in script_map.Values)
                        foreach (Function function in executor.RefParser.function_table.Values)
                            if (!cache_function_map.ContainsKey(function.FunctionName))
                                cache_function_map.Add(function.FunctionName, function);
                }
            }
            else
            {
                cache_function_map = null;
            }
        }




        #endregion

        #region Reference
        public int GetLoadedSricptCount() { return script_map.Count; }

        private void ReferenceAdd(string package_name, Executor executor)
        {
            if (!script_map.ContainsKey(package_name))
            {
                script_map.Add(package_name, executor);
                Log.Debug(String.Format("{0} is new script ,load to ScriptMap", package_name));
            }
            executor.Link();
            if (is_cache_reference_function)
                foreach (Function function in executor.RefParser.function_table.Values)
                    if (!cache_function_map.ContainsKey(function.FunctionName))
                        cache_function_map.Add(function.FunctionName, function);
        }

        public void RecoveredExecutingExecutor()
        {
            if (executing_executor_stack.Count == 0)
                return;
            //Log.Debug(String.format("the executor <%s> was popped into current caller executor stack",GetCurrentExecutor().GetPackageName()));
            executing_executor_stack.Pop();
        }

        public void RecordExecutingExecutor(Executor executor)
        {
            executing_executor_stack.Push(executor);
            //Log.Debug(String.format("the executor <%s> was pushed into current caller executor stack",executor.GetPackageName()));
        }

        public Executor GetCurrentExecutor() => executing_executor_stack.Peek();

        public bool ContainScript(string package_name) => script_map.ContainsKey(package_name);




        #endregion
    }
}
