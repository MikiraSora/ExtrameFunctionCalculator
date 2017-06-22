using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ExtrameFunctionCalculator.Types;
using System.Text.RegularExpressions;
using ExtrameFunctionCalculator.BooleanCalculatorSupport;

namespace ExtrameFunctionCalculator.BooleanCalculatorSupport
{
    class BooleanCalculator
    {
        static Regex checkFunctionFormatRegex = new Regex("([a-zA-Z]\\w*)\\((.*)\\)");
        static string specialOperationChar = "+ ++ - -- * / ~ ! != @ # $ % ^ & && ( ) ; : \" \" || ? > >= < <= , ` ' = == ";
        private List<Expression> rawExpressionChain = new List<Expression>();
        private List<Expression> BSEChain = new List<Expression>();
        Calculator calculator = null;
        public Calculator Calculator { get { return calculator; } }
        public BooleanCalculator(Calculator cal)
        {
            calculator = cal;
            Init();
        }

        Function GetFunction(string name) => calculator.GetFunction(name);
        Variable GetVariable(string name) => calculator.GetVariable(name);

        string RequestVariable(string name) => calculator.Solve(name);

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
                c = expression[(position)];
                if (specialOperationChar.Contains(" " + c.ToString() + " "))
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
                                        c = expression[(++position)];
                                    }
                                    expressionArrayList.Add(new ExpressionVariable("", Utils.RepeatingDecimalCoverToExpression(statement), Calculator));
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
                        string indexes = "";
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
                            Log.ExceptionError(new Exception(statement + " is not found."));
                        if (variable.VariableType != VariableType.MapVariable)
                            Log.ExceptionError(new Exception(String.Format("{0} isnt MapVariable", statement)));
                        /*
                        ((MapVariable)variable).SetIndexes(indexes);
                        expressionArrayList.Add(new WrapperVariable(variable, indexes));
                        */
                        expressionArrayList.Add(new Digit(((MapVariable)variable).Solve()));
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
                                position++;
                                if (!specialOperationChar.Contains(" " + (tmp_op) + " "))
                                {
                                    tmp_op = c.ToString();
                                    position--;
                                }
                            }
                        }
                        expressionArrayList.Add(new Symbol(tmp_op,calculator));
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

        private Expression checkConverExpression(string text) => calculator.ParseStringToExpression(text);
            /*
        {
            if (Utils.IsFunction(text))
            {
                //Get function name
                //Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
                Match result = checkFunctionFormatRegex.Match(text);
                if (result.Groups.Count != 3)
                    Log.ExceptionError(new Exception("Cannot parse function ：" + text));
                string function_name = result.Groups[(1)].Value;
                string function_paramters = result.Groups[(2)].Value;
                if (!calculator.ContainFunction(function_name))
                    Log.ExceptionError(new Exception(String.Format("function {0} hadnt declared!", function_name)));

                Function function = GetFunction(function_name);
                return new Digit(function.Solve(function_paramters));
                //Get function paramater list
            }

            if (Utils.IsDigit(text))
            {
                return new Digit(text);
            }

            if (Utils.IsValidVariable(text))
            {
                Variable variable = GetVariable(text);
                if (variable == null)
                    Log.ExceptionError(new Exception(text + " is not found."));
                //因为MapVariable并不在此处理所以为了减少引用调用所以不用new WrapperVariable;
                return variable;
            }

            return null;
        }
        */

        private void ConverFunctionToDigit()
        {
            int position = 0;
            Expression node;
            Function function;
            Digit result;
            for (position = 0; position < rawExpressionChain.Count; position++)
            {
                node = rawExpressionChain[(position)];
                if (node.ExpressionType == ExpressionType.Function)
                {
                    function = (Function)node;
                    if (function.FunctionType == FunctionType.ReflectionFunction)
                        function.BindCalculator(calculator);
                    result = function.GetSolveToDigit();
                    rawExpressionChain.RemoveAt(position);
                    rawExpressionChain.Insert(position, result);
                }
            }
        }

        private void ConverVariableToDigit()
        {
            int position = 0;
            Expression node;
            Variable variable;
            Expression result;
            try
            {
                for (position = 0; position < rawExpressionChain.Count; position++)
                {
                    node = rawExpressionChain[(position)];
                    if (node.ExpressionType == ExpressionType.Variable)
                    {
                        variable = (Variable)node;
                        if (variable.VariableType != VariableType.BooleanVariable)
                            result = variable.GetDigit();
                        else
                            result = variable;
                        rawExpressionChain.RemoveAt(position);
                        rawExpressionChain.Insert(position, result);
                    }
                }
            }
            catch (Exception e)
            {

            }
        }

        private void ConverToBSE()
        {
            List<Expression> result_list = new List<Expression>();
            Stack<Symbol> operation_stack = new Stack<Symbol>();
            Symbol symbol = null;
            Expression node = null;
            for (int position = 0; position < rawExpressionChain.Count; position++)
            {
                node = rawExpressionChain[(position)];
                if (node.ExpressionType == ExpressionType.Digit || node.ExpressionType == ExpressionType.Variable)
                    result_list.Add(node);
                else
                {
                    if (((Symbol)node).RawText == ("-"))
                    {
                        if (position == 0)
                        {
                            result_list.Add(new Digit("0"));
                        }
                        else if (rawExpressionChain[(position - 1)].ExpressionType == ExpressionType.Symbol)
                            if (((Symbol)rawExpressionChain[(position - 1)]).RawText == ("("))
                                result_list.Add(new Digit("0"));
                    }
                    if (operation_stack.Count == 0)
                        operation_stack.Push((Symbol)node);
                    else
                    {
                        if (!(((Symbol)node).RawText == (")")))
                        {
                            symbol = operation_stack.Peek();
                            while ((symbol == null ? false : (!(symbol.RawText == ("(")) && symbol.CompareOperationPrioty((Symbol)node) >= 0))/*(symbol == null ? false : (symbol.symbol_type != Calculator.Symbol.SymbolType.Bracket_Left && symbol.CompareOperationPrioty((Calculator.Symbol) node) >= 0))*/)
                            {
                                result_list.Add(operation_stack.Pop());
                                symbol = operation_stack.Count != 0 ? operation_stack.Peek() : null;
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
                                if (symbol.RawText == ("(")/*symbol.symbol_type == Calculator.Symbol.SymbolType.Bracket_Left*/)
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
            //Calculator.Expression node;
            for (int i = 0; i < result_list.Count; i++)
            {
                node = result_list[(i)];
                if (node.ExpressionType == ExpressionType.Symbol)
                    if (((Symbol)node).RawText == ("(")/*((Calculator.Symbol) node).symbol_type == Calculator.Symbol.SymbolType.Bracket_Left*/)
                        result_list.Remove(node);
            }
            BSEChain = result_list;
        }

        private bool ExucuteBSE()
        {
            if (BSEChain.Count == 1)
                if (BSEChain[0].ExpressionType == ExpressionType.Digit)
                    return ((((Digit)BSEChain[(0)]).GetDouble())) != 0;
            Stack<Expression> digit_stack = new Stack<Expression>();
            List<Expression> paramterList, result;
            Symbol op;
            try
            {
                foreach (Expression node in BSEChain)
                {
                    if (node.ExpressionType == ExpressionType.Symbol)
                    {
                        op = (Symbol)node;
                        paramterList = new List<Expression>();
                        for (int i = 0; i < op.GetParamterCount(); i++)
                            paramterList.Add(digit_stack.Count == 0 ? new Digit("0") : digit_stack.Pop());
                        paramterList.Reverse();
                        result = op.Solve(paramterList, calculator);
                        foreach (Expression expr in result)
                            digit_stack.Push(expr);
                    }
                    else
                    {
                        if (node.ExpressionType == ExpressionType.Digit || node.ExpressionType == ExpressionType.Variable)
                        {
                            digit_stack.Push(node);
                        }
                        else
                            Log.ExceptionError(new Exception("Unknown Node"));
                    }
                }
            }
            catch (Exception e)
            {
                //Log.ExceptionError(new Exception(e.Message));
                throw e;
            }
            //get last expression in stack as result and output.
            Expression va = digit_stack.Pop();
            if (va.ExpressionType == ExpressionType.Digit)
                return Double.Parse(va.Solve()) != 0;
            if (va.ExpressionType == ExpressionType.Variable)
                if (((Variable)va).VariableType == VariableType.BooleanVariable)
                    return ((BooleanVariable)va).BoolValue;
            Log.ExceptionError(new Exception("Uncalculatable type :" + va.ExpressionType.ToString()));
            return false;
        }

        public bool Solve(string expression)
        {
            rawExpressionChain = ParseExpression(expression);
            ConverVariableToDigit();
            ConverFunctionToDigit();
            ConverToBSE();
            return ExucuteBSE();
        }


        #region Init

        void Init()
        {

            Calculator.RegisterRawVariable(new BooleanVariable(true, null));

            Calculator.RegisterRawVariable(new BooleanVariable(false, null));

            calculator.RegisterOperation(">", 2, 3.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va > vb, calculator));
                return result;
            });

            calculator.RegisterOperation("<", 2, 3.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va < vb, calculator));
                return result;
            });

            calculator.RegisterOperation(">=", 2, 3.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va >= vb, calculator));
                return result;
            });

            calculator.RegisterOperation("<=", 2, 3.0f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va <= vb, calculator));
                return result;
            });

            calculator.RegisterOperation("==", 2, 2.5f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va == vb, calculator));
                return result;
            });

            calculator.RegisterOperation("!=", 2, 2.5f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable(va != vb, calculator));
                return result;
            });

            calculator.RegisterOperation("&&", 2, 2.3f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable((va!=0) && (vb!=0), calculator));
                return result;
            });

            calculator.RegisterOperation("||", 2, 2.3f, (paramsList, calculator) => {
                List<Expression> result = new List<Expression>();
                Expression a = paramsList[0], b = paramsList[1];
                if (!((a.IsCalculatable) && (b.IsCalculatable)))
                    Log.ExceptionError(new Exception("cant take a pair of valid type to calculate."));

                double va = double.Parse(calculator.Solve(a.Solve()));
                double vb = double.Parse(calculator.Solve(b.Solve()));

                result.Add(new BooleanVariable((va!=0) || (vb!=0), calculator));
                return result;
            });
        }

        #endregion
    }
}
