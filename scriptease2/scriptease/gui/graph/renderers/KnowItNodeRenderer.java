package scriptease.gui.graph.renderers;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.SETree.cell.TypeWidget;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.graph.SEGraph;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.model.atomic.KnowIt;

public class KnowItNodeRenderer extends SEGraphNodeRenderer<KnowItNode> {
	public KnowItNodeRenderer(SEGraph<KnowItNode> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			KnowItNode node) {
		component.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

		KnowIt knowIt = node.getKnowIt();
		if (knowIt != null) {
			JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,
					0));
			typePanel.setOpaque(false);
			for (String type : knowIt.getAcceptableTypes()) {
				TypeWidget typeWidget = ScriptWidgetFactory.getTypeWidget(type);

				typeWidget.setSelected(true);
				typeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);
				typePanel.add(typeWidget);
			}
			component.add(typePanel);
			component.add(new JLabel(knowIt.getDisplayText()));
		}
	}
}
