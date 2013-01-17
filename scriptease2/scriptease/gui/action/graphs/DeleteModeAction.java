package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the DeleteMode command, as well as encapsulating its
 * enabled and name display state. This sets the ToolBar's mode to "Delete".
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class DeleteModeAction extends GraphToolBarModeAction {
	private static final String DELETE_TEXT = "Delete";
	private final static String ICON_TEXT = "node_delete";

	private static final DeleteModeAction instance = new DeleteModeAction();

	/**
	 * Defines a <code>DeleteModeAction</code> object with an icon.
	 */
	protected DeleteModeAction() {
		super(DeleteModeAction.DELETE_TEXT, DeleteModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, DELETE_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static DeleteModeAction getInstance() {
		return DeleteModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setMode(ToolBarMode.DELETE);
	}
}
