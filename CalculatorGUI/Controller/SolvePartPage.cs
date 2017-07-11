using CalculatorGUI.Model;
using ExtrameFunctionCalculator;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace CalculatorGUI.Controller
{
    public class SolvePartPage
    {
        public enum InputMode
        {
            Comment,
            Execute,
            Solve
        }

        public InputMode CurrentInputMode
        {
            get; set;
        } = InputMode.Solve;

        public ObservableCollection<DisplayItem> MessageList { get; set; }

        public Calculator CurrentCalculator { get; set; }

        public SolvePartPage()
        {
            MessageList = new ObservableCollection<DisplayItem>();
        }

        public void BindMessageList(ListBox listbox) => listbox.ItemsSource = MessageList;

        public void AddDisplay(DisplayItem item)
        {
            MessageList.Add(item);
        }

        public void SwitchInputMode(InputMode mode)
        {
            CurrentInputMode = mode;

            DisplayItem item = new DisplayItem()
            {
                Message = $"Switch current mode : {mode.ToString()}",
                Type = MessageType.Info
            };

            AddDisplay(item);
        }

        public void RecviedCommand(string command)
        {
            DisplayItem item = new DisplayItem();

            try
            {
                if (CurrentCalculator == null)
                    throw new Exception("Please new a calculator instance and continue.");

                switch (CurrentInputMode)
                {
                    case InputMode.Comment:
                        item.Type = MessageType.Comment;
                        item.Message = "//" + command;
                        break;

                    case InputMode.Execute:
                        item.Type = MessageType.Execute;
                        item.Message = "> " + command;
                        string result = CurrentCalculator.Execute(command);
                        if (command.StartsWith("solve "))
                            result = " = " + result;
                        else
                            result = string.Empty;
                        item.Message += result;
                        break;

                    case InputMode.Solve:
                        item.Type = MessageType.Solve;
                        item.Message =command +" = "+CurrentCalculator.Solve(command);
                        break;
                }

            }
            catch (Exception e)
            {
                item.Type = MessageType.Error;
                item.Message = "Error : "+e.Message;
            }

            AddDisplay(item);
        }

        public void NewCreateCalculatorInstance()
        {
            CurrentCalculator = new Calculator();
            MessageList.Clear();

            DisplayItem item = new DisplayItem()
            {
                Message ="New Calculator Instance.",
                Type = MessageType.Info
            };

            AddDisplay(item);
        }
    }
}
