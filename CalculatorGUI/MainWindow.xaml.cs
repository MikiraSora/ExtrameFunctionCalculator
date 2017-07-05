using CalculatorGUI.Controller;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace CalculatorGUI
{
    /// <summary>
    /// MainWindow.xaml 的交互逻辑
    /// </summary>
    public partial class MainWindow : Window
    {
        SolvePartPage pageController = new SolvePartPage();

        List<string> command_history = new List<string>();
        string current_command = string.Empty;
        int current_history_itor = 0;

        static string NoticeString = "Input here. >///<";
        static SolidColorBrush GetFocusBrush = new SolidColorBrush(Colors.Black);
        static SolidColorBrush LostFocusBrush = new SolidColorBrush(Color.FromArgb(125,125,125,125));

        public MainWindow()
        {
            InitializeComponent();

            DisplayList.ItemsSource = pageController.MessageList;
            InputCommand.Text = NoticeString;
            InputCommand.Foreground = LostFocusBrush;
        }

        public void PushCommand()
        {
            string command = InputCommand.Text;
            InputCommand.Text = "";
            pageController.RecviedCommand(command);

            command_history.Add(command);
            current_history_itor = command_history.Count;
        }

        string GetNextCommand()
        {
            if (current_history_itor >= command_history.Count)
                return InputCommand.Text;
            else if ((++current_history_itor) >= command_history.Count)
                return current_command;
            return command_history[current_history_itor];
        }

        string GetPrevCommand()
        {
            if (current_history_itor == command_history.Count)
                current_command = InputCommand.Text;
            current_history_itor=current_history_itor!=0?current_history_itor-1:0;
            if (current_history_itor < 0 || current_history_itor >= command_history.Count)
                return string.Empty;
            return command_history[current_history_itor];
        }

        private void TextBox_KeyDown(object sender, KeyEventArgs e)
        {
            switch (e.Key)
            {
                case Key.Enter:
                    PushCommand();
                    break;
            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            PushCommand();
        }

        private void CreateNewCalculatorInstanceButton_Click(object sender, RoutedEventArgs e)
        {
            pageController.NewCreateCalculatorInstance();
        }

        private void MenuItem_Click(object sender, RoutedEventArgs e)
        {
            pageController.MessageList.Clear();
        }

        private void MenuItem_Click_1(object sender, RoutedEventArgs e)
        {
            pageController.SwitchInputMode(SolvePartPage.InputMode.Solve);
            ChangeMenuItemStatus(SolvePartPage.InputMode.Solve);
        }

        void ChangeMenuItemStatus(SolvePartPage.InputMode mode)
        {
            SolveStatusItem.IsChecked = ExecuteStatusItem.IsChecked= CommentStatusItem.IsChecked=false;
            switch (mode)
            {
                case SolvePartPage.InputMode.Comment:
                    CommentStatusItem.IsChecked = true;
                    break;
                case SolvePartPage.InputMode.Execute:
                    ExecuteStatusItem.IsChecked = true;
                    break;
                case SolvePartPage.InputMode.Solve:
                    SolveStatusItem.IsChecked = true;
                    break;
                default:
                    break;
            }
        }

        private void ExecuteStatusItem_Click(object sender, RoutedEventArgs e)
        {
            pageController.SwitchInputMode(SolvePartPage.InputMode.Execute);
            ChangeMenuItemStatus(SolvePartPage.InputMode.Execute);
        }

        private void CommentStatusItem_Click(object sender, RoutedEventArgs e)
        {
            pageController.SwitchInputMode(SolvePartPage.InputMode.Comment);
            ChangeMenuItemStatus(SolvePartPage.InputMode.Comment);
        }

        private void InputCommand_PreviewKeyDown(object sender, KeyEventArgs e)
        {
            switch (e.Key)
            {
                case Key.Up:
                    InputCommand.Text = GetPrevCommand();
                    break;
                case Key.Down:
                    InputCommand.Text = GetNextCommand();
                    break;
            }
        }

        private void InputCommand_PreviewTextInput(object sender, TextCompositionEventArgs e)
        {

        }

        private void InputCommand_GotFocus(object sender, RoutedEventArgs e)
        {
            if (InputCommand.Text.Trim() == NoticeString)
            {
                InputCommand.Text = string.Empty;
                InputCommand.Foreground = GetFocusBrush;
            }
        }
        private void InputCommand_LostFocus(object sender, RoutedEventArgs e)
        {
            if (InputCommand.Text.Trim() == string.Empty)
            {
                InputCommand.Text = NoticeString;
                InputCommand.Foreground = LostFocusBrush;
            }
        }
    }
}
