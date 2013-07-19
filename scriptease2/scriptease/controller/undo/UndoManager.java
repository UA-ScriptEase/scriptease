package scriptease.controller.undo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scriptease.controller.FileManager;
import scriptease.controller.observer.FileManagerObserver;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.UndoManagerObserver;
import scriptease.gui.StatusManager;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;

/**
 * Maintains multiple modification histories and manages requests for undoing or
 * redoing an operation within those histories. There is one undo history and
 * one redo history for every Pattern Model. Every history has a maximum size
 * that is defined by the user preferences file.<br>
 * <br>
 * Each unit of 'undoability' is called an <i>Undoable Action</i>. Undoable
 * Actions represent operations made by the user, and are composites of
 * <i>Modifications</i>. Modifications are individual state changes, like
 * setting a pattern's name. New Undoable Actions are opened by calling
 * {@link #startUndoableAction(String)}. Modifications may then be recorded by
 * using {@link #appendModification(Modification)}. To complete an Undoable
 * Action, use {@link #endUndoableAction()}, which closes the open Undoable
 * Action and places it into the undo history of the pattern model it was
 * started for. Undoable Actions are immutable and atomic after
 * <code>endUndoableAction</code> has been called.<br>
 * <br>
 * It is illegal to start a new action before closing an old action as a safety
 * precaution. While it other semantics could be implemented, it was deemed to
 * be safer to disallow that operation.<br>
 * <br>
 * Undoing a command automatically adds that command to the redo history, and
 * vice versa. The undo history can be queried to see if there are any unsaved
 * changes for the active pattern model. Unsaved changes are defined as being
 * any change made to an Undo-enabled object after a Save Marker or the bottom
 * of the stack. Save Markers are special empty commands inserted into the
 * history via #appendSaveMarker()<br>
 * <br>
 * UndoManager implements the Singleton pattern.
 * 
 * @author friesen
 * @author remiller
 * @author mfchurch
 */
public final class UndoManager {
	private final static UndoManager instance = new UndoManager();
	private ObserverManager<UndoManagerObserver> observerManager;

	private List<History> storyHistories = new ArrayList<History>();
	private History activeHistory;

	/**
	 * The command that is currently being filled in. This will be null when we
	 * are not recording an undoable command.
	 */
	private UndoableCommand unfinishedCommand = null;

	/**
	 * This is true only while the UndoManager is performing an undo or redo
	 * operation.
	 */
	private boolean performingUndoRedo = false;

	private UndoManager() {
		this.observerManager = new ObserverManager<UndoManagerObserver>();

		final SEModelObserver modelObserver;
		final FileManagerObserver fileObserver;

		modelObserver = new SEModelObserver() {
			@Override
			public void modelChanged(SEModelEvent event) {
				final SEModelEvent.Type eventType = event.getEventType();
				final SEModel model = event.getPatternModel();

				// Keep an up-to-date mapping of open models to their histories
				if (eventType == SEModelEvent.Type.ADDED) {
					UndoManager.this.storyHistories.add(new History(model));
				} else if (eventType == SEModelEvent.Type.REMOVED) {
					History removed = UndoManager.this
							.findHistoryForModel(model);

					if (removed != null
							&& UndoManager.this.activeHistory != null) {
						UndoManager.this.storyHistories.remove(removed);

						if (UndoManager.this.activeHistory.equals(removed)) {
							UndoManager.this.activeHistory = null;
							UndoManager.this.notifyObservers();
						}

						if (!removed.undoStack.isEmpty()
								&& (removed.undoStack.peek() == UndoManager.this.unfinishedCommand))
							UndoManager.this.unfinishedCommand = null;
					}
				} else if (eventType == SEModelEvent.Type.ACTIVATED) {
					UndoManager.this.setActiveHistory(model);
				}
			}
		};

		fileObserver = new FileManagerObserver() {
			@Override
			public void fileReferenced(SEModel model, File location) {
				History history = UndoManager.this.findHistoryForModel(model);

				history.markSaved();

				UndoManager.this.notifyObservers();
			}
		};

		SEModelManager.getInstance().addSEModelObserver(this, modelObserver);
		FileManager.getInstance().addModelObserver(this, fileObserver);
	}

	public final void addUndoManagerObserver(Object object,
			UndoManagerObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	public final void removeUndoManagerObserver(UndoManagerObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	protected final void notifyObservers() {
		for (UndoManagerObserver observer : this.observerManager.getObservers())
			observer.stackChanged();
	}

	/**
	 * Gets the instance of UndoMananger.
	 * 
	 * @return The sole instance of this class.
	 */
	public static UndoManager getInstance() {
		return UndoManager.instance;
	}

	/**
	 * Starts a new undoable action and causes the UndoManager to begin
	 * recording model changes into that new action. Clearing the redo stack is
	 * a side-effect.
	 * 
	 * @param name
	 *            The name of the action being performed. The name will be
	 *            displayed in the view, so it should be the same name as the
	 *            name of the operation they chose to perform.
	 */
	public void startUndoableAction(String name) {
		if (this.hasOpenUndoableAction())
			throw new IllegalStateException(
					"Cannot start a new undoable action inside of undoable action '"
							+ this.unfinishedCommand + "'");

		if (this.activeHistory != null)
			this.unfinishedCommand = new UndoableCommand(name);
	}

	/**
	 * Adds a new modification to the current Undoable Action. If there is no
	 * open Undoable Action, an IllegalStateException is thrown.
	 * 
	 * @param newChange
	 *            The command that can perform and reverse the change.
	 * @throws IllegalStateException
	 *             if there is no Undoable Action open to append to.
	 */
	public void appendModification(Modification newChange) {
		if (!this.hasOpenUndoableAction())
			throw new IllegalStateException(
					"UndoManager tried to append a modification when there is no open UndoableAction.");

		this.unfinishedCommand.addModification(newChange);
	}

	/**
	 * Pulls the previous undoable action from the undoStack and appends,
	 * instead of starting a new action.
	 * 
	 * Introduced to deal with some transfer handler problems which caused
	 * inconsistencies in the undo/redo stacks.
	 */
	public void appendToLastAction() {
		if (this.hasOpenUndoableAction())
			throw new IllegalStateException(
					"Cannot start a new undoable action inside of undoable action '"
							+ this.unfinishedCommand + "'");

		History history = this.getActiveHistory();
		if (history.undoStack.isEmpty())
			throw new IllegalAccessError(
					"Undostack is empty, you cannot append to the last undoable action");

		this.unfinishedCommand = history.undoStack.pop();
	}

	/**
	 * Clears the active redo stack
	 * 
	 */
	public void clearRedo() {
		this.activeHistory.redoStack.clear();
		this.notifyObservers();
	}

	/**
	 * Marks the current unfinished action as finished.
	 * 
	 * @throws IllegalStateException
	 *             if there isn't an unfinished command to close or if the
	 *             active stack is not the same as was opened.
	 */
	public void endUndoableAction() {
		final History activeHistory = this.getActiveHistory();

		String model = "";
		if (activeHistory.getModel() != null)
			model = activeHistory.getModel() + " Stack";

		activeHistory.undoStack.push(this.unfinishedCommand);
		// Make sure there is something to close and that the one we're closing
		// is the one that was opened.
		if (!this.hasOpenUndoableAction())
			throw new IllegalStateException(
					"Tried to finish an undoable action when there is not one open.");
		else if (this.unfinishedCommand != activeHistory.undoStack.peek())
			throw new IllegalStateException(
					"UndoManager: Failed to end UndoableCommand - active history was not as expected.");

		// empty commands get trashed and ignored.
		if (this.unfinishedCommand.getModificationCount() < 1) {
			activeHistory.undoStack.pop();

			System.err
					.println("Undo: Command \""
							+ this.unfinishedCommand.getName()
							+ "\" was not included in the undo history because it is empty.");
		} else {
			activeHistory.redoStack.clear();
			System.out.println("Undoable Action "
					+ this.unfinishedCommand.getName() + " added to " + model);
		}

		activeHistory.ensureMaxSize();
		this.unfinishedCommand = null;
		this.notifyObservers();
	}

	/**
	 * Undoes the most recent operation in the active pattern model's undo
	 * history and moves it into the redo history.
	 * 
	 * @throws IllegalStateException
	 *             if there is nothing to undo.
	 */
	public void undo() {
		final UndoableCommand command;
		final History history;

		if (!this.canUndo())
			throw new IllegalStateException("Received an illegal undo request.");

		history = this.getActiveHistory();

		command = history.undoStack.pop();
		System.out.println("Undo " + command);
		this.performingUndoRedo = true;
		command.undo();
		history.redoStack.push(command);
		this.performingUndoRedo = false;
		StatusManager.getInstance().setTemp("Undo " + command.getName());
		this.notifyObservers();
	}

	/**
	 * Undoes the top operation on the redo stack, pushing it onto the undo
	 * stack.
	 * 
	 * @throws IllegalStateException
	 *             if there is nothing to redo.
	 */
	public void redo() {
		final UndoableCommand command;
		final History history;

		if (!this.canRedo())
			// this can happen if there is an unfinished command open, there
			// isn't anything to redo, or we were already in the middle of
			// undoing or redoing something
			throw new IllegalStateException("Received an illegal redo request.");

		history = this.getActiveHistory();

		this.performingUndoRedo = true;
		command = history.redoStack.pop();
		System.out.println("Redo " + command);
		command.redo();
		history.undoStack.push(command);
		this.performingUndoRedo = false;
		StatusManager.getInstance().setTemp("Redo " + command.getName());
		this.notifyObservers();
	}

	/**
	 * Destroys both the undo and redo histories. Use this after a major change
	 * that you don't want the user to be able to undo.
	 */
	public void clear() {
		this.getActiveHistory().clear();
		this.notifyObservers();
	}

	/**
	 * Determines whether there are any remaining actions to be undone.
	 * 
	 * @return True if this UndoManager can undo again, false otherwise
	 */
	public boolean canUndo() {
		final History history = this.activeHistory;
		return history != null && history.canUndo()
				&& !this.isUndoingOrRedoing() && !this.hasOpenUndoableAction();
	}

	/**
	 * Determines whether there are any remaining actions to be redone.
	 * 
	 * @return True if this UndoManager can redo again, false otherwise
	 */
	public boolean canRedo() {
		final History history = this.activeHistory;
		return history != null && history.canRedo()
				&& !this.isUndoingOrRedoing() && !this.hasOpenUndoableAction();
	}

	/**
	 * Determines whether the given model's changes have been saved or not.
	 * 
	 * @param model
	 *            the model to test for savedness
	 * 
	 * @return <code>true</code> if the given model's changes has been saved.
	 */
	public boolean isSaved(SEModel model) {
		History history = this.findHistoryForModel(model);
		if (history != null)
			return history.isSaved();
		return true;
	}

	/**
	 * Gets the name of the UndoableAction that would be performed if
	 * {@link #undo()} were called.
	 * 
	 * @return the name of the most recent UndoableAction in the Undo history.
	 */
	public String getLastUndoName() {
		History history = this.getActiveHistory();
		if (history != null)
			return history.getLastUndoName();
		return "";
	}

	/**
	 * Gets the name of the UndoableAction that would be performed if
	 * {@link #redo()} were called.
	 * 
	 * @return the name of the most recent UndoableAction in the Redo history.
	 */
	public String getLastRedoName() {
		History history = this.getActiveHistory();
		if (history != null)
			return history.getLastRedoName();
		return "";
	}

	/**
	 * Determines whether the UndoManager is currently undoing or redoing. If an
	 * operation is being performed for the first time, this is not considered
	 * to be "redoing".
	 * 
	 * @return <code>true</code> if the UndoManager is currently undoing or
	 *         redoing something.
	 */
	public boolean isUndoingOrRedoing() {
		return this.performingUndoRedo;
	}

	/**
	 * Determines whether the UndoManager has an open Undoable Action.
	 * 
	 * @return <code>true</code> iff there is an open Undoable Action.
	 */
	public boolean hasOpenUndoableAction() {
		return this.unfinishedCommand != null;
	}

	/**
	 * @return the History for the active pattern model
	 */
	private History getActiveHistory() {
		return this.activeHistory;
	}

	/**
	 * Sets the activeHistory to that of the given PatternModel.
	 * 
	 * @param model
	 */
	public void setActiveHistory(SEModel model) {
		if (model != null) {
			History storyHistory = this.findHistoryForModel(model);
			if (storyHistory != null) {
				this.activeHistory = storyHistory;
				this.notifyObservers();
			}
		}
	}

	private History findHistoryForModel(SEModel model) {
		for (History candidate : this.storyHistories) {
			if (candidate.getModel() == model) {
				return candidate;
			}
		}
		return null;
	}
}
