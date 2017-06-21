using ExtrameFunctionCalculator.Script.Types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ExtrameFunctionCalculator.BooleanCalculatorSupport;

namespace ExtrameFunctionCalculator.Script
{
    public class Executor
    {
        Calculator calculator = null;
        Calculator GetCalculator() { return calculator == null ? calculator = new Calculator() : calculator; }

        string include_file_path = "";

        List<Executor> recordIncludeExecutor = new List<Executor>();
        public List<Executor> RecordIncludeExecutorList { get { return recordIncludeExecutor; } }

        int reference_count = 0;
        public int ReferenceCount { get { return reference_count; } }

        Stack<List<String>> recordTmpVariable = new Stack<List<string>>();
        Dictionary<String, Stack<ExtrameFunctionCalculator.Types.Variable>> TmpVariable = new Dictionary<string, Stack<ExtrameFunctionCalculator.Types.Variable>>();

        Parser parser;
        public Parser RefParser { get { return parser; } }

        private static Dictionary<String, Parser.ExecutorAction> preprocessActionMap = null;

        static Executor()
        {
            preprocessActionMap = new Dictionary<string, Parser.ExecutorAction>();
            preprocessActionMap.Add("package", (param, reference_parser) =>
            {
                reference_parser.Set("package_name", param);
            }
        );
            preprocessActionMap.Add("version", (param, reference_parser) =>
            {
                reference_parser.Set("package_version", param);
            }
        );
            preprocessActionMap.Add("include", (param, reference_parser) =>
            {
                try
                {
                    if (IsAbsolutePath(param))
                        if (param.Length < 3)
                            throw new Exception();
                        else
                            param = GetBackFolderPath(reference_parser.RefExecutor.IncludeFilePath) + param.Substring(2);
                    reference_parser.RefExecutor.AddChildExecutor(param);
                }
                catch (Exception e)
                {
                    Log.Error(String.Format("cant open file %s", param));
                }
            });
            preprocessActionMap.Add("define", (param, reference_parser) =>
            {
                //// TODO: 2016/11/4 实现define

            });
        }

        public Executor(Calculator calculator)
        {
            this.calculator = calculator;
            parser = new Parser(this);
        }

        public string IncludeFilePath { get { return include_file_path; } }

        static bool IsAbsolutePath(string path)
        {
            if (path.Length == 0)
                return false;
            if (path[(0)] == '.' && path[(1)] == '/')
                return true;
            return false;
        }

        static string GetBackFolderPath(string path)
        {
            if (path.Contains("\\"))
            {
                for (int i = path.Length - 2; i >= 0; i--)
                    if (path[(i)] == '\\')
                        return path.Substring(0, i + 1);
            }
            return "";
        }

        #region Load Files

        public void InitFromFile(string input_file)
        {
            List<string> arrayList = new List<string>(System.IO.File.ReadAllLines(input_file));

            include_file_path = input_file;

            parser.SetPreCompileExecutors(preprocessActionMap);
            parser.Parse(arrayList);
            parser.FunctionRegister();

            if (!(parser.Propherty.ContainsKey("package_name") && parser.Propherty.ContainsKey("package_version")))
                throw new Exception(String.Format("the file from \"{0}\" is missed pre-propherty \"#package xxx\" or \"#version xxx\"", input_file));
            return;
        }

        #endregion

        public string GetPackageName => parser.Propherty[("package_name")];

        public string GetPackageVersion() => parser.Propherty[("package_version")];

        public int GetCurrentExecutorFunctionCount() => parser.FunctionTable.Count;

        public int GetAllExecutorFunctionCount()
        {
            int count = 0;
            count = GetCurrentExecutorFunctionCount();
            foreach (Executor executor in recordIncludeExecutor)
            {
                count += executor.GetAllExecutorFunctionCount();
            }
            return count;
        }

        public List<string> GetAllFunctionName()
        {
            List<string> allFunction = new List<string>();
            foreach (var pair in parser.FunctionTable)
                allFunction.Add(pair.Key);
            return allFunction;
        }

        #region Singal

        public abstract class Singal : Exception
        {
            protected string value;
            public Singal(string value)
            {
                this.value = value;
            }
        }

        class ReturnSignal : Singal
        {
            public string ReturnValue { get { return value; } }

            public ReturnSignal(string expr) : base(expr)
            {
            }
        }

        class EndfunctionSignal : Singal
        {
            public EndfunctionSignal() : base(null)
            {

            }
        }

        #endregion

        public string ExecuteFunction(string name, List<ExtrameFunctionCalculator.Types.Expression> paramster)
        {
            if (!parser.FunctionTable.ContainsKey(name))
            {
                foreach (Executor executor in recordIncludeExecutor)
                    if (executor.parser.FunctionTable.ContainsKey(name))
                        return executor.ExecuteFunction(name, paramster);
            }
            Function function = parser.FunctionTable[(name)];
            if (paramster.Count != function.ParameterRequestCount)
                throw new Exception("not enough paramester to take");
            Dictionary<String, ExtrameFunctionCalculator.Types.Variable> param_set = new Dictionary<string, ExtrameFunctionCalculator.Types.Variable>();
            foreach (ExtrameFunctionCalculator.Types.Expression expr in paramster)
                if (expr.ExpressionType == ExtrameFunctionCalculator.Types.ExpressionType.Variable)
                    param_set.Add(((ExtrameFunctionCalculator.Types.Variable)expr).GetName(), (ExtrameFunctionCalculator.Types.Variable)expr);
            //参数入栈保存
            PushTmpVariable(param_set);
            //开始执行
            Unit unit = null;
            try
            {
                GetCalculator().GetScriptManager().RecordExecutingExecutor(this);
                int position = function.Line;
                while (true)
                {
                    position++;
                    if (position >= parser.StatementLine.Count)
                        throw new Exception("Current execute command is out of function range");
                    unit = parser.StatementLine[(position)];
                    switch (unit.UnitType)
                    {
                        case UnitType.Statement:
                            {
                                switch (((Statement)unit).StatementType)
                                {
                                    case StatementType.Function:
                                        {
                                            if (((Function)unit).FunctionType == FunctionType.End)
                                            {
                                                //position==(((Parser.Statement.Function.EndFcuntion)unit).line;
                                                if ((((EndFunction)unit).EndLine) == function.EndLine)
                                                    throw new EndfunctionSignal();
                                                throw new Exception("Different function end.");
                                            }

                                            throw new Exception("Miaomiao?");

                                        }
                                    case StatementType.Return:
                                        {
                                            throw new ReturnSignal(((Statement)unit).StatementContext);
                                        }
                                    case StatementType.Call:
                                        {
                                            GetCalculator().ScriptCallExecute(((Statement)unit).StatementContext);
                                            break;
                                        }
                                    case StatementType.Set:
                                        {
                                            SetVariableValue(((Set)unit).VariableName, ((Set)unit).VariableValue);
                                            break;
                                        }
                                    case StatementType.Goto:
                                        {
                                            position = int.Parse((((Statement)unit).StatementContext));
                                            break;
                                        }
                                }
                                break;
                            }
                        case UnitType.Symbol:
                            {
                                switch (((Symbol)unit).SymbolType)
                                {
                                    case SymbolType.ConditionBranch:
                                        {
                                            switch (((Condition)unit).ConditionType)
                                            {
                                                case ConditionType.If:
                                                    {
                                                        BooleanCalculator booleanCaculator = new BooleanCalculator(calculator);
                                                        if (!booleanCaculator.Solve(((If)unit).ConditionExpression))
                                                        {
                                                            position = ((If)unit).ElseLine < 0 ? ((If)unit).EndLine : ((If)unit).ElseLine;
                                                        }
                                                        break;
                                                    }
                                            }
                                            break;
                                        }
                                    case SymbolType.LoopBranch:
                                        {
                                            switch (((Loop)unit).LoopType)
                                            {
                                                case LoopType.Continue:
                                                case LoopType.Endloop:
                                                    position = ((Loop)unit).ReferenceLoop.Line;
                                                    break;

                                                case LoopType.Break:
                                                    position = ((Loop)unit).ReferenceLoop.EndLine;
                                                    break;
                                            }
                                            break;
                                        }
                                }
                                break;
                            }
                    }
                    {
                        continue;
                    }
                }
            }
            catch (ReturnSignal e) { return GetCalculator()._Solve(e.ReturnValue); }
            //catch (Exception e) { throw e; }
            finally
            {
                PopTmpVariable();
                GetCalculator().GetScriptManager().RecoveredExecutingExecutor();
            }
        }

        #region Variables

        private void PushTmpVariable(Dictionary<string, ExtrameFunctionCalculator.Types.Variable> variableHashMap)
        {
            List<string> recordList = new List<string>();
            foreach (var pair in variableHashMap)
            {
                recordList.Add(pair.Key);
                if (!TmpVariable.ContainsKey(pair.Key))
                    TmpVariable.Add(pair.Key, new Stack<ExtrameFunctionCalculator.Types.Variable>());
                TmpVariable[(pair.Key)].Push(pair.Value);
            }
            recordTmpVariable.Push(recordList);
        }

        private void PopTmpVariable()
        {
            List<string> recordList = recordTmpVariable.Pop();
            foreach (string tmp_name in recordList)
            {
                TmpVariable[(tmp_name)].Pop();
                if (TmpVariable[(tmp_name)].Count == 0)
                    TmpVariable.Remove(tmp_name);
            }
        }

        private ExtrameFunctionCalculator.Types.Variable GetTmpVariable(string name)
        {
            if (TmpVariable.ContainsKey(name))
                return TmpVariable[(name)].Peek();
            return null;//todo
        }

        /*
            查询变量的顺序,低到高

            lv1 :当前代码文件区域
            lv2 :GetCalculator::GetVariable() -> lv3 :计算器本体区域
                                                 lv4 :脚本遍历查询 -> lv5 :脚本本地代码区域(RequestVariable()) -> lv6 : 引用(include)的子脚本遍历查询 ..... 
        
         */

        public bool isTmpVariable(string variable_name) => TmpVariable.ContainsKey(variable_name);

        public ExtrameFunctionCalculator.Types.Variable GetVariable(string name)
        {
            if (isTmpVariable(name))
                return TmpVariable[(name)].Peek();
            return null;
        }

        public void SetVariableValue(string name, string Value)
        {
            ExtrameFunctionCalculator.Types.Variable variable = GetVariable(name);

            if (variable == null)
            {
                RegisterTmpVariable(name);
                TmpVariable[(name)].Push(new ExtrameFunctionCalculator.Types.Variable(name, GetCalculator()._Solve(Value), GetCalculator()));
                return;
            }

            variable.RawText = GetCalculator()._Solve(Value);
        }

        public void RegisterTmpVariable(string name)
        {
            recordTmpVariable.Peek().Add(name);
            TmpVariable.Add(name, new Stack<ExtrameFunctionCalculator.Types.Variable>());
        }

        #endregion

        #region Child Executors

        public void AddChildExecutor(string input_file)
        {
            Executor executor = new Executor(GetCalculator());
            executor.InitFromFile(input_file);
            recordIncludeExecutor.Add(executor);
            GetCalculator().GetScriptManager().LoadScript(executor);
        }

        internal void Link() => reference_count++;

        internal void Drop() => reference_count--;

        internal bool IsNonReferenced() => reference_count <= 0;
        #endregion
    }
}
