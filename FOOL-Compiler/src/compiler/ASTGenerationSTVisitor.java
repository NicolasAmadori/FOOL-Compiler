package compiler;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.*;
import static compiler.lib.FOOLlib.*;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

	String indent;
    public boolean print;
	
    ASTGenerationSTVisitor() {}    
    ASTGenerationSTVisitor(boolean debug) { print=debug; }
        
    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix="";        
    	Class<?> ctxClass=ctx.getClass(), parentClass=ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
        	prefix=lowerizeFirstChar(extractCtxName(parentClass.getName()))+": production #";
    	System.out.println(indent+prefix+lowerizeFirstChar(extractCtxName(ctxClass.getName())));
    }
        
    @Override
	public Node visit(ParseTree t) {
    	if (t==null) return null;
        String temp=indent;
        indent=(indent==null)?"":indent+"  ";
        Node result = super.visit(t);
        indent=temp;
        return result; 
	}

	@Override
	public Node visitProg(ProgContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.progbody());
	}

	@Override
	public Node visitLetInProg(LetInProgContext c) {
		if (print) printVarAndProdName(c);
		List<DecNode> allDecList = new ArrayList<>();
		for (CldecContext clDec : c.cldec()) allDecList.add((DecNode) visit(clDec));

		for (DecContext dec : c.dec()) allDecList.add((DecNode) visit(dec));

		return new ProgLetInNode(allDecList, visit(c.exp()));
	}

	@Override
	public Node visitNoDecProg(NoDecProgContext c) {
		if (print) printVarAndProdName(c);
		return new ProgNode(visit(c.exp()));
	}

	@Override
	public Node visitTimesDiv(TimesDivContext c) {
		if (print) printVarAndProdName(c);

		Node n;
		if (c.TIMES() != null) {
			n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.TIMES().getSymbol().getLine());		// setLine added
		} else {
			n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.DIV().getSymbol().getLine());		// setLine added
		}
        return n;		
	}

	@Override
	public Node visitPlusMinus(PlusMinusContext c) {
		if (print) printVarAndProdName(c);
		Node n;
		if (c.PLUS() != null) {
			n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.PLUS().getSymbol().getLine());
		} else {
			n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.MINUS().getSymbol().getLine());
		}
        return n;		
	}

	@Override
	public Node visitAndOr(AndOrContext c) {
		if (print) printVarAndProdName(c);

		Node n;
		if (c.AND() != null) {
			n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.AND().getSymbol().getLine());
		} else {
			n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.OR().getSymbol().getLine());
		}

		return n;
	}

	@Override
	public Node visitNot(NotContext c) {
		if (print) printVarAndProdName(c);

		Node n = new NotNode(visit(c.exp()));
		n.setLine(c.NOT().getSymbol().getLine());

		return n;
	}

	@Override
	public Node visitComp(CompContext c) {
		if (print) printVarAndProdName(c);
		Node n;
		if (c.EQ() != null) {
			n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.EQ().getSymbol().getLine());
		} else if (c.GE() != null) {
			n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.GE().getSymbol().getLine());
		} else {
			n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
			n.setLine(c.LE().getSymbol().getLine());
		}
        return n;		
	}

	@Override
	public Node visitVardec(VardecContext c) {
		if (print) printVarAndProdName(c);
		Node n = null;
		if (c.ID()!=null) { //non-incomplete ST
			n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
			n.setLine(c.VAR().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitFundec(FundecContext c) {
		if (print) printVarAndProdName(c);
		List<ParNode> parList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) {
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			parList.add(p);
		}
		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
		Node n = null;
		if (!c.ID().isEmpty()) { //non-incomplete ST
			n = new FunNode(c.ID(0).getText(),(TypeNode)visit(c.type(0)),parList,decList,visit(c.exp()));
			n.setLine(c.FUN().getSymbol().getLine());
		}
        return n;
	}

	@Override
	public Node visitIntType(IntTypeContext c) {
		if (print) printVarAndProdName(c);
		return new IntTypeNode();
	}

	@Override
	public Node visitBoolType(BoolTypeContext c) {
		if (print) printVarAndProdName(c);
		return new BoolTypeNode();
	}

	@Override
	public Node visitInteger(IntegerContext c) {
		if (print) printVarAndProdName(c);
		int v = Integer.parseInt(c.NUM().getText());
		return new IntNode(c.MINUS()==null?v:-v);
	}

	@Override
	public Node visitTrue(TrueContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(true);
	}

	@Override
	public Node visitFalse(FalseContext c) {
		if (print) printVarAndProdName(c);
		return new BoolNode(false);
	}

	@Override
	public Node visitIf(IfContext c) {
		if (print) printVarAndProdName(c);
		Node ifNode = visit(c.exp(0));
		Node thenNode = visit(c.exp(1));
		Node elseNode = visit(c.exp(2));
		Node n = new IfNode(ifNode, thenNode, elseNode);
		n.setLine(c.IF().getSymbol().getLine());			
        return n;		
	}

	@Override
	public Node visitPrint(PrintContext c) {
		if (print) printVarAndProdName(c);
		return new PrintNode(visit(c.exp()));
	}

	@Override
	public Node visitPars(ParsContext c) {
		if (print) printVarAndProdName(c);
		return visit(c.exp());
	}

	@Override
	public Node visitId(IdContext c) {
		if (print) printVarAndProdName(c);
		Node n = new IdNode(c.ID().getText());
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitCall(CallContext c) {
		if (print) printVarAndProdName(c);		
		List<Node> arglist = new ArrayList<>();
		for (ExpContext arg : c.exp()) arglist.add(visit(arg));
		Node n = new CallNode(c.ID().getText(), arglist);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	// OBJECT-ORIENTED EXTENSION

	@Override
	public Node visitIdType(IdTypeContext c) {
		if (print) printVarAndProdName(c);
		RefTypeNode rtNode = new RefTypeNode(c.ID().getText());
		rtNode.setLine(c.ID().getSymbol().getLine());
		return rtNode;
	}

	@Override
	public Node visitCldec(CldecContext c) {
		if (print) printVarAndProdName(c);

		String className = c.ID(0).getText();

		//Lettura classe padre (se presente)
		String superClass = null;
		int startingIndex = 1;
		if (c.EXTENDS() != null){
			superClass = c.ID(1).getText();
			startingIndex = 2;
		}

		//Lettura dei fields con id e tipi
		List<FieldNode> fieldList = new ArrayList<>();
		for (int i = startingIndex; i < c.ID().size(); i++) {
			FieldNode f = new FieldNode(
					c.ID(i).getText(),
					(TypeNode) visit(c.type(i - startingIndex))
			);
			f.setLine(c.ID(i).getSymbol().getLine());
			fieldList.add(f);
		}
		fieldList.get(0).getType();

		//Lettura dei metodi (a partire dal loro context)
		List<MethodNode> methodList = new ArrayList<>();
		for (MethdecContext mC : c.methdec()) methodList.add((MethodNode) visit(mC));

		List<TypeNode> fieldTypes = fieldList.stream().map(DecNode::getType).toList();
		List<ArrowTypeNode> methodTypes = methodList.stream()
				.map(m -> new ArrowTypeNode(m.parlist.stream()
						.map(DecNode::getType)
						.toList(),
					m.retType))
				.toList();

		ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(fieldTypes), new ArrayList<>(methodTypes));

		ClassNode classNode = new ClassNode(className, superClass, fieldList, methodList, classType);
		classNode.setLine(c.ID(0).getSymbol().getLine());
		return classNode;
	}

	@Override
	public Node visitMethdec(MethdecContext c) {
		if (print) printVarAndProdName(c);

		String methodName = c.ID(0).getText();
		TypeNode returnType = (TypeNode) visit(c.type(0));

		List<ParNode> paramList = new ArrayList<>();
		for (int i = 1; i < c.ID().size(); i++) {
			ParNode p = new ParNode(c.ID(i).getText(),(TypeNode) visit(c.type(i)));
			p.setLine(c.ID(i).getSymbol().getLine());
			paramList.add(p);
		}

		List<DecNode> decList = new ArrayList<>();
		for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));

		Node body = visit(c.exp());
		MethodNode m = new MethodNode(methodName, returnType, paramList, decList, body);
		m.setLine(c.ID(0).getSymbol().getLine());
		return m;
	}

	@Override
	public Node visitNew(NewContext c) {
		if (print) printVarAndProdName(c);

		String className = c.ID().getText();

		List<Node> expList = new ArrayList<>();
		for (ExpContext exp : c.exp()) expList.add(visit(exp));

		NewNode n = new NewNode(className, expList);
		n.setLine(c.ID().getSymbol().getLine());
		return n;
	}

	@Override
	public Node visitNull(NullContext c) {
		if (print) printVarAndProdName(c);
		EmptyNode e = new EmptyNode();
		e.setLine(c.NULL().getSymbol().getLine());
		return e;
	}

	@Override
	public Node visitDotCall(DotCallContext c) {
		if (print) printVarAndProdName(c);

		String objectName = c.ID(0).getText();
		String methodName = c.ID(1).getText();

		List<Node> expList = new ArrayList<>();
		for (ExpContext exp : c.exp()) expList.add(visit(exp));

		ClassCallNode classCallNode =  new ClassCallNode(objectName, methodName, expList);
		classCallNode.setLine(c.ID(0).getSymbol().getLine());
		return classCallNode;
	}
}
