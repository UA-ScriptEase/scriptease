package scriptease.controller.undo;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic Command (of the Command design pattern) that is a composite of
 * changes made to a model object. <br>
 * <br>
 * The UndoFactory aspect will watch for any modifications done within that
 * method, and automatically generate the undo and redo methods from it, using
 * Modifications.
 * 
 * @author friesen
 * @author remiller
 */
public final class UndoableCommand {
	private final List<Modification> modifications = new ArrayList<Modification>();
	private boolean locked = false;
	private final String name;

	/**
	 * Builds a new UndoableCommand that has the given name.
	 * 
	 * @param name
	 *            The name for the command.
	 */
	protected UndoableCommand(String name) {
		this.name = name;
	}

	/**
	 * Permanently prevents this command from receiving any more modifications.
	 */
	protected void finish() {
		this.locked = true;
	}

	/**
	 * Adds a Modification to the command. This operation cannot be performed on
	 * an UndoableCommand that has been sent a call to {@link #finish()}.
	 * 
	 * @param command
	 * @throws IllegalStateException
	 *             if called after a call to {@link #finish()}
	 */
	protected final void addModification(Modification command) {
		if (this.locked)
			throw new IllegalStateException(
					"Cannot add modifications to a sealed UndoableCommand.");

		this.modifications.add(command);
	}

	/**
	 * Undoes each modification in the UndoableCommand in reverse order that
	 * they were added.
	 */
	protected final void undo() {
		// I wish there was a foreach statement that went backwards. - remiller
		for (int i = this.modifications.size() - 1; i >= 0; i--) {
			Modification modification = this.modifications.get(i);
			modification.undo();
		}
	}

	/**
	 * Performs each modification in this UndoableCommand in the order that they
	 * were added.
	 */
	protected final void redo() {
		for (Modification mod : this.modifications) {
			mod.redo();
		}
	}

	/**
	 * Gets the number of modifications in this UndoableCommand.
	 * 
	 * @return the number of modifications in the UndoableCommand. Always
	 *         greater than or equal to 0.
	 */
	protected int getModificationCount() {
		return this.modifications.size();
	}

	/**
	 * Gets the name of this command.
	 * 
	 * @return the command name.
	 */
	protected String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.getName() + "[" + this.getModificationCount() + "]";
	}
}
