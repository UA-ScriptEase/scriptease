package scriptease.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

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

	public static void paintArrow(Graphics g, List<Point> points) {
		// Create a new graphics context
		Graphics2D g2 = (Graphics2D) g.create();
		// Antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		for (Point start : points) {
			final int index = points.indexOf(start);

			final double x1 = start.getX();
			final double y1 = start.getY();

			if (index < (points.size() - 1)) {
				final Point end = points.get(index + 1);

				final double x2 = end.getX();
				final double y2 = end.getY();

				float arrowWidth = 4.0f;
				float theta = 0.423f;
				int[] xPoints = new int[3];
				int[] yPoints = new int[3];
				float[] vecLine = new float[2];
				float[] vecLeft = new float[2];
				float fLength;
				float th;
				float ta;
				float baseX, baseY;

				xPoints[0] = (int) x2;
				yPoints[0] = (int) y2;

				// build the line vector
				vecLine[0] = (float) (xPoints[0] - x1);
				vecLine[1] = (float) (yPoints[0] - y1);

				// build the arrow base vector - normal to the line
				vecLeft[0] = -vecLine[1];
				vecLeft[1] = vecLine[0];

				// setup length parameters
				fLength = (float) Math.sqrt(vecLine[0] * vecLine[0]
						+ vecLine[1] * vecLine[1]);
				th = arrowWidth / (1.5f * fLength);
				ta = arrowWidth
						/ (1.5f * ((float) Math.tan(theta) / 1.5f) * fLength);

				// find the base of the arrow
				baseX = (xPoints[0] - ta * vecLine[0]);
				baseY = (yPoints[0] - ta * vecLine[1]);

				// build the points on the sides of the arrow
				xPoints[1] = (int) (baseX + th * vecLeft[0]);
				yPoints[1] = (int) (baseY + th * vecLeft[1]);
				xPoints[2] = (int) (baseX - th * vecLeft[0]);
				yPoints[2] = (int) (baseY - th * vecLeft[1]);

				g2.drawLine((int) x1, (int) y1, (int) baseX, (int) baseY);

				// Last point in list. Draw the arrowhead.
				if (index == (points.size() - 2))
					g2.fillPolygon(xPoints, yPoints, 3);
			}
		}
		g2.dispose();
	}

	/**
	 * Gets the middle right point of the GraphNode's component
	 * 
	 * @return
	 */
	public static Point getMidRight(JComponent component) {
		Point point = new Point();
		if (component != null) {
			point.setLocation((int) (component.getX() + component
					.getPreferredSize().getWidth()),
					(int) (component.getY() + component.getPreferredSize()
							.getHeight() / 2));
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

		// Set it to it's preferred size. (optional)
		c.setSize(c.getPreferredSize());
		layoutComponent(c);

		BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		CellRendererPane crp = new CellRendererPane();
		crp.add(c);
		crp.paintComponent(img.createGraphics(), c, crp, c.getBounds());
		return img;
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

		final int height;
		final int stringWidth;

		final Dimension newSize;
		final int xDifference;
		final int yDifference;

		oldSize = field.getSize();
		metrics = field.getFontMetrics(field.getFont());

		height = metrics.getHeight();
		stringWidth = metrics.stringWidth(field.getText());

		newSize = new Dimension(stringWidth + 26, height + 6);
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
}
