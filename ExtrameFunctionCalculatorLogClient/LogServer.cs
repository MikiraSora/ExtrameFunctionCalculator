using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace ExtrameFunctionCalculatorLogServer
{
    public class LogServer
    {
        public LogServer(int port)
        {
            this.port = port;
        }

        private int port = 2857;

        public void SetPort(int port)
        {
            this.port = port;
        }

        public int GetPort()
        {
            return port;
        }

        public delegate void OnReceiveMessageFunc(string message);

        public event OnReceiveMessageFunc OnReceiveMessage;

        public abstract class OnChangeNetStatus
        {
            public abstract void onWaitConnecting();

            public abstract void onConnected();

            public abstract bool onConnectError(Exception e);

            public abstract void onConnectedLost();
        }

        private OnChangeNetStatus trigger_onChangeNetStatus = null;

        public void SetOnChangeNetStatus(OnChangeNetStatus onChangeNetStatus)
        {
            trigger_onChangeNetStatus = onChangeNetStatus;
        }

        public void LoopRun()
        {
            Socket socket = null;

            try
            {
                if (trigger_onChangeNetStatus != null)
                    trigger_onChangeNetStatus.onWaitConnecting();
                socket = new Socket(SocketType.Stream, ProtocolType.Tcp);
                IPAddress ipaddr = IPAddress.Parse("127.0.0.1");
                IPEndPoint point = new IPEndPoint(ipaddr, port);
                socket.Bind(point);
                socket.Listen(0);
                var responseSocket = socket.Accept();
                if (trigger_onChangeNetStatus != null)
                    trigger_onChangeNetStatus.onConnected();

                byte[] buffer = new byte[256];

                while (responseSocket.Connected)
                {
                    for (int i = 0; i < buffer.Length; i++)
                    {
                        buffer[i] = 0;
                    }

                    int size = responseSocket.Receive(buffer);
                    Debug.Print($"receive data size:{size}");

                    string message = Encoding.UTF8.GetString(buffer).Trim();
                    message = message.Replace("\0", string.Empty);
                    OnReceiveMessage?.Invoke(message);
                }

                socket.Close();

                trigger_onChangeNetStatus?.onConnectedLost();
            }
            catch (Exception e)
            {
                if (trigger_onChangeNetStatus != null)
                {
                    trigger_onChangeNetStatus.onConnectError(e);
                }
            }
            finally
            {
                socket.Close();
            }
        }
    }
}