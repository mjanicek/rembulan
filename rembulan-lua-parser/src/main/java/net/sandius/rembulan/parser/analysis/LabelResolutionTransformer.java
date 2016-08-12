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

package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

class LabelResolutionTransformer extends Transformer {

	private final Stack<Scope> scopes;

	private final Map<LabelStatement, ResolvedLabel> defs;
	private final Map<GotoStatement, ResolvedLabel> uses;

	public LabelResolutionTransformer() {
		this.scopes = new Stack<>();
		this.defs = new HashMap<>();
		this.uses = new HashMap<>();
	}

	private static boolean isVoidStatement(Statement stat) {
		return stat instanceof LabelStatement || stat instanceof GotoStatement;
	}

	private void enterBlock() {
		scopes.push(new Scope());
	}

	private void endBlockScopes() {
		scopes.peek().clearLocals();
	}

	private void leaveBlock() {
		Scope s = scopes.pop();

		if (!scopes.isEmpty()) {
			Scope current = scopes.peek();
			current.addGotos(s.pending);
		}
		else {
			// this was the top block
			if (!s.pending.isEmpty()) {
				GotoStatement gs = s.pending.get(0).statement;
				throw new NameResolutionException("no visible label '" + gs.labelName().value() + "' for <goto> at line " + gs.line());
			}
		}
	}

	private void defLabel(LabelStatement node) {
		scopes.peek().defLabel(node);
	}

	private void useLabel(GotoStatement node) {
		scopes.peek().useLabel(node);
	}

	private void defLocal(Name name) {
		scopes.peek().defLocal(name);
	}

	private Block annotate(Block b) {
		LabelAnnotatorTransformer annotator = new LabelAnnotatorTransformer() {

			@Override
			protected Object annotation(LabelStatement node) {
				ResolvedLabel rl = defs.get(node);
				if (rl == null) {
					throw new IllegalStateException("unresolved label '" + node.labelName().value() + "' in label statement at line " + node.line());
				}
				return rl;
			}

			@Override
			protected Object annotation(GotoStatement node) {
				ResolvedLabel rl = uses.get(node);
				if (rl == null) {
					throw new IllegalStateException("unresolved label '" + node.labelName().value() + "' in goto statement at line " + node.line());
				}
				return rl;
			}

		};

		return annotator.transform(b);
	}

	private Block transformTopBlock(Block b) {
		b = transform(b);
		return annotate(b);
	}

	@Override
	public Chunk transform(Chunk chunk) {
		return chunk.update(this.transformTopBlock(chunk.block()));
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		LabelResolutionTransformer child = new LabelResolutionTransformer();
		return e.update(e.params(), child.transformTopBlock(e.block()));
	}

	private static Statement lastNonVoidStatement(Block block) {
		Statement result = null;
		for (BodyStatement bs : block.statements()) {
			if (!isVoidStatement(bs)) {
				result = bs;
			}
		}
		if (block.returnStatement() != null) {
			result = block.returnStatement();
		}

		return result;
	}

	@Override
	public Block transform(Block block) {
		enterBlock();

		Statement localScopeEnd = lastNonVoidStatement(block);

		List<BodyStatement> stats = new ArrayList<>();
		for (BodyStatement bs : block.statements()) {
			stats.add(bs.accept(this));

			if (Objects.equals(localScopeEnd, bs)) {
				endBlockScopes();
			}
		}

		ReturnStatement ret = block.returnStatement() != null
				? block.returnStatement().accept(this)
				: null;

		leaveBlock();

		return block.update(Collections.unmodifiableList(stats), ret);
	}

	@Override
	public BodyStatement transform(LabelStatement node) {
		defLabel(node);
		return node;
	}

	@Override
	public BodyStatement transform(GotoStatement node) {
		useLabel(node);
		return node;
	}

	@Override
	public BodyStatement transform(LocalDeclStatement node) {
		defLocal(node.names().get(0));
		return node;
	}

	private static class LabelDef {

		public final int line;
		public final ResolvedLabel labelObject;

		private LabelDef(int line, ResolvedLabel labelObject) {
			this.line = line;
			this.labelObject = Check.notNull(labelObject);
		}

	}

	private static class PendingGoto {

		private final GotoStatement statement;
		private final List<Name> localsSince;

		private PendingGoto(GotoStatement statement) {
			this.statement = Check.notNull(statement);
			this.localsSince = new ArrayList<>();
		}

		public void addLocal(Name local) {
			localsSince.add(local);
		}

	}

	private class Scope {

		private final Map<Name, LabelDef> definedHere;
		private final List<PendingGoto> pending;
		private final Set<Name> locals;

		public Scope() {
			this.definedHere = new HashMap<>();
			this.pending = new ArrayList<>();
			this.locals = new HashSet<>();
		}

		public void defLabel(LabelStatement node) {
			Name labelName = node.labelName();

			ResolvedLabel rl = new ResolvedLabel();

			{
				LabelDef prevDef = definedHere.put(labelName, new LabelDef(node.line(), rl));
				if (prevDef != null) {
					throw new NameResolutionException("label '" + labelName.value() + "' already defined on line " + prevDef.line);
				}
			}

			{
				ResolvedLabel old = defs.put(node, rl);
				assert (old == null);
			}

			for (PendingGoto pg : pending) {
				GotoStatement gotoStat = pg.statement;

				if (gotoStat.labelName().equals(labelName)) {
					if (!pg.localsSince.isEmpty()) {
						Name localName = pg.localsSince.get(0);
						throw new NameResolutionException("<goto " + labelName.value() + "> at line " + gotoStat.line() + " jumps into the scope of local '" + localName.value() + "'");
					}
					else {
						ResolvedLabel old = uses.put(gotoStat, rl);
						assert (old == null);

						// found the goto
						pending.remove(pg);
						break;
					}
				}
			}
		}

		public void useLabel(GotoStatement node) {
			LabelDef ldef = definedHere.get(node.labelName());

			if (ldef != null) {
				ResolvedLabel old = uses.put(node, ldef.labelObject);
				assert (old == null);
			}
			else {
				pending.add(new PendingGoto(node));
			}
		}

		public void addGotos(Iterable<PendingGoto> gotos) {
			for (PendingGoto pg : gotos) {
				useLabel(pg.statement);
			}
		}

		public void defLocal(Name name) {
			locals.add(name);
			for (PendingGoto pg : pending) {
				pg.addLocal(name);
			}
		}

		public void clearLocals() {
			for (Name n : locals) {
				for (PendingGoto pg : pending) {
					pg.localsSince.remove(n);
				}
			}
			locals.clear();
		}

	}

}
