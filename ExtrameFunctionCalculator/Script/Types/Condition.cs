using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum ConditionType
    {
        If, Then, Else, EndIf, Unknown
    }

    public class Condition : Symbol
    {
        int end_line = -1, else_line = -1;

        public int EndLine { get { return end_line; } internal set { end_line = value; } }

        public int ElseLine { get { return else_line; } internal set { else_line = value; } }

        public virtual ConditionType ConditionType { get { return ConditionType.Unknown; } }

        public Condition(int line) : base(line)
        {
        }

        public override string ToString() => $"{base.ToString()} : {ConditionType.ToString()}";

        public override SymbolType SymbolType => SymbolType.ConditionBranch;
    }

    public class If : Condition
    {
        string condition;

        public string ConditionExpression { get { return condition; } }

        public If(int line,string condition) : base(line)
        {
            this.condition = condition;
        }

        public override ConditionType ConditionType => ConditionType.If;
    }

    public class Then : Condition
    {
        public Then(int line) : base(line)
        {
        }

        public override ConditionType ConditionType => ConditionType.Then;
    }

    public class Else: Condition
    {
        public Else(int line) : base(line)
        {
        }

        public override ConditionType ConditionType => ConditionType.Else;
    }

    public class EndIf : Condition
    {
        public EndIf(int line) : base(line)
        {
        }

        public override ConditionType ConditionType => ConditionType.EndIf;
    }
}

