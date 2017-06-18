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
            //SpeedTestLogger();

            Calculator calculator = new Calculator();

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

        volatile static int counter = 0;

        static void SpeedTestLogger()
        {
            Calculator calculator = new Calculator();
            Log.EnableLog = false;
            counter = 0;
            Timer timer = new Timer((state) => {
                Console.WriteLine($"{counter} t/s");
                counter = 0;
            },null,0,1000);

            for (int i = 0; i < 7; i++)
            {
                ThreadPool.QueueUserWorkItem((state) => {
                    while (true)
                    {
                        calculator.Solve("4+6*8/2*random(100)");
                        counter++;
                    }
                },null);
            }

            Console.ReadLine();
            Environment.Exit(0);
        }
    }
}
