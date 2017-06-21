using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class ComboExpression : Expression
    {
        List<Expression> expression_list;

        public ComboExpression(List<Expression> expression_list,Calculator calculator) : base(calculator)
        {
            this.expression_list = expression_list;
        }

        public override string Solve()
        {
            return base.Solve();
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }
    }
}
