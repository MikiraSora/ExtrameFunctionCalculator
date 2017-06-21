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
        public override SymbolType SymbolType => SymbolType.ConditionBranch;

        public virtual ConditionType ConditionType { get { return ConditionType.Unknown; } }

        public Condition(int line) : base(line)
        {
        }

        public override string ToString() => $"{base.ToString()} : {ConditionType.ToString()}";
    }

    public class If : Condition
    {
        string condition;

        public string ConditionExpression { get { return condition; } }

        public override ConditionType ConditionType => ConditionType.If;

        public If(int line, string condition) : base(line)
        {
            this.condition = condition;
        }
    }

    public class Then : Condition
    {
        public override ConditionType ConditionType => ConditionType.Then;

        public Then(int line) : base(line)
        {
        }
    }

    public class Else: Condition
    {
        public override ConditionType ConditionType => ConditionType.Else;

        public Else(int line) : base(line)
        {
        }
    }

    public class EndIf : Condition
    {
        public override ConditionType ConditionType => ConditionType.EndIf;

        public EndIf(int line) : base(line)
        {
        }
    }
}

