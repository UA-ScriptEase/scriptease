package scriptease.gui.ui.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.border.AbstractBorder;

/**
 * Border class that draws a gradient or simple colour around the contour shape
 * that it is a border for.<br>
 * <Br>
 * <b>Warning: </b> This class relies on Swing's Graphics2D to paint.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public class GradientLineBorder extends AbstractBorder {
	protected Stroke stroke;
	protected Paint gradient;
	protected Shape outline;
	protected int thickness;

	/**
	 * Builds a new GradientBorder the will trace the given shape with a line
	 * painted using a default colour at the given thickness.
	 * 
	 * @param thickness
	 *            The thickness of the line to draw.
	 */
	public GradientLineBorder(int thickness) {
		this(Color.BLACK, thickness);
	}

	/**
	 * Builds a new GradientBorder the will trace a rectangle with a line
	 * painted using the given paint at the given thickness.
	 * 
	 * @param gradient
	 *            The colour or gradient to use in drawing
	 * @param thickness
	 *            The thickness of the line to draw.
	 */
	public GradientLineBorder(Paint gradient, int thickness) {
		this(gradient, new Rectangle(), thickness);
	}

	/**
	 * Builds a new GradientBorder the will trace the given shape with a line
	 * painted using the given gradient at the given thickness.
	 * 
	 * @param gradient
	 *            The colour or gradient to use in drawing
	 * @param outline
	 *            The shape to follow while drawing.
	 * @param thickness
	 *            The thickness of the line to draw.
	 */
	public GradientLineBorder(Paint gradient, Shape outline, int thickness) {
		this(gradient, outline, new BasicStroke(thickness), thickness);
	}

	/**
	 * Builds a new GradientBorder the will trace the given shape with a line
	 * painted using the given gradient with the given stroke.
	 * 
	 * @param gradient
	 *            The colour or gradient to use in drawing
	 * @param outline
	 *            The shape to follow while drawing.
	 * @param stroke
	 *            The stroke of the line to draw.
	 * @param thickness
	 *            The thickness of the border to be used for layout purposes.
	 */
	public GradientLineBorder(Paint gradient, Shape outline, Stroke stroke,
			int thickness) {
		this.stroke = stroke;
		this.gradient = gradient;
		this.outline = outline;
		this.thickness = thickness;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		final Graphics2D g2d = (Graphics2D) g;
		final Stroke oldStroke = g2d.getStroke();
		final Paint oldPaint = g2d.getPaint();
		final Object oldHint = g2d
				.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		g2d.setStroke(this.stroke);
		g2d.setPaint(this.gradient);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.draw(this.outline);

		// reset values
		g2d.setStroke(oldStroke);
		g2d.setPaint(oldPaint);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
	}

	/**
	 * Returns the insets of the border.
	 * 
	 * @param comp
	 *            the component for which this border insets value applies
	 */
	@Override
	public Insets getBorderInsets(Component comp) {
		return new Insets(this.thickness, this.thickness, this.thickness,
				this.thickness);
	}

	/**
	 * Resets the insets parameter with this Border's thickness.
	 */
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.top = insets.right = insets.bottom = this.thickness;
		return insets;
	}

	/**
	 * Returns the colour of the border.
	 * 
	 * @return the line colour
	 */
	public Paint getGradient() {
		return this.gradient;
	}

	/**
	 * Sets the paint that the border will draw with.
	 * 
	 * @param gradient
	 */
	public void setGradient(Paint gradient) {
		this.gradient = gradient;
	}

	/**
	 * Returns the stroke of the border.
	 * 
	 * @return the line stroke
	 */
	public Stroke getStroke() {
		return this.stroke;
	}

	/**
	 * sets the stroke of the border.
	 * 
	 * @param stroke
	 *            the new stroke to use
	 */
	public void setThickness(Stroke stroke) {
		this.stroke = stroke;
	}

	/**
	 * Returns the outline shape of the border.
	 * 
	 * @return the outline shape
	 */
	public Shape getOutline() {
		return this.outline;
	}

	/**
	 * sets the shape to trace for the border.
	 * 
	 * @param outline
	 *            the new outline to follow
	 */
	public void setOutline(Shape outline) {
		this.outline = outline;
	}
}
