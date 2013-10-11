package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import scriptease.ScriptEase;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.ScriptEaseKeywords;
import scriptease.util.StringOp;

/**
 * A factory used to create a library editor. This is a singleton class, so use
 * the {@link #getInstance()} method to work with it.
 * 
 * @author kschenk
 * @author jyuen
 */
public class LibraryEditorPanelFactory {
	private static LibraryEditorPanelFactory instance = new LibraryEditorPanelFactory();
	public static Font labelFont = new Font("SansSerif", Font.BOLD,
			Integer.parseInt(ScriptEase.getInstance().getPreference(
					ScriptEase.FONT_SIZE_KEY)) + 1);

	// Stores the current observers for the selected StoryComponent so that they
	// do not get garbage collected.
	private StoryComponentObserver currentNameObserver;
	private StoryComponentObserver currentLabelObserver;

	/**
	 * Returns the single instance of StoryComponentBuilderPanelFactory
	 * 
	 * @return
	 */
	public static LibraryEditorPanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a JPanel with fields for Name, Labels, and a check box for
	 * Visibility. This JPanel is common to all library editor panes.
	 * 
	 * @param libraryPane
	 *            The LibraryPanel to be acted on.
	 * 
	 * @return
	 */
	public JPanel buildLibraryEditorPanel() {
		final JPanel editorPanel;
		editorPanel = new LibraryEditorPanel();

		return editorPanel;
	}

	// ******************* BEHAVIOUR EDITING PANEL ************************* //

	/**
	 * Builds a panel used to edit a behaviour.
	 * 
	 * @param behaviour
	 * @return
	 */
	@SuppressWarnings("serial")
	public JPanel buildBehaviourEditingPanel(final Behaviour behaviour) {
		final JPanel behaviourPanel;

		final JPanel buttonsPanel;
		final JButton independentButton;
		final JButton collaborativeButton;
		final JLabel warningLabel;

		behaviourPanel = new JPanel();
		behaviourPanel
				.setLayout(new BoxLayout(behaviourPanel, BoxLayout.Y_AXIS));

		// Create buttons panel
		buttonsPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 60;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 60;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 60;
				return dimension;
			}
		};

		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonsPanel.setBorder(BorderFactory
				.createTitledBorder("Behaviour Type"));

		independentButton = new JButton("Independent");
		collaborativeButton = new JButton("Collaborative");
		warningLabel = new JLabel(
				"Warning: By changing the behaviour type, you are removing any existing tasks.");
		warningLabel.setForeground(Color.RED);

		independentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (Component component : behaviourPanel.getComponents()) {
					if (component != buttonsPanel)
						behaviourPanel.remove(component);
				}

				behaviour.setStartTask(null);
				LibraryEditorPanelFactory.this.buildIndependentBehaviourPanel(
						behaviour, behaviourPanel);
			}
		});

		collaborativeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Component component : behaviourPanel.getComponents()) {
					if (component != buttonsPanel)
						behaviourPanel.remove(component);
				}

				behaviour.setStartTask(null);
				LibraryEditorPanelFactory.this
						.buildCollaborativeBehaviourPanel(behaviour,
								behaviourPanel);
			}
		});

		buttonsPanel.add(independentButton);
		buttonsPanel.add(collaborativeButton);
		buttonsPanel.add(warningLabel);

		behaviourPanel.add(buttonsPanel);

		if (behaviour.getType() == Behaviour.Type.INDEPENDENT
				|| behaviour.getType() == null) {
			LibraryEditorPanelFactory.this.buildIndependentBehaviourPanel(
					behaviour, behaviourPanel);
		} else {
			LibraryEditorPanelFactory.this.buildCollaborativeBehaviourPanel(
					behaviour, behaviourPanel);
		}

		return behaviourPanel;
	}

	private JPanel buildBehaviourGraphPanel(String graphName,
			SEGraph<Task> graph) {
		final JPanel graphPanel;

		// Create the graph panel.
		graphPanel = new JPanel();
		graphPanel.setBorder(BorderFactory.createTitledBorder(graphName));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.X_AXIS));

		graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		return graphPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildBehaviourToolbarPanel(SEGraph<Task> graph) {
		final JPanel toolbarPanel;

		toolbarPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 70;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 70;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 70;
				return dimension;
			}
		};

		toolbarPanel.setBorder(BorderFactory
				.createTitledBorder("Graph Toolbar"));
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		toolbarPanel.add(graph.getToolBar());
		graph.getToolBar().setHorizontal();

		return toolbarPanel;
	}

	private SEGraph<Task> buildBehaviourGraph(final Behaviour behaviour,
			final JPanel behaviourPanel, final String type) {
		final SEGraph<Task> graph;
		final Task startTask;

		if (behaviour.getStartTask() != null) {
			// If behaviour is defined, build its existing task graph
			startTask = behaviour.getStartTask();
		} else {
			// Else create a new one.
			if (type == ScriptEaseKeywords.INDEPENDENT)
				startTask = new IndependentTask("");
			else
				startTask = new CollaborativeTask("", "");

			behaviour.setStartTask(startTask);
		}

		graph = SEGraphFactory.buildTaskGraph(startTask);
		graph.setAlignmentY(JPanel.LEFT_ALIGNMENT);

		graph.addSEGraphObserver(new SEGraphAdapter<Task>() {

			@Override
			public void nodesSelected(final Collection<Task> nodes) {
				final JPanel effectsPanel = new JPanel();

				effectsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = behaviourPanel
						.getComponent(behaviourPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel) {
					final JPanel panel = (JPanel) lastComponent;

					if (panel.getComponentCount() > 0
							&& panel.getComponent(0) instanceof TaskEffectsPanel) {
						behaviourPanel.remove(lastComponent);
					}
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();

				if (task instanceof IndependentTask) {
					final Collection<ScriptIt> effects = new ArrayList<ScriptIt>();

					for (StoryComponent child : task.getChildren())
						effects.add((ScriptIt) child);

					effectsPanel.add(new TaskEffectsPanel("Task Panel", task,
							TaskEffectsPanel.TYPE.INDEPENDENT));

				} else if (task instanceof CollaborativeTask) {
					final Collection<ScriptIt> initiatorEffects = new ArrayList<ScriptIt>();
					final Collection<ScriptIt> collaboratorEffects = new ArrayList<ScriptIt>();

					final StoryComponentContainer initiatorContainer = (StoryComponentContainer) task
							.getChildAt(0);
					final StoryComponentContainer collaboratorContainer = (StoryComponentContainer) task
							.getChildAt(1);

					for (StoryComponent child : initiatorContainer
							.getChildren())
						initiatorEffects.add((ScriptIt) child);

					for (StoryComponent child : collaboratorContainer
							.getChildren())
						collaboratorEffects.add((ScriptIt) child);

					effectsPanel.add(new TaskEffectsPanel(
							"Initiator Task Panel", task,
							TaskEffectsPanel.TYPE.COLLABORATIVE_INIT));
					effectsPanel.add(new TaskEffectsPanel(
							"Collaborator Task Panel", task,
							TaskEffectsPanel.TYPE.COLLABORATIVE_REACT));
				}

				behaviourPanel.add(effectsPanel);
				behaviourPanel.repaint();
				behaviourPanel.revalidate();
			}

			@Override
			public void nodeOverwritten(Task task) {
				task.revalidateKnowItBindings();
			}

			@Override
			public void nodeRemoved(Task task) {
				task.revalidateKnowItBindings();
			}
		});

		return graph;
	}

	private void buildIndependentBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, ScriptEaseKeywords.INDEPENDENT);

		behaviourPanel.add(this.buildBehaviourToolbarPanel(graph));
		behaviourPanel.add(this.buildBehaviourGraphPanel("Independent Graph",
				graph));
		behaviourPanel.repaint();
		behaviourPanel.revalidate();
	}

	private void buildCollaborativeBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, ScriptEaseKeywords.COLLABORATIVE);

		behaviourPanel.add(this.buildBehaviourToolbarPanel(graph));
		behaviourPanel.add(this.buildBehaviourGraphPanel("Collaborative Graph",
				graph));

		behaviourPanel.repaint();
		behaviourPanel.revalidate();
	}

	// *************** DESCRIPTION EDITING PANEL *************************

	/**
	 * Builds a panel used to edit a KnowItBindingDescribeIt.
	 * 
	 * @param describeIt
	 * @param knowIt
	 * @return
	 */
	public JComponent buildDescribeItEditingPanel(final DescribeIt describeIt,
			final KnowIt knowIt) {

		final JPanel bindingPanel;
		final JPanel describeItGraphPanel;

		final EffectHolderPanel effectHolder;
		final SetEffectObserver effectObserver;
		final SEGraph<DescribeItNode> graph;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();

		effectHolder = new EffectHolderPanel(describeIt.getTypes());

		graph = SEGraphFactory.buildDescribeItEditorGraph(describeIt
				.getStartNode());

		// Set the effectHolder to reflect the initial path of the describeIt
		// (since it doesn't throw a path selection even in SEGraph the
		// constructor)
		final ScriptIt initialScriptIt = describeIt.getScriptItForPath(graph
				.getSelectedNodes());
		effectHolder.setEffect(initialScriptIt);

		effectObserver = new SetEffectObserver() {
			@Override
			public void effectChanged(ScriptIt newEffect) {
				// We need to make a copy or else the path is ALWAYS the current
				// selected nodes, which is not what we want at all.
				final Collection<DescribeItNode> selectedNodes;

				selectedNodes = new ArrayList<DescribeItNode>(
						graph.getSelectedNodes());

				describeIt.assignScriptItToPath(selectedNodes, newEffect);

				final ScriptIt scriptItForPath = describeIt
						.getScriptItForPath(describeIt.getShortestPath());
				if (scriptItForPath != null) {
					knowIt.setBinding(scriptItForPath);
				} else {
					knowIt.clearBinding();
				}

			}
		};

		graph.addSEGraphObserver(new SEGraphAdapter<DescribeItNode>() {

			@Override
			public void nodesSelected(Collection<DescribeItNode> nodes) {
				final ScriptIt pathScriptIt;
				pathScriptIt = describeIt.getScriptItForPath(nodes);

				effectHolder.removeSetEffectObserver(effectObserver);
				effectHolder.setEffect(pathScriptIt);
				effectHolder.addSetEffectObserver(effectObserver);
			}
		});

		effectHolder.addSetEffectObserver(effectObserver);

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graph.getToolBar(), BorderLayout.WEST);
		describeItGraphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		bindingPanel
				.setLayout(new BoxLayout(bindingPanel, BoxLayout.PAGE_AXIS));
		bindingPanel.setBorder(BorderFactory
				.createTitledBorder("DescribeIt Binding"));

		bindingPanel.add(effectHolder);
		bindingPanel.add(describeItGraphPanel);

		return bindingPanel;
	}

	/**
	 * Builds a JTextField used to edit the name of the story component. The
	 * TextField gets updated if the Story Component's name changes for other
	 * reasons, such as undoing.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildNameEditorPanel(final StoryComponent component) {
		final JTextField nameField;
		final Runnable commitText;

		nameField = new JTextField(component.getDisplayText());

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					String text = nameField.getText();
					if (!text.equals(component.getDisplayText())) {
						UndoManager.getInstance().startUndoableAction(
								"Change " + component + "'s display text to "
										+ text);
						component.setDisplayText(text);

						if (UndoManager.getInstance().hasOpenUndoableAction())
							UndoManager.getInstance().endUndoableAction();
					}
				}
			}
		};

		this.currentNameObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
					nameField.setText(component.getDisplayText());
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false, Color.white);

		nameField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentNameObserver);

		return nameField;
	}

	/**
	 * Builds a JTextField used to edit the labels of a story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildLabelEditorField(final StoryComponent component) {
		final String SEPARATOR = ", ";

		final JTextField labelField;
		final String labelToolTip;
		final Runnable commitText;

		labelField = new JTextField(StringOp.getCollectionAsString(
				component.getLabels(), SEPARATOR));
		labelToolTip = "<html><b>Labels</b> are seperated by commas.<br>"
				+ "Leading and trailing spaces are<br>"
				+ "removed automatically.</html>";

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					final Set<String> labels = new HashSet<String>();
					final String labelFieldText = labelField.getText();
					final String[] labelArray = labelFieldText.split(SEPARATOR);
					for (String label : labelArray) {
						labels.add(label.trim());
					}

					UndoManager.getInstance().startUndoableAction(
							"Setting " + component + "'s labels to "
									+ labelFieldText);
					component.setLabels(labels);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		this.currentLabelObserver = new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				StoryComponentChangeEnum eventType = event.getType();
				if (eventType == StoryComponentChangeEnum.CHANGE_LABELS_CHANGED) {
					labelField.setText(StringOp.getCollectionAsString(
							component.getLabels(), SEPARATOR));
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(labelField,
				commitText, false, Color.white);

		labelField.setToolTipText(labelToolTip);

		labelField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(this.currentLabelObserver);

		return labelField;
	}

	/**
	 * Builds a JCheckBox to set a component's visibility.
	 * 
	 * @param component
	 * @return
	 */
	private JCheckBox buildVisibleBox(final StoryComponent component) {
		final JCheckBox visibleBox;

		visibleBox = new JCheckBox();
		visibleBox.setSelected(component.isVisible());
		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					UndoManager.getInstance().startUndoableAction(
							"Toggle " + component + "'s visiblity");
					component.setVisible(visibleBox.isSelected());
					UndoManager.getInstance().endUndoableAction();
				}
			}
		});
		component.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_VISIBILITY) {
					visibleBox.setSelected(component.isVisible());
				}
			}
		});

		return visibleBox;
	}

	/**
	 * Builds a panel containing a name, label, and visibility editor.
	 * 
	 * @param component
	 * @return
	 */
	public JPanel buildDescriptorPanel(StoryComponent component) {
		final JPanel descriptorPanel;
		final GroupLayout descriptorPanelLayout;

		final JLabel nameLabel;
		final JLabel labelLabel;
		final JLabel visibleLabel;

		final JTextField nameField;
		final JTextField labelsField;
		final JCheckBox visibleBox;

		descriptorPanel = new JPanel();
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");

		nameField = this.buildNameEditorPanel(component);
		labelsField = this.buildLabelEditorField(component);
		visibleBox = this.buildVisibleBox(component);

		// Set up the labels
		nameLabel.setFont(labelFont);
		nameLabel.setLabelFor(nameField);

		labelLabel.setFont(labelFont);
		labelLabel.setLabelFor(labelsField);
		labelLabel.setToolTipText(labelsField.getToolTipText());

		visibleLabel.setFont(labelFont);
		visibleLabel.setLabelFor(visibleBox);

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setBorder(new TitledBorder("Component Descriptors"));

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		// Add JComponents to DescriptorPanel using GroupLayout
		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createParallelGroup().addGroup(
						descriptorPanelLayout
								.createSequentialGroup()
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(nameLabel)
												.addComponent(visibleLabel)
												.addComponent(labelLabel))
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(visibleBox)
												.addComponent(nameField)
												.addComponent(labelsField))));

		descriptorPanelLayout.setVerticalGroup(descriptorPanelLayout
				.createSequentialGroup()
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(visibleLabel)
								.addComponent(visibleBox))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(nameLabel)
								.addComponent(nameField))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(labelLabel)
								.addComponent(labelsField)));

		return descriptorPanel;
	}

	/**
	 * Builds a parameter panel.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param knowIt
	 * @return
	 */
	public JPanel buildParameterPanel(ScriptIt scriptIt, CodeBlock codeBlock,
			KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}

}
