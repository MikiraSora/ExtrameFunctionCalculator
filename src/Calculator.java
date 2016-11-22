import com.sun.tracing.dtrace.FunctionAttributes;
import sun.font.Script;

import javax.print.DocFlavor;
import java.io.*;
import java.sql.Ref;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mikir on 2016/8/20.
 * */

    public class Calculator {

    public static abstract class Expression {
        Expression() {
            this(null);
        }

        Expression(Calculator calculator1) {
            setCalculator(calculator1);
        }

        /**
         * 表达式类型
         * */
        public static enum ExpressionType {
            Function,
            Variable,
            Digit,
            Symbol,
            Derivative,
            Unknown
        }

        /**
         * 返回表达式的类型
         * @return 表达式的类型
         * */
        ExpressionType GetType() {
            return ExpressionType.Unknown;
        }

        /**
         * 原文本，不同子类有不同定义
         * */
        String rawText = null;

        Calculator calculator = null;
        /**
         * 是否被Solve()计算出结果
         * @return 如果返回true，则意味着这表达式可以通过Solve()计算得到结果;<br>
         *     如果返回false，则意味着这表达式并不能直接通过Solve()计算得到结果*/
        boolean isCalculatable(){return false;}

        /**
         * 绑定计算器到表达式对象上,可以通过GetCalculator()来获取此计算器对象
         * @param calculator 被绑定的计算器对象
         * */
        void setCalculator(Calculator calculator) {
            this.calculator = calculator;
        }

        /**
         * 获取自身绑定的计算器以便于查找内容，若没有则自行new一个计算器器对象并返回<br></>
         * @return 自己的绑定的计算器*/
        Calculator getCalculator() {
            return calculator==null?(this.calculator=new Calculator()):calculator;
        }

        /**
         * 此方法必须被重载，根据自己不同定义表达式做出不同计算实现，但返回内容可以是被计算器计算的表达式
         * @return 可被计算的表达式*/
        String Solve() throws Exception
        {
            return rawText;
        }

        /**
         * 获取表达式的名称
         * @return 表达式的名称*/
        String GetName() {
            return null;
        }

        //static Expression Deserialize(String text)throws Exception{return null;}

        /**
         * 表达式可以通过此方法控制序列化的内容，但反序列化也必须自己定义实现
         * @return 序列化后的内容**/
        String Serialize(){
            return null;
        }
    }

    public static class ReflectionFunction extends Function {
        private static Pattern FunctionFormatRegex = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
        ReflectionFunction(String expression, OnReflectionFunction onReflectionFunction) throws Exception {
            super();
            if (expression == null)
                return;
            setReflectionFunction(onReflectionFunction);
            rawText = expression;
            //Pattern reg = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            Matcher result = FunctionFormatRegex.matcher(expression);
            result.find();
            if (result.groupCount() != 2)
                Log.ExceptionError( new Exception("Cannot parse function ：" + expression));
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

            public String onHelp(){return null;}

            //返回null意味此函数不可求导
            public String onDerivativeParse(){
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
                Log.Warning(e.getMessage());
            }
            return null;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", function_name, function_paramters);
        }

        @Override
        protected String Solve(String parameterList) throws Exception {
            HashMap<String,Variable> custom_paramter=reflectionFunction.onParseParamter(parameterList,request,getCalculator());

            if(custom_paramter==null)/*返回null意味着按照国际惯例来解析传入参数*/
                Parse(parameterList);
            else
                paramter=custom_paramter;
            return reflectionFunction.onReflectionFunction(paramter, getCalculator());
        }
    }


    public static class ScriptFunction extends Function{
        String function_name=null;
        Executor executor=null;
        //Parser.Statement.Function reference_function=null;

        public ScriptFunction(String function_name,Executor executor,Calculator calculator){
            function_type=FunctionType.Script_Function;
            this.function_name=function_name;
            this.executor=executor;
            request=executor.parser.FunctionTable.get(function_name).request;
            setCalculator(calculator);
        }

        @Override
        protected String Solve(String parameterList) throws Exception {
            Parse(parameterList);
            //// TODO: 2016/11/2 将ArrayList转化成Hashmap
            ArrayList<Calculator.Expression> arrayList=new ArrayList<>();
            for(HashMap.Entry<String,Variable> pair:paramter.entrySet())
                arrayList.add(pair.getValue());
                return executor.ExecuteFunction(function_name,arrayList);
        }

        @Override
        String Solve() throws Exception {
            return super.Solve();
        }


    }


    public static class Function extends Expression {

        enum FunctionType {
            Normal_Function,
            Reflection_Function,
            Script_Function,
            Unknown
        }

        /**
         * 静态解析限定，间接变量
         * */
        public class RequestVariable extends Variable{
            Function function=null;
            RequestVariable(String name,Calculator calculator1,Function function){super(name,null,calculator1);this.function=function;}
            @Override
            String Solve() throws Exception{
                if(function!=null){
                    if(function.paramter.containsKey(Variable_name))
                        return function.paramter.get(Variable_name).Solve();
                }
                try {
                    return this.getCalculator().GetVariable(Variable_name).Solve();
                }catch (Exception e){
                    Log.Warning(String.format("cant get value of request variable cause :%s ,so return 0",e.getMessage()));
                    return "0";
                }
            }
        }

        /**
         * 静态解析限定，间接函数
         * */
        static class RequestFunction extends Function{
            Function function=null;
            RequestFunction(String name,Calculator calculator1,Function function)throws Exception{super(null,calculator1);function_name=name;this.function=function;}

            @Override
            String Solve() {
                String result=null;
                try {
                    this.getCalculator().PushTmpVariable(function.paramter);
                    result= this.getCalculator().GetFunction(function_name).Solve(current_paramters);
                    this.getCalculator().PopTmpVariable();
                    return result;
                }catch (Exception e){
                    Log.Warning(String.format("cant get value of request function cause :%s ,so return 0 as value result",e.getMessage()));
                    return "0";
                }
            }
        }

        FunctionType function_type = FunctionType.Unknown;

        ArrayList<Expression> staticBSEList=null;

        /**
         * 返回函数的类型
         * @return Normal_Function 返回普通函数的字段
         * Reflection_Function 返回反射函数的字段
         * */
        public FunctionType getFunction_type() {
            return function_type;
        }

        /**
         * 函数名
         * */
        protected String function_name;
        /**
         * 函数参数
         * */
        protected String function_paramters;
        /**
         * 普通函数的表达式
         * */
        protected String function_body;

        /**
         * 当前参数,默认无参
         * */
        protected String current_paramters = "";

        protected Function() {
            super();
        }

        /**
         * 解析普通函数的正则表达式
         * */
        private static Pattern FunctionFormatRegex = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)=(.+)");

        public Function(String expression, Calculator calculator1) throws Exception {
            super(calculator1);
            if (expression == null)
                return;
            rawText = expression;
            Matcher result = FunctionFormatRegex.matcher(expression);
            result.find();
            if (result.groupCount() != 3)
                Log.ExceptionError( new Exception("Cannot parse function ：" + expression));
            function_name = result.group(1);
            function_paramters = result.group(2);
            ParameterRequest parameterRequest = new ParameterRequest(function_paramters);
            request = parameterRequest;
            function_body = result.group(3);
            function_type = FunctionType.Normal_Function;
        }

        /**
         * 解析函数表达式，并将结果再以转换成后缀表达式并保存
         * */
        private void StaticParseExpression()throws Exception{
            ArrayList<Expression> tmpBSEArrayList=ParseExpression(function_body);
            staticBSEList=getCalculator().ConverToBSE(tmpBSEArrayList);
        }

        /**
         * 将函数表达式解析成可以分析的表达式组
         * @param expression 函数表达式，通常是function_body字段传入
         *
         * @return 表达式组
         * */
        ArrayList<Expression> ParseExpression(String expression) throws Exception {
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
                                    expressionArrayList.add(new ExpressionVariable("",RepeatingDecimalCoverToExpression(statement),this.getCalculator()));
                                    statement="";
                                    break;
                                }
                            }
                            else if (c == ')') {
                                if (bracket_stack.isEmpty())
                                    Log.ExceptionError( new Exception("Extra brackets in position: " + position));
                                bracket_stack.pop();
                                if (bracket_stack.isEmpty()) {
                                    statement += ")";
                                    expressionArrayList.add(checkConverExpression(statement));//should always return Function
                                    break;
                                }
                                statement += c;
                            }else{
                                statement += c;
                            }
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
                                /*判断是否存在双字符组合的操作符,兼容逻辑操作符，如>= == !=*/
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

        /**
         * 分割文本的正则表达式
         * */
        private static Pattern checkFunctionFormatRegex=Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");

        /**
         * 分析文本，并转换成相对应的表达式元素对象
         * @param text 传入文本
         * @return 解析后的表达式对象
         * */
        private Expression checkConverExpression(String text) throws Exception {
            if (isFunction(text)) {
                //Get function name
                Matcher result = checkFunctionFormatRegex.matcher(text);
                result.find();
                if (result.groupCount() != 2)
                    Log.ExceptionError( new Exception("Cannot parse function ：" + text));
                String function_name = result.group(1);
                String function_paramters = result.group(2);
                if (!this.getCalculator().ContainFunction(function_name))
                    Log.ExceptionError( new Exception(String.format("function %s hadnt declared!", function_name)));
                Function function = new RequestFunction(function_name,getCalculator(),this);
                function.current_paramters = function_paramters;
                return function;
            }

            if (isDigit(text)) {
                return new Digit(text);
            }

            if (isValidVariable(text)) {
                return new RequestVariable(text,this.getCalculator(),this);
            }
            return null;
        }

        /**
         * 函数参数解析类,函数声明时便创建，以便调用时辅助参数传入
         * */
        static class ParameterRequest {
            /**
             * 参数列表
             * */
            private ArrayList<String> requestion_list = new ArrayList<String>();

            private ParameterRequest() {
            }

            /**
             * 初始化参数列表
             * @param rawText 函数声明时所传入参数文本<br>
             *                如f(x,y,z)=x+y+z，rawText便是"x,y,z"
             * */
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

            /**
             * 获取调用函数所需要传入参数的个数
             * @return 参数数量
             * */
            public int GetParamterRequestCount(){return requestion_list.size();}

            /**
             * 获取调用函数所需要传入参数名的集合
             * @return 参数名的集合
             * */
            public String[] GetParamterNameArray(){
                String[] array=new String[GetParamterRequestCount()];
                requestion_list.toArray(array);
                return array;
            }

            /**
             * 获取第index个参数的名字
             * @return 参数名
             * */
            public String GetParamterName(int index){return requestion_list.get(index);}
        }

        /**
         * 函数调用解析后的参数列表
         * */
        protected HashMap<String, Variable> paramter = new HashMap<>();

        /**参数列表*/
        protected ParameterRequest request;

        /**
         * 解析函数调用时传入的参数文本
         * @return 无，但解析的结果放到paramter对象中
         * */
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
                        BracketStack.pop();
                    else
                        Log.ExceptionError( new Exception("Not found a pair of bracket what defining a expression"));
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

        /**
         * 参数替换到函数表达文本上
         * @param expression 函数表达式文本，通常由function_body传入
         * @return 替换后的表达式文本
         * */
        protected String ParseDeclaring(String expression) throws Exception, StatementNotDeclaredException {
            String newExpression = expression,replaceParam;
            for (Map.Entry<String, Variable> pair : paramter.entrySet()) {
                //replaceParam="("+pair.getValue().Solve()+")";
                newExpression = newExpression.replaceAll(pair.getKey(),"("+pair.getValue().Solve()+")");
            }
            return newExpression;
        }


        @Override
        public String toString() {
            return String.format("%s(%s)=%s", function_name, function_paramters, function_body);
        }

        /**
         * 函数调用计算
         * @return 计算结果
         * */
        @Override
        String Solve() throws Exception{
                return Solve(current_paramters);
        }

        boolean specialAbleStaticParseFunction=true;
        protected String Solve(String parameterList) throws Exception {
            Parse(parameterList);
            if(paramter.size()!=request.GetParamterRequestCount())
                Log.ExceptionError(new Exception(String.format("function \"%s\" requests %d paramter(s) but you input %d paramter(s)",function_name,request.GetParamterRequestCount(),paramter.size())));
            String result;
            if(specialAbleStaticParseFunction&&this.getCalculator().ableStaticParseFunction){
                if(staticBSEList==null)
                    StaticParseExpression();
                result= Solve(staticBSEList);
                paramter.clear();
                return result;
            }else{
                staticBSEList=null;
            }
            String exression;
            exression = ParseDeclaring(function_body);
            paramter.clear();
            return getCalculator().Solve(exression);
        }

        /**函数调用计算(静态解析限定)*/
        private String Solve(ArrayList<Expression> expressionArrayList)throws Exception{
            if(!this.getCalculator().ableStaticParseFunction)
                Log.ExceptionError( new Exception("cant not solve with static parsing"));
            if (expressionArrayList.size() == 1)
                if (expressionArrayList.get(0).GetType() == Expression.ExpressionType.Digit||expressionArrayList.get(0).GetType()==ExpressionType.Variable)
                    return String.valueOf((((Digit) expressionArrayList.get(0)).GetDouble()));
            Stack<Expression> digit_stack = new Stack<>();
            Expression digit_a, digit_b, digit_result;
            Symbol operator;
            ArrayList<Expression> paramterList,result;
            try {
                for (Expression node : expressionArrayList) {
                    if (node.GetType() == Expression.ExpressionType.Symbol) {
                        operator = (Symbol) node;
                        paramterList=new ArrayList<>();
                        for(int i=0;i<operator.GetParamterCount();i++)
                            paramterList.add(digit_stack.isEmpty() ? new Digit("0") : digit_stack.pop());
                        Collections.reverse(paramterList);
                        result=operator.Solve(paramterList,this.getCalculator());
                        for(Expression expr:result)
                            digit_stack.push(expr);
                    } else {
                        if (node.GetType() == Expression.ExpressionType.Digit||node.GetType()==ExpressionType.Function||node.GetType()==ExpressionType.Variable) {
                            digit_stack.push(new Digit(node.Solve()));
                        } else
                            Log.ExceptionError( new Exception("Unknown Node"));
                    }
                }
            } catch (Exception e) {
                Log.ExceptionError( new Exception(e.getMessage()));

            }
            Expression resultExpr=digit_stack.pop();
            return (resultExpr.GetType()== Expression.ExpressionType.Digit)?Double.toString(((Digit)resultExpr).GetDouble()):resultExpr.Solve();
        }

        /**获取函数计算结果的数值对象
         * @return 计算结果后的数值对象
         * */
        Digit GetSolveToDigit()throws Exception{
            return new Digit(Solve());
        }

        /**获取函数名称
         * @return 函数名*/
        @Override
        String GetName() {
            return function_name;
        }

        /**
         * 获取表达式类型字段
         * @return 函数字段*/
        @Override
        ExpressionType GetType() {
            return ExpressionType.Function;
        }

        /**
         * 反序列化正则表达式
         * */
        private static Pattern FunctionDeserializeRegex=Pattern.compile("(\\w)##([a-zA-Z_]+\\w*)##(.*?)##(.*)");

        /**
         * 反序列化字符串转换成函数
         * @param text 文本内容
         * @return 反序列化后的新的函数对象
         * */
        static Function Deserialize(String text)throws Exception{
            Matcher result=FunctionDeserializeRegex.matcher(text);
            if(!result.find())
                Log.ExceptionError( new Exception("Cannot parse text :"+text));
            String rawText=String.format("%s(%s)=%s",result.group(2),result.group(3),result.group(4));
            Function function=new Function(rawText,null);
            return function;
        }

        /**
         * 将当前对象序列化并输出
         * @return 序列化后的文本,可被Deserialize()反序列化
         * */
        @Override
        String Serialize() {
            return String.format("f##%s##%s##%s",function_name,function_paramters,function_body);
        }
    }

    public static class Symbol extends Expression {

        /**
         * 比较符号和当前符号优先值
         * @param symbol 比较的符号
         * @return 返回0意味着优先级相等<br>
         * 返回1 意味着当前表达式比要比较的符号有更高优先<br>
         * 返回-1 意味着要比较的符号比当前符号有更高的优先
         * */
        public int CompareOperationPrioty(Symbol symbol) {
            float val = OperatorPrioty.get(this.rawText) - OperatorPrioty.get(symbol.rawText);
            return val == 0 ? 0 : (val > 0 ? 1 : -1);
        }

        Symbol(String op) {
            rawText = op;
        }

        /*
        @Override
        String Solve() {
            return super.Solve();
        }
*/

        @Override
        public String toString() {
            return rawText;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Symbol;
        }

        /**
         * 操作符计算映射表
         * */
        static HashMap<String,OperatorFunction> OperatorFunction=new HashMap<>();

        /**
         * 操作符优先级查询表
         * */
        static HashMap<String,Float> OperatorPrioty=new HashMap<>();

        /**
         * 操作符对象缓存共享
         * */
        static HashMap<String, Symbol> SharedSymbolCache=new HashMap<>();
        
        /**
         * 获取已缓存可共享的操作符
         * */
        static Symbol GetSymbolMayFromCache(String op){return SharedSymbolCache.containsKey(op)?SharedSymbolCache.get(op):new Symbol(op);}

        /**
         * 操作符参数查询表
         * */
        static HashMap<String,Integer> OperatorRequestParamterCount=new HashMap<>();

        static abstract class OperatorFunction{
            ArrayList<Expression> onCalculate(ArrayList<Expression> paramterList,Calculator calculator) throws Exception{return null;}
        }

        /**
         * 注册记录操作符映射
         * @param operatorSymbol 操作符符号
         * @param requestParamterSize 操作符参数数量
         * @param operatorPrioty 操作符优先级<br>
         *                          加和减优先级为6<br>    乘除以取余优先级为9<br>  乘方优先级为12<br>    左右括号有限度为99<br>
         * */
        public static void RegisterOperation(String operatorSymbol,int requestParamterSize,float operatorPrioty,OperatorFunction operatorFunction) {
            OperatorFunction.put(operatorSymbol,operatorFunction);
            OperatorPrioty.put(operatorSymbol,operatorPrioty);
            OperatorRequestParamterCount.put(operatorSymbol,requestParamterSize);
            SharedSymbolCache.put(operatorSymbol,Symbol.GetSymbolMayFromCache(operatorSymbol));
        }

        /**
         * 操作符计算表达式
         * @param paramterList 参数列表
         * @param calculator 当前计算器对象
         * @return 返回结果表达式，通常只有一个Digit类
         * */
        ArrayList<Expression> Solve(ArrayList<Expression> paramterList,Calculator calculator)throws Exception{
            return OperatorFunction.get(rawText).onCalculate(paramterList, calculator);
        }

        /**
         * 获取操作符所需数值数量
         * */
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

        /**变量类型*/
        VariableType variable_type=VariableType.Unknown;

        /**
         * 变量名
         * */
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

        /**
         * 是否可以直设置变量的值为数值
         * @return 返回true 可以直接设置数值<br>
         *     返回false 不可以直接设置数值<br>
         * */
        boolean ableSetVariableDirectly(){
            return true;
        }

        /**
         * 返回变量的值
         * @return 变量的值
         * */
        @Override
        String Solve()throws Exception {
            return super.Solve();
        }

        @Override
        public String toString() {
            String rightValue;
            try {
                rightValue=Solve();
            }catch (Exception e){
                //Log.Debug(e.getMessage());
                rightValue="null";
            }
            return String.format("%s=%s", GetName(),rightValue);
        }

        /**
         * 获取变量的值并以Digit类对象返回
         * @return 变量的值(Ditgit)
         * */
        public Digit GetDigit() throws Exception {
            return new Digit(rawText == null ? getCalculator().RequestVariable(this.GetName()) : rawText);
        }

        /**
         * 获取变量的名称
         * @return 变量名
         * */
        @Override
        String GetName() {
            return Variable_name;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Variable;
        }

        /**
         * 文本反序列化正则表达式
         * */
        private static Pattern VariableDeserializeRegex=Pattern.compile("(\\w)##([a-zA-Z_]+\\w*)##(.*)");

        /**
         * 反序列化文本至变量并返回
         * @return 反序列化后的新建变量对象
         * */
        static Variable Deserialize(VariableType type,String text)throws Exception{
            Matcher result=VariableDeserializeRegex.matcher(text);
            if(!result.find())
                Log.ExceptionError( new Exception("Cannot parse text :"+text));
            //String rawText=String.format("%s=%s",result.group(2),result.group(3));
            switch (type){
                case Normal:return new Variable(result.group(2),result.group(3),null);
                case ExpressionVariable:return new ExpressionVariable(result.group(2),result.group(3),null);
                case BooleanVariable:return new BooleanCaculator.BooleanVariable(result.group(2),result.group(3),null);

            }
            return null;
        }

        /**
         * 将当前变量对象序列化成文本并返回
         * @return 变量序列化后的文本
         * */
        @Override
        String Serialize() {
            return String.format("v##%s##%s",Variable_name,rawText);
        }
    }

    public static class Digit extends Expression {

        static boolean IsPrecisionTruncation=false;

        public Digit(String value) {
            rawText = value;
        }

        /**返回数值*/
        @Override
        String Solve() {
            return rawText;
        }

        @Override
        ExpressionType GetType() {
            return ExpressionType.Digit;
        }

        /**
         * 获取以浮点形式的数值，注意:只有15位精确度
         * @return 浮点数值
         * */
        public double GetDouble() {
            return IsPrecisionTruncation?CutMaxPerseicelDecimal(Solve()):Double.valueOf(Solve());
        }

        /**
         * 判断并截取至15位精度
         * @param decimal 浮点数值字符串
         * @return 处理后的浮点数值
         * */
        private double CutMaxPerseicelDecimal(String decimal){
            //15
            if(!decimal.contains("."))
                return Double.parseDouble(decimal);
            int pointPos=decimal.lastIndexOf(".");
            if(decimal.length()-pointPos>=15)
                return Double.parseDouble(decimal.substring(0,pointPos+14));
            return Double.parseDouble(decimal);
        }

        /**
         * 获取以浮点形式的数值
         * @return 整数数值
         * */
        public int GetInteger() {
            return Integer.valueOf(Solve());
        }

        /**
         *
         * */
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

        /**
         * 计算表达式
         * @return 表达式计算的结果
         * */
        @Override
        String Solve(){
            try {
                return getCalculator().Solve(rawText);
            }catch (Exception e){
                e.printStackTrace();
                Log.Warning(e.getMessage());
            }
            return null;
        }

        @Override
        boolean isCalculatable() {
            return true;
        }

        /**
         * 计算表达式，并以Digit类对象返回
         * */
        @Override
        public Digit GetDigit() throws Exception {
            return new Digit(Solve());
        }

        /**
         * 反序列化表达式变量正则表达式
         * */
        private static Pattern ExpressionVariableRegex=Pattern.compile("(\\w)##([a-zA-Z_]+\\w*)##(.*)");
        static ExpressionVariable Deserialize(String text)throws Exception{
            Matcher result=ExpressionVariableRegex.matcher(text);
            if(!result.find())
                Log.ExceptionError( new Exception("Cannot parse text :"+text));
            return new ExpressionVariable(result.group(2),result.group(3),null);
        }

        /**
         * 将当前表达式变量对象序列化成文本
         * @return 序列化后的文本
         * */
        @Override
        String Serialize() {
            return String.format("e##%s##%s",Variable_name,rawText);
        }

        @Override
        public String toString() {
            return String.format("<expr> %s=%s",Variable_name,rawText);
        }

        /**
         * 获取表达式文本
         * @return 表达式文本
         * */
        public String GetExpreesion(){return rawText;}
    }

    /**
     * 已经声明定义的函数列表
     * */
    private HashMap<String, Function> function_table = new HashMap<>();

    /**
     * 已经声明定义的变量列表
     * */
    private HashMap<String, Variable> variable_table = new HashMap<>();

    /**
     * 内置变量列表
     * */
    private static HashMap<String, ReflectionFunction> raw_function_table = new HashMap<>();

    /**
     * 内置函数列表
     * */
    private static HashMap<String, Variable> raw_variable_table = new HashMap<>();

    /**
     * 函数静态解析开关
     **/
    boolean ableStaticParseFunction=true;

    static {
        Log.SetPort(2857);
        Log.SetAddress("127.0.0.1");
        Log.InitRecordTime();
        Log.SetIsThreadCommitLog(true);
        //Log.Debug("Log Init");
        CalculatorHelper.InitOperatorDeclare();
        CalculatorHelper.InitFunctionDeclare();
    }

    public Calculator(){
        calculatorOptimizer=new CalculatorOptimizer(this);
    }

    enum EnableType{
        FunctionStaticParse,
        ExpressionOptimize,
        PrecisionTruncation,
        ScriptFunctionCache
        }

    /**
     * 开启功能，这些功能均为可选的,可以通过DisEnable()关闭
     * @param enableType 功能类型
     * */
    public void Enable(EnableType enableType){
        switch (enableType){
            case FunctionStaticParse:
                FunctionStaticEnable(true);
                break;
            case ExpressionOptimize:
                OptimizeEnable(true);
                break;
            case PrecisionTruncation:
                Digit.IsPrecisionTruncation=true;
                break;
            case ScriptFunctionCache:
                GetScriptManager().SetCacheReferenceFunction(true);
                break;
        }
        Log.Debug(String.format("开启功能:%s",enableType.toString()));
    }

    /**
     *关闭功能，这些功能均为可选的,可以通过Enable()开启
     * @param enableType 功能类型
     * */
    public void DisEnable(EnableType enableType){
        switch (enableType){
            case FunctionStaticParse:
                FunctionStaticEnable(false);
                break;
            case ExpressionOptimize:
                OptimizeEnable(false);
                break;
            case PrecisionTruncation:
                Digit.IsPrecisionTruncation=false;
                break;
            case ScriptFunctionCache:
                GetScriptManager().SetCacheReferenceFunction(false);
                break;
        }
        Log.Debug(String.format("关闭功能:%s",enableType.toString()));
    }

    /**
     * 获取指定名称的函数，会在内置函数列表和定义函数中搜寻
     * @param name 函数名
     * @return 函数对象
     * */
    Function GetFunction(String name) throws Exception, FunctionNotFoundException {
        if (raw_function_table.containsKey(name)) {
            Function function = raw_function_table.get(name);
            function.setCalculator(this);
            return function;
        }
        if (function_table.containsKey(name))
            return function_table.get(name);
        return GetScriptManager().RequestFunction(name);
        //return function_table.get(name);
    }

    /**
     * 获取指定名称的变量，会在内置变量列表和定义变量中搜寻
     * @param name 变量名
     * @return 变量对象
     * */
    Variable GetVariable(String name) throws Exception {
        Variable tmp_variable=GetTmpVariable(name);
        if(tmp_variable!=null)
            return tmp_variable;
        if(raw_variable_table.containsKey(name)){
            return raw_variable_table.get(name);
        }
        if (variable_table.containsKey(name))
            return variable_table.get(name);
        tmp_variable=GetScriptManager().RequestVariable(name,null);
        if(tmp_variable!=null)
            return tmp_variable;
        Log.ExceptionError( new VariableNotFoundException(name));
        return null;
    }

    /**
     * 声明函数并加入函数列表
     * @param function 要登记加入的函数对象
     * */
    private void RegisterFunction(Function function) {
        Log.Debug(function.toString());
        function_table.put(function.GetName(), function);
    }

    /**
     * 声明函数并加入变量列表
     * @param variable 要登记加入的变量对象
     * */
    private void RegisterVariable(Variable variable) {
        Log.Debug(variable.toString());
        variable_table.put(variable.GetName(), variable);
    }

    /**
     * 操作符列表
     * */
    private static String specialOperationChar = " + - * / ~ ! @ # $ % ^ & ( ) ; : \" | ? > < , ` ' \\ ";

    /**
     * 获取名为name的变量的值
     * @param name 变量名
     * @return name变量的值
     * */
    private String RequestVariable(String name) throws Exception {
        if (!variable_table.containsKey(name))
            Log.ExceptionError( new VariableNotFoundException(name));
        return variable_table.get(name).Solve();
    }

    /**
     * 解析文本成表达式链
     * @param expression 表达式文本,如"4+x/(abs(sin(x))+6)+1*2"
     * @return 解析好的表达式链表
     * */
    ArrayList<Expression> ParseExpression(String expression) throws Exception {
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
                                expressionArrayList.add(new ExpressionVariable("",RepeatingDecimalCoverToExpression(statement),this));
                                statement="";
                                break;
                            }
                        }
                        else {
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


    //10.4412312312312.. -> (10.44+123/99900)
    /**
     * 将无限小数的文本转换成可计算的表达式文本
     * @param decimalExpr 浮点文本,如"10.4412312312312"
     * @return 可计算的表达式文本，如"(10.44+123/99900)"
     * */
    private static String ExpressionCoverToRepeatingDecimal(String decimalExpr)throws Exception{
        Matcher result=RepeatingDecimalReg.matcher(decimalExpr);
        if(!result.matches())
            Log.ExceptionError( new Exception(decimalExpr+" is invalid repeating decimal!"));
        String intDigit=result.group(1),notRepeatDecimal=result.group(2),RepeatDecimal=result.group(3),endDecimal=result.group(4);
        if(endDecimal.length()>RepeatDecimal.length())
            Log.ExceptionError( new Exception(decimalExpr+" is invalid repeating decimal!"));
        String devNumber="";
        for(int i=0;i<RepeatDecimal.length();i++)
            devNumber+=9;
        for(int i=0;i<notRepeatDecimal.length();i++)
            devNumber+=0;
        String expr=String.format("(%s.%s+%s/%s)",intDigit,notRepeatDecimal,RepeatDecimal,devNumber);
        return expr;
    }

    //0.6(7) -> 0.677777777... -> (0.6+7/90)
    /**
     * 将无限小数的文本转换成可计算的表达式文本
     * @param decimalExpr 浮点文本,如"0.6(7)"
     * @return 可计算的表达式文本，如"(0.6+7/90)"
     * */
    private static String RepeatingDecimalCoverToExpression(String decimalExpr)throws Exception{
        char c=0;
        int pos=0,notRepeatingDecimalLength=0;
        String notRepeating="",Repeating="";
        while(true){
            c=decimalExpr.charAt(pos);
            if(c!='('&&c!=')'){
                if(c!='.'){
                    notRepeating+=c;
                }
                else {
                    notRepeating+=c;
                    pos++;
                    while (true){
                        c=decimalExpr.charAt(pos);
                        if(c=='(')
                            break;
                        notRepeating+=c;
                        notRepeatingDecimalLength++;
                        pos++;
                        if(pos>=decimalExpr.length())
                            Log.ExceptionError( new Exception(decimalExpr+"cant cover to Expression"));
                    }
                    pos--;
                }
            }else{
                if(c=='('){
                    pos++;
                    while (true){
                        c=decimalExpr.charAt(pos);
                        if(c==')')
                            break;
                        Repeating+=c;
                        pos++;
                        if(pos>=decimalExpr.length())
                            Log.ExceptionError( new Exception(decimalExpr+"cant cover to Expression"));
                    }
                    break;
                }
            }
            pos++;
        }
        String devNumber="";
        for(int i=0;i<Repeating.length();i++)
            devNumber+=9;
        for(int i=0;i<notRepeatingDecimalLength;i++)
            devNumber+=0;
        return String.format("(%s+%s/%s)",notRepeating,Repeating,devNumber);
    }

    /**
     * 将表达式链中的函数都进行计算并将结果替换
     * */
    private void ConverFunctionToDigit() throws FunctionNotFoundException, VariableNotFoundException,Exception {
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

    /**
     * 将表达式链中的变量都进行计算并将结果替换
     * */
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
            Log.Warning(e.getMessage());
        }
    }

    /**
     * 函数格式解析正则表达式
     * */
    private static Pattern checkFunctionFormatRegex=Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");

    /**
     * 分析文本，并转换成相对应的表达式元素对象
     * @param text 传入文本
     * @return 解析后的表达式对象
     * */
    private Expression checkConverExpression(String text) throws Exception {
        if (isFunction(text)) {
            //Get function name
            Matcher result = checkFunctionFormatRegex.matcher(text);
            result.find();
            if (result.groupCount() != 2)
                Log.ExceptionError( new Exception("Cannot parse function ：" + text));
            String function_name = result.group(1);
            String function_paramters = result.group(2);
            if (!ContainFunction(function_name))
                Log.ExceptionError( new Exception(String.format("function %s hadnt declared!", function_name)));
            Function function = GetFunction(function_name);
            function.current_paramters = function_paramters;
            return function;
            //Get function paramater list
        }

        if (isDigit(text)) {
            return new Digit(text);
        }

        if (isValidVariable(text)) {
            return GetVariable(text);
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
     * @returns 判断结果
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

    /**
     * 将表达式链转换成后缀表达式并返回
     * @param expressionArrayList 表达式链表,通常由ParseExpression()处理获得
     * @return 后缀表达式形式的表达式链表
     * */
    ArrayList<Expression> ConverToBSE(ArrayList<Expression> expressionArrayList) throws Exception {
        if(expressionArrayList.get(expressionArrayList.size()-1).GetType()== Expression.ExpressionType.Symbol?!((Symbol)expressionArrayList.get(expressionArrayList.size()-1)).rawText.equals(")"):false)
            Log.ExceptionError(new Exception("the last expression in list cannot be symbol"));
        ArrayList<Expression> result_list = new ArrayList<>();
        Stack<Symbol> operation_stack = new Stack<>();
        Symbol symbol = null;
        Expression node=null;
        for (int position=0;position<expressionArrayList.size();position++) {
            node=expressionArrayList.get(position);
            if (node.GetType() == Expression.ExpressionType.Digit||(ableStaticParseFunction?(node.GetType()== Expression.ExpressionType.Function||node.GetType()== Expression.ExpressionType.Variable):false))
                result_list.add(node);
            else {
                if(((Symbol)node).rawText.equals("-")){
                    if(position==0){
                        result_list.add(new Digit("0"));
                    }else if(expressionArrayList.get(position-1).GetType()== Expression.ExpressionType.Symbol)
                        if(((Symbol)expressionArrayList.get(position-1)).rawText.equals("("))
                            result_list.add(new Digit("0"));
                }
                if (operation_stack.isEmpty())
                    operation_stack.push((Symbol) node);
                else {
                    if (!(((Symbol) node).rawText.equals(")"))){
                        symbol = operation_stack.peek();
                        while(symbol!=null){
                            if(!(symbol.rawText .equals("(")))
                                    if(symbol.CompareOperationPrioty((Symbol)node) >= 0){
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
                                Log.ExceptionError( new Exception("喵喵喵?"));
                            if ((symbol.rawText.equals("("))) {
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
        for (int i = 0; i < result_list.size(); i++) {
            node = result_list.get(i);
            if (node.GetType() == Expression.ExpressionType.Symbol)
                if (((Symbol) node).rawText.equals("("))
                    result_list.remove(node);
        }
        return result_list;
    }

    /**
     * 执行后缀表达式链表，并输出结果
     * @return 计算结果
     * */
    private String ExucuteBSE() throws Exception {
        if (getBSEChain_Stack().size() == 1)
            if (getBSEChain_Stack().get(0).GetType() == Expression.ExpressionType.Digit)
                return String.valueOf((((Digit) getBSEChain_Stack().get(0)).GetDouble()));
        Stack<Expression> digit_stack = new Stack<>();
        Expression digit_a, digit_b, digit_result;
        Symbol operator;
        Expression node=null;
        ArrayList<Expression> executeList=getBSEChain_Stack();
        ArrayList<Expression> paramterList,result;
        try {
            for (int position=0;position<executeList.size();position++) {
                node=executeList.get(position);
                if (node.GetType() == Expression.ExpressionType.Symbol) {
                    operator = (Symbol) node;
                    paramterList=new ArrayList<>();
                    for(int i=0;i<operator.GetParamterCount();i++)
                    {
                        if(digit_stack.isEmpty()){
                            if(operator.rawText.equals("-")&&i!=0){
                                paramterList.add(new Digit("0"));
                            }else{
                                Log.ExceptionError(new Exception("expression synatx error!because not enough calculatable type could solve with current operator \""+operator.rawText+"\""));
                            }
                        }else{
                            paramterList.add(digit_stack.pop());
                        }
                    }
                        //paramterList.add(digit_stack.isEmpty() ? (?new Digit("0")): digit_stack.pop());
                    Collections.reverse(paramterList);
                    result=operator.Solve(paramterList,this);
                    for(Expression expr:result)
                        digit_stack.push(expr);
                } else {
                    if (node.GetType() == Expression.ExpressionType.Digit) {
                        digit_stack.push((Digit) node);
                    } else
                        Log.ExceptionError( new Exception("Unknown Node"));
                }
            }
        } catch (Exception e) {
            Log.ExceptionError( new Exception(e.getMessage()));

        }
        Expression resultExpr=digit_stack.pop();
        return (resultExpr.GetType()== Expression.ExpressionType.Digit)?Double.toString(((Digit)resultExpr).GetDouble()):resultExpr.Solve();
    }

    /**
     * 严格判断里面是否只存在可计算的符号Symbol或者常量数值Digit
     * @deprecated
     * */
    private void CheckNormalizeChain() throws Exception {
        for (Expression node : getRawExpressionChain_Stack()) {
            if (node.GetType() != Expression.ExpressionType.Digit && node.GetType() != Expression.ExpressionType.Symbol)
                Log.ExceptionError( new Exception(node.GetName() + " isnt digit or symbol."));
        }
    }

    /**
     * 当前保存的后缀表达式链的栈
     * */
    Stack<ArrayList<Expression>> BSEChain_Stack=new Stack<>();

    /**
     * 当前保存的表达式链的栈
     * */
    Stack<ArrayList<Expression>> rawExpressionChain_Stack=new Stack<>();

    /**
     * 将后缀表达式链表推入栈并当当前计算的后缀表达式链
     * @param list 后缀表达式链表，通常由ConverToBSE()得到
     * */
    private void  setBSEChain_Stack(ArrayList<Expression> list){
        BSEChain_Stack.push(list);
    }

    /**
     * 将表达式链表推入栈并当当前计算的表达式链
     * @param list 表达式链表，通常由ParseExpression()得到
     * */
    private void  setRawExpressionChain_Stack(ArrayList<Expression> list){rawExpressionChain_Stack.push(list);}

    /**
     * 获取当前计算的后缀表达式链表
     * @return 当前后缀表达式链表
     * */
    private ArrayList<Expression> getBSEChain_Stack(){return BSEChain_Stack.empty()?BSEChain_Stack.push(new ArrayList<Expression>()):BSEChain_Stack.peek();}

    /**
     * 获取当前计算的表达式链表
     * @return 当前表达式链表
     * */
    private ArrayList<Expression> getRawExpressionChain_Stack(){return rawExpressionChain_Stack.empty()?rawExpressionChain_Stack.push(new ArrayList<Expression>()):rawExpressionChain_Stack.peek();}

    /**
     * 结束计算，清空不必要的内容
     * */
    private void Term_Solve(){
        BSEChain_Stack.pop();
        rawExpressionChain_Stack.pop();
    }

    /**
     * 计算表达式文本
     *  @param expression 表达式文本,如"4+f(2)-1*a/b"
     *  @return 计算结果,如"5"
     * */
    public String Solve(String expression) throws Exception {
        expression=expression.replaceAll(" ","");
        if(isDigit(expression))
            return expression;
        //rawExpressionChain = ParseExpression(expression);
        setRawExpressionChain_Stack(ParseExpression(expression));
        ConverVariableToDigit();
        ConverFunctionToDigit();
        CheckNormalizeChain();//// TODO: 2016/10/2 此方法存在争议，暂时保留
        ExpressionOptimization();

        setBSEChain_Stack(ConverToBSE(getRawExpressionChain_Stack()));

        String result= ExucuteBSE();

        Term_Solve();
        return result;
    }

    /**计算优化器*/
    private CalculatorOptimizer calculatorOptimizer=null;

    /**
     * 开关优化
     * @param sw 状态开关
     * */
    private void OptimizeEnable(boolean sw){
        if(sw)
            calculatorOptimizer.Enable();
        else
            calculatorOptimizer.DisEnable();
    }

    /**
     * 开关函数静态解析
     * @param sw 解析开关
     * */
    private void FunctionStaticEnable(boolean sw){
        ableStaticParseFunction=sw;
    }

    /**
     * 优化表达式开关
     * @param sw 开关文本，格式"< 开关状态 open|close > < 优化等级 >0 >",例如"true 2""false""true 999"
     * @return 调用结果
     * */
    private String Optimize(String sw)throws Exception{
        String level=sw.substring(sw.indexOf(" ")).replaceAll(" ","");
        sw=sw.substring(0,sw.indexOf(" ")).replaceAll(" ","");
        switch (sw){
            case "true":{
                Enable(EnableType.ExpressionOptimize);
                calculatorOptimizer.SetOptimiizeLevel(Integer.valueOf(level));
                return "Optimized open.";
            }
            case "false":{
                DisEnable(EnableType.ExpressionOptimize);
                return "Optimized close.";
            }
            default:{Log.ExceptionError( new Exception("unknown command"));}
        }
        return "unknown command";
    }

    /**
     * 优化表达式,若优化已经关闭则无变化
     * */
    private void ExpressionOptimization(){
        ArrayList<Expression> optimizeResult=calculatorOptimizer.OptimizeExpression(getRawExpressionChain_Stack());
        if(optimizeResult==null)
            return;
        getRawExpressionChain_Stack().clear();
        getRawExpressionChain_Stack().addAll(optimizeResult);
        return;
    }

    /**
     * 命令执行并返回命令执行结果
     * @param text 命令文本,格式是"< 命令 solve|reg|set|.. >  < 参数 > "
     * @return 执行结果
     * */
    public String Execute(String text) throws Exception {
        Log.Debug(String.format("Try Execute : %s",text));
        Clear();
        if (text.isEmpty())
            Log.ExceptionError( new Exception("empty text to execute"));
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
            case "test": {
                result = DumpInfo(paramter);
                break;
            }
            case "load_script":{
                result=LoadScriptFile(paramter);
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
            case "reg_df":{
                result=new DerivativeParser(this).Solve(paramter,"x");
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
            case "optimize":{
                result=Optimize(paramter);
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
                Log.ExceptionError( new Exception(String.format("unknown command \"%s\"", executeType)));
            }
        }
        return result;
    }

    /**
     * 声明变量
     * @param expression 声明文本，格式"< 变量名 >=< 表达式 >",表达式将会计算成常量数值才赋值给变量
     * */
    private void SetVariable(String expression) throws Exception {
        if (expression.isEmpty())
            Log.ExceptionError( new Exception("empty text"));
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

    /**
     * 声明定义函数
     * @param expression 声明定义文本，格式"< 函数名 >(< 表达式 x,y,z...>)=< 表达式 >"
     * */
    private void SetFunction(String expression) throws Exception {
        if (expression.isEmpty())
            Log.ExceptionError( new Exception("empty text"));
        Function function = new Function(expression, this);
        RegisterFunction(function);
    }

    /**
     * 重置计算器，并清除已经定义的变量和函数
     * @return 执行结果
     * */
    public String Reset() {
        Clear();
        this.variable_table.clear();
        this.function_table.clear();
        return "Reset finished!";
    }

    /**
     * 清除计算器，清空当前表达式链和后缀表达式链
     * @return 执行结果
     * */
    private String Clear() {
        if(!BSEChain_Stack.empty())
            this.getBSEChain_Stack().clear();
        if(!rawExpressionChain_Stack.empty())
            this.getRawExpressionChain_Stack().clear();
        return "Clean finished!";
    }

    /**
     * 声明反射函数并加入函数列表
     * @param expression 函数声明表达文本，如"getMin(a,b,c)","getRandom()"
     * @param onReflectionFunction 反射函数接口
     * */
    void RegisterReflectionFunction(String expression, ReflectionFunction.OnReflectionFunction onReflectionFunction) throws Exception {
        ReflectionFunction reflectionFunction = new ReflectionFunction(expression, onReflectionFunction);
        RegisterFunction(reflectionFunction);
    }

    /**
     * 声明反射函数并加入内置函数列表，全计算器对象将会共有此函数
     * @param expression 函数声明表达文本，如"getMin(a,b,c)","getRandom()"
     * @param reflectionFunction 反射函数接口
     * */
    public static void RegisterRawFunction(String expression, ReflectionFunction.OnReflectionFunction reflectionFunction) {
        try {
            ReflectionFunction function = new ReflectionFunction(expression, reflectionFunction);
            raw_function_table.put(function.GetName(), function);
        } catch (Exception e) {
            Log.Warning(e.getMessage());
        }
    }

    /**
     * 声明一个变量为内置变量，全计算器对象将会共有此变量
     * @param variable 要内置的变量
     * */
    public static void RegisterRawVariable(Variable variable)throws Exception{
        raw_variable_table.put(variable.GetName(),variable);
    }


    /**
     * 判断是否存在函数
     * @param name 函数名
     * @return 返回true，则存在此函数;否则不存在此函数。
     * */
    public boolean ContainFunction(String name) {
        if (raw_function_table.containsKey(name))
            return true;
        if (function_table.containsKey(name))
            return true;
        if(GetScriptManager().ContainFunction(name))
            return true;
        return false;
    }

    enum InfoType {
        RAW_FUNCTION_LIST,
        CUSTOM_FUNCTION_LIST,
        VARIABLE_LIST
    }

    /**
     * 返回(打印)相关信息
     * @param name ；打印类型
     * @return 相关信息内容
     * */
    public String DumpInfo(String name) {
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

    /**
     * 返回(打印)相关信息
     * @param type 要打印的类型
     * @return 传回结果
     * */
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

    /**
     * 保存类型数据
     * @param type 保存类型
     * @param output_path 保存地址
     * @return 处理结果
     * */
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

    /**
     * 根据类型格式化序列化
     * @param type 保存类型
     * @return 序列化后文本，可被Load()反序列化加载
     * */
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

    /**
     * 加载文本转换对象
     * @param input_path 加载地址
     * @return 处理结果
     * */
    public String Load(String input_path)throws Exception{
        Reader reader=new InputStreamReader(new FileInputStream(input_path));
        if(!reader.ready())
            Log.ExceptionError( new Exception("Cannot load from file :"+input_path));
        int c=0;
        StringBuffer stringBuffer=new StringBuffer();
        while((c=reader.read())>=0){
            stringBuffer.append(((char)c));
        }
        ParseLoadText(stringBuffer.toString());
        return "Load finished!";
    }

    /**
     *无限循环小数判断正则表达式
     * */
    private static Pattern RepeatingDecimalReg=Pattern.compile("^(\\p{Nd}*)\\.(\\p{Nd}*?)(\\p{Nd}+?)(\\3)+(\\p{Nd}*)$");

    /**
     * 判断数字是否为无限循环小数
     * @param Decimal 数字,如"0.1521515151515"
     * @return 返回true则为无限循环小数，否则不是，
     * */
    public boolean isRepeatingDecimal(String Decimal){
        Matcher result=RepeatingDecimalReg.matcher(Decimal);
        if(!result.matches())
            return false;
        String /*intDigit=result.group(1),notRepeatDecimal=result.group(2),*/RepeatDecimal=result.group(3),endDecimal=result.group(4);
        if(endDecimal.length()>RepeatDecimal.length())
            return false;
        return true;
    }

    /**
     *将文本分行解析并反序列化
     * @param rawText 传入文本，通常读取某个文件或者Load()传入
     */
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

    /**
     * 解析文本并反序列化
     * @param text 传入文本,例如"v##myvar##6"会被当做变量并反序列化成变量
     * */
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

    /**
     * 删除某个变量或者函数
     * @param type 指定类型
     * @param name 变量名或者函数名
     * @return 处理结果
     * */
    public String Delete(String type,String name)throws Exception{
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
                Log.ExceptionError( new Exception("Cannot recongize type :"+type));
        }
        return "delete successfully";
    }

    /**
     * 声明一个表达式变量并加入变量列表
     * @param expression 表达式文本，如"myexpr_var=a+b-rand()",此类变量只有在参与运算时候才计算自身表达式。
     * */
    public void SetExpressionVariable(String expression)throws Exception{
        if (expression.isEmpty())
            Log.ExceptionError( new Exception("empty text"));
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

    /*临时变量*/

    Stack<ArrayList<String>> recordTmpVariable=new Stack<>();
    HashMap<String,Stack<Variable>> TmpVariable=new HashMap<>();


    private void PushTmpVariable(HashMap<String,Variable> variableHashMap){
        ArrayList<String> recordList=new ArrayList<>();
        for(HashMap.Entry<String,Variable> pair:variableHashMap.entrySet())
        {
            recordList.add(pair.getKey());
            if(!TmpVariable.containsKey(pair.getKey()))
                TmpVariable.put(pair.getKey(),new Stack<>());
            TmpVariable.get(pair.getKey()).push(pair.getValue());
            //Log.Debug(String.format("tmp variable \"%s\" was push",pair.getValue().toString()));
        }
        recordTmpVariable.push(recordList);
        Log.Debug(String.format("there are %d tmp variables are pushed in %d layout",recordList.size(),recordTmpVariable.size()));
    }

    private void PopTmpVariable()throws Exception{
        ArrayList<String> recordList=recordTmpVariable.pop();
        for(String tmp_name:recordList){
            TmpVariable.get(tmp_name).pop();
            //Log.Debug(String.format("tmp variable \"%s\" was pop",TmpVariable.get(tmp_name).pop().toString()));
            if(TmpVariable.get(tmp_name).empty())
                TmpVariable.remove(tmp_name);
        }
        Log.Debug(String.format("there are %d tmp variables are popped in %d layout",recordList.size(),recordTmpVariable.size()+1));
    }

    private Variable GetTmpVariable(String name){
        if(TmpVariable.containsKey(name))
            return TmpVariable.get(name).peek();
        return null;
    }

    /**脚本**/

    /**
     * 加载脚本文件(从磁盘上加载)
     * @param file_path 脚本文件路径，比如"g:\stdmath.cml"
     * @return 加载成功的话就会返回成功提示字符串
     */
    public String LoadScriptFile(String file_path)throws Exception{
        try{
            GetScriptManager().LoadScript(file_path);
        }catch (Exception e){
            Log.Error(e.getMessage());
            return "loaded scriptfile failed!";
        }
        return "loaded scriptfile successfully!";
    }

    private ScriptManager scriptManager=new ScriptManager(this);

    /**
     * 获取本计算器的当前脚本管理器
     * */
    public ScriptManager GetScriptManager(){return scriptManager;}
}
