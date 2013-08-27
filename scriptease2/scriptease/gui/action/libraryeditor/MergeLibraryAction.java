package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

@SuppressWarnings("serial")
/**
 * MergeLibraryAction will combine the selected library with the library that is 
 * currently open.
 * 
 * @author jyuen
 */
public class MergeLibraryAction extends ActiveModelSensitiveAction {
	private static final String MERGE_LIBRARY_NAME = "Merge Library";

	private static final MergeLibraryAction instance = new MergeLibraryAction();

	public static MergeLibraryAction getInstance() {
		return MergeLibraryAction.instance;
	}

	private MergeLibraryAction() {
		super(MergeLibraryAction.MERGE_LIBRARY_NAME);
		this.putValue(Action.SHORT_DESCRIPTION,
				MergeLibraryAction.MERGE_LIBRARY_NAME);
		this.putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK
						+ ActionEvent.SHIFT_MASK));
	}

	@Override
	protected boolean isLegal() {
		return super.isLegal()
				&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final LibraryModel library = (LibraryModel) SEModelManager
				.getInstance().getActiveModel();

		if (library == null)
			return;

		WindowFactory.getInstance().buildMergeLibraryChoiceDialog(
				library.getTranslator());
	}

	/**
	 * Merges the given library into the active library.
	 * 
	 * @param libraryToMerge
	 */
	public void mergeLibrary(LibraryModel libraryToMerge) {
		final LibraryModel library = (LibraryModel) SEModelManager
				.getInstance().getActiveModel();

		if (library == null || libraryToMerge == null
				|| library == libraryToMerge)
			return;
		
		//final StoryComponentlibraryToMerge.getCausesCategory();
		
		
	}
}
