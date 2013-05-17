package scriptease.gui.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.border.Border;

import scriptease.util.GUIOp;

/**
 * Common interface for all of the ScriptEase UI classes.
 * 
 * @author remiller
 * @author jyuen
 */
public interface ScriptEaseUI {
	/**
	 * The colour (green) used to display known object bindings.
	 */
	public static final Color COLOUR_KNOWN_OBJECT = new Color(20, 175, 0);

	/**
	 * The colour (lighter green) used to display the background of text fields
	 * with known object bindings.
	 */
	public static final Color COLOUR_KNOWN_OBJECT_INNER = new Color(213, 255,
			201);

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
	 * The coulour (light brown) used to display text in simple bindings.
	 */
	public static final Color COLOUR_SIMPLE_TEXT = new Color(237, 186, 128);
	
	/**
	 * The colour (Light Grey) used for bound type widgets
	 */
	public static final Color COLOUR_BOUND = Color.LIGHT_GRAY;

	/**
	 * The colour (Grey) used for selected UI Components
	 */
	public static final Color SELECTED_COLOUR = new Color(200, 200, 200);

	/**
	 * The colour (White) used for unselected UI components
	 */
	public static final Color UNSELECTED_COLOUR = Color.WHITE;

	/**
	 * The colour (light blue) used by default for node backgrounds.
	 */
	public static final Color COLOUR_NODE_DEFAULT = new Color(205, 221, 250);

	/**
	 * The colour (dark blue) used for selected graph nodes
	 */
	public static final Color COLOUR_SELECTED_NODE = new Color(89, 147, 255);

	/**
	 * The colour (darker blue) used for parent of selected node
	 */
	public static final Color COLOUR_PARENT_NODE = new Color(190, 180, 255);

	/**
	 * The colour (lighter blue) used for children of selected node
	 */
	public static final Color COLOUR_CHILD_NODE = new Color(180, 255, 185);

	/**
	 * The colour (red) used for the delete node tool
	 */
	public static final Color COLOUR_DELETE_NODE = new Color(255, 97, 97);

	/**
	 * The colour (green) used for the insert node tool
	 */
	public static final Color COLOUR_INSERT_NODE = new Color(176, 255, 97);

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
	public static final Color SERIES_FRAGMENT_COLOR = new Color(250, 165, 17)
			.darker();

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
	 * The text colour (Dark Grey) we use for Notes.
	 */
	public static final Color COLOUR_NOTE_TEXT = new Color(133, 133, 133);

	/**
	 * The color used for the background of the text field for Notes.
	 */
	public static final Color COLOUR_NOTE_TEXT_BG = new Color(255, 252, 161);

	/**
	 * The standard increment that should usually be used for vertical
	 * scrollbars.
	 */
	public static final int VERTICAL_SCROLLBAR_INCREMENT = 16;

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

	/**
	 * Cursor used for adding nodes to a graph.
	 */
	public static final Cursor CURSOR_NODE_ADD = GUIOp.createCursor("node_add");

	/**
	 * Cursor used for deleting nodes from the graph.
	 */
	public static final Cursor CURSOR_NODE_DELETE = GUIOp
			.createCursor("node_delete");

	/**
	 * Cursor used for drawing paths between nodes.
	 */
	public static final Cursor CURSOR_PATH_DRAW = GUIOp
			.createCursor("path_draw");

	/**
	 * Cursor used for erasing paths between nodes.
	 */
	public static final Cursor CURSOR_PATH_ERASE = GUIOp
			.createCursor("path_erase");

	/**
	 * Cursor to represent unavailable actions. A circle with a line through it,
	 * like in No Smoking or No Pie signs.
	 */
	public static final Cursor CURSOR_UNAVAILABLE = GUIOp.createCursor(
			"unavailable", new Point(15, 15));

	/**
	 * The maximum screen width that ScriptEase can support. Can be increased if
	 * we encounter ridiculous situations (e.g. 15 Macbook Pros with retina
	 * displays running ScriptEase for the entire length)
	 */
	public static final int MAX_SCREEN_WIDTH = 2400;

	/**
	 * The border used for selected UI components.
	 */
	public static final Border SELECTED_BORDER = BorderFactory
			.createMatteBorder(1, 1, 1, 1, Color.black);

	public static final Border UNSELECTED_BORDER = BorderFactory
			.createEmptyBorder(1, 1, 1, 1);
}
