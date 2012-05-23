package scriptease.gui.action.story.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Delete Quest Point command, as well as 
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 *
 */
@SuppressWarnings("serial")
public final class DeleteGraphNodeAction extends ToolBarButtonAction{
	private static final String DELETE_TEXT = "Delete";
	private final static String ICON_TEXT = "node_delete";
	
	private static final DeleteGraphNodeAction instance = new DeleteGraphNodeAction();
	
	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected DeleteGraphNodeAction() {
		super(DeleteGraphNodeAction.DELETE_TEXT,
				DeleteGraphNodeAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return	The sole instance of this particular type of Action
	 */
	public static DeleteGraphNodeAction getInstance() {
		return DeleteGraphNodeAction.instance;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		setCursorToImageFromPath(getJComponent(), ICON_TEXT);
		setMode(ToolBarButtonMode.DELETE_GRAPH_NODE);
	}
}
