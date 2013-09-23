package scriptease.gui.SEGraph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEGraphToolBarObserver;
import scriptease.gui.WindowFactory;
import scriptease.gui.component.UserInformationPane.UserInformationType;
import scriptease.util.FileOp;
import scriptease.util.GUIOp;

/**
 * Builds a ToolBar to edit graphs with. Includes buttons for selecting nodes,
 * adding and deleting nodes, and adding and deleting paths. The ToolBar buttons
 * only set the mode; the graph itself contains the specific actions that should
 * happen.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
@SuppressWarnings("serial")
public class SEGraphToolBar extends JToolBar {
	private final ObserverManager<SEGraphToolBarObserver> observers;

	private final JToggleButton selectButton;
	private final JToggleButton insertButton;
	private final JToggleButton deleteButton;
	private final JToggleButton connectButton;
	private final JToggleButton disconnectButton;
	private final JToggleButton groupButton;

	private Mode mode;

	/**
	 * The current mode of the toolbar.
	 * 
	 * @author kschenk
	 * @author jyuen
	 * 
	 */
	public static enum Mode {
		SELECT(false), INSERT, DELETE, CONNECT, DISCONNECT, GROUP;

		private static final String CURSOR_DIRECTORY = "scriptease/resources/icons/buttonicons/";
		private static final String CURSOR_EXTENSION = ".png";

		private final ImageIcon image;
		private final Cursor cursor;

		private Mode() {
			this(true);
		}

		private Mode(boolean useCustomCursor) {
			final String iconName = this.name().toLowerCase();

			ImageIcon image;

			try {
				final BufferedImage buttonImage;

				buttonImage = ImageIO.read(FileOp
						.getFileResource(CURSOR_DIRECTORY + iconName
								+ CURSOR_EXTENSION));

				image = new ImageIcon(buttonImage);
			} catch (IOException e) {
				UncaughtExceptionHandler handler = Thread
						.getDefaultUncaughtExceptionHandler();
				handler.uncaughtException(Thread.currentThread(),
						new IllegalStateException("Exception " + e
								+ "while adding the icon for "
								+ "ToolBarButton " + this.name()));
				image = null;
			}

			if (useCustomCursor) {
				this.cursor = GUIOp.createCursor(iconName);
			} else
				this.cursor = null;

			this.image = image;

		}

		/**
		 * Returns the cursor associated with the mode.
		 * 
		 * @param mode
		 * @return
		 */
		public Cursor getCursor() {
			return this.cursor;
		}

		/**
		 * Loads the image for the toolbar button from the path:
		 * "scriptease/resources/icons/buttonicons/actionName.png", where
		 * actionName refers to the name of the icon.
		 * 
		 * All images loaded in this way must be .png files, and must be located
		 * in the buttonicons folder.
		 * 
		 * @param iconName
		 *            The name of the image file being loaded, without the .png
		 *            extension.
		 * 
		 * @return An {@link ImageIcon} for the loaded image, or null if image
		 *         cannot be loaded.
		 */
		private ImageIcon getIcon() {
			return this.image;
		}
	}

	public SEGraphToolBar(boolean disableGroupTool) {
		super();
		this.observers = new ObserverManager<SEGraphToolBarObserver>();

		final ButtonGroup buttonGroup = new ButtonGroup();

		this.selectButton = this.buildToggleButton(Mode.SELECT);
		this.insertButton = this.buildToggleButton(Mode.INSERT);
		this.deleteButton = this.buildToggleButton(Mode.DELETE);
		this.connectButton = this.buildToggleButton(Mode.CONNECT);
		this.disconnectButton = this.buildToggleButton(Mode.DISCONNECT);
		this.groupButton = this.buildToggleButton(Mode.GROUP);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setRollover(true);
		this.setFloatable(false);
		this.setBackground(Color.WHITE);

		buttonGroup.add(this.selectButton);
		buttonGroup.add(this.insertButton);
		buttonGroup.add(this.deleteButton);
		buttonGroup.add(this.connectButton);
		buttonGroup.add(this.disconnectButton);

		if (!disableGroupTool)
			buttonGroup.add(this.groupButton);

		// Sorry about the bizarre loop, but that's the way these work :(
		for (final Enumeration<AbstractButton> buttons = buttonGroup
				.getElements(); buttons.hasMoreElements();) {
			this.add(buttons.nextElement());
		}

		this.setMode(Mode.SELECT);
	}

	/**
	 * Builds a {@link JToggleButton} based on the passed in mode.
	 * 
	 * @param mode
	 * @return
	 */
	private JToggleButton buildToggleButton(final Mode mode) {
		final JToggleButton button = new JToggleButton();

		button.setIcon(mode.getIcon());
		button.setHideActionText(true);
		button.setFocusable(false);

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SEGraphToolBar.this.setMode(mode);
			}
		});

		final Timer timer = new Timer(1500, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (mode == Mode.SELECT) {
				} else if (mode == Mode.INSERT) {
					WindowFactory.getInstance().showUserInformationBox(
							"Use the insert tool to insert a new story point.",
							UserInformationType.INFO);
				} else if (mode == Mode.DELETE) {
					WindowFactory.getInstance().showUserInformationBox(
							"Use the delete tool to delete story points / ungroup story groups.",
							UserInformationType.INFO);
				} else if (mode == Mode.CONNECT) {
					WindowFactory.getInstance().showUserInformationBox(
							"Use the connect tool to link story points together.",
							UserInformationType.INFO);
				} else if (mode == Mode.DISCONNECT) {
					WindowFactory.getInstance().showUserInformationBox(
							"Use the disconnect tool to unlink story points.",
							UserInformationType.INFO);
				} else if (mode == Mode.GROUP) {
					WindowFactory.getInstance().showUserInformationBox(
							"Use the group tool to group story points together.",
							UserInformationType.INFO);
				} else {
					WindowFactory.getInstance().showUserInformationBox(
							"Select a story node to show its contents.",
							UserInformationType.INFO);
				}
			}
		});
		timer.setRepeats(false);
		
		button.addMouseListener(new MouseInputAdapter() {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				timer.start();
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				timer.stop();
			}
		});

		return button;
	}

	public void setMode(Mode mode) {
		this.mode = mode;

		final ButtonModel buttonModel;
		if (mode == Mode.SELECT) {
			buttonModel = this.selectButton.getModel();
		} else if (mode == Mode.INSERT) {
			buttonModel = this.insertButton.getModel();
		} else if (mode == Mode.DELETE) {
			buttonModel = this.deleteButton.getModel();
		} else if (mode == Mode.CONNECT) {
			buttonModel = this.connectButton.getModel();
		} else if (mode == Mode.DISCONNECT) {
			buttonModel = this.disconnectButton.getModel();
		} else if (mode == Mode.GROUP) {
			buttonModel = this.groupButton.getModel();

		} else {
			// Handle any strange behaviour by setting this to Select by
			// default.
			this.mode = Mode.SELECT;
			buttonModel = this.selectButton.getModel();
		}

		final ButtonGroup group = ((DefaultButtonModel) buttonModel).getGroup();

		group.setSelected(buttonModel, true);

		this.notifyModeSelection();
	}

	public Mode getMode() {
		return this.mode;
	}

	/**
	 * Adds a {@link SEGraphToolBarObserver} to the toolbar. This observer will
	 * last for the lifetime of the toolbar.
	 * 
	 * @param observer
	 */
	public void addObserver(SEGraphToolBarObserver observer) {
		this.observers.addObserver(this, observer);
	}

	private void notifyModeSelection() {
		for (SEGraphToolBarObserver observer : this.observers.getObservers()) {
			observer.modeChanged(this.mode);
		}
	}
}
