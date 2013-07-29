package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeItNode;

/**
 * Renders DescribeItNodes as GraphNodes for display in an SEGraph. This
 * renderer allows the DescribeItNodes to be edited. Since the display looks
 * completely different from regular DescribeItNodes that the user sees, we
 * create a whole separate renderer for these.
 * 
 * @author kschenk
 * 
 */
public class EditableDescribeItNodeRenderer extends
		SEGraphNodeRenderer<DescribeItNode> {

	final SEGraph<DescribeItNode> graph;

	public EditableDescribeItNodeRenderer(SEGraph<DescribeItNode> graph) {
		super(graph);
		this.graph = graph;
	}

	@Override
	protected void configureInternalComponents(final JComponent component,
			final DescribeItNode node) {

		final KnowIt knowIt;
		final JToggleButton knowItButton;

		knowIt = node.getKnowIt();
		knowItButton = new JToggleButton("KnowIt");

		if (knowIt != null) {
			final JButton typesButton;
			final TypeAction typeAction;

			typeAction = new TypeAction();
			typesButton = new JButton(typeAction);

			typeAction.getTypeSelectionDialogBuilder().deselectAll();
			typeAction.getTypeSelectionDialogBuilder().selectTypesByKeyword(
					knowIt.getTypes(), true);

			typeAction.setAction(new Runnable() {
				@Override
				public void run() {
					knowIt.setTypes(typeAction.getTypeSelectionDialogBuilder()
							.getSelectedTypeKeywords());

					component.removeAll();
					configureInternalComponents(component, node);

					EditableDescribeItNodeRenderer.this.graph.revalidate();
					EditableDescribeItNodeRenderer.this.graph.repaint();
				}
			});

			knowItButton.setSelected(true);

			component.add(knowItButton);
			component.add(ScriptWidgetFactory.buildSlotPanel(knowIt, true));
			component.add(typesButton);
		} else {
			final JTextField nodeNameEditor;
			final Runnable commitText;

			nodeNameEditor = new JTextField(node.getName());
			commitText = new Runnable() {
				@Override
				public void run() {
					node.setName(nodeNameEditor.getText());
				}
			};

			WidgetDecorator.decorateJTextFieldForFocusEvents(nodeNameEditor,
					commitText, true, Color.white);

			component.add(knowItButton);
			component.add(nodeNameEditor);
		}

		knowItButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (knowItButton.isSelected()) {
					node.setKnowIt(new KnowIt(node.getName()));
				} else {
					node.setName(node.getKnowIt().getDisplayText());
					node.setKnowIt(null);
				}

				component.removeAll();
				configureInternalComponents(component, node);

				EditableDescribeItNodeRenderer.this.graph.revalidate();
				EditableDescribeItNodeRenderer.this.graph.repaint();
			}
		});
	}
}
