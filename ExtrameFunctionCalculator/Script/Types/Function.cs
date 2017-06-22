using System;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum FunctionType
    {
        Begin, End, Unknown
    }

    public class Function : Statement
    {
        private int end_line = -1;

        private string function_name;
        private Parser reference_parser = null;
        public ExtrameFunctionCalculator.Types.Function.ParameterRequestWrapper request = null;
        private static Regex check_function_format_regex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");
        public int EndLine { get { return end_line; } internal set { end_line = value; } }
        public Parser RefParser { get { return reference_parser; } }

        public virtual FunctionType FunctionType { get { return FunctionType.Unknown; } }
        public override StatementType StatementType => StatementType.Function;

        public string FunctionName { get { return function_name; } }
        public int ParameterRequestCount { get { return request.GetParamterRequestCount(); } }

        public Function(int line, string body, Parser parser) : base(line, body)
        {
            if (body == null)
                return;
            Match result = check_function_format_regex.Match(statement);
            if (result.Groups.Count != 3)
                throw new Exception("Cant parse function :" + statement);
            //Log.ExceptionError( new Exception("Cannot parse function ：" + expression));
            function_name = result.Groups[1].Value;
            request = new ExtrameFunctionCalculator.Types.Function.ParameterRequestWrapper(result.Groups[2].Value);
            reference_parser = parser;
        }

        public override string ToString() => $"{base.ToString()} - {FunctionType.ToString()}";
    }

    public class FunctionBody : Function
    {
        public override FunctionType FunctionType => FunctionType.Begin;

        public FunctionBody(int line, string body, Parser parser) : base(line, body, parser)
        {
        }
    }

    public class EndFunction : Function
    {
        public override FunctionType FunctionType => FunctionType.End;

        public EndFunction(int line, Parser parser) : base(line, null, parser)
        {
        }
    }
}