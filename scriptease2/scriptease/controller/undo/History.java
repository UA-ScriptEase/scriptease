package scriptease.controller.undo;

import java.util.Stack;

import scriptease.ScriptEase;
import scriptease.model.PatternModel;

/**
 * Stores the undo and redo modification histories for a Pattern Model.
 * {@link #markSaved()} will mark that the changes performed in the undo portion
 * of a History have been saved to disk. A History can also share whether its
 * changes have been saved or not with {@link #isSaved()}.
 * 
 * @author remiller
 */
public final class History {
	/**
	 * The maximum number of Undoable Actions allowed in either the redo stack
	 * or undo stack.
	 */
	public static final int MAX_STACK_SIZE = Integer.parseInt(ScriptEase
			.getInstance().getPreference(ScriptEase.UNDO_STACK_SIZE_KEY));

	protected final Stack<UndoableCommand> undoStack;
	protected final Stack<UndoableCommand> redoStack;
	private final PatternModel model;
	private UndoableCommand lastSavedAction;

	/**
	 * Creates a new History object with a null model.
	 */
	protected History() {
		this(null);
	}

	/**
	 * Creates a new History object.
	 */
	protected History(PatternModel model) {
		this.undoStack = new Stack<UndoableCommand>();
		this.redoStack = new Stack<UndoableCommand>();
		this.model = model;
		this.lastSavedAction = null;
	}

	/**
	 * Ensures that both the undo and redo stacks have not exceeded their
	 * maximum size. If a stack has become too large, the modifications deepest
	 * into that stack are removed until it meets the size requirement.
	 */
	protected void ensureMaxSize() {
		this.ensureMaxSize(this.undoStack);
		this.ensureMaxSize(this.redoStack);
	}

	private void ensureMaxSize(Stack<UndoableCommand> stack) {
		while (stack.size() > History.MAX_STACK_SIZE) {
			stack.remove(stack.firstElement());
		}
	}

	/**
	 * Gets the model that this History applies to.
	 * 
	 * @return the history's target model
	 */
	public final PatternModel getModel() {
		return this.model;
	}

	/**
	 * Destroys both the undo and redo histories. Use this after a major change
	 * that you don't want the user to be able to undo.
	 */
	public void clear() {
		this.undoStack.clear();
		this.redoStack.clear();
	}

	/**
	 * Determines whether there are any remaining actions to be undone.
	 * 
	 * @return <code>true</code> if there is anything in the undo history.
	 */
	protected boolean canUndo() {
		return !this.undoStack.isEmpty();
	}

	/**
	 * Determines whether there are any remaining actions to be redone.
	 * 
	 * @return <code>true</code> if there is anything in the redo history.
	 */
	protected boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	/**
	 * Gets the name of the UndoableAction at the top of the undo stack.
	 * 
	 * @return the name of the most recent UndoableAction in the Undo history.
	 */
	public String getLastUndoName() {
		if (!this.undoStack.empty() && this.undoStack.lastElement() != null)
			return this.undoStack.lastElement().getName();
		return "";
	}

	/**
	 * Gets the name of the UndoableAction at the top of the redo stack.
	 * 
	 * @return the name of the most recent UndoableAction in the Redo history.
	 */
	public String getLastRedoName() {
		if (!this.redoStack.empty() && this.redoStack.lastElement() != null)
			return this.redoStack.lastElement().getName();
		return "";
	}

	/**
	 * Marks the UndoableAction at the top of the Undo stack as the last saved
	 * action. if there are no actions to mark as saved, this method does
	 * nothing.
	 */
	public final void markSaved() {
		if (!this.undoStack.isEmpty())
			this.lastSavedAction = this.undoStack.peek();
		else
			this.lastSavedAction = null;
	}

	/**
	 * Marks the UndoableAction at the top of the Undo stack as the last saved
	 * action. It is also considered saved if the undo stack is empty.
	 * 
	 * @return <code>true</code> if the past actions have been saved.
	 */
	public final boolean isSaved() {
		if (!this.undoStack.isEmpty()) {
			return this.undoStack.peek() == this.lastSavedAction;
		} else
			return this.lastSavedAction == null;
	}
}