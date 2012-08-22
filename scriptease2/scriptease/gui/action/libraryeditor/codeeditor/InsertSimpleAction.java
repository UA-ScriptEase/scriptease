package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;

/**
 * Represents and performs the Insert Simple command, as well as encapsulating
 * its enabled and name display state. This command will insert a new
 * "SimpleFragment" into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertSimpleAction extends AbstractInsertFragmentAction {
	private static final String INSERT_SIMPLE_TEXT = "Simple";

	private static final InsertSimpleAction instance = new InsertSimpleAction();

	/**
	 * Defines a <code>InsertLiteralAction</code> object.
	 */
	private InsertSimpleAction() {
		super(InsertSimpleAction.INSERT_SIMPLE_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertSimpleAction getInstance() {
		return InsertSimpleAction.instance;
	}

	@Override
	protected AbstractFragment newFragment() {
		return new SimpleDataFragment();
	}
}
