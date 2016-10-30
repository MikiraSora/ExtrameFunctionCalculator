import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MikiraSora on 2016/9/11.
 */
public class DerivativeParser {
    public abstract static class Expression extends Calculator.Expression{
        enum DerivativeType{
            RawSymbol,
            RawExpression,
            RawVariable,
            DerivativeSymbol,
            DerivativeFunction,
            DerivativeExpression,
            ResultSymbol,
            ResultFunction,
            DerivativeVariable,
            ResultExpression,
            Unknown
        }

        DerivativeType GetDerivateType(){return null;}

        DerivativeParser derivativeParser=null;

        String DerivativeSolve()throws Exception{return null;}

        @Override
        ExpressionType GetType() {
            return ExpressionType.Derivative;
        }

        public DerivativeParser getDerivativeParser() {
            return derivativeParser==null?derivativeParser=new DerivativeParser(null):derivativeParser;
        }
    }

    public class RawSymbol extends Expression{
        Calculator.Symbol rawSymbol=null;
        public RawSymbol(Calculator.Symbol symbol){rawSymbol=symbol;}

        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.RawSymbol;
        }
    }

    public class RawExpression extends Expression{
        ArrayList<Calculator.Expression> expressionArrayList=null;
        public void setExpressionArrayList(ArrayList<Calculator.Expression> list){expressionArrayList=list;}
        public ArrayList<Calculator.Expression> getExpressionArrayList() {
            return expressionArrayList;
        }
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.RawExpression;
        }
    }

    public class DerivativeSymbol extends RawSymbol{
        public DerivativeSymbol(Calculator.Symbol symbol){super(symbol);}
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.DerivativeSymbol;
        }
    }

    DerivativeParser(Calculator calculator1){calculator=calculator1;}

    public class DerivativeFunction extends Expression{
        String function_name=null;
        String derivative_name=null;
        public DerivativeFunction(String fname,String dname,DerivativeParser parser){
            function_name=fname;
            derivative_name=dname;
            derivativeParser=parser;
        }

        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.DerivativeFunction;
        }

        @Override
        String DerivativeSolve()throws Exception{
            Calculator.Function function=(GetCalculator().GetFunction(function_name));
            if(function.getFunction_type()!= Calculator.Function.FunctionType.Reflection_Function){
                return getDerivativeParser().Solve(function.function_body,derivative_name);
            }else{
                Calculator.ReflectionFunction reflectionFunction=(Calculator.ReflectionFunction)function;
                String result=reflectionFunction.reflectionFunction.onDerivativeParse();
                if(result!=null){
                    throw new Exception(String.format("%s(%s) is reflection function and not support derivative.",function_name,derivative_name));
                }
                return (result);
            }
        }
    }

    public class DerivativeExpression extends RawExpression{
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.DerivativeExpression;
        }
        DerivativeExpression(DerivativeParser parser){this.derivativeParser=parser;}
    }

    public class ResultSymbol extends RawSymbol{
        public ResultSymbol(Calculator.Symbol symbol){super(symbol);}
    }

    public class ResultFunction extends Expression{
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.ResultFunction;
        }
        String resultExpression=null;
        public ResultFunction(String result){resultExpression=result;}
    }

    public class RawVariable extends Expression{
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.RawVariable;
        }
        ArrayList<Calculator.Expression> variable_list=null;

        Calculator.Expression powerExtraExpressionList=null;

        public void SetPowerExtraExpressionList(Calculator.Expression powerExtraExpressionList) {
            this.powerExtraExpressionList = powerExtraExpressionList;
        }

        public RawVariable(){}
        public RawVariable(ArrayList<Calculator.Expression> list){variable_list=list;}

        @Override
        String DerivativeSolve() throws Exception {
            return null;
        }

        ArrayList<Calculator.Expression> Cover()throws Exception{
            ArrayList<Calculator.Expression> resultExpressionList=new ArrayList<>();
            if(powerExtraExpressionList==null)
            {
                resultExpressionList.add(new ResultVariable(powerExtraExpressionList));
            }
            else{
                resultExpressionList.add(new Calculator.Digit(powerExtraExpressionList.Solve()));
                resultExpressionList.add(new ResultVariable(powerExtraExpressionList));
            }
            return resultExpressionList;
        }
    }

    public class DerivativeVariable extends RawVariable{
        DerivativeVariable(ArrayList<Calculator.Expression> list){super(list);}
        DerivativeVariable(){}

        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.DerivativeVariable;
        }
    }

    public class ResultVariable extends DerivativeVariable{
        Calculator.Expression extraExpression=null;
        public ResultVariable(Calculator.Expression _extraExpression){extraExpression=_extraExpression;}

        @Override
        String Solve()throws Exception{
            return derivativeVariableName+extraExpression==null?"":String.format("((%s)-1)",extraExpression.Solve());
        }
    }

    public class ResultExpression extends RawExpression{
        @Override
        DerivativeType GetDerivateType() {
            return DerivativeType.ResultExpression;
        }
    }

    private Calculator calculator=null;
    Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    static String derivativeVariableName="x";
    private static String specialOperationChar = " + - * / ~ ! @ # $ % ^ & ( ) ; : \" | ? > < , ` ' \\ ";


    private ArrayList<Calculator.Expression> ParseExpression(String expression)throws Exception{
        ArrayList<Calculator.Expression> expressionArrayList = new ArrayList<>();
        int position = 0;
        char c,tmp_c;
        Calculator.Expression expr = null;
        String statement = new String(),tmp_op;
        Stack<Integer> bracket_stack = new Stack<>();
        while (true) {
            if (position >= expression.length())
                break;
            c = expression.charAt(position);
            if (specialOperationChar.contains(" "+String.valueOf(c)+" ")) {
                if ((!statement.isEmpty()) && (c == '(')) {
                    //Function Parser
                    bracket_stack.clear();
                    while (true) {
                        if (position >= expression.length())
                            break;
                        c = expression.charAt(position);
                        if (c == '(') {
                            bracket_stack.push(position);
                        }
                        if (c == ')') {
                            if (bracket_stack.isEmpty())
                                throw new Exception("Extra brackets in position: " + position);
                            bracket_stack.pop();
                            if (bracket_stack.isEmpty()) {
                                statement += ")";
                                expressionArrayList.add(checkConverExpression(statement));//should always return Function
                                break;
                            }

                        }
                        statement += c;
                        position++;
                    }
                } else {
                    expr = checkConverExpression(statement);
                    if (expr != null)
                        expressionArrayList.add(expr);
                    tmp_op=Character.toString(c);
                    {
                        if(position<(expression.length()-1)){
                            tmp_c=expression.charAt(position+1);
                            tmp_op+=tmp_c;
                            if(!specialOperationChar.contains(" "+(tmp_op)+" ")){
                                tmp_op=Character.toString(c);
                            }
                        }
                    }
                    expressionArrayList.add(new Calculator.Symbol(tmp_op));
                }
                //Reflush statement
                statement = new String();
            } else {
                statement += c;
            }
            position++;
        }
        if (!statement.isEmpty())
            expressionArrayList.add(checkConverExpression(statement));
        return expressionArrayList;
    }

    private Calculator.Expression checkConverExpression(String expression) throws Exception {
        if (Calculator.isFunction(expression)) {
            //Get function name
            Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = reg.matcher(expression);
            result.find();
            if (result.groupCount() != 2)
                throw new Exception("Cannot parse function ：" + expression);
            String function_name = result.group(1);
            String function_paramters = result.group(2);
            if (!GetCalculator().ContainFunction(function_name))
                throw new Exception(String.format("function %s hadnt declared!", function_name));

            if(function_paramters.equals(derivativeVariableName)){
                //导函数
                Expression function=null;
                function=new DerivativeFunction(function_name,derivativeVariableName,this);
                return function;
            }else{
                Calculator.Function function =GetCalculator().GetFunction(function_name);
                function.current_paramters = function_paramters;
                return function;
            }
            //Get function paramater list
        }

        if (Calculator.isDigit(expression)) {
            return new Calculator.Digit(expression);
        }

        if (Calculator.isValidVariable(expression)) {
            return expression.equals(derivativeVariableName)?new RawVariable()/*todo*/:GetCalculator().GetVariable(expression);
        }

        return null;
    }

    ArrayList<Calculator.Expression> ProcessExpression(ArrayList<Calculator.Expression> expressionArrayList)throws Exception{
        //
        Calculator.Expression expression=null;
        ArrayList<Calculator.Expression> expressionCollection;
        Stack<Integer> bracketStack=new Stack<>();
        int start=0,end=0;
        for(int pos=0;pos<expressionArrayList.size();pos++){
            expression=expressionArrayList.get(pos);
            if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol)
                if(((Calculator.Symbol)expression).rawText.equals("(")){
                    start=pos;
                    expressionCollection=new ArrayList<>();
                    for(++pos;pos<expressionArrayList.size();pos++){
                        expression=expressionArrayList.get(pos);
                        if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol) {
                            if (((Calculator.Symbol) expression).rawText.equals("(")) {
                                bracketStack.add(pos);
                            }else if (((Calculator.Symbol) expression).rawText.equals(")")){
                                if(bracketStack.empty()) {
                                    end = pos;
                                    for (int mpos = start; mpos <= end; mpos++) {
                                        expressionArrayList.remove(start);
                                    }
                                    DerivativeExpression derivativeExpression = new DerivativeExpression(this);
                                    derivativeExpression.setExpressionArrayList(expressionCollection);
                                    expressionArrayList.add(start, derivativeExpression);
                                    expressionCollection = new ArrayList<>();
                                    pos = start;
                                    break;
                                }else{
                                    bracketStack.pop();
                                }
                            }

                        }
                        expressionCollection.add(expression);
                    }
                }
        }

        //Calculator.Expression forwardExpression,nextExpression;
        for(int pos=0;pos<expressionArrayList.size();pos++){
            expression=expressionArrayList.get(pos);
            if(expression.GetType()== Calculator.Expression.ExpressionType.Symbol){
                if(((Calculator.Symbol)expression).rawText.equals("^")){
                    Calculator.Expression previous=expressionArrayList.get(pos-1),next=expressionArrayList.get(pos+1);
                    if(previous.GetType()==Calculator.Expression.ExpressionType.Derivative){
                        if(((Expression)previous).GetDerivateType()== Expression.DerivativeType.RawVariable&&((next).GetType()!= Calculator.Expression.ExpressionType.Derivative)){
                            //D^(!D)
                            ((RawVariable)previous).SetPowerExtraExpressionList(next);
                            expressionArrayList.remove(expression);
                            expressionArrayList.remove(next);
                        }else{
                            throw new Exception("Cannot parse the Expression :"+next.rawText+",because it needs higher feature.");
                        }
                    }
                }
            }
        }

        return expressionArrayList;
        //todo
        }



    public String Solve(String expression,String derivativeName)throws Exception{
        String expression_=expression;
        derivativeVariableName=derivativeName;
        ArrayList<Calculator.Expression> expression_list=ParseExpression(expression);
        expression_list=ProcessExpression(expression_list);
        return null;//// TODO: 2016/9/11
    }


}
