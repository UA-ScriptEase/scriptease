package scriptease.gui.action.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.observer.SEFocusObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelJList;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Represents and performs the Delete command, as well as encapsulates its
 * enabled and name display state.
 * 
 * @author remiller
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class DeleteAction extends ActiveModelSensitiveAction implements
		SEFocusObserver {
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
		} else if (focusOwner instanceof SEGraph) {
			isLegal = !((SEGraph<?>) focusOwner).isReadOnly();
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
		} else if (focusOwner instanceof StoryComponentPanelJList) {
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
		}

		// Reset the focus after we delete something.
		// SEFocusManager.getInstance().setFocus(null);
	}
}
