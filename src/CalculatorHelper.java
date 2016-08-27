import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by mikir on 2016/8/26.
 */

public class CalculatorHelper {
    public static void InitDeclare(){
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
    }

    public static String GetHelp(){
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("使用用法:\n").append('\n')
        .append("reg 函数名(函数参数1,函数参数2,函数参数3...函数参数n)=表达式\nreg用来声明一个函数,例如:\n  reg f(x)=x*10\n  reg f(x,y,z)=g(x)+t(x,y)+o(f(z))\n\n")
        .append("set 变量名=表达式\nset用来定义声明变量,例如:\n  set x=6\n  set y=x\n  set x=f(6) //注意此时函数f并未开始计算值\n\n")
        .append("solve 表达式\nsolve用来计算表达式 例如:\n  solve 6  //输出6\n  solve x //输出变量x的值(如果变量代表的是表达式则是计算此表达式并得到值)\n  solve f(100)+g(h(x))\n\n")
        .append("dump 参数\ndump用来打印指定的信息内容，注意参数只能输入一个,可用参数如下:\n  \"-rf\"或者\"raw_function\" :将打印出所有内建函数(不包括函数定义内容)\n  \"-cf\"或者\"custom_function\" :将打印出所有自己定义的函数(包括函数定义内容)\n  \"-var\"或者\"variable\" :将打印出所有自己定义的变量\n  \"-all\"或者\"all\" :将打印出以上所有内容\n例如:\n  dump -cf\n  dump variable\n\n");
        return stringBuffer.toString();
    }
}
