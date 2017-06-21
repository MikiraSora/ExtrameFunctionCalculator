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
        #region ParameterRequestWrapper

        public class ParameterRequestWrapper : List<String>
        {
            private ParameterRequestWrapper() { }

            public ParameterRequestWrapper(string paramRawText)
            {
                string[] parametersArray = paramRawText.Split(',');
                this.AddRange(parametersArray);
            }

            public int GetParamterRequestCount() => this.Count;

            public string[] GetParamterNameArray() => this.ToArray();

            public string GetParamterName(int index) => this[index];
        }

        #endregion

        protected string _function_name, _function_paramters, _function_body;

        protected ParameterRequestWrapper request;
        
        public override ExpressionType ExpressionType { get { return ExpressionType.Function; } }

        public virtual FunctionType FunctionType { get { return FunctionType.NormalFunction; } }

        protected Function() : base(null) { }

        static Regex FunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)=(.+)");
        
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
                    paramsMap[requestParamterName] = new Variable(requestParamterName, this.Calculator._Solve(paramter), this.Calculator);
                    paramter = "";
                }
                else
                {
                    paramter += c;
                }
            }
            if ((paramter.Length != 0))
                paramsMap[request[(requestIndex)]] = new ExpressionVariable(request[(requestIndex)], this.Calculator._Solve(paramter), this.Calculator);

            return paramsMap;
        }

        string ParseDeclaring(string expression, Dictionary<string, Variable> parameters)
        {
            string newExpression = expression;
            foreach (var pair in parameters)
            {
                newExpression = newExpression.Replace(pair.Key, "(" + pair.Value.Solve() + ")");
            }
            return newExpression;
        }

        public override string GetName() => _function_name;

        public virtual string Solve(string paramsRawText)
        {
            Dictionary<string, Variable>  parameters =Parse(paramsRawText);
            if (parameters.Count != request.GetParamterRequestCount())
                Log.ExceptionError(new Exception($"function \"{_function_name}\" requests {request.GetParamterRequestCount()} paramter(s) but you input {parameters.Count} paramter(s)"));
            string exression;
            exression = ParseDeclaring(_function_body,parameters);
            return Calculator._Solve(exression);
        }

        public override string Solve() => Solve("");

        public void BindCalculator(Calculator calculator)
        {
            _ref_calculator = calculator;
        }

        public override string ToString() => $"{_function_name}({_function_paramters})={_function_body}";

        static Regex checkFunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");
        
        public Digit GetSolveToDigit() => new Digit(Solve());

        //Stack<string> paramsExpressionStack = new Stack<string>();

        //public void PushParam(string paramsRawExpression) => paramsExpressionStack.Push(paramsRawExpression);
        //protected string PopParam() => paramsExpressionStack.Count==0?"":paramsExpressionStack.Pop();
    }
}