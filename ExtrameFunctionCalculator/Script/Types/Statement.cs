using System;

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
        public delegate void ExecuteAction(Object obj);

        protected string statement;

        public virtual StatementType StatementType { get { return StatementType.Unknown; } }

        public override UnitType UnitType => UnitType.Statement;

        public string StatementContext
        {
            get { return statement; }
        }

        public Statement(int line, string statement) : base(line)
        {
            this.statement = statement;
        }

        public override string ToString() => $"{base.ToString()} - {StatementType.ToString()} : {statement}";

        public virtual void Execute()
        {
        }
    }
}