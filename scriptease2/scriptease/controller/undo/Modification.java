package scriptease.controller.undo;

/**
 * Represents one reversible atomic state change. This is derived from the
 * Command Pattern. The operation for moving forward, or 'doing', is defined by
 * {@link #redo()}, and the operation for moving backwards, or 'undoing' is
 * defined by {@link #undo()}.
 * 
 * @author friesen
 * @author remiller
 */
public interface Modification {
	/**
	 * Performs the modification.
	 */
	public void redo();

	/**
	 * Performs the opposite operation to undo the modification.
	 */
	public void undo();
}
