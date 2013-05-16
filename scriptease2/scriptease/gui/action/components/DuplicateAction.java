package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Action;

import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.gui.SEFocusManager;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.APIDictionary;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Represents and performs the Duplicate command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author mfchurch
 */
@SuppressWarnings("serial")
public final class DuplicateAction extends ActiveModelSensitiveAction implements
		SEFocusObserver {
	private static final String DUPLICATE_TEXT = "Duplicate";

	private static final Action instance = new DuplicateAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DuplicateAction.instance;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private DuplicateAction() {
		super(DuplicateAction.DUPLICATE_TEXT);

		SEFocusManager.getInstance().addSEFocusObserver(this);
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final SEModel activeModel;
		final Component focusOwner;
		final boolean isLegal;

		focusOwner = SEFocusManager.getInstance().getFocus();
		activeModel = SEModelManager.getInstance().getActiveModel();

		if (focusOwner instanceof StoryComponentPanel) {
			isLegal = ((StoryComponentPanel) focusOwner).isRemovable();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			isLegal = SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
		} else
			isLegal = false;

		return activeModel != null && isLegal;
	}

	@Override
	public void gainFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	@Override
	public void loseFocus(Component oldFocus) {
		this.updateEnabledState();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Delete StoryComponentPanels
			final StoryComponentPanel panel;
			final StoryComponentPanelManager manager;

			panel = (StoryComponentPanel) focusOwner;
			manager = panel.getSelectionManager();

			if (manager != null)
				manager.duplicateSelected();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			// Delete elements from StoryComponentPanelJList
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object selectedObject : list.getSelectedValues()) {
				final StoryComponentPanel selectedPanel;
				final StoryComponent selectedComponent;

				final Translator activeTranslator;
				final APIDictionary apiDictionary;
				final LibraryModel libraryModel;

				activeTranslator = TranslatorManager.getInstance()
						.getActiveTranslator();
				apiDictionary = activeTranslator.getApiDictionary();
				libraryModel = apiDictionary.getLibrary();

				selectedPanel = (StoryComponentPanel) selectedObject;
				selectedComponent = selectedPanel.getStoryComponent();

				selectedComponent.process(new StoryAdapter() {

					// Clone ScriptIts, then replace the referenced codeBlocks
					// with modifiable duplicates since we want them to be
					// unique
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						final ScriptIt clone = scriptIt.clone();
						final Collection<CodeBlock> codeBlocks = clone
								.getCodeBlocks();
						for (CodeBlock codeBlock : codeBlocks) {
							clone.removeCodeBlock(codeBlock);
							codeBlock.process(new StoryAdapter() {

								@Override
								public void processCodeBlockSource(
										CodeBlockSource codeBlockSource) {
									final CodeBlockSource duplicate = codeBlockSource
											.duplicate(libraryModel
													.getNextCodeBlockID());
									clone.addCodeBlock(duplicate);
								}

								@Override
								public void processCodeBlockReference(
										CodeBlockReference codeBlockReference) {
									codeBlockReference.getTarget()
											.process(this);
								}
							});
						}
						libraryModel.add(clone);
					}

					@Override
					protected void defaultProcess(StoryComponent component) {
						final StoryComponent clone = component.clone();
						libraryModel.add(clone);
					}
				});
			}
		}

		// Reset the focus after we duplicate something.
		SEFocusManager.getInstance().setFocus(null);
	}
}
