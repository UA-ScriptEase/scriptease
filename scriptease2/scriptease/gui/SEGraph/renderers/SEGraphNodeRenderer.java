package scriptease.gui.SEGraph.renderers;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import scriptease.gui.PanelFactory;
import scriptease.gui.SEFocusManager;
import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction.ToolBarMode;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.util.GUIOp;

/**
 * Renders individual components as graph nodes. The default behaviour sets the
 * background and border of the JComponent depending on the current tool
 * selected and the actions of the user.<br>
 * <br>
 * To add internal components to the JComponent, override the
 * {@link #configureInternalComponents(JComponent, Object)} method.
 * 
 * @author remiller
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphNodeRenderer<E> {
	private final SEGraph<E> graph;

	// This is such a weird hack. I apologize. - remiller
	private JComponent hoveredComponent = null;
	private JComponent pressedComponent = null;

	public SEGraphNodeRenderer(SEGraph<E> graph) {
		this.graph = graph;
	}

	/**
	 * Creates a component for the passed in node.
	 * 
	 * @param node
	 * @return
	 */
	public final JComponent createComponentForNode(E node) {
		final JComponent component;

		component = PanelFactory.getInstance().buildGradientPanel(1.3);

		this.reconfigureAppearance(component, node);
		this.configureInternalComponents(component, node);

		component.addMouseListener(this.componentAppearanceMouseListener(node));

		return component;
	}

	/**
	 * By default, this does nothing.<br>
	 * <br>
	 * It can be used by subclasses to add any special components inside of the
	 * component representing the node. For example, StoryPoint nodes add Fan In
	 * panels and binding widgets for the StoryPoint.
	 * 
	 * @param component
	 * @param node
	 */
	protected void configureInternalComponents(JComponent component, E node) {
	}

	protected E getStartNode() {
		return this.graph.getStartNode();
	}

	/**
	 * Creates a mouse adapter for appearance. For mouse listeners on components
	 * that act on the model, see {@link SEGraph}.
	 */
	private MouseAdapter componentAppearanceMouseListener(final E node) {
		return new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();
				final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

				/*
				 * Only respond to releases that happen over this component. The
				 * default is to respond to releases if the press occurred in
				 * this component. This seems to be a Java bug, but I can't find
				 * any kind of complaint for it. Either way, we want this
				 * behaviour, not the default. - remiller
				 */
				if (!component.contains(
						mouseLoc.x - component.getLocationOnScreen().x,
						mouseLoc.y - component.getLocationOnScreen().y))
					return;

				SEGraphNodeRenderer.this.pressedComponent = null;

				resetAppearances();

				/*
				 * We redraw the actual clicked on components by calling
				 * reconfigureAppearance(JComponent, E) in SEGraph. This is
				 * because the listeners are added in a different order. This
				 * does not work out in the case of selecting multiple nodes.
				 */
			}

			@Override
			public void mousePressed(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoveredComponent = null;
				SEGraphNodeRenderer.this.pressedComponent = component;
				reconfigureAppearance(component, node);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoveredComponent = component;
				reconfigureAppearance(component, node);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				final JComponent component = (JComponent) e.getSource();

				SEGraphNodeRenderer.this.hoveredComponent = null;
				SEGraphNodeRenderer.this.pressedComponent = null;

				reconfigureAppearance(component, node);
			}
		};
	}

	/**
	 * Resets the appearance of all components to the default white colour.
	 */
	public void resetAppearances() {
		final E lastSelectedNode;

		lastSelectedNode = this.graph.getLastSelectedNode();

		for (Entry<E, JComponent> entry : this.graph.getNodesToComponentsMap()
				.getEntrySet()) {
			if (this.graph.getSelectedComponents().contains(entry.getValue()))
				continue;

			final E key = entry.getKey();

			final Color backgroundColour;

			if (this.graph.getParents(lastSelectedNode).contains(key)) {
				backgroundColour = ScriptEaseUI.COLOUR_PARENT_NODE;
			} else if (this.graph.getChildren(lastSelectedNode).contains(key)) {
				backgroundColour = ScriptEaseUI.COLOUR_CHILD_NODE;
			} else {
				backgroundColour = ScriptEaseUI.COLOUR_NODE_DEFAULT;
			}

			this.setComponentAppearance(entry.getValue(), key, backgroundColour);
		}
	}

	/**
	 * Sets the appearance of the passed in node to the background colour and
	 * border colour passed in.
	 * 
	 * @param component
	 *            The component to set the appearance for.
	 * @param borderColour
	 *            The border colour to set on the component.
	 * @param backgroundColour
	 *            The background colour to set for the component.
	 */
	private void setComponentAppearance(JComponent component, E node,
			Color backgroundColour) {
		final int INNER_BORDER_THICKNESS = 3;

		final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(
				INNER_BORDER_THICKNESS, INNER_BORDER_THICKNESS,
				INNER_BORDER_THICKNESS, INNER_BORDER_THICKNESS);

		final Border innerBorder;
		final Border lineBorder;
		final Border lineSpaceBorder;

		lineBorder = BorderFactory.createRaisedBevelBorder();
		innerBorder = EMPTY_BORDER;

		if (node != this.graph.getStartNode()) {
			lineSpaceBorder = BorderFactory.createCompoundBorder(lineBorder,
					innerBorder);
		} else {
			final Border secondLineBorder;

			secondLineBorder = BorderFactory.createCompoundBorder(lineBorder,
					innerBorder);
			lineSpaceBorder = BorderFactory.createCompoundBorder(
					secondLineBorder, BorderFactory.createLoweredBevelBorder());
		}

		component.setBorder(lineSpaceBorder);
		component.setBackground(backgroundColour);
	}

	/**
	 * Method to configure the component's appearance based on the current mode
	 * of the ToolBar.
	 * 
	 * @param component
	 *            The display component to configure.
	 * @param node
	 *            The graph node to configure based on.
	 */
	public void reconfigureAppearance(final JComponent component, final E node) {
		if (component == null)
			return;
		// The default colour of the tool.
		final Color toolColour;
		// The colour when we hover with the tool
		final Color toolHighlight;
		// The colour when we press with the tool
		final Color toolPress;

		final Color backgroundColour;

		// first, determine the tool colour and highlight.
		/*
		 * These are using the game object colours because they're convenient
		 * and close enough. Feel free to add colours to ScriptEaseUI if you
		 * want other colours. - remiller
		 */
		if (this.hoveredComponent == component) {
			final ToolBarMode mode;

			mode = GraphToolBarModeAction.getMode();

			if (!graph.isReadOnly()
					&& (mode == ToolBarMode.INSERT || mode == ToolBarMode.CONNECT)) {
				toolColour = ScriptEaseUI.COLOUR_INSERT_NODE;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.1);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.1);
			} else if (!graph.isReadOnly()
					&& (mode == ToolBarMode.DELETE || mode == ToolBarMode.DISCONNECT)) {
				toolColour = ScriptEaseUI.COLOUR_DELETE_NODE;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.2);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.4);
			} else {
				toolColour = ScriptEaseUI.COLOUR_SELECTED_NODE;
				toolHighlight = GUIOp.scaleWhite(toolColour, 1.25);
				toolPress = GUIOp.scaleWhite(toolHighlight, 1.1);
			}

			if (this.pressedComponent == component) {
				// If pressed while being hovered over
				backgroundColour = toolPress;
			} else {
				// If hovered over
				backgroundColour = toolHighlight;
			}
		} else if (this.graph.getSelectedNodes().contains(node)) {
			/*
			 * Use a bright tool colour if its pressed, use the tool colour if
			 * it's hovered over, use gold if its selected and not hovered,
			 * white/gray otherwise.
			 */
			final Color initialColour;

			if (graph.equals(SEFocusManager.getInstance().getFocus())) {
				initialColour = ScriptEaseUI.COLOUR_SELECTED_NODE;
			} else {
				initialColour = GUIOp.scaleWhite(
						ScriptEaseUI.COLOUR_SELECTED_NODE, 1.2);
			}

			if (this.pressedComponent == component) {
				backgroundColour = GUIOp.scaleWhite(initialColour, 1.2);
			} else
				backgroundColour = initialColour;

			// If nothing and selected
		} else {
			final E lastSelectedNode;

			lastSelectedNode = this.graph.getLastSelectedNode();

			if (this.graph.getParents(lastSelectedNode).contains(node)) {
				backgroundColour = ScriptEaseUI.COLOUR_PARENT_NODE;
			} else if (this.graph.getChildren(lastSelectedNode).contains(node)) {
				backgroundColour = ScriptEaseUI.COLOUR_CHILD_NODE;
			} else {
				backgroundColour = ScriptEaseUI.COLOUR_NODE_DEFAULT;
			}
		}

		this.setComponentAppearance(component, node, backgroundColour);
	}
}