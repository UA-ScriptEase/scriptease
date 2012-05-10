package scriptease.gui.describeIts;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JRadioButtonMenuItem;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.editor.KnowItNodeEditor;
import scriptease.gui.graph.editor.PathAssigner;
import scriptease.gui.graph.editor.TextNodeEditor;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;

/**
 * Editor used for creating DescribeIt graphs.
 * 
 * @author mfchurch
 *
 */
@SuppressWarnings("serial")
public class DescribeItGraphEditor extends GraphEditor {
	private final String NEW_OPTIONNODE_TEXT = "<html>New Option Node</html>";
	private final String NEW_TEXTNODE_TEXT = "<html>New Text Node</html>";
 
	private final DescribeIt editedDescribeIt;

	public DescribeItGraphEditor(DescribeIt original, AbstractAction saveAction) {
		super(saveAction);
		// Clone the original
		this.editedDescribeIt = original.clone();

		// Clear the initial selection
		this.editedDescribeIt.clearSelection();

		// Set the headNode as editing
		this.setHeadNode(this.editedDescribeIt.getHeadNode());
	}

	/**
	 * Gets the changes to the original describeIt in the form of a new
	 * describeIt
	 * 
	 * @return
	 */
	public DescribeIt getChanges() {
		return this.editedDescribeIt;
	}

	@Override
	protected void setActiveTool(GraphTool tool) {
		super.setActiveTool(tool);
		this.editedDescribeIt.clearSelection();
		// clear the editing component
		setEditingPanel(null);
	}

	/**
	 * Supports TextNodes and KnowItNodes for DescribeIts
	 */
	protected Collection<AbstractButton> getNodeButtons() {
		Collection<AbstractButton> buttons = new ArrayList<AbstractButton>();
		AbstractButton textNodeButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.NEW_TEXTNODE_TOOL);
					}
				});
		textNodeButton.setText(NEW_TEXTNODE_TEXT);
		buttons.add(textNodeButton);

		AbstractButton knowItNodeButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.NEW_KNOWITNODE_TOOL);
					}
				});
		knowItNodeButton.setText(NEW_OPTIONNODE_TEXT);
		buttons.add(knowItNodeButton);

		return buttons;
	}

	/**
	 * Supports Node and Path selection
	 */
	protected Collection<AbstractButton> getSelectButtons() {
		Collection<AbstractButton> buttons = new ArrayList<AbstractButton>();
		AbstractButton selectNodeButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.SELECT_NODE_TOOL);
					}
				});
		selectNodeButton.setText("<html>Select Node</html>");
		buttons.add(selectNodeButton);

		AbstractButton selectPathButton = new JRadioButtonMenuItem(
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActiveTool(GraphTool.SELECT_PATH_TOOL);
					}
				});
		selectPathButton.setText("<html>Select Path</html>");
		buttons.add(selectPathButton);

		return buttons;
	}

	@Override
	public void nodeChanged(GraphNode node, GraphNodeEvent event) {
		super.nodeChanged(node, event);
		final GraphNode sourceNode = event.getSource();
		final short type = event.getEventType();

		if (type == GraphNodeEvent.CLICKED) {
			// Determine what the active tool is.
			switch (this.getActiveTool()) {
			case NEW_TEXTNODE_TOOL:
				TextNode textNode = new TextNode("New Text Node");
				sourceNode.addChild(textNode);
				break;
			case NEW_KNOWITNODE_TOOL:
				KnowItNode knowItNode = new KnowItNode(new KnowIt(
						"New Option Node"));
				sourceNode.addChild(knowItNode);
				break;
			case SELECT_NODE_TOOL:
				this.editedDescribeIt.clearSelection();
				sourceNode.setSelectedColour(ScriptEaseUI.COLOUR_GAME_OBJECT);
				sourceNode.setSelected(true);
				node.process(new AbstractNoOpGraphNodeVisitor() {

					@Override
					public void processTextNode(TextNode textNode) {
						setEditingPanel(new TextNodeEditor(textNode));
					}

					@Override
					public void processKnowItNode(KnowItNode knowItNode) {
						setEditingPanel(new KnowItNodeEditor(knowItNode));
					} 
				});
				break;
			case SELECT_PATH_TOOL:
				this.editedDescribeIt.selectFromHeadToNode(sourceNode);
				/*
				 * only allow for path assigning on complete paths (finish with
				 * a terminal)
				 */
				if (node.isTerminalNode())
					setEditingPanel(new PathAssigner(this.editedDescribeIt));
				else
					setEditingPanel(null);
				break;
			}
		}
	}
}
