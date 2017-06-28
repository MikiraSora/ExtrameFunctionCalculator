using ExtrameFunctionCalculator.UtilTools;
using System;
using System.Collections.Generic;

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
        private OnReflectionFunction bind_reflection_function;

        public override FunctionType FunctionType => FunctionType.ReflectionFunction;

        public ReflectionFunction(string expression, OnReflectionFunction onReflectionFunction) : base()
        {
            bind_reflection_function = onReflectionFunction;
            raw_text = expression;

            if (!ParserUtils.TryParseTextToFunctionDeclear(expression, out function_name, out function_paramters))
                Log.ExceptionError(new Exception("Cannot parse function ：" + expression));
            request = new ParameterRequestWrapper(function_paramters);
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