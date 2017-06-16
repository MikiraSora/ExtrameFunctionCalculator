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

        public Loop(int line) : base(line)
        {
        }

        public override SymbolType SymbolType => SymbolType.LoopBranch;

        public virtual LoopType LoopType { get { return LoopType.Unknown; } }

        public override string ToString() => $"{base.ToString()} : {LoopType.ToString()}";
    }

    public class LoopBegin : Loop
    {
        public LoopBegin(int line) : base(line)
        {
        }

        public override LoopType LoopType => LoopType.LoopBegin;
    }

    public class EndLoop : Loop
    {
        public EndLoop(int line) : base(line)
        {
        }

        public override LoopType LoopType => LoopType.Endloop;
    }

    public class Continue : Loop
    {
        public Continue(int line) : base(line)
        {
        }

        public override LoopType LoopType => LoopType.Continue;
    }

    public class Break : Loop
    {
        public Break(int line) : base(line)
        {
        }

        public override LoopType LoopType => LoopType.Break;
    }
}
