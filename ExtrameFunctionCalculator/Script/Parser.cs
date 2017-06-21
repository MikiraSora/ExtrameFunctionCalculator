using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ExtrameFunctionCalculator.Script.Types;

namespace ExtrameFunctionCalculator.Script
{
    public class Parser
    {
        public delegate void ExecutorAction(string param, Parser reference_parser);

        Dictionary<int, Unit> statement_lines = new Dictionary<int, Unit>();
        List<string> include_file_names = new List<string>();

        Dictionary<String, int> label_postion_cache = new Dictionary<string, int>();
        Dictionary<String, ExecutorAction> reflection_precompiling_execution_map = null;
        Executor executor;

        public Dictionary<String, Function> function_table = new Dictionary<string, Function>();

        Dictionary<string, string> propherty = new Dictionary<string, string>();

        public Dictionary<int, Unit> StatementLine { get { return statement_lines; } }
        public Dictionary<string, string> Propherty { get { return propherty; } }
        public Executor RefExecutor { get { return executor; } }

        public Parser(Executor executor)
        {
            this.executor = executor;
        }

        public void Set(string key, string value) => propherty.Add(key, value);

        public string Get(string key) => propherty[key];

        void PushStatement(Unit unit)=>statement_lines.Add(GetNewLineId(), unit);

        int GetNewLineId()=>statement_lines.Count;

        public Dictionary<int , Unit> Parse(List<String> statements)
        {
            string text = null, command = null, paramter = null;
            Unit unit = null;
            int c = 0;

            for (int position = 0; position < statements.Count; position++)
            {
                text = statements[(position)].Trim().Trim(new char[]{'\t','\n'});

                if (text.Length==0)
                    continue;

                //预处理
                if (IsPreCompileCommand(text))
                {
                    ExecutePreCommand(position, text);
                    continue;
                }
                //转换
                //Label
                //text.toLowerCase();

                switch (text)
                {
                    case "endfunction":
                        PushStatement(new EndFunction(GetNewLineId(), this));
                        break;
                    case "endloop":
                        PushStatement(new EndLoop(GetNewLineId()));
                        break;
                    case "loop":
                        PushStatement(new LoopBegin(GetNewLineId()));
                        break;
                    case "break":
                        PushStatement(new Break(GetNewLineId()));
                        break;
                    case "continue":
                        PushStatement(new Continue(GetNewLineId()));
                        break;
                    case "endif":
                        PushStatement(new EndIf(GetNewLineId()));
                        break;
                    case "else":
                        PushStatement(new Else(GetNewLineId()));
                        break;
                    case "then":
                        PushStatement(new Then(GetNewLineId()));
                        break;
                    default:
                        {
                            unit = null;
                            command = "";
                            for (int tmp_position = 0; tmp_position < text.Length; tmp_position++)
                            {
                                c = text[(tmp_position)];
                                if (c == ' ')
                                {
                                    paramter = text.Substring(tmp_position + 1);
                                    unit = CommandCoverToUnit(command, paramter);
                                    break;
                                }
                                else
                                {
                                    command += (char)c;
                                }
                            }
                            if (unit != null)
                            {
                                PushStatement(unit);
                                break;
                            }
                            command = "";
                            //if(x+1)
                            for (int tmp_position = 0; tmp_position < text.Length; tmp_position++)
                            {
                                c = text[tmp_position];
                                if (c == '(')
                                {
                                    paramter = text.Substring(tmp_position);
                                    unit = CommandCoverToStatement(command, paramter);
                                    /*
                                    if(unit==null)
                                        throw new Exception(String.format("Cant parse command:%s",command));*/
                                    break;
                                }
                                else
                                {
                                    command += (char)c;
                                }
                            }
                            if (unit == null)
                                throw new SynatxErrorException(position, "cant parse command :" + text, RefExecutor);
                            else
                            {
                                PushStatement(unit);
                                break;
                            }
                        }
                }
            }
            return null;//// TODO: 2016/10/31
        }

        Unit CommandCoverToStatement(string command, string paramter)
        {
            Statement statement = null;
            Unit unit = null;
            switch (command)
            {
                case "if": unit = (new If(GetNewLineId(), paramter)); break;
            }
            if (statement == null && unit == null)
                return null;
            if (statement != null)
                return statement;
            return unit;
        }

        Unit CommandCoverToUnit(string command, string paramter)
        {
            Statement statement = null;
            Unit unit = null;
            switch (command)
            {
                case "function": statement = (new FunctionBody(GetNewLineId(), paramter, this)); break;
                //case "if":unit=(new Symbol.Condition.If(GetNewLineId(),paramter));break;
                case "set": statement = (new Set(GetNewLineId(), paramter)); break;
                case "call": statement = (new Call(GetNewLineId(), paramter)); break;
                case "return": statement = new Return(GetNewLineId(), paramter); break;
                case "goto": statement = new Goto(GetNewLineId(), paramter); break;
            }
            if (statement == null && unit == null)
                return null;
            if (statement != null)
                return statement;
            return unit;
        }

        static bool IsPreCompileCommand(string command) => command.Length==0 ? false : command[(0)] == '#';

        public void SetPreCompileExecutors(Dictionary<String, ExecutorAction> ExecutorActionHashMap)=>reflection_precompiling_execution_map = ExecutorActionHashMap;

        void ExecutePreCommand(int line, string text)
        {
            if (reflection_precompiling_execution_map == null)
                return;
            int position = 1, c = 0;
            string command = "", param ="";
            while (true)
            {
                if (position >= text.Length)
                    throw new SynatxErrorException(line, "Cant parse preCompile command: " + text, RefExecutor);
                c = text[(position)];
                if (c == ' ')
                    break;
                command += (char)c;
                position++;
            }
            param = text.Substring(position + 1);
            if (reflection_precompiling_execution_map.ContainsKey(command))
                reflection_precompiling_execution_map[(command)].Invoke(param, this);
        }

        public void FunctionRegister()
        {
            int position = -1;
            Unit unit = null;
            Stack<Function> function_stack = new Stack<Function>();//,else_stack=new Stack<>();
            Stack<If> if_stack = new Stack<If>();
            Stack<LoopBegin> loop_stack = new Stack<LoopBegin>();
            Function function = null;
            while (true) {
                position++;
                if (position >= statement_lines.Count)
                    break;
                unit = statement_lines[(position)];
                if (unit.UnitType == UnitType.Statement) {
                    if (((Statement)unit).StatementType == StatementType.Function) {
                        if (((Function)unit).FunctionType == FunctionType.Begin) {
                            function_stack.Push((Function)unit);
                        } else if (((Function)unit).FunctionType ==FunctionType.End) {
                            if (function_stack.Count==0)
                                throw new SynatxErrorException(position, ("No more \"function\" head can be matched with \"endfunction\" label"), RefExecutor);
                            if (function_stack.Peek().EndLine >= 0)
                                throw new SynatxErrorException(position, ("duplicate \"endfcuntion\" in current function statement"), RefExecutor);
                            function_stack.Peek().EndLine = position;
                            function = function_stack.Pop();
                            function_table.Add(function.FunctionName, function);
                        }
                    }
                } else if (unit.UnitType == UnitType.Symbol) {
                    if (((Symbol)unit).SymbolType == SymbolType.ConditionBranch) {
                        //Condition Branch
                        if (((Condition)unit).ConditionType == ConditionType.Else) {
                            if (if_stack.Count==0)
                                throw new SynatxErrorException(position, ("No more \"if\" head can be matched with \"else\" label"), RefExecutor);
                            if (if_stack.Peek().ElseLine >= 0)
                                throw new SynatxErrorException(position, ("duplicate \"else\" in current if branch"), RefExecutor);
                            if_stack.Peek().ElseLine = position;
                        } else if (((Condition)unit).ConditionType == ConditionType.EndIf) {
                            if (if_stack.Count==0)
                                throw new SynatxErrorException(position, ("No more \"if\" head can be match with \"else\" label"), RefExecutor);
                            if_stack.Peek().EndLine = position;
                            if_stack.Pop();
                        } else if (((Condition)unit).ConditionType == ConditionType.If) {
                            if_stack.Push((If)unit);
                        }
                    } else if (((Symbol)unit).SymbolType == SymbolType.LoopBranch) {
                        if (((Loop)unit).LoopType == LoopType.LoopBegin) {
                            loop_stack.Push((LoopBegin)unit);
                        } else {
                            if (loop_stack.Count==0)
                                throw new SynatxErrorException(position, ("No more \"loop\" head can be matched with \"endloop\" label"), RefExecutor);
                            if (((Loop)unit).LoopType ==LoopType.Endloop) {
                                loop_stack.Peek().EndLine = position;
                                ((Loop)unit).ReferenceLoop = loop_stack.Peek();
                                loop_stack.Pop();
                            } else if (((Loop)unit).LoopType == LoopType.Break) {
                                if (((Loop)unit).ReferenceLoop != null)
                                    throw new SynatxErrorException(position, ("duplicate \"begin_line\" in current loop branch"), RefExecutor);
                                ((Loop)unit).ReferenceLoop = loop_stack.Peek();
                            } else if (((Loop)unit).LoopType == LoopType.Continue) {
                                if (((Loop)unit).ReferenceLoop != null)
                                    throw new SynatxErrorException(position, ("duplicate \"begin_line\" in current loop branch"), RefExecutor);
                                ((Loop)unit).ReferenceLoop = loop_stack.Peek();
                            } else if (((Symbol)unit).SymbolType== SymbolType.LoopBranch) {

                            } else
                                throw new SynatxErrorException(position, ("unknown loop branch type"), RefExecutor);
                        }
                    } else if (((Symbol)unit).SymbolType == SymbolType.Label) {
                        //// TODO: 2016/11/1
                    } else
                        throw new SynatxErrorException(position, ("unknown symbol type"), RefExecutor);
                } else
                    throw new SynatxErrorException(position, (String.Format("unknown unit type {0}", unit.GetType().Name)), RefExecutor);
            }
            return;
        }
    }

    public class SynatxErrorException : Exception
    {
        public SynatxErrorException(int line,string cause,Executor executor) : base(
            $"SynatxError {executor.GetPackageName}, line {line}:{cause}"
            )
        { }
    }
}
