using System;
using System.Collections.Generic;

namespace ExtrameFunctionCalculator.Types
{
    public class Symbol : Expression
    {
        public override ExpressionType ExpressionType => ExpressionType.Symbol;
        public override bool IsCalculatable => base.IsCalculatable;

        private Symbol() : base(null)
        {
        }

        public Symbol(string op, Calculator calculator) : base(calculator)
        {
            raw_text = op;
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }

        public int CompareOperationPrioty(Symbol symbol)
        {
            float val = Calculator.operator_info[raw_text].prioty - Calculator.operator_info[symbol.RawText].prioty;
            return val == 0 ? 0 : (val > 0 ? 1 : -1);
        }

        public override string ToString() => raw_text;

        public List<Expression> Solve(List<Expression> paramterList, Calculator calculator) => Calculator.operator_info[raw_text].call_function(paramterList, calculator);
    }
}