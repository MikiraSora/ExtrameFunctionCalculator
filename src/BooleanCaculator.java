
import javafx.beans.binding.BooleanExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mikir on 2016/8/28.
 */
public class BooleanCaculator {
    Calculator calculator=null;
    private Calculator getCalculator(){return calculator==null?calculator=new Calculator():calculator;}
    static String TRUE="true",FALSE="false";
    public static class BooleanVariable extends Calculator.ExpressionVariable{
        boolean boolean_value=false;
        BooleanVariable(String name,String expression,Calculator c)throws Exception{
            super(name,expression,c);
            setCalculator(c);
            boolean_value=expression==TRUE?true:expression==FALSE?false:(Double.valueOf(getCalculator().Solve(expression))==0);
            variable_type=VariableType.BooleanVariable;
        }

        BooleanVariable(boolean value,Calculator calculator1)throws Exception{
            this("",Boolean.toString(value),calculator1);
            boolean_value=value;
        }

        @Override
        Calculator.ExpressionVariable Copy() {
            try{
                return new BooleanVariable(boolean_value,calculator);
            }catch (Exception e){
                return null;
            }
        }

        @Override
        String Solve() {
            return boolean_value?"1":"0";
        }

        @Override
        String GetName() {
            return boolean_value?TRUE:FALSE;//boolean_value?"bool_"+TRUE:"bool_"+FALSE;
        }

        @Override
        String Serialize() {
            return String.format("b##%s##%s",Variable_name,rawText);
        }

    }



    Calculator.Function GetFunction(String name) throws Exception, Calculator.FunctionNotFoundException {
        return calculator.GetFunction(name);
    }

    Calculator.Variable GetVariable(String name) throws Calculator.VariableNotFoundException {
        return calculator.GetVariable(name);
    }
    private static String specialOperationChar = "+ ++ - -- * / ~ ! != @ # $ % ^ & && ( ) ; : \" \" || ? > >= < >= , ` ' = ==";

    private String RequestVariable(String name) throws Calculator.VariableNotFoundException,Exception{
        return calculator.Solve(name);
    }

    private ArrayList<Calculator.Expression> ParseExpression(String expression) throws Exception {
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
            if (specialOperationChar.contains(String.valueOf(c))) {
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
                            position++;
                            if(!specialOperationChar.contains((tmp_op))){
                                tmp_op=Character.toString(tmp_op.charAt(0));
                                position--;
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

    private void ConverFunctionToDigit() throws Calculator.FunctionNotFoundException, Calculator.VariableNotFoundException {
        int position = 0;
        Calculator.Expression node;
        Calculator.Function function;
        Calculator.Digit result;
        for (position = 0; position < rawExpressionChain.size(); position++) {
            node = rawExpressionChain.get(position);
            if (node.GetType() == Calculator.Expression.ExpressionType.Function) {
                function = (Calculator.Function) node;
                if (function.getFunction_type() == Calculator.Function.FunctionType.Reflection_Function)
                    function.setCalculator(calculator);
                result = function.GetSolveToDigit();
                rawExpressionChain.remove(position);
                rawExpressionChain.add(position, result);
            }
        }
    }

    private void ConverVariableToDigit() throws Exception, Calculator.VariableNotFoundException {
        int position = 0;
        Calculator.Expression node;
        Calculator.Variable variable;
        Calculator.Expression result;
        try {
            for (position = 0; position < rawExpressionChain.size(); position++) {
                node = rawExpressionChain.get(position);
                if (node.GetType() == Calculator.Expression.ExpressionType.Variable) {
                    variable = (Calculator.Variable) node;
                    if(variable.variable_type!= Calculator.Variable.VariableType.BooleanVariable)
                        result = variable.GetDigit();
                    else
                        result=variable;
                    rawExpressionChain.remove(position);
                    rawExpressionChain.add(position, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    private Calculator.Expression Execute(Calculator.Expression a, Calculator.Symbol op, Calculator.Expression b)throws Exception{
        if(isBooleanOperator((Calculator.Symbol) op)){
            return op.Solve() //return ExecuteBool(a,op,b);
        }
        if(!isSameType(a,b))
            throw new Exception(String.format("It cannot calculate between %s and %s",a.GetType().toString(),b.GetType().toString()));

        if(a.GetType()== Calculator.Expression.ExpressionType.Digit){
            return new Calculator.Digit(calculator.Solve(a.Solve()+op.toString()+b.Solve()));
        }
        if(a.GetType()==Calculator.Expression.ExpressionType.Variable){
            return Execute(((Calculator.Variable)a).GetDigit(),op,((Calculator.Variable)a).GetDigit());
        }
        return null;
    }
*/
    static String boolOperatorSymbol="< > == <= >= || && !=";
    private static boolean isBooleanOperator(Calculator.Symbol op){
        return boolOperatorSymbol.contains(op.Solve());
    }

    private static boolean isSameType(Calculator.Expression a, Calculator.Expression b){
        if(a.GetType()!=b.GetType())
            return false;
        return true;
    }

    private static boolean isCalculatable(Calculator.Expression expression){
        if(expression.GetType()== Calculator.Expression.ExpressionType.Digit)
            return true;
        if(expression.GetType()== Calculator.Expression.ExpressionType.Variable)
            if(((Calculator.Variable)expression).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                return true;
        return false;
    }
/*
    private BooleanVariable ExecuteBool(Calculator.Expression a, Calculator.Symbol op, Calculator.Expression b)throws Exception {
        double va,vb;
        switch (op.Solve()){
            case ">":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable(va>vb,calculator);
                }
                break;
            }
            case "<":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable(va<vb,calculator);
                }
                break;
            }
            case "==":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable(va==vb,calculator);
                }
                if(a.GetType()== Calculator.Expression.ExpressionType.Variable)
                    if(isSameType(a,b))
                        if(((Calculator.Variable)a).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                            return new BooleanVariable(((BooleanVariable)b).boolean_value==((BooleanVariable)a).boolean_value,calculator);
                break;
            }
            case ">=":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable(va>=vb,calculator);
                }
                break;
            }
            case "<=":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable(va<=vb,calculator);
                }
                break;
            }
            case "!":{
                if(isCalculatable(a)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    return new BooleanVariable((va==0),calculator);
                }
                if(a.GetType()== Calculator.Expression.ExpressionType.Variable)
                    if(isSameType(a,b))
                        if(((Calculator.Variable)a).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                            return new BooleanVariable(!((BooleanVariable)a).boolean_value,calculator);
                break;
            }
            case "||":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable((va!=0)||(vb!=0),calculator);
                }
                if(a.GetType()== Calculator.Expression.ExpressionType.Variable)
                    if(isSameType(a,b))
                        if(((Calculator.Variable)a).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                            return new BooleanVariable(((BooleanVariable)b).boolean_value||((BooleanVariable)a).boolean_value,calculator);
                break;
            }
            case "&&":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable((va!=0)&&(vb!=0),calculator);
                }
                if(a.GetType()== Calculator.Expression.ExpressionType.Variable)
                    if(isSameType(a,b))
                        if(((Calculator.Variable)a).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                            return new BooleanVariable(((BooleanVariable)b).boolean_value&&((BooleanVariable)a).boolean_value,calculator);

                break;
            }
            case "!=":{
                if(isCalculatable(a)&&isCalculatable(b)){
                    va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    return new BooleanVariable((va!=0)!=(vb!=0),calculator);
                }
                if(a.GetType()== Calculator.Expression.ExpressionType.Variable)
                    if(isSameType(a,b))
                        if(((Calculator.Variable)a).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                            return new BooleanVariable(((BooleanVariable)b).boolean_value!=((BooleanVariable)a).boolean_value,calculator);

                break;
            }
            default:throw new Exception(String.format("unknown bool operator symbol \"%s\"",op.Solve()));
        }

        return new BooleanVariable("",FALSE,calculator);
    }
*/
    private Calculator.Expression checkConverExpression(String expression) throws Exception {
        if (isFunction(expression)) {
            //Get function name
            Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = reg.matcher(expression);
            result.find();
            if (result.groupCount() != 2)
                throw new Exception("Cannot parse function ：" + expression);
            String function_name = result.group(1);
            String function_paramters = result.group(2);
            if (!calculator.ContainFunction(function_name))
                throw new Exception(String.format("function %s hadnt declared!", function_name));
            Calculator.Function function = GetFunction(function_name).Copy();
            function.current_paramters = function_paramters;
            return function;
            //Get function paramater list
        }

        if (isDigit(expression)) {
            return new Calculator.Digit(expression);
        }

        if (isValidVariable(expression)) {
            return calculator.GetVariable(expression).Copy();
        }

        return null;
    }

    public static boolean isDigit(String expression) {
        if (expression.isEmpty())
            return false;
        for (char c : expression.toCharArray()) {
            if (!(Character.isDigit(c) || (Character.compare(c, '.') == 0)))
                return false;
        }
        return true;
    }

    public static boolean isValidVariable(String expression) {
        expression.isEmpty();
        if (expression.isEmpty())
            return false;
        if (isDigit(Character.toString(expression.charAt(0))))
            return false;
        if (isDigit(expression))
            return false;
        for (char c : expression.toCharArray())
            if (!(Character.isLetterOrDigit(c) || (Character.compare(c, '_') == 0)))
                return false;
        return true;
    }

    public static boolean isFunction(String expression) {
        if (expression.isEmpty())
            return false;
        if (Character.isDigit(expression.charAt(0)))
            return false;
        int position = 0;
        boolean hasMatchBracket = false, alreadyMatch = false;
        Stack<Integer> bracket_stack = new Stack<>();
        while (true) {
            if (position >= expression.length())
                break;
            if (expression.charAt(position) == '(') {
                bracket_stack.push(position);
            }
            if (expression.charAt(position) == ')') {
                if (bracket_stack.isEmpty())
                    return false;
                bracket_stack.pop();
                if (bracket_stack.isEmpty()) {
                    if (alreadyMatch) {
                        return false;
                    } else {
                        alreadyMatch = true;
                    }
                }
                hasMatchBracket = true;
            }
            position++;
        }
        if (!bracket_stack.isEmpty())
            return false;
        return hasMatchBracket;
    }

    private ArrayList<Calculator.Expression> rawExpressionChain = new ArrayList<>();
    private ArrayList<Calculator.Expression> BSEChain = new ArrayList<>();

    private void ConverToBSE() throws Exception {
        ArrayList<Calculator.Expression> result_list = new ArrayList<>();
        Stack<Calculator.Symbol> operation_stack = new Stack<>();
        Calculator.Symbol symbol = null;
        for (Calculator.Expression node : rawExpressionChain) {
            if (node.GetType() == Calculator.Expression.ExpressionType.Digit||node.GetType() == Calculator.Expression.ExpressionType.Variable)
                result_list.add(node);
            else {
                if (operation_stack.isEmpty())
                    operation_stack.push((Calculator.Symbol) node);
                else {
                    if (!((Calculator.Symbol) node).rawText.equals(")") /*((Calculator.Symbol) node).symbol_type != Calculator.Symbol.SymbolType.Bracket_Right*/) {
                        symbol = operation_stack.peek();
                        while ((symbol == null ? false : (!symbol.rawText.equals("(") && symbol.CompareOperationPrioty((Calculator.Symbol) node) >= 0))/*(symbol == null ? false : (symbol.symbol_type != Calculator.Symbol.SymbolType.Bracket_Left && symbol.CompareOperationPrioty((Calculator.Symbol) node) >= 0))*/) {
                            result_list.add(operation_stack.pop());
                            symbol = operation_stack.size() != 0 ? operation_stack.peek() : null;
                        }
                        operation_stack.push((Calculator.Symbol) node);
                    } else {
                        symbol = operation_stack.peek();
                        while (true) {
                            if (operation_stack.size() == 0)
                                throw new Exception("喵喵喵?");
                            if (symbol.rawText.equals("(")/*symbol.symbol_type == Calculator.Symbol.SymbolType.Bracket_Left*/) {
                                operation_stack.pop();
                                break;
                            }
                            result_list.add(operation_stack.pop());
                            symbol = operation_stack.peek();
                        }
                    }
                }
            }
        }
        while (!operation_stack.isEmpty()) {
            result_list.add(operation_stack.pop());
        }
        Calculator.Expression node;
        for (int i = 0; i < result_list.size(); i++) {
            node = result_list.get(i);
            if (node.GetType() == Calculator.Expression.ExpressionType.Symbol)
                if (((Calculator.Symbol) node).rawText.equals("(")/*((Calculator.Symbol) node).symbol_type == Calculator.Symbol.SymbolType.Bracket_Left*/)
                    result_list.remove(node);
        }
        BSEChain = result_list;
    }

    private boolean ExucuteBSE() throws Exception {
        if (BSEChain.size() == 1)
            if (BSEChain.get(0).GetType() == Calculator.Expression.ExpressionType.Digit)
                return ((((Calculator.Digit) BSEChain.get(0)).GetDouble()))!=0;
        Stack<Calculator.Expression> digit_stack = new Stack<>();
        ArrayList<Calculator.Expression> paramterList,result;
        Calculator.Expression digit_a, digit_b, digit_result;
        Calculator.Symbol operator;
        try {
            for (Calculator.Expression node : BSEChain) {
                if (node.GetType() == Calculator.Expression.ExpressionType.Symbol) {
                    operator = (Calculator.Symbol) node;
                    paramterList=new ArrayList<>();
                    for(int i=0;i<operator.GetParamterCount();i++)
                        paramterList.add(digit_stack.isEmpty() ? new Calculator.Digit("0") : digit_stack.pop());
                    /*digit_b = digit_stack.pop();
                    digit_a = digit_stack.isEmpty() ? new Digit("0") : digit_stack.pop();
                    digit_result = Execute(digit_a, operator, digit_b);*/
                    Collections.reverse(paramterList);
                    result=operator.Solve(paramterList,calculator.Copy());
                    for(Calculator.Expression expr:result)
                        digit_stack.push(expr);
                } else {
                    if (node.GetType() == Calculator.Expression.ExpressionType.Digit||node.GetType() == Calculator.Expression.ExpressionType.Variable) {
                        digit_stack.push(node);
                    } else
                        throw new Exception("Unknown Node");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());

        }
        //get last expression in stack as result and output.
        Calculator.Expression va=digit_stack.pop();
        if(va.GetType()== Calculator.Expression.ExpressionType.Digit)
            return Double.valueOf(digit_stack.pop().Solve())!=0;
        if(va.GetType()== Calculator.Expression.ExpressionType.Variable)
            if(((Calculator.Variable)va).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                return ((BooleanVariable)va).boolean_value;
        throw new Exception("Uncalculatable type :"+va.GetType().toString());
    }

    public BooleanCaculator(Calculator cal){calculator=cal;}

    public Boolean Solve(String expression)throws Exception{
        rawExpressionChain = ParseExpression(expression);
        ConverVariableToDigit();
        ConverFunctionToDigit();
        ConverToBSE();
        return ExucuteBSE();
    }

    static{
        InitBoolOperatorDeclare();
        //register false and true as variables
        try {
            Calculator.RegisterRawVariable(new BooleanVariable(true,null));
            Calculator.RegisterRawVariable(new BooleanVariable(false,null));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void InitBoolOperatorDeclare(){
        Calculator.Symbol.RegisterOperation(">", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va>vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("<", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va<vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation(">=", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va>=vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("<=", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va<=vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("==", 2, 2.5f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va==vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("!=", 2,2.5f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable(va!=vb,calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("&&", 2, 2.3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable((va!=0)&&(vb!=0),calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });

        Calculator.Symbol.RegisterOperation("||", 2, 2f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Copy().Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Copy().Solve(b.Solve()));
                    result.add(new BooleanVariable((va!=0)||(vb!=0),calculator));
                    return result;
                }
                throw new Exception("cant take a pair of valid type to calculate.");
            }
        });
    }
}
