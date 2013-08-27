package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.StoryComponentContainer;
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

		WindowFactory.getInstance()
				.buildMergeLibraryChoiceDialog(library.getTranslator())
				.setVisible(true);
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

		final StoryComponentContainer causes = libraryToMerge
				.getCausesCategory();
		final StoryComponentContainer effects = libraryToMerge
				.getEffectsCategory();
		final StoryComponentContainer descriptions = libraryToMerge
				.getDescriptionsCategory();
		final StoryComponentContainer controls = libraryToMerge
				.getControllersCategory();

//		// Add the include files
//		final Collection<String> includeFiles = library.getIncludeFilePaths();
//		includeFiles.addAll(libraryToMerge.getIncludeFilePaths());
//		library.setIncludeFilePaths(includeFiles);
//
//		for (StoryComponent cause : causes.getChildren()) {
//			CauseIt causeIt = (CauseIt) cause;
//
//			CauseIt clone = causeIt.clone();
//
//			for (CodeBlock codeBlock : clone.getCodeBlocks())
//				clone.removeCodeBlock(codeBlock);
//
//			for (CodeBlock codeBlock : causeIt.getCodeBlocks()) {
//				if (codeBlock instanceof CodeBlockSource) {
//					CodeBlockSource source = (CodeBlockSource) codeBlock;
//					clone.addCodeBlock(source.copySource());
//				}
//			}
//
//			library.add(clone);
//		}
//
//		for (StoryComponent effect : effects.getChildren()) {
//			library.add(effect.clone());
//		}
//
//		for (StoryComponent description : descriptions.getChildren()) {
//			library.add(description.clone());
//		}
//
//		for (StoryComponent control : controls.getChildren()) {
//			library.add(control.clone());
//		}
	}
}
