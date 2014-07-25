package scriptease.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;

import scriptease.gui.WindowFactory;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.ui.ScriptEaseUI;

/**
 * Collection of simple GUI-related routines that don't really belong anywhere.
 * These exist to facilitate common GUI operations.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public class GUIOp {
	public static int MAX_COLOUR_VALUE = 255;
	public static int MIN_COLOUR_VALUE = 0;
	public static Border emptyBorder = BorderFactory.createEmptyBorder();
	public static Border whiteBorder = BorderFactory
			.createLineBorder(Color.WHITE);

	/**
	 * Scales the given white value (but not alpha) by <code>factor</code>,
	 * which is normalized to 1.0. So, for example, a <code>factor</code> of 0.5
	 * will return a colour that is half as white as <code>source</code> on all
	 * three colour axes, while a <code>factor</code> of 2.0 will return a
	 * colour that is twice as white.
	 * 
	 * @param source
	 *            The original colour to base the new colour from.
	 * @param factor
	 *            The factor to differ by.
	 * @returns A new colour that is different in whiteness from the source
	 *          colour by the given factor.
	 */
	public static Color scaleWhite(Color source, double factor) {
		double average = (((double) (source.getRed() + source.getBlue() + source
				.getGreen())) / (double) (3 * GUIOp.MAX_COLOUR_VALUE))
				* GUIOp.MAX_COLOUR_VALUE;
		double amount = average * (factor - 1.0);
		int red = (int) (source.getRed() + amount);
		int green = (int) (source.getGreen() + amount);
		int blue = (int) (source.getBlue() + amount);

		// keep it within the extreme values allowed by colours.
		red = Math.min(red, GUIOp.MAX_COLOUR_VALUE);
		green = Math.min(green, GUIOp.MAX_COLOUR_VALUE);
		blue = Math.min(blue, GUIOp.MAX_COLOUR_VALUE);

		red = Math.max(red, GUIOp.MIN_COLOUR_VALUE);
		green = Math.max(green, GUIOp.MIN_COLOUR_VALUE);
		blue = Math.max(blue, GUIOp.MIN_COLOUR_VALUE);

		return new Color(red, green, blue);
	}

	/**
	 * Scales the given colour's RGB values (but not alpha) by
	 * <code>factor</code>, which is normalized to 1.0. So, for example, a
	 * <code>factor</code> of 0.5 will return a colour that is half as bright as
	 * <code>source</code> on all three colour axes, while a <code>factor</code>
	 * of 2.0 will return a colour that is twice as bright. <br>
	 * <br>
	 * If the colour contains a value that is 0, then the colour is increased to
	 * 1 before the factor is applied.<br>
	 * <br>
	 * The standard colour changing methods {@link Color.brighter()} and {@link
	 * Color.darker()} are essentially equivalent to using a factor of 1.4 and
	 * 0.7, respectively.
	 * 
	 * @param source
	 *            The original colour to base the new colour from.
	 * @param factor
	 *            The factor to differ by.
	 * @returns A new colour that is different on all three axes from the source
	 *          colour by the given factor.
	 */
	public static Color scaleColour(Color source, double factor) {
		int red = Math.max(source.getRed(), 1);
		int green = Math.max(source.getGreen(), 1);
		int blue = Math.max(source.getBlue(), 1);

		red = (int) (red * factor);
		green = (int) (green * factor);
		blue = (int) (blue * factor);

		// keep it within the extreme values allowed by colours.
		red = Math.min(red, GUIOp.MAX_COLOUR_VALUE);
		green = Math.min(green, GUIOp.MAX_COLOUR_VALUE);
		blue = Math.min(blue, GUIOp.MAX_COLOUR_VALUE);

		red = Math.max(red, GUIOp.MIN_COLOUR_VALUE);
		green = Math.max(green, GUIOp.MIN_COLOUR_VALUE);
		blue = Math.max(blue, GUIOp.MIN_COLOUR_VALUE);

		return new Color(red, green, blue);
	}

	/**
	 * Combines the two given colours with the given alpha value.
	 * 
	 * @param c1
	 * @param c2
	 * @param alpha
	 * @return
	 */
	public static Color combine(Color c1, Color c2, double alpha) {
		int red = (int) (alpha * c1.getRed() + (1 - alpha) * c2.getRed());
		int green = (int) (alpha * c1.getGreen() + (1 - alpha) * c2.getGreen());
		int blue = (int) (alpha * c1.getBlue() + (1 - alpha) * c2.getBlue());
		return new Color(red, green, blue);
	}

	/**
	 * Spawns a thread to fades the colour of the given component from c1 to c2
	 * 
	 * @param c1
	 *            the current colour of the component
	 * @param c2
	 *            the desired colour of the component
	 */
	public static void fadeBackground(final JComponent component,
			final Color c1, final Color c2) {
		SwingWorker<Void, Void> fader = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				final int frames = 100;
				for (int n = 0; n <= frames; n++) {
					double alpha = 1.0 * n / frames;
					component.setBackground(GUIOp.combine(c2, c1, alpha));
					component.repaint();
				}
				return null;
			}
		};
		fader.execute();
	}

	/**
	 * Paint an arrow from start to end.
	 * 
	 * @param g
	 * @param start
	 * @param end
	 * @param curveFactor
	 *            If this is 0, the arrow won't curve.
	 */
	public static void paintArrow(Graphics g, Point start, Point end,
			int curveFactor) {
		final Graphics2D g2 = (Graphics2D) g.create();

		final float arrowWidth = 8.0f;
		final float theta = 0.423f;

		final float startX = (float) start.getX();
		final float startY = (float) start.getY();

		final float endX = (float) end.getX();
		final float endY = (float) end.getY();

		final float lineX;
		final float lineY;

		final float fLength;
		final float thickness;
		final float length;
		final float baseX;
		final float baseY;

		final int baseLeftX, baseLeftY, baseRightX, baseRightY;

		// build the line vector
		lineX = endX - startX;
		lineY = endY - startY;

		// setup length parameters
		fLength = (float) Math.sqrt(lineX * lineX + lineY * lineY);
		thickness = arrowWidth / (1.5f * fLength);
		length = arrowWidth / (((float) Math.tan(theta)) * fLength);

		// find the base of the arrow
		baseX = (float) (endX - length * lineX);
		baseY = (float) (endY - length * lineY);

		// The base of the arrow's coordinates
		baseLeftX = (int) (baseX + thickness * -lineY);
		baseLeftY = (int) (baseY + thickness * lineX);
		baseRightX = (int) (baseX - thickness * -lineY);
		baseRightY = (int) (baseY - thickness * lineX);

		// Antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (curveFactor > 0)
			g2.draw(new QuadCurve2D.Float(start.x, start.y,
					(end.x + start.x) / 2, start.y + curveFactor * 50, end.x
							- arrowWidth, end.y));
		else
			g2.drawLine((int) startX, (int) startY, (int) baseX, (int) baseY);

		g2.fillPolygon(new int[] { (int) endX, baseLeftX, baseRightX },
				new int[] { (int) endY, baseLeftY, baseRightY }, 3);

		g2.dispose();
	}

	/**
	 * Gets the middle right point of the GraphNode's component
	 * 
	 * @return
	 */
	public static Point getMidRight(JComponent component) {
		final Dimension componentSize = component.getPreferredSize();

		Point point = new Point();
		if (component != null) {
			point.setLocation(
					(int) (component.getX() + componentSize.getWidth()),
					(int) (component.getY() + componentSize.getHeight() / 2));
		}
		return point;
	}

	/**
	 * Gets the middle left point of the GraphNode's component
	 * 
	 * @return
	 */
	public static Point getMidLeft(JComponent component) {
		Point point = new Point();
		if (component != null) {
			point.setLocation(component.getX(),
					component.getY() + component.getHeight() / 2);
		}
		return point;
	}

	/**
	 * Returns a BufferedImage representing the component.
	 * 
	 * @author aioobe from <a href=http://stackoverflow.com/a/4154510>Stack
	 *         Overflow</a>
	 * 
	 * 
	 * @param component
	 * @return
	 */
	public static BufferedImage getScreenshot(Component c) {
		final Container original = c.getParent();

		// Set it to it's preferred size. (optional)
		c.setSize(c.getPreferredSize());
		layoutComponent(c);

		BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		CellRendererPane crp = new CellRendererPane();
		crp.add(c);
		crp.paintComponent(img.createGraphics(), c, crp, c.getBounds());

		if (original != null) {
			original.add(c);
			original.repaint();
		}
		return img;
	}

	/**
	 * Saves a screenshot as a .png file to the passed in path. The path must
	 * end with .png, or else an exception will be thrown. If you want to print
	 * to the desktop, the code for that is:
	 * <code>System.getProperty("user.home")
				+ "/Desktop/image.png</code>
	 * 
	 * @deprecated This doesn't really work.
	 * @param component
	 *            The component to draw to the path.
	 * @param pathThe
	 *            path must end with .png, or else an exception will be thrown.
	 * 
	 */
	@Deprecated
	public static File saveScreenshot(final Component component, String path) {
		final String png = "png";
		final BufferedImage image = GUIOp.getScreenshot(component);

		if (!path.endsWith(png)) {
			throw new IllegalArgumentException("Path must end in ." + png);
		}

		final File outputFile = new File(path);

		try {
			ImageIO.write(image, png, outputFile);
		} catch (IOException e) {
			WindowFactory.getInstance().showExceptionDialog(
					"Could Not Save Image",
					"The image wasn't saved.",
					"Something went wrong when saving a screenshot to " + path
							+ ".", UIManager.getIcon("OptionPane.warningIcon"),
					e);
		}

		return outputFile;
	}

	// from the example of user489041
	private static void layoutComponent(Component c) {
		synchronized (c.getTreeLock()) {
			c.doLayout();
			if (c instanceof Container)
				for (Component child : ((Container) c).getComponents())
					layoutComponent(child);
		}
	}

	/**
	 * Resizes the passed in JTextField to match the length of the text.
	 * 
	 * @param field
	 */
	public static void resizeJTextField(JTextField field) {
		final Dimension oldSize;
		final FontMetrics metrics;
		final String text;

		final int height;
		final int stringWidth;

		final Dimension newSize;
		final int xDifference;
		final int yDifference;

		oldSize = field.getSize();
		metrics = field.getFontMetrics(field.getFont());
		text = field.getText();

		height = metrics.getHeight();

		stringWidth = metrics.stringWidth(text);

		if (stringWidth > 24) {
			newSize = new Dimension(stringWidth + 26, height + 6);
		} else {
			newSize = new Dimension(50, height + 6);
		}
		xDifference = newSize.width - oldSize.width;
		yDifference = newSize.height - oldSize.height;

		// resize
		field.setSize(newSize);
		field.setPreferredSize(newSize);

		// Get the difference
		// Resize the next 5 levels above us. Hardcoded to work specifically for
		// use in StoryComponentPanels.
		Container parent = field.getParent();

		while (parent != null && parent instanceof JPanel) {
			final Dimension oldParentSize = parent.getSize();
			// Calculate the new parent size
			Dimension newParentSize = new Dimension(oldParentSize.width
					+ xDifference, oldParentSize.height + yDifference);
			// resize
			parent.setSize(newParentSize);
			parent.doLayout();
			parent = parent.getParent();
		}
	}

	/**
	 * Creates a cursor based on a name with a hotspot of 0, 0. Returns null if
	 * the file is not found. We recommend using one of the constant cursors
	 * found in {@link ScriptEaseUI} instead of this method, since all cursors
	 * should be created in there anyways.
	 * 
	 * @param name
	 *            Part of the path to a .png file represting the cursor:
	 *            "scriptease/resources/icons/cursors/[name].png"
	 * @return
	 */
	public static Cursor createCursor(String name) {
		return GUIOp.createCursor(name, new Point(0, 0));
	}

	/**
	 * Adds all components passed in to the passed in parent in the order
	 * supplied.
	 * 
	 * @param parent
	 * @param components
	 */
	public static void addComponents(Container parent, Component... components) {
		for (Component component : components) {
			parent.add(component);
		}
	}

	/**
	 * Creates a cursor based on a name. Returns null if the file is not found.
	 * We recommend using one of the constant cursors found in
	 * {@link ScriptEaseUI} instead of this method, since all cursors should be
	 * created in there anyways.
	 * 
	 * @param name
	 *            Part of the path to a .png file represting the cursor:
	 *            "scriptease/resources/icons/cursors/[name].png"
	 * @param hotspot
	 *            The hotspot for the cursor.
	 * @return
	 */
	public static Cursor createCursor(String name, Point hotspot) {
		final File file;
		final String resultingCursorPath;

		Cursor customCursor = null;

		if (name != null) {
			resultingCursorPath = "scriptease/resources/icons/cursors/" + name
					+ ".png";
			file = FileOp.getFileResource(resultingCursorPath);

			if (file != null) {
				try {
					final BufferedImage cursorImage;
					final Toolkit toolkit;

					cursorImage = ImageIO.read(file);

					toolkit = Toolkit.getDefaultToolkit();

					customCursor = toolkit.createCustomCursor(cursorImage,
							hotspot, resultingCursorPath);
				} catch (IOException e) {
					System.err.println("Failed to read cursor file at " + file
							+ ". Setting cursor to default.");
				}
			}
		}

		return customCursor;
	}

	/**
	 * Scrolls the passed in JScrollPane by current mouse position. Usually used
	 * in combination with a mouse listener listening for drag events.
	 * 
	 * @param pane
	 */
	public static void scrollJScrollPaneToMousePosition(JScrollPane pane) {
		final int SCROLL_RECT_DEPTH = 20;

		final JScrollBar verticalScrollBar;
		final JScrollBar horizontalScrollBar;
		final Point mousePosition;

		final int verticalScrollBarValue;
		final int horizontalScrollBarValue;

		final Rectangle viewPort;
		final Rectangle topScrollRectangle;
		final Rectangle bottomScrollRectangle;
		final Rectangle leftScrollRectangle;
		final Rectangle rightScrollRectangle;

		verticalScrollBar = pane.getVerticalScrollBar();
		horizontalScrollBar = pane.getHorizontalScrollBar();
		mousePosition = pane.getMousePosition();

		if (mousePosition == null)
			return;

		verticalScrollBarValue = verticalScrollBar.getValue();
		horizontalScrollBarValue = horizontalScrollBar.getValue();

		viewPort = pane.getViewportBorderBounds();
		topScrollRectangle = new Rectangle(0, 0, viewPort.width,
				SCROLL_RECT_DEPTH);
		bottomScrollRectangle = new Rectangle(0, viewPort.height
				- SCROLL_RECT_DEPTH, viewPort.width, SCROLL_RECT_DEPTH);
		leftScrollRectangle = new Rectangle(0, 0, SCROLL_RECT_DEPTH,
				viewPort.height);
		rightScrollRectangle = new Rectangle(
				viewPort.width - SCROLL_RECT_DEPTH, 0, SCROLL_RECT_DEPTH,
				viewPort.height);

		if (topScrollRectangle.contains(mousePosition)) {
			verticalScrollBar.setValue(verticalScrollBarValue
					- ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		} else if (bottomScrollRectangle.contains(mousePosition)) {
			verticalScrollBar.setValue(verticalScrollBarValue
					+ ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		}

		if (leftScrollRectangle.contains(mousePosition)) {
			horizontalScrollBar.setValue(horizontalScrollBarValue
					- ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		} else if (rightScrollRectangle.contains(mousePosition)) {
			horizontalScrollBar.setValue(horizontalScrollBarValue
					+ ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		}
	}

	/**
	 * Get all components inside of a container, including components in
	 * containers within that component and so on.
	 * 
	 * @param container
	 * @return
	 */
	public static Collection<Component> getContainerComponents(
			Container container) {
		final Collection<Component> allComponents = new ArrayList<Component>();
		final Component[] components = container.getComponents();

		for (Component component : components) {
			allComponents.add(component);
			if (component instanceof Container)
				allComponents.addAll(GUIOp
						.getContainerComponents((Container) component));
		}

		return allComponents;
	}

	/**
	 * Returns the index of the component in its parent. If the component or its
	 * parent are not found, this will return -1.
	 * 
	 * @param component
	 * @return
	 */
	public static int getComponentIndex(Component component) {
		final Container parent = component.getParent();
		if (component != null && parent != null) {
			for (int i = 0; i < parent.getComponentCount(); i++) {
				if (parent.getComponent(i) == component)
					return i;
			}
		}

		return -1;
	}

	/**
	 * Creates a new JPanel that has a gradient as a background. The gradient
	 * colour comes from the component's background colour.
	 * 
	 * @param factor
	 *            The factor by which the bottom colour should be scaled to
	 *            white.
	 * @return
	 * @deprecated Gradients are ugly. We don't use this method anywhere, but
	 *             it's code is an example of custom graphics, and if we ever
	 *             need a gradient, this is how to do it. Painting many of these
	 *             continuously (e.g. in the graph) is very slow, so use this at
	 *             your own risk.
	 */
	@SuppressWarnings("serial")
	public static JPanel buildGradientPanel(final double factor) {
		final JPanel gradientPanel;

		gradientPanel = new JPanel() {

			@Override
			protected void paintComponent(Graphics grphcs) {
				final Color topColour;
				final Color bottomColour;

				final Graphics2D g2d;
				final GradientPaint gp;

				topColour = this.getBackground();
				bottomColour = GUIOp.scaleWhite(topColour, factor);

				g2d = (Graphics2D) grphcs;
				gp = new GradientPaint(0, 0, topColour, 0, this.getHeight(),
						bottomColour);

				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.setPaint(gp);

				g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
				super.paintComponent(grphcs);

			}
		};

		gradientPanel.setOpaque(false);

		return gradientPanel;
	}

	/**
	 * Takes in a JComponent and resizes it's font using the same font style and
	 * family as before.
	 * 
	 * @param newFontSize
	 * @param component
	 */
	public static void resizeFont(int newFontSize, JComponent component) {
		component.setFont(new Font(component.getFont().getFamily(), component
				.getFont().getStyle(), newFontSize));
	}

	/**
	 * Returns true if a given border has a null, empty, or white border
	 * 
	 * @param panel
	 * @return
	 */
	public static boolean isPanelBorderEmpty(StoryComponentPanel panel) {
		Border border = panel.getBorder();
		if (border == null || border.equals(emptyBorder)
				|| border.equals(whiteBorder)) {
			return true;
		} else {
			return false;
		}
	}
}
