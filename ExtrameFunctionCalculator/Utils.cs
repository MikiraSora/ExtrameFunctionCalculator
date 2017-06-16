using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator
{
    public static class Utils
    {
        public static bool isDigit(string expression)
        {
            if (expression.Length == 0)
                return false;
            foreach (char c in expression)
                if (!(char.IsDigit(c) || (c == '.')))
                    return false;
            return true;
        }

        public static bool isValidVariable(string expression)
        {
            if (expression.Length == 0)
                return false;
            if (char.IsDigit(expression[0]))
                return false;
            if (isDigit(expression))
                return false;
            foreach (char c in expression)
                if (!(char.IsLetterOrDigit(c) || (c == '_')))
                    return false;
            return true;
        }

        public static bool isFunction(string expression)
        {
            if (expression.Length == 0)
                return false;
            if (Char.IsDigit(expression[0]))
                return false;
            int position = 0;
            bool hasMatchBracket = false, alreadyMatch = false;
            Stack<int> bracket_stack = new Stack<int>();
            while (true)
            {
                if (position >= expression.Length)
                    break;
                if (expression[position] == '(')
                {
                    bracket_stack.Push(position);
                }
                if (expression[position] == ')')
                {
                    if (bracket_stack.Count == 0)
                        return false;
                    bracket_stack.Pop();
                    if (bracket_stack.Count == 0)
                    {
                        if (alreadyMatch)
                        {
                            return false;
                        }
                        else
                        {
                            alreadyMatch = true;
                        }
                    }
                    hasMatchBracket = true;
                }
                position++;
            }
            if (bracket_stack.Count != 0)
                return false;
            return hasMatchBracket;
        }

        public static string RepeatingDecimalCoverToExpression(String decimalExpr)
        {
            char c;
            int pos = 0, notRepeatingDecimalLength = 0;
            String notRepeating = "", Repeating = "";
            while (true)
            {
                c = decimalExpr[(pos)];
                if (c != '(' && c != ')')
                {
                    if (c != '.')
                    {
                        notRepeating += c;
                    }
                    else
                    {
                        notRepeating += c;
                        pos++;
                        while (true)
                        {
                            c = decimalExpr[(pos)];
                            if (c == '(')
                                break;
                            notRepeating += c;
                            notRepeatingDecimalLength++;
                            pos++;
                            if (pos >= decimalExpr.Length)
                                Log.ExceptionError(new Exception(decimalExpr + "cant cover to Expression"));
                        }
                        pos--;
                    }
                }
                else
                {
                    if (c == '(')
                    {
                        pos++;
                        while (true)
                        {
                            c = decimalExpr[(pos)];
                            if (c == ')')
                                break;
                            Repeating += c;
                            pos++;
                            if (pos >= decimalExpr.Length)
                                Log.ExceptionError(new Exception(decimalExpr + "cant cover to Expression"));
                        }
                        break;
                    }
                }
                pos++;
            }
            String devNumber = "";
            for (int i = 0; i < Repeating.Length; i++)
                devNumber += 9;
            for (int i = 0; i < notRepeatingDecimalLength; i++)
                devNumber += 0;
            return $"({notRepeating}+{Repeating}/{devNumber})";
        }

        static Regex RepeatingDecimalReg = new Regex(@"(\d*)\.(\d*?)(\d+?)\3+(\d*)");

        public static string ExpressionCoverToRepeatingDecimal(string decimalExpr)
        {
            Match result = RepeatingDecimalReg.Match(decimalExpr);
            if (!result.Success)
                Log.ExceptionError(new Exception(decimalExpr + " is invalid repeating decimal!"));
            String intDigit = result.Captures[1].Value, notRepeatDecimal = result.Captures[(2)].Value, RepeatDecimal = result.Captures[3].Value, endDecimal = result.Captures[(4)].Value;
            if (endDecimal.Length> RepeatDecimal.Length)
                Log.ExceptionError(new Exception(decimalExpr + " is invalid repeating decimal!"));
            String devNumber = "";
            for (int i = 0; i < RepeatDecimal.Length; i++)
                devNumber += 9;
            for (int i = 0; i < notRepeatDecimal.Length; i++)
                devNumber += 0;
            String expr = String.Format("({0}.{1}+{2}/{3})", intDigit, notRepeatDecimal, RepeatDecimal, devNumber);
            return expr;
        }

        public static bool isRepeatingDecimal(string Decimal)
        {
            Match result = RepeatingDecimalReg.Match(Decimal);
            if (!result.Success)
                return false;
            string /*intDigit=result.group(1),notRepeatDecimal=result.group(2),*/RepeatDecimal = result.Captures[3].Value, endDecimal = result.Captures[4].Value;
            if (endDecimal.Length > RepeatDecimal.Length)
                return false;
            return true;
        }
    }
}
