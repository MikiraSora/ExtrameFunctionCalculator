using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Types
{
    public class OnReflectionFunction
    {
        public delegate string OnFunctionSolveFunc(Dictionary<String, Variable> parameter, Calculator calculator);
        public OnFunctionSolveFunc onReflectionFunction=null;

        public string HelpDesc { get; set; } = null;

        public delegate string OnDerivativeConvertParseFunc();
        public OnDerivativeConvertParseFunc onDerivativeConvertParse=null;

        public delegate Dictionary<String, Variable> OnParseParameterFunc(String paramter, Function.ParameterRequestWrapper request, Calculator calculator);
        public OnParseParameterFunc onParseParameter=null;
    }

    public class ReflectionFunction:Function
    {
        static Regex FunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");

        OnReflectionFunction _bind_reflection_function;

        public override FunctionType FunctionType => FunctionType.ReflectionFunction;

        public ReflectionFunction(string expression,OnReflectionFunction onReflectionFunction) : base()
        {
            _bind_reflection_function = onReflectionFunction;
            _raw_text = expression;
            Match result = FunctionFormatRegex.Match(expression);
            if (result.Groups.Count != 3)
                Log.ExceptionError(new Exception("Cannot parse function ：" + expression));
            _function_name = result.Groups[1].Value;
            _function_paramters = result.Groups[2].Value;
            ParameterRequestWrapper parameterRequest = new ParameterRequestWrapper(_function_paramters);
            request = parameterRequest;
        }

        public override string Solve()
        {
            return base.Solve();
        }

        public override string Solve(string parameterList)
        {
            Dictionary<String, Variable> paramter = _bind_reflection_function.onParseParameter?.Invoke(parameterList, request,Calculator);

            if (paramter == null)/*返回null意味着按照国际惯例来解析传入参数*/
                paramter = Parse(parameterList);
                
            return _bind_reflection_function.onReflectionFunction(paramter, Calculator);
        }
    }
}
