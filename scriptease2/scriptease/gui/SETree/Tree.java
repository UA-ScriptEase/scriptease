package scriptease.gui.SETree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *  
 * @param <T>
 *          Object's type in the tree.
 */
public class Tree<T> {

  private T root;
  private ArrayList<Tree<T>> leafs = new ArrayList<Tree<T>>();
  private Tree<T> parent = null;
  private HashMap<T, Tree<T>> find = new HashMap<T, Tree<T>>();

  public Tree(T root) {
    this.root = root;
    find.put(root, this);
  }

  public void addLeaf(T root, T leaf) {
    if (find.containsKey(root)) {
      find.get(root).addLeaf(leaf);
    } else {
      addLeaf(root).addLeaf(leaf);
    }
  }

  public Tree<T> addLeaf(T leaf) {
    Tree<T> t = new Tree<T>(leaf);
    leafs.add(t);
    t.parent = this;
    t.find = this.find;
    find.put(leaf, t);
    return t;
  }

  public Tree<T> setAsParent(T parentRoot) {
    Tree<T> t = new Tree<T>(parentRoot);
    t.leafs.add(this);
    this.parent = t;
    t.find = this.find;
    t.find.put(root, this);
    t.find.put(parentRoot, t);
    return t;
  }

  public T getHead() {
    return root;
  }

  public Tree<T> getTree(T element) {
    return find.get(element);
  }

  public Tree<T> getParent() {
    return parent;
  }

  public Collection<T> getSuccessors(T root) {
    Collection<T> successors = new ArrayList<T>();
    Tree<T> tree = getTree(root);
    if (null != tree) {
      for (Tree<T> leaf : tree.leafs) {
        successors.add(leaf.root);
      }
    }
    return successors;
  }

  public Collection<Tree<T>> getSubTrees() {
    return leafs;
  }

  public static <T> Collection<T> getSuccessors(T of, Collection<Tree<T>> in) {
    for (Tree<T> tree : in) {
      if (tree.find.containsKey(of)) {
        return tree.getSuccessors(of);
      }
    }
    return new ArrayList<T>();
  }

  
}
