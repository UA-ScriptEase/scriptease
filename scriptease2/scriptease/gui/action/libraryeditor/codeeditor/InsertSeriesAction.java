package scriptease.gui.action.libraryeditor.codeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;

/**
 * Represents and performs the Insert Series command, as well as encapsulating
 * its enabled and name display state. This command will insert a new
 * "SeriesFragment" into the story component builder code editor pane.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class InsertSeriesAction extends AbstractInsertFragmentAction {
	private static final String INSERT_SERIES_TEXT = "Series";

	private static final InsertSeriesAction instance = new InsertSeriesAction();

	/**
	 * Defines a <code>InsertSeriesAction</code> object.
	 */
	private InsertSeriesAction() {
		super(InsertSeriesAction.INSERT_SERIES_TEXT);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK));
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static InsertSeriesAction getInstance() {
		return InsertSeriesAction.instance;
	}

	@Override
	protected AbstractFragment newFragment() {
		return new SeriesFragment();
	}
}
