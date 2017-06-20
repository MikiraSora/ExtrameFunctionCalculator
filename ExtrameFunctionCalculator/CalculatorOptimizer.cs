using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ExtrameFunctionCalculator.Types;

namespace ExtrameFunctionCalculator
{
    class CalculatorOptimizer
    {

        #region 分析基本类

        private class Expression
        {
            public enum ExpressionType
            {
                Operator,
                Digit,
                Fraction,
                Unknown
            }

            ExtrameFunctionCalculator.Types.Expression Cal_ExpressionReference = null;

            public virtual ExpressionType OptExpressionType { get { return Expression.ExpressionType.Unknown; } }

            private Expression() { }
            public Expression(ExtrameFunctionCalculator.Types.Expression cal_ExpressionReference) { Cal_ExpressionReference = cal_ExpressionReference; }

            public virtual ExtrameFunctionCalculator.Types.Expression GetExpressionReference() => Cal_ExpressionReference;
        }

        private class Operator : Expression
        {
            public Operator(ExtrameFunctionCalculator.Types.Symbol symbol) : base(symbol)
            {

            }

            public override ExpressionType OptExpressionType => ExpressionType.Operator;

            public ExtrameFunctionCalculator.Types.Symbol GetOperatorReference()=> (ExtrameFunctionCalculator.Types.Symbol)GetExpressionReference();

            private float GetOperatorPriorty() { return GetOperatorReference().Calculator.OperatorPrioty.ContainsKey(GetOperatorReference().RawText) ? GetOperatorReference().Calculator.OperatorPrioty[(GetOperatorReference().RawText)] : -1; }

            public bool isSameLevelLayout(Operator op) => this.GetOperatorPriorty() == op.GetOperatorPriorty();

            public bool isSameLevelLayout(String operatorChar) => this.GetOperatorPriorty() == (GetOperatorReference().Calculator.OperatorPrioty.ContainsKey(operatorChar) ? GetOperatorReference().Calculator.OperatorPrioty[(operatorChar)] : 0);

            public bool isBaseOperator()
            {
                switch (GetOperatorReference().RawText)
                {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        return true;
                }
                return false;
            }

            public override string ToString() => GetOperatorReference().RawText;
        }

        private class Digit : Expression
        {
            public Digit(ExtrameFunctionCalculator.Types.Digit digit) : base(digit)
            { }

            public override ExpressionType OptExpressionType => ExpressionType.Digit;

            public virtual ExtrameFunctionCalculator.Types.Digit GetDigitReference() { return (ExtrameFunctionCalculator.Types.Digit)GetExpressionReference(); }

            public override string ToString()
            {
                return GetDigitReference().RawText;
            }
        }

        private class ExpressionDigit:Digit
        {
            Calculator calculator = null;
            Calculator GetCalculator() => calculator == null ? calculator = new Calculator() : calculator;
            ExtrameFunctionCalculator.Types.Digit DigitResult = null;

            List<Expression> expressionArrayList = null;

            public ExpressionDigit(List<Expression> expressions, Calculator calculator):base(null)
            {
                this.calculator = calculator;
                expressionArrayList = expressions;
                try
                {
                    DigitResult = new ExtrameFunctionCalculator.Types.Digit(GetCalculator().Solve(ToString()));
                }
                catch (Exception e)
                {
                    DigitResult = new ExtrameFunctionCalculator.Types.Digit("0");
                }
                expressionArrayList = null;

            }

            public override ExtrameFunctionCalculator.Types.Digit GetDigitReference() => DigitResult;

            public override ExtrameFunctionCalculator.Types.Expression GetExpressionReference() => this.GetDigitReference();

            public override string ToString()
            {
                StringBuilder stringBuilder = new StringBuilder();
                foreach (Expression expression in expressionArrayList)
                {
                    stringBuilder.Append(expression.ToString());
                }
                return stringBuilder.ToString();
            }
        }

        private class Fraction:Expression
        {
            private double Numerator = 1;
            private double Denominator;

            public override ExpressionType OptExpressionType => ExpressionType.Fraction;

            public Fraction(double numerator, double denominator):base(null) { Numerator = numerator; Denominator = denominator; }
            public Fraction(double denominator) : base(null) { Denominator = denominator; }

            public Fraction Multi(Fraction fraction) { return new Fraction(this.Numerator * fraction.Numerator, this.Denominator * fraction.Denominator); }
        }

        #endregion

        private bool enableOptimize = false;
        private int optimizeLevel = 1;
        private Calculator calculator = null;

        public CalculatorOptimizer(Calculator bindCalculator)
        {
            calculator = bindCalculator;
        }

        public int OptimizeLevel { get { return optimizeLevel; } set { optimizeLevel = value; } }
        
        public bool Enable { get { return enableOptimize; } set { enableOptimize = value; } }

        public List<ExtrameFunctionCalculator.Types.Expression> OptimizeExpression(List<ExtrameFunctionCalculator.Types.Expression> expressionArrayList)
        {
            if (!enableOptimize)
                return null;
            List<Expression> tmpAnalyseExpressionArrayList = AnalyseConver(expressionArrayList);
            expressionArrayList = null;

            if (OptimizeLevel >= 1)
                tmpAnalyseExpressionArrayList = Level1_Optimize(tmpAnalyseExpressionArrayList, calculator);

            /* 封印
            if (OptimizeLevel >= 2)
                tmpAnalyseExpressionArrayList = Level2_Optimize(tmpAnalyseExpressionArrayList, calculator);
            */

            expressionArrayList = ConverToCalExpression(tmpAnalyseExpressionArrayList);
            return expressionArrayList;
        }

        private List<Expression> AnalyseConver(List<ExtrameFunctionCalculator.Types.Expression> expressionArrayList)
        {
            List<Expression> result = new List<Expression>(), bracketList = null;
            Stack<int> stack = new Stack<int>();
            ExtrameFunctionCalculator.Types.Expression expression = null;
            ExtrameFunctionCalculator.Types.Symbol symbol = null;
            for (int position = 0; position < expressionArrayList.Count; position++)
            {
                expression = expressionArrayList[(position)];
                if (expression.ExpressionType == ExtrameFunctionCalculator.Types.ExpressionType.Digit)
                {
                    result.Add(new Digit((ExtrameFunctionCalculator.Types.Digit)expression));
                    continue;
                }
                if (expression.ExpressionType == ExtrameFunctionCalculator.Types.ExpressionType.Symbol)
                {
                    symbol = (ExtrameFunctionCalculator.Types.Symbol)expression;
                    if (symbol.RawText==("("))
                    {
                        stack.Count();
                        stack.Push(position);
                        bracketList = new List<Expression>();
                        for (++position; position < expressionArrayList.Count; position++)
                        {
                            expression = expressionArrayList[(position)];
                            if (expression.ExpressionType == ExtrameFunctionCalculator.Types.ExpressionType.Symbol)
                            {
                                if (((ExtrameFunctionCalculator.Types.Symbol)expression).RawText==("("))
                                {
                                    stack.Push(position);
                                }
                                else if (((ExtrameFunctionCalculator.Types.Symbol)expression).RawText==(")"))
                                {
                                    stack.Pop();
                                    if (stack.Count==0)
                                    {
                                        result.Add(new ExpressionDigit(bracketList, calculator));
                                        bracketList = new List<Expression>();
                                        break;
                                    }
                                }
                                else
                                    bracketList.Add(new Operator((ExtrameFunctionCalculator.Types.Symbol)expression));
                            }
                            else
                                bracketList.Add(new Digit((ExtrameFunctionCalculator.Types.Digit)expression));
                        }
                    }
                    else
                    {
                        result.Add(new Operator((ExtrameFunctionCalculator.Types.Symbol)expression));
                    }
                }
            }
            return result;
        }

        private List<ExtrameFunctionCalculator.Types.Expression> ConverToCalExpression(List<Expression> expressions)
        {
            List<ExtrameFunctionCalculator.Types.Expression> resultArrayList = new List<ExtrameFunctionCalculator.Types.Expression>();
            Expression expression = null;
            for (int i = 0; i < expressions.Count; i++)
            {
                expression = expressions[(i)];
                resultArrayList.Add(expression.GetExpressionReference());
            }
            return resultArrayList;
        }

        private List<Expression> Level1_Optimize(List<Expression> expressionArrayList, Calculator calculator)
        {
            Expression expression = null;
            Digit digit = null;
            for (int position = 0; position < expressionArrayList.Count; position++)
            {
                expression = expressionArrayList[position];
                if (expression.OptExpressionType != Expression.ExpressionType.Digit)
                    continue;
                digit = (Digit)expression;
                if (digit.GetDigitReference().GetDouble() == 0)
                {
                    /*
                    * 4+0+6 -> 4+6
                    * 4*0*9/a/c/d*6446  -> 0
                    * */
                    if (position != expressionArrayList.Count - 1)/*判断是否在末尾，如果是就跳过后面的删除*/
                    {
                        if (((Operator)expressionArrayList[(position + 1)]).isSameLevelLayout("*"))
                        {
                            while ((position + 1) < expressionArrayList.Count ? ((Operator)expressionArrayList[(position + 1)]).isSameLevelLayout("*") : false)
                            {
                                expressionArrayList.RemoveAt(position + 1);
                                expressionArrayList.RemoveAt(position + 1);
                            }
                        }
                        else
                        {
                            if (((Operator)expressionArrayList[(position + 1)]).GetOperatorReference().RawText==("+") && (position != 0 ? ((Operator)expressionArrayList[(position - 1)]).isSameLevelLayout((Operator)expressionArrayList[(position + 1)]) : true))
                            {
                                expressionArrayList.RemoveAt(position--);
                                expressionArrayList.RemoveAt(position--);
                                position -= 1;
                            }
                        }
                    }
                    if (position != 0 ? expressionArrayList[(position - 1)].OptExpressionType == Expression.ExpressionType.Operator : false)/*判断是否在首位，如果是就跳前面的删除*/
                    {
                        if (((Operator)expressionArrayList[(position - 1)]).isSameLevelLayout("*"))
                        {
                            while ((position - 1) >= 0 ? ((Operator)expressionArrayList[(position - 1)]).isSameLevelLayout("*") : false)
                            {
                                expressionArrayList.RemoveAt(--position);
                                expressionArrayList.RemoveAt(--position);
                            }
                        }
                        else if (((Operator)expressionArrayList[(position - 1)]).isSameLevelLayout("+"))
                        {
                            expressionArrayList.RemoveAt(position--);
                            expressionArrayList.RemoveAt(position);
                        }
                    }
                    continue;
                }
                if (digit.GetDigitReference().GetDouble() == 1)
                {
                    /*
                    * 1*a*b*1*c-5 -> a*b*c-5
                    * a/1*6*1*7 -> a*6*7
                    * */
                    if (position < expressionArrayList.Count - 1)
                    {
                        expression = expressionArrayList[(position + 1)];
                        if (expression.OptExpressionType == Expression.ExpressionType.Operator ? ((Operator)expression).GetOperatorReference().RawText==("*") : false)
                        {
                            expressionArrayList.RemoveAt(position);
                            expressionArrayList.RemoveAt(position);
                            position--;
                            continue;
                        }
                    }
                    if (position > 0)
                    {
                        expression = expressionArrayList[(position - 1)];
                        if (expression.OptExpressionType == Expression.ExpressionType.Operator ? ((Operator)expression).GetOperatorReference().RawText==("*") : false)
                        {
                            expressionArrayList.RemoveAt(position);
                            expressionArrayList.RemoveAt(--position);
                        }
                        else if (expression.OptExpressionType == Expression.ExpressionType.Operator ? ((Operator)expression).GetOperatorReference().RawText==("/") : false)
                        {
                            expressionArrayList.RemoveAt(position);
                            expressionArrayList.RemoveAt(--position);
                        }
                    }
                }
            }
            return expressionArrayList;
        }

        /*
        private List<Expression> Level2_Optimize(List<Expression> expressionArrayList, Calculator calculator)
        {
            Expression expression = null;
            Digit digit = null;
            Operator op = null;
            for (int position = 0; position < expressionArrayList.Count; position++)
            {
                expression = expressionArrayList.get(position);
                if (expression.OptExpressionType != Expression.ExpressionType.Operator)
                    continue;
                op = (Operator)expression;
                if (!(op.GetOperatorReference().RawText == ("/")))
                    continue;
                expressionArrayList.RemoveAt(position);
                expressionArrayList.Add(new Operator(new ExtrameFunctionCalculator.Types.Symbol("*")));
                var td = expressionArrayList[position];
                expressionArrayList.RemoveAt(position);
                expressionArrayList.Add(new Fraction(((Digit)td).GetDigitReference().GetDouble()));
            }

            Fraction a, b, f;

            if (expressionArrayList.Count >= 3)
            {
                while (expressionArrayList[(expressionArrayList.Count - 3)].OptExpressionType == Expression.ExpressionType.Fraction && expressionArrayList.get(expressionArrayList.size() - 1).GetType() == Expression.ExpressionType.Fraction && (expressionArrayList[(expressionArrayList.Count - 2)].GetType() == Expression.ExpressionType.Operator ? ((Operator)expressionArrayList.get(expressionArrayList.size() - 2)).GetOperatorReference().rawText.equals("*") : false))
                {
                    a = (Fraction)expressionArrayList[(expressionArrayList.Count - 1)];
                    expressionArrayList.RemoveAt(expressionArrayList.Count - 1);
                    expressionArrayList.RemoveAt(expressionArrayList.Count - 1);
                    b = (Fraction)expressionArrayList[expressionArrayList.Count - 1];
                    expressionArrayList.RemoveAt(expressionArrayList.Count - 1);
                    expressionArrayList.Add(a.Multi(b));
                }
            }

            f = (Fraction)expressionArrayList[(expressionArrayList.Count - 1)];
            re
            expressionArrayList.RemoveAt(expressionArrayList.size() - 1);
            expressionArrayList.add(new Operator(new Calculator.Symbol("*")));
            return expressionArrayList;
        }
        */
    }
}
