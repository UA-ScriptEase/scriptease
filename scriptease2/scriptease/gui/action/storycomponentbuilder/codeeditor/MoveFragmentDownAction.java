package scriptease.gui.action.storycomponentbuilder.codeeditor;

/**
 * Represents and performs the Move Fragment Down action as well as
 * encapsulating its enabled and name display state. This command will move a
 * fragment one down in the list of fragments in a CodeBlock.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class MoveFragmentDownAction extends AbstractMoveFragmentAction {
	private static final String MOVE_DOWN_TEXT = "Down";

	private static final MoveFragmentDownAction instance = new MoveFragmentDownAction();

	private MoveFragmentDownAction() {
		super(MoveFragmentDownAction.MOVE_DOWN_TEXT);
	}

	public static MoveFragmentDownAction getInstance() {
		return MoveFragmentDownAction.instance;
	}

	@Override
	protected int delta() {
		return 1;
	}
}
