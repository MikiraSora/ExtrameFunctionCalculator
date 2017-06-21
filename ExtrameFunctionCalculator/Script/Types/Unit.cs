using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum UnitType
    {
        Statement,
        Symbol,
        Unknown
    }

    public class Unit
    {

        protected int line = -1;
        public int Line { get { return line; } }

        public virtual UnitType UnitType { get { return UnitType.Unknown; } }

        public Unit(int line) {
            this.line = line;
        }

    }
}
