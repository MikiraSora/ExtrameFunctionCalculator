using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Script.Types
{
    public class Set : Statement
    {
        string variable_name = null, variable_value = null;

        public string VariableName { get { return variable_name; } }

        public override StatementType StatementType => StatementType.Set;
        public string VariableValue { get { return variable_value; } }

        public Set(int line, string statement) : base(line, statement)
        {
            char c;
            variable_name = "";
            for (int position = 0; position < this.statement.Length - 1; position++)
            {
                c = this.statement[position];
                if (c == '=')
                {
                    variable_value = this.statement.Substring(position + 1);
                    break;
                }
                else
                {
                    variable_name += c;
                }
            }
        }
    }
}
