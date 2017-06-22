using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace ExtrameFunctionCalculator
{
    public static class Log
    {
        public class Message
        {
            public enum Type
            {
                User,
                Debug,
                Warning,
                Exception
            }

            private Type message_type = Type.Debug;

            private string caller_method_name = null;

            private long time_strip = 0;

            private string message = null;

            private static long recore_time = 0;

            public Message()
            {
                time_strip = GetCurrentTime();
            }

            public Message(Type type, long time, string message, string methodname)
            {
                caller_method_name = methodname;
                message_type = type;
                time_strip = time;
                this.message = message;
            }

            public Message(Type type, long time, string message)
            {
                message_type = type;
                time_strip = time;
                this.message = message;
            }

            public Message(Type type, string message) : this(type, GetCurrentTime(), message)
            {
            }

            public Message(Type type, string message, string methodname) : this(type, GetCurrentTime(), message, methodname)
            {
            }

            internal static void InitRecordTime()
            {
                recore_time = Environment.TickCount;
            }

            private static long GetCurrentTime()
            {
                return Environment.TickCount - recore_time;
            }

            public string toString()
            {
                long min = time_strip / 1000 / 60;
                long sec = (time_strip - min * 1000 * 60) / 1000;
                long mill_sec = time_strip - min * 60 * 1000 - sec * 1000;
                if (Log.IsShowCallerMethod)
                    return String.Format("{0}[{1}:{2}:{3} {4}]{5}():{6}", message_type.ToString()[(0)], min, sec, mill_sec, message_type.ToString(), caller_method_name != null ? caller_method_name : "unknown_method", message);
                return String.Format("{0}[{1}:{2}:{3} {4}]{5}", message_type.ToString()[0], min, sec, mill_sec, message_type.ToString(), message);
            }
        }

        private class MaintenanceRunnable
        {
            private volatile bool is_lock = true;
            private volatile bool is_exit = false;

            private long flush_inv = 30;

            private Queue<Message> commit_queue = new Queue<Message>(100);

            public bool IsExit()
            {
                return is_exit;
            }

            public void Exit()
            {
                is_exit = true;
            }

            public void Unlock()
            {
                is_lock = false;
            }

            public void CommitLog(Message message)
            {
                commit_queue.Enqueue(message);
                Unlock();
            }

            public void ThreadRun()
            {
                Message message = null;
                is_exit = false;
                while (!is_exit)
                {
                    while (is_lock)
                    {
                        try
                        {
                            Thread.Sleep((int)flush_inv);
                        }
                        catch (Exception e) { }
                    }

                    message = commit_queue.Count == 0 ? null : commit_queue.Dequeue();
                    if (message == null)
                        continue;
                    try
                    {
                        SocketWrite(message.toString() + Log.segmentation_maker);
                    }
                    catch (Exception e) { }
                }
                is_lock = true;
            }
        }

        private static bool is_show_caller_method = true;
        internal static string segmentation_maker = "#@#";
        private static Socket socket;
        public static Thread thread = null;
        public static bool is_enable_log = true;
        private static MaintenanceRunnable maintenance_thread = new MaintenanceRunnable();
        private static bool is_thread_commit_log = false;
        private static int port = 2857;
        private static string address = "127.0.0.1";
        private static List<Message> messages_history = new List<Message>();
        private static int histroy_size = 10;
        public static bool EnableLog { get { return is_enable_log; } set { is_enable_log = value; } }
        public static bool IsShowCallerMethod { get { return is_show_caller_method; } }

        public static void SetShowCallerMethod(bool isShowCallerMethod)
        {
            is_thread_commit_log = isShowCallerMethod;
        }

        public static void InitRecordTime()
        {
            Message.InitRecordTime();
        }

        public static void SetIsThreadCommitLog(bool isThreadCommitLog)
        {
            is_thread_commit_log = isThreadCommitLog;
            if (is_thread_commit_log)
            {
                if (thread == null)
                {
                    thread = new Thread(maintenance_thread.ThreadRun);
                    thread.Name = ("Log_Commit thread");
                    thread.Start();
                }
                if (maintenance_thread.IsExit())
                    thread.Start();
            }
            else
            {
                if (thread == null)
                    return;
                if (!maintenance_thread.IsExit())
                    maintenance_thread.Exit();
            }
        }

        public static void SetPort(int port)
        {
            Log.port = port;
        }

        public static int GetPort()
        {
            return port;
        }

        public static void SetAddress(string address)
        {
            Log.address = address;
        }

        public static string GetAddress()
        {
            return address;
        }

        public static void SetHistorySize(int history_size)
        {
            Log.histroy_size = history_size;
        }

        private static void PushHistory(Message message)
        {
            if (messages_history.Count >= histroy_size)
                messages_history.RemoveAt(0);
            messages_history.Add(message);
        }

        public static bool IsConnecting()
        {
            if (socket == null)
                return false;
            return socket.Connected;
        }

        public static void Connect()
        {
            if (IsConnecting())
                socket.Close();
            if (socket != null)
                socket.Close();
            socket = new Socket(SocketType.Stream, ProtocolType.Tcp);
            IPAddress ipaddr = IPAddress.Parse("127.0.0.1");
            IPEndPoint point = new IPEndPoint(ipaddr, port);

            socket.Connect(point);
        }

        private static void SocketWrite(string text)
        {
            if (!IsConnecting())
                Connect();
            socket.Send(Encoding.UTF8.GetBytes(text));
        }

        public static void LogWrite(Message message)
        {
            if (!EnableLog)
                return;
            if (is_thread_commit_log)
            {
                maintenance_thread.CommitLog(message);
                return;
            }
            try
            {
                SocketWrite(message.toString());
            }
            catch (Exception e)
            {
                return;
            }
            PushHistory(message);
        }

        public static string GetCallerMethodName()
        {
            /*暂时咕咕
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            int caller_method = 3;
            StackTraceElement caller = stackTraceElements[caller_method];
            return caller.getMethodName();
            */
            return "Unknown_Method";
        }

        public static void Error(string message)
        {
            Message msg = new Message(Message.Type.Exception, message, GetCallerMethodName());
            try
            {
                LogWrite(msg);
            }
            catch (Exception e) { return; }
        }

        public static void ExceptionError(Exception e)
        {
            Message msg = IsShowCallerMethod ? new Message(Message.Type.Exception, e.Message, GetCallerMethodName()) : new Message(Message.Type.Exception, e.Message);
            LogWrite(msg);
            throw e;
        }

        public static void Debug(string message)
        {
            try
            {
                Message msg = IsShowCallerMethod ? new Message(Message.Type.Debug, message, GetCallerMethodName()) : new Message(Message.Type.Debug, message);
                LogWrite(msg);
            }
            catch (Exception e) { }
        }

        public static void Warning(string message)
        {
            try
            {
                Message msg = IsShowCallerMethod ? new Message(Message.Type.Warning, message, GetCallerMethodName()) : new Message(Message.Type.Warning, message);
                LogWrite(msg);
            }
            catch (Exception e) { }
        }

        public static void User(string message)
        {
            try
            {
                Message msg = IsShowCallerMethod ? new Message(Message.Type.User, message, GetCallerMethodName()) : new Message(Message.Type.User, message);
                LogWrite(msg);
            }
            catch (Exception e) { }
        }

        public static void DisConnect()
        {
            socket.Close();
        }
    }
}