package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

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
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.container.AbstractContainerFragment;

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
		final Component focusOwner;
		focusOwner = SEFocusManager.getInstance().getFocus();

		if (focusOwner instanceof StoryComponentPanel) {
			return super.isLegal()
					&& ((StoryComponentPanel) focusOwner).isRemovable();
		} else if (focusOwner instanceof StoryComponentPanelJList) {
			return super.isLegal()
					&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel;
		} else if (focusOwner instanceof SEGraph) {
			return super.isLegal() && !((SEGraph<?>) focusOwner).isReadOnly();
		} else if (focusOwner instanceof CodeFragmentPanel) {
			return super.isLegal()
					&& ((CodeFragmentPanel) focusOwner).getFragment() != null;
		} else
			return false;
	}

	/**
	 * Delete the selected FormatFragment in the list of FormatFragments after
	 * the other specified line fragment. This gets called recursively until the
	 * selected fragment is found. To start at the top, pass in "null" as the
	 * parent fragment.
	 * 
	 * @param topLevelFormatFragments
	 * @param selected
	 * @param parent
	 * @return
	 */
	private void deleteFragment(
			final List<AbstractFragment> topLevelFormatFragments,
			final AbstractFragment selected,
			final AbstractContainerFragment parent) {

		if (topLevelFormatFragments.remove(selected)) {
			if (parent != null)
				parent.setSubFragments(topLevelFormatFragments);

		} else {
			for (AbstractFragment fragment : topLevelFormatFragments) {
				if (fragment instanceof AbstractContainerFragment) {
					final AbstractContainerFragment container;

					container = (AbstractContainerFragment) fragment;

					this.deleteFragment(new ArrayList<AbstractFragment>(
							container.getSubFragments()), selected, container);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

			if (manager != null) {
				manager.deleteSelected();
			}
		} else if (focusOwner instanceof StoryComponentPanelJList
				&& SEModelManager.getInstance().getActiveModel() instanceof LibraryModel
				&& (!((LibraryModel) SEModelManager.getInstance()
						.getActiveModel()).getReadOnly() || ScriptEase.DEBUG_MODE)) {

			/*
			 * TODO Needs undoability
			 * 
			 * Ticket: 48089063
			 */
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

				final LibraryModel libraryModel;

				selectedComponent = ((StoryComponentPanel) value)
						.getStoryComponent();

				libraryModel = selectedComponent.getLibrary();

				if (selectedComponent instanceof KnowIt) {
					final DescribeIt describeIt;

					describeIt = libraryModel.getDescribeIt(selectedComponent);

					if (describeIt != null)
						libraryModel.removeDescribeIt(describeIt);
				}

				libraryModel.remove(selectedComponent);

				if (!alreadyUndoing) {
					UndoManager.getInstance().endUndoableAction();
				}
			}
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

			if (codeBlock != null) {
				final List<AbstractFragment> origFragments;
				final List<AbstractFragment> fragments;
				final AbstractFragment selectedFragment;

				origFragments = codeBlock.getCode();
				fragments = AbstractFragment.cloneFragments(origFragments);
				selectedFragment = AbstractFragment.getInSamePosition(
						panel.getFragment(), origFragments, fragments);

				if (selectedFragment != null) {
					this.deleteFragment(fragments, selectedFragment, null);

					UndoManager.getInstance().startUndoableAction(
							"Setting CodeBlock " + codeBlock + " code to "
									+ fragments);
					codeBlock.setCode(fragments);
					UndoManager.getInstance().endUndoableAction();
				}
			}
		}

		// Reset the focus after we delete something.
		// SEFocusManager.getInstance().setFocus(null);
	}
}
