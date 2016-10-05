import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by mikir on 2016/8/20.
 */
public class Test {
    public static void main(String args[])throws Exception {
        System.out.println("Test Start");
        Calculator c=new Calculator();
        System.out.println("please input \"help\" if you dont know how to use this.");
        long time = 0, prev_t = System.nanoTime();
        long times = 0;
        long raw_times = 0;
        long time_max = 1000000000;//1000000000;
        long x = 0;

        c.OptimizeEnable(true);

        while(true){
            try {
                System.gc();
                System.out.println("input : ");
                Scanner input = new Scanner(System.in);
                String expression = input.nextLine();
                time=System.nanoTime();
                System.out.print(c.Execute(expression));
                System.out.print(String.format("(用时 %f ms)\n",(System.nanoTime()-time)/1000000.0f));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
/*
        System.out.println(String.format("test start!,calculate 4*6*0/1*6*88888/9999"));
        long times_total=0,raw_times_total=0;
        //c.Execute("reg f(x)=if(x==0,0,f(x-1)+1)");

        for(int i=0;i<10;i++) {
            time=0;
            times=0;
            c.OptimizeEnable(false);
            while (time < time_max) {
                prev_t = System.nanoTime();
                c.Execute("solve 4*6*0/1*6*88888/9999");
                time += (System.nanoTime() - prev_t);
                times++;
            }
            times_total+=times;
            c.OptimizeEnable(true);
            time=0;
            raw_times=0;
            while (time < time_max) {
                prev_t = System.nanoTime();
                c.Execute("solve 4*6*0/1*6*88888/9999");
                time += (System.nanoTime() - prev_t);
                raw_times++;
            }
            System.out.println(String.format("raw: %d--> %dtimes/sec , optimize: %dtimes/sec =>%.2f%%",i,times,raw_times,(times*1.0f/raw_times)*100));
            raw_times_total+=raw_times;
        }
        System.out.println(String.format("ave--> raw: %.2ftimes/sec , optimize: %.2ftimes/sec =>%.2f%%",times_total/10.0f,raw_times_total/10.0f,(times_total/10.0f)/(raw_times_total/10.0f)*100));

/*
        System.out.println("------Test Start------");
        for(String str:getTest())
            System.out.println(String.format("%s ==> %s",str,c.Execute(str)));
        System.out.println("------Test End------");*/

    }

    public static String[] getTest(){
        String test[]={
                "solve 4+2",
                "solve 4+(2+4)",
                "solve 4*(1+4+(6*2))",
                "set a=6",
                "set b=9",
                "set c=100",
                "solve a+b-c",
                "reg f(x)=if(x==0,0,f(x-1)+x)",
                "set_expr myexpr=a+b*c-d%g(c)",
                "set_expr d=a+b+g(f(c))",
                "reg g(x)=x+100",
                "solve myexpr",
                "reg g(x)=sin(x)-sin(-x)",
                "delete variable a",
                "delete function f",
                "solve g(1)+g(fact(2))",
                "dump -all",
                "reset"
        };
        return test;
    }
}
