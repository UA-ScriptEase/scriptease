package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.ui.ScriptEaseUI;

/**
 * Represents and performs the InsertMode command, as well as encapsulating its
 * enabled and name display state. This sets the ToolBar's mode to "Insert".
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertModeAction extends GraphToolBarModeAction {
	private final static String INSERT_TEXT = "Insert";
	private final static String ICON_TEXT = "node_add";

	private static final InsertModeAction instance = new InsertModeAction();

	/**
	 * Defines a <code>InsertModeAction</code> object with an icon.
	 */
	protected InsertModeAction() {
		super(InsertModeAction.INSERT_TEXT, InsertModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, INSERT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertModeAction getInstance() {
		return InsertModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.setCursor(ScriptEaseUI.CURSOR_NODE_ADD);
		setMode(ToolBarMode.INSERT);
	}
}
