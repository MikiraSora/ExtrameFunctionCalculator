using CalculatorGUI.Controller;
using CalculatorGUI.Model;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading;
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

        private void MenuItem_Click_2(object sender, RoutedEventArgs e)
        {
            DisplayItem item = DisplayList.SelectedItem as DisplayItem;

            if (item == null)
            {
                MessageBox.Show("select item is null");
                return;
            }

            if (item.Message.StartsWith(">reg "))
            {
                MessageBox.Show("this is not a function");
                return;
            }

            string function = item.Message.Substring(5);
            function = function.Substring(0, function.IndexOf("(")).Trim();

            ExtrameFunctionCalculator.Types.Function rawFunction = pageController.CurrentCalculator.GetFunction(function);
            
            MyFunctionPrinterPanel.AppendFunctionPrinterData(function,rawFunction.FunctionParamesters.ToArray(),rawFunction.FunctionParamesters[0]);
        }

        List<FunctionPrinterData> CurrentFunctionData;

        private void MyFunctionPrinterPanel_OnFunctionPrinterApply(ObservableCollection<FunctionPrinterData> data)
        {
            CurrentFunctionData = data.ToList();
            DrawFunctions();
        }

        private void MainCoordnationCanvas_OnDrawData(object sender, PathGeometry drawProvider)
        {
            DrawFunctions();
        }

        volatile bool isCalculating = false;

        void DrawFunctions()
        {
            if (CurrentFunctionData == null || isCalculating)
                return;

            List<FunctionPrinterData> current_functionList = CurrentFunctionData;

            System.Collections.Generic.List<System.Collections.Generic.List<Point>> ResultList = new List<List<Point>>();

            int task_count = 0;

            foreach (var data in current_functionList)
            {
                task_count++;
                ThreadPool.QueueUserWorkItem((state) => {
                    string function_head = data.FunctionName+"(";
                    string independent_var_name=string.Empty;
                    System.Collections.Generic.List<Point> result_point = new List<Point>();
                    ParamestersData tmp_param_data;
                    double cal_Value = 0;
                    for (int i = 0; i < data.Paramesters.Count-1; i++)
                    {
                        tmp_param_data = data.Paramesters[i];
                        function_head += $"??{tmp_param_data.Name},";
                        if (tmp_param_data.IsIndependent)
                            independent_var_name = tmp_param_data.Name;
                    }

                    if (data.Paramesters[data.Paramesters.Count - 1].IsIndependent)
                        independent_var_name = data.Paramesters[data.Paramesters.Count - 1].Name;
                    function_head += $"??{data.Paramesters[data.Paramesters.Count-1].Name})";
                    
                    for (double i = 0; i < MainCoordnationCanvas.ActualWidth; i=i+MainCoordnationCanvas.Step* 0.1f)
                    {
                        double real_x = MainCoordnationCanvas.GetRealCurrentX(i);
                        string tmp = function_head;

                        foreach (var param in data.Paramesters)
                        {
                            if (param.IsIndependent)
                                continue;
                            tmp = tmp.Replace("??" + param.Name,param.Value);
                        }

                        tmp = tmp.Replace("??" + independent_var_name, real_x.ToString());
                        cal_Value = double.Parse(pageController.CurrentCalculator.Solve(tmp));
                        result_point.Add(new Point(real_x, cal_Value));
                    }

                    ResultList.Add(result_point);
                    
                    task_count--;
                    if (task_count == 0)
                        ReleaseDrawingStatus(ResultList);
                }, null);
            }
        }

        void ReleaseDrawingStatus(System.Collections.Generic.List<System.Collections.Generic.List<Point>> result_list)
        {
            Dispatcher.Invoke(() => {
                MainCoordnationCanvas.ClearAllLines();

                foreach (var points in result_list)
                {
                    MainCoordnationCanvas.AppendLines(points.ToArray(), true);
                }
                isCalculating = false;
            });
        }

        void HoldDrawingStatus()
        {
            isCalculating = true;
        }
    }
}
