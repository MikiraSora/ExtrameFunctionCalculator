import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by MikiraSora on 2016/10/3.
 */
public class CalculatorOptimizer {
    //Analysis Element Define
    private static class Expression{
        enum ExpressionType{
            Operator,
            Digit,
            Fraction,
            Unknown
        }
        Calculator.Expression Cal_ExpressionReference=null;

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

        @Override
        public String toString() {
            return GetOperatorReference().rawText;
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

        @Override
        public String toString() {
            return GetDigitReference().rawText;
        }
    }

    private class ExpressionDigit extends Digit{
        Calculator calculator=null;
        Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}
        Calculator.Digit DigitResult=null;

        ArrayList<Expression> expressionArrayList=null;

        private ExpressionDigit(){}
        ExpressionDigit(ArrayList<Expression> expressions,Calculator calculator){
            this.calculator=calculator;
            expressionArrayList=expressions;
            try {
                DigitResult=new Calculator.Digit(GetCalculator().Solve(toString()));
            }catch (Exception e){
                DigitResult=new Calculator.Digit("0");
            }
            expressionArrayList=null;

        }

        @Override
        Calculator.Digit GetDigitReference() {
            return DigitResult;
        }

        @Override
        Calculator.Expression GetExpressionReference() {
            return this.GetDigitReference();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder=new StringBuilder();
            for(Expression expression:expressionArrayList){
                stringBuilder.append(expression.toString());
            }
            return stringBuilder.toString();
        }
    }

    private class Fraction extends Expression{
        private double Numerator=1;
        private double Denominator;

        private Fraction(){}
        private Fraction(double numerator,double denominator){Numerator=numerator;Denominator=denominator;}
        private Fraction(double denominator){Denominator=denominator;}

        Fraction Multi(Fraction fraction){return new Fraction(this.Numerator*fraction.Numerator,this.Denominator*fraction.Denominator);}
    }


    public CalculatorOptimizer(Calculator calculator){this.calculator=calculator;}

    private boolean enableOptimize=false;
    private int OptimizeLevel=1;
    private Calculator calculator=null;

    public void SetOptimiizeLevel(int i){OptimizeLevel=i;}
    public void Enable(){enableOptimize=true;}
    public void DisEnable(){enableOptimize=false;}
    private  Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}


    public ArrayList<Calculator.Expression> OptimizeExpression(ArrayList<Calculator.Expression> expressionArrayList){
        if(!enableOptimize)
            return null;
        ArrayList<Expression> tmpAnalyseExpressionArrayList=AnalyseConver(expressionArrayList);
        expressionArrayList=null;

        if(OptimizeLevel>=1)
            tmpAnalyseExpressionArrayList=Level1_Optimize(tmpAnalyseExpressionArrayList,GetCalculator());

        if(OptimizeLevel>=2)
            tmpAnalyseExpressionArrayList=Level2_Optimize(tmpAnalyseExpressionArrayList,GetCalculator());

        Log.Debug(String.format("Optimize finished: %s",tmpAnalyseExpressionArrayList.toString()));
        expressionArrayList=ConverToCalExpression(tmpAnalyseExpressionArrayList);
        return expressionArrayList;
    }

    private ArrayList<Expression> AnalyseConver(ArrayList<Calculator.Expression> expressionArrayList){
        ArrayList<Expression> result=new ArrayList<>(),bracketList=null;
        Stack<Integer> stack=new Stack();
        Calculator.Expression expression=null;
        Calculator.Symbol symbol=null;
        for(int position=0;position<expressionArrayList.size();position++){
            expression=expressionArrayList.get(position);
            if(expression.GetType()== Calculator.Expression.ExpressionType.Digit){
                result.add(new Digit((Calculator.Digit)expression));
                continue;
            }
            if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol){
                symbol=(Calculator.Symbol)expression;
                if(symbol.rawText.equals("(")){
                    stack.clear();
                    stack.push(position);
                    bracketList=new ArrayList<>();
                    for(++position;position<expressionArrayList.size();position++){
                        expression=expressionArrayList.get(position);
                        if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol){
                            if (((Calculator.Symbol) expression).rawText.equals("(")){
                                stack.push(position);
                            } else if (((Calculator.Symbol) expression).rawText.equals(")")) {
                                stack.pop();
                                if (stack.isEmpty()) {
                                    result.add(new ExpressionDigit(bracketList, GetCalculator()));
                                    bracketList = new ArrayList<>();
                                    break;
                                }
                            }else
                                bracketList.add(new Operator((Calculator.Symbol) expression));
                        }else
                            bracketList.add(new Digit((Calculator.Digit) expression));
                    }
                }else {
                    result.add(new Operator((Calculator.Symbol)expression));
                }
            }
        }
        return result;
    }

    private ArrayList<Calculator.Expression> ConverToCalExpression(ArrayList<Expression> expressions){
        ArrayList<Calculator.Expression> resultArrayList=new ArrayList<>();
        Expression expression=null;
        for(int i=0;i<expressions.size();i++){
            expression=expressions.get(i);
            resultArrayList.add(expression.GetExpressionReference());
        }
        return resultArrayList;
    }

    //lv.1
    private ArrayList<Expression> Level1_Optimize(ArrayList<Expression> expressionArrayList,Calculator calculator){
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
                        while ((position+1)<expressionArrayList.size()?((Operator) expressionArrayList.get(position + 1)).isSameLevelLayout("*"):false) {
                            expressionArrayList.remove(position + 1);
                            expressionArrayList.remove(position + 1);
                        }
                    }else{
                        if(((Operator)expressionArrayList.get(position + 1)).GetOperatorReference().rawText.equals("+")&&(position!=0?((Operator)expressionArrayList.get(position-1)).isSameLevelLayout((Operator)expressionArrayList.get(position+1)):true)) {
                            expressionArrayList.remove(position--);
                            expressionArrayList.remove(position--);
                            position -= 1;
                        }
                    }
                }
                if(position!=0?expressionArrayList.get(position-1).GetType()== Expression.ExpressionType.Operator:false)/*判断是否在首位，如果是就跳前面的删除*/{
                    if(((Operator)expressionArrayList.get(position - 1)).isSameLevelLayout("*")) {
                        while ((position-1)>=0?((Operator) expressionArrayList.get(position - 1)).isSameLevelLayout("*"):false) {
                            expressionArrayList.remove(--position);
                            expressionArrayList.remove(--position);
                        }
                    }else if(((Operator)expressionArrayList.get(position - 1)).isSameLevelLayout("+")){
                        expressionArrayList.remove(position--);
                        expressionArrayList.remove(position);
                    }
                }
                continue;
            }
            if (digit.GetDigitReference().GetDouble()==1){
                /*
                * 1*a*b*1*c-5 -> a*b*c-5
                * a/1*6*1*7 -> a*6*7
                * */
                if (position<expressionArrayList.size()-1) {
                    expression = expressionArrayList.get(position + 1);
                    if (expression.GetType() == Expression.ExpressionType.Operator ? ((Operator) expression).GetOperatorReference().rawText.equals("*") : false) {
                        expressionArrayList.remove(position);
                        expressionArrayList.remove(position);
                        position--;
                        continue;
                    }
                }
                if(position>0) {
                    expression = expressionArrayList.get(position - 1);
                    if (expression.GetType() == Expression.ExpressionType.Operator ? ((Operator) expression).GetOperatorReference().rawText.equals("*") : false) {
                        expressionArrayList.remove(position);
                        expressionArrayList.remove(--position);
                    } else if (expression.GetType() == Expression.ExpressionType.Operator ? ((Operator) expression).GetOperatorReference().rawText.equals("/") : false) {
                        expressionArrayList.remove(position);
                        expressionArrayList.remove(--position);
                    }
                }
            }
        }
        return expressionArrayList;
    }

    private ArrayList<Expression> Level2_Optimize(ArrayList<Expression> expressionArrayList,Calculator calculator){
        Expression expression=null;
        Digit digit=null;
        Operator operator=null;
        for (int position=0;position<expressionArrayList.size();position++){
            expression=expressionArrayList.get(position);
            if(expression.GetType()!= Expression.ExpressionType.Operator)
                continue;
            operator=(Operator) expression;
            if(!operator.GetOperatorReference().rawText.equals("/"))
                continue;
            expressionArrayList.remove(position);
            expressionArrayList.add(new Operator(new Calculator.Symbol("*")));
            expressionArrayList.add(new Fraction(((Digit)expressionArrayList.remove(position)).GetDigitReference().GetDouble()));
        }

        Fraction a,b,f;

        if(expressionArrayList.size()>=3){
            while(expressionArrayList.get(expressionArrayList.size()-3).GetType()==Expression.ExpressionType.Fraction&&expressionArrayList.get(expressionArrayList.size()-1).GetType()==Expression.ExpressionType.Fraction&&(expressionArrayList.get(expressionArrayList.size()-2).GetType()== Expression.ExpressionType.Operator?((Operator)expressionArrayList.get(expressionArrayList.size()-2)).GetOperatorReference().rawText.equals("*"):false)){
                a=(Fraction) expressionArrayList.remove(expressionArrayList.size()-1);
                expressionArrayList.remove(expressionArrayList.size()-1);
                b=(Fraction) expressionArrayList.remove(expressionArrayList.size()-1);
                expressionArrayList.add(a.Multi(b));
            }
        }

        f=(Fraction) expressionArrayList.remove(expressionArrayList.size()-1);
        expressionArrayList.remove(expressionArrayList.size()-1);
        expressionArrayList.add(new Operator(new Calculator.Symbol("*")));
        return expressionArrayList;
    }
}