using ExtrameFunctionCalculator.Script;
using ExtrameFunctionCalculator.Types;
using ExtrameFunctionCalculator.UtilTools;
using System;
using System.Collections.Generic;
using System.Numerics;

namespace ExtrameFunctionCalculator
{
    public class Calculator
    {
        internal struct SymbolInfo
        {
            public string symbol;
            public int right_params_request;
            public int left_params_request;
            public float prioty;
            public OnCalculateFunc call_function;
            public bool able_default_param_value;
            public string default_param_value;
            public bool is_right_to_left;
        }

        private Dictionary<String, Stack<Variable>> tmp_variable_map = new Dictionary<string, Stack<Variable>>();
        private ScriptManager script_manager;
        private Stack<List<String>> record_tmp_variable_stack = new Stack<List<string>>();

        internal Dictionary<string, SymbolInfo> operator_info = new Dictionary<string, SymbolInfo>();
        internal Dictionary<string, Symbol> shared_symbol_cache = new Dictionary<string, Symbol>();

        private CalculatorOptimizer calculator_optimizer = null;
        private static string special_operator_chars = " [ ] ";
        private static Dictionary<String, Variable> raw_variable_table = new Dictionary<string, Variable>();
        private static Dictionary<String, ReflectionFunction> raw_function_table = new Dictionary<string, ReflectionFunction>();
        private Dictionary<String, Variable> variable_table = new Dictionary<string, Variable>();
        private Dictionary<String, Function> function_table = new Dictionary<string, Function>();

        private ObjectPool<Digit> digit_cache_pool = new ObjectPool<Digit>(() => new Digit("0"), (digit) => { });
        private ObjectPool<List<Expression>> expression_list_cache_pool = new ObjectPool<List<Expression>>(() => new List<Expression>(), (list) => { list.Clear(); });

        public Calculator()
        {
            Init();
            calculator_optimizer = new CalculatorOptimizer(this);
            script_manager = new ScriptManager(this);
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

        private void OptimizeEnable(bool sw)
        {
            calculator_optimizer.Enable = sw;
        }

        private string Optimize(string sw)
        {
            string level = sw.Substring(sw.IndexOf(" ")).Replace(" ", string.Empty);
            sw = sw.Substring(0, sw.IndexOf(" ")).Replace(" ", string.Empty);
            switch (sw)
            {
                case "true":
                    {
                        Enable(EnableType.ExpressionOptimize);
                        calculator_optimizer.OptimizeLevel = (int.Parse(level));
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

        #endregion EnableWrappers

        #region 变量

        private void SetMapVariable(string expression)
        {
            //mymapvar[myvar]["mykeyname"][5]=myvalue
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            int position = 0;
            char c = (char)0;
            //读取名字
            string variable_name = string.Empty;
            while (true)
            {
                if (position >= expression.Length)
                    Log.Error(String.Format("{0} isnt vaild format", expression));
                c = expression[position];
                if (c == '[')
                    break;
                variable_name += c;
                position++;
            }
            //读取下标
            string indexes = string.Empty;
            Stack<int> balanceStack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    Log.Error(String.Format("{0} isnt vaild format", expression));
                c = expression[position];
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
                        c = expression[position];
                        if (c == '=')
                            break;
                    }
                }
            }
            //读取右值表达式
            string variable_expr = expression.Substring(++position);
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

        private void SetVariable(string expression)
        {
            char c;
            string variable_name = string.Empty, variable_expression = string.Empty;
            for (int position = 0; position < expression.Length - 1; position++)
            {
                c = expression[position];
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

        private void RegisterVariable(Variable variable)
        {
            Log.Debug(variable.GetName());
            variable_table.Add(variable.GetName(), variable);
        }

        public static void RegisterRawVariable(Variable variable) => raw_variable_table[variable.GetName()] = variable;

        internal string RequestVariable(string name)
        {
            if (!variable_table.ContainsKey(name))
                Log.ExceptionError(new Exception($"Variable {name} not found"));
            return variable_table[name].Solve();
        }

        public void SetExpressionVariable(string expression)
        {
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            char c;
            string variable_name = string.Empty, variable_expression = string.Empty;
            Variable variable = null;
            for (int position = 0; position < expression.Length - 1; position++)
            {
                c = expression[position];
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

        private Variable GetTmpVariable(string name)
        {
            if (tmp_variable_map.ContainsKey(name))
                return tmp_variable_map[name].Peek();
            return null;
        }

        internal void PopTmpVariable()
        {
            List<String> recordList = record_tmp_variable_stack.Pop();
            foreach (string tmp_name in recordList)
            {
                tmp_variable_map[tmp_name].Pop();
                //Log.Debug(String.format("tmp variable \"%s\" was pop",TmpVariable.get(tmp_name).pop().toString()));
                if (tmp_variable_map[tmp_name].Count == 0)
                    tmp_variable_map.Remove(tmp_name);
            }
            Log.Debug(String.Format("there are {0} tmp variables are popped in {1} layout", recordList.Count, record_tmp_variable_stack.Count + 1));
        }

        internal void PushTmpVariable(Dictionary<String, Variable> variableHashMap)
        {
            List<String> recordList = new List<string>();
            foreach (var pair in variableHashMap)
            {
                recordList.Add(pair.Key);
                if (!tmp_variable_map.ContainsKey(pair.Key))
                    tmp_variable_map.Add(pair.Key, new Stack<Variable>());
                tmp_variable_map[pair.Key].Push(pair.Value);
                //Log.Debug(String.format("tmp variable \"%s\" was push",pair.getValue().toString()));
            }
            record_tmp_variable_stack.Push(recordList);
            Log.Debug(String.Format("there are {0} tmp variables are pushed in {1} layout", recordList.Count, record_tmp_variable_stack.Count));
        }

        internal Variable GetVariable(string name)
        {
            Variable tmp_variable = GetTmpVariable(name);
            if (tmp_variable != null)
                return tmp_variable;
            if (raw_variable_table.ContainsKey(name))
            {
                return raw_variable_table[name];
            }
            if (variable_table.ContainsKey(name))
                return variable_table[name];
            tmp_variable = GetScriptManager().RequestVariable(name, null);
            if (tmp_variable != null)
                return tmp_variable;
            //Log.Warning( new VariableNotFoundException(name).getMessage());
            return null;
        }

        public bool TryGetVariable(string name, out Variable variable)
        {
            variable = GetVariable(name);
            return variable != null;
        }

        #endregion 变量

        #region 脚本

        public string LoadScriptFile(string file_path)
        {
            GetScriptManager().LoadScript(file_path);
            return "loaded scriptfile successfully!";
        }

        public string UnloadScriptFile(string package_name)
        {
            try
            {
                GetScriptManager().UnloadScript(package_name);
            }
            catch
            {
                return "unloaded scriptfile failed!";
            }
            return "unloaded scriptfile successfully!";
        }

        public ScriptManager GetScriptManager()
        {
            return script_manager;
        }

        #endregion 脚本

        #region 函数

        public bool ContainFunction(string name)
        {
            if (raw_function_table.ContainsKey(name))
                return true;
            if (function_table.ContainsKey(name))
                return true;
            if (GetScriptManager().ContainFunction(name))
                return true;
            return false;
        }

        internal Function GetFunction(string name)
        {
            if (raw_function_table.ContainsKey(name))
            {
                Function function = raw_function_table[name];
                function.BindCalculator(this);
                return function;
            }
            if (function_table.ContainsKey(name))
                return function_table[name];
            return GetScriptManager().RequestFunction(name);
            //return function_table.get(name);
        }

        public static void RegisterRawFunction(string expression, OnReflectionFunction reflectionFunction)
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

        private void RegisterReflectionFunction(string expression, OnReflectionFunction onReflectionFunction)
        {
            ReflectionFunction reflectionFunction = new ReflectionFunction(expression, onReflectionFunction);
            RegisterFunction(reflectionFunction);
        }

        private void SetFunction(string expression)
        {
            if (expression.Length == 0)
                Log.ExceptionError(new Exception("empty text"));
            Function function = new Function(expression, this);
            RegisterFunction(function);
        }

        private void RegisterFunction(Function function)
        {
            Log.Debug(function.ToString());
            function_table[function.GetName()] = function;
        }

        #endregion 函数

        #region 执行

        public string Solve(string expression)
        {
            if (Utils.IsDigit(expression))
                return expression;
            List<Expression> expression_list = (ParseExpression(expression));
            Expression result_expr = SolveExpressionList(expression_list);

            string result = result_expr.RawText;

            digit_cache_pool.Push(result_expr as Digit);
            double d = double.Parse(result);

            /*
            if (result.Contains("."))
            {
                string tmpDecial = result.Substring(result.IndexOf('.') + 1);

                if (int.Parse(tmpDecial) == (0))
                    return result.Substring(0, result.IndexOf('.'));
            }*/

            return ((int)d) == d ? ((int)d).ToString() : d.ToString();
        }

        public bool BoolSolve(string expression)
        {
            if (Utils.IsDigit(expression))
                return double.Parse(expression) != 0;

            List<Expression> expression_list = (ParseExpression(expression));
            Expression result_expr = SolveExpressionList(expression_list);

            if (result_expr.ExpressionType == ExpressionType.Digit)
            {
                string result = result_expr.Solve();
                digit_cache_pool.Push((Digit)result_expr);
                return Double.Parse(result) != 0;
            }
            if (result_expr.ExpressionType == ExpressionType.Variable)
                if (((Variable)result_expr).VariableType == VariableType.BooleanVariable)
                    return ((BooleanVariable)result_expr).BoolValue;

            throw new Exception("Uncalculatable type :" + result_expr.ExpressionType.ToString());
        }

        private void CheckNormalizeChain(ref List<Expression> expression_list)
        {
            foreach (Expression node in expression_list)
            {
                if (/*node.ExpressionType != ExpressionType.Digit && node.ExpressionType != ExpressionType.Symbol*/!(ExpressionType.Digit | ExpressionType.Symbol).HasFlag(node.ExpressionType))
                    Log.ExceptionError(new Exception(node.GetName() + " isnt digit or symbol."));
            }
        }

        private void ConverVariableToDigit(ref List<Expression> expression_list)
        {
            int position = 0;
            Expression node;
            Variable variable;
            Digit result;

            for (position = 0; position < expression_list.Count; position++)
            {
                node = expression_list[position];
                if (node.ExpressionType == ExpressionType.Variable)
                {
                    variable = (Variable)node;
                    result = variable.GetDigit();
                    expression_list.RemoveAt(position);
                    expression_list.Insert(position, result);
                }
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
                node = expression_list[position];
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

        public string Reset()
        {
            this.variable_table.Clear();
            this.function_table.Clear();
            return "Reset finished!";
        }

        private List<Expression> ExpressionOptimization(List<Expression> expression_list)
        {
            List<Expression> optimizeResult = calculator_optimizer.OptimizeExpression(expression_list);
            return optimizeResult == null ? expression_list : optimizeResult;
        }

        public List<Expression> ParseExpression(string expression)
        {
            List<Expression> expressionArrayList = expression_list_cache_pool.Pop();
            int position = 0;
            char c, tmp_c;
            Expression expr = null;
            string statement = string.Empty, tmp_op;
            Stack<int> bracket_stack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    break;
                c = expression[position];
                if (special_operator_chars.Contains($" {c} "))
                {
                    if ((!(statement.Length == 0)) && (c == '('))
                    {
                        //Function Parser
                        bracket_stack.Clear();
                        while (true)
                        {
                            if (position >= expression.Length)
                                break;
                            c = expression[position];
                            if (c == '(')
                            {
                                //判断是否有无限循环小数格式的可能
                                if (!Utils.IsDigit(statement))
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
                                        c = expression[++position];
                                    }
                                    expressionArrayList.Add(new ExpressionVariable(string.Empty, Utils.RepeatingDecimalCoverToExpression(statement), this));
                                    statement = string.Empty;
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
                                        expressionArrayList.Add(ParseStringToExpression(statement));//should always return Function
                                        break;
                                    }
                                }
                                statement += c;
                            }
                            position++;
                        }
                    }
                    else if ((!(statement.Length == 0)) && c == '[')
                    {
                        //array/map
                        char tmp_ch = (char)0;
                        //读取下标
                        string indexes = string.Empty;
                        Stack<int> balanceStack = new Stack<int>();

                        while (true)
                        {
                            if (position >= expression.Length)
                                Log.Error(String.Format("{0} isnt vaild format", expression));
                            tmp_ch = expression[position];
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
                                    tmp_ch = expression[position];
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
                        expressionArrayList.Add(((MapVariable)variable).RouteGetVariable(indexes));
                        position--;
                    }
                    else
                    {
                        expr = ParseStringToExpression(statement);
                        if (expr != null)
                            expressionArrayList.Add(expr);
                        tmp_op = c.ToString();
                        {
                            if (position < (expression.Length - 1))
                            {
                                tmp_c = expression[position + 1];
                                tmp_op += tmp_c;
                                if (!special_operator_chars.Contains(" " + (tmp_op) + " "))
                                {
                                    tmp_op = c.ToString();
                                }
                                else
                                    position++;
                            }
                        }
                        expressionArrayList.Add(new Symbol(tmp_op, this));
                    }
                    //Reflush statement
                    statement = string.Empty;
                }
                else
                {
                    statement += c;
                }
                position++;
            }

            if (!(statement.Length == 0))
            {
                expr = (ParseStringToExpression(statement));
                if (expr == null)
                    throw new Exception($"cant parse expression \"{expression}\" for internal error.");
                expressionArrayList.Add(expr);
            }

            return expressionArrayList;
        }

        public string Execute(string text)
        {
            try
            {
                return ParseExecuteCommand(text);
            }
            catch (Exception e)
            {
                return $"ocurred a error -> {e.Message}";
            }
        }

        private string ParseExecuteCommand(string text)
        {
            Log.Debug(String.Format("Try Execute : {0}", text));
            //Clear(); //// TODO: 2016/11/22 此处存在和脚本语言的Call部分功能起到致命冲突，stdmath2::getCall().
            if (string.IsNullOrWhiteSpace(text))
                Log.ExceptionError(new Exception("empty text to execute"));
            char c;
            string executeType = string.Empty, paramter = string.Empty;
            string result = string.Empty;

            for (int position = 0; position < text.Length; position++)
            {
                c = text[position];
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
                case "set_map":
                    {
                        SetMapVariable(paramter);
                        break;
                    }
                case "log":
                    {
                        Log.User(paramter);
                        result = string.Empty;
                        break;
                    }
                case "load_script":
                    {
                        result = LoadScriptFile(paramter);
                        break;
                    }
                case "unload_script":
                    {
                        result = UnloadScriptFile(paramter);
                        break;
                    }
                case "reset":
                    {
                        result = Reset();
                        break;
                    }
                default:
                    {
                        Log.ExceptionError(new Exception(String.Format("unknown command \"{0}\"", executeType)));
                        break;
                    }
            }
            return result;
        }

        public Expression SolveExpressionList(List<Expression> expression_list)
        {
            if (expression_list.Count == 1 && expression_list[0].ExpressionType == ExpressionType.Digit)
                return expression_list[0];

            CalculateBracket(ref expression_list);
            ConverVariableToDigit(ref expression_list);
            ConverFunctionToDigit(ref expression_list);
            CheckNormalizeChain(ref expression_list);
            expression_list = ExpressionOptimization(expression_list);

            return CalculateExecute(expression_list);
        }

        internal void CalculateBracket(ref List<Expression> expression_list)
        {
            int position = 0, position_start;
            while (true)
            {
                if (position >= expression_list.Count)
                    break;
                Expression node = expression_list[position];
                if (node.ExpressionType == ExpressionType.Symbol && ((Symbol)node).RawText == "(")
                {
                    position_start = position;
                    int stack = 0;
                    while (true)
                    {
                        if (position >= expression_list.Count)
                        {
                            if (stack != 0)
                                throw new Exception("未配对的多余括号");
                        }
                        node = expression_list[position];
                        if (node.ExpressionType == ExpressionType.Symbol && ((Symbol)node).RawText == "(")
                        {
                            stack++;
                        }
                        if (node.ExpressionType == ExpressionType.Symbol && ((Symbol)node).RawText == ")")
                        {
                            stack--;
                            if (stack == 0)
                            {
                                var sub_exprssion_list = expression_list.GetRange(position_start + 1, position - position_start - 1);
                                var result = digit_cache_pool.Pop((obj) => { obj.RawText = SolveExpressionList(sub_exprssion_list).RawText; });
                                expression_list.RemoveRange(position_start, position - position_start + 1);
                                expression_list.Insert(position_start, result);
                                position = position_start;
                                break;
                            }
                        }
                        position++;
                    }
                }
                position++;
            }
        }

        internal Expression CalculateExecute(List<Expression> expression_list)
        {
            int position = 0;
            List<Expression> paramsList = expression_list_cache_pool.Pop();

            while (true)
            {
                if (position >= expression_list.Count)
                    break;
                Expression node = expression_list[position];
                if (node.ExpressionType == ExpressionType.Symbol)
                {
                    Symbol op = (Symbol)node, next_op;
                    SymbolInfo symbol_info = operator_info[op.RawText];
                    int next_symbol_position;

                    if (GetNextSymbol(expression_list, position, out next_op, out next_symbol_position))
                    {
                        int prioty = op.CompareOperationPrioty(next_op);
                        if (symbol_info.is_right_to_left ? prioty <= 0 : prioty < 0)
                        {
                            position = next_symbol_position;
                            continue;
                        }
                    }

                    //符号计算
                    paramsList.Clear();
                    Digit next_value, prev_value;
                    int next_i = position, prev_i = position;

                    for (int r = 0, l = 0; r + l < symbol_info.right_params_request + symbol_info.left_params_request;)
                    {
                        if (l < symbol_info.left_params_request)
                        {
                            if (!GetPrevDigit(expression_list, prev_i, out prev_value, out prev_i))
                            {
                                if (!symbol_info.able_default_param_value)
                                    throw new Exception("缺少必要的参数");
                                prev_value = digit_cache_pool.Pop((obj) => { obj.RawText = symbol_info.default_param_value; });
                                prev_i = position == 0 ? 0 : position - 1;
                            }

                            paramsList.Add(prev_value);
                            l++;
                        }

                        if (r < symbol_info.right_params_request)
                        {
                            if (!GetNextDigit(expression_list, next_i, out next_value, out next_i))
                            {
                                if (!symbol_info.able_default_param_value)
                                    throw new Exception("缺少必要的参数");
                                next_value = digit_cache_pool.Pop((obj) => { obj.RawText = symbol_info.default_param_value; });
                                next_i = position == 0 ? 0 : position - 1;
                            }

                            paramsList.Add(next_value);
                            r++;
                        }
                    }

                    var result = op.Solve(paramsList, this);
                    Expression expr = !(result[0] is Digit) ? digit_cache_pool.Pop(obj => { obj.RawText = result[0].Solve(); }) : result[0];

                    expression_list.Insert(prev_i, expr);

                    foreach (var digit in paramsList)
                    {
                        expression_list.Remove(digit);
                        digit_cache_pool.Push((Digit)digit);
                    }

                    expression_list.Remove(op);
                    expression_list_cache_pool.Push(result);

                    position = 0;
                    continue;
                }
                position++;
            }
            expression_list_cache_pool.Push(paramsList);
            if (expression_list.Count != 1)
                Log.ExceptionError(new Exception($"still exsit more expression object in result list"));
            return expression_list[0];
        }

        #region GetNode

        private bool GetNextSymbol(List<Expression> expression_list, int position, out Symbol next_symbol, out int next_i) => TryGetNextTypeValue<Symbol>(expression_list, position, out next_symbol, out next_i);

        private bool TryGetPrevTypeValue<T>(List<Expression> expression_list, int position, out T next_symbol, out int prev_i) where T : Expression
        {
            T tmp;
            for (int i = position - 1; (i >= 0) && (i < expression_list.Count); i--)
            {
                tmp = expression_list[i] as T;
                if (tmp != null)
                {
                    next_symbol = tmp;
                    prev_i = i;
                    return true;
                }
            }
            next_symbol = null;
            prev_i = 0;
            return false;
        }

        private bool GetNextDigit(List<Expression> expression_list, int position, out Digit next_digit, out int next_i) => TryGetNextTypeValue<Digit>(expression_list, position, out next_digit, out next_i);

        private bool GetPrevDigit(List<Expression> expression_list, int position, out Digit prev_digit, out int prev_i) => TryGetPrevTypeValue<Digit>(expression_list, position, out prev_digit, out prev_i);

        private bool GetPrevtSymbol(List<Expression> expression_list, int position, out Symbol prev_symbol, out int prev_i) => TryGetPrevTypeValue<Symbol>(expression_list, position, out prev_symbol, out prev_i);

        private bool TryGetNextTypeValue<T>(List<Expression> expression_list, int position, out T next_symbol, out int next_i) where T : Expression
        {
            T tmp;
            for (int i = position + 1; i < expression_list.Count; i++)
            {
                tmp = expression_list[i] as T;
                if (tmp != null)
                {
                    next_symbol = tmp;
                    next_i = i;
                    return true;
                }
            }
            next_symbol = null;
            next_i = 0;
            return false;
        }

        #endregion GetNode

        internal Expression ParseStringToExpression(string text)
        {
            if (Utils.IsFunction(text))
            {
                string function_name;
                string function_paramters;
                if (!ParserUtils.TryParseTextToFunctionDeclear(text, out function_name, out function_paramters))
                    Log.ExceptionError(new Exception("Cannot parse function ：" + text));
                if (!ContainFunction(function_name))
                    Log.ExceptionError(new Exception(String.Format("function {0} hadnt declared!", function_name)));
                Function function = GetFunction(function_name);

                return digit_cache_pool.Pop(obj => { obj.RawText = function.Solve(function_paramters); });
            }
            if (Utils.IsDigit(text))
            {
                return digit_cache_pool.Pop(obj => { obj.RawText = text; });
            }

            if (Utils.IsValidVariable(text))
            {
                Variable variable = GetVariable(text);
                if (variable == null)
                    Log.ExceptionError(new Exception($"Variable {text} is not found"));
                //因为MapVariable并不在此处理所以为了减少引用调用所以不用new WrapperVariable;
                return digit_cache_pool.Pop(obj => { obj.RawText = variable.Solve(); });
            }

            return null;
        }

        #endregion 执行

        #region Operators

        private Symbol GetSymbolMayFromCache(string op)
        {
            if (!shared_symbol_cache.ContainsKey(op))
                shared_symbol_cache[op] = new Symbol(op, this);
            return shared_symbol_cache[op];
        }

        public delegate List<Expression> OnCalculateFunc(List<Expression> parametersList, Calculator refCalculator);

        public void RegisterOperator(string operatorSymbol, int requestParamterSize, float operatorPrioty, OnCalculateFunc operatorFunction, bool able_default_param_value = false, string default_param_value = "0", bool is_right_to_left = false)
        {
            int t = requestParamterSize / 2;
            RegisterOperation(operatorSymbol, (t, requestParamterSize - t), operatorPrioty, operatorFunction, able_default_param_value, default_param_value, is_right_to_left);
        }

        public void RegisterOperation(string operatorSymbol, (int l, int r) request_params_count, float operatorPrioty, OnCalculateFunc operatorFunction, bool able_default_param_value = false, string default_param_value = "0", bool is_right_to_left = false)
        {
            SymbolInfo info = new SymbolInfo()
            {
                call_function = operatorFunction,
                prioty = operatorPrioty,
                left_params_request = request_params_count.l,
                right_params_request = request_params_count.r,
                symbol = operatorSymbol,
                able_default_param_value = able_default_param_value,
                default_param_value = default_param_value,
                is_right_to_left = is_right_to_left
            };

            shared_symbol_cache[operatorSymbol] = GetSymbolMayFromCache(operatorSymbol);
            this.operator_info[operatorSymbol] = info;

            special_operator_chars += $" {operatorSymbol} ";
            special_operator_chars += $" {operatorSymbol[0]} ";
        }

        #endregion Operators

        #region Init

        static Calculator()
        {
            Log.SetIsThreadCommitLog(true);
        }

        private void Init()
        {
            #region 基本操作符

            RegisterOperator("+", 2, 10, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = (a.GetDouble() + b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("-", 2, 10, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = (a.GetDouble() - b.GetDouble()).ToString(); }));
                return result;
            }, true);

            RegisterOperator("*", 2, 11, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = (a.GetDouble() * b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("/", 2, 11, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = (a.GetDouble() / b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("%", 2, 11, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() % (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("^^", 2, 12.0f, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = Math.Pow(a.GetDouble(), b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("(", 2, 99.0f, (paramsList, calculator) =>
            {
                return null;
            });

            RegisterOperator(")", 2, 99.0f, (paramsList, calculator) =>
            {
                return null;
            });

            #endregion 基本操作符

            #region 逻辑运算符

            RegisterRawVariable(new BooleanVariable(true, null));

            RegisterRawVariable(new BooleanVariable(false, null));

            RegisterOperator(">", 2, 8, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va > vb, calculator));
                return result;
            });

            RegisterOperator("<", 2, 8, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va < vb, calculator));
                return result;
            });

            RegisterOperator(">=", 2, 8, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va >= vb, calculator));
                return result;
            });

            RegisterOperator("<=", 2, 8, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va <= vb, calculator));
                return result;
            });

            RegisterOperator("==", 2, 7, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va == vb, calculator));
                return result;
            });

            RegisterOperator("!=", 2, 7, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va != vb, calculator));
                return result;
            });

            RegisterOperator("&&", 2, 3, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable((va != 0) && (vb != 0), calculator));
                return result;
            });

            RegisterOperator("||", 2, 2, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable((va != 0) || (vb != 0), calculator));
                return result;
            });

            RegisterOperation("!", (0, 1), 12, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0];
                if (!(a.IsCalculatable))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));

                result.Add(new BooleanVariable(!(va != 0), calculator));
                return result;
            }, false, "0", true);

            #endregion 逻辑运算符

            #region 位运算操作符

            RegisterOperator("<<", 2, 9, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() << (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator(">>", 2, 9, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() >> (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("&", 2, 6.0f, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() & (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("|", 2, 4, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() | (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperator("^", 2, 5, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Digit a = (Digit)paramsList[0], b = (Digit)paramsList[1];
                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = ((int)a.GetDouble() ^ (int)b.GetDouble()).ToString(); }));
                return result;
            });

            RegisterOperation("~", (0, 1), 12, (paramsList, calculator) =>
            {
                List<Expression> result = expression_list_cache_pool.Pop();
                Expression a = paramsList[0];
                if (!(a.IsCalculatable))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));

                result.Add(digit_cache_pool.Pop(obj => { obj.RawText = (~((int)va)).ToString(); }));
                return result;
            }, false, "0", true);

            #endregion 位运算操作符

            #region 单参数函数

            Calculator.RegisterRawFunction("cos(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Cos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sin(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Sin(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("tan(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Tan(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("abs(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Abs(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sqrt(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Sqrt(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });
            /*
            Calculator.RegisterRawFunction("acos(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) => {
                    return Math.Acos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });
            */
            Calculator.RegisterRawFunction("asin(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Asin(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("atan(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Atan(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("ceil(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Ceiling(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("cosh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Cosh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sinh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Sinh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("tanh(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Tanh(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("exp(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Exp(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("floor(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Floor(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("log(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Log(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("acos(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Acos(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("log10(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Log10(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Random random = new Random(Environment.TickCount);

            Calculator.RegisterRawFunction("random(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return (random.Next() % ((int)(paramsList["x"].GetDigit().GetDouble()))).ToString();
                }
            });

            Calculator.RegisterRawFunction("round(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Round(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("sign(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Sign(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("truncate(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Truncate(paramsList["x"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("toRadians(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return (paramsList["x"].GetDigit().GetDouble() * Math.PI / 180.0f).ToString();
                }
            });

            Calculator.RegisterRawFunction("toDegrees(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return (paramsList["x"].GetDigit().GetDouble() * 180.0f / Math.PI).ToString();
                }
            });

            Calculator.RegisterRawFunction("fact(x)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    BigInteger bigInt = new BigInteger(1);
                    for (int i = 1; i <= ((int)paramsList["x"].GetDigit().GetDouble()); i++)
                    {
                        bigInt = bigInt * i;
                    }
                    return bigInt.ToString();
                }
            });

            #endregion 单参数函数

            #region 双参数函数

            Calculator.RegisterRawFunction("mod(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return (paramsList["x"].GetDigit().GetDouble() % paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("IEEERemainder(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.IEEERemainder(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("max(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Max(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("min(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Min(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            Calculator.RegisterRawFunction("pow(x,y)", new OnReflectionFunction()
            {
                onReflectionFunction = (paramsList, calculator) =>
                {
                    return Math.Pow(paramsList["x"].GetDigit().GetDouble(), paramsList["y"].GetDigit().GetDouble()).ToString();
                }
            });

            #endregion 双参数函数

            #region 超级无敌炫酷牛逼吊炸上天函数

            Calculator.RegisterRawFunction("if(condition,true_expr,false_expr)", new OnReflectionFunction()
            {
                onParseParameter = (string paramter, Function.ParameterRequestWrapper request, Calculator calculator) =>
                {
                    char c;
                    int requestIndex = 0;
                    Dictionary<string, Variable> variableHashMap = new Dictionary<string, Variable>();
                    Stack<int> BracketStack = new Stack<int>();
                    string paramterString = string.Empty;
                    for (int pos = 0; pos < paramter.Length; pos++)
                    {
                        c = paramter[pos];
                        if (c == '(')
                        {
                            BracketStack.Push(pos);
                        }
                        else if (c == ')')
                        {
                            if (!(BracketStack.Count == 0))
                                BracketStack.Pop();
                            else
                                throw new Exception("Not found a pair of bracket what defining a expression");
                        }

                        if (c == ',' && BracketStack.Count == 0)
                        {
                            string requestParamterName = request.GetParamterName(requestIndex++);
                            variableHashMap[requestParamterName] = new ExpressionVariable(requestParamterName, paramterString, calculator);
                            paramterString = string.Empty;
                        }
                        else
                        {
                            paramterString += c;
                        }
                    }
                    if (!(paramter.Length == 0))
                        variableHashMap[request.GetParamterName(requestIndex)] = new ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator);
                    return variableHashMap;
                },

                onReflectionFunction = (paramsList, calculator) =>
                {
                    if (/*boolcalculator.Solve*/calculator.BoolSolve(((ExpressionVariable)paramsList["condition"]).RawText))
                        return calculator.Solve(paramsList["true_expr"].Solve());
                    else
                        return calculator.Solve(paramsList["false_expr"].Solve());
                }
            });

            Calculator.RegisterRawFunction("loop_with(step,min,max,expr)", new OnReflectionFunction()
            {
                onParseParameter = (string paramter, Function.ParameterRequestWrapper request, Calculator calculator) =>
                {
                    char c;
                    int requestIndex = 0;
                    Dictionary<String, Variable> variableHashMap = new Dictionary<string, Variable>();
                    Stack<int> BracketStack = new Stack<int>();
                    string paramterString = string.Empty;
                    for (int pos = 0; pos < paramter.Length; pos++)
                    {
                        c = paramter[pos];
                        if (c == '(')
                        {
                            BracketStack.Push(pos);
                        }
                        else if (c == ')')
                        {
                            if (!(BracketStack.Count == 0))
                                BracketStack.Pop();
                            else
                                throw new Exception("Not found a pair of bracket what defining a expression");
                        }

                        if (c == ',' && BracketStack.Count == 0)
                        {
                            string requestParamterName = request.GetParamterName(requestIndex++);
                            variableHashMap[requestParamterName] = requestParamterName == ("expr") ? new ExpressionVariable(requestParamterName, paramterString, calculator) : new Variable(requestParamterName, paramterString, calculator);
                            paramterString = string.Empty;
                        }
                        else
                        {
                            paramterString += c;
                        }
                    }
                    if (!(paramter.Length == 0))
                        variableHashMap[request.GetParamterName(requestIndex)] = new ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator);
                    return variableHashMap;
                },

                onReflectionFunction = (paramsList, calculator) =>
                {
                    double step = paramsList["step"].GetDigit().GetDouble();
                    double min = paramsList["min"].GetDigit().GetDouble();
                    double max = paramsList["max"].GetDigit().GetDouble();
                    string expr = ((ExpressionVariable)paramsList["expr"]).RawText;
                    Function function = new Function(String.Format("tmp_execute(_index,_step,_min,_max,_out)={0}", expr), calculator);//todo 可优化
                    string result = "0";
                    for (double i = min; i <= max; i += step)
                    {
                        result = function.Solve(String.Format("{0},{1},{2},{3},{4}", i, step, min, max, result));
                    }
                    return result;
                }
            });

            Calculator.RegisterRawFunction("execute(command)", new OnReflectionFunction()
            {
                onParseParameter = (string paramter, Function.ParameterRequestWrapper parameterRequest, Calculator calculator) =>
                {
                    Dictionary<String, Variable> ParamterMap = new Dictionary<string, Variable>();
                    ParamterMap[parameterRequest.GetParamterName(0)] = new ExpressionVariable(parameterRequest.GetParamterName(0), paramter, calculator);
                    return ParamterMap;
                },

                onReflectionFunction = (Dictionary<String, Variable> parameter, Calculator calculator) =>
                {
                    Variable variable = parameter["command"];
                    string execute_text = string.Empty;
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

            #endregion 超级无敌炫酷牛逼吊炸上天函数
        }

        #endregion Init
    }
}