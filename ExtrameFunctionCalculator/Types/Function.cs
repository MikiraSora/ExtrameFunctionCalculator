using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Types
{
    [Flags]
    public enum FunctionType
    {
        NormalFunction=1,
        ReflectionFunction=2,
        ScriptFunction=4,
        Unknown=8
    }

    public class Function : Expression
    {
        #region ParameterRequestWrapper

        public class ParameterRequestWrapper : List<String>
        {
            private ParameterRequestWrapper()
            {
            }

            public ParameterRequestWrapper(string paramRawText)
            {
                string[] parametersArray = paramRawText.Split(',');
                this.AddRange(parametersArray);
            }

            public int GetParamterRequestCount() => this.Count;

            public string[] GetParamterNameArray() => this.ToArray();

            public string GetParamterName(int index) => this[index];
        }

        #endregion ParameterRequestWrapper

        private static Regex FunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)=(.+)");

        protected string function_name, function_paramters, function_body;

        private static Regex check_function_format_regex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");

        protected ParameterRequestWrapper request;

        public override ExpressionType ExpressionType { get { return ExpressionType.Function; } }

        public virtual FunctionType FunctionType { get { return FunctionType.NormalFunction; } }

        protected Function() : base(null)
        {
        }

        public Function(string expression, Calculator calculator) : base(calculator)
        {
            if (expression == null || expression.Length == 0)
                return;
            raw_text = expression;
            Match result = FunctionFormatRegex.Match(expression);
            if (result.Groups.Count != 4)
                Log.ExceptionError(new Exception("Cannot parse function ：" + expression));
            function_name = result.Groups[1].Value;
            function_paramters = result.Groups[2].Value;
            ParameterRequestWrapper parameterRequest = new ParameterRequestWrapper(function_paramters);
            request = parameterRequest;
            function_body = result.Groups[3].Value;
        }

        protected Dictionary<string, Variable> Parse(string paramsRawText)
        {
            char c;
            int requestIndex = 0;
            Stack<int> BracketStack = new Stack<int>();
            Dictionary<string, Variable> paramsMap = new Dictionary<string, Variable>();
            string paramter = "";
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
                    paramsMap[requestParamterName] = new Variable(requestParamterName, this.Calculator.Solve(paramter), this.Calculator);
                    paramter = "";
                }
                else
                {
                    paramter += c;
                }
            }
            if ((paramter.Length != 0))
                paramsMap[request[(requestIndex)]] = new ExpressionVariable(request[(requestIndex)], this.Calculator.Solve(paramter), this.Calculator);

            return paramsMap;
        }

        private string ParseDeclaring(string expression, Dictionary<string, Variable> parameters)
        {
            string newExpression = expression;
            foreach (var pair in parameters)
            {
                newExpression = newExpression.Replace(pair.Key, "(" + pair.Value.Solve() + ")");
            }
            return newExpression;
        }

        public override string GetName() => function_name;

        public virtual string Solve(string paramsRawText)
        {
            Dictionary<string, Variable> parameters = Parse(paramsRawText);
            if (parameters.Count != request.GetParamterRequestCount())
                Log.ExceptionError(new Exception($"function \"{function_name}\" requests {request.GetParamterRequestCount()} paramter(s) but you input {parameters.Count} paramter(s)"));
            string exression;
            exression = ParseDeclaring(function_body, parameters);
            return Calculator.Solve(exression);
        }

        public override string Solve() => Solve("");

        public void BindCalculator(Calculator calculator)
        {
            ref_calculator = calculator;
        }

        public override string ToString() => $"{function_name}({function_paramters})={function_body}";

        public Digit GetSolveToDigit() => new Digit(Solve());

        //Stack<string> paramsExpressionStack = new Stack<string>();

        //public void PushParam(string paramsRawExpression) => paramsExpressionStack.Push(paramsRawExpression);
        //protected string PopParam() => paramsExpressionStack.Count==0?"":paramsExpressionStack.Pop();
    }
}