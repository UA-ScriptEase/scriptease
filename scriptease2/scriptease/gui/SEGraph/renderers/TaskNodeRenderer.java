package scriptease.gui.SEGraph.renderers;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scriptease.ScriptEase;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;

/**
 * Special renderer for nodes representing Tasks {@link Task}. There are two
 * types of tasks. The first of which are independent tasks. Independent tasks
 * are rendered with a single text field, whereas collaborative tasks are
 * rendered with two text fields - one to represent the initiator and the other
 * to represent the collaborator.
 * 
 * @author jyuen
 * 
 */
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
	private void updateComponents(final JComponent component, final Task task) {
		if (task == null)
			return;

		if (task instanceof IndependentTask)
			this.updateIndependentComponent(component, (IndependentTask) task);
		else if (task instanceof CollaborativeTask)
			this.updateCollaborativeComponent(component,
					(CollaborativeTask) task);
	}

	private void updateIndependentComponent(final JComponent component,
			final IndependentTask task) {
		final JTextField nameField;
		final int verticalMargin = 10;
		final int horizontalMargin = 20;

		final SpinnerNumberModel model;
		final JSpinner spinner;
		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !task.getLibrary().isReadOnly();

		model = new SpinnerNumberModel(100, 0.0f, 100f, 1.0f);
		spinner = new JSpinner(model);
		spinner.setValue(task.getChance());

		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				task.setChance(((Double) spinner.getValue()).intValue());
			}
		});

		spinner.setEnabled(isEditable);

		if (graph.getStartNode() == task) {
			nameField = new JTextField("Start Node");
			nameField.setEnabled(false);
		} else {
			nameField = new JTextField(task.getDisplayText());
			nameField.setEnabled(isEditable);
		}

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField,
				new Runnable() {

					@Override
					public void run() {
						task.setDisplayText(nameField.getText());

						graph.revalidate();
						graph.repaint();
					}

				});

		if (graph.isReadOnly()) {
			nameField.setEditable(false);
		}

		this.createBufferRectangle(verticalMargin, horizontalMargin, component);
		component.add(nameField);
		component.add(spinner);
		this.createBufferRectangle(verticalMargin, horizontalMargin, component);
	}

	private void updateCollaborativeComponent(final JComponent component,
			final CollaborativeTask task) {

		final JPanel namePanel;
		final JTextField initiatorField;
		final JTextField reactorField;

		final JLabel percent;

		final SpinnerNumberModel model;
		final JSpinner spinner;
		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !task.getLibrary().isReadOnly();

		namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

		if (graph.getStartNode() == task) {
			initiatorField = new JTextField("Start Node");
			reactorField = new JTextField("Start Node");
			initiatorField.setEnabled(false);
			reactorField.setEnabled(false);
		} else {
			initiatorField = new JTextField(task.getInitiatorName());
			reactorField = new JTextField(task.getResponderName());
			initiatorField.setEnabled(isEditable);
			reactorField.setEnabled(isEditable);
		}

		model = new SpinnerNumberModel(100, 0.0f, 100f, 1.0f);
		spinner = new JSpinner(model);
		spinner.setValue(task.getChance());

		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				task.setChance((Integer) spinner.getValue());
			}
		});

		spinner.setEnabled(isEditable);

		percent = new JLabel(" %");
		percent.setForeground(ScriptEaseUI.SE_BLACK);

		WidgetDecorator.decorateJTextFieldForFocusEvents(initiatorField,
				new Runnable() {

					@Override
					public void run() {
						task.setInitiatorName(initiatorField.getText());

						graph.revalidate();
						graph.repaint();
					}
				});

		WidgetDecorator.decorateJTextFieldForFocusEvents(reactorField,
				new Runnable() {

					@Override
					public void run() {
						task.setResponderName(reactorField.getText());

						graph.revalidate();
						graph.repaint();
					}
				});

		if (graph.isReadOnly()) {
			initiatorField.setEditable(false);
			reactorField.setEditable(false);
		}

		namePanel.add(initiatorField);
		namePanel.add(reactorField);

		component.add(namePanel);
		component.add(spinner);
		component.add(percent);
	}
}
