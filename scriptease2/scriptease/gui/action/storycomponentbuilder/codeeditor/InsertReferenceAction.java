package scriptease.gui.action.storycomponentbuilder.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.ReferenceFragment;

/**
 * Represents and performs the Insert Reference command, as well as
 * encapsulating its enabled and name display state. This command will insert a
 * new "ReferenceFragment" into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertReferenceAction extends AbstractInsertFragmentAction {
	private static final String INSERT_REFERENCE_TEXT = "Reference";

	private static final InsertReferenceAction instance = new InsertReferenceAction();

	/**
	 * Defines a <code>InsertLiteralAction</code> object.
	 */
	private InsertReferenceAction() {
		super(InsertReferenceAction.INSERT_REFERENCE_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertReferenceAction getInstance() {
		return InsertReferenceAction.instance;
	}

	@Override
	protected FormatFragment newFragment() {
		return new ReferenceFragment("");
	}
}
