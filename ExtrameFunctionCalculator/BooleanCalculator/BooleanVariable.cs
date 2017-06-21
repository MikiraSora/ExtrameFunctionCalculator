using ExtrameFunctionCalculator.Types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.BooleanCalculatorSupport
{
    class BooleanVariable : ExpressionVariable
    {
        bool boolean_value = false;
        static string TRUE = "true", FALSE = "false";

        public bool BoolValue
        {
            get { return boolean_value; }
        }
        public override VariableType VariableType => VariableType.BooleanVariable;
        public override bool IsCalculatable => false;
        public override bool IsSetVariableDirectly => false;
        public BooleanVariable(string name, string expression, Calculator c) : base(name, expression, c)
        {
            boolean_value = expression == TRUE ? true : expression == FALSE ? false : (Double.Parse(Calculator.Solve(expression)) == 0);
        }

        public BooleanVariable(bool value, Calculator calculator1):base("",value.ToString(), calculator1)
        {
            boolean_value=value;
        }

        ExpressionVariable Copy()
        {
            try
            {
                return this;//new BooleanVariable(boolean_value,calculator);
            }
            catch (Exception e)
            {
                return null;
            }
        }
        public override string Solve() => boolean_value ? "1" : "0";

        public override Digit GetDigit() => new Digit(Solve());

        public override string GetName() => boolean_value ? TRUE : FALSE;
    }
}
