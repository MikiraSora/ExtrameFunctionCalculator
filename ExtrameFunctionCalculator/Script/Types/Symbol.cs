using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum SymbolType
    {
        ConditionBranch,
        LoopBranch,
        Label,
        Unknown
    }

    public class Symbol : Unit
    {
        public virtual SymbolType SymbolType { get { return SymbolType.Unknown; } }

        public override UnitType UnitType => UnitType.Symbol;

        public Symbol(int line) : base(line)
        {

        }

        public override string ToString() => $"{base.ToString()} - {SymbolType.ToString()}";
    }
}
