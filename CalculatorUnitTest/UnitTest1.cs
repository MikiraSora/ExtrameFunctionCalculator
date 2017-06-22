using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Diagnostics;
using ExtrameFunctionCalculator;
using System.IO;

namespace CalculatorUnitTest
{
    [TestClass]
    public class UnitTest1
    {
        [TestMethod]
        public void NormalSolveTest()
        {
            Calculator calculator = new Calculator();

            string[] test_data = File.ReadAllLines(Environment.CurrentDirectory+"\\TestData\\normal_solve.txt");
            foreach (var case_string in test_data)
            {
                string casestring = case_string.Trim();
                if (casestring.Length == 0)
                    continue;

                double value = double.Parse(calculator.Solve(casestring.Substring(0,casestring.LastIndexOf('='))));
                Assert.AreEqual<double>(double.Parse(casestring.Substring(casestring.LastIndexOf('=')+1)),value);
            }
        }

        [TestMethod]
        public void BooleanSolveTest()
        {
            Calculator calculator = new Calculator();

            string[] test_data = File.ReadAllLines(Environment.CurrentDirectory + "\\TestData\\bool_solve.txt");
            foreach (var case_string in test_data)
            {
                string casestring = case_string.Trim();
                if (casestring.Length == 0)
                    continue;

                double value = double.Parse(calculator.Solve(casestring.Substring(0, casestring.LastIndexOf('='))));
                Assert.AreEqual<double>(double.Parse(casestring.Substring(casestring.LastIndexOf('=') + 1)), value);
            }
        }

        [TestMethod]
        public void VariableSolveTest()
        {
            Calculator calculator = new Calculator();

            string[] test_data = File.ReadAllLines(Environment.CurrentDirectory + "\\TestData\\variable_solve.txt");
            foreach (var case_string in test_data)
            {
                string casestring = case_string.Trim();
                if (casestring.Length == 0)
                    continue;
                
                if (casestring[casestring.Length - 1] == '\\')
                {
                    calculator.Execute(casestring.Substring(0, casestring.Length - 1));
                    continue;
                }

                double value = double.Parse(calculator.Solve(casestring.Substring(0, casestring.LastIndexOf('='))));
                Assert.AreEqual<double>(double.Parse(casestring.Substring(casestring.LastIndexOf('=') + 1)), value);
            }
        }

        [TestMethod]
        public void FunctionSolveTest()
        {
            Calculator calculator = new Calculator();

            string[] test_data = File.ReadAllLines(Environment.CurrentDirectory + "\\TestData\\function_solve.txt");
            foreach (var case_string in test_data)
            {
                string casestring = case_string.Trim();
                if (casestring.Length == 0)
                    continue;

                if (casestring[casestring.Length - 1] == '\\')
                {
                    calculator.Execute(casestring.Substring(0, casestring.Length - 1));
                    continue;
                }

                double value = double.Parse(calculator.Solve(casestring.Substring(0, casestring.LastIndexOf('='))));
                Assert.AreEqual<double>(double.Parse(casestring.Substring(casestring.LastIndexOf('=') + 1)), value);
            }
        }
    }
}
