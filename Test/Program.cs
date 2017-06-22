using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ExtrameFunctionCalculator;
using System.Threading;

namespace Test
{
    class Program
    {
        static void Main(string[] args)
        {
            Calculator calculator = new Calculator();
            //calculator.Enable(Calculator.EnableType.ExpressionOptimize); 傻逼玩意
            calculator.Enable(Calculator.EnableType.PrecisionTruncation);

            calculator.BoolSolve("4>8");

            while (true)
            {
                Console.Write("<=:");
                string command=Console.ReadLine();
                Console.ForegroundColor = ConsoleColor.Green;
                string result = calculator.Execute(command);
                if(result.Trim().Length!=0)
                    Console.WriteLine(result);
                Console.ResetColor();
            }
            Console.ReadLine();
        }
    }
}
