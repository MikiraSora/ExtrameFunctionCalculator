using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public enum ExpressionType
    {
        Function,
        Variable,
        Digit,
        Symbol,
        Derivative,
        Unknown
    }

    public abstract class Expression
    {

        protected Calculator ref_calculator = null;
        public Calculator Calculator { get { return ref_calculator; } internal set { ref_calculator = value; } }

        protected string raw_text = "";
        public string RawText { get { return raw_text; } internal set { raw_text = value; } }


        private Expression():this(null){}

        public Expression(Calculator calculator) { ref_calculator = calculator; }

        public virtual ExpressionType ExpressionType { get { return ExpressionType.Unknown; } }

        public virtual bool IsCalculatable { get { return false; } }

        public virtual string Solve() => raw_text;

        public abstract string GetName();
    }
}
