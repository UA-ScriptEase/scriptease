package scriptease.gui.action.library;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

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
	private final LibraryModel library;

	public AddLibraryToStoryModelAction(LibraryModel library) {
		super(library.getName());

		this.library = library;

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
		final StoryModel model;

		model = (StoryModel) SEModelManager.getInstance().getActiveModel();

		model.addLibrary(this.library);
	}
}