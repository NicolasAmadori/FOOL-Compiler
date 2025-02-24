package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

	public static final Map<String, String> superType = new HashMap<>();

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	// aggiunto la condizione che ogni EmptyTypeNode sia sottotipo di ogni RefTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {

		boolean isSubClass = false;
		if (a instanceof RefTypeNode refA && b instanceof RefTypeNode refB){
            String superTypeId = superType.get(refA.id);
			while(superTypeId != null){
				if (superTypeId.equals(refB.id)){
					isSubClass = true;
					break;
				}
				superTypeId = superType.get(superTypeId);
			}
		}

		boolean isSubMethod = false;

		if (a instanceof ArrowTypeNode methodA && b instanceof ArrowTypeNode methodB) {
			if (methodA.parlist.size() == methodB.parlist.size() && isSubtype(methodA.ret, methodB.ret)) {
				isSubMethod = true; //Temporanea

				for (int i = 0; i < methodA.parlist.size(); i++) {
					if (!isSubtype(methodB.parlist.get(i), methodA.parlist.get(i))) {
						isSubMethod = false; // Violazione controvarianza
						break;
					}
				}
			}
		}

		return
				a.getClass().equals(b.getClass()) ||
				((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				(a instanceof EmptyTypeNode && b instanceof RefTypeNode) ||
				isSubClass || isSubMethod;

	}

}
