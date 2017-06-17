using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public enum FunctionType
    {
        NormalFunction,
        ReflectionFunction,
        ScriptFunction,
        Unknown
    }

    public class Function : Expression
    {
        #region IndirectVariable

        class IndirectVariable : Variable
        {
            Function _bind_function = null;

            public IndirectVariable(string name, Calculator calculator, Function bindFunction) : base(name, null, calculator)
            {
                _bind_function = bindFunction;
            }

            public override string Solve()
            {
                if (_bind_function != null)
                {
                    if (_bind_function.parameters.ContainsKey(VariableName))
                        return _bind_function.parameters[(VariableName)].Solve();
                }
                try
                {
                    return this.Calculator.GetVariable(VariableName).Solve();
                }
                catch (Exception e)
                {
                    Log.Warning(String.Format("cant get value of request variable cause :{0} ,so return 0", e.Message));
                    return "0";
                }
            }
        }

        #endregion

        #region IndirectFunction

        class IndirectFunction : Function
        {
            Function _bind_function = null;

            public IndirectFunction(string name, Calculator calculator, Function bindFunction) : base(null, calculator)
            {
                _function_name = name;
                _bind_function = bindFunction;
            }

            public override string Solve()
            {
                String result = null;
                try
                {
                    this.Calculator.PushTmpVariable(_bind_function.parameters);
                    result = this.Calculator.GetFunction(_function_name).Solve(_current_paramsRawText);
                    this.Calculator.PopTmpVariable();
                    return result;
                }
                catch (Exception e)
                {
                    Log.Warning(String.Format("cant get value of request function cause :{0} ,so return 0 as value result", e.Message));
                    return "0";
                }
            }
        }

        #endregion

        #region ParameterRequestWrapper

        public class ParameterRequestWrapper : List<String>
        {
            private ParameterRequestWrapper() { }

            public ParameterRequestWrapper(string paramRawText)
            {
                /*一巴打死当年的我
                char c;
                String name = new String();
                for (int pos = 0; pos < rawText.length(); pos++)
                {
                    c = rawText.charAt(pos);
                    if (c == ',')
                    {
                        requestion_list.add(name);
                        name = new String();
                    }
                    else
                    {
                        name += c;
                    }
                }
                if (!name.isEmpty())
                    requestion_list.add(name);
                    */
                string[] parametersArray = paramRawText.Split(',');
                this.AddRange(parametersArray);
            }

            public int GetParamterRequestCount() => this.Count;

            public string[] GetParamterNameArray() => this.ToArray();

            public string GetParamterName(int index) => this[index];
        }

        #endregion

        protected string _function_name, _function_paramters, _function_body, _current_paramsRawText;

        protected ParameterRequestWrapper request;

        internal static bool ableStaticParseFunction = false;

        public override ExpressionType ExpressionType { get { return ExpressionType.Function; } }

        public virtual FunctionType FunctionType { get { return FunctionType.NormalFunction; } }

        List<Expression> staticBSEList = null;

        protected Function() : base(null) { }

        static Regex FunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)=(.+)");

        static string specialOperationChar = " + - * / [ ] ~ ! @ # $ % ^ & ( ) ; : \" | ? > < , ` ' \\ ";

        bool specialAbleStaticParseFunction = true;
        public bool IsSpecialAbleStaticParseFunction { get { return specialAbleStaticParseFunction; } set { specialAbleStaticParseFunction = value; } }

        protected Dictionary<string, Variable> parameters = new Dictionary<string, Variable>();

        public Function(string expression, Calculator calculator) : base(calculator)
        {
            if (expression == null || expression.Length == 0)
                return;
            _raw_text = expression;
            Match result = FunctionFormatRegex.Match(expression);
            if (result.Groups.Count != 4)
                Log.ExceptionError(new Exception("Cannot parse function ：" + expression));
            _function_name = result.Groups[1].Value;
            _function_paramters = result.Groups[2].Value;
            ParameterRequestWrapper parameterRequest = new ParameterRequestWrapper(_function_paramters);
            request = parameterRequest;
            _function_body = result.Groups[3].Value;
        }

        protected void Parse(string paramsRawText)
        {
            char c;
            int requestIndex = 0;
            Stack<int> BracketStack = new Stack<int>();
            String paramter = "";
            for (int pos = 0; pos < paramsRawText.Length; pos++)
            {
                c = paramsRawText[(pos)];
                if (c == '(')
                {
                    BracketStack.Push(pos);
                }
                else if (c == ')')
                {
                    if (!(BracketStack.Count == 0))
                        BracketStack.Pop();
                    else
                        Log.ExceptionError(new Exception("Not found a pair of bracket what defining a expression"));
                }
                if (c == ',' && (BracketStack.Count == 0))
                {
                    string requestParamterName = request[(requestIndex++)];
                    this.parameters[requestParamterName] = new Variable(requestParamterName, this.Calculator.Solve(paramter), this.Calculator);
                    paramter = "";
                }
                else
                {
                    paramter += c;
                }
            }
            if ((paramter.Length != 0))
                this.parameters[request[(requestIndex)]] = new ExpressionVariable(request[(requestIndex)], this.Calculator.Solve(paramter), this.Calculator);
        }

        string ParseDeclaring(string expression)
        {
            string newExpression = expression;
            foreach (var pair in parameters)
            {
                newExpression = newExpression.Replace(pair.Key, "(" + pair.Value.Solve() + ")");
            }
            return newExpression;
        }

        public override string GetName() => _function_name;

        private void StaticParseExpression()
        {
            List<Expression> tmpBSEArrayList = ParseExpression(_function_body);
            staticBSEList = Calculator.ConverToBSE(tmpBSEArrayList);
        }

        List<Expression> ParseExpression(string expression)
        {
            List<Expression> expressionArrayList = new List<Expression>();
            int position = 0;
            char c, tmp_c;
            Expression expr = null;
            string statement = "", tmp_op;
            Stack<int> bracket_stack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    break;
                c = expression[position];
                if (specialOperationChar.Contains(" " + (c.ToString()) + " "))
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
                                    expressionArrayList.Add(new ExpressionVariable("", Utils.RepeatingDecimalCoverToExpression(statement), this.Calculator));
                                    statement = "";
                                    break;
                                }
                            }
                            else if (c == ')')
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
                                statement += c;
                            }
                            else
                            {
                                statement += c;
                            }
                            position++;
                        }
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
                                position++;
                                /*判断是否存在双字符组合的操作符,兼容逻辑操作符，如>= == !=*/
                                if (!specialOperationChar.Contains(" " + (tmp_op) + " "))
                                {
                                    tmp_op = c.ToString();
                                    position--;
                                }
                            }
                        }
                        expressionArrayList.Add(new Symbol(tmp_op));
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

        public virtual string Solve(string paramsRawText)
        {
            Parse(paramsRawText);
            if (parameters.Count != request.GetParamterRequestCount())
                Log.ExceptionError(new Exception($"function \"{_function_name}\" requests {request.GetParamterRequestCount()} paramter(s) but you input {parameters.Count} paramter(s)"));
            String result;
            if (specialAbleStaticParseFunction && Function.ableStaticParseFunction)
            {
                if (staticBSEList == null)
                    StaticParseExpression();
                result = Solve(staticBSEList);
                parameters.Clear();
                return result;
            }
            else
            {
                staticBSEList = null;
            }
            String exression;
            exression = ParseDeclaring(_function_body);
            parameters.Clear();
            return Calculator.Solve(exression);
        }

        public override string Solve() => Solve(_current_paramsRawText);

        string Solve(List<Expression> expressionArrayList)
        {
            if (expressionArrayList.Count == 1)
                if (expressionArrayList[0].ExpressionType == ExpressionType.Digit || expressionArrayList[0].ExpressionType == ExpressionType.Variable)
                    return ((((Digit)expressionArrayList[(0)]).GetDouble())).ToString();
            Stack<Expression> digit_stack = new Stack<Expression>();
            Symbol op;
            List<Expression> paramterList, result;
            try
            {
                foreach (Expression node in expressionArrayList)
                {
                    if (node.ExpressionType == ExpressionType.Symbol)
                    {
                        op = (Symbol)node;
                        paramterList = new List<Expression>();
                        for (int i = 0; i < op.GetParamterCount(); i++)
                            paramterList.Add(digit_stack.Count == 0 ? new Digit("0") : digit_stack.Pop());
                        paramterList.Reverse();
                        result = op.Solve(paramterList, this.Calculator);
                        foreach (Expression expr in result)
                            digit_stack.Push(expr);
                    }
                    else
                    {
                        if (node.ExpressionType == ExpressionType.Digit || node.ExpressionType == ExpressionType.Function || node.ExpressionType == ExpressionType.Variable)
                        {
                            digit_stack.Push(new Digit(node.Solve()));
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

        public void BindCalculator(Calculator calculator)
        {
            _ref_calculator = calculator;
        }

        public override string ToString() => $"{_function_name}({_function_paramters})={_function_body}";

        static Regex checkFunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");

        private Expression checkConverExpression(String text)
        {
            if (Utils.isFunction(text))
            {
                //Get function name
                Match result = checkFunctionFormatRegex.Match(text);
                if (result.Groups.Count != 3)
                    Log.ExceptionError(new Exception("Cannot parse function ：" + text));
                String function_name = result.Groups[(1)].Value;
                String function_paramters = result.Groups[(2)].Value;
                if (!this.Calculator.ContainFunction(function_name))
                    Log.ExceptionError(new Exception(String.Format("function {0} hadnt declared!", function_name)));
                Function function = new IndirectFunction(function_name, Calculator, this);
                function._current_paramsRawText = function_paramters;
                return function;
            }

            if (Utils.isDigit(text))
            {
                return new Digit(text);
            }

            if (Utils.isValidVariable(text))
            {
                return new IndirectVariable(text, this.Calculator, this);
            }
            return null;
        }

        public Digit GetSolveToDigit() => new Digit(Solve());

    }
}