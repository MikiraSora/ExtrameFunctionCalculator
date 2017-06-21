using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public enum LoopType
    {
        LoopBegin, Continue, Break, Endloop, Unknown
    }

    public class Loop:Symbol
    {
        LoopBegin reference_loop = null;
        int end_line = -1;

        public int EndLine { get { return end_line; } internal set { end_line = value; } }

        public LoopBegin ReferenceLoop { get { return reference_loop; } internal set { reference_loop = value; } }
        public override SymbolType SymbolType => SymbolType.LoopBranch;
        public virtual LoopType LoopType { get { return LoopType.Unknown; } }

        public Loop(int line) : base(line)
        {
        }
        public override string ToString() => $"{base.ToString()} : {LoopType.ToString()}";
    }

    public class LoopBegin : Loop
    {
        public override LoopType LoopType => LoopType.LoopBegin;

        public LoopBegin(int line) : base(line)
        {
        }
    }

    public class EndLoop : Loop
    {
        public override LoopType LoopType => LoopType.Endloop;

        public EndLoop(int line) : base(line)
        {
        }
    }

    public class Continue : Loop
    {
        public override LoopType LoopType => LoopType.Continue;

        public Continue(int line) : base(line)
        {
        }
    }

    public class Break : Loop
    {
        public override LoopType LoopType => LoopType.Break;

        public Break(int line) : base(line)
        {
        }
    }
}
