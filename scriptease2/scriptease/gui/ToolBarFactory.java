package scriptease.gui;

import java.awt.Dimension;

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

import scriptease.gui.action.story.quests.ConnectQuestPointAction;
import scriptease.gui.action.story.quests.DeleteQuestPointAction;
import scriptease.gui.action.story.quests.DisconnectQuestPointAction;
import scriptease.gui.action.story.quests.InsertQuestPointAction;
import scriptease.gui.action.story.quests.SelectQuestPointAction;
import scriptease.gui.action.story.quests.ToggleCommittingAction;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {

	private static JTextField nameField;

	private static QuestPointNode currentQuestPointNode;
	private static QuestPoint currentQuestPoint;

	private static JSpinner fanInSpinner;

	private static JLabel nameLabel;
	private static JLabel commitLabel;
	private static JLabel fanInLabel;

	public static JButton propButton = new JButton("Properties");

	/**
	 * Builds a toolbar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths.
	 * 
	 * @return
	 */
	public static JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar = new JToolBar();

		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);

		JToggleButton selectQuestButton = new JToggleButton();
		selectQuestButton.setAction(SelectQuestPointAction.getInstance());
		selectQuestButton.setText(null);

		JToggleButton insertQuestButton = new JToggleButton();
		insertQuestButton.setAction(InsertQuestPointAction.getInstance());
		insertQuestButton.setText(null);

		JToggleButton deleteQuestButton = new JToggleButton();
		deleteQuestButton.setAction(DeleteQuestPointAction.getInstance());
		deleteQuestButton.setText(null);

		JToggleButton connectQuestButton = new JToggleButton();
		connectQuestButton.setAction(ConnectQuestPointAction.getInstance());
		connectQuestButton.setText(null);

		JToggleButton disconnectQuestButton = new JToggleButton();
		disconnectQuestButton.setAction(DisconnectQuestPointAction
				.getInstance());
		disconnectQuestButton.setText(null);

		selectQuestButton.setSelected(true);

		graphEditorButtonGroup.add(selectQuestButton);
		graphEditorButtonGroup.add(insertQuestButton);
		graphEditorButtonGroup.add(deleteQuestButton);
		graphEditorButtonGroup.add(connectQuestButton);
		graphEditorButtonGroup.add(disconnectQuestButton);

		graphEditorToolBar.add(selectQuestButton);
		graphEditorToolBar.add(insertQuestButton);
		graphEditorToolBar.add(deleteQuestButton);
		graphEditorToolBar.add(connectQuestButton);
		graphEditorToolBar.add(disconnectQuestButton);

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for the quest editor. It adds all of the graph editor
	 * buttons from the GraphEditorToolbar, and then adds Quest specific options
	 * for the user after a separator.
	 * 
	 * @return
	 */
	public static JToolBar buildQuestEditorToolBar() {
		final JToolBar questEditorToolBar = buildGraphEditorToolBar();

		Dimension minSize = new Dimension(15, 32);
		Dimension prefSize = new Dimension(15, 32);
		Dimension maxSize = new Dimension(15, 32);

		ToolBarFactory.nameField = new JTextField(10);
		DocumentListener nameFieldListener = nameFieldListener();
		ToolBarFactory.nameField.getDocument().addDocumentListener(
				nameFieldListener);

		ToolBarFactory.nameField.setMaximumSize(new Dimension(150, 32));
		ToolBarFactory.nameField.setEnabled(false);

		JButton toggleCommittingButton = new JButton();

		toggleCommittingButton.setAction(ToggleCommittingAction.getInstance());
		toggleCommittingButton.setText(null);
		toggleCommittingButton.setOpaque(false);
		toggleCommittingButton.setContentAreaFilled(false);
		toggleCommittingButton.setBorderPainted(false);

		ToolBarFactory.fanInSpinner = new JSpinner();
		ToolBarFactory.fanInSpinner.setMaximumSize(new Dimension(50, 32));

		updateFanInSpinner();

		ToolBarFactory.nameLabel = new JLabel(Il8nResources.getString("Name")
				+ ": ");
		ToolBarFactory.commitLabel = new JLabel(
				Il8nResources.getString("Committing") + ": ");
		ToolBarFactory.fanInLabel = new JLabel("Fan In: ");

		ToolBarFactory.nameLabel.setEnabled(false);
		ToolBarFactory.commitLabel.setEnabled(false);
		ToolBarFactory.fanInLabel.setEnabled(false);

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.addSeparator();

		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		questEditorToolBar.add(ToolBarFactory.nameLabel);

		questEditorToolBar.add(ToolBarFactory.nameField);

		questEditorToolBar.add(ToolBarFactory.commitLabel);

		questEditorToolBar.add(toggleCommittingButton);

		questEditorToolBar.add(ToolBarFactory.fanInLabel);

		questEditorToolBar.add(ToolBarFactory.fanInSpinner);

		return questEditorToolBar;
	}

	/**
	 * Sets the current quest point node to the specified node and updates the
	 * Quest Point Toolbar.
	 * 
	 * @param currentQuestPointNode
	 */
	public static void setCurrentQuestPointNode(
			QuestPointNode currentQuestPointNode) {
		ToolBarFactory.currentQuestPointNode = currentQuestPointNode;
		ToolBarFactory.currentQuestPoint = ToolBarFactory.currentQuestPointNode
				.getQuestPoint();

		ToggleCommittingAction.getInstance().setQuestPoint(
				ToolBarFactory.currentQuestPoint);

		String displayText = ToolBarFactory.currentQuestPoint.getDisplayText();

		ToolBarFactory.nameField.setText(displayText);
		ToolBarFactory.nameField.setEnabled(true);
		ToolBarFactory.nameLabel.setEnabled(true);
		ToolBarFactory.commitLabel.setEnabled(true);
		ToolBarFactory.fanInLabel.setEnabled(true);

		updateFanInSpinner();

	}

	/**
	 * Creates a ChangeListener for the Fan In Spinner.
	 * 
	 * @return The ChangeListener
	 */
	private static ChangeListener fanInSpinnerListener() {
		ChangeListener fanInSpinnerListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				SpinnerModel spinnerModel = ToolBarFactory.fanInSpinner
						.getModel();
				Integer spinnerValue = (Integer) spinnerModel.getValue();

				ToolBarFactory.currentQuestPoint.setFanIn(spinnerValue);
			}

		};

		return fanInSpinnerListener;
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
				ToolBarFactory.currentQuestPoint.setDisplayText(text);
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
	 * Creates a SpinnerModel for the FanIn function based on the current quest
	 * point, then sets the FanIn Spinner Model to it.
	 * 
	 * If there is no Quest Point selected, the SpinnerModel is a spinner set to
	 * 1.
	 * 
	 * @return The SpinnerModel
	 */
	public static void updateFanInSpinner() {
		if (ToolBarFactory.currentQuestPointNode != null) {
			int maxFanIn = ToolBarFactory.currentQuestPointNode.getParents()
					.size();

			// If maxFanIn >1, maxFanIn. Otherwise, 1.
			maxFanIn = maxFanIn > 1 ? maxFanIn : 1;

			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					ToolBarFactory.currentQuestPoint.getFanIn(),
					new Integer(1), new Integer(maxFanIn), new Integer(1));

			System.out.println("Value FanIn: "
					+ ToolBarFactory.currentQuestPoint.getFanIn());
			System.out.println("Max FanIn: " + maxFanIn);

			ToolBarFactory.fanInSpinner.setModel(fanInSpinnerModel);
			ChangeListener fanInSpinnerListener = fanInSpinnerListener();
			ToolBarFactory.fanInSpinner.addChangeListener(fanInSpinnerListener);
			ToolBarFactory.fanInSpinner.setEnabled(true);

		} else {
			final SpinnerModel fanInSpinnerModel = new SpinnerNumberModel(
					new Integer(1), new Integer(1), new Integer(1),
					new Integer(1));

			ToolBarFactory.fanInSpinner.setModel(fanInSpinnerModel);
			ToolBarFactory.fanInSpinner.setEnabled(false);
		}
	}
}
