package scriptease.gui.action.story.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Insert Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertGraphNodeAction extends ToolBarButtonAction {
	private final static String INSERT_TEXT = "Insert";
	private final static String ICON_TEXT = "node_add";

	private static final InsertGraphNodeAction instance = new InsertGraphNodeAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected InsertGraphNodeAction() {
		super(InsertGraphNodeAction.INSERT_TEXT,
				InsertGraphNodeAction.ICON_TEXT);
		
		this.putValue(SHORT_DESCRIPTION, INSERT_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertGraphNodeAction getInstance() {
		return InsertGraphNodeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		setCursorToImageFromPath(ICON_TEXT);
		setMode(ToolBarButtonMode.INSERT_GRAPH_NODE);
	}
}
