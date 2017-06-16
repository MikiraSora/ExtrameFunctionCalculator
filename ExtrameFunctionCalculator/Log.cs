using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

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

            String callerMethodName = null;

            private long time_strip = 0;

            private String message = null;

            private static long recordTime = 0;

            internal static void InitRecordTime() { recordTime = Environment.TickCount; }

            private static long GetCurrentTime() { return Environment.TickCount - recordTime; }

            public Message()
            {
                time_strip = GetCurrentTime();
            }

            public Message(Type type, long time, String message, String methodname)
            {
                callerMethodName = methodname;
                message_type = type;
                time_strip = time;
                this.message = message;
            }

            public Message(Type type, long time, String message)
            {
                message_type = type;
                time_strip = time;
                this.message = message;
            }

            public Message(Type type, String message) : this(type, GetCurrentTime(), message) { }
            public Message(Type type, String message, String methodname) : this(type, GetCurrentTime(), message, methodname) { }

            public String toString()
            {
                long min = time_strip / 1000 / 60;
                long sec = (time_strip - min * 1000 * 60) / 1000;
                long mill_sec = time_strip - min * 60 * 1000 - sec * 1000;
                if (Log.IsShowCallerMethod)
                    return String.Format("%c[%2d:%2d:%2d %s]%s():%s", message_type.ToString()[(0)], min, sec, mill_sec, message_type.ToString(), callerMethodName != null ? callerMethodName : "unknown_method", message);
                return String.Format("%c[%2d:%2d:%2d %s]%s", message_type.ToString()[0], min, sec, mill_sec, message_type.ToString(), message);
            }
        }

        private static bool _isShowCallerMethod = true;
        public static void SetShowCallerMethod(bool isShowCallerMethod) { IsThreadCommitLog = isShowCallerMethod; }
        public static bool IsShowCallerMethod { get { return _isShowCallerMethod; } }

        private static Socket socket;

        public static void InitRecordTime() { Message.InitRecordTime(); }

        public static Thread thread = null;

        public static bool _enable_log = true;
        public static bool EnableLog { get { return _enable_log; } set { _enable_log = value; } }

        private class MaintenanceRunnable
        {
            private volatile bool isLock = true;
            private volatile bool isExit = false;
            public volatile bool isHighLevelCommit = false;

            private long flushInv = 30;

            public bool IsExit() { return isExit; }

            public void Exit() { isExit = true; }
            public void Unlock() { isLock = false; }

            private List<Message> CommitLogQueue = new List<Message>();
            public void CommitLog(Message message)
            {
                CommitLogQueue.Add(message);
                Unlock();
            }

            public void run()
            {
                Message message = null;
                isExit = false;
                while (!isExit)
                {
                    while (isLock)
                    {
                        try
                        {
                            Thread.Sleep((int)flushInv);
                        }
                        catch (Exception e) { }
                    }
                    /*
                    while(!CommitLogQueue.isEmpty()) {
                        message = CommitLogQueue.remove(0);
                        if (CommitLogQueue.size() > limitCount) {
                            //触发规则
                            if(!isHighLevelCommit){
                                //启动
                                isHighLevelCommit=true;
                                if(highlevelThread==null){
                                    highlevelThread=new Thread(highLevelMaintenanceRunnable);
                                    highlevelThread.start();
                                }else{
                                    if(highLevelMaintenanceRunnable.isExit()){
                                        highlevelThread.start();
                                    }
                                }
                            }
                        }
                        if(isHighLevelCommit){
                            highLevelMaintenanceRunnable.PostMessageSend(message);
                            continue;
                        }*/
                    message = CommitLogQueue.Count == 0 ? null : CommitLogQueue[(0)];
                    if (message == null)
                        continue;
                    CommitLogQueue.RemoveAt(0);
                    try
                    {
                        SocketWrite(message.toString());
                    }
                    catch (Exception e) { }
                }
                isLock = true;
            }

        }

        private static MaintenanceRunnable maintenanceRunnable = new MaintenanceRunnable();

        private static bool IsThreadCommitLog = false;
        public static void SetIsThreadCommitLog(bool isThreadCommitLog)
        {
            IsThreadCommitLog = isThreadCommitLog;
            if (IsThreadCommitLog)
            {
                if (thread == null)
                {
                    thread = new Thread(maintenanceRunnable.run);
                    thread.Name = ("Log_Commit thread");
                    thread.Start();
                }
                if (maintenanceRunnable.IsExit())
                    thread.Start();
            }
            else
            {
                if (thread == null)
                    return;
                if (!maintenanceRunnable.IsExit())
                    maintenanceRunnable.Exit();
            }
        }

        private static int port = 2857;
        public static void SetPort(int port) { Log.port = port; }
        public static int GetPort() { return port; }

        private static String address = "127.0.0.1";
        public static void SetAddress(String address) { Log.address = address; }
        public static String GetAddress() { return address; }

        private static List<Message> messages_history = new List<Message>();

        private static int histroy_size = 10;
        public static void SetHistorySize(int history_size) { Log.histroy_size = history_size; }

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
            socket = new Socket(SocketType.Raw, ProtocolType.Tcp);
        }

        private static void SocketWrite(String text)
        {
            if (!IsConnecting())
                Connect();
            socket.Send(Encoding.Default.GetBytes(text));
        }

        public static void LogWrite(Message message)
        {
            if (!EnableLog)
                return;
            if (IsThreadCommitLog)
            {
                maintenanceRunnable.CommitLog(message);
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

        public static String GetCallerMethodName()
        {
            /*暂时咕咕
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            int caller_method = 3;
            StackTraceElement caller = stackTraceElements[caller_method];
            return caller.getMethodName();
            */
            return "Unknown_Method";
        }

        public static void Error(String message)
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

        public static void Debug(String message)
        {
            try
            {
                Message msg = IsShowCallerMethod ? new Message(Message.Type.Debug, message, GetCallerMethodName()) : new Message(Message.Type.Debug, message);
                LogWrite(msg);
            }
            catch (Exception e) { }
        }

        public static void Warning(String message)
        {
            try
            {
                Message msg = IsShowCallerMethod ? new Message(Message.Type.Warning, message, GetCallerMethodName()) : new Message(Message.Type.Warning, message);
                LogWrite(msg);
            }
            catch (Exception e) { }
        }

        public static void User(String message)
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
