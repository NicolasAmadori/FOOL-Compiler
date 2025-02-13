package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {
	
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) printNode(n);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	// OBJECT-ORIENTED EXTENSION

	@Override
	public Void visitNode(ClassNode n) {
		if (print) printNode(n);

		List<TypeNode> fieldTypes = n.fields.stream().map(FieldNode::getType).toList();

		List<ArrowTypeNode> methodTypes = new ArrayList<>();
		for (MethodNode m : n.methods) {
			List<TypeNode> paramTypes = m.parlist.stream().map(ParNode::getType).toList();
			methodTypes.add(new ArrowTypeNode(paramTypes, m.retType));
		}

		ClassTypeNode classType = new ClassTypeNode(fieldTypes, methodTypes);
		STentry entry = new STentry(nestingLevel, classType, decOffset--);

		if (symTable.get(nestingLevel).put(n.id, entry) != null) {
			System.out.println("Class " + n.id + " already declared at line " + n.getLine());
			stErrors++;
		}

		nestingLevel++;

		//Creazione dello scope vuoto, riempito poi da visit(MethodNode)
		symTable.add(new HashMap<>());

		int prevDeclOffset = decOffset;
		decOffset = -1;

		for (FieldNode f : n.fields) visit(f);

		decOffset = 0;

		for (MethodNode m : n.methods) visit(m);

		decOffset = prevDeclOffset;

		nestingLevel--;

		return null;
	}


	@Override
	public Void visitNode(FieldNode n) {
		if (print) printNode(n);

		if (symTable.get(nestingLevel).containsKey(n.id)) { //Controllo che il campo non esista già
			System.out.println("Errore: Campo " + n.id + " già dichiarato.");
			stErrors++;
			return null;
		}

		symTable.get(nestingLevel).put(n.id, new STentry(nestingLevel, n.getType(), decOffset--));

		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);

		Map<String, STentry> classTable = symTable.get(nestingLevel);

		if (classTable.containsKey(n.id)) {
			System.out.println("Errore: Metodo " + n.id + " già dichiarato.");
			stErrors++;
			return null;
		}

		List<TypeNode> paramTypes = n.parlist.stream().map(ParNode::getType).toList();

		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(paramTypes, n.retType), decOffset++);
		classTable.put(n.id, entry);

		nestingLevel++;
		Map<String, STentry> methodTable = new HashMap<>();
		symTable.add(methodTable);

		int paramOffset = 1;
		for (ParNode par : n.parlist) {
			if (methodTable.put(par.id, new STentry(nestingLevel, par.getType(), paramOffset++)) != null) {
				System.out.println("Errore: Parametro " + par.id + " già dichiarato.");
				stErrors++;
			}
		}

		int prevDecOffset = decOffset;
		decOffset = -2; //Valore di partenza

		for (Node dec : n.declist) visit(dec);

		visit(n.exp);

		symTable.remove(nestingLevel--); //TODO: Va probabilmente salvato da qualche parte
		decOffset = prevDecOffset;

		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.objectId);
		if (entry == null) {
			System.out.println("Class id " + n.objectId + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {

			ClassTypeNode classType = (ClassTypeNode) entry.type;
			ArrowTypeNode methodType = null;


//			for(Map.Entry<String, ArrowTypeNode> method : classType.allMethods().entrySet()) {
//				if (method.)
//			}
			for (Map.Entry<String, ArrowTypeNode> method : classType.allMethods().entrySet()) {
				if (method.getKey().equals(n.methodId)) {
					methodType = method.getValue();
					break;
				}
			}

			if (methodType == null) {
				System.out.println("Method id " + n.methodId + " at line "+ n.getLine() + " not declared");
				stErrors++;
			} else {
				n.entry = entry;
				n.nl = nestingLevel;
			}
			for (Node arg : n.arglist) visit(arg);
		}
		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public Void visitNode(ClassTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public Void visitNode(RefTypeNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public Void visitNode(EmptyTypeNode n) {
		if (print) printNode(n);
		return null;
	}
}
