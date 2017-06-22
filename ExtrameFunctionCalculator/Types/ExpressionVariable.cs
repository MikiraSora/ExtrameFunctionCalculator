namespace ExtrameFunctionCalculator.Types
{
    public class ExpressionVariable : Variable
    {
        public override VariableType VariableType => VariableType.ExpressionVariable;

        public ExpressionVariable(string name, string expr_value, Calculator calculator) : base(name, expr_value, calculator)
        {
        }

        public override string Solve()
        {
            return Calculator.Solve(raw_text);
        }

        internal override void SetValue(string value)
        {
            raw_text = value;
        }

        public override Digit GetDigit() => new Digit(Solve());

        public override string ToString() => $"<expr> {VariableName}={RawText}";
    }
}