package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Branch;
import net.sandius.rembulan.compiler.gen.block.Exit;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Map;

public class LuaInstructionToNodeTranslator {

	private final Prototype prototype;
	private final ReadOnlyArray<Target> pcToLabel;

	private final Map<Prototype, Unit> units;
	
	public LuaInstructionToNodeTranslator(Prototype prototype, ReadOnlyArray<Target> pcToLabel, Map<Prototype, Unit> units) {
		Check.notNull(prototype);
		Check.notNull(pcToLabel);
		Check.notNull(units);

		this.prototype = prototype;
		this.pcToLabel = pcToLabel;
		this.units = units;
	}
	
	public class MyNodeAppender {
		private final int pc;
		private final NodeAppender appender;

		public MyNodeAppender(int pc) {
			this.appender = new NodeAppender(pcToLabel.get(pc));
			this.pc = pc;
		}

		public MyNodeAppender append(Linear lin) {
			appender.append(lin);
			return this;
		}

		public Map<Prototype, Unit> units() {
			return units;
		}

		public void branch(Branch branch) {
			appender.branch(branch);
		}

		public void term(Exit term) {
			appender.append(new AccountingNode.End()).term(term);
		}

//		public void jumpTo(int dest) {
//			appender.jumpTo(target(pc, dest));
//		}

		public Target target(int offset) {
			if (offset == 0) {
				throw new IllegalArgumentException();
			}

			Target jmpTarget = pcToLabel.get(pc + offset);

			if (offset < 0) {
				// this is a backward jump

				Target tgt = new Target();
				NodeAppender appender = new NodeAppender(tgt);
				appender.append(new AccountingNode.Flush())
						.jumpTo(jmpTarget);

				return tgt;
			}
			else {
				return jmpTarget;
			}
		}

		public void jumpToOffset(int offset) {
			appender.jumpTo(target(offset));
		}

		public void toNext() {
			jumpToOffset(1);
		}

	}

	public void translate(int pc) {
		MyNodeAppender appender = new MyNodeAppender(pc);

		int line = prototype.getLineAtPC(pc);

		if (line > 0) {
			appender.append(new LineInfo(line));
		}

		appender.append(new AccountingNode.TickBefore());

		InstructionDispatch.dispatch(new AppenderEmitter(prototype, appender), prototype.getCode().get(pc));
	}

}
