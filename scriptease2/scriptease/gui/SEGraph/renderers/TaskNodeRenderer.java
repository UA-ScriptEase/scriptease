package scriptease.gui.SEGraph.renderers;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JTextField;

import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.model.complex.behaviours.Task;

public class TaskNodeRenderer extends SEGraphNodeRenderer<Task> {

	private SEGraph<Task> graph;

	public TaskNodeRenderer(SEGraph<Task> graph) {
		super(graph);
		this.graph = graph;
	}

	@Override
	protected void configureInternalComponents(final JComponent component,
			final Task node) {

		component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));
		this.updateComponents(component, node);
	}

	/**
	 * Updates the components in the passed in component to represent the passed
	 * in Task
	 * 
	 * @param component
	 * @param task
	 */
	private void updateComponents(final JComponent component,
			final Task task) {
		
		if (task == null)
			return;

		final JTextField nameField = new JTextField(task.getDisplayText());

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
				new Runnable() {
			
					@Override
					public void run() {
						task.setDisplayText(nameField.getText());

						graph.revalidate();
						graph.repaint();
					}
				}, true, Color.WHITE);

		component.add(nameField);
	}
}
