using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum StatementType
    {
        Function,
        Call,
        Return,
        Goto,
        Set,
        Unknown
    }

    public abstract class Statement : Unit
    {
        protected string statement;

        public string StatementContext
        {
            get { return statement; }
        }

        public delegate void ExecuteAction(Object obj);

        public virtual StatementType StatementType { get { return StatementType.Unknown; } }

        public Statement(int line,string statement) : base(line)
        {
            this.statement = statement;
        }

        public override string ToString() => $"{base.ToString()} - {StatementType.ToString()} : {statement}";

        public override UnitType UnitType => UnitType.Statement;

        public virtual void Execute()
        {

        }
    }
}
