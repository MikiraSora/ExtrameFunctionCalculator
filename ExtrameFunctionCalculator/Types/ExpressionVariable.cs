using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class ExpressionVariable:Variable
    {
        public override VariableType VariableType => VariableType.ExpressionVariable;

        public ExpressionVariable(string name, string expr_value, Calculator calculator):base(name,expr_value,calculator)
        {

        }

        public override string Solve()
        {
            return Calculator._Solve(_raw_text);
        }

        internal override void SetValue(string value)
        {
            _raw_text = value;
        }

        public override Digit GetDigit() => new Digit(Solve());

        public override string ToString() => $"<expr> {VariableName}={RawText}";
    }
}
