package scriptease.gui.graph.editor;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.gui.graph.nodes.GraphNode;

/**
 * Abstract JComponent made for editing the fields of a GraphNode. As of right
 * now, it is used for TextNodes and KnowItNodes.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public abstract class GraphNodeEditor extends JComponent {
	
	/*
	 * TODO This could likely all go into the specific "Renderers"!
	 */
	
	protected static int FIELD_SIZE = 30;
	protected GraphNode node;
	private JComponent field;

	/**
	 * Constructor. Creates a new GraphNodeEditor.
	 */
	public GraphNodeEditor() {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.field = buildField("Name", getNameField());
		this.add(this.field);
	}

	/**
	 * Sets the node for the passed GraphNode. Builds the name field for the
	 * node, which will represent the name of it.
	 * 
	 * @param node
	 */
	public void setNode(GraphNode node) {
		System.out.println("Setting the node" + node.toString());
		this.node = node;
		this.remove(this.field);
		this.field = buildField("Name", getNameField());
		this.add(this.field);

		this.repaint();
		this.revalidate();
	}

	/**
	 * Abstract method. Subclasses must return the JTextField that will be used
	 * to edit the name of the node.
	 * 
	 * @return
	 */
	protected abstract JTextField getNameField();

	/**
	 * Builds the field for the passed label and field.
	 * 
	 * @param label
	 * @param field
	 * @return
	 */
	protected JComponent buildField(String label, Component field) {
		JComponent panel = new JPanel();
		panel.setOpaque(false);

		panel.add(new JLabel(label));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(field);
		return panel;
	}
}
