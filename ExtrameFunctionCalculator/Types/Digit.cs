﻿using System;
using System.Linq;

namespace ExtrameFunctionCalculator.Types
{
    public class Digit : Expression
    {
        internal static bool IsIsPrecisionTruncation { get; set; } = false;

        public override ExpressionType ExpressionType => ExpressionType.Digit;

        public override bool IsCalculatable => true;

        public Digit(string digit) : base(null)
        {
            raw_text = digit;
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }

        public override string Solve()
        {
            return raw_text;
        }

        public double GetDouble()
        {
            return IsIsPrecisionTruncation ? CutMaxPerseicelDecimal(Solve()) : double.Parse(Solve());
        }

        private double CutMaxPerseicelDecimal(string digit)
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