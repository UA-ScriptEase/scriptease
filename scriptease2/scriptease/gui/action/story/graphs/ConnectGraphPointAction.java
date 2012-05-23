package scriptease.gui.action.story.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Connect Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class ConnectGraphPointAction extends ToolBarButtonAction {
	private static final String CONNECT_TEXT = "Connect";
	private final static String ICON_TEXT = "path_draw";
	
	private static final ConnectGraphPointAction instance = new ConnectGraphPointAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected ConnectGraphPointAction() {
		super(ConnectGraphPointAction.CONNECT_TEXT,
				ConnectGraphPointAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static ConnectGraphPointAction getInstance() {
		return ConnectGraphPointAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setCursorToImageFromPath(getJComponent(), ICON_TEXT);
		setMode(ToolBarButtonMode.CONNECT_GRAPH_NODE);
	}
}
