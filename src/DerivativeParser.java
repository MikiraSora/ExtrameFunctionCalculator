import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MikiraSora on 2016/9/11.
 */
public class DerivativeParser {
    public abstract class Expression extends Calculator.Expression{
        DerivativeParser derivativeParser=null;

        String DerivativeSolve()throws Exception{return null;}

        public DerivativeParser getDerivativeParser() {
            return derivativeParser==null?derivativeParser=new DerivativeParser(DerivativeParser.this.GetCalculator()):derivativeParser;
        }
    }

    public class RawSymbol extends Expression{
        Calculator.Symbol rawSymbol=null;
        public RawSymbol(Calculator.Symbol symbol){rawSymbol=symbol;}
    }

    public class RawExpression extends Expression{
        ArrayList<Calculator.Expression> expressionArrayList=null;
        public void setExpressionArrayList(ArrayList<Calculator.Expression> list){expressionArrayList=list;}
        public ArrayList<Calculator.Expression> getExpressionArrayList() {
            return expressionArrayList;
        }
    }

    public class DerivativeSymbol extends RawSymbol{
        public DerivativeSymbol(Calculator.Symbol symbol){super(symbol);}
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

    }

    public class ResultSymbol extends RawSymbol{
        public ResultSymbol(Calculator.Symbol symbol){super(symbol);}
    }

    public class ResultFunction extends Expression{
        String resultExpression=null;
        public ResultFunction(String result){resultExpression=result;}
    }

    public class DerivativeVariable extends Expression{
        ArrayList<Calculator.Expression> variable_list=null;
        public DerivativeVariable(){}
        public DerivativeVariable(ArrayList<Calculator.Expression> list){variable_list=list;}
    }

    public class ResultExpression extends RawExpression{}

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
            return expression.equals(derivativeVariableName)?new DerivativeVariable()/*todo*/:GetCalculator().GetVariable(expression);
        }

        return null;
    }

    ArrayList<Calculator.Expression> ProcessExpression(ArrayList<Calculator.Expression> expressionArrayList){
        //
        Calculator.Expression expression=null;
        ArrayList<Calculator.Expression> expressionCollection;
        for(int pos=0;pos<expressionArrayList.size();pos++){
            expression=expressionArrayList.get(pos);
            if()
        }

        return null;
        //todo
        }

    public String Solve(String expression,String derivativeName)throws Exception{
        derivativeVariableName=derivativeName;
        ArrayList<Calculator.Expression> expression_list=ParseExpression(expression);

        return null;//// TODO: 2016/9/11
    }


}
