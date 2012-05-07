package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import scriptease.controller.GraphNodeVisitor;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * JUnit tests relating to GraphNodes for ScriptEase 2
 * 
 * @author mfchurch
 * 
 */
public class GraphNodeTests {

	@Test
	public void childrenStartEmpty() {
		MockGraphNode MockGraphNode = new MockGraphNode();
		assertEquals(MockGraphNode.getChildren().size(), 0);
	}

	@Test
	public void parentsStartEmpty() {
		MockGraphNode MockGraphNode = new MockGraphNode();
		assertEquals(MockGraphNode.getParents().size(), 0);
	}

	@Test
	public void childIsAddedToParent() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		parent.addChild(child);
		assertTrue(parent.getChildren().contains(child));
	}

	@Test
	public void parentIsAddedToChild() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		child.addParent(parent);
		assertTrue(child.getParents().contains(parent));
	}

	@Test
	public void setSelectionMakesSelected() {
		MockGraphNode graphNode = new MockGraphNode();
		graphNode.setSelected(true);
		assertTrue(graphNode.isSelected());
		graphNode.setSelected(false);
		assertFalse(graphNode.isSelected());
	}

	@Test
	public void parentIsRemovedFromChild() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		parent.addChild(child);

		child.removeFromParent(parent, false);
		assertFalse(child.getParents().contains(parent));
		assertFalse(parent.getChildren().contains(child));
	}

	@Test
	public void childIsRemovedFromParent() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		parent.addChild(child);

		parent.removeChild(child, false);
		assertFalse(parent.getChildren().contains(child));
		assertFalse(child.getParents().contains(parent));
	}
	
	@Test
	public void descendantIsRemoved() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		MockGraphNode grandChild = new MockGraphNode();
		parent.addChild(child);
		child.addChild(grandChild);

		assertTrue(parent.removeDescendant(grandChild, false));
		assertFalse(parent.isDescendant(grandChild));
		assertFalse(child.getChildren().contains(grandChild));
	}
	
	@Test
	public void replaceReplaces() {
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		MockGraphNode child2 = new MockGraphNode();
		parent.addChild(child);

		assertTrue(parent.replaceChild(child, child2));
		assertFalse(parent.getChildren().contains(child)); 
		assertTrue(parent.getChildren().contains(child2));
	}
	
	@Test
	public void removeParentsRemovesParents() {
		MockGraphNode parent1 = new MockGraphNode();
		MockGraphNode parent2 = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		
		parent1.addChild(child);
		parent2.addChild(child);
		
		assertTrue(child.removeParents());
		assertFalse(parent1.getChildren().contains(child));
		assertFalse(parent2.getChildren().contains(child));
		assertFalse(child.getParents().contains(parent1));
		assertFalse(child.getParents().contains(parent2));
	}
	
	@Test
	public void removeChildrenRemovesChildren(){
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode child1 = new MockGraphNode();
		MockGraphNode child2 = new MockGraphNode();
		
		parent.addChild(child1);
		parent.addChild(child2);
		
		assertTrue(parent.removeChildren());
		assertFalse(parent.getChildren().contains(child1));
		assertFalse(parent.getChildren().contains(child2));
	}
	
	@Test
	public void removeChildReparentsProperly(){
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode node = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		
		parent.addChild(node);
		node.addChild(child);
		
		parent.removeChild(node, true);
		assertTrue(parent.getChildren().contains(child));
		assertTrue(child.getParents().contains(parent));
		assertFalse(parent.getChildren().contains(node));
		assertFalse(child.getParents().contains(node));
	}
	
	@Test
	public void removeParentReparentsProperly(){
		MockGraphNode parent = new MockGraphNode();
		MockGraphNode node = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		
		parent.addChild(node);
		node.addChild(child);
		
		node.removeFromParent(parent, true);
		assertTrue(parent.getChildren().contains(child));
		assertTrue(child.getParents().contains(parent));
		assertFalse(parent.getChildren().contains(node));
		assertFalse(child.getParents().contains(node));
	}
	
	@Test
	public void removeDescendantRemovesDescendant(){
		MockGraphNode node = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		MockGraphNode descendant = new MockGraphNode();
		MockGraphNode descendantChild = new MockGraphNode();
		
		node.addChild(child);
		child.addChild(descendant);
		descendant.addChild(descendantChild);
		
		assertTrue(descendant.isDescendant(node));
		assertTrue(descendant.isDescendant(child));
		assertTrue(descendantChild.isDescendant(node));
		assertTrue(descendantChild.isDescendant(child));
		
		node.removeDescendant(descendant, false);
		
		assertFalse(descendant.isDescendant(node));
		assertFalse(descendant.isDescendant(child));
		assertFalse(descendantChild.isDescendant(node));
		assertFalse(descendantChild.isDescendant(child));
	}
	
	@Test
	public void removeDescendantReparentsProperly(){
		MockGraphNode node = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		MockGraphNode descendant = new MockGraphNode();
		MockGraphNode descendantChild = new MockGraphNode();
		
		node.addChild(child);
		child.addChild(descendant);
		descendant.addChild(descendantChild);
		
		assertTrue(descendant.isDescendant(node));
		assertTrue(descendant.isDescendant(child));
		assertTrue(descendantChild.isDescendant(node));
		assertTrue(descendantChild.isDescendant(child));
		
		node.removeDescendant(descendant, true);
		
		assertFalse(descendant.isDescendant(node));
		assertFalse(descendant.isDescendant(child));
		assertTrue(descendantChild.isDescendant(node));
		assertTrue(descendantChild.isDescendant(child));
	}
	
	@Test
	public void isDescendantWorksProperly(){
		MockGraphNode node = new MockGraphNode();
		MockGraphNode child = new MockGraphNode();
		MockGraphNode descendant = new MockGraphNode();
		
		node.addChild(child);
		child.addChild(descendant);
		
		assertTrue(child.isDescendant(node));
		assertTrue(descendant.isDescendant(node));
		assertFalse(node.isDescendant(child));
		assertFalse(node.isDescendant(descendant));
	}
}

/**
 * Mock GraphNode class used for testing GraphNode methods
 * 
 * @author mfchurch
 * 
 */
class MockGraphNode extends GraphNode {

	@Override
	public boolean represents(Object object) {
		return false;
	}

	@Override
	public void process(GraphNodeVisitor processController) {
	}
}
