using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum FunctionType
    {
        Begin, End, Unknown
    }

    public class Function : Statement
    {
        int end_line=-1;

        public int EndLine { get { return end_line; } internal set { end_line = value; } }

        string function_name;

        Parser reference_parser = null;
        public Parser RefParser { get { return reference_parser; } }

        public virtual FunctionType FunctionType { get { return FunctionType.Unknown; } }

        public ExtrameFunctionCalculator.Types.Function.ParameterRequestWrapper request = null;

        static Regex FunctionFormatRegex = new Regex(@"([a-zA-Z]\w*)\((.*)\)");

        public override StatementType StatementType => StatementType.Function; 

        public string FunctionName { get { return function_name; } }

        public Function(int line, string body,Parser parser) : base(line, body)
        {
            if (body == null)
                return;
            Match result = FunctionFormatRegex.Match(statement);
            if (result.Captures.Count != 2)
                throw new Exception("Cant parse function :" + statement);
            //Log.ExceptionError( new Exception("Cannot parse function ：" + expression));
            function_name = result.Captures[1].Value;
            request = new ExtrameFunctionCalculator.Types.Function.ParameterRequestWrapper(result.Captures[2].Value);
            reference_parser = parser;
        }

        public int ParameterRequestCount { get { return request.GetParamterRequestCount(); } }

        public override string ToString() => $"{base.ToString()} - {FunctionType.ToString()}";
    }

    public class FunctionBody : Function
    {
        public FunctionBody(int line, string body, Parser parser) : base(line, body, parser)
        {

        }

        public override FunctionType FunctionType => FunctionType.Begin;
    }

    public class EndFunction : Function
    {
        public EndFunction(int line, Parser parser) : base(line, null, parser)
        {
        }

        public override FunctionType FunctionType => FunctionType.End;
    }
}
