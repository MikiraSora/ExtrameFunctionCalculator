using ExtrameFunctionCalculator;
using System;

namespace Test
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            Calculator calculator = new Calculator();
            //calculator.Enable(Calculator.EnableType.ExpressionOptimize); 傻逼玩意
            calculator.Enable(Calculator.EnableType.PrecisionTruncation);

            while (true)
            {
                Console.Write("<=:");
                string command = Console.ReadLine();
                Console.ForegroundColor = ConsoleColor.Green;
                string result = calculator.Execute(command);
                if (result.Trim().Length != 0)
                    Console.WriteLine(result);
                Console.ResetColor();
            }
            Console.ReadLine();
        }
    }
}