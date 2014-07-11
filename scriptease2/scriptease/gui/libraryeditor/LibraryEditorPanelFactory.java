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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import scriptease.ScriptEase;
import scriptease.controller.observer.CodeBlockPanelObserver;
import scriptease.controller.observer.ParameterPanelObserver;
import scriptease.controller.observer.SetEffectObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.libraryeditor.codeblocks.CodeBlockPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.complex.behaviours.Task;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;
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
		return new LibraryEditorPanel();
	}

	/**
	 * Panel used to edit ActivityIts.
	 * 
	 * @param activityIt
	 * @return
	 */
	public JPanel buildActivityItEditingPanel(final ActivityIt activityIt) {
		final JPanel activityPanel;
		final CodeBlockPanel codeBlockPanel;
		final StoryComponentPanel transferPanel;
		final boolean isEditable = !activityIt.getLibrary().isReadOnly()
				|| ScriptEase.DEBUG_MODE;

		activityPanel = new JPanel();
		activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
		activityPanel.setOpaque(false);

		transferPanel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(activityIt);

		codeBlockPanel = new CodeBlockPanel(activityIt.getMainCodeBlock(),
				activityIt, true);

		activityPanel.add(codeBlockPanel, 0);
		activityPanel.add(this.buildActivityItParameterPanel(activityIt), 1);

		if (isEditable) {
			activityPanel.add(new StoryComponentPanelTree(transferPanel), 2);
		} else {
			JPanel subPanel = new JPanel();
			subPanel.add(new JLabel(new ImageIcon(GUIOp
					.getScreenshot(new StoryComponentPanelTree(transferPanel)))));
			activityPanel.add(subPanel, 2);
		}

		codeBlockPanel.addListener(new CodeBlockPanelObserver() {

			// Rebuilds the implicit panel and StoryComponentPanelTree when a
			// parameter has changed
			@Override
			public void codeBlockPanelChanged() {
				final StoryComponentPanel newTransferPanel = StoryComponentPanelFactory
						.getInstance().buildStoryComponentPanel(activityIt);

				// Should always be able to remove the tree regardless
				if (activityPanel.getComponentCount() > 1
						&& activityPanel.getComponent(1) != null) {
					activityPanel.remove(1);
				}

				activityPanel.add(buildActivityItParameterPanel(activityIt), 1);

				// Checking if getComponent(3) is null results in an out of
				// bounds exception so we have to make sure we list is big
				// enough before doing our null check
				if (activityPanel.getComponentCount() > 2
						&& activityPanel.getComponent(2) != null) {
					activityPanel.remove(2);
				}
				activityPanel.add(
						new StoryComponentPanelTree(newTransferPanel), 2);
				activityPanel.repaint();
				activityPanel.revalidate();
			}
		});

		activityIt.addStoryComponentObserver(activityPanel,
				new StoryComponentObserver() {

					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME
								|| event.getType() == StoryComponentChangeEnum.CHANGE_PARAMETER_TYPE) {
							final StoryComponentPanel newTransferPanel = StoryComponentPanelFactory
									.getInstance().buildStoryComponentPanel(
											activityIt);

							if (activityPanel.getComponents().length >= 1
									&& activityPanel.getComponent(1) != null) {
								activityPanel.remove(1);
							}

							activityPanel.add(
									buildActivityItParameterPanel(activityIt),
									1);

							if (activityPanel.getComponents().length >= 2
									&& activityPanel.getComponent(2) != null) {
								activityPanel.remove(2);
							}
							activityPanel.add(new StoryComponentPanelTree(
									newTransferPanel), 2);

							activityPanel.repaint();
							activityPanel.revalidate();
						}
					}
				});

		return activityPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildActivityItParameterPanel(final ActivityIt activityIt) {
		final JPanel parameterPanel;

		/*
		 * Sets a fixed height for panels. Probably not the cleanest way to do
		 * this. Refactor if necessary.
		 */
		parameterPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 40;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 40;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 40;
				return dimension;
			}
		};

		parameterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		final CodeBlock codeBlock = activityIt.getMainCodeBlock();

		for (KnowIt parameter : codeBlock.getParameters()) {
			parameterPanel.add(ScriptWidgetFactory.buildBindingWidget(
					parameter, false));
		}

		return parameterPanel;
	}

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
		final JLabel readOnlyLabel;

		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !behaviour.getLibrary().isReadOnly();

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

		independentButton.setEnabled(isEditable);
		collaborativeButton.setEnabled(isEditable);

		warningLabel = new JLabel(
				"Warning: By changing the behaviour type, you are removing any existing behaviour owner(s) and tasks.");
		warningLabel.setForeground(Color.RED);
		readOnlyLabel = new JLabel(
				"This element is from a read-only library and cannot be edited.");

		readOnlyLabel.setFont(labelFont);
		readOnlyLabel.setForeground(ScriptEaseUI.SE_BLUE);

		behaviour.addStoryComponentObserver(new StoryComponentObserver() {

			@Override
			public void componentChanged(StoryComponentEvent event) {
				final StoryComponentChangeEnum type = event.getType();

				if (type == StoryComponentChangeEnum.CHANGE_BEHAVIOUR_TYPE) {
					for (Component component : behaviourPanel.getComponents()) {
						if (component != buttonsPanel)
							behaviourPanel.remove(component);
					}

					LibraryEditorPanelFactory.this
							.buildChangeableBehaviourPanel(behaviour,
									behaviourPanel);
				}
			}
		});

		independentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				behaviour.setType(Behaviour.Type.INDEPENDENT);
			}
		});

		collaborativeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				behaviour.setType(Behaviour.Type.COLLABORATIVE);
			}
		});

		if (!isEditable) {
			JPanel readOnlyPanel = new JPanel();
			readOnlyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			readOnlyPanel.add(readOnlyLabel);
			behaviourPanel.add(readOnlyPanel);
		}
		buttonsPanel.add(independentButton);
		buttonsPanel.add(collaborativeButton);
		buttonsPanel.add(warningLabel);

		behaviourPanel.add(buttonsPanel);

		LibraryEditorPanelFactory.this.buildChangeableBehaviourPanel(behaviour,
				behaviourPanel);

		return behaviourPanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildBehaviourGraphPanel(SEGraph<Task> graph,
			boolean isEditable) {
		final JPanel graphPanel;

		// Create the graph panel.
		graphPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				final Dimension dimension = super.getPreferredSize();
				dimension.height = 245;
				return dimension;
			}

			@Override
			public Dimension getMaximumSize() {
				final Dimension dimension = super.getMaximumSize();
				dimension.height = 245;
				return dimension;
			}

			@Override
			public Dimension getMinimumSize() {
				final Dimension dimension = super.getMinimumSize();
				dimension.height = 245;
				return dimension;
			}
		};

		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.X_AXIS));

		if (isEditable) {
			graphPanel.add(graph.getToolBar());
		}
		graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);

		return graphPanel;
	}

	private SEGraph<Task> buildBehaviourGraph(final Behaviour behaviour,
			final JPanel behaviourPanel, final Behaviour.Type type) {
		final SEGraph<Task> graph;
		final Task startTask;
		final LibraryModel library = behaviour.getLibrary();
		final boolean isEditable = !library.isReadOnly()
				|| ScriptEase.DEBUG_MODE;

		if (behaviour.getStartTask() != null) {
			// If behaviour is defined, build its existing task graph
			startTask = behaviour.getStartTask();
		} else {
			// Else create a new one.
			if (type == Behaviour.Type.INDEPENDENT)
				startTask = new IndependentTask(library);
			else
				startTask = new CollaborativeTask(library);

			behaviour.setStartTask(startTask);
		}

		graph = SEGraphFactory.buildTaskGraph(startTask, false);

		graph.setAlignmentY(JPanel.LEFT_ALIGNMENT);

		graph.addSEGraphObserver(new SEGraphAdapter<Task>() {

			@Override
			public void nodesSelected(final Collection<Task> nodes) {
				final JPanel taskPanel = new JPanel();
				final FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
				layout.setAlignOnBaseline(true);

				taskPanel.setLayout(layout);
				taskPanel.setBorder(BorderFactory
						.createTitledBorder("Task Panel"));

				// Remove the previous task's effects panel if there is one.
				final Component lastComponent = behaviourPanel
						.getComponent(behaviourPanel.getComponents().length - 1);

				if (lastComponent instanceof JPanel) {
					final JPanel panel = (JPanel) lastComponent;

					if (panel.getComponentCount() > 0) {
						final Component comp = panel.getComponent(0);

						if (comp instanceof JScrollPane
								|| comp instanceof JSplitPane
								|| comp instanceof JLabel)
							behaviourPanel.remove(lastComponent);
					}
				}

				// Set up the effects panel for the task we selected.
				final Task task = nodes.iterator().next();

				if (task instanceof IndependentTask
						&& task != behaviour.getStartTask()) {

					final StoryComponentPanelTree storyComponentPanelTree;

					StoryComponentPanel initiatorTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((IndependentTask) task)
											.getInitiatorContainer());

					storyComponentPanelTree = new StoryComponentPanelTree(
							initiatorTaskPanel);

					storyComponentPanelTree.setBorder(BorderFactory
							.createEmptyBorder());

					if (isEditable) {
						taskPanel.add(storyComponentPanelTree);
					} else {
						taskPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
						taskPanel.add(new JLabel(new ImageIcon(GUIOp
								.getScreenshot(storyComponentPanelTree))));
					}

				} else if (task instanceof CollaborativeTask
						&& task != behaviour.getStartTask()) {

					final StoryComponentPanelTree initiatorPanelTree;
					final StoryComponentPanelTree responderPanelTree;

					StoryComponentPanel initiatorTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((CollaborativeTask) task)
											.getInitiatorContainer());

					StoryComponentPanel responderTaskPanel = StoryComponentPanelFactory
							.getInstance().buildStoryComponentPanel(
									((CollaborativeTask) task)
											.getResponderContainer());

					initiatorPanelTree = new StoryComponentPanelTree(
							initiatorTaskPanel);
					responderPanelTree = new StoryComponentPanelTree(
							responderTaskPanel);

					initiatorPanelTree.setBorder(BorderFactory
							.createEmptyBorder());
					responderPanelTree.setBorder(BorderFactory
							.createEmptyBorder());

					JSplitPane splitPane = new JSplitPane(
							JSplitPane.VERTICAL_SPLIT, initiatorPanelTree,
							responderPanelTree);
					splitPane.setResizeWeight(0.5);
					taskPanel.setLayout(new BoxLayout(taskPanel,
							BoxLayout.X_AXIS));

					// Provide minimum sizes for the two components in the split
					// pane
					// Should be enough space for each panel to have 3 effects
					// event at low resolutions.
					Dimension minimumSize = new Dimension(500, 300);
					splitPane.setMinimumSize(minimumSize);
					// splitPane.setPreferredSize(minimumSize);

					if (isEditable) {
						taskPanel.add(splitPane);
					} else {
						taskPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
						taskPanel.add(new JLabel(new ImageIcon(GUIOp
								.getScreenshot(splitPane))));
					}

				} else {
					// Here is what we do for start task nodes

					final JLabel startLabel;

					startLabel = new JLabel(
							"You cannot add any components to the start task node!");
					taskPanel.add(startLabel);
				}

				behaviourPanel.add(taskPanel);
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

	private JPanel buildBehaviourParameterPanel(final Behaviour behaviour,
			boolean isEditable) {
		final JPanel paramPanel;
		final Collection<KnowIt> parameters = behaviour.getParameters();

		paramPanel = new JPanel();
		paramPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
		paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));

		for (final KnowIt parameter : parameters) {
			final BindingWidget bindingWidget = ScriptWidgetFactory
					.buildBindingWidget(parameter, false);
			final JPanel subPanel = new JPanel();
			final ParameterPanel parameterPanel;

			subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			subPanel.add(bindingWidget);

			if (!parameter.getDisplayText().equals(Behaviour.PRIORITY_TEXT)) {
				parameterPanel = buildParameterPanel(parameter, isEditable);

				parameterPanel.addListener(new ParameterPanelObserver() {
					@Override
					public void parameterPanelChanged() {
						subPanel.remove(0);
						subPanel.add(ScriptWidgetFactory.buildBindingWidget(
								parameter, false), 0);
						subPanel.revalidate();
					}
				});

				parameterPanel.setEnabled(isEditable);
				subPanel.add(parameterPanel, false);
			}

			paramPanel.add(subPanel, false);
		}

		final Dimension dimension = paramPanel.getPreferredSize();
		dimension.height = parameters.size() * 90;
		dimension.width = paramPanel.getMaximumSize().width;
		paramPanel.setMaximumSize(dimension);

		return paramPanel;
	}

	private void buildChangeableBehaviourPanel(final Behaviour behaviour,
			final JPanel behaviourPanel) {
		final Behaviour.Type type = behaviour.getType();
		final SEGraph<Task> graph = this.buildBehaviourGraph(behaviour,
				behaviourPanel, type);

		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !behaviour.getLibrary().isReadOnly();

		graph.setBorder(BorderFactory.createEmptyBorder());

		if (type == Behaviour.Type.INDEPENDENT) {
			behaviourPanel.add(this
					.buildIndependentBehaviourNamePanel(behaviour));
		} else if (type == Behaviour.Type.COLLABORATIVE) {
			behaviourPanel.add(this
					.buildCollaborativeBehaviourNamePanel(behaviour));
		}

		behaviourPanel.add(this.buildBehaviourGraphPanel(graph, isEditable));
		behaviourPanel.add(this.buildBehaviourParameterPanel(behaviour,
				isEditable));

		graph.setSelectedNode(graph.getStartNode());

		behaviourPanel.repaint();
		behaviourPanel.revalidate();
	}

	@SuppressWarnings("serial")
	private JPanel buildCollaborativeBehaviourNamePanel(
			final Behaviour behaviour) {
		final JPanel namePanel;

		final JLabel initiatorLabel;
		final JLabel priorityLabel;

		final JTextField actionField;
		final JTextField priorityField;

		final Runnable commitText;
		final Runnable commitPriority;

		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !behaviour.getLibrary().isReadOnly();

		namePanel = new JPanel() {
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
		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBorder(BorderFactory.createTitledBorder("Behaviour Name"));

		initiatorLabel = new JLabel("Initiator ");
		initiatorLabel.setFont(LibraryEditorPanelFactory.labelFont);
		priorityLabel = new JLabel(" Responder with priority ");
		priorityLabel.setFont(LibraryEditorPanelFactory.labelFont);

		final String displayText = behaviour.getDisplayText();

		final String actionName = displayText.substring(
				displayText.indexOf("<") + 12,
				displayText.indexOf(" <Responder>"));
		actionField = new JTextField(actionName, 15);

		final String priority = Integer.toString(behaviour.getPriority());
		priorityField = ComponentFactory.buildNumberTextField();
		priorityField.setText(priority);
		priorityField.setColumns(5);

		actionField.setEnabled(isEditable);
		priorityField.setEnabled(isEditable);

		// Add listeners for the text fields

		commitText = new Runnable() {
			@Override
			public void run() {
				final String oldDisplayText = behaviour.getDisplayText();

				final String oldActionName = oldDisplayText.substring(
						oldDisplayText.indexOf("<") + 12,
						oldDisplayText.indexOf(" <Responder>"));

				if (actionField.getText().contains("<")
						|| actionField.getText().contains(">"))
					return;

				behaviour.setDisplayText(behaviour.getDisplayText().replace(
						oldActionName, actionField.getText()));

			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(actionField,
				commitText, false);

		commitPriority = new Runnable() {
			@Override
			public void run() {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(2);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(priorityField,
				commitPriority, false);

		namePanel.add(initiatorLabel);
		namePanel.add(actionField);
		namePanel.add(priorityLabel);
		namePanel.add(priorityField);

		return namePanel;
	}

	@SuppressWarnings("serial")
	private JPanel buildIndependentBehaviourNamePanel(final Behaviour behaviour) {
		final JPanel namePanel;

		final JLabel initiatorLabel;
		final JLabel priorityLabel;

		final JTextField actionField;
		final JTextField priorityField;

		final Runnable commitText;
		final Runnable commitPriority;
		final boolean isEditable = ScriptEase.DEBUG_MODE
				|| !behaviour.getLibrary().isReadOnly();

		namePanel = new JPanel() {
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

		namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		namePanel.setBorder(BorderFactory.createTitledBorder("Behaviour Name"));

		initiatorLabel = new JLabel("Initiator ");
		initiatorLabel.setFont(LibraryEditorPanelFactory.labelFont);
		priorityLabel = new JLabel(" with priority ");
		priorityLabel.setFont(LibraryEditorPanelFactory.labelFont);

		final String displayText = behaviour.getDisplayText();

		final String actionName = displayText.substring(
				displayText.indexOf("<") + 12,
				displayText.indexOf(" with priority"));
		actionField = new JTextField(actionName, 15);

		final String priority = Integer.toString(behaviour.getPriority());
		priorityField = ComponentFactory.buildNumberTextField();
		priorityField.setText(priority);
		priorityField.setColumns(5);

		actionField.setEnabled(isEditable);
		priorityField.setEnabled(isEditable);

		// Add listeners for the text fields

		commitText = new Runnable() {
			@Override
			public void run() {
				final String oldDisplayText = behaviour.getDisplayText();

				final String oldActionName = oldDisplayText.substring(
						oldDisplayText.indexOf("<") + 12,
						oldDisplayText.indexOf(" with priority"));

				if (actionField.getText().contains("<")
						|| actionField.getText().contains(">"))
					return;

				behaviour.setDisplayText(behaviour.getDisplayText().replace(
						oldActionName, actionField.getText()));
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(actionField,
				commitText, false);

		commitPriority = new Runnable() {
			@Override
			public void run() {
				final KnowIt priorityKnowIt = behaviour.getMainCodeBlock()
						.getParameters().get(1);
				final String priority = priorityField.getText();

				behaviour.setPriority(Integer.parseInt(priority));

				priorityKnowIt.setBinding(new SimpleResource(priorityKnowIt
						.getTypes(), priority));

				priorityKnowIt.revalidateKnowItBindings();

				behaviour.setDisplayText(behaviour.getDisplayText());

			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(priorityField,
				commitPriority, false);

		namePanel.add(initiatorLabel);
		namePanel.add(actionField);
		namePanel.add(priorityLabel);
		namePanel.add(priorityField);

		return namePanel;
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

		final boolean isEditable;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();

		effectHolder = new EffectHolderPanel(describeIt.getTypes());

		isEditable = ScriptEase.DEBUG_MODE || !knowIt.getLibrary().isReadOnly();

		graph = SEGraphFactory.buildDescribeItEditorGraph(
				describeIt.getStartNode(), isEditable);

		describeItGraphPanel.setOpaque(false);
		effectHolder.setBackground(ScriptEaseUI.SECONDARY_UI);
		bindingPanel.setOpaque(false);

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

		final JScrollPane graphScroll = new JScrollPane(graph);

		graphScroll.setBorder(BorderFactory.createEmptyBorder());

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graph.getToolBar(), BorderLayout.WEST);
		describeItGraphPanel.add(graphScroll, BorderLayout.CENTER);

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

		WidgetDecorator.decorateJTextFieldForFocusEvents(nameField, commitText,
				false);

		nameField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(nameField,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_TEXT_NAME) {
							nameField.setText(component.getDisplayText());
						}
					}
				});

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

		WidgetDecorator.decorateJTextFieldForFocusEvents(labelField,
				commitText, false);

		labelField.setToolTipText(labelToolTip);

		labelField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(labelField,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						StoryComponentChangeEnum eventType = event.getType();
						if (eventType == StoryComponentChangeEnum.CHANGE_LABELS_CHANGED) {
							labelField.setText(StringOp.getCollectionAsString(
									component.getLabels(), SEPARATOR));
						}
					}
				});

		return labelField;
	}

	/**
	 * Builds a JTextField used to edit the labels of a story component.
	 * 
	 * @param component
	 * @return
	 */
	private JTextField buildDescriptionEditorField(
			final StoryComponent component) {
		final JTextField descriptionField;
		final String labelToolTip;
		final Runnable commitText;

		descriptionField = new JTextField(component.getDescription());
		labelToolTip = "<html>The <b>Description</b> shows up when<br>"
				+ "the user hovers over this component.</html>";

		commitText = new Runnable() {
			@Override
			public void run() {
				if (!UndoManager.getInstance().hasOpenUndoableAction()) {
					final String description = descriptionField.getText();

					UndoManager.getInstance().startUndoableAction(
							"Setting " + component + "'s description to "
									+ description);
					component.setDescription(description);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		};

		WidgetDecorator.decorateJTextFieldForFocusEvents(descriptionField,
				commitText, false);

		descriptionField.setToolTipText(labelToolTip);

		descriptionField.setHorizontalAlignment(JTextField.LEADING);

		component.addStoryComponentObserver(descriptionField,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						StoryComponentChangeEnum eventType = event.getType();
						if (eventType == StoryComponentChangeEnum.CHANGE_TEXT_DESCRIPTION) {
							descriptionField.setText(component.getDescription());
						}
					}
				});

		return descriptionField;
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
		component.addStoryComponentObserver(visibleBox,
				new StoryComponentObserver() {
					@Override
					public void componentChanged(StoryComponentEvent event) {
						if (event.getType() == StoryComponentChangeEnum.CHANGE_VISIBILITY) {
							visibleBox.setSelected(component.isVisible());
						}
					}
				});

		visibleBox.setOpaque(false);

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
		final JLabel descriptionLabel;

		final JLabel visibleLabel;
		final JLabel readOnlyLabel;

		final JTextField nameField;
		final JTextField labelsField;
		final JTextField descriptionField;

		final JCheckBox visibleBox;

		final boolean isEditable;

		descriptorPanel = new JPanel();
		descriptorPanelLayout = new GroupLayout(descriptorPanel);

		nameLabel = new JLabel("Name: ");
		labelLabel = new JLabel("Labels: ");
		visibleLabel = new JLabel("Visible: ");
		descriptionLabel = new JLabel("Description: ");

		readOnlyLabel = new JLabel(
				"This element is from a read-only library and cannot be edited.");

		nameField = this.buildNameEditorPanel(component);
		labelsField = this.buildLabelEditorField(component);
		descriptionField = this.buildDescriptionEditorField(component);

		visibleBox = this.buildVisibleBox(component);

		// Check whether or not this StoryComponent should be editable (debug
		// mode, or not read-only)
		isEditable = ScriptEase.DEBUG_MODE
				|| !component.getLibrary().isReadOnly();

		// Set up the labels
		nameLabel.setFont(labelFont);
		labelLabel.setFont(labelFont);
		visibleLabel.setFont(labelFont);
		descriptionLabel.setFont(labelFont);
		readOnlyLabel.setFont(labelFont);
		readOnlyLabel.setForeground(ScriptEaseUI.SE_BLUE);

		labelLabel.setToolTipText(labelsField.getToolTipText());
		descriptionLabel.setToolTipText(descriptionField.getToolTipText());

		// Set up the descriptorPanel
		descriptorPanel.setLayout(descriptorPanelLayout);
		descriptorPanel.setOpaque(false);

		descriptorPanelLayout.setAutoCreateGaps(true);
		descriptorPanelLayout.setAutoCreateContainerGaps(true);
		descriptorPanelLayout.setHonorsVisibility(true);

		descriptorPanelLayout.setHorizontalGroup(descriptorPanelLayout
				.createParallelGroup().addGroup(
						descriptorPanelLayout
								.createSequentialGroup()
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(nameLabel)
												.addComponent(descriptionLabel)
												.addComponent(visibleLabel)
												.addComponent(labelLabel))
								.addGroup(
										descriptorPanelLayout
												.createParallelGroup()
												.addComponent(visibleBox)
												.addComponent(nameField)
												.addComponent(descriptionField)
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
								.addComponent(descriptionLabel)
								.addComponent(descriptionField))
				.addGroup(
						descriptorPanelLayout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(labelLabel)
								.addComponent(labelsField)));
		if (!isEditable) {
			final JPanel containerPanel = new JPanel(new BorderLayout());

			containerPanel.add(readOnlyLabel, BorderLayout.NORTH);
			containerPanel.add(descriptorPanel, BorderLayout.CENTER);

			visibleBox.setEnabled(false);
			nameField.setEnabled(false);
			labelsField.setEnabled(false);
			descriptionField.setEnabled(false);

			return containerPanel;
		}

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
	public ParameterPanel buildParameterPanel(ScriptIt scriptIt,
			CodeBlock codeBlock, KnowIt knowIt) {
		return new ParameterPanel(scriptIt, codeBlock, knowIt);
	}

	/**
	 * Builds a parameter panel for behaviour implicits.
	 * 
	 * @param scriptIt
	 * @param codeBlock
	 * @param knowIt
	 * @return
	 */
	public ParameterPanel buildParameterPanel(KnowIt knowIt, boolean isEditable) {
		return new ParameterPanel(knowIt, isEditable);
	}
}
