package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import scriptease.gui.action.AbstractToolBarButtonAction;
import scriptease.gui.action.ActiveModelSensitiveAction;

/**
 * Reperesents and performs the Insert Quest Point command, as well as 
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 *
 */
@SuppressWarnings("serial")
public final class InsertQuestPointAction extends AbstractToolBarButtonAction{
	private static final String INSERT_TEXT = "node_add";
	
	private static final Action instance = new InsertQuestPointAction();
	
	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected InsertQuestPointAction() {
		super(InsertQuestPointAction.INSERT_TEXT);
		
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return	The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return InsertQuestPointAction.instance;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
