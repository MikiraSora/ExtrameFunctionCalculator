using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class WrapperFunction : Function
    {
        Function bind_function = null;
        public WrapperFunction(Function function, string currentParamster)
        {
            bind_function = function;
            this._current_paramsRawText = currentParamster;
        }

        public override string Solve()
        {
            return bind_function.Solve(_current_paramsRawText);
        }
    }
}
