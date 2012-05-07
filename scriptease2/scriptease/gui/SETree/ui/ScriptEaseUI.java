package scriptease.gui.SETree.ui;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Common interface for all of the ScriptEase UI classes.
 * 
 * @author remiller
 */
public interface ScriptEaseUI {
	/**
	 * The colour (green) used to display game object bindings.
	 */
	public static final Color COLOUR_GAME_OBJECT = new Color(20, 175, 0);

	/**
	 * The colour (sort of a light blue) used to display known object bindings.
	 */
	public static final Color COLOUR_KNOWN_OBJECT = new Color(0.40f, 0.55f,
			1.0f);

	/**
	 * The colour (Warning Red) used to display unbound bindings.
	 */
	public static final Color COLOUR_UNBOUND = new Color(0.95f, 0.00f, 0.0f);

	/**
	 * The colour (Simple Brown) used to display simple bindings.
	 */
	public static final Color COLOUR_SIMPLE = new Color(164, 78, 18);

	/**
	 * The colour (Light Grey) used for bound type widgets
	 */
	public static final Color COLOUR_BOUND = Color.LIGHT_GRAY;

	/**
	 * Icon used to indicate a collapse will occur
	 */
	public static final Icon COLLAPSE_ICON = (Icon) UIManager
			.get("Tree.expandedIcon");

	/**
	 * Icon used to indicate an expand will occur
	 */
	public static final Icon EXPAND_ICON = (Icon) UIManager
			.get("Tree.collapsedIcon");
}
