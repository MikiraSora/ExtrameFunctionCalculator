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
        Operator(Calculator.Symbol symbol){super(symbol);}

        @Override
        ExpressionType GetType() {
            return ExpressionType.Operator;
        }

        Calculator.Symbol GetOperatorReference(){return (Calculator.Symbol)GetExpressionReference();}

        private float GetOperatorPriorty(){return Calculator.Symbol.OperatorPrioty.containsKey(GetOperatorReference().rawText)?Calculator.Symbol.OperatorPrioty.get(GetOperatorReference().rawText):-1;}

        boolean isSameLevelLayout(Operator operator){return }
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

    //
    private ArrayList<Calculator.Expression> Level1Optimize(ArrayList<Calculator.Expression> expressionArrayList,Calculator calculator){

        return expressionArrayList;
    }
}
