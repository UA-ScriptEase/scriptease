package scriptease.gui.graph.nodes;

import java.awt.FlowLayout;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.graph.SEGraphNodeRenderer;
import scriptease.gui.quests.QuestPoint;

public class QuestPointNodeRenderer extends SEGraphNodeRenderer<QuestPoint> {
	private Map<QuestPoint, JComponent> componentMap = new IdentityHashMap<QuestPoint, JComponent>();
	private JComponent component;

	@Override
	public JComponent getComponentForNode(QuestPoint node) {
		// check if the node already has a component
		final JComponent storedComponent = this.componentMap.get(node);
		if (storedComponent != null) {
			this.component = storedComponent;
		} else {
			// otherwise build it and store it
			this.component = new JPanel();
			this.configureListeners(node, this.component);
			this.configureAppearance(this.component, node);
			this.component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			this.component.add(ScriptWidgetFactory.buildFanInPanel(node
					.getFanIn()));
			this.component.add(ScriptWidgetFactory.buildBindingWidget(node,
					false));
			this.componentMap.put(node, this.component);
		}

		// return the component for the node
		return this.component;
	}
}
