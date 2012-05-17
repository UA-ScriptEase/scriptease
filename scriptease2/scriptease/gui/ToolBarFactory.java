package scriptease.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.text.PlainDocument;

import scriptease.controller.AbstractNoOpGraphNodeVisitor;
import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeObserver;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.story.graphs.ConnectGraphPointAction;
import scriptease.gui.action.story.graphs.DeleteGraphNodeAction;
import scriptease.gui.action.story.graphs.DisconnectGraphPointAction;
import scriptease.gui.action.story.graphs.InsertGraphNodeAction;
import scriptease.gui.action.story.graphs.SelectGraphNodeAction;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.quests.QuestEditor;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.util.FileOp;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs. A specialized Quest Editor Toolbar can also be
 * created.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {

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
		final int FAN_IN_SPINNER_LENGTH = 50;
		final int NAME_FIELD_LENGTH = 150;

		final JTextField nameField = nameField(new Dimension(NAME_FIELD_LENGTH,
				TOOL_BAR_HEIGHT));

		final JToggleButton commitButton = committingButton();

		final JSpinner fanInSpinner = buildFanInSpinner(new Dimension(
				FAN_IN_SPINNER_LENGTH, TOOL_BAR_HEIGHT));

		updateQuestToolBar(nameField, commitButton, fanInSpinner, null);

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

		questEditorToolBar.add(nameField);

		questEditorToolBar.add(commitLabel);

		questEditorToolBar.add(commitButton);

		questEditorToolBar.add(fanInLabel);

		questEditorToolBar.add(fanInSpinner);

		GraphNodeObserver nodeObserver = new GraphNodeObserver() {

			@Override
			public void nodeChanged(GraphNodeEvent event) {

				nameLabel.setEnabled(true);
				commitLabel.setEnabled(true);
				fanInLabel.setEnabled(true);

				final GraphNode node = event.getSource();

				// TODO
				//
				// Maybe this is not getting called if the node gets
				// deleted before it has a chance to be called.
				// What also seems likely is that it thinks a different node
				// is the headNode, which should not be happening.
				GraphNode.observeDepthMap(this, editor.getHeadNode());

				node.process(new AbstractNoOpGraphNodeVisitor() {
					@Override
					public void processQuestPointNode(
							QuestPointNode questPointNode) {

						// If it's selected, update.
						// If path removed, update.
						// If path added, update
						// Need to make sure this stuff happens later!

						switch (ToolBarButtonAction.getMode()) {

						// case INSERT_GRAPH_NODE:

						case DELETE_GRAPH_NODE:

							List<GraphNode> children = questPointNode
									.getChildren();

							System.out.println("# OF CHILD ==="
									+ children.size());

							for (GraphNode child : children) {
								child.process(new AbstractNoOpGraphNodeVisitor() {
									public void processQuestPointNode(
											QuestPointNode questPointNode) {

										QuestPoint questPoint = questPointNode
												.getQuestPoint();
										int fanIn = questPoint.getFanIn();

										if (fanIn > 1)
											questPoint.setFanIn(fanIn - 1);

									}
								});
							}

							break;

						case DISCONNECT_GRAPH_NODE:

							node.process(new AbstractNoOpGraphNodeVisitor() {
								public void processQuestPointNode(
										QuestPointNode questPointNode) {

									QuestPoint questPoint = questPointNode
											.getQuestPoint();
									int fanIn = questPoint.getFanIn();

									if (fanIn > 1)
										questPoint.setFanIn(fanIn - 1);

								}
							});

							break;
						}

						updateQuestToolBar(nameField, commitButton,
								fanInSpinner, questPointNode);

					}
				});
				// GraphNode.observeDepthMap(this, event.getSource());
			}
		};

		GraphNode.observeDepthMap(nodeObserver, editor.getHeadNode());

		return questEditorToolBar;
	}

	/**
	 * Updates the entire quest tool bar, including the namefiled, the
	 * committing button, the fan in spinner, and all of the labels.
	 * 
	 * 
	 * @param nameField
	 * @param commitButton
	 * @param fanInSpinner
	 * @param questNode
	 */
	private static void updateQuestToolBar(JTextField nameField,
			JToggleButton commitButton, JSpinner fanInSpinner,
			QuestPointNode questNode) {

		updateFanInSpinner(fanInSpinner, questNode);
		updateCommittingButton(commitButton, questNode);
		updateNameField(nameField, questNode);

	}

	/**
	 * Returns a name field JTextField with the proper appearance.
	 * 
	 * @param maxSize
	 * @return
	 */
	private static JTextField nameField(final Dimension maxSize) {
		JTextField nameField = new JTextField(10);

		nameField.setMaximumSize(maxSize);

		return nameField;
	}

	/**
	 * Creates an DocumentListener for the TextField.
	 * 
	 * @return
	 */
	private static DocumentListener nameFieldListener(JTextField nameField,
			QuestPointNode questNode) {
		final JTextField nField = nameField;
		final QuestPointNode qNode = questNode;

		DocumentListener nameFieldListener = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = nField.getText();
				// System.out.println("Text!$!$@!!%@!%()$~)!$*)! ! WQETEWTXT !"
				// + text);
				qNode.getQuestPoint().setDisplayText(text);
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

	/**
	 * Updates the name field for the quest node passed in.
	 * 
	 * @param nameField
	 * @param questNode
	 */
	private static void updateNameField(JTextField nameField,
			QuestPointNode questNode) {
		if (questNode != null) {
			String displayText = questNode.getQuestPoint().getDisplayText();

			nameField.setDocument(new PlainDocument());
			nameField.getDocument().addDocumentListener(
					nameFieldListener(nameField, questNode));

			nameField.setText(displayText);
			nameField.setEnabled(true);
		} else {
			nameField.setText("");
			nameField.setEnabled(false);
		}
	}

	/**
	 * Returns a committing button with the proper appearance.
	 * 
	 * @return
	 */
	private static JToggleButton committingButton() {
		final JToggleButton committingButton = new JToggleButton();
		final String COMMIT_FALSE_ICON = "commit_false";
		final String COMMIT_TRUE_ICON = "commit_true";

		committingButton.setOpaque(false);
		committingButton.setContentAreaFilled(false);
		committingButton.setBorderPainted(false);
		committingButton.setRolloverEnabled(false);

		try {
			BufferedImage falseIconImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/buttonicons/"
							+ COMMIT_FALSE_ICON + ".png"));

			BufferedImage trueIconImage = ImageIO.read(FileOp
					.getFileResource("scriptease/resources/icons/buttonicons/"
							+ COMMIT_TRUE_ICON + ".png"));

			committingButton.setIcon(new ImageIcon(falseIconImage));
			committingButton.setSelectedIcon(new ImageIcon(trueIconImage));
			committingButton.setDisabledIcon(new ImageIcon(falseIconImage));

		} catch (IOException e) {
			UncaughtExceptionHandler handler = Thread
					.getDefaultUncaughtExceptionHandler();
			handler.uncaughtException(Thread.currentThread(),
					new IllegalStateException("Exception " + e
							+ "while adding icon for CommitButton"));

			committingButton.setText("><");
		}

		return committingButton;
	}

	/**
	 * Creates an ItemListener for the CommitButton. Listens if the CommitButton
	 * is selected or deselected, and sets the model appropriately.
	 * 
	 * @param questNode
	 * @return
	 */
	private static ItemListener commitButtonListener(QuestPointNode questNode) {
		final QuestPointNode questN = questNode;

		ItemListener commitButtonListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					questN.getQuestPoint().setCommitting(true);

					System.out.println("SSSSSSSSSEEEEEEEEEEEELLLECTED!");

				} else {
					questN.getQuestPoint().setCommitting(false);

					System.out.println("DEEEEEEEEESELECTED!");
				}
			}
		};

		return commitButtonListener;
	}

	/**
	 * Updates the committing button for the quest node passed in.
	 * 
	 * @param cButton
	 *            The commmitting button to update.
	 * @param questNode
	 *            The QuestPointNode to update the committing button to.
	 */
	private static void updateCommittingButton(JToggleButton cButton,
			QuestPointNode questNode) {
		if (questNode != null) {
			Boolean committing = questNode.getQuestPoint().getCommitting();

			if (cButton.getItemListeners().length > 0)
				cButton.removeItemListener(cButton.getItemListeners()[0]);

			cButton.setSelected(committing);

			cButton.addItemListener(commitButtonListener(questNode));

			cButton.setEnabled(true);
		} else {
			cButton.setEnabled(false);
		}
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
	 * Creates a ChangeListener for a JSpinner. Returns listener. When the
	 * Spinner value changes, the listener updates the model and updates the fan
	 * in spinner itself to show the change.
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

				// updateFanInSpinner(fanInSpin, questN);
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

			if (questNode.getQuestPoint().getFanIn() > maxFanIn) {
				questNode.getQuestPoint().setFanIn(1);
			}

			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					questNode.getQuestPoint().getFanIn(), new Integer(1),
					new Integer(maxFanIn), new Integer(1));

			fanInSpinner.setModel(fanInSpinnerModel);

			fanInSpinner
					.removeChangeListener(fanInSpinner.getChangeListeners()[1]);

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
}
