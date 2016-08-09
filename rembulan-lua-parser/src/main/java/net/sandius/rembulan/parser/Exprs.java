/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.*;

import java.util.List;

public abstract class Exprs {

	private Exprs() {
		// not to be instantiated
	}

	private static Attributes attr(SourceInfo src) {
		return Attributes.of(src);
	}

	public static LiteralExpr literal(SourceInfo src, Literal value) {
		return new LiteralExpr(attr(src), value);
	}

	public static FunctionDefExpr functionDef(SourceInfo src, FunctionDefExpr.Params params, Block block) {
		return new FunctionDefExpr(attr(src), params, block);
	}

	public static TableConstructorExpr tableConstructor(SourceInfo src, List<TableConstructorExpr.FieldInitialiser> fields) {
		return new TableConstructorExpr(attr(src), fields);
	}

	public static TableConstructorExpr.FieldInitialiser fieldInitialiser(Expr keyExpr, Expr valueExpr) {
		return new TableConstructorExpr.FieldInitialiser(keyExpr, valueExpr);
	}

	public static IndexExpr index(SourceInfo src, Expr object, Expr key) {
		return new IndexExpr(attr(src), object, key);
	}

	public static VarExpr var(SourceInfo src, Name name) {
		return new VarExpr(attr(src), name);
	}

	public static VarargsExpr varargs(SourceInfo src) {
		return new VarargsExpr(attr(src));
	}

	public static CallExpr.FunctionCallExpr functionCall(SourceInfo src, Expr fn, List<Expr> args) {
		return new CallExpr.FunctionCallExpr(attr(src), fn, args);
	}

	public static CallExpr.MethodCallExpr methodCall(SourceInfo src, Expr target, Name methodName, List<Expr> args) {
		return new CallExpr.MethodCallExpr(attr(src), target, methodName, args);
	}

	public static Expr paren(Expr expr) {
		if (expr instanceof MultiExpr) {
			return new ParenExpr(expr.attributes(), (MultiExpr) expr);
		}
		else {
			return expr;
		}
	}

	public static BinaryOperationExpr binaryOperation(SourceInfo src, Operator.Binary op, Expr left, Expr right) {
		return new BinaryOperationExpr(attr(src), op, left,right);
	}

	public static UnaryOperationExpr unaryOperation(SourceInfo src, Operator.Unary op, Expr arg) {
		return new UnaryOperationExpr(attr(src), op, arg);
	}

}
