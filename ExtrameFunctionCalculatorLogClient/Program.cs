using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using static ExtrameFunctionCalculatorLogServer.LogServer;

namespace ExtrameFunctionCalculatorLogServer
{
    class ChangeNetStatus : OnChangeNetStatus
    {
        public override void onConnected()
        {
            Console.WriteLine("Server accept a client >///<!\n-------------------------------");
        }

        public override void onConnectedLost()
        {
            Console.WriteLine("Server lost a client.");
        }

        public override bool onConnectError(Exception e)
        {
            Console.WriteLine("Server occured a error.");
            return false;
        }

        public override void onWaitConnecting()
        {
            Console.WriteLine("Server is connecting......");
        }
    }

    class Program
    {
        private static String SegmentationMarker = "#@#seg_marker#%#";

        static void PrintMessage(String message)
        {
            String context = message.Substring(1);
            switch (message[(0)])
            {
                case 'U':
                    {
                        Console.ForegroundColor = ConsoleColor.Green;
                        break;
                    }
                case 'D':
                    {
                        break;
                    }
                case 'W':
                    {
                        Console.ForegroundColor = ConsoleColor.Yellow;
                        break;
                    }
                case 'E':
                    {
                        Console.ForegroundColor = ConsoleColor.Yellow;
                        Console.BackgroundColor = ConsoleColor.Red;
                        break;
                    }
                default:
                    {
                        break;
                    }
            }

            Console.WriteLine(context);
            Console.ResetColor();
        }

        static void Main(string[] args)
        {
            LogServer server = new LogServer(2857);

            server.SetOnChangeNetStatus(new ChangeNetStatus());

            server.OnReceiveMessage += (msg) =>
            {
                string[] msgs = msg.Split(new string[] { SegmentationMarker},StringSplitOptions.RemoveEmptyEntries);
                foreach (var item in msgs)
                {
                    PrintMessage(item.Trim());
                }
            };

            while(true)
                server.LoopRun();
        }
    }
}
