package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Insert Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertQuestPointAction extends ToolBarButtonAction {
	private final static String INSERT_TEXT = "Insert Point";
	private final static String ICON_TEXT = "node_add";

	private static final InsertQuestPointAction instance = new InsertQuestPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected InsertQuestPointAction() {
		super(InsertQuestPointAction.INSERT_TEXT,
				InsertQuestPointAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertQuestPointAction getInstance() {
		return InsertQuestPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.ADD_NODE_CURSOR);
		setMode(ToolBarButtonMode.INSERT_QUEST_POINT);
	}
}
