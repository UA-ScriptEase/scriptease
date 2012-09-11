package scriptease.gui.graph.renderers;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;

import scriptease.gui.graph.nodes.TextNode;

public class TextNodeRenderer extends SEGraphNodeRenderer<TextNode> {

	@Override
	protected void configureInternalComponents(JComponent component) {
		final TextNode node = this.getNodeForComponent(component);
		final JLabel textLabel = new JLabel(node.getText());

		textLabel.setFont(new Font(textLabel.getFont().getName(), node
				.getBoldStatus(), textLabel.getFont().getSize()));

		component.add(textLabel);
	}
}