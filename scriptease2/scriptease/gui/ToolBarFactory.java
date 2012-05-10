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

import scriptease.gui.action.story.quests.ConnectQuestPointAction;
import scriptease.gui.action.story.quests.DeleteQuestPointAction;
import scriptease.gui.action.story.quests.DisconnectQuestPointAction;
import scriptease.gui.action.story.quests.InsertQuestPointAction;
import scriptease.gui.action.story.quests.SelectQuestPointAction;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {

	private static JTextField nameField = new JTextField();

	public static JButton propButton = new JButton("Properties");

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

		questEditorToolBar.addSeparator();

		Dimension minSize = new Dimension(30, 50);
		Dimension prefSize = new Dimension(30, 50);
		Dimension maxSize = new Dimension(Short.MAX_VALUE, 50);
		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		// Temporary Hacks
		questEditorToolBar.add(new JLabel("Name: "));
		questEditorToolBar.add(nameField);
		questEditorToolBar.add(new JButton("><"));
		questEditorToolBar.add(new JSpinner());
		// questEditorToolBar.add(propButton);
		// questEditorToolBar.add(new JButton("Save Changes"));

		return questEditorToolBar;
	}

	public static void updateQuestPointNameField(String textChange) {
		nameField.setText(textChange);
	}

	public static void updateCommittingCheckBox(Boolean committing) {
		if (committing) {

		} else {

		}
	}

	public void updateQuestEditorToolBar(JToolBar questEditorToolBar) {
		// questEditorToolBar.
	}
}
