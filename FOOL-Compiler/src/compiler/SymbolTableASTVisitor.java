package compiler;

import java.util.*;
import java.util.stream.Collectors;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {
	
	private List<Map<String, STentry>> symTable = new ArrayList<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	Map<String, Map<String, STentry>> classTable = new HashMap<>();

	private final int METHODS_STARTING_OFFSET = 0;
	private final int METHODS_OFFSET_DELTA = 1;

	private final int FIELDS_STARTING_OFFSET = -1;
	private final int FIELDS_OFFSET_DELTA = -1;

	private final int PARAMETERS_STARTING_OFFSET = 1;
	private final int PARAMETERS_OFFSET_DELTA = 1;

	private final int DECLARATIONS_STARTING_OFFSET = -2;
	private final int DECLARATIONS_OFFSET_DELTA = -1;

	private final int FUNCTIONS_STARTING_OFFSET = 0;
	private final int FUNCTIONS_OFFSET_DELTA = 1;



	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean incomplExc) {super(incomplExc);} // enables print for debugging
	SymbolTableASTVisitor(boolean incomplExc, boolean debug) {super(incomplExc, debug);}

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

		ClassTypeNode classType;
		final Map<String, STentry> virtualTable = new HashMap<>();
		int fieldsOffset = FIELDS_STARTING_OFFSET;

		Map<String, STentry> superClassvirtualTable = null;
		if(n.superClassId != null){
			STentry superClassEntry = symTable.get(nestingLevel).get(n.superClassId);
			n.superEntry = superClassEntry;
			ClassTypeNode superClassType = (ClassTypeNode) superClassEntry.type;
			ArrayList<TypeNode> allFieldsCopy = new ArrayList<>(superClassType.allFields);
			ArrayList<ArrowTypeNode> allMethodsCopy = new ArrayList<>(superClassType.allMethods);

			classType = new ClassTypeNode(allFieldsCopy, allMethodsCopy);

			superClassvirtualTable = classTable.get(n.superClassId);
			if(superClassvirtualTable == null){
				System.out.println("Error: Super class " + n.superClassId + " of the class " + n.id + " do not exist.");
				stErrors++;
			} else {
                virtualTable.putAll(superClassvirtualTable);
			}
			fieldsOffset = -classType.allFields.size()-1;
		} else {
			classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
		}

		STentry entry = new STentry(nestingLevel, classType, decOffset--);

		if (symTable.get(nestingLevel).put(n.id, entry) != null) {
			System.out.println("Class " + n.id + " already declared at line " + n.getLine());
			stErrors++;
		}

		nestingLevel++;
		classTable.put(n.id, virtualTable); //Aggiunta definitiva in classTable
		symTable.add(virtualTable); //Aggiunta temporanea in symTable

		List<String> classFieldsNames = new ArrayList<>();
		for (FieldNode f : n.fields) {
			if (classFieldsNames.contains(f.id)) {
				System.out.println("Error: Field " + f.id + " already declared.");
				stErrors++;
			} else {
				classFieldsNames.add(f.id);
			}

			STentry fieldEntry;
			if(virtualTable.containsKey(f.id)){
				STentry oldEntry = virtualTable.get(f.id);
				if(oldEntry.type instanceof ArrowTypeNode){
					System.out.println("Error: Impossible to create the field  " + f.id + " because it exists a method in the superclass " + n.superClassId + " with the same name.");
					stErrors++;
				}
				fieldEntry = new STentry(nestingLevel, f.getType(), oldEntry.offset);
				classType.allFields.set(- fieldEntry.offset - 1, f.getType());
			} else {
				fieldEntry = new STentry(nestingLevel, f.getType(), fieldsOffset);
				classType.allFields.add(- fieldEntry.offset - 1, f.getType());
				fieldsOffset += FIELDS_OFFSET_DELTA;
			}

			virtualTable.put(f.id, fieldEntry);

			visit(f);
		}

		int prevDeclOffset = decOffset;
		decOffset = (superClassvirtualTable != null) ? classType.allMethods.size() : METHODS_STARTING_OFFSET;


		List<String> classMethodsNames = new ArrayList<>();
		for (MethodNode m : n.methods) {
			if (classMethodsNames.contains(m.id)) {
				System.out.println("Error: Method " + m.id + " already declared in the class scope " + m.id + ".");
				stErrors++;
			} else {
				classMethodsNames.add(m.id);
			}

			final STentry oldMethodEntry = virtualTable.get(m.id);

			visit(m);

			if(oldMethodEntry != null){
				if(!(oldMethodEntry.type instanceof ArrowTypeNode)){
					System.out.println("Error: It is impossible to create the method " + m.id + " because it exists a field in the superclass " + n.superClassId + " with the same name.");
					stErrors++;
				}
				classType.allMethods.set(oldMethodEntry.offset, (ArrowTypeNode) oldMethodEntry.type);
			} else {
				final STentry newMethodEntry = virtualTable.get(m.id);
				classType.allMethods.add(decOffset, (ArrowTypeNode) newMethodEntry.type);
				decOffset += METHODS_OFFSET_DELTA;
			}
		}

		decOffset = prevDeclOffset;

		symTable.remove(nestingLevel--);
		ClassTypeNode nType = (ClassTypeNode) n.getType();
		nType.allFields.clear();
		nType.allFields.addAll(classType.allFields);
		nType.allMethods.clear();
		nType.allMethods.addAll(classType.allMethods);
		return null;
	}


	@Override
	public Void visitNode(FieldNode n) {
		if (print) printNode(n);
		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);

		Map<String, STentry> virtualTable = symTable.get(nestingLevel);

		List<TypeNode> paramTypes = n.parlist.stream().map(ParNode::getType).toList();

		STentry methodEntry;
		if (virtualTable.containsKey(n.id)) {
			STentry oldEntry = virtualTable.get(n.id);
			methodEntry = new STentry(nestingLevel, new ArrowTypeNode(paramTypes, n.retType), oldEntry.offset);
		} else {
			methodEntry = new STentry(nestingLevel, new ArrowTypeNode(paramTypes, n.retType), decOffset);
		}
		virtualTable.put(n.id, methodEntry);
		n.offset = methodEntry.offset;

		nestingLevel++;
		Map<String, STentry> methodTable = new HashMap<>();
		symTable.add(methodTable);

		int paramOffset = PARAMETERS_STARTING_OFFSET;
		for (ParNode par : n.parlist) {
			if (methodTable.put(par.id, new STentry(nestingLevel, par.getType(), paramOffset)) != null) {
				paramOffset += PARAMETERS_OFFSET_DELTA;
				System.out.println("Error: Parameter " + par.id + " already declared.");
				stErrors++;
			}
		}

		int prevDecOffset = decOffset;
		decOffset = DECLARATIONS_STARTING_OFFSET; //Valore di partenza

		n.declist.forEach(this::visit);

		visit(n.exp);

		symTable.remove(nestingLevel--);
		decOffset = prevDecOffset;

		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.objectId);
		if (entry == null) {
			System.out.println("Class object with id " + n.objectId + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else if (entry.type instanceof RefTypeNode) {
			n.nl = nestingLevel;
			n.entry = entry;
			Map<String, STentry> virtualTable = classTable.get(((RefTypeNode) entry.type).id);
			if (virtualTable.containsKey(n.methodId) && virtualTable.get(n.methodId).type instanceof ArrowTypeNode) {
				n.methodEntry = virtualTable.get(n.methodId);
			}
			else {
				System.out.println("Method id " + n.methodId + " at line "+ n.getLine() + " not declared");
				stErrors++;
			}
		}
		n.arglist.forEach(this::visit);
		return null;
	}

	@Override
	public Void visitNode(NewNode n) {
		if (print) printNode(n);
		if(!classTable.containsKey(n.classId)) {
			System.out.println("Class " + n.classId + " at line "+ n.getLine() + " not declared");
			stErrors++;
		}
		n.entry = symTable.getFirst().get(n.classId);
		n.arglist.forEach(this::visit);
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
