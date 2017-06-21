using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Goto : Statement
    {
        public override StatementType StatementType => StatementType.Goto;

        public Goto(int line, string statement) : base(line, statement)
        {
        }
    }
}
