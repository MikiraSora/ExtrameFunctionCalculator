
import java.io.*;
import java.sql.Ref;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mikir on 2016/8/20.
 */
    public class Calculator {
    static abstract class Expression {
        Expression() {
            this(null);
        }

        Expression(Calculator calculator1) {
            setCalculator(calculator1);
        }

        enum ExpressionType {
            Function,
            Variable,
            Digit,
            Symbol,
            Unknown
        }


        int Generation=0;

        ExpressionType GetType() {
            return ExpressionType.Unknown;
        }

        String rawText = null;
        Calculator calculator = null;

        boolean isCalculatable(){return false;}

        void setCalculator(Calculator calculator1) {
            calculator = calculator1;
        }

        Calculator getCalculator() {
            return calculator==null?(this.calculator=new Calculator()):calculator.Copy();
        }

        String Solve() {
            return rawText;
        }

        ;

        String GetName() {
            return null;
        }

        //static Expression Deserialize(String text)throws Exception{return null;}

        String Serialize(){
            return null;
        }
    }

    public static class ReflectionFunction extends Function {
        ReflectionFunction(String expression, OnReflectionFunction onReflectionFunction) throws Exception {
            super();
            if (expression == null)
                return;
            setReflectionFunction(onReflectionFunction);
            rawText = expression;
            Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = reg.matcher(expression);
            result.find();
            if (result.groupCount() != 2)
                throw new Exception("Cannot parse function ：" + expression);
            function_name = result.group(1);
            function_paramters = result.group(2);
            ParameterRequest parameterRequest = new ParameterRequest(function_paramters);
            request = parameterRequest;
            function_type = FunctionType.Reflection_Function;
        }

        public static abstract class OnReflectionFunction {
            public String onReflectionFunction(HashMap<String, Variable> parameter, Calculator calculator) throws Exception {
                return null;
            }

            //如果不重写此办法或者重写却return null,那么将会自动调用Parse()来自己解析。
            public HashMap<String,Variable> onParseParamter(String paramter,ParameterRequest request,Calculator calculator)throws Exception{
                return null;
            }
        }

        OnReflectionFunction reflectionFunction = null;

        public void setReflectionFunction(OnReflectionFunction reflectionFunction) {
            this.reflectionFunction = reflectionFunction;
        }

        @Override
        String Solve() {
            try {
                return Solve(current_paramters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", function_name, function_paramters);
        }

        public ReflectionFunction Copy() throws Exception {
            /*
            ReflectionFunction reflectionFunction = new ReflectionFunction(rawText, this.reflectionFunction);
            reflectionFunction.current_paramters = this.current_paramters;
            return reflectionFunction;*/
            return this;
        }

        @Override
        String Solve(String parameterList) throws Exception {
            HashMap<String,Variable> custom_paramter=reflectionFunction.onParseParamter(parameterList,request,getCalculator());
            if(custom_paramter==null)
                Parse(parameterList);
            else
                paramter=custom_paramter;
            return /*getCalculator().Solve(*/reflectionFunction.onReflectionFunction(paramter, getCalculator());//);
        }
    }



    public static class Function extends Expression {
        enum FunctionType {
            Normal_Function,
            Reflection_Function,
            Unknown
        }

        FunctionType function_type = FunctionType.Unknown;



        public FunctionType getFunction_type() {
            return function_type;
        }

        protected String function_name, function_paramters, function_body;
        protected String current_paramters = "";

        protected Function() {
            super();
        }

        public Function(String expression, Calculator calculator1) throws Exception {
            super(calculator1);
            if (expression == null)
                return;
            rawText = expression;
            Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)=(.+)");
            Matcher result = reg.matcher(expression);
            result.find();
            if (result.groupCount() != 3)
                throw new Exception("Cannot parse function ：" + expression);
            function_name = result.group(1);
            function_paramters = result.group(2);
            ParameterRequest parameterRequest = new ParameterRequest(function_paramters);
            request = parameterRequest;
            function_body = result.group(3);
            function_type = FunctionType.Normal_Function;
        }

        public Function Copy() throws Exception {
            /*Function function = new Function(rawText, getCalculator());
            function.current_paramters = this.current_paramters;
            return function;*/
            return this;
        }

        class ParameterRequest {
            private ArrayList<String> requestion_list = new ArrayList<String>();

            private ParameterRequest() {
            }

            public ParameterRequest(String rawText) {
                char c;
                String name = new String();
                for (int pos = 0; pos < rawText.length(); pos++) {
                    c = rawText.charAt(pos);
                    if (c == ',') {
                        requestion_list.add(name);
                        name = new String();
                    } else {
                        name += c;
                    }
                }
                if (!name.isEmpty())
                    requestion_list.add(name);
            }

            public int GetParamterRequestCount(){return requestion_list.size();}

            public String[] GetParamterNameArray(){
                String[] array=new String[GetParamterRequestCount()];
                requestion_list.toArray(array);
                return array;
            }

            public String GetParamterName(int index){return requestion_list.get(index);}
        }

        protected HashMap<String, Variable> paramter = new HashMap<>();
        protected ParameterRequest request;

        protected void Parse(String name) throws Exception {
            char c;
            int requestIndex = 0;
            Stack<Integer> BracketStack = new Stack<>();
            String paramter = new String();
            for (int pos = 0; pos < name.length(); pos++) {
                c = name.charAt(pos);
                if (c ==    '(') {
                    BracketStack.push(pos);
                } else if (c == ')') {
                    if (!BracketStack.isEmpty())
                        //BracketStack.push(pos);
                        BracketStack.pop();
                    else
                        throw new Exception("Not found a pair of bracket what defining a expression");
                }

                if (c == ',' && BracketStack.isEmpty()) {
                    String requestParamterName = request.requestion_list.get(requestIndex++);
                    this.paramter.put(requestParamterName, new Variable(requestParamterName, getCalculator().Solve(paramter), getCalculator()));
                    paramter = new String();
                } else {
                    paramter += c;
                }
            }
            if (!paramter.isEmpty())
                this.paramter.put(request.requestion_list.get(requestIndex), new ExpressionVariable(request.requestion_list.get(requestIndex), getCalculator().Solve(paramter), getCalculator()));
        }

        protected String ParseDeclaring(String expression) throws Exception, StatementNotDeclaredException {
            String newExpression = expression;
            for (Map.Entry<String, Variable> pair : paramter.entrySet()) {
                newExpression = newExpression.replace(pair.getKey(), pair.getValue().Solve());
            }
            return newExpression;
        }


        @Override
        public String toString() {
            return String.format("%s(%s)=%s", function_name, function_paramters, function_body);
        }

        @Override
        String Solve() {
            try {
                return Solve(current_paramters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        String Solve(String parameterList) throws Exception {
            String exression;
            Parse(parameterList);
            exression = ParseDeclaring(function_body);
            return getCalculator().Solve(exression);
        }

        Digit GetSolveToDigit() {
            return new Digit(Solve());
        }

        @Override
        String GetName() {
            return function_name;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Function;
        }

        private static String reg_expr="(\\w)##([a-zA-Z_]+\\w*)##(.*?)##(.*)";
        static Function Deserialize(String text)throws Exception{
            Pattern reg=Pattern.compile(reg_expr);
            Matcher result=reg.matcher(text);
            if(!result.find())
                throw new Exception("Cannot parse text :"+text);
            String rawText=String.format("%s(%s)=%s",result.group(2),result.group(3),result.group(4));
            Function function=new Function(rawText,null);
            return function;
        }

        @Override
        String Serialize() {
            return String.format("f##%s##%s##%s",function_name,function_paramters,function_body);
        }
    }

    public static class Symbol extends Expression {

        public int CompareOperationPrioty(Symbol symbol) {
            float val = OperatorPrioty.get(this.rawText) - OperatorPrioty.get(symbol.rawText);
            return val == 0 ? 0 : (val > 0 ? 1 : -1);
        }

        Symbol(String op) {
            rawText = op;
        }

        @Override
        String Solve() {
            return super.Solve();
        }

        @Override
        public String toString() {
            return "operation symbol : " + rawText;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Symbol;
        }

        static HashMap<String,OperatorFunction> OperatorFunction=new HashMap<>();
        static HashMap<String,Float> OperatorPrioty=new HashMap<>();
        static HashMap<String,Integer> OperatorRequestParamterCount=new HashMap<>();

        static abstract class OperatorFunction{
            ArrayList<Expression> onCalculate(ArrayList<Expression> paramterList,Calculator calculator) throws Exception{return null;}
        }

        public static void RegisterOperation(String operatorSymbol,int requestParamterSize,float operatorPrioty,OperatorFunction operatorFunction) {
            OperatorFunction.put(operatorSymbol,operatorFunction);
            OperatorPrioty.put(operatorSymbol,operatorPrioty);
            OperatorRequestParamterCount.put(operatorSymbol,requestParamterSize);
        }

        ArrayList<Expression> Solve(ArrayList<Expression> paramterList,Calculator calculator)throws Exception{
            return OperatorFunction.get(rawText).onCalculate(paramterList, calculator);
        }

        int GetParamterCount(){return OperatorRequestParamterCount.get(rawText);}
    }

    class VariableNotFoundException extends Exception {
        String variable_name;

        private VariableNotFoundException() {
        }

        public VariableNotFoundException(String name) {
            variable_name = name;
        }

        @Override
        public String getMessage() {
            return variable_name + " is not found.";
        }
    }

    class StatementNotDeclaredException extends Exception {
        String name;
        int position;

        private StatementNotDeclaredException() {
        }

        public StatementNotDeclaredException(String n, int p) {
            name = n;
            position = p;
        }

        @Override
        public String getMessage() {
            return String.format("In position %d , name \"%s\" hadn't been declared in Function or Variable.", position, name);
        }
    }

    public static class Variable extends Expression {


        enum VariableType{
            ExpressionVariable,
            BooleanVariable,
            Normal,
            Unknown
        }
        VariableType variable_type=VariableType.Unknown;
        String Variable_name;

        public Variable(String name, String value, Calculator calculator1) {
            super();
            variable_type=VariableType.Normal;
            rawText = value;
            Variable_name = name;
            setCalculator(calculator1);
        }


        @Override
        boolean isCalculatable() {
            return true;
        }

        boolean ableSetVariableDirectly(){
            return true;
        }

        @Override
        String Solve() {
            return super.Solve();
        }

        @Override
        public String toString() {
            return String.format("%s=%s", GetName(), Solve());
        }

        Variable Copy() {
            Variable variable = new Variable(Variable_name, this.rawText, getCalculator());
            return variable;
        }

        public Digit GetDigit() throws Exception {
            return new Digit(rawText == null ? getCalculator().RequestVariable(this.GetName()) : rawText);
        }

        @Override
        String GetName() {
            return Variable_name;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Variable;
        }

        private static String reg_expr="(\\w)##([a-zA-Z_]+\\w*)##(.*)";
        static Variable Deserialize(VariableType type,String text)throws Exception{
            Pattern reg=Pattern.compile(reg_expr);
            Matcher result=reg.matcher(text);
            if(!result.find())
                throw new Exception("Cannot parse text :"+text);
            //String rawText=String.format("%s=%s",result.group(2),result.group(3));
            switch (type){
                case Normal:return new Variable(result.group(2),result.group(3),null);
                case ExpressionVariable:return new ExpressionVariable(result.group(2),result.group(3),null);
                case BooleanVariable:return new BooleanCaculator.BooleanVariable(result.group(2),result.group(3),null);

            }
            return null;
        }

        @Override
        String Serialize() {
            return String.format("v##%s##%s",Variable_name,rawText);
        }
    }

    public static class Digit extends Expression {

        //

        public Digit(String value) {
            rawText = value;
        }

        @Override
        String Solve() {
            return rawText;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Digit;
        }

        public double GetDouble() {
            return Double.parseDouble(Solve());
        }

        public int GetInteger() {
            return Integer.valueOf(Solve());
        }

        @Override
        String GetName() {
            return super.GetName();
        }

        @Override
        public String toString() {
            return "digit : " + Solve();
        }
    }


    class FunctionNotFoundException extends Exception {

        String function_name;

        private FunctionNotFoundException() {
        }

        public FunctionNotFoundException(String name) {
            function_name = name;
        }

        @Override
        public String getMessage() {
            return function_name + " is not found.";
        }

    }

    static class ExpressionVariable extends Variable{


        ExpressionVariable(String name,String expr_value,Calculator calculator1){
            super(name,expr_value,calculator1);
            variable_type=VariableType.ExpressionVariable;
        }

        @Override
        String Solve(){
            try {
                return getCalculator().Solve(rawText);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        boolean isCalculatable() {
            return true;
        }

        @Override
        ExpressionVariable Copy() {
            return new ExpressionVariable(Variable_name,rawText,getCalculator());
        }

        @Override
        public Digit GetDigit() throws Exception {
            return new Digit(Solve());
        }
        private static String reg_expr="(\\w)##([a-zA-Z_]+\\w*)##(.*)";
        static ExpressionVariable Deserialize(String text)throws Exception{
            Pattern reg=Pattern.compile(reg_expr);
            Matcher result=reg.matcher(text);
            if(!result.find())
                throw new Exception("Cannot parse text :"+text);
            return new ExpressionVariable(result.group(2),result.group(3),null);
        }

        @Override
        String Serialize() {
            return String.format("e##%s##%s",Variable_name,rawText);
        }

        @Override
        public String toString() {
            return String.format("<expr> %s=%s",Variable_name,rawText);
        }

        public String GetExpreesion(){return rawText;}
    }

    private HashMap<String, Function> function_table = new HashMap<>();
    private HashMap<String, Variable> variable_table = new HashMap<>();

    private static HashMap<String, ReflectionFunction> raw_function_table = new HashMap<>();
    private static HashMap<String, Variable> raw_variable_table = new HashMap<>();

    int Generation=0;

    static {
        CalculatorHelper.InitOperatorDeclare();
        CalculatorHelper.InitFunctionDeclare();
    }


    Function GetFunction(String name) throws Exception, FunctionNotFoundException {
        if (raw_function_table.containsKey(name)) {
            Function function = raw_function_table.get(name).Copy();
            function.setCalculator(this);
            return function;
        }
        if (!function_table.containsKey(name))
            throw new FunctionNotFoundException(name);
        return function_table.get(name);
    }

    Variable GetVariable(String name) throws VariableNotFoundException {
        if(raw_variable_table.containsKey(name)){
            return raw_variable_table.get(name);
        }
        if (!variable_table.containsKey(name))
            throw new VariableNotFoundException(name);
        return variable_table.get(name);
    }

    private void RegisterFunction(Function function) {
        function_table.put(function.GetName(), function);
    }

    private void RegisterVariable(Variable variable) {
        variable_table.put(variable.GetName(), variable);
    }

    private static String specialOperationChar = " + - * / ~ ! @ # $ % ^ & ( ) ; : \" | ? > < , ` ' \\ ";

    private String RequestVariable(String name) throws VariableNotFoundException {
        if (!variable_table.containsKey(name))
            throw new VariableNotFoundException(name);
        return variable_table.get(name).Solve();
    }

    private ArrayList<Expression> ParseExpression(String expression) throws Exception {
        ArrayList<Expression> expressionArrayList = new ArrayList<>();
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

    private void ConverFunctionToDigit() throws FunctionNotFoundException, VariableNotFoundException {
        int position = 0;
        Expression node;
        Function function;
        Digit result;
        for (position = 0; position < getRawExpressionChain_Stack().size(); position++) {
            node = getRawExpressionChain_Stack().get(position);
            if (node.GetType() == Expression.ExpressionType.Function) {
                function = (Function) node;
                if (function.getFunction_type() == Function.FunctionType.Reflection_Function)
                    function.setCalculator(this);
                result = function.GetSolveToDigit();
                getRawExpressionChain_Stack().remove(position);
                getRawExpressionChain_Stack().add(position, result);
            }
        }
    }

    private void ConverVariableToDigit() throws Exception, VariableNotFoundException {
        int position = 0;
        Expression node;
        Variable variable;
        Digit result;
        try {
            for (position = 0; position < getRawExpressionChain_Stack().size(); position++) {
                node = getRawExpressionChain_Stack().get(position);
                if (node.GetType() == Expression.ExpressionType.Variable) {
                    variable = (Variable) node;
                    result = variable.GetDigit();
                    getRawExpressionChain_Stack().remove(position);
                    getRawExpressionChain_Stack().add(position, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Expression checkConverExpression(String expression) throws Exception {
        if (isFunction(expression)) {
            //Get function name
            Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = reg.matcher(expression);
            result.find();
            if (result.groupCount() != 2)
                throw new Exception("Cannot parse function ：" + expression);
            String function_name = result.group(1);
            String function_paramters = result.group(2);
            if (!ContainFunction(function_name))
                throw new Exception(String.format("function %s hadnt declared!", function_name));
            Function function = GetFunction(function_name).Copy();
            function.current_paramters = function_paramters;
            return function;
            //Get function paramater list
        }

        if (isDigit(expression)) {
            return new Digit(expression);
        }

        if (isValidVariable(expression)) {
            return GetVariable(expression).Copy();
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
/*
    private ArrayList<Expression> rawExpressionChain = new ArrayList<>();
    private ArrayList<Expression> BSEChain = new ArrayList<>();
*/
    private void ConverToBSE() throws Exception {
        ArrayList<Expression> result_list = new ArrayList<>();
        Stack<Symbol> operation_stack = new Stack<>();
        Symbol symbol = null;
        for (Expression node : getRawExpressionChain_Stack()) {
            if (node.GetType() == Expression.ExpressionType.Digit)
                result_list.add(node);
            else {
                if (operation_stack.isEmpty())
                    operation_stack.push((Symbol) node);
                else {
                    if (!(((Symbol) node).rawText.equals(")")/*((Symbol) node).symbol_type != Symbol.SymbolType.Bracket_Right*/)){// )
                        symbol = operation_stack.peek();
                        /*
                        while ((symbol == null ? false : (symbol.symbol_type != Symbol.SymbolType.Bracket_Left && symbol.CompareOperationPrioty((Symbol) node) >= 0))) {
                            result_list.add(operation_stack.pop());
                            symbol = operation_stack.size() != 0 ? operation_stack.peek() : null;
                        }*/

                        while(symbol!=null){
                            if(!(symbol.rawText .equals("("))/*&&((Symbol)node).symbol_type!= Symbol.SymbolType.Bracket_Left*/)
                                    if(symbol.CompareOperationPrioty((Symbol) node) >= 0){
                                    result_list.add(operation_stack.pop());
                                    symbol = operation_stack.size() != 0 ? operation_stack.peek() : null;
                                    continue;
                                }
                            break;
                        }
                        operation_stack.push((Symbol) node);
                    } else {
                        symbol = operation_stack.peek();
                        while (true) {
                            if (operation_stack.size() == 0)
                                throw new Exception("喵喵喵?");
                            if ((symbol.rawText.equals("("))/*symbol.symbol_type!= Symbol.SymbolType.Bracket_Left*/) {
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
        Expression node;
        for (int i = 0; i < result_list.size(); i++) {
            node = result_list.get(i);
            if (node.GetType() == Expression.ExpressionType.Symbol)
                if (((Symbol) node).rawText.equals("(")/*((Symbol) node).symbol_type == Symbol.SymbolType.Bracket_Left*/)
                    result_list.remove(node);
        }
        setBSEChain_Stack(result_list);
    }

    private String ExucuteBSE() throws Exception {
        if (getBSEChain_Stack().size() == 1)
            if (getBSEChain_Stack().get(0).GetType() == Expression.ExpressionType.Digit)
                return String.valueOf((((Digit) getBSEChain_Stack().get(0)).GetDouble()));
        Stack<Expression> digit_stack = new Stack<>();
        //Stack<Digit> digit_stack = new Stack<>();
        Expression/*Digit*/ digit_a, digit_b, digit_result;
        Symbol operator;
        ArrayList<Expression> paramterList,result;
        try {
            for (Expression node : getBSEChain_Stack()) {
                if (node.GetType() == Expression.ExpressionType.Symbol) {
                    operator = (Symbol) node;
                    paramterList=new ArrayList<>();
                    for(int i=0;i<operator.GetParamterCount();i++)
                        paramterList.add(digit_stack.isEmpty() ? new Digit("0") : digit_stack.pop());
                    /*digit_b = digit_stack.pop();
                    digit_a = digit_stack.isEmpty() ? new Digit("0") : digit_stack.pop();
                    digit_result = Execute(digit_a, operator, digit_b);*/
                    Collections.reverse(paramterList);
                    result=operator.Solve(paramterList,Copy());
                    for(Expression expr:result)
                        digit_stack.push(expr);
                } else {
                    if (node.GetType() == Expression.ExpressionType.Digit) {
                        digit_stack.push((Digit) node);
                    } else
                        throw new Exception("Unknown Node");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());

        }
        return digit_stack.pop().Solve();
    }

    private void CheckNormalizeChain() throws Exception {
        for (Expression node : getRawExpressionChain_Stack()) {
            if (node.GetType() != Expression.ExpressionType.Digit && node.GetType() != Expression.ExpressionType.Symbol)
                throw new Exception(node.GetName() + " isnt digit or symbol.");
        }
    }

    Stack<ArrayList<Expression>> BSEChain_Stack=new Stack<>();
    Stack<ArrayList<Expression>> rawExpressionChain_Stack=new Stack<>();

    private void Init_Solve(){
        //???
    }

    private void  setBSEChain_Stack(ArrayList<Expression> list){BSEChain_Stack.push(list);}
    private void  setRawExpressionChain_Stack(ArrayList<Expression> list){rawExpressionChain_Stack.push(list);}

    private ArrayList<Expression> getBSEChain_Stack(){return BSEChain_Stack.empty()?BSEChain_Stack.push(new ArrayList<Expression>()):BSEChain_Stack.peek();}
    private ArrayList<Expression> getRawExpressionChain_Stack(){return rawExpressionChain_Stack.empty()?BSEChain_Stack.push(new ArrayList<Expression>()):rawExpressionChain_Stack.peek();}

    private void Term_Solve(){
        BSEChain_Stack.pop();
        rawExpressionChain_Stack.pop();
    }

    String Solve(String expression) throws Exception {
        //rawExpressionChain = ParseExpression(expression);
        setRawExpressionChain_Stack(ParseExpression(expression));
        ConverVariableToDigit();
        ConverFunctionToDigit();
        CheckNormalizeChain();
        ConverToBSE();

        String result= ExucuteBSE();

        Term_Solve();
        return result;
    }

    public String Execute(String text) throws Exception {
        Clear();
        if (text.isEmpty())
            throw new Exception("input empty text to execute");
        char c;
        String executeType = "", paramter = "";
        String result = "";
        for (int position = 0; position < text.length(); position++) {
            c = text.charAt(position);
            if (c == ' ') {
                paramter = text.substring(position + 1);
                break;
            } else {
                executeType += c;
            }
        }
        switch (executeType) {
            case "set": {
                SetVariable(paramter);
                break;
            }
            case "set_expr":{
                SetExpressionVariable(paramter);
                break;
            }
            case "reg": {
                SetFunction(paramter);
                break;
            }
            case "solve": {
                result = Solve(paramter);
                break;
            }
            case "dump": {
                result = DumpInfo(paramter);
                break;
            }
            case "help": {
                result = CalculatorHelper.GetHelp();
                break;
            }
            case "load":{
                result = Load(paramter);
                break;
            }
            case "clear":{
                result=Clear();
                break;
            }
            case "reset":{
                result=Reset();
                break;
            }
            case "save":{
                String type="",output_path="";
                for (int position = 0; position < paramter.length(); position++) {
                    c = paramter.charAt(position);
                    if (c == ' ') {
                        output_path = paramter.substring(position + 1);
                        break;
                    } else {
                        type += c;
                    }
                }
                result=Save(type,output_path);
                break;
            }
            case "delete":{
                String type="",name="";
                for (int position = 0; position < paramter.length(); position++) {
                    c = paramter.charAt(position);
                    if (c == ' ') {
                        name = paramter.substring(position + 1);
                        break;
                    } else {
                        type += c;
                    }
                }
                result=Delete(type,name);
                break;
            }
            default: {
                throw new Exception(String.format("unknown execution other \"%s\"", executeType));
            }
        }
        return result;
    }

    private void SetVariable(String expression) throws Exception {
        if (expression.isEmpty())
            throw new Exception("empty text");
        char c;
        String variable_name = "", variable_expression = "";
        Variable variable = null;
        for (int position = 0; position < expression.length() - 1; position++) {
            c = expression.charAt(position);
            if (c == '=') {
                variable_expression = expression.substring(position + 1);
                variable = new Variable(variable_name, Solve(variable_expression), this);
                RegisterVariable(variable);
                break;
            } else {
                variable_name += c;
            }
        }
    }

    private void SetFunction(String expression) throws Exception {
        if (expression.isEmpty())
            throw new Exception("empty text");
        Function function = new Function(expression, this);
        RegisterFunction(function);
    }

    public String Reset() {
        Clear();
        this.variable_table.clear();
        this.function_table.clear();
        return "Reset finished!";
    }

    private String Clear() {
        this.getBSEChain_Stack().clear();
        this.getRawExpressionChain_Stack().clear();
        return "Clean finished!";
    }

    public Calculator Copy() {
        /*
        Calculator calculator = new Calculator();
        calculator.function_table = this.function_table;
        calculator.variable_table = this.variable_table;
        calculator.Generation=this.Generation+1;
        return calculator;*/
        return this;
    }

    public void RegisterReflectionFunction(String expression, ReflectionFunction.OnReflectionFunction onReflectionFunction) throws Exception {
        ReflectionFunction reflectionFunction = new ReflectionFunction(expression, onReflectionFunction);
        RegisterFunction(reflectionFunction);
    }

    public static void RegisterRawFunction(String expression, ReflectionFunction.OnReflectionFunction reflectionFunction) {
        try {
            ReflectionFunction function = new ReflectionFunction(expression, reflectionFunction);
            raw_function_table.put(function.GetName(), function);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void RegisterRawVariable(Variable variable)throws Exception{
        raw_variable_table.put(variable.GetName(),variable);
    }


    public boolean ContainFunction(String name) {
        if (raw_function_table.containsKey(name))
            return true;
        if (function_table.containsKey(name))
            return true;
        return false;
    }

    enum InfoType {
        RAW_FUNCTION_LIST,
        CUSTOM_FUNCTION_LIST,
        VARIABLE_LIST
    }

    private String DumpInfo(String name) {
        switch (name) {
            case "-rf":
            case "raw_function":
                return DumpInfo(InfoType.RAW_FUNCTION_LIST);
            case "-cf":
            case "custom_function":
                return DumpInfo(InfoType.CUSTOM_FUNCTION_LIST);
            case "-var":
            case "variable":
                return DumpInfo(InfoType.VARIABLE_LIST);
            case "-all":
            case "all": {
                StringBuffer stringBuffer = new StringBuffer();
                for (InfoType type : InfoType.values()) {
                    stringBuffer.append(DumpInfo(type)).append("\n");
                }
                return stringBuffer.toString();
            }
        }
        return "unknown type";
    }

    public String DumpInfo(InfoType type) {
        StringBuffer stringBuffer = new StringBuffer();
        switch (type) {
            case RAW_FUNCTION_LIST: {
                for (Map.Entry<String, ReflectionFunction> functionEntry : raw_function_table.entrySet())
                    stringBuffer.append(functionEntry.getValue().toString()).append("\n");
                break;
            }
            case CUSTOM_FUNCTION_LIST: {
                for (Map.Entry<String, Function> functionEntry : function_table.entrySet())
                    stringBuffer.append(functionEntry.getValue().toString()).append("\n");
                break;
            }
            case VARIABLE_LIST: {
                for (Map.Entry<String, Variable> functionEntry : variable_table.entrySet())
                    stringBuffer.append(functionEntry.getValue().toString()).append("\n");
                break;
            }
        }
        return stringBuffer.toString();
    }

    enum SaveType {
        Custom_Function,
        Variable,
        All
    }

    private String Save(String type, String output_path) throws Exception {
        String buffer = null;
        switch (type) {
            case "custom_function":
                buffer = Save(SaveType.Custom_Function);
                break;
            case "variable":
                buffer = Save(SaveType.Variable);
                break;
            case "all":
                buffer = Save(SaveType.All);
                break;
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(output_path));
        writer.write(buffer);
        writer.flush();
        writer.close();
        return "Save Successfully!";
    }

    public String Save(SaveType type) {
        StringBuffer stringBuffer = new StringBuffer();
        switch (type) {
            case Custom_Function: {
                stringBuffer.append("[custom functinon]").append("\n");
                for (Map.Entry<String, Function> entry : function_table.entrySet()) {
                    if(entry.getValue().getFunction_type()!= Function.FunctionType.Reflection_Function)
                        stringBuffer.append(entry.getValue().Serialize()).append("\n");
                }
                break;
            }
            case Variable: {
                stringBuffer.append("[variable]").append("\n");
                for (Map.Entry<String, Variable> entry : variable_table.entrySet()) {
                    stringBuffer.append(entry.getValue().Serialize()).append("\n");
                }
                break;
            }
            case All: {
                stringBuffer.append("[custom functinon]").append("\n");
                for (Map.Entry<String, Function> entry : function_table.entrySet()) {
                    if(entry.getValue().getFunction_type()!= Function.FunctionType.Reflection_Function)//暂a时未支持反射函数的序列化保存(预计先实现动态编译和反射文本)//TODO
                        stringBuffer.append(entry.getValue().Serialize()).append("\n");
                }
                stringBuffer.append("[variable]").append("\n");
                for (Map.Entry<String, Variable> entry : variable_table.entrySet()) {
                    stringBuffer.append(entry.getValue().Serialize()).append("\n");
                }
                break;
            }
        }
        return stringBuffer.toString();
    }

    private String Load(String input_path)throws Exception{
        Reader reader=new InputStreamReader(new FileInputStream(input_path));
        if(!reader.ready())
            throw new Exception("Cannot load from file :"+input_path);
        int c=0;
        StringBuffer stringBuffer=new StringBuffer();
        while((c=reader.read())>=0){
            stringBuffer.append(((char)c));
        }
        ParseLoadText(stringBuffer.toString());
        return "Load finished!";
    }

    public void ParseLoadText(String rawText) throws IOException {
        StringReader stringReader = new StringReader(rawText);
        String text = "";
        int c = 0;
        while (true) {
            c = stringReader.read();
            if (c < 0)
                break;
            if (c == '\n') {
                DecodeLoadString(text);
                text = new String();
            } else {
                text += (char) c;
            }
        }
        if (!text.isEmpty())
            DecodeLoadString(text);
    }

    private void DecodeLoadString(String text) {
        try {
            switch (text.charAt(0)) {
                case 'f': {
                    Function function=Function.Deserialize(text);
                    function.setCalculator(this);
                    RegisterFunction(function);
                    break;
                }
                case 'v': {
                    Variable variable=Variable.Deserialize(Variable.VariableType.Normal,text);
                    variable.setCalculator(this);
                    RegisterVariable(variable);
                    break;
                }
                case 'e': {
                    Variable variable=Variable.Deserialize(Variable.VariableType.ExpressionVariable,text);
                    variable.setCalculator(this);
                    RegisterVariable(variable);
                    break;
                }
            }
        } catch (Exception e) {
            //nothing to do,just skip this step.
        }
    }

    private String Delete(String type,String name)throws Exception{
        switch (type){
            case "variable":{
                if(variable_table.containsKey(name))
                    variable_table.remove(name);
                break;
            }
            case "function":{
                if(function_table.containsKey(name))
                    function_table.remove(name);
                break;
            }
            default:
                throw new Exception("Cannot recongize type :"+type);
        }
        return "delete successfully";
    }

    private void SetExpressionVariable(String expression)throws Exception{
        if (expression.isEmpty())
            throw new Exception("empty text");
        char c;
        String variable_name = "", variable_expression = "";
        ExpressionVariable variable = null;
        for (int position = 0; position < expression.length() - 1; position++) {
            c = expression.charAt(position);
            if (c == '=') {
                variable_expression = expression.substring(position + 1);
                variable = new ExpressionVariable(variable_name,variable_expression, this);
                RegisterVariable(variable);
                break;
            } else {
                variable_name += c;
            }
        }
    }

}
