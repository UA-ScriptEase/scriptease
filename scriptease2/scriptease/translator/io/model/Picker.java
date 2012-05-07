package scriptease.translator.io.model;

/**
 * A picker is a GUI widget that allows the user to choose a GameObject. <br>
 * <br>
 * Pickers should have some widgets for selecting the GameObject's method
 * of object resolution and its blueprint.<br>
 * <br>
 * An example picker would have a tree view of the categories of blueprint at
 * the top, with radio buttons for selecting object resolution method
 * 
 * @author remiller
 * 
 */
public interface Picker {
	
	/**
	 * Gets the current selection from the picker. Cannot be null.
	 * 
	 * @return The GameDataIsntance selected by the picker.
	 */
	public GameObject getSelection();
}
