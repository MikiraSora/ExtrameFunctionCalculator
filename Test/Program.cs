using ExtrameFunctionCalculator;
using ExtrameFunctionCalculator.UtilTools;
using System;
using System.Text.RegularExpressions;
using System.Threading;

namespace Test
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            //Speed();
            //Test();
            Calculator calculator = new Calculator();
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

        public static void Speed()
        {
            //1: 68000t/s
            //2: 79000t/s

            Calculator calculator = new Calculator();
            Log.EnableLog = false;
            //calculator.Enable(Calculator.EnableType.ExpressionOptimize); 傻逼玩意
            calculator.Enable(Calculator.EnableType.PrecisionTruncation);

            int i = 0;
            Timer t = new Timer((state) =>
            {
                Console.WriteLine($"Calculate Speed:{i}t/s");
                i = 0;
            }, null, 0, 1000);

            while (true)
            {
                calculator.Solve("4+6*2-1");
                i++;
            }
        }

        public static void Test()
        {
            string fun = "functionACQ(x,y,z)";
            Regex check_function_format_regex = new Regex(@"([a-zA-Z]\w*)\((.*)\)=(.+)");
            string a, b, c;

            int i = 0;
            Timer t = new Timer((state) =>
            {
                Console.WriteLine($"Calculate Speed:{i}t/s");
                i = 0;
            }, null, 0, 1000);

            while (true)
            {
                ParserUtils.TryParseTextToFunctionDeclear(fun, out a, out b);
                i++;
            }
        }
    }
}