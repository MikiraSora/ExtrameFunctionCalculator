using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Call : Statement
    {
        public override StatementType StatementType => StatementType.Call;

        public Call(int line, string statement) : base(line, statement)
        {
        }

    }
}
