using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class MapVariable : Variable
    {
        Dictionary<String, Variable> variable_key_map = new Dictionary<string, Variable>();
        public override VariableType VariableType => VariableType.MapVariable;

        public MapVariable(string variable_name, Calculator calculator) : base(variable_name, null, calculator)
        {
        }

        public MapVariable(string variable_name, string indexes, string variable_value, Calculator calculator) : this(variable_name, calculator)
        {
            SetValue(indexes, variable_value);
        }
        internal override void SetValue(string value)
        {
            throw new Exception("MapVariable cant call SetValue(String) directly!");
        }

        internal void SetValue(string indexes, string value)
        {
            RouteSetVariable(indexes, value);
        }

        internal void SetValue(string path, Variable variable)
        {
            if (!IsKeyPath(path))
                path = Calculator.Solve(path);
            variable_key_map.Add(path, variable);
        }

        public override bool IsSetVariableDirectly => false;

        public Variable RouteGetVariable(string indexes)
        {
            List<String> getList = ParseIndexesString(indexes);
            if (getList.Count == 0)
                throw new Exception(String.Format("{0} isnt vaild indexes", indexes));

            Variable variable = null, currentVariable = null;
            foreach (string var_name in getList)
            {
                if (currentVariable != null)
                {
                    if (currentVariable.VariableType == VariableType.MapVariable)
                        variable = ((MapVariable)variable).GetVariable(var_name);
                    else
                        throw new Exception(String.Format("{0} isnt vaild index", var_name));
                }
                else
                    variable = this.GetVariable(var_name);
                if (variable == null)
                    throw new Exception("cant found the variable " + var_name);
                currentVariable = variable;
            }
            return currentVariable;
        }

        private /*Variable*/void RouteSetVariable(string indexes, string value)
        {
            List<String> getList = ParseIndexesString(indexes);
            if (getList.Count == 0)
                throw new Exception(String.Format("{0} isnt vaild indexes", indexes));
            Variable variable = null, currentVariable = null, tmp_variable;
            string var_name;
            for (int position = 0; position < getList.Count; position++)
            {
                var_name = getList[(position)];
                //能用循环艹的就打死不要用递归
                if (currentVariable != null)
                {
                    if (currentVariable.VariableType == VariableType.MapVariable)
                        variable = ((MapVariable)currentVariable).GetVariable(var_name);
                    else
                        throw new Exception(String.Format("{0} isnt vaild index", var_name));
                }
                else
                    variable = this.GetVariable(var_name);

                if (variable == null)
                {
                    variable = currentVariable == null ? this : currentVariable;

                    tmp_variable = (position != getList.Count - 1) ? new MapVariable(var_name, Calculator) : new Variable(var_name, null, Calculator);

                    ((MapVariable)variable).SetValue(var_name, tmp_variable);
                    variable = tmp_variable;
                }
                currentVariable = variable;
            }

            currentVariable.SetValue(value);
        }

        public Variable GetVariable(string index)
        {
            //return variable_key_map.containsKey(index)?variable_key_map.get(index):null;
            if (IsKeyPath(index))
                return variable_key_map.ContainsKey(index) ? variable_key_map[(index)] : null;
            index = Calculator.Solve(index);
            return variable_key_map.ContainsKey(index) ? variable_key_map[(index)] : null;
        }

        public bool IsKeyPath(string path) => (path[0] == path[(path.Length - 1)] && path[(0)] == '"');

        private List<String> ParseIndexesString(string indexes)
        {
            //input "[index][rin]"
            List<String> IndexesList = new List<string>();
            Stack<int> recordPosition = new Stack<int>();
            int recordpos = -1;
            int position = -1;
            char c = (char)0;
            string indexString;
            while (true)
            {
                position++;
                if (position >= indexes.Length)
                    break;
                c = indexes[(position)];
                if (c == '[')
                {
                    recordPosition.Push(position);
                    continue;
                }
                if (c == ']')
                {
                    recordpos = recordPosition.Pop();
                    if (recordPosition.Count == 0)
                    {
                        indexString = indexes.Substring(recordpos + 1, position-recordpos-1);
                        IndexesList.Add(indexString);
                    }
                    continue;
                }
            }
            if (!(recordPosition.Count == 0))
                throw new Exception(String.Format("{0} isnt balance", indexes));
            return IndexesList;
        }

        public override string Solve() => throw new Exception("cant call MapVariable::Solve() !");

    }
}
