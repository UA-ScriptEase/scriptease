package scriptease.gui.libraryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
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
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.gui.component.ComponentFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.complex.ScriptIt;
import scriptease.util.StringOp;

/**
 * A factory used to create a library editor. This is a singleton class, so use
 * the {@link #getInstance()} method to work with it.
 * 
 * @author kschenk
 * 
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

	/**
	 * Builds a panel used to edit a KnowItBindingDescribeIt.
	 * 
	 * @param describeIt
	 * @param knowIt
	 * @return
	 */
	public JPanel buildDescribeItEditingPanel(final DescribeIt describeIt,
			final KnowIt knowIt) {
		final JPanel bindingPanel;
		final JPanel describeItGraphPanel;
		final JToolBar graphToolBar;

		final EffectHolderPanel effectHolder;
		final SetEffectObserver effectObserver;
		final SEGraph<DescribeItNode> graph;

		bindingPanel = new JPanel();
		describeItGraphPanel = new JPanel();
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();

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

		/*
		 * TODO We may need a listener that updates the graph on model changes.
		 * Not implementing this unless it's necessary, because the only case
		 * where this should happen is if we have two library editors open. In
		 * that case, we need to refactor a lot of code here anyways, incl this
		 */

		// Reset the ToolBar to select and add the Graph to it.
		GraphToolBarModeAction.setMode(ToolBarMode.SELECT);

		// Set up the JPanel containing the graph
		describeItGraphPanel.setLayout(new BorderLayout());
		describeItGraphPanel.add(graphToolBar, BorderLayout.WEST);
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
					final Collection<String> labels = new ArrayList<String>();
					final String labelFieldText = labelField.getText();
					if (!labelFieldText.isEmpty()) {
						final String[] labelArray = labelFieldText
								.split(SEPARATOR);
						for (String label : labelArray) {
							labels.add(label.trim());
						}
					}

					final Collection<String> oldLabels = component.getLabels();
					if (!oldLabels.containsAll(labels)) {
						UndoManager.getInstance().startUndoableAction(
								"Setting " + component + "'s labels to "
										+ labelFieldText);
						component.setLabels(labels);
						UndoManager.getInstance().endUndoableAction();
					}
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
