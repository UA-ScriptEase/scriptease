package scriptease.gui.graph;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import scriptease.controller.observer.GraphNodeEvent;
import scriptease.controller.observer.GraphNodeEvent.GraphNodeEventType;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.util.GUIOp;

public abstract class SEGraphNodeRenderer<E> {
	// This is such a weird hack. I apologize. - remiller
	private Set<JComponent> hoverComponents = new HashSet<JComponent>();
	private Set<JComponent> pressComponents = new HashSet<JComponent>();

	
	public abstract JComponent getComponentForNode(E node);

	/**
	 * Sets up the listeners.
	 * 
	 * @param node
	 * @param component
	 */
	public void configureListeners(final E node, final JComponent component) {
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

				/*	GraphNodeEvent event = new GraphNodeEvent(node,
							GraphNodeEventType.SELECTED);

					event.setShiftDown(e.isShiftDown());
					node.notifyObservers(event);*/

					SEGraphNodeRenderer.this.pressComponents.remove(src);

					configureAppearance(src, node);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					final JComponent src = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.pressComponents.add(src);
					configureAppearance(src, node);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					final JComponent nodeComponent = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.hoverComponents
							.add(nodeComponent);
					configureAppearance(nodeComponent, node);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					final JComponent nodeComponent = (JComponent) e.getSource();

					SEGraphNodeRenderer.this.hoverComponents
							.remove(nodeComponent);
					SEGraphNodeRenderer.this.pressComponents
							.remove(nodeComponent);

					configureAppearance(nodeComponent, node);
				}
			};

			component.addMouseListener(mouseAdapter);
			component.addMouseMotionListener(mouseAdapter);
		}
	}

	/**
	 * Method to configure the component's appearance.
	 * 
	 * @param component
	 *            The display component to configure.
	 * @param node
	 *            The graph node to configure based on.
	 */
	public void configureAppearance(final JComponent component, E node) {
		if (component == null)
			return;

		final boolean isHover = this.hoverComponents.contains(component);
		final boolean isPressed = this.pressComponents.contains(component);

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
		if (isHover) {
			if (isPressed) {
				backgroundColour = toolPress;
			} else {
				backgroundColour = toolHighlight;
			}
			borderColour = GUIOp.scaleColour(toolColour, 0.7);
		} /*else if (node == SEGraph.this.getSelectedNode()) {
			backgroundColour = ScriptEaseUI.SELECTED_GRAPH_NODE;
			borderColour = GUIOp.scaleColour(ScriptEaseUI.SELECTED_GRAPH_NODE,
					0.6);
		}*/ else {
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

}
