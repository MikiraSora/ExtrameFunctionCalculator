using System;

namespace ExtrameFunctionCalculator.Types
{
    [Flags]
    public enum ExpressionType
    {
        Function = 1,
        Variable = 2,
        Digit = 4,
        Symbol = 8,
        Derivative = 16,
        Unknown = 32
    }

    public abstract class Expression
    {
        protected Calculator ref_calculator = null;
        public Calculator Calculator { get { return ref_calculator; } internal set { ref_calculator = value; } }

        protected string raw_text = string.Empty;
        public string RawText { get { return raw_text; } internal set { raw_text = value; } }

        private Expression() : this(null)
        {
        }

        public Expression(Calculator calculator)
        {
            ref_calculator = calculator;
        }

        public virtual ExpressionType ExpressionType { get { return ExpressionType.Unknown; } }

        public virtual bool IsCalculatable { get { return false; } }

        public virtual string Solve() => raw_text;

        public abstract string GetName();
    }
}