package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.ScriptEase;
import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.libraryeditor.codeblocks.CodeFragmentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;

/**
 * Represents and performs the Delete command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author remiller
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class DeleteAction extends ActiveModelSensitiveAction {
	private static final String DELETE_TEXT = "Delete";

	private static final Action instance = new DeleteAction();

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static Action getInstance() {
		return DeleteAction.instance;
	}

	/**
	 * Defines a <code>DeleteStoryComponentAction</code> object with no icon.
	 */
	private DeleteAction() {
		super(DeleteAction.DELETE_TEXT);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		SEFocusManager.getInstance().addSEFocusObserver(new SEFocusObserver() {

			@Override
			public void gainFocus(Component oldFocus) {
				DeleteAction.this.updateEnabledState();
			}

			@Override
			public void loseFocus(Component oldFocus) {
				DeleteAction.this.updateEnabledState();
			}
		});
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * current selection.
	 */
	protected boolean isLegal() {
		final Component focusOwner = SEFocusManager.getInstance().getFocus();
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		if (focusOwner instanceof StoryComponentPanel) {
			return super.isLegal()
					&& ((StoryComponentPanel) focusOwner).isRemovable();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			return super.isLegal()
					&& ((model instanceof LibraryModel) || (model instanceof Translator));
		} else if (focusOwner instanceof SEGraph) {
			return super.isLegal() && !((SEGraph<?>) focusOwner).isReadOnly();
		} else if (focusOwner instanceof CodeFragmentPanel) {
			return super.isLegal()
					&& ((CodeFragmentPanel) focusOwner).getFragment() != null;
		} else
			return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void actionPerformed(ActionEvent e) {
		final SEModel model = SEModelManager.getInstance().getActiveModel();
		final Component focusOwner;

		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			// Delete StoryComponentPanels
			final StoryComponentPanel panel;
			final StoryComponentPanelManager manager;

			panel = (StoryComponentPanel) focusOwner;
			manager = panel.getSelectionManager();

			if (manager != null) {
				manager.deleteSelected();
			}
		} else if (focusOwner instanceof StoryComponentPanelJList
				&& ((model instanceof Translator) || (model instanceof LibraryModel && (!((LibraryModel) model)
						.isReadOnly() || ScriptEase.DEBUG_MODE)))) {

			final boolean alreadyUndoing = UndoManager.getInstance()
					.hasOpenUndoableAction();
			if (!alreadyUndoing) {
				UndoManager.getInstance().startUndoableAction("Delete");
			}

			// Delete elements from StoryComponentPanelJList
			final StoryComponentPanelJList list;
			list = (StoryComponentPanelJList) focusOwner;

			for (Object value : list.getSelectedValues()) {
				final StoryComponent selectedComponent;

				final LibraryModel library;

				selectedComponent = ((StoryComponentPanel) value)
						.getStoryComponent();

				library = selectedComponent.getLibrary();

				if (library == LibraryModel.getCommonLibrary())
					continue;

				if (selectedComponent instanceof KnowIt) {
					final DescribeIt describeIt;

					describeIt = library.getDescribeIt(selectedComponent);

					if (describeIt != null)
						library.removeDescribeIt(describeIt);
				}

				library.remove(selectedComponent);
			}

			if (UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().endUndoableAction();
		} else if (focusOwner instanceof SEGraph) {
			// Raw types here, but the way Graphs are set up, these should work
			final SEGraph graph;

			graph = (SEGraph) focusOwner;

			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction("Remove nodes");

			for (Object node : graph.getSelectedNodes()) {
				graph.removeNode(node);
			}

			UndoManager.getInstance().endUndoableAction();
		} else if (focusOwner instanceof CodeFragmentPanel) {
			final CodeFragmentPanel panel = (CodeFragmentPanel) focusOwner;
			final CodeBlock codeBlock = panel.getCodeBlock();

			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction(
						"Delete Code Fragment");
			codeBlock.deleteCodeFragment(panel.getFragment());
			UndoManager.getInstance().endUndoableAction();
		}
	}
}
