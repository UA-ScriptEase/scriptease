package scriptease.gui.SEGraph.renderers;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.TypeWidget;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeItNode;

/**
 * Renders DescribeItNodes as GraphNodes for display in an SEGraph.
 * 
 * @author kschenk
 * 
 */
public class DescribeItNodeRenderer extends SEGraphNodeRenderer<DescribeItNode> {

	public DescribeItNodeRenderer(SEGraph<DescribeItNode> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			DescribeItNode node) {

		component.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

		final KnowIt knowIt = node.getKnowIt();
		final JLabel nameLabel;

		if (knowIt != null) {
			final JPanel typePanel;

			typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

			typePanel.setOpaque(false);

			for (String type : knowIt.getAcceptableTypes()) {
				final TypeWidget typeWidget;

				typeWidget = ScriptWidgetFactory.buildTypeWidget(type);

				typeWidget.setSelected(true);
				typeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);
				typePanel.add(typeWidget);
			}
			component.add(typePanel);
			nameLabel = new JLabel(knowIt.getDisplayText());
		} else {
			nameLabel = new JLabel(node.getName());
		}

		component.add(nameLabel);
		
	}
}
