package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the DisconnectMode command, as well as encapsulating
 * its enabled and name display state. This sets the ToolBar's mode to
 * "Disconnect".
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class DisconnectModeAction extends GraphToolBarModeAction {
	private static final String DISCONNECT_TEXT = "Disconnect";
	private final static String ICON_TEXT = "path_erase";

	private static final DisconnectModeAction instance = new DisconnectModeAction();

	/**
	 * Defines a <code>DisconnectModeAction</code> object with an icon.
	 */
	protected DisconnectModeAction() {
		super(DisconnectModeAction.DISCONNECT_TEXT,
				DisconnectModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, DISCONNECT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static DisconnectModeAction getInstance() {
		return DisconnectModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setCursorToImageFromPath(ICON_TEXT);
		setMode(ToolBarMode.DISCONNECT);
	}
}
