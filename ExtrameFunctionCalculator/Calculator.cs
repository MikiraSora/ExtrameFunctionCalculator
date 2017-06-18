using ExtrameFunctionCalculator.Script;
using ExtrameFunctionCalculator.Types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator
{
    public class Calculator
    {
        private Dictionary<String, Function> function_table = new Dictionary<string, Function>();
        private Dictionary<String, Variable> variable_table = new Dictionary<string, Variable>();
        private static Dictionary<String, ReflectionFunction> raw_function_table = new Dictionary<string, ReflectionFunction>();

        private static Dictionary<String, Variable> raw_variable_table = new Dictionary<string, Variable>();

        static String specialOperationChar = " + - * / [ ] ~ ! @ # $ % ^ & ( ) ; : \" | ? > < , ` ' \\ ";

        private CalculatorOptimizer calculatorOptimizer = null;

        public Calculator()
        {
            Init();
            calculatorOptimizer = new CalculatorOptimizer(this);
            scriptManager = new ScriptManager(this);
        }

        public enum EnableType
        {
            FunctionStaticParse,
            ExpressionOptimize,
            PrecisionTruncation,
            ScriptFunctionCache
        }


        #region EnableWrappers

        public void Enable(EnableType enableType)
        {
            switch (enableType)
            {
                case EnableType.ExpressionOptimize:
                    OptimizeEnable(true);
                    break;
                case EnableType.PrecisionTruncation:
                    Digit.IsIsPrecisionTruncation = true;
                    break;
                case EnableType.ScriptFunctionCache:
                    GetScriptManager().SetCacheReferenceFunction(true);
                    break;
            }
            Log.Debug(String.Format("开启功能:{0}", enableType.ToString()));
        }

        void OptimizeEnable(bool sw)
        {
            calculatorOptimizer.Enable = sw;
        }

        private String Optimize(String sw)
        {
            String level = sw.Substring(sw.IndexOf(" ")).Replace(" ", "");
            sw = sw.Substring(0, sw.IndexOf(" ")).Replace(" ", "");
            switch (sw)
            {
                case "true":
                    {
                        Enable(EnableType.ExpressionOptimize);
                        calculatorOptimizer.OptimizeLevel = (int.Parse(level));
                        return "Optimized open.";
                    }
                case "false":
                    {
                        DisEnable(EnableType.ExpressionOptimize);
                        return "Optimized close.";
                    }
            }
            Log.ExceptionError(new Exception("unknown command"));
            return "unknown command";
        }

        public void DisEnable(EnableType enableType)
        {
            switch (enableType)
            {
                case EnableType.ExpressionOptimize:
                    OptimizeEnable(false);
                    break;
                case EnableType.PrecisionTruncation:
                    Digit.IsIsPrecisionTruncation = false;
                    break;
                case EnableType.ScriptFunctionCache:
                    GetScriptManager().SetCacheReferenceFunction(false);
                    break;
            }
            Log.Debug(String.Format("关闭功能:{0}", enableType.ToString()));
        }

        #endregion

        internal Function GetFunction(String name)
        {
            if (raw_function_table.ContainsKey(name))
            {
                Function function = raw_function_table[(name)];
                function.BindCalculator(this);
                return function;
            }
            if (function_table.ContainsKey(name))
                return function_table[(name)];
            return GetScriptManager().RequestFunction(name);
            //return function_table.get(name);
        }

        #region 变量

        Stack<List<String>> recordTmpVariable = new Stack<List<string>>();
        Dictionary<String, Stack<Variable>> TmpVariable = new Dictionary<string, Stack<Variable>>();

        private Variable GetTmpVariable(String name)
        {
            if (TmpVariable.ContainsKey(name))
                return TmpVariable[(name)].Peek();
            return null;
        }

        internal void PopTmpVariable()
        {
            List<String> recordList = recordTmpVariable.Pop();
            foreach (String tmp_name in recordList)
            {
                TmpVariable[(tmp_name)].Pop();
                //Log.Debug(String.format("tmp variable \"%s\" was pop",TmpVariable.get(tmp_name).pop().toString()));
                if (TmpVariable[(tmp_name)].Count == 0)
                    TmpVariable.Remove(tmp_name);
            }
            Log.Debug(String.Format("there are {0} tmp variables are popped in {1} layout", recordList.Count, recordTmpVariable.Count + 1));
        }

        internal void PushTmpVariable(Dictionary<String, Variable> variableHashMap)
        {
            List<String> recordList = new List<string>();
            foreach (var pair in variableHashMap)
            {
                recordList.Add(pair.Key);
                if (!TmpVariable.ContainsKey(pair.Key))
                    TmpVariable.Add(pair.Key, new Stack<Variable>());
                TmpVariable[(pair.Key)].Push(pair.Value);
                //Log.Debug(String.format("tmp variable \"%s\" was push",pair.getValue().toString()));
            }
            recordTmpVariable.Push(recordList);
            Log.Debug(String.Format("there are {0} tmp variables are pushed in {1} layout", recordList.Count, recordTmpVariable.Count));
        }

        internal Variable GetVariable(String name)
        {
            Variable tmp_variable = GetTmpVariable(name);
            if (tmp_variable != null)
                return tmp_variable;
            if (raw_variable_table.ContainsKey(name))
            {
                return raw_variable_table[(name)];
            }
            if (variable_table.ContainsKey(name))
                return variable_table[(name)];
            tmp_variable = GetScriptManager().RequestVariable(name, null);
            if (tmp_variable != null)
                return tmp_variable;
            //Log.Warning( new VariableNotFoundException(name).getMessage());
            return null;
        }

        #endregion

        #region 脚本

        public String LoadScriptFile(String file_path)
        {
            /*
            try
            {*/
                GetScriptManager().LoadScript(file_path);
           /* }
            catch (Exception e)
            {
                Log.Error(e.Message);
                return "loaded scriptfile failed!";
            }*/
            return "loaded scriptfile successfully!";
        }

        public String UnloadScriptFile(String package_name)
        {
            try
            {
                GetScriptManager().UnloadScript(package_name);
            }
            catch (Exception e)
            {
                return "unloaded scriptfile failed!";
            }
            return "unloaded scriptfile successfully!";
        }

        private ScriptManager scriptManager;

        public ScriptManager GetScriptManager() { return scriptManager; }

        #endregion

        public bool ContainFunction(String name)
        {
            if (raw_function_table.ContainsKey(name))
                return true;
            if (function_table.ContainsKey(name))
                return true;
            if (GetScriptManager().ContainFunction(name))
                return true;
            return false;
        }

        public static void RegisterRawVariable(Variable variable) => raw_variable_table[variable.GetName()] = variable;


        public static void RegisterRawFunction(String expression, OnReflectionFunction reflectionFunction)
        {
            try
            {
                ReflectionFunction function = new ReflectionFunction(expression, reflectionFunction);
                raw_function_table.Add(function.GetName(), function);
            }
            catch (Exception e)
            {
                Log.Warning(e.Message);
            }
        }

        void RegisterReflectionFunction(String expression, OnReflectionFunction onReflectionFunction)
        {
            ReflectionFunction reflectionFunction = new ReflectionFunction(expression, onReflectionFunction);
            RegisterFunction(reflectionFunction);
        }

        private String Clear()
        {
            /*
            if (!(BSEChain_Stack.Count == 0))
                this.getBSEChain_Stack().Clear();
            if (!(rawExpressionChain_Stack.Count == 0))
                this.getRawExpressionChain_Stack().Clear();
                */
            return "Clean finished!";
        }

        public String Reset()
        {
            Clear();
            this.variable_table.Clear();
            this.function_table.Clear();
            return "Reset finished!";
        }

        private void SetFunction(String expression)
        {
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            Function function = new Function(expression, this);
            RegisterFunction(function);
        }

        private void RegisterFunction(Function function)
        {
            Log.Debug(function.ToString());
            function_table[function.GetName()]= function;
        }

        private void RegisterVariable(Variable variable)
        {
            Log.Debug(variable.GetName());
            variable_table.Add(variable.GetName(), variable);
        }

        internal String RequestVariable(String name)
        {
            if (!variable_table.ContainsKey(name))
                Log.ExceptionError(new Exception($"Variable {name} not found"));
            return variable_table[(name)].Solve();
        }

        List<Expression> ParseExpression(String expression)
        {
            List<Expression> expressionArrayList = new List<Expression>();
            int position = 0;
            char c, tmp_c;
            Expression expr = null;
            String statement = "", tmp_op;
            Stack<int> bracket_stack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    break;
                c = expression[(position)];
                if (specialOperationChar.Contains($" {c} "))
                {
                    if ((!(statement.Length == 0)) && (c == '('))
                    {
                        //Function Parser
                        bracket_stack.Clear();
                        while (true)
                        {
                            if (position >= expression.Length)
                                break;
                            c = expression[(position)];
                            if (c == '(')
                            {
                                //判断是否有无限循环小数格式的可能
                                if (!Utils.isDigit(statement))
                                {
                                    bracket_stack.Push(position);
                                    statement += c;
                                }
                                else
                                {
                                    //无限循环小数格式
                                    int size = 0;
                                    while (true)
                                    {
                                        statement += c;
                                        if (c == ')')
                                            break;
                                        size++;
                                        c = expression[(++position)];
                                    }
                                    expressionArrayList.Add(new ExpressionVariable("", Utils.RepeatingDecimalCoverToExpression(statement), this));
                                    statement = "";
                                    break;
                                }
                            }
                            else
                            {
                                if (c == ')')
                                {
                                    if (bracket_stack.Count == 0)
                                        Log.ExceptionError(new Exception("Extra brackets in position: " + position));
                                    bracket_stack.Pop();
                                    if (bracket_stack.Count == 0)
                                    {
                                        statement += ")";
                                        expressionArrayList.Add(checkConverExpression(statement));//should always return Function
                                        break;
                                    }
                                    //statement += c;
                                } /*else {
                                //statement += c;
                            }*/
                                statement += c;
                            }
                            position++;
                        }
                    }
                    else if ((!(statement.Length == 0)) && c == '[')
                    {
                        //array
                        char tmp_ch = (char)0;
                        //position--;
                        //读取下标
                        String indexes = "";
                        Stack<int> balanceStack = new Stack<int>();

                        while (true)
                        {
                            if (position >= expression.Length)
                                Log.Error(String.Format("{0} isnt vaild format", expression));
                            tmp_ch = expression[(position)];
                            indexes += tmp_ch;
                            position++;
                            if (tmp_ch == '[')
                            {
                                balanceStack.Push(position);
                                continue;
                            }
                            if (tmp_ch == ']')
                            {
                                if (position >= expression.Length)
                                    break;
                                balanceStack.Pop();
                                if (balanceStack.Count == 0)
                                {
                                    tmp_ch = expression[(position)];
                                    if (tmp_ch != '[')
                                        break;
                                }
                            }
                        }
                        Variable variable = GetVariable(statement);
                        if (variable == null)
                            Log.ExceptionError(new Exception($"variable {statement} is not found"));
                        if (variable.VariableType != VariableType.MapVariable)
                            Log.ExceptionError(new Exception(String.Format("{0} isnt MapVariable", statement)));
                        ((MapVariable)variable).SetIndexes(indexes);
                        expressionArrayList.Add(new WrapperVariable(variable, indexes));
                        position--;
                    }
                    else
                    {
                        expr = checkConverExpression(statement);
                        if (expr != null)
                            expressionArrayList.Add(expr);
                        tmp_op = c.ToString();
                        {
                            if (position < (expression.Length - 1))
                            {
                                tmp_c = expression[(position + 1)];
                                tmp_op += tmp_c;
                                if (!specialOperationChar.Contains(" " + (tmp_op) + " "))
                                {
                                    tmp_op = c.ToString();
                                }
                            }
                        }
                        expressionArrayList.Add(new Symbol(tmp_op,this));
                    }
                    //Reflush statement
                    statement = "";
                }
                else
                {
                    statement += c;
                }
                position++;
            }
            if (!(statement.Length == 0))
                expressionArrayList.Add(checkConverExpression(statement));
            return expressionArrayList;
        }

        static Regex checkFunctionFormatRegex = new Regex("([a-zA-Z]\\w*)\\((.*)\\)");

        private Expression checkConverExpression(string text)
        {
            if (Utils.isFunction(text))
            {
                //Get function name
                Match result = checkFunctionFormatRegex.Match(text);
                if (result.Groups.Count != 3)
                    Log.ExceptionError(new Exception("Cannot parse function ：" + text));
                String function_name = result.Groups[(1)].Value;
                String function_paramters = result.Groups[(2)].Value;
                if (!ContainFunction(function_name))
                    Log.ExceptionError(new Exception(String.Format("function {0} hadnt declared!", function_name)));
                WrapperFunction function = new WrapperFunction(GetFunction(function_name), function_paramters);
                return function;
                //Get function paramater list
            }

            if (Utils.isDigit(text))
            {
                return new Digit(text);
            }

            if (Utils.isValidVariable(text))
            {
                Variable variable = GetVariable(text);
                if (variable == null)
                    Log.ExceptionError(new Exception($"Variable {text} is not found"));
                //因为MapVariable并不在此处理所以为了减少引用调用所以不用new WrapperVariable;
                return variable;
            }

            return null;
        }

        public void SetExpressionVariable(string expression)
        {
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            char c;
            string variable_name = "", variable_expression = "";
            Variable variable = null;
            for (int position = 0; position < expression.Length - 1; position++)
            {
                c = expression[(position)];
                if (c == '=')
                {
                    variable_expression = expression.Substring(position + 1);
                    break;
                }
                else
                {
                    variable_name += c;
                }
            }
            variable = GetVariable(variable_name);
            if (variable == null)
            {
                variable = new ExpressionVariable(variable_name, null, this);
                RegisterVariable(variable);
            }
            else if (variable.VariableType != VariableType.ExpressionVariable)
                Log.ExceptionError(new Exception(String.Format("{0} is exsited and not a ExpressionVariable.", variable_name)));
            ((ExpressionVariable)variable).SetValue(variable_expression);
        }

        /*
        private void setBSEChain_Stack(List<Expression> list)
        {
            BSEChain_Stack.Push(list);
        }
        

        private void setRawExpressionChain_Stack(List<Expression> list) { rawExpressionChain_Stack.Push(list); }

        */
        /*
        private void Term_Solve()
        {
            BSEChain_Stack.Pop();
            rawExpressionChain_Stack.Pop();
        }
        */
        private void CheckNormalizeChain(ref List<Expression> expression_list)
        {
            foreach (Expression node in expression_list)
            {
                if (node.ExpressionType != ExpressionType.Digit && node.ExpressionType != ExpressionType.Symbol)
                    Log.ExceptionError(new Exception(node.GetName() + " isnt digit or symbol."));
            }
        }

        private void ConverVariableToDigit(ref List<Expression> expression_list)
        {
            int position = 0;
            Expression node;
            Variable variable;
            Digit result;
            try
            {
                for (position = 0; position < expression_list.Count; position++)
                {
                    node = expression_list[(position)];
                    if (node.ExpressionType == ExpressionType.Variable)
                    {
                        variable = (Variable)node;
                        result = variable.GetDigit();
                        expression_list.RemoveAt(position);
                        expression_list.Insert(position, result);
                    }
                }
            }
            catch (Exception e)
            {
                Log.Warning(e.Message);
            }
        }

        private void ConverFunctionToDigit(ref List<Expression> expression_list)
        {
            int position = 0;
            Expression node;
            Function function;
            Digit result;
            for (position = 0; position < expression_list.Count; position++)
            {
                node = expression_list[(position)];
                if (node.ExpressionType == ExpressionType.Function)
                {
                    function = (Function)node;
                    if (function.FunctionType == FunctionType.ReflectionFunction)
                        function.Calculator = (this);
                    result = function.GetSolveToDigit();
                    expression_list.RemoveAt(position);
                    expression_list.Insert(position, result);
                }
            }
        }

        public String Solve(String expression)
        {
            if (Utils.isDigit(expression))
                return expression;
            List<Expression> expression_list=(ParseExpression(expression));
            ConverVariableToDigit(ref expression_list);
            ConverFunctionToDigit(ref expression_list);
            CheckNormalizeChain(ref expression_list);//// TODO: 2016/10/2 此方法存在争议，暂时保留
            expression_list=ExpressionOptimization(expression_list);
            ConverToBSE(ref expression_list);
            String result = ExucuteBSE(expression_list);

            if (result.Contains("."))
            {
                String tmpDecial = result.Substring(result.IndexOf('.') + 1);
                try
                {
                    if (int.Parse(tmpDecial) == (0))
                        return result.Substring(0, result.IndexOf('.'));
                }
                catch (Exception e) { }
            }
            return result;
        }

        private List<Expression> ExpressionOptimization(List<Expression> expression_list)
        {
            List<Expression> optimizeResult = calculatorOptimizer.OptimizeExpression(expression_list);
            return optimizeResult == null ? expression_list : optimizeResult;
        }

        internal void ConverToBSE(ref List<Expression> expressionArrayList)
        {
            if (expressionArrayList[(expressionArrayList.Count - 1)].ExpressionType == ExpressionType.Symbol ? !((/*(Symbol) */expressionArrayList[(expressionArrayList.Count - 1)]).RawText == (")")) : false)
                Log.ExceptionError(new Exception("the last expression in list cannot be symbol"));
            List<Expression> result_list = new List<Expression>();
            Stack<Symbol> operation_stack = new Stack<Symbol>();
            Symbol symbol = null;
            Expression node = null;
            for (int position = 0; position < expressionArrayList.Count; position++)
            {
                node = expressionArrayList[(position)];
                if (node.ExpressionType == ExpressionType.Digit)
                    result_list.Add(node);
                else
                {
                    if (((Symbol)node).RawText == ("-"))
                    {
                        if (position == 0)
                        {
                            result_list.Add(new Digit("0"));
                        }
                        else if (expressionArrayList[(position - 1)].ExpressionType == ExpressionType.Symbol)
                            if (((Symbol)expressionArrayList[(position - 1)]).RawText == ("("))
                                result_list.Add(new Digit("0"));
                    }
                    if (operation_stack.Count == 0)
                        operation_stack.Push((Symbol)node);
                    else
                    {
                        if (!(((Symbol)node).RawText == (")")))
                        {
                            symbol = operation_stack.Peek();
                            while (symbol != null)
                            {
                                if (!(symbol.RawText == ("(")))
                                    if (symbol.CompareOperationPrioty((Symbol)node) >= 0)
                                    {
                                        result_list.Add(operation_stack.Pop());
                                        symbol = operation_stack.Count != 0 ? operation_stack.Peek() : null;
                                        continue;
                                    }
                                break;
                            }
                            operation_stack.Push((Symbol)node);
                        }
                        else
                        {
                            symbol = operation_stack.Peek();
                            while (true)
                            {
                                if (operation_stack.Count == 0)
                                    Log.ExceptionError(new Exception("喵喵喵?"));
                                if ((symbol.RawText == ("(")))
                                {
                                    operation_stack.Pop();
                                    break;
                                }
                                result_list.Add(operation_stack.Pop());
                                symbol = operation_stack.Peek();
                            }
                        }
                    }
                }
            }
            while (!(operation_stack.Count == 0))
            {
                result_list.Add(operation_stack.Pop());
            }
            for (int i = 0; i < result_list.Count; i++)
            {
                node = result_list[(i)];
                if (node.ExpressionType == ExpressionType.Symbol)
                    if (((Symbol)node).RawText == ("("))
                        result_list.Remove(node);
            }

            expressionArrayList.Clear();
            expressionArrayList.AddRange(result_list);
        }

        private string ExucuteBSE(List<Expression> expression_list)
        {
            if (expression_list.Count == 1)
                if (expression_list[(0)].ExpressionType == ExpressionType.Digit)
                    return ((((Digit)expression_list[(0)]).GetDouble())).ToString();
            Stack<Expression> digit_stack = new Stack<Expression>();
            Symbol op;
            Expression node = null;
            List<Expression> executeList = expression_list;
            List<Expression> paramterList, result;
            try
            {
                for (int position = 0; position < executeList.Count; position++)
                {
                    node = executeList[(position)];
                    if (node.ExpressionType == ExpressionType.Symbol)
                    {
                        op = (Symbol)node;
                        paramterList = new List<Expression>();
                        for (int i = 0; i < op.GetParamterCount(); i++)
                        {
                            if (digit_stack.Count == 0)
                            {
                                if (op.RawText == ("-") && i != 0)
                                {
                                    paramterList.Add(new Digit("0"));
                                }
                                else
                                {
                                    Log.ExceptionError(new Exception("expression synatx error!because not enough calculatable type could solve with current operator \"" + op.RawText + "\""));
                                }
                            }
                            else
                            {
                                paramterList.Add(digit_stack.Pop());
                            }
                        }
                        //paramterList.add(digit_stack.isEmpty() ? (?new Digit("0")): digit_stack.pop());
                        //Collections.reverse(paramterList);
                        paramterList.Reverse();
                        result = op.Solve(paramterList, this);
                        foreach (Expression expr in result)
                            digit_stack.Push(expr);
                    }
                    else
                    {
                        if (node.ExpressionType == ExpressionType.Digit)
                        {
                            digit_stack.Push((Digit)node);
                        }
                        else
                            Log.ExceptionError(new Exception("Unknown Node"));
                    }
                }
            }
            catch (Exception e)
            {
                Log.ExceptionError(new Exception(e.Message));
            }
            Expression resultExpr = digit_stack.Pop();
            return (resultExpr.ExpressionType == ExpressionType.Digit) ? (((Digit)resultExpr).GetDouble()).ToString() : resultExpr.Solve();
        }

        #region Execute

        public String Execute(String text)
        {
            Clear();
            return ExecuteEx(text);
        }

        internal string ScriptCallExecute(String text)
        {
            return ExecuteEx(text);
        }

        private void SetVariable(String expression)
        {
            char c;
            String variable_name = "", variable_expression = "";
            for (int position = 0; position < expression.Length - 1; position++)
            {
                c = expression[(position)];
                if (c == '=')
                {
                    variable_expression = expression.Substring(position + 1);
                    break;
                }
                else
                {
                    variable_name += c;
                }
            }
            Variable variable = GetVariable(variable_name);
            if (variable == null)
            {
                variable = new Variable(variable_name, null, this);
                RegisterVariable(variable);
            }
            variable.SetValue(variable_expression);
        }

        private void SetMapVariable(String expression)
        {
            //mymapvar[myvar]["mykeyname"][5]=myvalue
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            int position = 0;
            char c = (char)0;
            //读取名字
            String variable_name = "";
            while (true)
            {
                if (position >= expression.Length)
                    Log.Error(String.Format("%s isnt vaild format", expression));
                c = expression[(position)];
                if (c == '[')
                    break;
                variable_name += c;
                position++;
            }
            //读取下标
            String indexes = "";
            Stack<int> balanceStack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    Log.Error(String.Format("{0} isnt vaild format", expression));
                c = expression[(position)];
                indexes += c;
                position++;
                if (c == '[')
                {
                    balanceStack.Push(position);
                    continue;
                }
                if (c == ']')
                {
                    balanceStack.Pop();
                    if (balanceStack.Count == 0)
                    {
                        c = expression[(position)];
                        if (c == '=')
                            break;
                    }
                }
            }
            //读取右值表达式
            String variable_expr = expression.Substring(++position);
            //// TODO: 2016/11/26
            Variable variable = GetVariable(variable_name);
            if (variable == null)
            {
                variable = new MapVariable(variable_name, indexes, Solve(variable_expr), this);
                RegisterVariable(variable);
            }
            else
            if (variable.VariableType != VariableType.MapVariable)
                Log.Error(String.Format("{0} isnt MapVariable", variable_name));
            else
                ((MapVariable)variable).SetValue(indexes, Solve(variable_expr));

        }

        private String ExecuteEx(String text)
        {
            Log.Debug(String.Format("Try Execute : {0}", text));
            //Clear(); //// TODO: 2016/11/22 此处存在和脚本语言的Call部分功能起到致命冲突，stdmath2::getCall().
            if (text.Length == 0)
                Log.ExceptionError(new Exception("empty text to execute"));
            char c;
            String executeType = "", paramter = "";
            String result = "";

            for (int position = 0; position < text.Length; position++)
            {
                c = text[(position)];
                if (c == ' ')
                {
                    paramter = text.Substring(position + 1);
                    break;
                }
                else
                {
                    executeType += c;
                }
            }
            switch (executeType)
            {
                case "set":
                    {
                        SetVariable(paramter);
                        break;
                    }
                case "set_expr":
                    {
                        SetExpressionVariable(paramter);
                        break;
                    }
                case "reg":
                    {
                        SetFunction(paramter);
                        break;
                    }
                case "solve":
                    {
                        result = Solve(paramter);
                        break;
                    }
                /*
                case "dump":
                {
                    result = DumpInfo(paramter);
                    break;
                }*/
                case "set_map":
                    {
                        SetMapVariable(paramter);
                        break;
                    }
                case "log":
                    {
                        Log.User(paramter);
                        result = "";
                        break;
                    }
                /*
            case "test":
                {
                    result = DumpInfo(paramter);
                    break;
                }*/
                case "load_script":
                    {
                        result = LoadScriptFile(paramter);
                        break;
                    }
                /*
            case "help":
                {
                    result = CalculatorHelper.GetHelp();
                    break;
                }*/
                /*
            case "load":
                {
                    result = Load(paramter);
                    break;
                }
                */
                case "unload_script":
                    {
                        result = UnloadScriptFile(paramter);
                        break;
                    }
                case "clear":
                    {
                        result = Clear();
                        break;
                    }
                case "reset":
                    {
                        result = Reset();
                        break;
                    }
                /*
            case "reg_df":
                {
                    result = new DerivativeParser(this).Solve(paramter, "x");
                    break;
                }
                */
                /*
            case "save":
                {
                    String type = "", output_path = "";
                    for (int position = 0; position < paramter.length(); position++)
                    {
                        c = paramter.charAt(position);
                        if (c == ' ')
                        {
                            output_path = paramter.substring(position + 1);
                            break;
                        }
                        else
                        {
                            type += c;
                        }
                    }
                    result = Save(type, output_path);
                    break;
                }
                */
                /*被DisEnable()/Enable()代替
                case "optimize":{
                    result=Optimize(paramter);
                    break;
                }*/
                /*
                case "delete":
                    {
                        String type = "", name = "";
                        for (int position = 0; position < paramter.Length; position++)
                        {
                            c = paramter[(position)];
                            if (c == ' ')
                            {
                                name = paramter.Substring(position + 1);
                                break;
                            }
                            else
                            {
                                type += c;
                            }
                        }
                        result = Delete(type, name);
                        break;
                    }
                    */
                default:
                    {
                        Log.ExceptionError(new Exception(String.Format("unknown command \"{0}\"", executeType)));
                        break;
                    }
            }
            //Clear();//// TODO: 2016/11/22 尝试放到这里 ,....看来不行
            return result;
        }
        #endregion

        #region Operators

        Symbol GetSymbolMayFromCache(String op) {
            if(!SharedSymbolCache.ContainsKey(op))
                SharedSymbolCache[(op)]=new Symbol(op,this);
            return SharedSymbolCache[(op)];
        }


        public delegate List<Expression> OnCalculateFunc(List<Expression> parametersList, Calculator refCalculator);

        internal Dictionary<string, OnCalculateFunc> OperatorFunction = new Dictionary<string, OnCalculateFunc>();

        internal Dictionary<string, float> OperatorPrioty = new Dictionary<string, float>();

        internal Dictionary<string, Symbol> SharedSymbolCache = new Dictionary<string, Symbol>();

        internal Dictionary<string, int> OperatorRequestParamterCount = new Dictionary<string, int>();

        public void RegisterOperation(String operatorSymbol, int requestParamterSize, float operatorPrioty, OnCalculateFunc operatorFunction)
        {
            OperatorFunction[operatorSymbol] = operatorFunction;
            OperatorPrioty[operatorSymbol] = operatorPrioty;
            OperatorRequestParamterCount[operatorSymbol]=requestParamterSize;
            SharedSymbolCache[operatorSymbol]=GetSymbolMayFromCache(operatorSymbol);
        }

        #endregion

        #region Init

        static Calculator()
        {
            Log.SetIsThreadCommitLog(true);
        }

        void Init()
        {
            #region 基本操作符

            RegisterOperation("+", 2, 6.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(new Digit((a.GetDouble() + b.GetDouble()).ToString()));
                return result;
            });

            RegisterOperation("-", 2, 6.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(new Digit((a.GetDouble() - b.GetDouble()).ToString()));
                return result;
            });

            RegisterOperation("*", 2, 9.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(new Digit((a.GetDouble() * b.GetDouble()).ToString()));
                return result;
            });

            RegisterOperation("/", 2, 9.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(new Digit((a.GetDouble() / b.GetDouble()).ToString()));
                return result;
            });

            RegisterOperation("^", 2, 12.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(new Digit(Math.Pow(a.GetDouble() , b.GetDouble()).ToString()));
                return result;
            });

            RegisterOperation("(", 2, 99.0f, (paramsList, calculator) => {
                return null;
            });

            RegisterOperation(")", 2, 99.0f, (paramsList, calculator) => {
                return null;
            });

            #endregion

            #region 单参数函数

            Calculator.RegisterRawFunction("cos(x)", new OnReflectionFunction() {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Cos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sin(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Sin(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("tan(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Tan(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("abs(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Abs(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sqrt(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Sqrt(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("acos(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Acos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("asin(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Asin(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("atan(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Atan(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("ceil(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Ceiling(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("cosh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Cosh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sinh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Sinh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("tanh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Tanh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("exp(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Exp(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("floor(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Floor(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("log(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Log(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("acos(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Acos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("log10(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Log10(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Random random = new Random(Environment.TickCount);

            Calculator.RegisterRawFunction("random(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return (random.Next()%((int)(paramsList["x"].GetDigit().GetDouble()))).ToString();
                }
            });

            Calculator.RegisterRawFunction("round(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Round(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sign(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Sign(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("truncate(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Truncate(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("toRadians(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return (paramsList["x"].GetDigit().GetDouble()*Math.PI/180.0f).ToString();
                }
            });

            Calculator.RegisterRawFunction("toDegrees(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return (paramsList["x"].GetDigit().GetDouble()*180.0f/Math.PI).ToString();
                }
            });

            Calculator.RegisterRawFunction("fact(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    BigInteger bigInt = new BigInteger(1);
                    for (int i = 1; i <= ((int)paramsList["x"].GetDigit().GetDouble()); i++)
                    {
                        bigInt = bigInt*i;
                    }
                    return bigInt.ToString();
                }
            });

            #endregion

            #region 双参数函数

            Calculator.RegisterRawFunction("mod(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return (paramsList["x"].GetDigit().GetDouble() % paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("IEEERemainder(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.IEEERemainder(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("max(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Max(paramsList["x"].GetDigit().GetDouble() , paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("min(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Min(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("pow(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Pow(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            #endregion

            #region 超级无敌炫酷牛逼吊炸上天函数

            var boolcalculator = new BooleanCalculatorSupport.BooleanCalculator(this);

            Calculator.RegisterRawFunction("if(condition,true_expr,false_expr)", new OnReflectionFunction()
            {
                onParseParameter=(String paramter, Function.ParameterRequestWrapper request, Calculator calculator) => {
                    char c;
                    int requestIndex = 0;
                    Dictionary<string, Variable> variableHashMap = new Dictionary<string, Variable>();
                    Stack<int> BracketStack = new Stack<int>();
                    String paramterString ="";
                    for (int pos = 0; pos < paramter.Length; pos++)
                    {
                        c = paramter[(pos)];
                        if (c == '(')
                        {
                            BracketStack.Push(pos);
                        }
                        else if (c == ')')
                        {
                            if (!(BracketStack.Count==0))
                                BracketStack.Pop();
                            else
                                throw new Exception("Not found a pair of bracket what defining a expression");
                        }

                        if (c == ',' && BracketStack.Count==0)
                        {
                            String requestParamterName = request.GetParamterName(requestIndex++);
                            variableHashMap[requestParamterName]= new ExpressionVariable(requestParamterName, paramterString, calculator);
                            paramterString = "";
                        }
                        else
                        {
                            paramterString += c;
                        }
                    }
                    if (!(paramter.Length==0))
                        variableHashMap[request.GetParamterName(requestIndex)] = new ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator);
                    return variableHashMap;
                },

                onReflectionFunction = (paramsList, calculator) => {
                    if(boolcalculator.Solve(((ExpressionVariable)paramsList[("condition")]).RawText))
                        return calculator.Solve(paramsList[("true_expr")].Solve());
                    else
                        return calculator.Solve(paramsList[("false_expr")].Solve());
                }
            });

            Calculator.RegisterRawFunction("loop_with(step,min,max,expr)", new OnReflectionFunction()
            {
                onParseParameter = (String paramter, Function.ParameterRequestWrapper request, Calculator calculator) => {
                    char c;
                    int requestIndex = 0;
                    Dictionary<String, Variable> variableHashMap = new Dictionary<string, Variable>();
                    Stack<int> BracketStack = new Stack<int>();
                    String paramterString = "";
                    for (int pos = 0; pos < paramter.Length; pos++)
                    {
                        c = paramter[(pos)];
                        if (c == '(')
                        {
                            BracketStack.Push(pos);
                        }
                        else if (c == ')')
                        {
                            if (!(BracketStack.Count==0))
                                BracketStack.Pop();
                            else
                                throw new Exception("Not found a pair of bracket what defining a expression");
                        }

                        if (c == ',' && BracketStack.Count==0)
                        {
                            String requestParamterName = request.GetParamterName(requestIndex++);
                            variableHashMap[requestParamterName] = requestParamterName==("expr") ? new ExpressionVariable(requestParamterName, paramterString, calculator) : new Variable(requestParamterName, paramterString, calculator);
                            paramterString = "";
                        }
                        else
                        {
                            paramterString += c;
                        }
                    }
                    if (!(paramter.Length==0))
                        variableHashMap[request.GetParamterName(requestIndex)] = new ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator);
                    return variableHashMap;
                },

                onReflectionFunction = (paramsList, calculator) => {
                    double step = paramsList[("step")].GetDigit().GetDouble();
                    double min = paramsList[("min")].GetDigit().GetDouble();
                    double max = paramsList[("max")].GetDigit().GetDouble();
                    String expr = ((ExpressionVariable)paramsList[("expr")]).RawText;
                    Function function = new Function(String.Format("tmp_execute(_index,_step,_min,_max,_out)={0}", expr), calculator);//todo 可优化
                    String result= "0";
                    for (double i = min; i <= max; i += step)
                    {
                        result = function.Solve(String.Format("{0},{1},{2},{3},{4}", i, step, min, max, result));
                    }
                    return result;
                }
            });

            Calculator.RegisterRawFunction("execute(command)",new OnReflectionFunction() {
                onParseParameter = (string paramter, Function.ParameterRequestWrapper parameterRequest, Calculator calculator) => {
                    Dictionary<String, Variable> ParamterMap = new Dictionary<string,Variable>();
                    ParamterMap[parameterRequest.GetParamterName(0)] = new ExpressionVariable(parameterRequest.GetParamterName(0), paramter, calculator);
                    return ParamterMap;
                },

                onReflectionFunction = (Dictionary<String, Variable> parameter, Calculator calculator) => {
                    Variable variable = parameter[("command")];
                    String execute_text = "";
                    switch (variable.VariableType)
                    {
                        case VariableType.Normal:
                            {
                                execute_text = variable.Solve();
                                return execute_text;
                            }
                        case VariableType.ExpressionVariable:
                            {
                                execute_text = ((ExpressionVariable)variable).RawText;
                                break;
                            }
                    }
                    return calculator.Execute(execute_text.Trim('\"'));
                }
            });

            #endregion
        }

        #endregion
    }
}