package scriptease.gui;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.action.story.graphs.ConnectGraphPointAction;
import scriptease.gui.action.story.graphs.DeleteGraphNodeAction;
import scriptease.gui.action.story.graphs.DisconnectGraphPointAction;
import scriptease.gui.action.story.graphs.InsertGraphNodeAction;
import scriptease.gui.action.story.graphs.SelectGraphNodeAction;
import scriptease.gui.action.story.graphs.quests.ToggleCommittingAction;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.quests.QuestEditor;
import scriptease.gui.quests.QuestPointNode;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs. A specialized Quest Editor Toolbar can also be
 * created.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {

	private static JTextField nameField;

	/**
	 * Builds a toolbar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths.
	 * 
	 * @return
	 */
	public static JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		final ArrayList<JToggleButton> buttonList = new ArrayList<JToggleButton>();

		final JToggleButton selectNodeButton = new JToggleButton(
				SelectGraphNodeAction.getInstance());

		final JToggleButton insertNodeButton = new JToggleButton(
				InsertGraphNodeAction.getInstance());

		final JToggleButton deleteNodeButton = new JToggleButton(
				DeleteGraphNodeAction.getInstance());

		final JToggleButton connectNodeButton = new JToggleButton(
				ConnectGraphPointAction.getInstance());

		final JToggleButton disconnectNodeButton = new JToggleButton(
				DisconnectGraphPointAction.getInstance());

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);

		buttonList.add(selectNodeButton);
		buttonList.add(insertNodeButton);
		buttonList.add(deleteNodeButton);
		buttonList.add(connectNodeButton);
		buttonList.add(disconnectNodeButton);

		for (JToggleButton toolBarButton : buttonList) {
			toolBarButton.setHideActionText(true);
			graphEditorButtonGroup.add(toolBarButton);
			graphEditorToolBar.add(toolBarButton);
		}

		selectNodeButton.setSelected(true);

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for the quest editor. It adds all of the graph editor
	 * buttons from the GraphEditorToolbar, and then adds Quest specific options
	 * for the user after a separator.
	 * 
	 * @return
	 */
	public static JToolBar buildQuestEditorToolBar(final QuestEditor editor) {
		final JToolBar questEditorToolBar = buildGraphEditorToolBar();

		final int TOOL_BAR_HEIGHT = 32;
		final int NAME_FIELD_LENGTH = 150;
		final int FAN_IN_SPINNER_LENGTH = 50;

		ToolBarFactory.nameField = new JTextField(10);
		DocumentListener nameFieldListener = nameFieldListener();
		ToolBarFactory.nameField.getDocument().addDocumentListener(
				nameFieldListener);

		ToolBarFactory.nameField.setMaximumSize(new Dimension(
				NAME_FIELD_LENGTH, TOOL_BAR_HEIGHT));
		ToolBarFactory.nameField.setEnabled(false);

		JButton toggleCommittingButton = new JButton();

		toggleCommittingButton.setAction(ToggleCommittingAction.getInstance());
		toggleCommittingButton.setText(null);
		toggleCommittingButton.setOpaque(false);
		toggleCommittingButton.setContentAreaFilled(false);
		toggleCommittingButton.setBorderPainted(false);

		final JSpinner fanInSpinner = buildFanInSpinner(new Dimension(
				FAN_IN_SPINNER_LENGTH, TOOL_BAR_HEIGHT));

		updateFanInSpinner(fanInSpinner, null);

		final JLabel nameLabel = new JLabel(Il8nResources.getString("Name")
				+ ": ");
		final JLabel commitLabel = new JLabel(
				Il8nResources.getString("Committing") + ": ");
		final JLabel fanInLabel = new JLabel("Fan In: ");

		nameLabel.setEnabled(false);
		commitLabel.setEnabled(false);
		fanInLabel.setEnabled(false);

		Dimension minSize = new Dimension(15, TOOL_BAR_HEIGHT);
		Dimension prefSize = new Dimension(15, TOOL_BAR_HEIGHT);
		Dimension maxSize = new Dimension(15, TOOL_BAR_HEIGHT);

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.addSeparator();

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.add(nameLabel);

		questEditorToolBar.add(ToolBarFactory.nameField);

		questEditorToolBar.add(commitLabel);

		questEditorToolBar.add(toggleCommittingButton);

		questEditorToolBar.add(fanInLabel);

		questEditorToolBar.add(fanInSpinner);

		GraphNodeObserver nodeObserver = new GraphNodeObserver() {

			@Override
			public void nodeChanged(GraphNodeEvent event) {
				nameLabel.setEnabled(true);
				commitLabel.setEnabled(true);
				fanInLabel.setEnabled(true);
				
				GraphNode node = event.getSource();
				
				node.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {

						updateFanInSpinner(fanInSpinner, questPointNode);

						// If it's selected, each of the three items updated.
						// If path removed, fanInSpinner is updated
						// If path added ""

						System.out.println("Hai thar"
								+ questPointNode.toString());
					}
				});
				// GraphNode.observeDepthMap(this, editor.getHeadNode());

			}
		};

		GraphNode.observeDepthMap(nodeObserver, editor.getHeadNode());

		return questEditorToolBar;
	}

	/**
	 * Returns a fanInSpinner, which has an attached listener that updates the
	 * model if its value is changed. This is used for quest fan in, i.e. how
	 * many preceding tests need to be finished before starting the selected
	 * one.
	 * 
	 * @return
	 */
	private static JSpinner buildFanInSpinner(final Dimension maxSize) {
		final JSpinner fanInSpinner = new JSpinner();
		fanInSpinner.setMaximumSize(maxSize);

		fanInSpinner.setEnabled(false);

		return fanInSpinner;
	}

	/**
	 * Creates a ChangeListener for a JSpinner. Returns listener.
	 * When the Spinner value changes, the listener updates the model and 
	 * updates the fan in spinner itself to show the change.
	 * 
	 * @param fanInSpinner
	 * @param questPoint
	 * @return
	 */
	private static ChangeListener fanInSpinnerListener(JSpinner fanInSpinner,
			QuestPointNode questNode) {
		final JSpinner fanInSpin = fanInSpinner;
		final QuestPointNode questN = questNode;

		ChangeListener fanInSpinnerListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				SpinnerModel spinnerModel = fanInSpin.getModel();
				Integer spinnerValue = (Integer) spinnerModel.getValue();

				questN.getQuestPoint().setFanIn(spinnerValue);

				updateFanInSpinner(fanInSpin, questN);
			}
		};

		return fanInSpinnerListener;
	}

	/**
	 * Creates a SpinnerModel for the FanIn function based on the current quest
	 * point, then sets the FanIn Spinner Model to it.
	 * 
	 * If there is no Quest Point selected, the SpinnerModel is a spinner set to
	 * 1.
	 * 
	 * @param fanInSpinner
	 *            FanInSpinner to be passed
	 * @param questNode
	 *            QuestPointNode that the spinner is operating on. Pass in null
	 *            if there is none.
	 * 
	 * @return The SpinnerModel
	 */
	private static void updateFanInSpinner(JSpinner fanInSpinner,
			QuestPointNode questNode) {

		if (questNode != null) {
			int maxFanIn = questNode.getParents().size();

			// If maxFanIn >1, maxFanIn. Otherwise, 1.
			maxFanIn = maxFanIn > 1 ? maxFanIn : 1;

			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					questNode.getQuestPoint().getFanIn(), new Integer(1),
					new Integer(maxFanIn), new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);

			fanInSpinner.removeChangeListener(fanInSpinner.getChangeListeners()[1]);
			
			fanInSpinner.addChangeListener(fanInSpinnerListener(fanInSpinner,
					questNode));
			fanInSpinner.setEnabled(true);
		} else {
			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					new Integer(1), new Integer(1), new Integer(1),
					new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);
			fanInSpinner.setEnabled(false);
		}
	}

	/**
	 * Sets the current quest point node to the specified node and updates the
	 * Quest Point Toolbar.
	 * 
	 * @param currentQuestPointNode
	 */
	public static void setCurrentQuestPointNode(QuestPointNode questNode) {
		String displayText = questNode.getQuestPoint().getDisplayText();

		ToolBarFactory.nameField.setText(displayText);
		ToolBarFactory.nameField.setEnabled(true);
		// updateFanInSpinner();
	}

	/**
	 * Creates an DocumentListener for the TextField.
	 * 
	 * @return
	 */
	private static DocumentListener nameFieldListener() {
		DocumentListener nameFieldListener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = nameField.getText();

				// TODO Implement this so it works.
				// ToolBarFactory.currentQuestPoint.setDisplayText(text);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		};
		return nameFieldListener;
	}

}
