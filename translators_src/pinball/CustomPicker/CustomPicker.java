package translators.Pinball.CustomPicker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;

import scriptease.gui.WindowManager;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.transfer.BindingTransferHandlerExportOnly;
import scriptease.gui.pane.GameObjectPane;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.translator.codegenerator.GameObjectPicker;
import scriptease.translator.io.model.GameConstant;
import translators.Pinball.LOTRPBGameObject;

/**
 * This class was written with a very cavalier attitude as it is meant to be
 * replaced. Beware.
 * 
 * @author jtduncan
 */
public class CustomPicker implements GameObjectPicker {

	private static final String DEFAULT_PICKER_TITLE = "Game Object List";
	private String showObject;
	List<BindingWidget> labels = new ArrayList<BindingWidget>();
	public LOTRPBGameObject dumbObject;
	private Map<KnowItBinding, PickerPanel> widgetBindingToPanel;
	private JWindow popup;
	private MouseListener hoverListener;
	private JPanel panel;

	public CustomPicker() {
		this("");
	}

	public CustomPicker(String newObject) {
		this.showObject = newObject;
		widgetBindingToPanel = new HashMap<KnowItBinding, PickerPanel>();

		this.hoverListener = new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				CustomPicker.this
						.onWidgetHovered((BindingWidget) e.getSource());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				CustomPicker.this.onWidgetUnHovered();
			}
		};
	}

	public JPanel getPickerPanel() {
		// Do not build the panel if it's already been done.
		if (this.panel != null) {
			return panel;
		}
		JTabbedPane tabbedPane = new JTabbedPane();
		if (this.showObject.isEmpty()) {
			GameObjectPicker defaultPicker = new GameObjectPane(this);
			JPanel defaultPickerPanel = defaultPicker.getPickerPanel();
			tabbedPane.add(defaultPickerPanel, DEFAULT_PICKER_TITLE);
		}

		BufferedImage image = null;

		// Parse switch labels
		ImageMapParser parser = new ImageMapParser(new File(
				"translators/Pinball/switchMap"), this.showObject, "switch");

		try {
			File imgFile = new File(
					"translators/Pinball/LOTRPlayFieldSwitches.png");

			URL imageURL = imgFile.toURI().toURL();
			image = ImageIO.read(imageURL);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!parser.getLabels().isEmpty()) {
			JPanel switchPanel = buildCustomPanel(parser, image);
			tabbedPane.add(switchPanel, "Switch Map");
		}

		// If no switches were found, check the dedicated switches.
		parser = new ImageMapParser(new File("translators/Pinball/dSwitchMap"),
				this.showObject, "dedicatedSwitch");
		if (!parser.getLabels().isEmpty()) {
			JPanel dSwitchPanel = buildCustomPanel(parser, image);
			tabbedPane.add(dSwitchPanel, "Dedicated Switch Map");
		}

		// If no matching switch labels were found, check the lamps
		parser = new ImageMapParser(new File("translators/Pinball/lampMap"),
				this.showObject, "lamp");
		try {
			File imgFile = new File("translators/Pinball/LampMap.png"); // "/translators/Pinball/LOTRPlayFieldSwitches.png"
			// );

			URL imageURL = imgFile.toURI().toURL();
			image = ImageIO.read(imageURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!parser.getLabels().isEmpty()) {
			JPanel lampPanel = buildCustomPanel(parser, image);
			tabbedPane.add(lampPanel, "Lamp Map");
		}

		JPanel returnPanel = new JPanel();
		returnPanel.setLayout(new GridLayout(1, 1));
		returnPanel.add(tabbedPane);

		this.panel = returnPanel;

		return returnPanel;
	}

	private PickerPanel buildCustomPanel(ImageMapParser parser,
			BufferedImage image) {

		List<BindingWidget> widgetList = new ArrayList<BindingWidget>();

		for (BindingWidget widget : parser.getLabels()) {
			widget.setTransferHandler(BindingTransferHandlerExportOnly
					.getInstance());
			widget.setEnabled(true);
			widget.setVisible(true);

			widget.addMouseListener(hoverListener);
			widgetList.add(widget);
			labels.add(widget);

		}

		WidgetMap widgetMap = new WidgetMap(image, widgetList);
		PickerPanel panel;
		panel = new PickerPanel(widgetMap);
		panel.setLayout(null);
		panel.setVisible(true);
		panel.setOpaque(false);
		panel.setEnabled(true);

		// panel.setMinimumSize(new Dimension(image.getWidth() / 2,
		// image.getHeight() / 2));
		for (BindingWidget widget : widgetList) {
			this.widgetBindingToPanel.put(widget.getBinding(), panel);
		}

		return panel;
	}

	public JWindow buildZoomedImageWindow(PickerPanel panel,
			BindingWidget widget) {

		Dimension subImageSize = new Dimension(100, 150); // This should be a
		// user pref
		int zoomFactor = 1; // This should be a user pref

		BufferedImage image = panel.origMap.getImage();

		JWindow window = new JWindow();
		window.setSize(subImageSize);

		Point widgetOrigLocation = panel.origMap.getOriginalFromClone(widget)
				.getLocation();

		double windowX = widgetOrigLocation.getX() - (subImageSize.width / 2);
		double windowY = widgetOrigLocation.getY() - (subImageSize.height / 2);

		// Adjust for out of bounds X coordinate
		if (windowX < image.getMinX())
			windowX = 0;
		else if ((windowX + window.getWidth()) > (image.getMinX() + image
				.getWidth()))
			windowX = image.getMinX() + image.getWidth() - window.getWidth();

		// Adjust for out of bounds Y coordinate
		if (windowY < image.getMinY())
			windowY = 0;
		else if ((windowY + window.getHeight()) > (image.getMinY() + image
				.getHeight()))
			windowY = image.getMinY() + image.getHeight() - window.getHeight();

		PickerPanel subPanel = panel.getSubPanel(new Rectangle(new Point(
				(int) windowX, (int) windowY), subImageSize), zoomFactor);
		for (Component comp : subPanel.getComponents()) {
			// Remove the hover listener from the widgets in the zoomed window,
			// so
			// they don't spawn more zoomed windows.
			comp.removeMouseListener(hoverListener);
			// Now that we aren't using a mouse hover listener, we can employ
			// the built in tooltip, so we'll show the widget name.
			((BindingWidget) comp).setToolTipText(((BindingWidget) comp)
					.getBinding().toString());
			comp.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					PointerInfo pointer = MouseInfo.getPointerInfo();
					if (!popup.contains(new Point(pointer.getLocation().x
							- popup.getX(), pointer.getLocation().y
							- popup.getY())))
						popup.setVisible(false);
				}
			});
		}

		subPanel.setBorder(new LineBorder(Color.black));
		subPanel.setOpaque(false);

		window.add(subPanel);

		// Set the window's absolute position
		panel.setVisible(true);
		double panelX = panel.getLocationOnScreen().getX();
		double panelY = panel.getLocationOnScreen().getY();
		window.setLocation(
				(int) (panelX + widget.getX() - window.getWidth() / 2),
				(int) (panelY + widget.getY() - window.getHeight() / 2));

		window.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				PointerInfo pointer = MouseInfo.getPointerInfo();
				double xPos = pointer.getLocation().getX() - popup.getX();
				double yPos = pointer.getLocation().getY() - popup.getY();
				if (!popup.contains(new Point((int) xPos, (int) yPos)))
					popup.setVisible(false);
			}
		});
		return window;
	}

	@Override
	public void onWidgetClicked(KnowItBindingConstant binding) {
		GameConstant gameObj = binding.getValue();
		// Filter out non-gameObjects, like int and string,
		// since they have no position on the pinball table.
		if ((gameObj instanceof LOTRPBGameObject)) {
			CustomPicker picker = new CustomPicker(binding.getScriptValue());
			JPanel hintPanel = picker.getPickerPanel();
			JDialog hintFrame = new JDialog(WindowManager.getInstance()
					.getMainFrame(), true);
			hintFrame.getContentPane().add(hintPanel);
			// hintFrame.pack();
			hintFrame.setSize(400, 550);
			hintFrame.setVisible(true);
		}
	}

	@Override
	public void onWidgetHovered(BindingWidget widget) {
		// In case a popup already exists, get rid of it before making a new
		// one.
		if (popup != null)
			popup.setVisible(false);
		popup = this.buildZoomedImageWindow(
				widgetBindingToPanel.get(widget.getBinding()), widget);
		popup.setVisible(true);
	}

	@Override
	public void onWidgetUnHovered() {
		// popup.setVisible(false);
	}
}
