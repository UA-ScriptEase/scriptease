package scriptease.gui.SEGraph.renderers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import scriptease.ScriptEase;
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
	final boolean isEditable;
	
	public EditableDescribeItNodeRenderer(SEGraph<DescribeItNode> graph, final boolean isEditable) {
		super(graph);
		this.graph = graph;
		this.isEditable = isEditable;
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

			typeAction.deselectAll();
			typeAction.selectTypesByKeyword(knowIt.getTypes(), true);
			
			typeAction.setAction(new Runnable() {
				@Override
				public void run() {
					knowIt.setTypes(typeAction.getSelectedTypes());

					component.removeAll();
					configureInternalComponents(component, node);

					EditableDescribeItNodeRenderer.this.graph.revalidate();
					EditableDescribeItNodeRenderer.this.graph.repaint();
				}
			});

			knowItButton.setSelected(true);

			knowItButton.setEnabled(this.isEditable);
			typesButton.setEnabled(this.isEditable);
			
			component.add(knowItButton);
			component.add(ScriptWidgetFactory.buildSlotPanel(knowIt, this.isEditable));
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
					commitText);

			knowItButton.setEnabled(this.isEditable);
			nodeNameEditor.setEnabled(this.isEditable);
	
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
