package scriptease.gui.SETree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @param <T>
 *            Object's type in the tree.
 */
public class Tree<T> {
	private final T root;
	private final List<Tree<T>> children;
	private Map<T, Tree<T>> find = new HashMap<T, Tree<T>>();
	private Tree<T> parent = null;

	public Tree(T root) {
		this.root = root;
		this.find.put(root, this);

		this.children = new ArrayList<Tree<T>>();
	}

	public void addChild(T root, T leaf) {
		if (this.find.containsKey(root)) {
			this.find.get(root).addLeaf(leaf);
		} else {
			addLeaf(root).addLeaf(leaf);
		}
	}

	public Tree<T> addLeaf(T leaf) {
		Tree<T> t = new Tree<T>(leaf);
		this.children.add(t);
		t.parent = this;
		t.find = this.find;
		this.find.put(leaf, t);
		return t;
	}

	public Tree<T> setAsParent(T parentRoot) {
		Tree<T> t = new Tree<T>(parentRoot);
		t.children.add(this);
		this.parent = t;
		t.find = this.find;
		t.find.put(this.root, this);
		t.find.put(parentRoot, t);
		return t;
	}

	public void clear() {
		this.children.clear();
		this.find.clear();

		if (this.parent != null)
			this.parent.clear();

		this.find.put(root, this);
	}

	public T getHead() {
		return this.root;
	}

	public Tree<T> getTree(T element) {
		return this.find.get(element);
	}

	public Tree<T> getParent() {
		return this.parent;
	}

	public Collection<T> getSuccessors(T root) {
		Collection<T> successors = new ArrayList<T>();
		Tree<T> tree = getTree(root);
		if (null != tree) {
			for (Tree<T> leaf : tree.children) {
				successors.add(leaf.root);
			}
		}
		return successors;
	}

	public Collection<Tree<T>> getSubTrees() {
		return this.children;
	}

	public static <T> Collection<T> getSuccessors(T of, Collection<Tree<T>> in) {
		for (Tree<T> tree : in) {
			if (tree.find.containsKey(of)) {
				return tree.getSuccessors(of);
			}
		}
		return new ArrayList<T>();
	}

	@Override
	public String toString() {
		String str = this.root.toString();

		if (this.children.size() > 0)
			str += " < " + this.children.toString();

		return str;
	}
}
