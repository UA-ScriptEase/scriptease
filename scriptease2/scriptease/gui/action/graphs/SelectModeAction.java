package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the SelectMode command, as well as encapsulating its
 * enabled and name display state. This sets the toolbar's mode to "Select".
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class SelectModeAction extends GraphToolBarModeAction {
	private final static String SELECT_TEXT = "Select";
	private final static String ICON_TEXT = "selection";

	private static final SelectModeAction instance = new SelectModeAction();

	/**
	 * Defines a <code>SelectModeAction</code> object with an icon.
	 */
	protected SelectModeAction() {
		super(SelectModeAction.SELECT_TEXT, SelectModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, SELECT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static SelectModeAction getInstance() {
		return SelectModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// We set the cursor to null so that it is just the default cursor.
		setCursor(null);
		setMode(ToolBarMode.SELECT);
	}
}
