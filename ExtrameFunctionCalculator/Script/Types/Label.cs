using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Label : Symbol
    {
        public override SymbolType SymbolType => SymbolType.Label;
        public Label(int line) : base(line)
        {
        }
    }
}
