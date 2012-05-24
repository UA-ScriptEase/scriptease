package scriptease.gui.action.story.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Disconnect Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class DisconnectGraphPointAction extends ToolBarButtonAction {
	private static final String DISCONNECT_TEXT = "Disconnect";
	private final static String ICON_TEXT = "path_erase";

	private static final DisconnectGraphPointAction instance = new DisconnectGraphPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected DisconnectGraphPointAction() {
		super(DisconnectGraphPointAction.DISCONNECT_TEXT,
				DisconnectGraphPointAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static DisconnectGraphPointAction getInstance() {
		return DisconnectGraphPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setCursorToImageFromPath(ICON_TEXT);
		setMode(ToolBarButtonMode.DISCONNECT_GRAPH_NODE);
	}
}
