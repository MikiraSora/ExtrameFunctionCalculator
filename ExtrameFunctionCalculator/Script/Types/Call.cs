using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Call : Statement
    {
        public Call(int line, string statement) : base(line, statement)
        {
        }

        public override StatementType StatementType => StatementType.Call;
    }
}
