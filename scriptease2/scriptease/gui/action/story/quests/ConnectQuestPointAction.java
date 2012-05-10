package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarAction;

/**
 * Reperesents and performs the Connect Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class ConnectQuestPointAction extends ToolBarAction {
	private static final String INSERT_TEXT = "path_draw";

	private static final ConnectQuestPointAction instance = new ConnectQuestPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected ConnectQuestPointAction() {
		super(ConnectQuestPointAction.INSERT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static ConnectQuestPointAction getInstance() {
		return ConnectQuestPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.DRAW_PATH_CURSOR);
		setMode(ToolBarButtonMode.CONNECT_QUEST_POINT);
	}
}
