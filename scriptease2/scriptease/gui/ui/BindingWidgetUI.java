package scriptease.gui.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.ui.border.GradientLineBorder;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.util.GUIOp;

/**
 * Look and Feel Strategy for BindingWidgets that are displaying a game object,
 * KnowIt reference, or DoIt reference in the ScriptEase Pattern Constructor
 * GUI. BindingWidgets are drawn as rounded rectangles with the label's text
 * displayed in the middle and when not a Null Binding, it will also draw a
 * TypeToggleButton over the left side of the rectangle.<br>
 * <br>
 * The shape is filled in with a gradient to make it look convex or concave
 * based on whether the Resource label is Filled or not. The colour of the
 * gradient is based on the binding type set on the label. Resources are green,
 * null bindings are red, and everything else is blue.
 * 
 * @author remiller
 */
public class BindingWidgetUI extends ComponentUI {
	private static final int LINE_THICKNESS = 2;
	private JComponent typeRenderer;

	@Override
	public void installUI(JComponent comp) {
		super.installUI(comp);

		final BindingWidget widget = (BindingWidget) comp;
		final KnowItBinding binding = widget.getBinding();

		if (binding.isBound()) {
			this.typeRenderer = ScriptWidgetFactory.buildTypeWidget(binding
					.getFirstType());
		} else
			this.typeRenderer = null;

		widget.addMouseMotionListener(new MouseAdapter() {
			// Enable drag on bindings. This is necessary.
			@Override
			public void mouseDragged(MouseEvent e) {
				final TransferHandler transferHandler = widget
						.getTransferHandler();
				if (transferHandler != null) {
					if (binding.isBound()) {
						transferHandler.exportAsDrag(widget, e,
								TransferHandler.MOVE);
					}
				}
			}
		});

		if (this.typeRenderer != null) {
			final LayoutManager layout = new BoxLayout(comp, BoxLayout.X_AXIS);

			// If the component is a BindingWidget, and is bound
			if (binding.isBound()) {
				// Add a placeholder strut for each type widget.
				for (int i = 0; i < binding.getTypes().size(); i++) {
					comp.add(Box.createHorizontalStrut(20));
				}
			}
			comp.setLayout(layout);
		}

		widget.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
	}

	@Override
	public Dimension getPreferredSize(JComponent comp) {
		Dimension size = super.getPreferredSize(comp);
		if (size == null)
			size = comp.getLayout().preferredLayoutSize(comp);
		final Dimension typeSize = this.typeRenderer == null ? new Dimension()
				: this.typeRenderer.getPreferredSize();
		double width = size.getWidth() + LINE_THICKNESS * 2;
		double height = Math.max(size.getHeight(), typeSize.getHeight());

		// if there is a type symbol to display, then add extra space for it
		if (this.isUp((BindingWidget) comp)) {
			width += typeSize.width - comp.getInsets().left;
		}

		size.setSize(width, height);

		return size;
	}

	@Override
	public boolean contains(JComponent comp, int x, int y) {
		boolean contains;
		final Shape labelShape = this
				.constructWidgetShape((BindingWidget) comp);

		contains = labelShape.contains(x, y);

		if (!contains && this.typeRenderer != null)
			contains = this.typeRenderer.contains(x, y);

		return contains;
	}

	private boolean isUp(BindingWidget comp) {
		return comp.getBinding().isBound();
	}

	/*
	 * This is an instance variable only so that we instantiate this once. It's
	 * a performance boost trick that I grabbed from the Sun's UI delegates.
	 * It's faster because configuring an old shape is faster than creating a
	 * new one, and we've got a bunch of these all over the screen at a time.
	 * 
	 * - remiller
	 */
	private RoundRectangle2D shape = new RoundRectangle2D.Float();

	private Shape constructWidgetShape(BindingWidget widget) {
		final FontMetrics metrics = widget.getFontMetrics(widget.getFont());
		float width = widget.getWidth() - LINE_THICKNESS;
		float height = metrics.getHeight() + LINE_THICKNESS;
		float totalHeight = widget.getHeight();

		this.shape.setRoundRect(LINE_THICKNESS / 2, (totalHeight - height) / 2,
				width, height, height, height);
		return this.shape;
	}

	@Override
	public void paint(Graphics g, JComponent comp) {
		if (g == null)
			return;

		final BindingWidget widget = (BindingWidget) comp;
		final Color base = widget.getBackground();
		final Graphics2D g2d = (Graphics2D) g.create();
		final Shape labelShape;
		final Border borderRenderer;

		labelShape = this.constructWidgetShape(widget);

		// paint the centre fill

		if (this.isUp(widget))
			g2d.setPaint(base);
		else
			g2d.setPaint(GUIOp.scaleColour(base, 0.6));

		g2d.fill(shape);

		/*
		 * So... you may find this odd, and that's because it is. Here's the
		 * problem: I originally set up the UI to install a GradientLineBorder
		 * as per normal. I then discovered that the border was painting over
		 * the type symbol because JComponent draws borders last, and the type
		 * symbol is drawn as part of this method.
		 * 
		 * So, in short, this is a hack to make it draw nicely. Now, it installs
		 * an EmptyBorder instead to reserve the space. Then, it builds a new
		 * GradientLineBorder just to paint, which is not unlike a CellRenderer.
		 * 
		 * - remiller
		 */
		borderRenderer = new GradientLineBorder(this.determineLinePaint(widget,
				this.isUp(widget)), labelShape, LINE_THICKNESS);

		borderRenderer.paintBorder(widget, g2d, 0, 0, widget.getWidth(),
				widget.getHeight());

		// paint the Type Symbol
		if (widget.getBinding().isBound()) {
			this.paintType(g2d, widget);
		}

		g2d.dispose();
	}

	/**
	 * Paints a type icon on the toggle button as a cell renderer
	 * 
	 * @param g
	 *            the graphics context to paint in
	 * @param comp
	 *            the label whose binding type is to be painted
	 */
	private void paintType(Graphics2D g, BindingWidget comp) {
		final Color bgColor = comp.getBackground();
		final CellRendererPane renderer = new CellRendererPane();

		int i = 0;
		// This is for BindingWidgets....
		for (String type : comp.getBinding().getTypes()) {

			this.typeRenderer = ScriptWidgetFactory.buildTypeWidget(type);
			this.typeRenderer.setBackground(bgColor);

			final Dimension preferredTypeSize;

			preferredTypeSize = this.typeRenderer.getPreferredSize();

			renderer.paintComponent(
					g,
					this.typeRenderer,
					comp,
					i * preferredTypeSize.width,
					Math.round(comp.getHeight() / 2 - preferredTypeSize.height
							/ 2), preferredTypeSize.width,
					preferredTypeSize.height, true);

			// Increment counter.
			i++;
		}
	}

	private Paint determineLinePaint(BindingWidget label, boolean isUp) {
		final Color base = label.getBackground();

		final Color specularHue;
		final Color diffuseHue;
		final Color shadowHue;
		final Paint paint;
		// I hate arrays. - remiller
		final float[] startDistances;
		final Color[] gradient;

		final int height = label.getHeight();
		final Point2D start = new Point(0, 0);
		final Point2D end = new Point(0, height == 0 ? 1 : height);

		if (isUp) {
			startDistances = new float[4];
			gradient = new Color[4];

			specularHue = GUIOp.scaleColour(base, 1.25);
			diffuseHue = base;
			shadowHue = GUIOp.scaleColour(base, 0.6);

			gradient[0] = diffuseHue;
			gradient[1] = specularHue;
			gradient[2] = diffuseHue;
			gradient[3] = shadowHue;

			startDistances[0] = 0.0f;
			startDistances[1] = 0.05f;
			startDistances[2] = 0.3f;
			startDistances[3] = 1.0f;
		} else {
			startDistances = new float[3];
			gradient = new Color[3];

			specularHue = GUIOp.scaleColour(base, 1.00);
			diffuseHue = GUIOp.scaleColour(base, 0.7);
			shadowHue = GUIOp.scaleColour(base, 0.3);

			gradient[0] = shadowHue;
			gradient[1] = diffuseHue;
			gradient[2] = specularHue;

			startDistances[0] = 0.0f;
			startDistances[1] = 0.6f;
			startDistances[2] = 1.0f;

		}

		paint = new LinearGradientPaint(start, end, startDistances, gradient);

		return (paint);
	}
}
