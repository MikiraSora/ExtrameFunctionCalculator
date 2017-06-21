using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Return : Statement
    {
        public override StatementType StatementType => StatementType.Return;

        public Return(int line, string statement) : base(line, statement)
        {
        }
    }
}
