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
import java.util.Collection;

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

		this.installListeners(comp);
		this.installLayout(comp);
		this.installBorder(comp);
	}

	private void installLayout(JComponent comp) {
		final LayoutManager layout;
		this.updateTypeRenderer((BindingWidget) comp);
		if (this.typeRenderer != null) {
			layout = new BoxLayout(comp, BoxLayout.X_AXIS);

			// If the component is a BindingWidget, and is bound
			if (comp instanceof BindingWidget
					&& ((BindingWidget) comp).getBinding().isBound()) {
				// Get the types of the Binding.
				Collection<String> types = ((BindingWidget) comp).getBinding()
						.getTypes();

				// Add a placeholder strut for each type widget.
				for (int i = 0; i < types.size(); i++) {
					comp.add(Box.createHorizontalStrut(20));
				}
			}
			comp.setLayout(layout);
		}
	}

	private void installListeners(JComponent comp) {

		comp.addMouseMotionListener(new MouseAdapter() {
			/*
			 * we have to do this to enable drag support on the bindings,
			 * because they're built from a component that does not natively
			 * support it. - remiller
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				final BindingWidget source = (BindingWidget) e.getSource();
				final TransferHandler transferHandler = source
						.getTransferHandler();
				if (transferHandler != null) {
					if (source.getBinding().isBound()) {
						// if (e.isShiftDown())
						transferHandler.exportAsDrag(source, e,
								TransferHandler.MOVE);
					}
				}
			}
		});
	}

	private void installBorder(JComponent comp) {
		final BindingWidget label = (BindingWidget) comp;
		final Border border;
		final int curveBuffer = 5;
		final Border insideSpacer;
		final Border outsideSpacer;

		// this only exists to reserve some space for the actual GradientBorder
		// to be painted in .paint() - remiller
		int borderSpace = LINE_THICKNESS;
		outsideSpacer = BorderFactory.createEmptyBorder(borderSpace,
				borderSpace, borderSpace, borderSpace);

		// pads the inside of the border, so that the text doesn't get too close
		// to the curve.
		insideSpacer = BorderFactory.createEmptyBorder(0, curveBuffer, 0,
				curveBuffer);

		// squish them together
		border = BorderFactory
				.createCompoundBorder(outsideSpacer, insideSpacer);

		label.setBorder(border);
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
		final Shape labelShape = this.constructLabelShape((BindingWidget) comp);

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

	private Shape constructLabelShape(BindingWidget label) {
		final FontMetrics metrics = label.getFontMetrics(label.getFont());
		float width = label.getWidth() - LINE_THICKNESS;
		float height = metrics.getHeight() + LINE_THICKNESS;
		float totalHeight = label.getHeight();

		this.shape.setRoundRect(LINE_THICKNESS / 2, (totalHeight - height) / 2,
				width, height, height, height);
		return this.shape;
	}

	@Override
	public void paint(Graphics g, JComponent comp) {
		final BindingWidget widget = (BindingWidget) comp;
		final Graphics2D tempGraphics;
		final Shape labelShape;

		// clone the other graphics so that we don't disturb its settings.
		tempGraphics = (g == null) ? null : (Graphics2D) g.create();

		labelShape = this.constructLabelShape(widget);

		// paint the centre fill
		this.paintFill(tempGraphics, widget, labelShape);

		/*
		 * Why yes, it is odd for us to paint a border here. Why not use the
		 * built-in Java border drawing? See paintBorder()'s comments for
		 * details! - remiller
		 */
		this.paintBorder(widget, tempGraphics, labelShape);

		// paint the Type Symbol
		if (widget.getBinding().isBound()) {
			this.paintType(tempGraphics, widget);
		}

		tempGraphics.dispose();
	}

	/*
	 * So... you may find this odd, and that's because it is. Here's the
	 * problem: I originally set up the UI to install a GradientLineBorder as
	 * per normal. I then discovered that the border was painting over the type
	 * symbol because JComponent draws borders last, and the type symbol is
	 * drawn as part of this method.
	 * 
	 * So, in short, this is a hack to make it draw nicely. Now, it installs an
	 * EmptyBorder instead to reserve the space. Then, it builds a new
	 * GradientLineBorder just to paint, which is not unlike a CellRenderer.
	 * 
	 * - remiller
	 */
	private void paintBorder(final BindingWidget label,
			final Graphics2D tempGraphics, final Shape labelShape) {
		final Border borderRenderer = new GradientLineBorder(
				this.determineLinePaint(label, this.isUp(label)), labelShape,
				LINE_THICKNESS);

		borderRenderer.paintBorder(label, tempGraphics, 0, 0, label.getWidth(),
				label.getHeight());
	}

	// Configure a Gradient to fill the shape with and paint it
	private void paintFill(Graphics2D g, BindingWidget label, Shape shape) {
		Color base = label.getBackground();

		if (!this.isUp(label)) {
			base = GUIOp.scaleColour(base, 0.6);
		}

		g.setPaint(base);

		g.fill(shape);
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

			this.typeRenderer = ScriptWidgetFactory.getTypeWidget(type);
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

	private void updateTypeRenderer(BindingWidget widget) {
		final String type;
		KnowItBinding binding = widget.getBinding();

		if (binding.isBound()) {
			type = binding.getFirstType();
			this.typeRenderer = ScriptWidgetFactory.getTypeWidget(type);
		} else
			this.typeRenderer = null;

	}

	private Paint determineLinePaint(BindingWidget label, boolean isUp) {
		final Color base = label.getBackground();
		final Color specularHue;
		final Color diffuseHue;
		final Color shadowHue;
		final Point2D start;
		final Point2D end;
		final Paint paint;
		final int height = label.getHeight();
		// I hate arrays. - remiller
		final float[] startDistances;
		final Color[] gradient;

		start = new Point(0, 0);
		end = new Point(0, height == 0 ? 1 : height);

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
