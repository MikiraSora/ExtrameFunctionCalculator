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
        public static bool IsDigit(string expression)
        {
            if (expression.Length == 0)
                return false;
            foreach (char c in expression)
                if (!(char.IsDigit(c) || (c == '.')))
                    return false;
            return true;
        }

        public static bool IsValidVariable(string expression)
        {
            if (expression.Length == 0)
                return false;
            if (char.IsDigit(expression[0]))
                return false;
            if (IsDigit(expression))
                return false;
            foreach (char c in expression)
                if (!(char.IsLetterOrDigit(c) || (c == '_')))
                    return false;
            return true;
        }

        public static bool IsFunction(string expression)
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

        public static string RepeatingDecimalCoverToExpression(string decimalExpr)
        {
            char c;
            int pos = 0, notRepeatingDecimalLength = 0;
            string notRepeating = "", Repeating = "";
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
            string devNumber = "";
            for (int i = 0; i < Repeating.Length; i++)
                devNumber += 9;
            for (int i = 0; i < notRepeatingDecimalLength; i++)
                devNumber += 0;
            if (notRepeating[notRepeating.Length - 1] == '.')
                notRepeating += '0';
            return $"({notRepeating}+{Repeating}/{devNumber})";
        }

        static Regex repeating_decimal_check_regex = new Regex(@"(\d*)\.(\d*?)(\d+?)\3+(\d*)");

        public static string ExpressionCoverToRepeatingDecimal(string decimalExpr)
        {
            Match result = repeating_decimal_check_regex.Match(decimalExpr);
            if (!result.Success)
                Log.ExceptionError(new Exception(decimalExpr + " is invalid repeating decimal!"));
            string intDigit = result.Captures[1].Value, notRepeatDecimal = result.Captures[(2)].Value, RepeatDecimal = result.Captures[3].Value, endDecimal = result.Captures[(4)].Value;
            if (endDecimal.Length> RepeatDecimal.Length)
                Log.ExceptionError(new Exception(decimalExpr + " is invalid repeating decimal!"));
            string devNumber = "";
            for (int i = 0; i < RepeatDecimal.Length; i++)
                devNumber += 9;
            for (int i = 0; i < notRepeatDecimal.Length; i++)
                devNumber += 0;
            string expr = String.Format("({0}.{1}+{2}/{3})", intDigit, notRepeatDecimal, RepeatDecimal, devNumber);
            return expr;
        }

        public static bool IsRepeatingDecimal(string Decimal)
        {
            Match result = repeating_decimal_check_regex.Match(Decimal);
            if (!result.Success)
                return false;
            string /*intDigit=result.group(1),notRepeatDecimal=result.group(2),*/RepeatDecimal = result.Captures[3].Value, endDecimal = result.Captures[4].Value;
            if (endDecimal.Length > RepeatDecimal.Length)
                return false;
            return true;
        }
    }
}
