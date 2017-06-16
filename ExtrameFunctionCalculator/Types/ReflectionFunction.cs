using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Types
{
    public interface OnReflectionFunction
    {
        string onReflectFunction(Dictionary<String, Variable> parameter, Calculator calculator);
        string HelpDesc { get; }
        bool IsDierivativable { get; }
        string onDerivativeConvertParse();
        Dictionary<String, Variable> onParseParameter(String paramter, Function.ParameterRequestWrapper request, Calculator calculator);
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
            Dictionary<String, Variable> custom_paramter = _bind_reflection_function.onParseParameter(parameterList, request,Calculator);

            if (custom_paramter == null)/*返回null意味着按照国际惯例来解析传入参数*/
                Parse(parameterList);
            else
                parameters = custom_paramter;
                
            return _bind_reflection_function.onReflectFunction(parameters, Calculator);
        }
    }
}
