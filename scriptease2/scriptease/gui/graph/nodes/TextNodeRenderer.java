import java.awt.Font;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import scriptease.gui.graph.SEGraphNodeRenderer;
import scriptease.gui.graph.nodes.TextNode;

public class TextNodeRenderer extends SEGraphNodeRenderer<TextNode> {
	private Map<TextNode, JComponent> componentMap = new IdentityHashMap<TextNode, JComponent>();
	private JComponent component;

	@Override
	public JComponent getComponentForNode(TextNode node) {
		// check if the node already has a component
		final JComponent storedComponent = this.componentMap.get(node);
		if (storedComponent != null) {
			this.component = storedComponent;
		} else {
			// otherwise build it and store it
			this.component = new JLabel(node.getText());
			this.configureListeners(node, this.component);
			this.configureAppearance(this.component, node);
			this.component.setFont(new Font(this.component.getFont().getName(),
					node.getBoldStatus(), this.component.getFont().getSize()));

			this.componentMap.put(node, this.component);
		}

		// return the component for the node
		return this.component;
	}

}