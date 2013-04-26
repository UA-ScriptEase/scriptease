package scriptease.gui.action.library;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.FileManager;
import scriptease.controller.io.FileIO;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.LibraryModel;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;

/**
 * Adds a library to an open {@link StoryModel}.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class AddLibraryToStoryModelAction extends ActiveModelSensitiveAction {
	private static final String ADD_LIBRARY = "Add Library";

	private static final Action instance = new AddLibraryToStoryModelAction();

	/**
	 * Returns the only instance of the action.
	 * 
	 * @return
	 */
	public static Action getInstance() {
		return instance;
	}

	private AddLibraryToStoryModelAction() {
		super(ADD_LIBRARY);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	protected boolean isLegal() {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		return super.isLegal() && model instanceof StoryModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final File location;

		location = WindowFactory.getInstance().showFileChooser(ADD_LIBRARY, "",
				FileManager.LIBRARY_FILTER);

		if (location != null) {
			final StoryModel model;
			final LibraryModel library;

			model = (StoryModel) SEModelManager.getInstance().getActiveModel();
			library = FileIO.getInstance().readLibrary(location);

			if (library != null)
				model.addLibrary(library);
		}
	}
}
