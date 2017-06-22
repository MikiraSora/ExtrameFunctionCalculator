namespace ExtrameFunctionCalculator.Script.Types
{
    public class Label : Symbol
    {
        public override SymbolType SymbolType => SymbolType.Label;

        public Label(int line) : base(line)
        {
        }
    }
}