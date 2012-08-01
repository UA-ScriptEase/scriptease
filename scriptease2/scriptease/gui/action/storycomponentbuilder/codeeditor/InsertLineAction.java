package scriptease.gui.action.storycomponentbuilder.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.Fragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;

/**
 * Represents and performs the Insert Line command, as well as encapsulating its
 * enabled and name display state. This command will insert a new "LineFragment"
 * into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertLineAction extends AbstractInsertFragmentAction {
	private static final String INSERT_LINE_TEXT = "Line";

	private static final InsertLineAction instance = new InsertLineAction();

	/**
	 * Defines a <code>InsertLineAction</code> object.
	 */
	private InsertLineAction() {
		super(InsertLineAction.INSERT_LINE_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertLineAction getInstance() {
		return InsertLineAction.instance;
	}

	@Override
	protected Fragment newFragment() {
		return new LineFragment("/n");
	}
}
