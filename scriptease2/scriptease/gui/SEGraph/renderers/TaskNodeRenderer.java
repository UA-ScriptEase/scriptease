package scriptease.gui.SEGraph.renderers;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

		final JTextField nameField;

		final JLabel percent;

		final SpinnerNumberModel model;
		final JSpinner spinner;
		
		model = new SpinnerNumberModel(100, 0.0f, 100f, 1.0f);
		spinner = new JSpinner(model);
		
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				task.setChance((double) spinner.getValue());
			}
		});
		
		nameField = new JTextField(task.getDisplayText());
		
		percent = new JLabel(" %");
		percent.setForeground(Color.WHITE);
		
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
		component.add(spinner);
		component.add(percent);
	}
}
