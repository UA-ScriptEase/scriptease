package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the UngroupMode command, as well as encapsulating its
 * enabled and name display state. This sets the toolbar's mode to "Ungroup"
 * mode.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public final class UngroupModeAction extends GraphToolBarModeAction {
	private static final String UNGROUP_TEXT = "Ungroup";
	private final static String ICON_TEXT = "selection";

	private static final UngroupModeAction instance = new UngroupModeAction();

	/**
	 * Defines a <code>UngroupModeAction</code> object with an icon.
	 */
	protected UngroupModeAction() {
		super(UngroupModeAction.UNGROUP_TEXT, UngroupModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, UNGROUP_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static UngroupModeAction getInstance() {
		return UngroupModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setMode(ToolBarMode.UNGROUP);
	}
}
