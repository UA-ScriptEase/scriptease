package scriptease.gui.action.library;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.dialog.DialogBuilder;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Adds a library to an open {@link StoryModel}.
 * 
 * @author kschenk
 * @author jyuen
 */
@SuppressWarnings("serial")
public class AddLibraryToStoryModelAction extends ActiveModelSensitiveAction {
	private final LibraryModel library;

	public AddLibraryToStoryModelAction(LibraryModel library) {
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
		DialogBuilder.getInstance().showAddLibraryInfoDialog(this.library);
	}
}
