using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public enum VariableType
    {
        ExpressionVariable,
        BooleanVariable,
        MapVariable,
        Normal,
        Unknown
    }

    public class Variable : Expression
    {
        public override ExpressionType ExpressionType { get { return ExpressionType.Variable; } }

        public override bool IsCalculatable => true;

        protected string _variable_name = "";
        public string VariableName { get { return _variable_name; } }

        public virtual VariableType VariableType { get { return VariableType.Normal; } }

        public virtual bool IsSetVariableDirectly { get { return true; } }

        public Variable(string name, string value, Calculator calculator) : base(calculator)
        {
            _raw_text = value;
            _variable_name = name;
        }

        public override string GetName()
        {
            return VariableName;
        }

        public override string ToString()
        {
            String rightValue;
            try
            {
                rightValue = Solve();
            }
            catch (Exception e)
            {
                rightValue = "null";
            }
            return $"{GetName()}={rightValue}";
        }

        public virtual Digit GetDigit()
        {
            return new Digit(_raw_text == null ? Calculator.RequestVariable(this.GetName()) : _raw_text);
        }

        internal virtual void SetValue(string value)
        {
            _raw_text = Calculator.Solve(value);
        }
    }
}
