package scriptease.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.gui.WidgetDecorator;
import scriptease.gui.action.graphs.ConnectModeAction;
import scriptease.gui.action.graphs.DeleteModeAction;
import scriptease.gui.action.graphs.DisconnectModeAction;
import scriptease.gui.action.graphs.GraphToolBarModeAction;
import scriptease.gui.action.graphs.InsertModeAction;
import scriptease.gui.action.graphs.SelectModeAction;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.SEModelManager;
import scriptease.util.GUIOp;

/**
 * For creation of specialized JComponents. If we're just adding properties to a
 * component, it should probably be decorated instead in {@link WidgetDecorator}
 * . Also, if we end up making a lot of one type of component, we should
 * probably move them out into their own factory. For example, if we make lots
 * of ToolBars, we should make a ToolBarFactory.
 * 
 * @author kschenk
 * 
 */
public final class ComponentFactory {
	/**
	 * Builds a ToolBar to edit graphs with. Includes buttons for selecting
	 * nodes, adding and deleting nodes, and adding and deleting paths. The
	 * ToolBar buttons only set the mode; the graph itself contains the specific
	 * actions that should happen.
	 * 
	 * @return
	 */
	public static JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar;
		final ButtonGroup graphEditorButtonGroup;
		final ArrayList<JToggleButton> buttonList;

		final JToggleButton selectNodeButton;
		final JToggleButton insertNodeButton;
		final JToggleButton deleteNodeButton;
		final JToggleButton connectNodeButton;
		final JToggleButton disconnectNodeButton;

		final Runnable selectButtonRunnable;

		graphEditorToolBar = new JToolBar();
		graphEditorButtonGroup = new ButtonGroup();
		buttonList = new ArrayList<JToggleButton>();

		selectNodeButton = new JToggleButton(SelectModeAction.getInstance());
		insertNodeButton = new JToggleButton(InsertModeAction.getInstance());
		deleteNodeButton = new JToggleButton(DeleteModeAction.getInstance());
		connectNodeButton = new JToggleButton(ConnectModeAction.getInstance());
		disconnectNodeButton = new JToggleButton(
				DisconnectModeAction.getInstance());

		selectButtonRunnable = new Runnable() {
			@Override
			public void run() {

				switch (GraphToolBarModeAction.getMode()) {

				case SELECT:
					graphEditorButtonGroup.setSelected(
							selectNodeButton.getModel(), true);
					break;
				case DELETE:
					graphEditorButtonGroup.setSelected(
							deleteNodeButton.getModel(), true);
					break;
				case INSERT:
					graphEditorButtonGroup.setSelected(
							insertNodeButton.getModel(), true);
					break;
				case CONNECT:
					graphEditorButtonGroup.setSelected(
							connectNodeButton.getModel(), true);
					break;
				case DISCONNECT:
					graphEditorButtonGroup.setSelected(
							disconnectNodeButton.getModel(), true);
					break;
				}
			}
		};

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.PAGE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);
		graphEditorToolBar.setBackground(Color.WHITE);

		buttonList.add(selectNodeButton);
		buttonList.add(insertNodeButton);
		buttonList.add(deleteNodeButton);
		buttonList.add(connectNodeButton);
		buttonList.add(disconnectNodeButton);

		for (JToggleButton toolBarButton : buttonList) {
			toolBarButton.setHideActionText(true);
			toolBarButton.setFocusable(false);
			graphEditorButtonGroup.add(toolBarButton);
			graphEditorToolBar.add(toolBarButton);
		}

		SEModelManager.getInstance().addSEModelObserver(graphEditorToolBar,
				new SEModelObserver() {
					@Override
					public void modelChanged(SEModelEvent event) {
						if (event.getEventType() == SEModelEvent.Type.ACTIVATED)
							selectButtonRunnable.run();
					}
				});

		selectButtonRunnable.run();

		return graphEditorToolBar;
	}

	private static enum ButtonType {
		ADD, REMOVE;
	}

	public static JButton buildRemoveButton() {
		return buildButton(ButtonType.REMOVE);
	}

	public static JButton buildAddButton() {
		return buildButton(ButtonType.ADD);
	}

	@SuppressWarnings("serial")
	private static JButton buildButton(final ButtonType type) {

		return new JButton() {
			private static final int SIZEXY = 24;

			{
				final Dimension size = new Dimension(SIZEXY, SIZEXY);

				this.setPreferredSize(size);
				this.setMaximumSize(size);
				this.setMinimumSize(size);
				this.setSize(size);

				this.setOpaque(false);
				this.setFocusable(false);
				this.setContentAreaFilled(false);
			}

			@Override
			protected void paintComponent(Graphics g) {
				final Color armedFillColour;
				final Color armedLineColour;
				final Color hoverFillColour;
				final Color unarmedLineColour;

				if (type == ButtonType.ADD) {
					armedFillColour = ScriptEaseUI.COLOUR_ADD_BUTTON_PRESSED_FILL;
					armedLineColour = ScriptEaseUI.COLOUR_ADD_BUTTON_PRESSED;
					hoverFillColour = ScriptEaseUI.COLOUR_ADD_BUTTON_HOVER_FILL;
					unarmedLineColour = ScriptEaseUI.COLOUR_ADD_BUTTON;
				} else if (type == ButtonType.REMOVE) {
					armedFillColour = ScriptEaseUI.COLOUR_REMOVE_BUTTON_PRESSED_FILL;
					armedLineColour = ScriptEaseUI.COLOUR_REMOVE_BUTTON_PRESSED;
					hoverFillColour = ScriptEaseUI.COLOUR_REMOVE_BUTTON_HOVER_FILL;
					unarmedLineColour = ScriptEaseUI.COLOUR_REMOVE_BUTTON;
				} else {
					armedFillColour = Color.LIGHT_GRAY;
					armedLineColour = Color.DARK_GRAY;
					hoverFillColour = Color.GRAY;
					unarmedLineColour = Color.BLACK;
				}

				final Graphics2D g2d = (Graphics2D) g;
				final ButtonModel model = this.getModel();

				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.setStroke(new BasicStroke(1.4f));

				final int circleX = 3;
				final int circleY = 3;
				final int diameter = 18;

				// The offset between lines and the circle
				final int lineOffset = 4;

				final int radius = diameter / 2;

				final int horizX1 = circleX + lineOffset;
				final int horizX2 = circleX + diameter - lineOffset;
				final int horizY = circleY + radius;

				final int vertiY1 = circleY + lineOffset;
				final int vertiY2 = circleY + diameter - lineOffset;
				final int vertiX = circleX + radius;

				final Color lineColour;

				if (model.isArmed()) {
					// If it's clicked, do this

					g2d.setColor(armedFillColour);

					g2d.fillOval(circleX, circleY, diameter, diameter);

					lineColour = armedLineColour;
				} else if (model.isRollover()) {
					g2d.setColor(hoverFillColour);
					g2d.fillOval(circleX, circleY, diameter, diameter);
					lineColour = unarmedLineColour;
				} else {
					lineColour = unarmedLineColour;
				}

				g2d.setColor(lineColour);

				// Draw the circle
				g2d.drawOval(circleX, circleY, diameter, diameter);
				if (type == ButtonType.ADD)
					// Draw the vertical line only for add.
					g2d.drawLine(vertiX, vertiY1, vertiX, vertiY2);
				// Draw the horizontal line
				g2d.drawLine(horizX1, horizY, horizX2, horizY);

				super.paintComponent(g);
				g2d.dispose();

			}
		};
	}

	/**
	 * Creates a JTextField that uses a JLabel as a background. The background
	 * disappears when the JTextField is focused on and does not appear if there
	 * is text inside the field.
	 * 
	 * @param size
	 * @param label
	 * @return
	 */
	@SuppressWarnings("serial")
	public static JTextField buildJTextFieldWithTextBackground(int size,
			String label, final String initialText) {
		final JTextField field;
		final BufferedImage background;
		final JLabel backgroundLabel;

		backgroundLabel = new JLabel(label);
		backgroundLabel.setForeground(Color.LIGHT_GRAY);

		background = GUIOp.getScreenshot(backgroundLabel);

		field = new JTextField(initialText, size) {
			private boolean drawLabel = false;
			{
				if (initialText.isEmpty()) {
					drawLabel = true;
					repaint();
				}

				this.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent e) {
						drawLabel = false;
						repaint();
					}

					@Override
					public void focusLost(FocusEvent e) {
						if (getText().isEmpty()) {
							drawLabel = true;
							repaint();
						}
					}
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (this.drawLabel) {
					final int x;
					final int y;

					x = 5;
					y = (getHeight() - background.getHeight()) / 2;

					g.drawImage(background, x, y, this);
				}
			}
		};

		return field;
	}
}
