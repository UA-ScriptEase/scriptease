package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;

/**
 * Represents and performs the Insert Literal command, as well as encapsulating
 * its enabled and name display state. This command will insert a new
 * "LiteralFragment" into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertLiteralAction extends AbstractInsertFragmentAction {
	private static final String INSERT_LITERAL_TEXT = "Literal";

	private static final InsertLiteralAction instance = new InsertLiteralAction();

	/**
	 * Defines a <code>InsertLiteralAction</code> object.
	 */
	private InsertLiteralAction() {
		super(InsertLiteralAction.INSERT_LITERAL_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertLiteralAction getInstance() {
		return InsertLiteralAction.instance;
	}

	@Override
	protected AbstractFragment newFragment() {
		return new LiteralFragment("");
	}
}
