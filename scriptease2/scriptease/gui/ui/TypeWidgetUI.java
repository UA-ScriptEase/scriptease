package scriptease.gui.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import scriptease.gui.ui.border.GradientLineBorder;
import scriptease.util.GUIOp;

/**
 * Look and Feel Strategy for Toggle buttons that are displaying type
 * information in the ScriptEase Pattern Constructor GUI. <br>
 * <br>
 * Types are drawn as circles with a type symbol in the centre. The Type Symbol
 * is either a translator-defined icon, or the first letter of the type name if
 * no icon is defined or exists. The type circle is filled in with a gradient to
 * make it look convex or concave based on the pressed state of the button. The
 * colour of the gradient is based on the background colour set on the button.<br>
 * <br>
 * A border is automatically applied to the Type Button so that it can be
 * distinguished as being raised from the background.
 * 
 * @author remiller
 */
public class TypeWidgetUI extends BasicToggleButtonUI implements ScriptEaseUI {
	private static final int LINE_THICKNESS = 1;

	private static final GradientLineBorder BORDER = new GradientLineBorder(
			LINE_THICKNESS);

	private static final TypeWidgetUI instance = new TypeWidgetUI();

	public static TypeWidgetUI getInstance() {
		return instance;
	}

	@Override
	public void installUI(JComponent comp) {
		super.installUI(comp);

		AbstractButton button = (AbstractButton) comp;

		this.installBorder(button);
		button.setOpaque(false);
	}

	private void installBorder(JComponent comp) {
		final AbstractButton button = (AbstractButton) comp;

		button.setBorder(BORDER);
	}

	@Override
	public void uninstallUI(JComponent button) {
		super.uninstallUI(button);
		button.setOpaque(true);
	}

	@Override
	public Dimension getPreferredSize(JComponent comp) {
		final Dimension size = super.getPreferredSize(comp);

		if (size.getWidth() < size.getHeight()) {
			size.setSize(size.getHeight(), size.getHeight());
		}

		return size;
	}

	@Override
	public void paint(Graphics g, JComponent comp) {
		final AbstractButton button = (AbstractButton) comp;
		final boolean isUp = this.isUp(button);
		final Graphics2D tempGraphics;
		final Shape typeShape;
		final int symbolXLoc;
		final int symbolYLoc;
		final GradientLineBorder border = (GradientLineBorder) button
				.getBorder();

		border.setGradient(determineLinePaint(button, isUp(button)));
		border.setOutline(constructTypeShape(button));

		// clone the other graphics so that we don't disturb its settings.
		tempGraphics = (g == null) ? null : (Graphics2D) g.create();

		// Type widgets are elliptical.
		typeShape = this.constructTypeShape(button);

		// the fill gradient
		this.paintFill(tempGraphics, button, typeShape, isUp);

		Icon typeIcon = (button.getIcon());

		// Draw the type symbol, which is either an icon, or text
		if (typeIcon != null) {
			// Anti aliasing is off by default, which could be important for
			// icons to draw pretty
			symbolXLoc = (button.getWidth() - typeIcon.getIconWidth()) / 2;
			symbolYLoc = (button.getHeight() - typeIcon.getIconHeight()) / 2;

			typeIcon.paintIcon(button, tempGraphics, symbolXLoc, symbolYLoc);
		} else {
			this.paintTypeTextIcon(button, tempGraphics, isUp);
		}

		tempGraphics.dispose();
	}

	private boolean isUp(AbstractButton button) {
		final ButtonModel model = button.getModel();

		return !(model.isArmed() && model.isPressed()) && !model.isSelected();
	}

	private void paintTypeTextIcon(final AbstractButton button,
			final Graphics2D g, boolean isUp) {
		final int symbolXLoc;
		final int symbolYLoc;
		final Color textColour;
		final FontMetrics fontMetrics;

		fontMetrics = button.getFontMetrics(button.getFont());
		symbolXLoc = (button.getWidth() - fontMetrics.stringWidth(button
				.getText())) / 2;
		symbolYLoc = (button.getHeight()
				+ (int) (fontMetrics.getLineMetrics(button.getText(), g)
						.getAscent()) - 2) / 2;

		Color foreground = button.getForeground();
		if (isUp) {
			textColour = foreground;
		} else {
			// dim it just a bit.
			textColour = GUIOp.scaleColour(foreground, 0.9);
		}

		// turn off AA so that the text is clear
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setColor(textColour);
		g.setFont(button.getFont());
		g.drawString(button.getText(), symbolXLoc, symbolYLoc);
	}

	private Shape constructTypeShape(JComponent button) {
		int width = button.getWidth();
		int height = button.getHeight();

		// square up type labels so that we get a a circle, but only if it's too
		// short, not when it's too long.
		width = width < height ? height : width;

		return new Ellipse2D.Float(LINE_THICKNESS / 2, LINE_THICKNESS / 2,
				width - LINE_THICKNESS, height - LINE_THICKNESS);
	}

	// Configure a Gradient to fill the shape with and paint it
	private void paintFill(Graphics2D g, JComponent button, Shape typeShape,
			boolean isUp) {
		final Color base = button.getBackground();
		final Color specularHue;
		final Color diffuseHue;
		// this is the centre of the gradient circle that we are drawing,
		// not the shape
		final Point2D centre;
		final int radius;
		final Paint paint;

		// Convex gradient.
		if (isUp) {
			specularHue = GUIOp.scaleColour(base, 1.25);
			diffuseHue = GUIOp.scaleColour(base, 0.85);

			centre = new Point(button.getWidth() * 50 / 100,
					button.getHeight() * 25 / 100);

			float[] startDistances = { 0.0f, 0.8f };
			Color[] gradient = { specularHue, diffuseHue };

			radius = button.getWidth() * 60 / 100;

			paint = new RadialGradientPaint(centre, radius, startDistances,
					gradient);
		}
		// Concave gradient.
		else {
			specularHue = GUIOp.scaleColour(base, 0.75);
			diffuseHue = GUIOp.scaleColour(base, 0.35);

			centre = new Point(button.getWidth() * 50 / 100,
					button.getHeight() * 15 / 100);

			float[] startDistances = { 0.0f, 0.8f };
			Color[] gradient = { diffuseHue, specularHue };

			radius = button.getWidth() * 90 / 100;

			paint = new RadialGradientPaint(centre, radius, startDistances,
					gradient);
		}

		g.setPaint(paint);
		g.fill(typeShape);
	}

	private Paint determineLinePaint(JComponent button, boolean isUp) {
		final Color baseHue = button.getBackground();
		final Color specularHue;
		final Color diffuseHue;
		final Color shadowHue;
		final int height = button.getHeight();
		final Point start = new Point(0, 0);
		final Point end = new Point(0, height == 0 ? 1 : height);
		// I hate arrays. - remiller
		final float[] startDistances = new float[3];
		final Color[] gradient = new Color[3];

		// We always want it to look like light is coming from the top to
		// suggest a natural light source. It's an art/psychology thing.
		// - remiller
		if (isUp) {
			specularHue = GUIOp.scaleColour(baseHue, 1.30);
			diffuseHue = baseHue;
			shadowHue = GUIOp.scaleColour(baseHue, 0.3);

			gradient[0] = specularHue;
			gradient[1] = diffuseHue;
			gradient[2] = shadowHue;

			startDistances[0] = 0.0f;
			startDistances[1] = 0.15f;
			startDistances[2] = 1.0f;
		} else {
			specularHue = GUIOp.scaleColour(baseHue, 0.80);
			diffuseHue = GUIOp.scaleColour(baseHue, 0.7);
			shadowHue = GUIOp.scaleColour(baseHue, 0.1);

			gradient[0] = shadowHue;
			gradient[1] = diffuseHue;
			gradient[2] = specularHue;

			startDistances[0] = 0.0f;
			startDistances[1] = 0.8f;
			startDistances[2] = 1.0f;
		}

		return new LinearGradientPaint(start, end, startDistances, gradient);
	}
}
