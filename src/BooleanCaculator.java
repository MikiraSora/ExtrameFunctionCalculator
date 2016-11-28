
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
    /**
     * 绑定的计算器
     * */
    Calculator calculator=null;

    /**
     * 获取本对象绑定的计算器
     * @return 绑定的计算器,如果没绑定，则新建一个绑定，再返回
     * */
    private Calculator getCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    static String TRUE="true",FALSE="false";

    public static class BooleanVariable extends Calculator.ExpressionVariable{
        /**
         * 布尔变量的值
         * */
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

        /**
         * 获取此布尔变量的复制实例
         * @return 本实例的复制体
         * @deprecated
         * */
        Calculator.ExpressionVariable Copy() {
            try{
                return this;//new BooleanVariable(boolean_value,calculator);
            }catch (Exception e){
                return null;
            }
        }

        @Override
        boolean isCalculatable() {
            return false;
        }

        /**
         * 是否可以直接设置变量
         * @return 返回true则可以直接设置变量的值,反之不可以.
         * */
        @Override
        boolean ableSetVariableDirectly() {
            return false;
        }

        /**
         * 获取布尔变量的值
         * @return 返回1代表true,返回0代表false
         * */
        @Override
        String Solve() {
            return boolean_value?"1":"0";
        }

        /**
         * 获取数值形式的布尔值
         * @return 数值类对象
         * */
        @Override
        public Calculator.Digit GetDigit() throws Exception {
            return new Calculator.Digit(Solve());
        }


        /**
         * 返回布尔值名
         * @return 返回"True"或"False"
         * */
        @Override
        String GetName() {
            return boolean_value?TRUE:FALSE;//boolean_value?"bool_"+TRUE:"bool_"+FALSE;
        }

        /**
         * 序列化当前对象为文本
         * @return 序列化后的文本
         * */
        @Override
        String Serialize() {
            return String.format("b##%s##%s",Variable_name,rawText);
        }

    }


    /**
     * 获取指定名称的函数，会在内置函数列表和定义函数中搜寻
     * @param name 函数名
     * @return 函数对象
     * */
    Calculator.Function GetFunction(String name) throws Exception, Calculator.FunctionNotFoundException {
        return calculator.GetFunction(name);
    }

    /**
     * 获取指定名称的变量，会在内置变量列表和定义变量中搜寻
     * @param name 变量名
     * @return 变量对象
     * */
    Calculator.Variable GetVariable(String name) throws Exception {
        return calculator.GetVariable(name);
    }

    /**
     * 操作符列表
     * */
    private static String specialOperationChar = "+ ++ - -- * / ~ ! != @ # $ % ^ & && ( ) ; : \" \" || ? > >= < <= , ` ' = == ";

    /**
     * 获取名为name的变量的值
     * @param name 变量名
     * @return name变量的值
     * */
    private String RequestVariable(String name) throws Calculator.VariableNotFoundException,Exception{
        return calculator.Solve(name);
    }

    /**
     * 解析文本成表达式链
     * @param expression 表达式文本,如"4+x/(abs(sin(x))+6)+1*2>0==false"
     * @return 解析好的表达式链表
     * */
    ArrayList<Calculator.Expression> ParseExpression(String expression) throws Exception {
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
                            //判断是否有无限循环小数格式的可能
                            if(!isDigit(statement)){
                                bracket_stack.push(position);
                                statement += c;
                            }
                            else {
                                //无限循环小数格式
                                int size=0;
                                while(true){
                                    statement+=c;
                                    if(c==')')
                                        break;
                                    size++;
                                    c=expression.charAt(++position);
                                }
                                expressionArrayList.add(new Calculator.ExpressionVariable("",Calculator.RepeatingDecimalCoverToExpression(statement),getCalculator()));
                                statement="";
                                break;
                            }
                        } else {
                            if (c == ')') {
                                if (bracket_stack.isEmpty())
                                    Log.ExceptionError( new Exception("Extra brackets in position: " + position));
                                bracket_stack.pop();
                                if (bracket_stack.isEmpty()) {
                                    statement += ")";
                                    expressionArrayList.add(checkConverExpression(statement));//should always return Function
                                    break;
                                }
                                //statement += c;
                            } /*else {
                                //statement += c;
                            }*/
                            statement += c;
                        }
                        position++;
                    }
                }else if((!statement.isEmpty())&&c=='['){
                    //array
                    char tmp_ch=0;
                    //position--;
                    //读取下标
                    String indexes="";
                    Stack<Integer> balanceStack=new Stack<>();

                    while (true){
                        if(position>=expression.length())
                            Log.Error(String.format("%s isnt vaild format",expression));
                        tmp_ch=expression.charAt(position);
                        indexes+=tmp_ch;
                        position++;
                        if(tmp_ch=='['){
                            balanceStack.push(position);
                            continue;
                        }
                        if(tmp_ch==']'){
                            if(position>=expression.length())
                                break;
                            balanceStack.pop();
                            if (balanceStack.isEmpty()){
                                tmp_ch=expression.charAt(position);
                                if(tmp_ch!='[')
                                    break;
                            }
                        }
                    }
                    Calculator.Variable variable=GetVariable(statement);
                    if (variable==null)
                        Log.ExceptionError(new Calculator.VariableNotFoundException(statement));
                    if(variable.variable_type!= Calculator.Variable.VariableType.MapVariable)
                        Log.ExceptionError(new Exception(String.format("%s isnt MapVariable",statement)));
                    ((Calculator.MapVariable)variable).SetIndexes(indexes);
                    expressionArrayList.add(new Calculator.WrapperVariable(variable,indexes));
                    position--;
                }
                else {
                    expr = checkConverExpression(statement);
                    if (expr != null)
                        expressionArrayList.add(expr);
                    tmp_op=Character.toString(c);
                    {
                        if(position<(expression.length()-1)){
                            tmp_c=expression.charAt(position+1);
                            tmp_op+=tmp_c;
                            position++;
                            if(!specialOperationChar.contains(" "+(tmp_op)+" ")){
                                tmp_op=Character.toString(c);
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
    /*
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
                                Log.ExceptionError( new Exception("Extra brackets in position: " + position));
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
*/

    /**
     * 将表达式链中的函数都进行计算并将结果替换
     * */
    private void ConverFunctionToDigit() throws Calculator.FunctionNotFoundException, Calculator.VariableNotFoundException,Exception {
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

    /**
     * 将表达式链中的变量都进行计算并将结果替换
     * */
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

    /**
     * 布尔运算操作符
     * */
    static String boolOperatorSymbol="< > == <= >= || && !=";

    /**
     * 判断是否为布尔运算操作符
     * @return 如果返回true则是布尔运算操作符，反之不是。
     * */
    private static boolean isBooleanOperator(Calculator.Symbol op){
        return boolOperatorSymbol.contains(op.rawText);
    }

    /**
     * 判断两者是否同类表达式
     * @return 如果返回true则是同类表达式,反之不是。
     * */
    private static boolean isSameType(Calculator.Expression a, Calculator.Expression b){
        if(a.GetType()!=b.GetType())
            return false;
        return true;
    }

    /**
     * 判断表达式是否可计算出值
     * @return 如果返回true则是可以直接计算，反之不是。
     * */
    private static boolean isCalculatable(Calculator.Expression expression){
        if(expression.GetType()== Calculator.Expression.ExpressionType.Digit)
            return true;
        if(expression.GetType()== Calculator.Expression.ExpressionType.Variable)
            if(((Calculator.Variable)expression).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                return true;
        return false;
    }

    /**
     * 函数格式解析正则表达式
     * */
    private static Pattern checkFunctionFormatRegex = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");

    /**
     * 分析文本，并转换成相对应的表达式元素对象
     * @param text 传入文本
     * @return 解析后的表达式对象
     * */
    private Calculator.Expression checkConverExpression(String text) throws Exception {
        if (isFunction(text)) {
            //Get function name
            //Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = checkFunctionFormatRegex.matcher(text);
            result.find();
            if (result.groupCount() != 2)
                Log.ExceptionError( new Exception("Cannot parse function ：" + text));
            String function_name = result.group(1);
            String function_paramters = result.group(2);
            if (!calculator.ContainFunction(function_name))
                Log.ExceptionError( new Exception(String.format("function %s hadnt declared!", function_name)));
            Calculator.WrapperFunction function=new Calculator.WrapperFunction(GetFunction(function_name),function_paramters);
            return function;
            //Get function paramater list
        }

        if (isDigit(text)) {
            return new Calculator.Digit(text);
        }

        if (isValidVariable(text)) {
            Calculator.Variable variable=GetVariable(text);
            if(variable==null)
                Log.ExceptionError(new Calculator.VariableNotFoundException(text));
            //因为MapVariable并不在此处理所以为了减少引用调用所以不用new WrapperVariable;
            return variable;
        }

        return null;
    }

    /**
     * 判断字符串是否为有效数字
     * @param expression 测试文本
     * @return 判断结果
     * */
    public static boolean isDigit(String expression) {
        if (expression.isEmpty())
            return false;
        for (char c : expression.toCharArray()) {
            if (!(Character.isDigit(c) || (Character.compare(c, '.') == 0)))
                return false;
        }
        return true;
    }

    /**
     * 判断字符串是否为有效的变量名(不管这个变量是否存在)
     * @param expression 测试文本
     * @return 判断结果
     * */
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

    /**
     * 判断字符串是否为有效的函数名(不管这个函数是否存在)
     * @param expression 测试文本
     * @return 判断结果
     * */
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

    /**
     * 将表达式链转换成后缀表达式并返回
     * @return 后缀表达式形式的表达式链表
     * */
    private void ConverToBSE() throws Exception {
        ArrayList<Calculator.Expression> result_list = new ArrayList<>();
        Stack<Calculator.Symbol> operation_stack = new Stack<>();
        Calculator.Symbol symbol = null;
        Calculator.Expression node=null;
        for (int position=0;position<rawExpressionChain.size();position++) {
            node=rawExpressionChain.get(position);
            if (node.GetType() == Calculator.Expression.ExpressionType.Digit||node.GetType() == Calculator.Expression.ExpressionType.Variable)
                result_list.add(node);
            else {
                if(((Calculator.Symbol)node).rawText.equals("-")){
                    if(position==0){
                        result_list.add(new Calculator.Digit("0"));
                    }else if(rawExpressionChain.get(position-1).GetType()== Calculator.Expression.ExpressionType.Symbol)
                        if(((Calculator.Symbol)rawExpressionChain.get(position-1)).rawText.equals("("))
                            result_list.add(new Calculator.Digit("0"));
                }
                if (operation_stack.isEmpty())
                    operation_stack.push((Calculator.Symbol) node);
                else {
                    if (!((Calculator.Symbol) node).rawText.equals(")")) {
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
                                Log.ExceptionError( new Exception("喵喵喵?"));
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
        //Calculator.Expression node;
        for (int i = 0; i < result_list.size(); i++) {
            node = result_list.get(i);
            if (node.GetType() == Calculator.Expression.ExpressionType.Symbol)
                if (((Calculator.Symbol) node).rawText.equals("(")/*((Calculator.Symbol) node).symbol_type == Calculator.Symbol.SymbolType.Bracket_Left*/)
                    result_list.remove(node);
        }
        BSEChain = result_list;
    }

    /**
     * 执行后缀表达式链表，并输出结果
     * @return 计算结果
     * */
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
                    Collections.reverse(paramterList);
                    result=operator.Solve(paramterList,calculator);
                    for(Calculator.Expression expr:result)
                        digit_stack.push(expr);
                } else {
                    if (node.GetType() == Calculator.Expression.ExpressionType.Digit||node.GetType() == Calculator.Expression.ExpressionType.Variable) {
                        digit_stack.push(node);
                    } else
                        Log.ExceptionError( new Exception("Unknown Node"));
                }
            }
        } catch (Exception e) {
            Log.ExceptionError( new Exception(e.getMessage()));

        }
        //get last expression in stack as result and output.
        Calculator.Expression va=digit_stack.pop();
        if(va.GetType()== Calculator.Expression.ExpressionType.Digit)
            return Double.valueOf(va.Solve())!=0;
        if(va.GetType()== Calculator.Expression.ExpressionType.Variable)
            if(((Calculator.Variable)va).variable_type== Calculator.Variable.VariableType.BooleanVariable)
                return ((BooleanVariable)va).boolean_value;
        Log.ExceptionError( new Exception("Uncalculatable type :"+va.GetType().toString()));
        return false;
    }

    public BooleanCaculator(Calculator cal){calculator=cal;}

    /**
     * 布尔运算表达式文本
     *  @param expression 表达式文本,如"4+f(2)-1*a/b"
     *  @return 运算结果
     * */
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
            Log.Warning(e.getMessage());
        }
    }

    /**
     * 初始动态加载布尔运算操作符
     * */
    private static void InitBoolOperatorDeclare(){
        Calculator.Symbol.RegisterOperation(">", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va>vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("<", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va<vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation(">=", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va>=vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("<=", 2, 3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va<=vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("==", 2, 2.5f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va==vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("!=", 2,2.5f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable(va!=vb,calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("&&", 2, 2.3f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable((va!=0)&&(vb!=0),calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });

        Calculator.Symbol.RegisterOperation("||", 2, 2f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Expression a=paramterList.get(0),b=paramterList.get(1);
                if(isCalculatable(a)&&isCalculatable(b)){
                    double va=Double.valueOf(calculator.Solve(a.Solve()));
                    double vb=Double.valueOf(calculator.Solve(b.Solve()));
                    result.add(new BooleanVariable((va!=0)||(vb!=0),calculator));
                    return result;
                }
                Log.ExceptionError( new Exception("cant take a pair of valid type to calculate."));return null;
            }
        });
    }
}
