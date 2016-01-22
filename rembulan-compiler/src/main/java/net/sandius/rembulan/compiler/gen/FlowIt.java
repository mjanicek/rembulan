package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
			pcLabels.get(i).followedBy(node);
		}

//		System.out.println("[");
//		for (int i = 0; i < pcLabels.size(); i++) {
//			NLabel label = pcLabels.get(i);
//			System.out.println(i + ": " + label.toString());
//		}
//		System.out.println("]");

		NEntry callEntry = new NEntry().enter(pcLabels.get(0));

		Set<NEntry> entryPoints = new HashSet<>();
		entryPoints.add(callEntry);

		removeInnerLabels(entryPoints);

		System.out.println();
		printNodes(entryPoints);

	}

	private void removeInnerLabels(Iterable<NEntry> entryPoints) {
		for (NNode n : accessibleNodes(entryPoints)) {
			if (n instanceof NLabel && n.inDegree() <= 1) {
				// only do this when the incoming edge is an unconditional node
				if (n.in().iterator().next() instanceof NUnconditional) {
					((NLabel) n).remove();
				}
			}
		}
	}

	private void printNodes(Iterable<NEntry> entryPoints) {
		ArrayList<NNode> nodes = new ArrayList<>();
		for (NNode n : accessibleNodes(entryPoints)) {
			nodes.add(n);
		}

		System.out.println("[");
		for (int i = 0; i < nodes.size(); i++) {
			NNode n = nodes.get(i);

			System.out.print("\t" + i + ": ");
			System.out.print("{ ");
			for (NNode m : n.in()) {
				int idx = nodes.indexOf(m);
				System.out.print(idx + " ");
			}
			System.out.print("} -> ");

			System.out.print(n.selfToString());

			System.out.print(" -> { ");
			for (NNode m : n.out()) {
				int idx = nodes.indexOf(m);
				System.out.print(idx + " ");
			}
			System.out.print("}");
			System.out.println();
		}
		System.out.println("]");
	}

	private Iterable<NNode> accessibleNodes(Iterable<NEntry> entryPoints) {
		return accessibility(entryPoints).keySet();
	}

	private Map<NNode, Integer> accessibility(Iterable<NEntry> entryPoints) {
		Map<NNode, Integer> inDegree = new HashMap<>();
		for (NEntry entry : entryPoints) {
			accessibilityRecurse(entry, inDegree);
		}
		return Collections.unmodifiableMap(inDegree);
	}

	private void accessibilityRecurse(NNode n, Map<NNode, Integer> inDegree) {
		if (inDegree.containsKey(n)) {
			if (n instanceof NEntry) {
				throw new IllegalStateException("Re-entering an entry node");
			}
			inDegree.put(n, inDegree.get(n) + 1);
		}
		else {
			inDegree.put(n, 1);
			if (n instanceof NEntry) {
				NEntry e = (NEntry) n;
				accessibilityRecurse(e.next(), inDegree);
			}
			else if (n instanceof NUnconditional) {
				NUnconditional u = (NUnconditional) n;
				accessibilityRecurse(u.next(), inDegree);
			}
			else if (n instanceof NBranch) {
				NBranch b = (NBranch) n;
				accessibilityRecurse(b.trueBranch(), inDegree);
				accessibilityRecurse(b.falseBranch(), inDegree);
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
