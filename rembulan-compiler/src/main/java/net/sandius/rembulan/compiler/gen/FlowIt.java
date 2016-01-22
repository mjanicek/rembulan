package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlowIt {

	public final Prototype prototype;

	public FlowIt(Prototype prototype) {
		this.prototype = prototype;
	}

	public void go() {
		IntVector code = prototype.getCode();
		NLabel[] labels = new NLabel[code.length()];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new NLabel(Integer.toString(i + 1));
		}

		ReadOnlyArray<NLabel> pcLabels = ReadOnlyArray.wrap(labels);

		for (int i = 0; i < pcLabels.size(); i++) {
			int insn = code.get(i);
			int line = prototype.getLineAtPC(i);
			NNode node = NInsn.translate(insn, i, line, pcLabels);
			pcLabels.get(i).append(node);
		}

		System.out.println("[");
		for (int i = 0; i < pcLabels.size(); i++) {
			NLabel label = pcLabels.get(i);
			System.out.println(i + ": " + label.toString());
		}
		System.out.println("]");


		Map<NNode, Integer> inDegrees = accessibility(pcLabels.get(0));

		System.out.println();
		for (NNode n : inDegrees.keySet()) {
			int in = inDegrees.get(n);
			if (in > 1) {
				System.out.println("in=" + in + ": " + n.toString());
			}
		}

	}

	private Map<NNode, Integer> accessibility(NNode entry) {
		Map<NNode, Integer> inDegree = new HashMap<>();
		accessibilityRecurse(entry, inDegree);
		return Collections.unmodifiableMap(inDegree);
	}

	private void accessibilityRecurse(NNode n, Map<NNode, Integer> inDegree) {
		if (inDegree.containsKey(n)) {
			inDegree.put(n, inDegree.get(n) + 1);
		}
		else {
			inDegree.put(n, 1);
			if (n instanceof NUnconditional) {
				NUnconditional u = (NUnconditional) n;
				accessibilityRecurse(u.next, inDegree);
			}
			else if (n instanceof NBranch) {
				NBranch b = (NBranch) n;
				accessibilityRecurse(b.trueBranch, inDegree);
				accessibilityRecurse(b.falseBranch, inDegree);
			}
			else if (n instanceof NTerminal) {
				// no-op
			}
			else {
				throw new IllegalStateException();
			}
		}
	}

}
