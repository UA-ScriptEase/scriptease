package scriptease.gui.action.graphs;

import java.awt.event.ActionEvent;

/**
 * Represents and performs the GroupMode command, as well as encapsulating its
 * enabled and name display state. This sets the toolbar's mode to "Group"
 * mode.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public final class GroupModeAction extends GraphToolBarModeAction {
	private static final String GROUP_TEXT = "Group";
	private final static String ICON_TEXT = "node_add";

	private static final GroupModeAction instance = new GroupModeAction();

	/**
	 * Defines a <code>GroupModeAction</code> object with an icon.
	 */
	protected GroupModeAction() {
		super(GroupModeAction.GROUP_TEXT, GroupModeAction.ICON_TEXT);

		this.putValue(SHORT_DESCRIPTION, GROUP_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static GroupModeAction getInstance() {
		return GroupModeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setMode(ToolBarMode.GROUP);
	}
}
