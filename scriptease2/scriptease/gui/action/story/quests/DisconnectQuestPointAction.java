package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarAction;

/**
 * Represents and performs the Disconnect Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class DisconnectQuestPointAction extends
		ToolBarAction {
	private static final String INSERT_TEXT = "path_erase";

	private static final DisconnectQuestPointAction instance = new DisconnectQuestPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected DisconnectQuestPointAction() {
		super(DisconnectQuestPointAction.INSERT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static DisconnectQuestPointAction getInstance() {
		return DisconnectQuestPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.ERASE_PATH_CURSOR);
		setMode(ToolBarButtonMode.DISCONNECT_QUEST_POINT);
	}
}
