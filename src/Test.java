import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by mikir on 2016/8/20.
 */
public class Test {
    public static void main(String args[])throws Exception{
        System.out.println("Test Start");
        BaseCalculator bc=new BaseCalculator();
        Calculator c=new Calculator();
        System.out.println("please input \"help\" if you dont known how to use this.");
        long time=0;
        while(true){
            try {
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
    }
}
