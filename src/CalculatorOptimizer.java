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
        if(OptimizeLevel>=1)
            expressionArrayList=Level1Optimize(expressionArrayList,GetCalculator());
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
    }

    //lv.1
    private ArrayList<Calculator.Expression> Level1Optimize(ArrayList<Calculator.Expression> expressionArrayList,Calculator calculator){

        return expressionArrayList;
    }
}
