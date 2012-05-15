package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Delete Quest Point command, as well as 
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 *
 */
@SuppressWarnings("serial")
public final class DeleteQuestPointAction extends ToolBarButtonAction{
	private static final String DELETE_TEXT = "Delete Point";
	private final static String ICON_TEXT = "node_delete";
	
	private static final DeleteQuestPointAction instance = new DeleteQuestPointAction();
	
	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected DeleteQuestPointAction() {
		super(DeleteQuestPointAction.DELETE_TEXT,
				DeleteQuestPointAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return	The sole instance of this particular type of Action
	 */
	public static DeleteQuestPointAction getInstance() {
		return DeleteQuestPointAction.instance;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.DELETE_NODE_CURSOR);
		setMode(ToolBarButtonMode.DELETE_QUEST_POINT);
	}
}
