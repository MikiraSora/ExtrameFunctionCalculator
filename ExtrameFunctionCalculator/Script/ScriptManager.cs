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
        Calculator GetCalculator() { return calculator == null ? calculator = new Calculator() : calculator; }

        Dictionary<String,Function> CacheFunctionMap = null;

        private ScriptManager() { }
        public ScriptManager(Calculator calculator) { this.calculator = calculator; }

        private Dictionary<string, Executor> ScriptMap = new Dictionary<string, Executor>();

        private Stack<Executor> ExecutingExecutorStack = new Stack<Executor>();
        bool ableCacheReferenceFunction = false;
        public int GetLoadedSricptCount() { return ScriptMap.Count; }

        #region Load/Unload Script

        public string LoadScript(string input_file)
        {
            Executor executor = new Executor(GetCalculator());
            executor.InitFromFile(input_file);
            if (executor == null)
                throw new Exception("cant load script " + input_file + " file by unknown cause.");//// TODO: 2016/11/23 可以弄成IOException
            if (ScriptMap.ContainsKey(executor.GetPackageName))
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
            if (!ScriptMap.ContainsKey(package_name))
                return;
            Executor executor = ScriptMap[(package_name)];
            executor.Drop();
            Log.Debug(String.Format("{0} decrease reference count.now is {1}", executor.GetPackageName, executor.ReferenceCount));
            if (!executor.IsNonReferenced())
                return;

            Log.Debug(String.Format("unloaded {0} ", executor.GetPackageName));

            if (ableCacheReferenceFunction)
            {
                foreach (Function function in executor.RefParser.FunctionTable.Values)
                {
                    if (!CacheFunctionMap.ContainsKey(function.FunctionName))
                        continue;
                    if (CacheFunctionMap[(function.FunctionName)].RefParser.RefExecutor.GetPackageName==(executor.GetPackageName))
                    {
                        CacheFunctionMap.Remove(function.FunctionName);
                        Log.Debug(String.Format("{0}::{1}() was removed from cache", executor.GetPackageName, function.FunctionName));
                    }
                }
            }

            foreach (Executor executor1 in executor.RecordIncludeExecutorList)
            {
                UnloadScript(executor1.GetPackageName);
            }

            ScriptMap.Remove(package_name);
        }
        #endregion

        public void SetCacheReferenceFunction(bool sw)
        {
            ableCacheReferenceFunction = sw;
            if (sw)
            {
                if (CacheFunctionMap == null)
                {
                    CacheFunctionMap = new Dictionary<string, Function>();
                    //init cache
                    foreach (Executor executor in ScriptMap.Values)
                        foreach (Function function in executor.RefParser.FunctionTable.Values)
                            if (!CacheFunctionMap.ContainsKey(function.FunctionName))
                                CacheFunctionMap.Add(function.FunctionName, function);
                }
            }
            else
            {
                CacheFunctionMap = null;
            }
        }

        public bool ContainScript(string package_name) => ScriptMap.ContainsKey(package_name);

        public bool ContainFunction(string function_name)
        {
            if (ableCacheReferenceFunction)
            {
                if (CacheFunctionMap.ContainsKey(function_name))
                {
                    //Parser.Statement.Function function=CacheFunctionMap.get(function_name);
                    return true;
                }

            }
            foreach (Executor executor in ScriptMap.Values)
                if (executor.RefParser.FunctionTable.ContainsKey(function_name))
                    return true;
            return false;
        }

        public Executor GetCurrentExecutor() => ExecutingExecutorStack.Peek();

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
            if (!(ExecutingExecutorStack.Count==0))
            {
                variable = GetCurrentExecutor().GetVariable(name);
                if (variable != null)
                {
                    //Log.Debug(String.format("got script variable <%s> from current executor <%s>.",name,GetCurrentExecutor().GetPackageName()));
                    return variable;
                }
            }
            foreach (Executor executor in ScriptMap.Values)
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

        public ExtrameFunctionCalculator.Types.ScriptFunction RequestFunction(string function_name)
        {
            if (ableCacheReferenceFunction)
            {
                if (CacheFunctionMap.ContainsKey(function_name))
                {
                   Function function = CacheFunctionMap[(function_name)];
                    return new ExtrameFunctionCalculator.Types.ScriptFunction(function_name, function.RefParser.RefExecutor, GetCalculator());
                }

            }
            foreach (Executor executor in ScriptMap.Values)
                if (executor.RefParser.FunctionTable.ContainsKey(function_name))
                    return new ExtrameFunctionCalculator.Types.ScriptFunction(function_name, executor, GetCalculator());
            return null;
        }

        public void RecordExecutingExecutor(Executor executor)
        {
            ExecutingExecutorStack.Push(executor);
            //Log.Debug(String.format("the executor <%s> was pushed into current caller executor stack",executor.GetPackageName()));
        }

        public void RecoveredExecutingExecutor()
        {
            if (ExecutingExecutorStack.Count==0)
                return;
            //Log.Debug(String.format("the executor <%s> was popped into current caller executor stack",GetCurrentExecutor().GetPackageName()));
            ExecutingExecutorStack.Pop();
        }

        private void ReferenceAdd(string package_name, Executor executor)
        {
            if (!ScriptMap.ContainsKey(package_name))
            {
                ScriptMap.Add(package_name, executor);
                Log.Debug(String.Format("{0} is new script ,load to ScriptMap", package_name));
            }
            executor.Link();
            if (ableCacheReferenceFunction)
                foreach (Function function in executor.RefParser.FunctionTable.Values)
                    if (!CacheFunctionMap.ContainsKey(function.FunctionName))
                        CacheFunctionMap.Add(function.FunctionName, function);
        }

    }
}
