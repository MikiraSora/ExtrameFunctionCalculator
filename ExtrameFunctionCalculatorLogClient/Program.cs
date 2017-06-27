using System;
using static ExtrameFunctionCalculatorLogServer.LogServer;

namespace ExtrameFunctionCalculatorLogServer
{
    internal class ChangeNetStatus : OnChangeNetStatus
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

    internal class Program
    {
        private static string SegmentationMarker = "#@#";

        private static void PrintMessage(string message)
        {
            string context = message.Substring(1);
            switch (message[0])
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

        private static void Main(string[] args)
        {
            LogServer server = new LogServer(2857);

            server.SetOnChangeNetStatus(new ChangeNetStatus());

            server.OnReceiveMessage += (msg) =>
            {
                string[] msgs = msg.Split(new string[] { SegmentationMarker }, StringSplitOptions.RemoveEmptyEntries);
                foreach (var item in msgs)
                {
                    PrintMessage(item.Trim().Replace(SegmentationMarker, string.Empty));
                }
            };

            while (true)
                server.LoopRun();
        }
    }
}