using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
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
    /// FunctionPrinterPanel.xaml 的交互逻辑
    /// </summary>

    public class ParamestersData : INotifyPropertyChanged
    {
        bool isIndependent = false;
        public bool IsIndependent
        {
            get { return isIndependent; }
            set
            {
                isIndependent = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("IsIndependent"));
            }
        }

        string name = "";
        public string Name
        {
            get { return name; }
            set
            {
                name = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("Name"));
            }
        }

        string _value;
        public string Value
        {
            get { return _value; }
            set
            {
                _value = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs("Value"));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }

    public class FunctionPrinterData
    {
        public string FunctionName { get; set; } = "<unknown function>";

        public List<ParamestersData> Paramesters { get; set; } = new List<ParamestersData>();
    }

    public partial class FunctionPrinterPanel : UserControl
    {
        ObservableCollection<FunctionPrinterData> FunctionList = new ObservableCollection<FunctionPrinterData>();

        public delegate void FunctionPrinterApplyFunc(ObservableCollection<FunctionPrinterData> data);

        public event FunctionPrinterApplyFunc OnFunctionPrinterApply;

        public FunctionPrinterPanel()
        {
            InitializeComponent();

            MyFunctionPrinterList.ItemsSource = FunctionList;
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            OnFunctionPrinterApply?.Invoke(FunctionList);
        }

        public void AppendFunctionPrinterData(string function_name, string[] function_paramesters, string independent_name)
        {
            FunctionPrinterData data = new FunctionPrinterData()
            {
                FunctionName = function_name
            };

            foreach (var param in function_paramesters)
            {
                ParamestersData paramData = new ParamestersData()
                {
                    Name = param,
                    Value = "0",
                    IsIndependent = param == independent_name
                };

                data.Paramesters.Add(paramData);
            }

            FunctionList.Add(data);
        }
    }
}
