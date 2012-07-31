package scriptease.gui.action.storycomponentbuilder.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;

/**
 * Represents and performs the Insert Scope command, as well as encapsulating
 * its enabled and name display state. This command will insert a new
 * "ScopeFragment" into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertScopeAction extends AbstractInsertFragmentAction {
	private static final String INSERT_SCOPE_TEXT = "Scope";

	private static final InsertScopeAction instance = new InsertScopeAction();

	/**
	 * Defines a <code>InsertScopeAction</code> object.
	 */
	private InsertScopeAction() {
		super(InsertScopeAction.INSERT_SCOPE_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertScopeAction getInstance() {
		return InsertScopeAction.instance;
	}

	@Override
	protected FormatFragment newFragment() {
		return new ScopeFragment();
	}
}
