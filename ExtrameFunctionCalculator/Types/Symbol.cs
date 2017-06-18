using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class Symbol : Expression
    {
        private Symbol() : base(null) { }

        public override ExpressionType ExpressionType => ExpressionType.Symbol;

        public override bool IsCalculatable => base.IsCalculatable;

        public Symbol(string op,Calculator calculator) : base(calculator)
        {
            _raw_text = op;
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }

        public int CompareOperationPrioty(Symbol symbol)
        {
            float val = Calculator.OperatorPrioty[(_raw_text)] - Calculator.OperatorPrioty[symbol.RawText];
            return val == 0 ? 0 : (val > 0 ? 1 : -1);
        }

        public override string ToString() => _raw_text;
        
        public List<Expression> Solve(List<Expression> paramterList, Calculator calculator) => Calculator.OperatorFunction[(_raw_text)](paramterList, calculator);

        public int GetParamterCount() => Calculator.OperatorRequestParamterCount[_raw_text];
    }
}
