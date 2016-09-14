import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MikiraSora on 2016/9/11.
 */
public class DerivativeParser {
    public class DerivativeVariable extends Calculator.Expression{

        ArrayList<Calculator.Expression> rawExpressionList=null;
        ArrayList<Calculator.Expression> derivativeExpressionList=null;
        DerivativeParser derivativeParser=null;
        DerivativeParser GetDerivativeParser(){return derivativeParser==null?derivativeParser=new DerivativeParser():derivativeParser;}

        private DerivativeVariable(){super(null);}
        public DerivativeVariable(ArrayList<Calculator.Expression> rawExprList,DerivativeParser parser){
            rawExpressionList=rawExprList;
            derivativeParser=parser;
        }

        private ArrayList<Calculator.Expression> Parse()throws Exception{
            String rawExpressionString=ListToString(rawExpressionList);
            ArrayList<Calculator.Expression> resultExpressionList=derivativeParser.ParseExpression(/*.GetCalculator().ParseExpression*/derivativeParser.Solve(rawExpressionString,derivativeParser.derivativeVariableName));
            return resultExpressionList;
        }

        private ArrayList<Calculator.Expression> GetDerivativeExpressionList()throws Exception{
            return derivativeExpressionList==null?derivativeExpressionList=Parse():derivativeExpressionList;
        }

        private String ListToString(ArrayList<Calculator.Expression> list){
            StringBuilder stringBuilder=new StringBuilder();
            for (Calculator.Expression expr:list) {
                stringBuilder.append(expr.Solve());
            }
            return stringBuilder.toString();
        }

        @Override
        String Solve(){
            String result=null;
            try {
                ArrayList<Calculator.Expression> expressionArrayList = GetDerivativeExpressionList();
                result = ListToString(expressionArrayList);
            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }

    private Calculator calculator=null;
    Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    String derivativeVariableName="x";

    //Todo
    private ArrayList<Calculator.Expression> ParseExpression(String expression)throws Exception{return calculator.ParseExpression(expression);}

    private ArrayList<Calculator.Expression> Parse(ArrayList<Calculator.Expression> expression_list)throws Exception{

        return null;
    }

    public String Solve(String expression,String derivativeName)throws Exception{
        derivativeVariableName=derivativeName;
        ArrayList<Calculator.Expression> expression_list=ParseExpression(expression);

        return null;//// TODO: 2016/9/11
    }


}
