package scriptease.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.View;

import scriptease.gui.WidgetDecorator;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.util.GUIOp;
import scriptease.util.StringOp;
import sun.swing.SwingUtilities2;

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
	 * Creates a spacer, which is really just a transparent JPanel with a
	 * specified width and height. But space is like totally rad, man!
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public static JComponent buildSpacer(int width, int height) {
		final JPanel spacer = new JPanel();

		spacer.setOpaque(false);
		spacer.setMaximumSize(new Dimension(width, height));

		return spacer;
	}

	private static enum ButtonType {
		ADD, REMOVE, WRENCH, PENCIL, EDIT;
	}

	public static JButton buildRemoveButton() {
		return buildButton(ButtonType.REMOVE);
	}

	public static JButton buildAddButton() {
		return buildButton(ButtonType.ADD);
	}

	public static JButton buildEditButton() {
		return buildButton(ButtonType.EDIT);
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
				} else if (type == ButtonType.WRENCH
						|| type == ButtonType.PENCIL || type == ButtonType.EDIT) {
					armedFillColour = ScriptEaseUI.COLOUR_EDIT_BUTTON_PRESSED_FILL;
					armedLineColour = ScriptEaseUI.COLOUR_EDIT_BUTTON_PRESSED;
					hoverFillColour = ScriptEaseUI.COLOUR_EDIT_BUTTON_HOVER_FILL;
					unarmedLineColour = ScriptEaseUI.COLOUR_EDIT_BUTTON;
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
				final int diameter = SIZEXY * 5 / 6;
				final int radius = diameter / 2;
				final int centerX = circleX + radius;
				final int centerY = circleY + radius;

				// The offset between lines and the circle
				final int lineOffset = 4;

				final int horizLineX1 = circleX + lineOffset;
				final int horizLineX2 = circleX + diameter - lineOffset;
				final int horizLineY = centerY;

				final int vertiLineY1 = circleY + lineOffset;
				final int vertiLineY2 = circleY + diameter - lineOffset;
				final int vertiLineX = centerX;

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

				switch (type) {
				case WRENCH: {
					// Draw a rotated wrench

					final int arcWidth = 4;
					final int arcX = centerX - arcWidth / 2;
					final int arcDegrees = 90;

					final int lineY1 = centerY - 3;
					final int lineY2 = diameter - 1;

					final double rotationAmount = Math.toRadians(-45);

					final AffineTransform rotation = new AffineTransform();

					rotation.setToRotation(rotationAmount, centerX, centerY);

					g2d.transform(rotation);

					g2d.drawArc(arcX, 0, arcWidth, lineY1, 225, arcDegrees);
					g2d.drawLine(centerX, lineY1, centerX, lineY2);
					g2d.drawArc(arcX, lineY2, arcWidth, lineY1, 45, arcDegrees);
					break;
				}

				case PENCIL: {
					final double rotationAmount = Math.toRadians(-45);
					final AffineTransform rotation = new AffineTransform();

					// Remember: this is all rotated, so our coords will look
					// a little weird.
					rotation.setToRotation(rotationAmount, centerX, centerY);

					g2d.transform(rotation);

					final int squareTop = centerY - 3;
					final int squareBottom = diameter + 1;
					final int offset = 2;
					final int rightX = centerX + offset;
					final int leftX = centerX - offset;

					// Square part of pencil

					// Right Side of Pencil
					g2d.drawLine(rightX, squareTop, rightX, squareBottom);
					// Left Side of Pencil
					g2d.drawLine(leftX, squareTop, leftX, squareBottom);
					// Bottom of square part
					g2d.drawLine(leftX, squareBottom, rightX, squareBottom);
					// Top of square part
					g2d.drawLine(leftX, squareTop, rightX, squareTop);

					// Draw the tip
					final int tipY = squareTop - 5;
					g2d.drawLine(leftX, squareTop, centerX, tipY);
					g2d.drawLine(rightX, squareTop, centerX, tipY);

					final int eraserEnd = squareBottom - 2;
					// Draw the eraser
					g2d.drawLine(leftX, eraserEnd, rightX, eraserEnd);
					// Draw a line down the middle
					g2d.drawLine(centerX, squareTop, centerX, eraserEnd);
					break;
				}
				case EDIT:
					g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
					g2d.drawString("edit", centerX - 7, centerY + 4);
					break;
				case ADD:
					// Draw a vertical line, which when combined with the line
					// in Remove creates a plus sign
					g2d.drawLine(vertiLineX, vertiLineY1, vertiLineX,
							vertiLineY2);
				case REMOVE:
					// Draw a horizontal line (minus sign)
					g2d.drawLine(horizLineX1, horizLineY, horizLineX2,
							horizLineY);

				}

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
				final Color borderColor;

				if (this.drawLabel) {
					final int x;
					final int y;

					x = 5;
					y = (getHeight() - background.getHeight()) / 2;

					g.drawImage(background, x, y, this);
				}

				if (this.isEnabled()) {
					borderColor = ScriptEaseUI.SE_BLACK;
				} else {
					borderColor = Color.LIGHT_GRAY;
				}

				this.setBorder(BorderFactory.createLineBorder(borderColor, 1));

			}
		};

		return field;
	}

	private enum ButtonState {
		NEUTRAL, CLICK, HOVER, TOGGLED
	}

	private enum ButtonArrow {
		NONE, LEFT, RIGHT
	}

	public static JButton buildFlatButton(Action action) {
		return ComponentFactory.buildFlatButton(action, ScriptEaseUI.SE_BLACK);
	}

	public static JButton buildFlatButton(Color color) {
		return ComponentFactory.buildFlatButton(null, color);
	}

	public static JButton buildFlatButton(Color color, String text) {
		return ComponentFactory.buildFlatButton(null, text, color,
				ButtonArrow.NONE);
	}

	public static JButton buildFlatButton(Action action, Color color) {
		return ComponentFactory.buildFlatButton(action, null, color,
				ButtonArrow.NONE);
	}

	public static JButton buildLeftArrowButton(Color color) {
		return ComponentFactory.buildFlatButton(null, "", color,
				ButtonArrow.LEFT);
	}

	public static JButton buildRightArrowButton(Color color) {
		return ComponentFactory.buildFlatButton(null, "", color,
				ButtonArrow.RIGHT);
	}

	/**
	 * This is private because we should provide simpler methods to make stuff
	 * using the parameters in this.
	 * 
	 * @param action
	 * @param text
	 * @param color
	 * @return
	 */
	@SuppressWarnings("serial")
	private static JButton buildFlatButton(Action action, String text,
			final Color color, final ButtonArrow arrow) {
		final JButton button = new JButton() {
			private ButtonState state;
			private ButtonState previousState;

			{
				this.state = ButtonState.NEUTRAL;

				this.addMouseListener(new MouseAdapter() {
					public void mouseEntered(MouseEvent e) {
						if (state == ButtonState.NEUTRAL)
							changeState(ButtonState.HOVER);
					};

					public void mouseExited(MouseEvent e) {
						if (state == ButtonState.HOVER)
							changeState(ButtonState.NEUTRAL);
					};

					public void mousePressed(MouseEvent e) {
						changeState(ButtonState.CLICK);

					};

					public void mouseReleased(MouseEvent e) {
						if (previousState == ButtonState.HOVER)
							changeState(ButtonState.HOVER);
						else
							changeState(ButtonState.NEUTRAL);
					};
				});
			}

			private void changeState(ButtonState state) {
				this.previousState = this.state;
				this.state = state;
			}

			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2d = (Graphics2D) g;
				final Color fillColor;
				final int width;
				final int height;
				final int middleY;
				final int offset;
				final int arrowSize;

				if (this.isEnabled())
					switch (this.state) {
					case CLICK:
						fillColor = GUIOp.scaleWhite(color, 1.8);
						break;
					case HOVER:
						fillColor = GUIOp.scaleWhite(color, 1.6);
						break;
					default:
						fillColor = color;
						break;
					}
				else
					fillColor = Color.LIGHT_GRAY;

				width = getSize().width;
				height = getSize().height;

				middleY = height / 2;

				offset = width / 3;
				arrowSize = 6;

				g2d.setStroke(new BasicStroke(3));

				g2d.setColor(fillColor);
				g2d.fillRect(0, 0, width, height);

				if (arrow != ButtonArrow.NONE) {
					g2d.setColor(Color.WHITE);
					g2d.drawLine(offset, middleY, width - offset, middleY);

					final int[] xcoords;
					final int[] ycoords;

					if (arrow == ButtonArrow.LEFT) {
						xcoords = new int[] { offset - arrowSize, offset,
								offset };
						ycoords = new int[] { middleY, middleY - arrowSize,
								middleY + arrowSize };
					} else if (arrow == ButtonArrow.RIGHT) {
						xcoords = new int[] { width - offset + arrowSize,
								width - offset, width - offset };
						ycoords = new int[] { middleY, middleY - arrowSize,
								middleY + arrowSize };
					} else
						throw new IllegalArgumentException(
								"Undefined ButtonArrow type: " + arrow);

					g2d.fillPolygon(xcoords, ycoords, 3);
				}

				super.paintComponent(g);
			}
		};

		button.setFont(new Font("SansSerif", Font.PLAIN, 12));
		button.setForeground(Color.white);

		button.setContentAreaFilled(false);

		if (action != null)
			button.setAction(action);

		if (StringOp.exists(text))
			button.setText(text);

		return button;
	}

	public static JToggleButton buildFlatToggleButton(final Color color) {
		return ComponentFactory.buildFlatToggleButton(color, "");
	}

	@SuppressWarnings("serial")
	public static JToggleButton buildFlatToggleButton(final Color color,
			String text) {
		final JToggleButton button = new JToggleButton() {
			private ButtonState state;
			private ButtonState previousState;

			{
				this.state = ButtonState.NEUTRAL;

				this.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ev) {
						if (ev.getStateChange() == ItemEvent.SELECTED) {
							changeState(ButtonState.TOGGLED);
						} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
							changeState(ButtonState.NEUTRAL);

						}
					}
				});

				this.addMouseListener(new MouseAdapter() {
					public void mouseEntered(MouseEvent e) {
						if (state == ButtonState.NEUTRAL)
							changeState(ButtonState.HOVER);
					};

					public void mouseExited(MouseEvent e) {
						if (state == ButtonState.HOVER)
							changeState(previousState);
					};

					public void mousePressed(MouseEvent e) {
						changeState(ButtonState.CLICK);
					};

					public void mouseReleased(MouseEvent e) {
						if (state == ButtonState.CLICK) {
							if (isSelected())
								changeState(ButtonState.TOGGLED);
							else
								changeState(ButtonState.NEUTRAL);
						}
					};
				});
			}

			private void changeState(ButtonState state) {
				this.previousState = this.state;
				this.state = state;
			}

			@Override
			protected void paintComponent(Graphics g) {
				final Color fillColor;

				if (this.isEnabled())
					switch (this.state) {
					case CLICK:
						fillColor = GUIOp.scaleWhite(color, 1.8);
						break;
					case HOVER:
						fillColor = GUIOp.scaleWhite(color, 1.6);
						break;
					case TOGGLED:
						this.setForeground(color);
						fillColor = GUIOp.scaleWhite(color, 2.0);
						break;
					default:
						this.setForeground(Color.white);
						fillColor = color;
						break;
					}
				else
					fillColor = Color.LIGHT_GRAY;

				g.setColor(fillColor);
				g.fillRect(0, 0, getSize().width, getSize().height);

				super.paintComponent(g);
			}
		};

		button.setFont(new Font("SansSerif", Font.PLAIN, 12));

		if (button.isSelected())
			button.setForeground(color);
		else
			button.setForeground(Color.white);

		button.setContentAreaFilled(false);

		if (StringOp.exists(text))
			button.setText(text);

		return button;
	}

	/**
	 * Builds a neat and tidy UI for tabbed panes.
	 * 
	 * @return
	 */
	public static BasicTabbedPaneUI buildFlatTabUI() {
		return new BasicTabbedPaneUI() {
			@Override
			protected void paintTabBackground(Graphics g, int tabPlacement,
					int tabIndex, int x, int y, int w, int h, boolean isSelected) {
				if (isSelected) {
					g.setColor(ScriptEaseUI.PRIMARY_UI);
				} else {
					g.setColor(ScriptEaseUI.TERTIARY_UI);
				}

				g.fillRect(x, y, w, h + 2);
			}

			@Override
			protected void paintText(Graphics g, int tabPlacement, Font font,
					FontMetrics metrics, int tabIndex, String title,
					Rectangle textRect, boolean isSelected) {
				// Had to modify this from the default code to paint selected
				// tab text black and unselected white.

				final Color selectedText = ScriptEaseUI.SECONDARY_UI;
				final Color unselectedText = ScriptEaseUI.PRIMARY_UI;
				g.setFont(font);

				final View v = getTextViewForTab(tabIndex);
				if (v != null) {
					// html
					v.paint(g, textRect);
				} else {
					// plain text
					int mnemIndex = tabPane
							.getDisplayedMnemonicIndexAt(tabIndex);

					if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
						final Color fg;

						if (isSelected) {
							fg = selectedText;
						} else {
							fg = unselectedText;
						}

						g.setColor(fg);
						SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
								title, mnemIndex, textRect.x, textRect.y
										+ metrics.getAscent());

					} else { // tab disabled
						g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
						SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
								title, mnemIndex, textRect.x, textRect.y
										+ metrics.getAscent());
						g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
						SwingUtilities2.drawStringUnderlineCharAt(tabPane, g,
								title, mnemIndex, textRect.x - 1, textRect.y
										+ metrics.getAscent() - 1);

					}
				}
			}

			@Override
			protected void paintContentBorderTopEdge(Graphics g,
					int tabPlacement, int selectedIndex, int x, int y, int w,
					int h) {
			}

			@Override
			protected void paintContentBorderRightEdge(Graphics g,
					int tabPlacement, int selectedIndex, int x, int y, int w,
					int h) {
			}

			protected void paintContentBorderLeftEdge(Graphics g,
					int tabPlacement, int selectedIndex, int x, int y, int w,
					int h) {
			};

			@Override
			protected void paintContentBorderBottomEdge(Graphics g,
					int tabPlacement, int selectedIndex, int x, int y, int w,
					int h) {
			}

			@Override
			protected void paintTabBorder(Graphics g, int tabPlacement,
					int tabIndex, int x, int y, int w, int h, boolean isSelected) {
				final int height = h * 2;

				g.setColor(ScriptEaseUI.SE_BLACK);

				g.drawLine(x, y, x + w, y);
				g.drawLine(x, y, x, y + height);
				g.drawLine(x + w, y, x + w, y + height);
			}
		};
	}

	@SuppressWarnings("serial")
	public static JTextField buildNumberTextField() {
		return new JTextField() {

			@Override
			protected Document createDefaultModel() {
				return new PlainDocument() {

					@Override
					public void insertString(int offs, String str,
							AttributeSet a) throws BadLocationException {
						if (str == null)
							return;
						else if (str.matches("\\d+"))
							super.insertString(offs, str, a);
					}
				};
			}
		};
	}

	/**
	 * Creates A JButton that when clicked will open your default browser and
	 * navigate to the specified link.
	 * 
	 * @param link
	 *            The url that the button will link to.
	 * @param button
	 *            TextThe text that will appear on the button.
	 * @return A JButton that when clicked will open your default browser and
	 *         navigate to the specified link.
	 */
	public static JButton buildLinkButton(final String link, String buttonText) {
		final JButton button = new JButton();
		button.setText(buttonText);
		button.setForeground(Color.BLUE);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setBorderPainted(false);
		button.setBorder(null);
		button.setOpaque(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setToolTipText(link);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(link));
				} catch (IOException ex) {
					System.err.println("Could not launch default browser.");
				} catch (URISyntaxException ex) {
					System.err.println(link
							+ " URI did not have expected syntax.");
				}
			}
		});

		return button;
	}
}
