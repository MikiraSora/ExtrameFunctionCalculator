import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * Created by mikir on 2016/8/26.
 */

public class CalculatorHelper {
    public static void InitOperatorDeclare(){
        Calculator.Symbol.RegisterOperation("+", 2, 6f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(a.GetDouble()+b.GetDouble())));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("-", 2, 6f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(a.GetDouble()-b.GetDouble())));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("*", 2, 9f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(a.GetDouble()*b.GetDouble())));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("/", 2, 9f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(a.GetDouble()/b.GetDouble())));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("%", 2, 9f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(a.GetDouble()%b.GetDouble())));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("^", 2, 12f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                ArrayList<Calculator.Expression> result=new ArrayList<Calculator.Expression>();
                Calculator.Digit a=(Calculator.Digit) paramterList.get(0),b=(Calculator.Digit) paramterList.get(1);
                result.add(new Calculator.Digit(Double.toString(Math.pow(a.GetDouble(),b.GetDouble()))));
                return result;
            }
        });

        Calculator.Symbol.RegisterOperation("(", 0, 99f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                return null;
            }
        });

        Calculator.Symbol.RegisterOperation(")", 0, 99f, new Calculator.Symbol.OperatorFunction() {
            @Override
            ArrayList<Calculator.Expression> onCalculate(ArrayList<Calculator.Expression> paramterList, Calculator calculator) throws Exception {
                return null;
            }
        });

    }

    public static void InitFunctionDeclare(){
        //cos
        Calculator.RegisterRawFunction("cos(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.cos(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //sin
        Calculator.RegisterRawFunction("sin(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.sin(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //tan
        Calculator.RegisterRawFunction("tan(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.tan(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //abs
        Calculator.RegisterRawFunction("abs(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.abs(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //sqrt
        Calculator.RegisterRawFunction("sqrt(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.sqrt(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //mod
        Calculator.RegisterRawFunction("mod(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(parameter.get("x").GetDigit().GetDouble()%parameter.get("y").GetDigit().GetDouble());
            }
        });
        //acos
        Calculator.RegisterRawFunction("acos(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.acos(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //asin
        Calculator.RegisterRawFunction("asin(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.asin(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //atan
        Calculator.RegisterRawFunction("atan(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.atan(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //cbrt
        Calculator.RegisterRawFunction("cbrt(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.cbrt(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //ceil
        Calculator.RegisterRawFunction("ceil(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.ceil(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //cosh
        Calculator.RegisterRawFunction("cosh(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.cosh(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //exp
        Calculator.RegisterRawFunction("exp(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.exp(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //floor
        Calculator.RegisterRawFunction("floor(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.floor(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //hypot
        Calculator.RegisterRawFunction("hypot(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.hypot(parameter.get("x").GetDigit().GetDouble(),parameter.get("y").GetDigit().GetDouble()));
            }
        });
        //log
        Calculator.RegisterRawFunction("log(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.log(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //max
        Calculator.RegisterRawFunction("max(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.max(parameter.get("x").GetDigit().GetDouble(),parameter.get("y").GetDigit().GetDouble()));
            }
        });
        //min
        Calculator.RegisterRawFunction("min(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.min(parameter.get("x").GetDigit().GetDouble(),parameter.get("y").GetDigit().GetDouble()));
            }
        });
        //random
        Calculator.RegisterRawFunction("random()", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.random());
            }
        });
        //round
        Calculator.RegisterRawFunction("round(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.round(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //nextUp
        Calculator.RegisterRawFunction("nextUp(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.nextUp(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //ulp
        Calculator.RegisterRawFunction("ulp(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.ulp(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //toDegrees
        Calculator.RegisterRawFunction("toDegrees(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.toDegrees(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //toRadians
        Calculator.RegisterRawFunction("toRadians(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.toRadians(parameter.get("x").GetDigit().GetDouble()));
            }
        });

        //nextDown
        Calculator.RegisterRawFunction("nextDown(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.nextDown(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //cosh
        Calculator.RegisterRawFunction("cosh(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.cosh(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //sinh
        Calculator.RegisterRawFunction("sinh(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.sinh(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //tanh
        Calculator.RegisterRawFunction("tanh(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.tanh(parameter.get("x").GetDigit().GetDouble()));
            }
        });

        //addExact
        Calculator.RegisterRawFunction("addExact(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.addExact(parameter.get("x").GetDigit().GetInteger(),parameter.get("y").GetDigit().GetInteger()));
            }
        });
        //expm1
        Calculator.RegisterRawFunction("expm1(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.expm1(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //decrementExact
        Calculator.RegisterRawFunction("decrementExact(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.decrementExact(parameter.get("x").GetDigit().GetInteger()));
            }
        });
        //copySign
        Calculator.RegisterRawFunction("copySign(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.copySign(parameter.get("x").GetDigit().GetDouble(),parameter.get("y").GetDigit().GetDouble()));
            }
        });
        //floorMod
        Calculator.RegisterRawFunction("floorMod(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.floorMod(parameter.get("x").GetDigit().GetInteger(),parameter.get("y").GetDigit().GetInteger()));
            }
        });
        //floorDiv
        Calculator.RegisterRawFunction("floorDiv(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.floorDiv(parameter.get("x").GetDigit().GetInteger(),parameter.get("y").GetDigit().GetInteger()));
            }
        });
        //IEEEremainder
        Calculator.RegisterRawFunction("IEEEremainder(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.IEEEremainder(parameter.get("x").GetDigit().GetDouble(),parameter.get("y").GetDigit().GetDouble()));
            }
        });
        //negateExact
        Calculator.RegisterRawFunction("negateExact(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.negateExact(parameter.get("x").GetDigit().GetInteger()));
            }
        });
        //signum
        Calculator.RegisterRawFunction("signum(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.signum(parameter.get("x").GetDigit().GetDouble()));
            }
        });
        //toIntExact
        Calculator.RegisterRawFunction("toIntExact(x,y)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                return Double.toString(Math.toIntExact(parameter.get("x").GetDigit().GetInteger()));
            }
        });
        //fact
        Calculator.RegisterRawFunction("fact(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                double max = parameter.get("x").GetDigit().GetDouble();
                BigInteger bigInteger=BigInteger.valueOf(1);
                long result=1;
                for(int i=1;i<=max;i++)
                    bigInteger=bigInteger.multiply(BigInteger.valueOf(i));
                return bigInteger.toString();
            }
        });

        /*
        *以下的函数并非是原MATH类所存在的函数，而且大多都是非计算性用途
        *所以使用以下函数前请务必读好函数定义前的注释。弄清楚使用方法和限定缺陷
        *否则GG
        * 请用master的支线版本，feature是测试中的，dev则是开发中
        * */

        /*execute
            此方法会执行代码，但本身将会被当作参数参与计算Caculator.Solve（）。
        */
        Calculator.RegisterRawFunction("execute(x)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                Calculator c=calculator==null?new Calculator():calculator;
                Calculator.Variable variable=parameter.get("x");
                String execute_text="";
                switch (variable.variable_type){
                    case Normal:{
                        execute_text= variable.Solve();
                        return execute_text;
                    }
                    case ExpressionVariable:{
                        execute_text= ((Calculator.ExpressionVariable)variable).GetExpreesion();
                        break;
                    }
                }
                return calculator.Execute(execute_text);
            }

            @Override
            public HashMap<String, Calculator.Variable> onParseParamter(String paramter, Calculator.Function.ParameterRequest parameterRequest,Calculator calculator) {
                HashMap<String, Calculator.Variable> ParamterMap=new HashMap<String, Calculator.Variable>();
                ParamterMap.put(parameterRequest.GetParamterName(0),new Calculator.ExpressionVariable(parameterRequest.GetParamterName(0),paramter,calculator));
                return ParamterMap;
            }
        });

        /*bool
        bool并不是独立的类型，而是普通的计算,若值为0则为false，反之true.
         */
        Calculator.RegisterRawFunction("bool(condition)", new Calculator.ReflectionFunction.OnReflectionFunction() {
            @Override
            public HashMap<String, Calculator.Variable> onParseParamter(String paramter, Calculator.Function.ParameterRequest request, Calculator calculator) {
                HashMap<String, Calculator.Variable> map = new HashMap<String, Calculator.Variable>();
                map.put(request.GetParamterName(0), new Calculator.ExpressionVariable(request.GetParamterName(0), paramter, calculator));
                return map;
            }
            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator) throws Exception {
                String paramter = new BooleanCaculator.BooleanVariable(new BooleanCaculator(calculator).Solve(((Calculator.ExpressionVariable) parameter.get("condition")).GetExpreesion()),calculator).Solve();
                return paramter;
            }
        });


        /*if
            此方法会根据条件执行相对应的表达式，布尔运算的是从左到右计算
            if(condition,true_expr,false_expr)
            condition : 条件表达式，只有这里才可以执行布尔运算 ，如 4+6>10 , 4^2<0==true , x==0||y!=0
            true_expr : 条件表达式最终结果为true或者数值不为0时就会计算此表达式(Solve())
            true_expr : 条件表达式最终结果为false或者数值为0时就会计算此表达式(Solve())
        */
        Calculator.RegisterRawFunction("if(condition,true_expr,false_expr)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public HashMap<String, Calculator.Variable> onParseParamter(String paramter, Calculator.Function.ParameterRequest request, Calculator calculator)throws Exception{
                char c;
                int requestIndex = 0;
                HashMap<String, Calculator.Variable> variableHashMap=new HashMap<String, Calculator.Variable>();
                Stack<Integer> BracketStack = new Stack<>();
                String paramterString = new String();
                for (int pos = 0; pos < paramter.length(); pos++) {
                    c = paramter.charAt(pos);
                    if (c == '(') {
                        BracketStack.push(pos);
                    } else if (c == ')') {
                        if (!BracketStack.isEmpty())
                            BracketStack.pop();
                        else
                            throw new Exception("Not found a pair of bracket what defining a expression");
                    }

                    if (c == ',' && BracketStack.isEmpty()) {
                        String requestParamterName = request.GetParamterName(requestIndex++);
                        variableHashMap.put(requestParamterName, new Calculator.ExpressionVariable(requestParamterName, paramterString, calculator));
                        paramterString = new String();
                    } else {
                        paramterString += c;
                    }
                }
                if (!paramter.isEmpty())
                    variableHashMap.put(request.GetParamterName(requestIndex), new Calculator.ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator));
                return variableHashMap;
            }

            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                if(new BooleanCaculator(calculator).Solve(((Calculator.ExpressionVariable)parameter.get("condition")).GetExpreesion()))
                    return calculator.Solve(parameter.get("true_expr").Solve());
                else
                    return calculator.Solve(parameter.get("false_expr").Solve());
            }

        });

        /*extraSolve
        * 此函数会先计算normal_expr表达式，后计算extra_expr表达式，但返回normal_expr表达式的计算结果
        * solve extraSolve(5+5+4,1) //返回14
        * */
        Calculator.RegisterRawFunction("extraSolve(normal_expr,extra_expr)", new Calculator.ReflectionFunction.OnReflectionFunction(){
            @Override
            public HashMap<String, Calculator.Variable> onParseParamter(String paramter, Calculator.Function.ParameterRequest request, Calculator calculator)throws Exception{
                char c;
                int requestIndex = 0;
                HashMap<String, Calculator.Variable> variableHashMap=new HashMap<String, Calculator.Variable>();
                Stack<Integer> BracketStack = new Stack<>();
                String paramterString = new String();
                for (int pos = 0; pos < paramter.length(); pos++) {
                    c = paramter.charAt(pos);
                    if (c == '(') {
                        BracketStack.push(pos);
                    } else if (c == ')') {
                        if (!BracketStack.isEmpty())
                            //BracketStack.push(pos);
                            BracketStack.pop();
                        else
                            throw new Exception("Not found a pair of bracket what defining a expression");
                    }

                    if (c == ',' && BracketStack.isEmpty()) {
                        String requestParamterName = request.GetParamterName(requestIndex++);
                        variableHashMap.put(requestParamterName, new Calculator.ExpressionVariable(requestParamterName, paramterString, calculator));
                        paramterString = new String();
                    } else {
                        paramterString += c;
                    }
                }
                if (!paramter.isEmpty())
                    variableHashMap.put(request.GetParamterName(requestIndex), new Calculator.ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator));
                return variableHashMap;
            }

            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator)throws Exception{
                String result=calculator.Solve(parameter.get("normal_expr").Solve());
                calculator.Solve(parameter.get("extra_expr").Solve());
                return result;
            }
        });
        /*loop_with
        * 以步长step,从min到max循环执行expr,内置_index,_step,_min,_max,_out变量
        * 注意expr是要计算的表达式而不是语句
        * */
        Calculator.RegisterRawFunction("loop_with(step,min,max,expr)", new Calculator.ReflectionFunction.OnReflectionFunction() {
            @Override
            public HashMap<String, Calculator.Variable> onParseParamter(String paramter, Calculator.Function.ParameterRequest request, Calculator calculator)throws Exception{
                char c;
                int requestIndex = 0;
                HashMap<String, Calculator.Variable> variableHashMap=new HashMap<String, Calculator.Variable>();
                Stack<Integer> BracketStack = new Stack<>();
                String paramterString = new String();
                for (int pos = 0; pos < paramter.length(); pos++) {
                    c = paramter.charAt(pos);
                    if (c == '(') {
                        BracketStack.push(pos);
                    } else if (c == ')') {
                        if (!BracketStack.isEmpty())
                            //BracketStack.push(pos);
                            BracketStack.pop();
                        else
                            throw new Exception("Not found a pair of bracket what defining a expression");
                    }

                    if (c == ',' && BracketStack.isEmpty()) {
                        String requestParamterName = request.GetParamterName(requestIndex++);
                        variableHashMap.put(requestParamterName, requestParamterName.equals("expr")?new Calculator.ExpressionVariable(requestParamterName, paramterString, calculator):new Calculator.Variable(requestParamterName, paramterString, calculator));
                        paramterString = new String();
                    } else {
                        paramterString += c;
                    }
                }
                if (!paramter.isEmpty())
                    variableHashMap.put(request.GetParamterName(requestIndex), new Calculator.ExpressionVariable(request.GetParamterName(requestIndex), (paramterString), calculator));
                return variableHashMap;
            }

            @Override
            public String onReflectionFunction(HashMap<String, Calculator.Variable> parameter, Calculator calculator) throws Exception {
                //double x=parameter.get("x").GetDigit().GetDouble();
                double step=parameter.get("step").GetDigit().GetDouble();
                double min=parameter.get("min").GetDigit().GetDouble();
                double max=parameter.get("max").GetDigit().GetDouble();
                String expr=((Calculator.ExpressionVariable)parameter.get("expr")).GetExpreesion();
                Calculator.Function function=new Calculator.Function(String.format("tmp_execute(_index,_step,_min,_max,_out)=%s",expr),calculator);//todo 可优化
                String out="0";
                for(double i=min;i<=max;i+=step){
//                    calculator.GetFunction("loop_with").paramter.get("x").rawText=Double.toString(i);
                    out=function.Solve(String.format("%f,%f,%f,%f,%s",i,step,min,max,out));
                }
                return out;
            }
        });
    }

    public static String GetHelp(){
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("使用用法:\n").append('\n')
        .append("reg 函数名(函数参数1,函数参数2,函数参数3...函数参数n)=表达式\nreg用来声明一个函数,例如:\n  reg f(x)=x*10\n  reg f(x,y,z)=g(x)+t(x,y)+o(f(z))\n\n")
        .append("set 变量名=表达式\nset用来定义声明变量,例如:\n  set x=6\n  set y=x\n  set x=f(6) //此时函数f开始计算值才赋给变量\n\n")
        .append("solve 表达式\nsolve用来计算表达式 例如:\n  solve 6  //输出6\n  solve x //输出变量x的值(如果变量代表的是表达式则是计算此表达式并得到值)\n  solve f(100)+g(h(x))\n\n")
        .append("dump 参数(指定类型)\ndump用来打印指定的信息内容，注意参数只能输入一个,可用参数如下:\n  \"-rf\"或者\"raw_function\" :将打印出所有内建函数(不包括函数定义内容)\n  \"-cf\"或者\"custom_function\" :将打印出所有自己定义的函数(包括函数定义内容)\n  \"-var\"或者\"variable\" :将打印出所有自己定义的变量\n  \"-all\"或者\"all\" :将打印出以上所有内容\n例如:\n  dump -cf\n  dump variable\n\n")
        .append("set_expr 表达式名=表达式\nset_expr类似于set，声明一个变量但是它储存表达式，前者set_expr可以不立即计算，后者set就必须立即计算，例如:\n  set_expr myexpr=a+b-f(x)//此时变量a和b,以及函数f()均为声明，但因为是声明一个表达式变量myexpr，并未开始计算，所以是可以的\n  set_expr refmyexpr=myexpr//ref_myexpr将会引用于myexpr的表达式\n\n")
        .append("delete 参数(指定类型) 名字\ndelete可以删除某个变量或者函数(不能删除内建函数),可用参数如下:\n  \"function\" :在查找并删除指定的函数(不包括内建函数)\n  \"variable\" :在查找并删除指定的变量\n例如:\n  delete variable myvar\n\n")
        .append("clear \nclear会刷新计算器(并不会清除已经定义的函数或者变量)\n\n")
        .append("reset \nreset会重置计算器，清空并删除已经定义的函数或者变量(除了内建函数),顺带执行clear命令\n\n")
        .append("save 参数(指定类型) 保存路径\nsave用来保存已声明的函数(暂不支持反射函数)或者变量,注意参数只能输入一个,也注意IO权限,可用参数如下:\n  \"function\" :将保存出所有内建函数(不包括反射函数)\n  \"variable\" :将保存出所有变量\n  \"all\" :将保存以上内容(还是不包括反射函数:D)\n例如:\n  save function C:\\e.opt\n  save function \\a.8s8s8s\n\n")
        .append("load 读取路径\nload用来读取前者save保存的文件，因为save保存时已经分开类型所以load会自行判断并读取，加载。例如:\n  load F:\\mysave.opt\n\n");
        return stringBuffer.toString();
    }
}
