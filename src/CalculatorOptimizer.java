import java.util.ArrayList;

/**
 * Created by MikiraSora on 2016/10/3.
 */
public class CalculatorOptimizer {
    //Analysis Element Define
    private static class Expression{
        enum ExpressionType{
            Operator,
            Digit,
            Unknown
        }
        private Calculator.Expression Cal_ExpressionReference=null;

        ExpressionType GetType(){return ExpressionType.Unknown;}

        private Expression(){}
        Expression(Calculator.Expression cal_ExpressionReference){Cal_ExpressionReference=cal_ExpressionReference;}

        Calculator.Expression GetExpressionReference(){return Cal_ExpressionReference;}
    }

    private class Operator extends Expression{
        private Operator(){}
        Operator(Calculator.Symbol symbol){super(symbol);}

        @Override
        ExpressionType GetType() {
            return ExpressionType.Operator;
        }

        Calculator.Symbol GetOperatorReference(){return (Calculator.Symbol)GetExpressionReference();}

        private float GetOperatorPriorty(){return Calculator.Symbol.OperatorPrioty.containsKey(GetOperatorReference().rawText)?Calculator.Symbol.OperatorPrioty.get(GetOperatorReference().rawText):-1;}

        boolean isSameLevelLayout(Operator operator){return this.GetOperatorPriorty()==operator.GetOperatorPriorty();}

        boolean isSameLevelLayout(String operatorChar){return this.GetOperatorPriorty()==(Calculator.Symbol.OperatorPrioty.containsKey(operatorChar)?Calculator.Symbol.OperatorPrioty.get(operatorChar):0);}

        boolean isBaseOperator(){
            switch (GetOperatorReference().rawText){
                case "+":
                case "-":
                case "*":
                case "/":
                    return true;
            }
            return false;
        }
    }

    private class Digit extends Expression{
        private Digit(){}
        Digit(Calculator.Digit digit){super(digit);}

        @Override
        ExpressionType GetType() {
            return ExpressionType.Digit;
        }

        Calculator.Digit GetDigitReference(){return (Calculator.Digit)GetExpressionReference();}
    }

    private class Fraction extends Expression{
        private double Numerator=1;
        private double Denominator;

        private Fraction(){}
        private Fraction(double numerator,double denominator){Numerator=numerator;Denominator=denominator;}
        private Fraction(double denominator){Denominator=denominator;}

        Fraction Multi(Fraction fraction){return new Fraction(this.Numerator*fraction.Numerator,this.Denominator*fraction.Denominator);}
    }


    private boolean enableOptimize=false;
    private int OptimizeLevel=1;
    private Calculator calculator=null;

    public void SetOptimiizeLevel(int i){OptimizeLevel=i;}
    public void Enable(){enableOptimize=true;}
    public void DisEnable(){enableOptimize=false;}
    private  Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}


    public ArrayList<Calculator.Expression> OptimizeExpression(ArrayList<Calculator.Expression> expressionArrayList,Calculator calculator){
        this.calculator=calculator;
        if(!enableOptimize)
            return expressionArrayList;
        ArrayList<Expression> tmpAnalyseExpressionArrayList=AnalyseConver(expressionArrayList);
        expressionArrayList=null;

        if(OptimizeLevel>=1)
            tmpAnalyseExpressionArrayList=Level1Optimize(tmpAnalyseExpressionArrayList,GetCalculator());

        expressionArrayList=ConverToCalExpression(tmpAnalyseExpressionArrayList);
        return expressionArrayList;
    }

    private ArrayList<Expression> AnalyseConver(ArrayList<Calculator.Expression> expressionArrayList){
        ArrayList<Expression> result=new ArrayList<>();
        for(Calculator.Expression expression:expressionArrayList){
            if(expression.GetType()== Calculator.Expression.ExpressionType.Digit){
                result.add(new Digit((Calculator.Digit)expression));
                continue;
            }
            if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol){
                result.add(new Operator((Calculator.Symbol)expression));
            }
        }
        return result;
    }

    private ArrayList<Calculator.Expression> ConverToCalExpression(ArrayList<Expression> expressions){
        ArrayList<Calculator.Expression> resultArrayList=new ArrayList<>();
        /*
        for(Expression expression:expressions)
            resultArrayList.add(expression.GetExpressionReference());*/
        Expression expression=null;
        for(int i=0;i<expressions.size();i++){
            expression=expressions.get(i);
            resultArrayList.add(expression.GetExpressionReference());
        }
        return resultArrayList;
    }

    //lv.1
    private ArrayList<Expression> Level1Optimize(ArrayList<Expression> expressionArrayList,Calculator calculator){
        Expression expression=null;
        Digit digit=null;
        for(int position=0;position<expressionArrayList.size();position++){
            expression=expressionArrayList.get(position);
            if(expression.GetType()!= Expression.ExpressionType.Digit)
                continue;
            digit=(Digit)expression;
            if(digit.GetDigitReference().GetDouble()==0){
                /*
                * 4+0+6 -> 4+6
                * 4*0*9/a/c/d*6446  -> 0
                * */
                if(position!=expressionArrayList.size()-1)/*判断是否在末尾，如果是就跳过后面的删除*/ {
                    if(((Operator) expressionArrayList.get(position + 1)).isSameLevelLayout("*")) {
                        while (((Operator) expressionArrayList.get(position + 1)).isSameLevelLayout("*")) {
                            expressionArrayList.remove(position + 1);
                            expressionArrayList.remove(position + 1);
                        }
                    }else{
                        expressionArrayList.remove(position);
                        expressionArrayList.remove(position);
                    }
                }

                if(position!=0)/*判断是否在首位，如果是就跳前面的删除*/{
                    while (((Operator) expressionArrayList.get(position - 1)).isSameLevelLayout("*")) {
                        expressionArrayList.remove(position - 1);
                        expressionArrayList.remove(position - 1);
                    }
                }
                continue;
            }
            if (digit.GetDigitReference().GetDouble()==1){
                /*
                * 1*a*b*1*c-5 -> a*b*c-5
                * a/1*6*1*7 -> a*6*7
                * */
            }
        }
        return expressionArrayList;
    }
}
