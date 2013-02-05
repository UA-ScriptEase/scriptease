package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the ConnectMode command, as well as encapsulating its
 * enabled and name display state. This sets the toolbar's mode to "Connect"
 * mode.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class ConnectModeAction extends GraphToolBarModeAction {
	private static final String CONNECT_TEXT = "Connect";
	private final static String ICON_TEXT = "path_draw";

	private static final ConnectModeAction instance = new ConnectModeAction();

	/**
	 * Defines a <code>ConnectModeAction</code> object with an icon.
	 */
	protected ConnectModeAction() {
		super(ConnectModeAction.CONNECT_TEXT, ConnectModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, CONNECT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static ConnectModeAction getInstance() {
		return ConnectModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setMode(ToolBarMode.CONNECT);
	}
}
