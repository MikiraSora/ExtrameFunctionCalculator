import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MikiraSora on 2016/9/11.
 */
public class DerivativeParser {
    public class Expression extends Calculator.Expression{}

    public class RawSymbol extends Expression{}

    public class RawExpression extends Expression{}

    //public class DerivativeSymbol extends RawExpression{}

    public class DerivativeExpression extends RawExpression{}

    public class ResultSymbol extends Expression{}

    public class ResultExpression extends Expression{}

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
            Calculator.Function function =GetCalculator().GetFunction(function_name);
            function.current_paramters = function_paramters;
            return function;
            //Get function paramater list
        }

        if (Calculator.isDigit(expression)) {
            return new Calculator.Digit(expression);
        }

        if (Calculator.isValidVariable(expression)) {
            return expression.equals(derivativeVariableName)?new DerivativeExpression()/*todo*/:GetCalculator().GetVariable(expression);
        }

        return null;
    }

    public String Solve(String expression,String derivativeName)throws Exception{
        derivativeVariableName=derivativeName;
        ArrayList<Calculator.Expression> expression_list=ParseExpression(expression);

        return null;//// TODO: 2016/9/11
    }


}
