package scriptease.gui.SEGraph.renderers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.typemenus.TypeAction;
import scriptease.gui.cell.ScriptWidgetFactory;
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

	public EditableDescribeItNodeRenderer(SEGraph<DescribeItNode> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
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
			typeAction.getTypeSelectionDialogBuilder().selectTypes(
					knowIt.getTypes(), true);

			typeAction.setAction(new Runnable() {
				@Override
				public void run() {
					knowIt.setTypes(typeAction.getTypeSelectionDialogBuilder()
							.getSelectedTypes());
				}
			});

			knowItButton.setSelected(true);

			component.add(knowItButton);
			component.add(ScriptWidgetFactory.buildSlotPanel(knowIt, true));
			component.add(typesButton);
		} else {
			final JTextField nodeNameEditor;

			nodeNameEditor = new JTextField(node.getName());

			nodeNameEditor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					node.setName(nodeNameEditor.getText());
				}
			});

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
			}
		});
	}
}
