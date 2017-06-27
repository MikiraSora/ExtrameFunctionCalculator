using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtrameFunctionCalculator.UtilTools
{
    public static class ParserUtils
    {
        //public static (bool parse_success,string func_name,string func_params,string func_body)
        //private static Regex check_function_format_regex = new Regex("([a-zA-Z]\\w*)\\((.*)\\)");

        public static bool TryParseTextToFunctionDeclear(string text,out string func_name,out string func_params)
        {
            func_params = func_name = string.Empty;
            text = text.Trim();
            int bracket_pos;
            if (text.Length == 0 || char.IsDigit(text[0]) || text[text.Length - 1] != ')'||((bracket_pos=text.IndexOf('('))<0))
                return false;
            func_name = text.Substring(0, bracket_pos);
            func_params = text.Substring(bracket_pos+1, text.Length - bracket_pos - 2);
            return true;
        }

        public static bool TryParseTextToFunction(string text, out string func_name, out string func_params,out string func_body)
        {
            text = text.Trim();
            func_params = func_body = func_name = string.Empty;
            int body_position = text.IndexOf('='), bracket_position = text.IndexOf('(');
            //0-----bracket_position-----body_position-----text.Length
            if (body_position<0||bracket_position<0||text[body_position - 1] != ')')
                return false;

            func_name = text.Substring(0, bracket_position);
            func_params = text.Substring(bracket_position + 1, body_position - bracket_position - 2);
            func_body = text.Substring(body_position + 1);

            return true;
        }
    }
}
