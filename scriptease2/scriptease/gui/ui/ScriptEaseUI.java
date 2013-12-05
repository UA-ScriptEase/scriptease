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

	public static final Color BUTTON_ORANGE = new Color(199, 78, 44);
	public static final Color BUTTON_GREEN = new Color(5, 142, 5);
	public static final Color BUTTON_BURGUNDY = new Color(160, 26, 59);
	public static final Color BUTTON_TEAL = new Color(5, 142, 158);
	public static final Color BUTTON_YELLOW = new Color(234, 160, 31);
	public static final Color BUTTON_BLUE = new Color(50, 131, 224);
	public static final Color BUTTON_PURPLE = new Color(91, 61, 173);
	public static final Color BUTTON_BLACK = new Color(68, 68, 68);

	public static final Color PRIMARY_UI = Color.WHITE;
	public static final Color SECONDARY_UI = BUTTON_BLACK;
	public static final Color TERTIARY_UI = Color.gray;

	public static final Color BUTTON_RED = new Color(255, 69, 40);

	/**
	 * The colour (white) used to display graph group backgrounds.
	 */
	public static final Color COLOUR_GROUP_BACKGROUND = Color.WHITE;

	/**
	 * The colour (purple) used to display uninitialized knowIts.
	 */
	public static final Color COLOUR_KNOWIT_UNINITIALIZED = new Color(187, 0,
			196);

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
	public static final Color SELECTED_COLOUR = new Color(140, 140, 140);

	/**
	 * The colour (White) used for unselected UI components
	 */
	public static final Color UNSELECTED_COLOUR = Color.WHITE;

	/**
	 * The colour (Secondary UI) used for selected graph nodes
	 */
	public static final Color COLOUR_SELECTED_NODE = BUTTON_BLUE;

	/**
	 * The colour (red) used for the delete node tool
	 */
	public static final Color COLOUR_DELETE_NODE = new Color(255, 97, 97);

	/**
	 * The colour (green) used for the insert node tool
	 */
	public static final Color COLOUR_INSERT_NODE = new Color(176, 255, 97);

	/**
	 * The colour (green) used for the insert node tool
	 */
	public static final Color COLOUR_GROUPABLE_END_NODE = new Color(122, 255,
			117);

	/**
	 * The colour (green) used for the insert node tool
	 */
	public static final Color COLOUR_GROUPABLE_NODE = new Color(162, 240, 250);

	/**
	 * The text colour (Dark Grey) we use for Notes.
	 */
	public static final Color COLOUR_NOTE_TEXT = new Color(133, 133, 133);

	/**
	 * The color used for the background of the text field for Notes.
	 */
	public static final Color COLOUR_NOTE_TEXT_BG = new Color(255, 252, 161);

	/**
	 * The colour (Bright Pink) used for disabled components.
	 */
	public static final Color COLOUR_DISABLED = new Color(255, 106, 0);

	public static final Color COLOUR_ADD_BUTTON = COLOUR_KNOWN_OBJECT;

	public static final Color COLOUR_ADD_BUTTON_PRESSED = GUIOp.scaleColour(
			COLOUR_ADD_BUTTON, 1.2);

	public static final Color COLOUR_ADD_BUTTON_HOVER_FILL = GUIOp.scaleWhite(
			COLOUR_ADD_BUTTON, 4.0);

	public static final Color COLOUR_ADD_BUTTON_PRESSED_FILL = GUIOp
			.scaleWhite(COLOUR_ADD_BUTTON, 3.4);

	public static final Color COLOUR_REMOVE_BUTTON = COLOUR_UNBOUND;

	public static final Color COLOUR_REMOVE_BUTTON_PRESSED = GUIOp.scaleColour(
			COLOUR_REMOVE_BUTTON, 1.2);

	public static final Color COLOUR_REMOVE_BUTTON_HOVER_FILL = GUIOp
			.scaleWhite(COLOUR_REMOVE_BUTTON, 3.7);

	public static final Color COLOUR_REMOVE_BUTTON_PRESSED_FILL = GUIOp
			.scaleWhite(COLOUR_REMOVE_BUTTON, 3.2);

	public static final Color COLOUR_EDIT_BUTTON = COLOUR_GAME_OBJECT;

	public static final Color COLOUR_EDIT_BUTTON_PRESSED = GUIOp.scaleColour(
			COLOUR_EDIT_BUTTON, 1.2);

	public static final Color COLOUR_EDIT_BUTTON_HOVER_FILL = GUIOp.scaleWhite(
			COLOUR_EDIT_BUTTON, 1.5);

	public static final Color COLOUR_EDIT_BUTTON_PRESSED_FILL = GUIOp
			.scaleWhite(COLOUR_EDIT_BUTTON, 1.4);

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
	 * Cursor to represent select actions.
	 */
	public static final Cursor CURSOR_SELECT = GUIOp.createCursor("select",
			new Point(10, 10));

	/**
	 * Cursor to represent unavailable actions. A circle with a line through it,
	 * like in No Smoking or No Pie signs.
	 */
	public static final Cursor CURSOR_UNAVAILABLE = GUIOp.createCursor(
			"unavailable", new Point(15, 15));

	/**
	 * Cursor to represent the start of the grouping action.
	 */
	public static final Cursor CURSOR_UNGROUP = GUIOp.createCursor("ungroup",
			new Point(15, 15));
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
