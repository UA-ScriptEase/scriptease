package scriptease.gui.graph.renderers;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;

import scriptease.gui.graph.SEGraph;
import scriptease.gui.graph.nodes.TextNode;

public class TextNodeRenderer extends SEGraphNodeRenderer<TextNode> {

	public TextNodeRenderer(SEGraph<TextNode> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component, TextNode node) {
		final JLabel textLabel = new JLabel(node.getText());

		textLabel.setFont(new Font(textLabel.getFont().getName(), node
				.getBoldStatus(), textLabel.getFont().getSize()));

		component.add(textLabel);
	}
}