package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;

/**
 * Represents and performs the Insert Indent command, as well as encapsulating
 * its enabled and name display state. This command will insert a new
 * "IndentedFragment" into the story component builder code editor pane.
 * 
 * Indents can only be inserted at the very top level, inside of scope
 * fragments, or inside of other indented fragments. They cannot be inserted
 * into line fragments.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertIndentAction extends AbstractInsertFragmentAction {
	private static final String INSERT_INDENT_TEXT = "Indent";

	private static final InsertIndentAction instance = new InsertIndentAction();

	/**
	 * Defines a <code>InsertIndentAction</code> object.
	 */
	private InsertIndentAction() {
		super(InsertIndentAction.INSERT_INDENT_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertIndentAction getInstance() {
		return InsertIndentAction.instance;
	}

	@Override
	protected AbstractFragment newFragment() {
		return new IndentFragment();
	}
}
