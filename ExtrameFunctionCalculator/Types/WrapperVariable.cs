using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class WrapperVariable : Variable
    {
        Variable bind_variable = null;
        string current_indexes = null;
        public WrapperVariable(Variable variable, string current_indexes) : base(null, null, null)
        {
            bind_variable = variable;
            this.current_indexes = current_indexes;
        }

        public override string Solve()
        {
            return bind_variable.VariableType != VariableType.MapVariable ? bind_variable.Solve() : ((MapVariable)bind_variable).RouteGetVariable(current_indexes).Solve();
        }

        public override Digit GetDigit()
        {
            return new Digit(Solve());
        }
    }
}
