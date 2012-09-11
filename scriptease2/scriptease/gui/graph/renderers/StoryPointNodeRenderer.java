package scriptease.gui.graph.renderers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.gui.quests.StoryPoint;

/**
 * Special renderer for nodes representing StoryPoints. These components also
 * contain Fan In panels and Binding Widgets.
 * 
 * @author kschenk
 * 
 */
public class StoryPointNodeRenderer extends SEGraphNodeRenderer<StoryPoint> {
	@Override
	protected void configureInternalComponents(final JComponent component) {
		final StoryPoint node;

		node = this.getNodeForComponent(component);

		if (node != null) {
			final JButton editNameButton;
			final BindingWidget uneditableWidget;
			final BindingWidget editableWidget;

			editNameButton = new JButton("Edit");
			uneditableWidget = ScriptWidgetFactory.buildBindingWidget(node,
					false);
			editableWidget = ScriptWidgetFactory.buildBindingWidget(node, true);

			editNameButton.setFocusable(false);
			editNameButton.setContentAreaFilled(false);
			editNameButton.setOpaque(true);

			editNameButton.setBackground(new Color(222, 222, 222));
			editNameButton.setBorder(new LineBorder(Color.GRAY, 1, true));
			editNameButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
			editNameButton.setPreferredSize(new Dimension(35, 20));

			editNameButton.addActionListener(new ActionListener() {
				private boolean inEditingMode = false;

				@Override
				public void actionPerformed(ActionEvent e) {
					this.inEditingMode = !this.inEditingMode;

					component.removeAll();

					if (this.inEditingMode) {
						editNameButton.setText("Done");
						editNameButton.setBackground(new Color(222, 250, 222));
						component.add(ScriptWidgetFactory.buildFanInPanel(node,
								10, true));
						component.add(editableWidget);
					} else {
						editNameButton.setText("Edit");
						editNameButton.setBackground(new Color(222, 222, 222));
						component.add(ScriptWidgetFactory.buildFanInPanel(node,
								0, false));
						component.add(uneditableWidget);
					}

					component.add(editNameButton);

					component.revalidate();
				}
			});

			component.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			component.add(ScriptWidgetFactory.buildFanInPanel(node, 0, false));
			component.add(uneditableWidget);
			component.add(editNameButton);
		}
	}
}
