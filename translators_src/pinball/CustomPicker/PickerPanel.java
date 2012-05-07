package translators.Pinball.CustomPicker;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import scriptease.gui.SETree.cell.BindingWidget;


public class PickerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	WidgetMap origMap;
	Image outputImage;
	List<BindingWidget> widgets;
	int offsetFromOrigX, offsetFromOrigY;
	Rectangle cropTangle;

	public PickerPanel(WidgetMap widgetMap) {
		this(widgetMap, 1);
	}

	public PickerPanel(WidgetMap widgetMap, int scaleFactor) {
		this(widgetMap, scaleFactor, new Rectangle(new Point(0, 0),
				new Dimension(widgetMap.getWidth(), widgetMap.getHeight())));
	}

	public PickerPanel(WidgetMap widgetMap, int scaleFactor,
			Rectangle cropTangle) {

		this.origMap = widgetMap;
		this.widgets = new ArrayList<BindingWidget>();

		// Need the croptangle offsets to adjust widget locations.
		this.cropTangle = cropTangle;

		for (BindingWidget widget : widgetMap.getWidgetsContainedIn(cropTangle)) {
			this.addWidget(widget);
		}

		try {
			BufferedImage croppedImage = origMap.getImage().getSubimage(
					cropTangle.x, cropTangle.y, cropTangle.width,
					cropTangle.height);
			this.outputImage = croppedImage.getScaledInstance(
					(int) (croppedImage.getWidth(null) * scaleFactor),
					(int) (croppedImage.getHeight(null) * scaleFactor),
					Image.SCALE_SMOOTH);

		} catch (Throwable e) { /* handled in paintComponent() */
		}

		this.setSize(new Dimension(outputImage.getWidth(null), outputImage
				.getHeight(null)));

		this.setLayout(new GridLayout(1, 1));
	}

	public PickerPanel getSubPanel(Rectangle cropTangle, int scaleFactor) {
		return new PickerPanel(origMap, scaleFactor, cropTangle);
	}

	public void addWidget(BindingWidget widget) {
		this.widgets.add(widget);
		super.add(widget);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (outputImage != null) {
			g.drawImage(outputImage, 0, 0, this.getWidth(), this.getHeight(),
					this);
		}
		// *** Need to sort out this scaling math for the zoom window.
		double panelXPercent = (this.getSize().getWidth() + (origMap.getWidth() - this.cropTangle
				.getWidth()))
				/ origMap.getWidth();
		double panelYPercent = (this.getSize().getHeight() + (origMap
				.getHeight() - this.cropTangle.getHeight()))
				/ origMap.getHeight();

		for (BindingWidget widget : this.widgets) {
			Point origLocation = this.origMap.getOriginalFromClone(widget)
					.getLocation();
			widget
					.setLocation(
							(int) ((origLocation.x - this.cropTangle.x) * panelXPercent),
							(int) ((origLocation.y - this.cropTangle.y) * panelYPercent));

			widget.setSize(20, 20);
		}
		super.paintComponent(g);
	}
}