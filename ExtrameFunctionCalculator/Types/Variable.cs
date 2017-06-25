using System;

namespace ExtrameFunctionCalculator.Types
{
    [Flags]
    public enum VariableType
    {
        ExpressionVariable=1,
        BooleanVariable=2,
        MapVariable=4,
        Normal=8,
        Unknown=16
    }

    public class Variable : Expression
    {
        protected string variable_name = string.Empty;
        public override ExpressionType ExpressionType { get { return ExpressionType.Variable; } }

        public override bool IsCalculatable => true;
        public string VariableName { get { return variable_name; } }

        public virtual VariableType VariableType { get { return VariableType.Normal; } }

        public virtual bool IsSetVariableDirectly { get { return true; } }

        public Variable(string name, string value, Calculator calculator) : base(calculator)
        {
            raw_text = value;
            variable_name = name;
        }

        public override string GetName()
        {
            return VariableName;
        }

        public override string ToString()
        {
            string rightValue;
            try
            {
                rightValue = Solve();
            }
            catch
            {
                rightValue = "null";
            }
            return $"{GetName()}={rightValue}";
        }

        public virtual Digit GetDigit()
        {
            return new Digit(raw_text == null ? Calculator.RequestVariable(this.GetName()) : raw_text);
        }

        internal virtual void SetValue(string value)
        {
            raw_text = Calculator.Solve(value);
        }
    }
}