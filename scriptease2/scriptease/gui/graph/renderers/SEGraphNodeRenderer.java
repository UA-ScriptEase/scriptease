package scriptease.gui.graph.renderers;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.SEGraph;
import scriptease.util.BiHashMap;
import scriptease.util.GUIOp;

/**
 * Renders individual components as graph nodes. There is some default
 * behaviour, but most will have to be implemented in a sub class that overrides
 * the {@link #configureInternalComponents(JComponent, Object)} method.
 * 
 * @author remiller
 * @author kschenk
 * 
 * @param <E>
 */
public class SEGraphNodeRenderer<E> {
	// This is such a weird hack. I apologize. - remiller
	private Set<JComponent> hoverComponents = new HashSet<JComponent>();
	private Set<JComponent> pressComponents = new HashSet<JComponent>();

	private BiHashMap<E, JComponent> componentMap = new BiHashMap<E, JComponent>();

	public final JComponent getComponentForNode(E node, SEGraph<E> graph) {
		final JComponent component;
		// check if the node already has a component
		final JComponent storedComponent = this.componentMap.getValue(node);
		if (storedComponent != null) {
			component = storedComponent;
		} else {
			// otherwise build it and store it
			component = new JPanel();
			this.configureAppearance(component, node, graph);
			this.configureListeners(component, node, graph);
			this.configureInternalComponents(component, node);
			this.componentMap.put(node, component);
		}
		// return the component for the node
		return component;
	}

	public E getNodeForComponent(JComponent component) {
		return this.componentMap.getKey(component);
	}

	/**
	 * By default, this does nothing.<br>
	 * <br>
	 * It can be used by subclasses to add any special components inside of the
	 * component representing the node. For example, QuestPoint nodes add Fan In
	 * panels and binding widgets for the Quest Point.
	 * 
	 * @param component
	 * @param node
	 */
	protected void configureInternalComponents(JComponent component, E node) {
	}

	/**
	 * Sets up the listeners for appearance. For mouse listeners on components
	 * that act on the model, see {@link SEGraph}.
	 * 
	 * @param component
	 * @param node
	 */
	private void configureListeners(final JComponent component, final E node,
			final SEGraph<E> graph) {
		if (component != null) {
			/*
			 * When a component is clicked, forward the click to the GraphNode,
			 * and its observers.
			 */
			final MouseAdapter mouseAdapter = new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					final JComponent src = (JComponent) e.getSource();
					final Point mouseLoc = MouseInfo.getPointerInfo()
							.getLocation();

					/*
					 * Only respond to releases that happen over this component.
					 * The default is to respond to releases if the press
					 * occurred in this component. This seems to be a Java bug,
					 * but I can't find any kind of complaint for it. Either
					 * way, we want this behaviour, not the default. - remiller
					 */
					if (!src.contains(mouseLoc.x - src.getLocationOnScreen().x,
							mouseLoc.y - src.getLocationOnScreen().y))
						return;

					SEGraphNodeRenderer.this.pressComponents.remove(src);

					configureAppearance(src, node, graph);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					final JComponent src = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.pressComponents.add(src);
					configureAppearance(src, node, graph);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					final JComponent nodeComponent = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.hoverComponents.add(nodeComponent);
					configureAppearance(nodeComponent, node, graph);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					final JComponent nodeComponent = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.hoverComponents
							.remove(nodeComponent);
					SEGraphNodeRenderer.this.pressComponents
							.remove(nodeComponent);

					configureAppearance(nodeComponent, node, graph);
				}
			};

			component.addMouseListener(mouseAdapter);
		}
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
	private void configureAppearance(final JComponent component, E node,
			SEGraph<E> graph) {
		if (component == null)
			return;

		final Color toolColour;
		final Color toolHighlight;
		final Color toolPress;

		Color borderColour;
		Color backgroundColour;

		// first, determine the tool colour and highlight.
		/*
		 * These are using the game object colours because they're convenient
		 * and close enough. Feel free to add colours to ScriptEaseUI if you
		 * want other colours. - remiller
		 */
		if (ToolBarButtonAction.getMode() == ToolBarButtonMode.INSERT_GRAPH_NODE) {
			toolColour = ScriptEaseUI.COLOUR_KNOWN_OBJECT;
			toolHighlight = GUIOp.scaleWhite(toolColour, 1.6);
			toolPress = GUIOp.scaleWhite(toolHighlight, 1.8);
		} else if (ToolBarButtonAction.getMode() == ToolBarButtonMode.DELETE_GRAPH_NODE) {
			toolColour = ScriptEaseUI.COLOUR_UNBOUND;
			toolHighlight = GUIOp.scaleWhite(toolColour, 1.3);
			toolPress = GUIOp.scaleWhite(toolHighlight, 1.8);
		} else {
			toolColour = ScriptEaseUI.SELECTED_GRAPH_NODE;
			toolHighlight = GUIOp.scaleColour(ScriptEaseUI.SELECTED_GRAPH_NODE,
					1.05);
			toolPress = GUIOp.scaleColour(toolHighlight, 1.6);
		}

		/*
		 * Use a bright tool colour if its pressed, use the tool colour if it's
		 * hovered over, use gold if its selected and not hovered, white/gray
		 * otherwise.
		 */
		if (this.hoverComponents.contains(component)) {
			if (this.pressComponents.contains(component)) {
				// If pressed while being hovered over
				backgroundColour = toolPress;
			} else {
				// If hovered over
				backgroundColour = toolHighlight;
			}
			borderColour = GUIOp.scaleColour(toolColour, 0.7);
		} else if (graph.getSelectedNode() == node) {
			// If nothing and selected
			backgroundColour = ScriptEaseUI.SELECTED_GRAPH_NODE;
			borderColour = GUIOp.scaleColour(ScriptEaseUI.SELECTED_GRAPH_NODE,
					0.6);
		} else {
			// If nothing
			backgroundColour = Color.white;
			borderColour = Color.GRAY;
		}

		// Double lined border on terminal nodes
		Border lineBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED,
				borderColour, borderColour.darker());
		Border lineSpaceBorder = BorderFactory.createCompoundBorder(lineBorder,
				BorderFactory.createEmptyBorder(3, 3, 3, 3));

		component.setBorder(lineSpaceBorder);
		component.setBackground(backgroundColour);
		component.setOpaque(true);
	}

	// TODO
	/*
	 * 
	 * 
	 * 
	 * private void resetAppearance :D
	 * 
	 * 
	 * reset the appearance to a regular look, with white background and default
	 * border and such!
	 * 
	 * Should happen on press.
	 * 
	 */

}