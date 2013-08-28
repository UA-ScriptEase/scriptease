package scriptease.gui.action.libraryeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.Slot;

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

		// Add the include files
		final Collection<String> includeFiles = library.getIncludeFilePaths();
		includeFiles.addAll(libraryToMerge.getIncludeFilePaths());

		// Add the slots
		final Collection<Slot> slots = libraryToMerge.getSlots();
		final Collection<Slot> newSlots = new ArrayList<Slot>();
		for (Slot slot : slots) {
			Slot clone = slot.clone();

			// Remove all existing implicits, we don't want references to the
			// old library.
			clone.setImplicits(new ArrayList<KnowIt>());

			for (KnowIt implicit : slot.getImplicits()) {

				final KnowItBinding binding = implicit.getBinding();
				if (binding instanceof KnowItBindingFunction) {

					final ScriptIt scriptIt = (ScriptIt) binding.getValue();

					final ScriptIt scriptItClone = this.cloneScriptIt(scriptIt,
							library);

					final KnowIt knowItClone = implicit.clone();

					knowItClone.setBinding(new KnowItBindingFunction(
							scriptItClone));

					clone.addImplicit(knowItClone);
				}
			}

			newSlots.add(clone);

		}
		library.addSlots(newSlots);

		// Add the causes, descriptions, effects, and controls
		final StoryComponentContainer causes = libraryToMerge
				.getCausesCategory();
		final Collection<DescribeIt> describeIts = libraryToMerge
				.getDescribeIts();
		final StoryComponentContainer effects = libraryToMerge
				.getEffectsCategory();
		final StoryComponentContainer controls = libraryToMerge
				.getControllersCategory();

		for (StoryComponent cause : causes.getChildren()) {
			library.add(this.cloneScriptIt((ScriptIt) cause, library));
		}

		for (DescribeIt describeIt : describeIts) {
			library.add(describeIt);
		}

		for (StoryComponent effect : effects.getChildren()) {
			final ScriptIt scriptIt = (ScriptIt) effect;
			final ScriptIt clone = this.cloneScriptIt(scriptIt, library);

			final StoryComponentContainer descrips = library
					.getDescriptionsCategory();

			for (StoryComponent description : descrips.getChildren()) {
				KnowIt knowIt = (KnowIt) description;
				KnowItBinding binding = knowIt.getBinding();

				if (binding instanceof KnowItBindingFunction) {
					KnowItBindingFunction function = (KnowItBindingFunction) binding;

					ScriptIt value = function.getValue();

					if (value.getDisplayText()
							.equals(scriptIt.getDisplayText())
							&& value.getMainCodeBlock().getId() == scriptIt
									.getMainCodeBlock().getId()) {
						function.setValue(clone);
					}
				}
			}

			library.add(clone);
		}

		for (StoryComponent control : controls.getChildren()) {
			library.add(this.cloneScriptIt((ScriptIt) control, library));
		}
	}

	private ScriptIt cloneScriptIt(ScriptIt scriptIt, LibraryModel library) {
		final ScriptIt clone = scriptIt.clone();

		// Remove all codeblocks referencing the old library
		for (CodeBlock codeBlock : clone.getCodeBlocks())
			clone.removeCodeBlock(codeBlock);

		for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
			if (codeBlock instanceof CodeBlockSource) {
				CodeBlockSource source = (CodeBlockSource) codeBlock;
				clone.addCodeBlock(source.duplicate(library
						.getNextCodeBlockID()));
			}
		}

		return clone;
	}
}
