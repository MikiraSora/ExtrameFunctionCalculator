using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media;

namespace CalculatorGUI.Model
{
    public enum MessageType
    {
        Info,//消息
        Error,//错误
        Execute,//操作
        Comment,//
        Solve//计算
    }

    public class DisplayItem: INotifyPropertyChanged
    {
        #region Brush
        static System.Windows.Media.SolidColorBrush InfoBackgroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Aqua);
        static System.Windows.Media.SolidColorBrush InfoForegroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Orange);

        static System.Windows.Media.SolidColorBrush ErrorBackgroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Red);
        static System.Windows.Media.SolidColorBrush ErrorForegroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Yellow);

        static System.Windows.Media.SolidColorBrush ExecuteBackgroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Blue);
        static System.Windows.Media.SolidColorBrush ExecuteForegroundBrush = new System.Windows.Media.SolidColorBrush(Colors.White);

        static System.Windows.Media.SolidColorBrush CommentBackgroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Green);
        static System.Windows.Media.SolidColorBrush CommentForegroundBrush = new System.Windows.Media.SolidColorBrush(Colors.White);

        static System.Windows.Media.SolidColorBrush SolveBackgroundBrush = new System.Windows.Media.SolidColorBrush(Colors.Orange);
        static System.Windows.Media.SolidColorBrush SolveForegroundBrush = new System.Windows.Media.SolidColorBrush(Colors.White);
        #endregion

        private string message = "";
        public string Message { get { return message; } set
            {
                message = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("message"));
            }
        }

        public MessageType Type { get; set; } = MessageType.Solve;

        public SolidColorBrush BackgroundBrush { get
            {
                switch (Type)
                {
                    case MessageType.Info:
                        return InfoBackgroundBrush;
                    case MessageType.Error:
                        return ErrorBackgroundBrush;
                    case MessageType.Execute:
                        return ExecuteBackgroundBrush;
                    case MessageType.Comment:
                        return CommentBackgroundBrush;
                    case MessageType.Solve:
                        return SolveBackgroundBrush;
                    default:
                        throw new Exception("unknown type");
                }
            } }

        public SolidColorBrush ForegroundBrush
        {
            get
            {
                switch (Type)
                {
                    case MessageType.Info:
                        return InfoForegroundBrush;
                    case MessageType.Error:
                        return ErrorForegroundBrush;
                    case MessageType.Execute:
                        return ExecuteForegroundBrush;
                    case MessageType.Comment:
                        return CommentForegroundBrush;
                    case MessageType.Solve:
                        return SolveForegroundBrush;
                    default:
                        throw new Exception("unknown type");
                }
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }
}
