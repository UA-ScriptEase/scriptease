package translators.Pinball.CustomPicker;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.gui.SETree.cell.BindingWidget;

public class WidgetMap {
		private BufferedImage origImage;
		private List<BindingWidget> widgets;
		private Map<BindingWidget, BindingWidget> cloneToOrigWidget;

		public WidgetMap(BufferedImage image) {
			this(image, null);
		}

		public WidgetMap(BufferedImage image, List<BindingWidget> widgetList) {
			this.origImage = image;
			if (widgetList != null)
				this.widgets = widgetList;
			else
				this.widgets = new ArrayList<BindingWidget>();

			this.cloneToOrigWidget = new HashMap<BindingWidget, BindingWidget>();
		}

		public void addWidget(BindingWidget widget) {
			this.widgets.add(widget);
		}

		public List<BindingWidget> getWidgets() {
			List<BindingWidget> clonedList = new ArrayList<BindingWidget>();
			for (BindingWidget widget : this.widgets) {
				BindingWidget clone = widget.clone();
				this.cloneToOrigWidget.put(clone, widget);
				clonedList.add(clone);
			}

			return clonedList;
		}

		public List<BindingWidget> getWidgetsContainedIn(Rectangle rect) {
			List<BindingWidget> containedWidgets = new ArrayList<BindingWidget>();
			for (BindingWidget widget : this.widgets) {
				if (rect.contains(widget.getLocation())
						|| rect.contains(new Point(widget.getLocation().x
								+ widget.getWidth(), widget.getLocation().y
								+ widget.getHeight()))) {
					BindingWidget clone = widget.clone();
					containedWidgets.add(clone);
					this.cloneToOrigWidget.put(clone, widget);
				}
			}

			return containedWidgets;
		}

		public BindingWidget getOriginalFromClone(BindingWidget clone) {
			return this.cloneToOrigWidget.get(clone);
		}

		public BufferedImage getImage() {
			return this.origImage;
		}

		public int getWidth() {
			return this.origImage.getWidth();
		}

		public int getHeight() {
			return this.origImage.getHeight();
		}
	}