package scriptease.gui.graph.nodes;

import java.awt.FlowLayout;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.cell.TypeWidget;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.graph.SEGraphNodeRenderer;
import scriptease.model.atomic.KnowIt;

public class KnowItNodeRenderer extends SEGraphNodeRenderer<KnowItNode> {
	private Map<KnowItNode, JComponent> componentMap = new IdentityHashMap<KnowItNode, JComponent>();
	private JComponent component;

	@Override
	public JComponent getComponentForNode(KnowItNode node) {
		// check if the node already has a component
		final JComponent storedComponent = this.componentMap.get(node);
		if (storedComponent != null) {
			this.component = storedComponent;
		} else {
			// otherwise build it and store it
			this.component = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));

			this.configureListeners(node, this.component);
			this.configureAppearance(this.component, node);

			KnowIt knowIt = node.getKnowIt();
			if (knowIt != null) {
				JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
						0, 0));
				typePanel.setOpaque(false);
				for (String type : knowIt.getAcceptableTypes()) {
					TypeWidget typeWidget = ScriptWidgetFactory
							.getTypeWidget(type);

					typeWidget.setSelected(true);
					typeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);
					typePanel.add(typeWidget);
				}
				this.component.add(typePanel);
				this.component.add(new JLabel(knowIt.getDisplayText()));
			}

			this.componentMap.put(node, this.component);
		}

		// return the component for the node
		return this.component;
	}

}
