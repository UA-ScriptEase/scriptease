package scriptease.gui.action.libraryeditor.codeeditor;

/**
 * Represents and performs the Move Fragment Up action as well as encapsulating
 * its enabled and name display state. This command will move a fragment one up
 * in the list of fragments in a CodeBlock.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class MoveFragmentUpAction extends AbstractMoveFragmentAction {
	private static final String MOVE_UP_TEXT = "Up";

	private static final MoveFragmentUpAction instance = new MoveFragmentUpAction();

	private MoveFragmentUpAction() {
		super(MoveFragmentUpAction.MOVE_UP_TEXT);
	}

	public static MoveFragmentUpAction getInstance() {
		return MoveFragmentUpAction.instance;
	}

	@Override
	protected int delta() {
		return -1;
	}
}
