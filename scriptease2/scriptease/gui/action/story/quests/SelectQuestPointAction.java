package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Select Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class SelectQuestPointAction extends ToolBarButtonAction {
	private static final String INSERT_TEXT = "selection";

	private static final SelectQuestPointAction instance = new SelectQuestPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected SelectQuestPointAction() {
		super(SelectQuestPointAction.INSERT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static SelectQuestPointAction getInstance() {
		return SelectQuestPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.SYSTEM_CURSOR);
		setMode(ToolBarButtonMode.SELECT_QUEST_POINT);
	}
}
