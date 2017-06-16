using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class Digit : Expression
    {
        internal static bool IsIsPrecisionTruncation { get; set; } = false;

        public override ExpressionType ExpressionType => ExpressionType.Digit;

        public Digit(string digit) : base(null)
        {
            _raw_text = digit;
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }

        public override string Solve()
        {
            return _raw_text;
        }

        public double GetDouble()
        {
            return IsIsPrecisionTruncation ? _CutMaxPerseicelDecimal(Solve()) : double.Parse(Solve());
        }

        private double _CutMaxPerseicelDecimal(String digit)
        {
            if (!digit.Contains('.'))
                return double.Parse(digit);
            int pointPos = digit.IndexOf('.');
            if (digit.Length - pointPos >= 15)
                return double.Parse(digit.Substring(0, pointPos + 14));
            return double.Parse(digit);
        }
    }
}
