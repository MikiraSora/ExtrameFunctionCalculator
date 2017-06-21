using ExtrameFunctionCalculator.Script;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class ScriptFunction : Function
    {
        string function_name = null;
        Executor executor = null;

        public override FunctionType FunctionType => FunctionType.ScriptFunction;

        public ScriptFunction(string function_name, Executor executor, Calculator calculator) : base(null, calculator)
        {
            this.function_name = function_name;
            this.executor = executor;
            request = executor.RefParser.function_table[(function_name)].request;
        }

        public override string Solve(string parameterList)
        {
            Dictionary<string,Variable> parameters=Parse(parameterList);
            //// TODO: 2016/11/2 将ArrayList转化成Hashmap
            List<Expression> arrayList = new List<Expression>();
            foreach (var pair in parameters)
                arrayList.Add(pair.Value);
            return executor.ExecuteFunction(function_name, arrayList);
        }

        public override string Solve()
        {
            return base.Solve();
        }

    }
}
