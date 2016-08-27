import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

class BaseCalculator{
	private abstract static class Node{
		protected String rawValueText;
		public enum NodeType{
			Digit,
			Symbol,
			Unknown
		}
		public NodeType GetNodeType(){return NodeType.Unknown;}
		public void Check()throws Exception{}
	}
	private static class SymbolNode extends Node
	{
		public String rawText;
		public enum SymbolType{
			Add,//+
			Subtract,//-
			Multiply,//*
			Divide,///
			Power,//^
			Mod,//%
			Bracket_Left,//(
			Bracket_Right,//)
			Unknown
			}

		@Override
		public String toString()
		{
			return rawValueText;
		}

		private SymbolType symbol_type=SymbolType.Unknown;
		public SymbolType GetSymbolType(){return symbol_type;}

		public SymbolNode(String op){
			rawValueText=op;
			OperationPriority=new HashMap<SymbolType,Float>();
			OperationPriorityInit();
			switch(op.charAt(0)){
				case '+':symbol_type=SymbolType.Add;break;
				case '-':symbol_type=SymbolType.Subtract;break;
				case '*':symbol_type=SymbolType.Multiply;break;
				case '/':symbol_type=SymbolType.Divide;break;
				case '^':symbol_type=SymbolType.Power;break;
				case '%':symbol_type=SymbolType.Mod;break;
				case '(':symbol_type=SymbolType.Bracket_Left;break;
				case ')':symbol_type=SymbolType.Bracket_Right;break;
				default:symbol_type=SymbolType.Unknown;
			}
		}
		@Override
		public BaseCalculator.Node.NodeType GetNodeType()
		{return Node.NodeType.Symbol;}
		@Override
		public void Check() throws Exception
		{
			if(symbol_type==SymbolType.Unknown)
				throw new Exception("Unknown symbol like "+rawText);
		}

		private HashMap<SymbolType,Float> OperationPriority=new HashMap<SymbolType,Float>();
		private void OperationPriorityInit(){
			setOperationPriority(SymbolType.Add,1);
			setOperationPriority(SymbolType.Subtract,1);
			setOperationPriority(SymbolType.Divide,3);
			setOperationPriority(SymbolType.Multiply,3);
			setOperationPriority(SymbolType.Mod,3);
			setOperationPriority(SymbolType.Power,9);
			setOperationPriority(SymbolType.Bracket_Left,12);
			setOperationPriority(SymbolType.Bracket_Right,12);
		}
		private void setOperationPriority(SymbolType type,float level){
			OperationPriority.put(type,level);
		}
		public int CompareToOperationPriority(SymbolNode node){
			float result=this.OperationPriority.get(symbol_type)-node.OperationPriority.get(node.symbol_type);
			if(result==0)
				return 0;
			if(result>0)
				return 1;
			return -1;
		}

	}
	private static class DigitNode extends Node{
		String rawText;
		enum DigitType{
			Float,
			Integer,
			Unknown
			}

		@Override
		public String toString()
		{
			return rawText;
		}
		DigitType digit_type=DigitType.Unknown;
		public DigitNode(String digit){
			rawText=digit;
			if(isFloat(digit))
				digit_type=DigitType.Float;
				else
					if(isInteger(digit))
						digit_type=DigitType.Integer;
						else
							digit_type=DigitType.Unknown;
		}
		@Override
		public BaseCalculator.Node.NodeType GetNodeType(){return Node.NodeType.Digit;}

		public boolean isInteger(String text){
			try{
				return isDigit(text)&&Integer.decode(text).toString().compareTo(text)==0;
				}catch(Exception e){return false;}
		}
		public boolean isFloat(){return isFloat(rawText);}
		public boolean isFloat(String text){
			return (!isInteger(text))&&text.contains(Character.toString('.'));
		}

		public Double toValue(){return Double.parseDouble(rawText);}

		@Override
		public void Check() throws Exception
		{
			if(digit_type==DigitType.Unknown)
				throw new Exception("Unknown symbol like "+rawText);
		}

		public int toInteger(){return Integer.parseInt(rawText);}
		public float toFloat(){return Float.parseFloat(rawText);}
	}

	class InvaildOperationException extends Exception{
		private int position;
		private String causeMsg;
		public InvaildOperationException(int pos, String msg)
		{position = pos;causeMsg = msg;}
		@Override
		public String getMessage(){return String.format("In position %d , %s",position,causeMsg);}
	}
	public static interface RequestVariableValue{
		public String onRequestVariableValue(String name);
	}

	private ArrayList<Node> NodeChain=new ArrayList<Node>();
	private ArrayList<Node> BSEChain;
	private String specialSymbol="+-/*%^()";
	public String Solve(String expression)throws InvaildOperationException,Exception{
		Reset();
		String sub_expression=new String();
		char c;
		for(int position=0;position<expression.length();position++){
			c=expression.charAt(position);
			if(specialSymbol.contains(Character.toString(c))){
				if(sub_expression.length()!=0)
					addDigit(position-1-sub_expression.length(),sub_expression);
				NodeChain.add(new SymbolNode(Character.toString(c)));
				sub_expression=new String();
			}else{
				sub_expression+=c;
			}
		}
		if(!sub_expression.isEmpty())
			addDigit(expression.length()-sub_expression.length(),sub_expression);
		CheckExpressionChain();
		try{
			ConverToBackSymbolExpression();
		}catch(Exception e){
			throw new InvaildOperationException(-1,e.getMessage());}
		return ExecuteBSE();
	}

	private  void CheckExpressionChain()throws InvaildOperationException{
		try{
			if(NodeChain.size()==0)
				throw new Exception("no any Node in chain list.");
			if(NodeChain.size()==1&&(NodeChain.get(0).GetNodeType()==Node.NodeType.Symbol))
				throw new Exception("invalid chain list because size is 1 and  the first of node of type is Symbol.");
			Node previewNode=null;
			for(Node node:NodeChain){
				node.Check();
				/*鐣欏潙
				if(previewNode!=null)
					if(previewNode.GetNodeType()==node.GetNodeType())
						throw new Exception("two same symbol is close");*/
				previewNode=node;
			}
			if(previewNode.GetNodeType()==Node.NodeType.Symbol?(((SymbolNode)previewNode).GetSymbolType()!=SymbolNode.SymbolType.Bracket_Right):false)
				throw new Exception("the end of chain list is symbol");
		}catch(Exception e){
			throw new InvaildOperationException(-1,e.getMessage());
		}
	}

	private void ConverToBackSymbolExpression()throws Exception{
		ArrayList<Node> result_list=new ArrayList<Node>();
		Stack<SymbolNode> operation_stack=new Stack<SymbolNode>();
		SymbolNode symbol=null;
		for(Node node:NodeChain){
			if(node.GetNodeType()==Node.NodeType.Digit)
				result_list.add(node);
				else{
					if(operation_stack.isEmpty())
						operation_stack.push((SymbolNode)node);
							else{
								if(((SymbolNode)node).symbol_type!=SymbolNode.SymbolType.Bracket_Right){
								symbol=operation_stack.peek();
								while((symbol==null?false:(symbol.symbol_type!=SymbolNode.SymbolType.Bracket_Left&&symbol.CompareToOperationPriority((SymbolNode)node)>=0))){
									result_list.add(operation_stack.pop());
									symbol=operation_stack.size()!=0?operation_stack.peek():null;
								}
								operation_stack.push((SymbolNode)node);
							}else{
								symbol=operation_stack.peek();
								while(true){
									if(operation_stack.size()==0)
										throw new Exception("no left bracket and take a pair of close bracket.");
									if(symbol.symbol_type==SymbolNode.SymbolType.Bracket_Left){
										operation_stack.pop();
										break;
									}
									result_list.add(operation_stack.pop());
									symbol=operation_stack.peek();
								}
							}
						}
				}
		}
		while(!operation_stack.isEmpty()){
			result_list.add(operation_stack.pop());
		}
		Node node;
		for(int i=0;i<result_list.size();i++){
			node=result_list.get(i);
			if(node.GetNodeType()==Node.NodeType.Symbol)
				if(((SymbolNode)node).symbol_type==SymbolNode.SymbolType.Bracket_Left)
					result_list.remove(node);
		}
		BSEChain=result_list;
	}

	private String ExecuteBSE()throws InvaildOperationException,Exception{
		if(BSEChain.size()==1)
			if(BSEChain.get(0).GetNodeType()==Node.NodeType.Digit)
				return String.valueOf((((DigitNode)BSEChain.get(0)).isFloat()?((DigitNode)BSEChain.get(0)).toFloat():((DigitNode)BSEChain.get(0)).toInteger()));
		Stack<DigitNode> digit_stack=new Stack<DigitNode>();
		DigitNode digit_a,digit_b,digit_result;
		SymbolNode operator;
		try{
			for(Node node:BSEChain){
				if(node.GetNodeType()==Node.NodeType.Symbol){
					operator=(SymbolNode)node;
					digit_b=digit_stack.pop();
					digit_a=digit_stack.pop();
					digit_result=Execute(digit_a,operator,digit_b);
					digit_stack.push(digit_result);
				}else{
					if(node.GetNodeType()==Node.NodeType.Digit){
						digit_stack.push((DigitNode)node);
					}else
						throw new Exception("Unknown Node");
				}
			}
		}catch(Exception e){
			throw new InvaildOperationException(-1,e.getMessage());
			}
		return digit_stack.pop().rawText;
	}

	private DigitNode Execute(DigitNode a,SymbolNode op,DigitNode b)throws Exception{
		if(op.symbol_type==SymbolNode.SymbolType.Add)
			return new DigitNode(String.valueOf(a.toValue()+b.toValue()));
		if(op.symbol_type==SymbolNode.SymbolType.Multiply)
			return new DigitNode(String.valueOf(a.toValue()*b.toValue()));
		if(op.symbol_type==SymbolNode.SymbolType.Subtract)
			return new DigitNode(String.valueOf(a.toValue()-b.toValue()));
		if(op.symbol_type==SymbolNode.SymbolType.Mod)
			return new DigitNode(String.valueOf((a.isFloat()?a.toFloat():a.toInteger())%(b.isFloat()?b.toFloat():b.toInteger())));
		if(op.symbol_type==SymbolNode.SymbolType.Divide)
			return new DigitNode(String.valueOf((a.isFloat()?a.toFloat():a.toInteger())/(b.isFloat()?b.toFloat():b.toInteger())));
		if(op.symbol_type==SymbolNode.SymbolType.Power)
			return new DigitNode(String.valueOf(Math.pow((a.isFloat()?a.toFloat():a.toInteger()),(b.isFloat()?b.toFloat():b.toInteger()))));
		if(op.symbol_type==SymbolNode.SymbolType.Unknown)
			throw new Exception("Unknown Operator");

		return null;
	}

	private void addDigit(int position,String sub_expression)throws InvaildOperationException{
		if(isDigit(sub_expression))
			NodeChain.add(new DigitNode(sub_expression));
		else
		if(isValidVariable(sub_expression))
			NodeChain.add(new DigitNode(requestVariable(sub_expression)));
		else
			throw new InvaildOperationException(position,String.format("%s is invalid variable in table",sub_expression));
	}

	private RequestVariableValue requestVariableValue;
	public void setRequestVariableValue(RequestVariableValue r){requestVariableValue=r;}
	private String requestVariable(String name){return requestVariableValue.onRequestVariableValue(name);}

	public static boolean isDigit(String expression){
		if(expression.isEmpty())
			return false;
		for(char c:expression.toCharArray()){
			if(!(Character.isDigit(c)||(Character.compare(c,'.')==0)))
				return false;
		}
		return true;
	}

	public static boolean isValidVariable(String expression){
		if(expression.isEmpty())
			return false;
		if(isDigit(Character.toString(expression.charAt(0))))
			return false;
		if(isDigit(expression))
			return false;
		for(char c: expression.toCharArray())
			if(!(Character.isLetterOrDigit(c)||(Character.compare(c,'_')==0)))
				return false;
		return true;
	}

	public void Reset(){
		if (BSEChain!=null)
		    BSEChain.clear();
        if (NodeChain!=null)
            NodeChain.clear();
	}
}

