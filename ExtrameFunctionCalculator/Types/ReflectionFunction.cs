using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Types
{
    public class OnReflectionFunction
    {
        public delegate string OnFunctionSolveFunc(Dictionary<String, Variable> parameter, Calculator calculator);

        public OnFunctionSolveFunc onReflectionFunction = null;

        public string HelpDesc { get; set; } = null;

        public delegate string OnDerivativeConvertParseFunc();

        public OnDerivativeConvertParseFunc onDerivativeConvertParse = null;

        public delegate Dictionary<String, Variable> OnParseParameterFunc(string paramter, Function.ParameterRequestWrapper request, Calculator calculator);

        public OnParseParameterFunc onParseParameter = null;
    }

    public class ReflectionFunction : Function
    {
        private static Regex check_function_format_regex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");

        private OnReflectionFunction bind_reflection_function;

        public override FunctionType FunctionType => FunctionType.ReflectionFunction;

        public ReflectionFunction(string expression, OnReflectionFunction onReflectionFunction) : base()
        {
            bind_reflection_function = onReflectionFunction;
            raw_text = expression;
            Match result = check_function_format_regex.Match(expression);
            if (result.Groups.Count != 3)
                Log.ExceptionError(new Exception("Cannot parse function ：" + expression));
            function_name = result.Groups[1].Value;
            function_paramters = result.Groups[2].Value;
            ParameterRequestWrapper parameterRequest = new ParameterRequestWrapper(function_paramters);
            request = parameterRequest;
        }

        public override string Solve()
        {
            return base.Solve();
        }

        public override string Solve(string parameterList)
        {
            Dictionary<String, Variable> paramter = bind_reflection_function.onParseParameter?.Invoke(parameterList, request, Calculator);

            if (paramter == null)/*返回null意味着按照国际惯例来解析传入参数*/
                paramter = Parse(parameterList);

            return bind_reflection_function.onReflectionFunction(paramter, Calculator);
        }
    }
}