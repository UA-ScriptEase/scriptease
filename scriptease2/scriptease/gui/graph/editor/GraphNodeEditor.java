package scriptease.gui.graph.editor;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scriptease.gui.graph.nodes.GraphNode;

/**
 * JPanel made for editing the fields of a GraphNode
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public abstract class GraphNodeEditor extends JPanel {
	protected static int FIELD_SIZE = 30;
	protected GraphNode node;

	public GraphNodeEditor(GraphNode node) {
		this.node = node;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// add a name editor
		this.add(buildField("Name", getNameField()));
	}

	protected abstract JTextField getNameField();

	protected JPanel buildField(String label, Component field) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);

		BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);

		panel.add(new JLabel(label));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(field);
		return panel;
	}
}
