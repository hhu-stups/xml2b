package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.node.*;

import java.util.ArrayList;
import java.util.List;

public class ASTUtils {
	public static AIdentifierExpression createIdentifier(String name) {
		ArrayList<TIdentifierLiteral> list = new ArrayList<>();
		list.add(new TIdentifierLiteral(name));
		return new AIdentifierExpression(list);
	}

	public static List<PExpression> createIdentifierList(String... strings) {
		ArrayList<PExpression> list = new ArrayList<>();
		for (String string : strings) {
			list.add(createIdentifier(string));
		}
		return list;
	}

	public static AIntegerExpression createInteger(int name) {
		return createInteger(String.valueOf(name));
	}

	public static AIntegerExpression createInteger(String name) {
		return new AIntegerExpression(new TIntegerLiteral(name));
	}

	public static List<TIdentifierLiteral> createTIdentifierLiteral(String name) {
		List<TIdentifierLiteral> list = new ArrayList<>();
		TIdentifierLiteral tid = new TIdentifierLiteral(name);
		list.add(tid);
		return list;
	}
}