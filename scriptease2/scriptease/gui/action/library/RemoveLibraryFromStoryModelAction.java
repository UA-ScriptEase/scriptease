package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Removes a library from an open {@link StoryModel}
 * 
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class RemoveLibraryFromStoryModelAction extends
		ActiveModelSensitiveAction {

	private final LibraryModel library;

	public RemoveLibraryFromStoryModelAction(LibraryModel library) {
		super(library.getTitle());

		this.setEnabled(true);

		this.library = library;
	}

	@Override
	protected boolean isLegal() {
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		return super.isLegal() && model instanceof StoryModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DialogBuilder.getInstance().showRemoveLibraryInfoDialog(this.library);
	}
	
	public LibraryModel getLibrary() {
		return library;
	}
}
