package scriptease.gui.SETree.ui;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.UIManager;

import scriptease.util.GUIOp;

/**
 * Common interface for all of the ScriptEase UI classes.
 * 
 * @author remiller
 */
public interface ScriptEaseUI {
	/**
	 * The colour (green) used to display known object bindings.
	 */
	public static final Color COLOUR_KNOWN_OBJECT = new Color(20, 175, 0);

	/**
	 * The colour (sort of a light blue) used to display game object bindings.
	 */
	public static final Color COLOUR_GAME_OBJECT = new Color(102, 140, 255);

	/**
	 * The colour (Warning Red) used to display unbound bindings.
	 */
	public static final Color COLOUR_UNBOUND = new Color(242, 0, 0);

	/**
	 * The colour (Simple Brown) used to display simple bindings.
	 */
	public static final Color COLOUR_SIMPLE = new Color(164, 78, 18);

	/**
	 * The colour (Light Grey) used for bound type widgets
	 */
	public static final Color COLOUR_BOUND = Color.LIGHT_GRAY;

	/**
	 * The colour (gold) used for selected graph nodes
	 */
	public static final Color SELECTED_GRAPH_NODE = new Color(255, 234, 67);

	/**
	 * The colour (White) used by Fragments by default Story Component Builder.
	 */
	public static final Color FRAGMENT_DEFAULT_COLOR = Color.white;
	/**
	 * The colour (Grey) used for the code editor in Story Component Builder.
	 */
	public static final Color CODE_EDITOR_COLOR = Color.GRAY;

	/**
	 * The colour (Light Grey) used for Line Fragments in Story Component
	 * Builder.
	 */
	public static final Color LINE_FRAGMENT_COLOR = Color.LIGHT_GRAY;

	/**
	 * The colour (Light Grey) used for the Indent Fragments in Story Component
	 * Builder.
	 */
	public static final Color INDENT_FRAGMENT_COLOR = Color.LIGHT_GRAY;

	/**
	 * The colour (Dark Green) used for the Scope Fragments in Story Component
	 * Builder.
	 */
	public static final Color SCOPE_FRAGMENT_COLOR = GUIOp.scaleColour(
			Color.GREEN, 0.5);

	/**
	 * The colour (Dark Orange) used for the Series Fragments in Story Component
	 * Builder.
	 */
	public static final Color SERIES_FRAGMENT_COLOR = new Color(250, 165, 17).darker();

	/**
	 * The colour (Blue) used for the Simple Fragments in Story Component
	 * Builder.
	 */
	public static final Color SIMPLE_FRAGMENT_COLOR = Color.BLUE;

	/**
	 * The colour (Gray) used for the Literal Fragments in Story Component
	 * Builder.
	 */
	public static final Color LITERAL_FRAGMENT_COLOR = Color.GRAY;

	/**
	 * The colour (Purple) used for the Reference Fragments in Story Component
	 * Builder.
	 */
	public static final Color REFERENCE_FRAGMENT_COLOR = GUIOp.scaleColour(
			Color.magenta, 0.7);

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
