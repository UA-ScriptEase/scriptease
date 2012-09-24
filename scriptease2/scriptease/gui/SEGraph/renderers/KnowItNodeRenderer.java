package scriptease.gui.SEGraph.renderers;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.nodes.KnowItNode;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.gui.cell.TypeWidget;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.KnowIt;

public class KnowItNodeRenderer extends SEGraphNodeRenderer<KnowItNode> {
	public KnowItNodeRenderer(SEGraph<KnowItNode> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			KnowItNode node) {
		component.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

		final KnowIt knowIt = node.getKnowIt();

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
