using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.Types
{
    public class Symbol : Expression
    {
        public delegate List<Expression> OnCalculateFunc(List<Expression> parametersList, Calculator refCalculator);
        private Symbol() : base(null) { }

        public override ExpressionType ExpressionType => ExpressionType.Symbol;

        internal static Dictionary<string, OnCalculateFunc> OperatorFunction = new Dictionary<string, OnCalculateFunc>();

        internal static Dictionary<string, float> OperatorPrioty = new Dictionary<string, float>();

        internal static Dictionary<string, Symbol> SharedSymbolCache = new Dictionary<string, Symbol>();

        internal static Dictionary<string, int> OperatorRequestParamterCount = new Dictionary<string, int>();

        public override bool IsCalculatable => base.IsCalculatable;

        public Symbol(string op) : this()
        {
            _raw_text = op;
        }

        public override string GetName()
        {
            throw new NotImplementedException();
        }

        public int CompareOperationPrioty(Symbol symbol)
        {
            float val = OperatorPrioty[(_raw_text)] - OperatorPrioty[symbol.RawText];
            return val == 0 ? 0 : (val > 0 ? 1 : -1);
        }

        public override string ToString() => _raw_text;

        static Symbol GetSymbolMayFromCache(String op) => SharedSymbolCache.ContainsKey(op) ? SharedSymbolCache[(op)] : new Symbol(op);


        public static void RegisterOperation(String operatorSymbol, int requestParamterSize, float operatorPrioty, OnCalculateFunc operatorFunction)
        {
            OperatorFunction.Add(operatorSymbol, operatorFunction);
            OperatorPrioty.Add(operatorSymbol, operatorPrioty);
            OperatorRequestParamterCount.Add(operatorSymbol, requestParamterSize);
            SharedSymbolCache.Add(operatorSymbol, Symbol.GetSymbolMayFromCache(operatorSymbol));
        }

        public List<Expression> Solve(List<Expression> paramterList, Calculator calculator) => OperatorFunction[(_raw_text)](paramterList, calculator);

        public int GetParamterCount() => OperatorRequestParamterCount[_raw_text];
    }
}
