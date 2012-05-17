package scriptease.gui.action.story.graphs;

import java.awt.event.ActionEvent;

import scriptease.gui.SEFrame;
import scriptease.gui.action.ToolBarButtonAction;

/**
 * Represents and performs the Select Quest Point command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class SelectGraphNodeAction extends ToolBarButtonAction {
	private final static String SELECT_TEXT = "Select";
	private final static String ICON_TEXT = "selection";

	private static final SelectGraphNodeAction instance = new SelectGraphNodeAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected SelectGraphNodeAction() {
		super(SelectGraphNodeAction.SELECT_TEXT,
				SelectGraphNodeAction.ICON_TEXT);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static SelectGraphNodeAction getInstance() {
		return SelectGraphNodeAction.instance;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SEFrame.getInstance().changeCursor(SEFrame.SYSTEM_CURSOR);
		setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);
	}
}
