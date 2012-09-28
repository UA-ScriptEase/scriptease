package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import scriptease.gui.WindowFactory;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.util.GUIOp;

/**
 * Builds a dialog box that appears to let the user select types. This is
 * separate from DialogFactory and WindowManager since it is a much larger class
 * than most dialogs. It also needs to take in custom actions.
 * 
 * @see {@link #ShowTypeMenuAction}
 * 
 * @author kschenk
 * 
 */
public class TypeSelectionDialogBuilder {
	private final Map<String, Boolean> typesToSelected;
	private final List<CheckBoxPanel> checkBoxPanels;
	private final JButton allButton;

	private Runnable closeAction;

	/**
	 * Creates a new TypeSelectionDialogBuilder, intializing the variables.
	 */
	public TypeSelectionDialogBuilder() {
		this.allButton = new JButton("Deselect All");
		this.typesToSelected = new HashMap<String, Boolean>();
		this.checkBoxPanels = new ArrayList<CheckBoxPanel>();

		final Translator activeTranslator;
		final Collection<String> types;
		// Create the translator and populate lists.
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		if (activeTranslator == null)
			return;

		types = activeTranslator.getGameTypeManager().getKeywords();

		for (String type : types) {
			this.typesToSelected.put(type, Boolean.TRUE);
		}
	}

	/**
	 * Creates a new TypeSelectionDialogBuilder. The passed okAction will run
	 * when "Ok" is pressed on the dialog.
	 * 
	 * @param closeAction
	 */
	public TypeSelectionDialogBuilder(Runnable closeAction) {
		this();
		this.closeAction = closeAction;
	}

	/**
	 * Sets the close action to the passed runnable. This will run when the
	 * dialog box is closed.
	 * 
	 * @param closeAction
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	/**
	 * Returns the dialog box that represents Type Selection.
	 * 
	 * @return
	 */
	public JDialog buildTypeDialog() {
		final JPanel typesPanel;
		final JPanel content;
		final JButton closeButton;
		final JSeparator separator;
		final JDialog typeDialog;

		final GroupLayout groupLayout;

		typeDialog = WindowFactory.getInstance().buildDialog("Type Selection");

		typesPanel = this.buildTypesPanel();
		content = new JPanel();
		closeButton = new JButton("Close");
		separator = new JSeparator(SwingConstants.HORIZONTAL);

		groupLayout = new GroupLayout(content);

		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);

		// Set up the action listeners for the buttons.
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (TypeSelectionDialogBuilder.this.closeAction != null)
					TypeSelectionDialogBuilder.this.closeAction.run();
				typeDialog.setVisible(false);
			}
		});

		typeDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (TypeSelectionDialogBuilder.this.closeAction != null)
					TypeSelectionDialogBuilder.this.closeAction.run();
				typeDialog.setVisible(false);
			}
		});

		for (ActionListener listener : this.allButton.getActionListeners()) {
			this.allButton.removeActionListener(listener);
		}

		this.allButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectTypes(TypeSelectionDialogBuilder.this.typesToSelected
						.keySet(), !isAllSelected());
				updateAllButton();
			}
		});

		content.setLayout(groupLayout);

		groupLayout.setHorizontalGroup(groupLayout
				.createParallelGroup()
				.addComponent(typesPanel)
				.addComponent(separator)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						groupLayout.createSequentialGroup()
								.addComponent(this.allButton)
								.addComponent(closeButton)));

		groupLayout.setVerticalGroup(groupLayout
				.createSequentialGroup()
				.addComponent(typesPanel)
				.addComponent(separator)
				.addGroup(
						groupLayout.createParallelGroup()
								.addComponent(this.allButton)
								.addComponent(closeButton)));

		typeDialog.setContentPane(content);
		typeDialog.pack();
		typeDialog.setResizable(false);
		typeDialog.setLocationRelativeTo(typeDialog.getParent());
		return typeDialog;
	}

	/**
	 * Populates a panel with types in the form of CheckBoxPanels.
	 * 
	 * @return
	 */
	private JPanel buildTypesPanel() {
		final int SCROLLPANE_HEIGHT = 250;

		final Dimension MAX_SCROLLPANE_SIZE = new Dimension(500,
				SCROLLPANE_HEIGHT);
		final Dimension MIN_SCROLLPANE_SIZE = new Dimension(300,
				SCROLLPANE_HEIGHT);

		final JPanel typesPanel;

		final JPanel gameObjectPanel;
		final JPanel gameConstantPanel;
		final JPanel listPanel;

		final JPanel gameObjectPanel1;
		final JPanel gameObjectPanel2;
		final JPanel gameConstantPanel1;
		final JPanel gameConstantPanel2;
		final JPanel listPanel1;
		final JPanel listPanel2;

		final JScrollPane gameObjectScrollPane;
		final JScrollPane gameConstantScrollPane;
		final JScrollPane listScrollPane;

		final GridLayout gridLayout;

		final List<CheckBoxPanel> checkBoxPanels;
		final Translator activeTranslator;
		final GameTypeManager typeManager;

		typesPanel = new JPanel();

		gameObjectPanel = new JPanel();
		gameConstantPanel = new JPanel();
		listPanel = new JPanel();

		gameObjectPanel1 = new JPanel();
		gameObjectPanel2 = new JPanel();
		gameConstantPanel1 = new JPanel();
		gameConstantPanel2 = new JPanel();
		listPanel1 = new JPanel();
		listPanel2 = new JPanel();

		gameObjectScrollPane = new JScrollPane(gameObjectPanel);
		gameConstantScrollPane = new JScrollPane(gameConstantPanel);
		listScrollPane = new JScrollPane(listPanel);

		gridLayout = new GridLayout(0, 2);

		checkBoxPanels = new ArrayList<CheckBoxPanel>();
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		typeManager = activeTranslator.getGameTypeManager();

		// create a menu item for each type
		if (activeTranslator != null) {
			final Collection<String> types;

			types = new ArrayList<String>(this.typesToSelected.keySet());

			for (String type : types) {
				final CheckBoxPanel checkBoxPanel;
				final Boolean typeBool;

				checkBoxPanel = new CheckBoxPanel(type);
				typeBool = this.typesToSelected.get(type);

				this.checkBoxPanels.add(checkBoxPanel);

				if (typeBool != null)
					checkBoxPanel.setSelected(typeBool.booleanValue());

				checkBoxPanels.add(checkBoxPanel);
			}
		}

		Collections.sort(checkBoxPanels, new Comparator<CheckBoxPanel>() {
			@Override
			public int compare(CheckBoxPanel o1, CheckBoxPanel o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(
						typeManager.getDisplayText(o1.getTypeKeyword()),
						typeManager.getDisplayText(o2.getTypeKeyword()));
			}
		});

		typesPanel.setLayout(new BoxLayout(typesPanel, BoxLayout.LINE_AXIS));

		gameObjectPanel1.setLayout(new BoxLayout(gameObjectPanel1,
				BoxLayout.PAGE_AXIS));
		gameObjectPanel2.setLayout(new BoxLayout(gameObjectPanel2,
				BoxLayout.PAGE_AXIS));
		gameConstantPanel1.setLayout(new BoxLayout(gameConstantPanel1,
				BoxLayout.PAGE_AXIS));
		gameConstantPanel2.setLayout(new BoxLayout(gameConstantPanel2,
				BoxLayout.PAGE_AXIS));
		listPanel1.setLayout(new BoxLayout(listPanel1, BoxLayout.PAGE_AXIS));
		listPanel2.setLayout(new BoxLayout(listPanel2, BoxLayout.PAGE_AXIS));

		gameObjectScrollPane.setMinimumSize(MIN_SCROLLPANE_SIZE);
		gameConstantScrollPane.setMinimumSize(MIN_SCROLLPANE_SIZE);
		listScrollPane.setMinimumSize(MIN_SCROLLPANE_SIZE);

		gameObjectScrollPane.setMaximumSize(MAX_SCROLLPANE_SIZE);
		gameConstantScrollPane.setMaximumSize(MAX_SCROLLPANE_SIZE);
		listScrollPane.setMaximumSize(MAX_SCROLLPANE_SIZE);

		gameObjectScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		gameConstantScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);
		listScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		gameObjectScrollPane.setBorder(BorderFactory
				.createTitledBorder("Game Objects"));
		gameConstantScrollPane.setBorder(BorderFactory
				.createTitledBorder("Game Constants"));
		listScrollPane.setBorder(BorderFactory.createTitledBorder("Lists"));

		gridLayout.setHgap(5);
		gridLayout.setVgap(5);

		gameObjectPanel.setOpaque(false);
		gameConstantPanel.setOpaque(false);
		listPanel.setOpaque(false);

		gameObjectPanel.setLayout(gridLayout);
		gameConstantPanel.setLayout(gridLayout);
		listPanel.setLayout(gridLayout);

		int count = 0;
		int size = checkBoxPanels.size();
		// fill the menu
		for (CheckBoxPanel checkBoxPanel : checkBoxPanels) {
			final String type;

			type = checkBoxPanel.getTypeKeyword();

			if (typeManager.hasEnum(type)) {
				if (count <= size / 2)
					listPanel1.add(checkBoxPanel);
				else
					listPanel2.add(checkBoxPanel);
			} else if (typeManager.getSlots(type).size() > 0) {
				if (count <= size / 2)
					gameObjectPanel1.add(checkBoxPanel);
				else
					gameObjectPanel2.add(checkBoxPanel);
			} else {
				if (count <= size / 2)
					gameConstantPanel1.add(checkBoxPanel);
				else
					gameConstantPanel2.add(checkBoxPanel);
			}
			count++;
		}

		if (gameObjectPanel1.getComponents().length > 0) {
			gameObjectPanel.add(gameObjectPanel1);
			gameObjectPanel.add(gameObjectPanel2);
		}
		if (gameConstantPanel1.getComponents().length > 0) {
			gameConstantPanel.add(gameConstantPanel1);
			gameConstantPanel.add(gameConstantPanel2);
		}
		if (listPanel1.getComponents().length > 0) {
			listPanel.add(listPanel1);
			listPanel.add(listPanel2);
		}

		if (gameObjectPanel.getComponents().length > 0)
			typesPanel.add(gameObjectScrollPane);

		if (gameConstantPanel.getComponents().length > 0)
			typesPanel.add(gameConstantScrollPane);

		if (listPanel.getComponents().length > 0)
			typesPanel.add(listScrollPane);

		this.updateAllButton();

		return typesPanel;
	}

	/**
	 * Updates the All Button's text based on whether all types are selected or
	 * not.
	 */
	private void updateAllButton() {
		if (this.isAllSelected()) {
			this.allButton.setText("Deselect All");
		} else {
			this.allButton.setText("Select All");
		}
	}

	/**
	 * Gets a collection of selected type check boxes
	 * 
	 * @return a collection of selected type check boxes
	 */
	public Collection<String> getSelectedTypes() {
		final List<String> checked = new ArrayList<String>();
		Boolean isAccepted;

		for (String key : this.typesToSelected.keySet()) {
			isAccepted = this.typesToSelected.get(key);

			if (isAccepted.booleanValue())
				checked.add(key);
		}
		return checked;
	}

	public void deselectAll() {
		for (String type : this.typesToSelected.keySet()) {
			selectType(type, false);
		}
	}

	public Collection<String> getTypes() {
		return this.typesToSelected.keySet();
	}

	/**
	 * Selects a collection of types.
	 * 
	 * @param types
	 * @param isSelected
	 */
	public void selectTypes(Collection<String> types, boolean isSelected) {
		for (String type : types)
			selectType(type, isSelected);
	}

	/**
	 * Sets the passed type to the passed boolean value, and deals with the
	 * "all" button.
	 * 
	 * @param type
	 * @param isSelected
	 */
	public void selectType(String type, boolean isSelected) {
		this.typesToSelected.put(type, Boolean.valueOf(isSelected));

		for (CheckBoxPanel panel : this.checkBoxPanels) {
			if (panel.getTypeKeyword().equals(type))
				panel.setSelected(isSelected);
		}

		this.updateAllButton();
	}

	private boolean isAllSelected() {
		return this.getSelectedTypes().size() >= this.typesToSelected.size();
	}

	/**
	 * Listener for check box panels that changes the GUI and sets the states of
	 * buttons.
	 * 
	 * @author kschenk
	 * 
	 */
	private class CheckBoxPanelListener extends MouseAdapter {
		private CheckBoxPanel checkPanel;

		private CheckBoxPanelListener(CheckBoxPanel checkPanel) {
			this.checkPanel = checkPanel;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.checkPanel.setColour(GUIOp.scaleWhite(Color.GRAY, 1.8), false);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			final String typeText = this.checkPanel.getTypeKeyword();

			boolean selected = TypeSelectionDialogBuilder.this.typesToSelected
					.get(typeText);

			selectType(typeText, !selected);
		}

		// On hover behaviour
		@Override
		public void mouseEntered(MouseEvent e) {
			this.checkPanel.setColour(GUIOp.scaleWhite(Color.GRAY, 1.7), false);
		}

		// On exit hover behaviour
		@Override
		public void mouseExited(MouseEvent e) {
			this.checkPanel
					.setColour(this.checkPanel.getCurrentColour(), false);
		}
	}

	/**
	 * A CheckBoxPanel is a JPanel with a JCheckBox and JLabel that is used to
	 * represent a type and whether it is selected or not.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class CheckBoxPanel extends JPanel {
		private final Color selectedColour = GUIOp.scaleWhite(Color.GRAY, 1.5);

		private final JCheckBox checkBox;
		private final String typeKeyword;
		private final JLabel typeLabel;
		private final Color defaultColour;

		private Color currentColour;

		private CheckBoxPanel(String typeKeyword) {
			super();
			final Dimension MAX_PANEL_SIZE = new Dimension(2400, 25);
			final Dimension MIN_PANEL_SIZE = new Dimension(10, 25);

			final String typeDisplayText;

			typeDisplayText = TranslatorManager.getInstance()
					.getActiveTranslator().getGameTypeManager()
					.getDisplayText(typeKeyword);

			this.checkBox = new JCheckBox();
			this.typeKeyword = typeKeyword;
			this.typeLabel = new JLabel(" " + typeDisplayText);
			this.defaultColour = this.getBackground();

			this.setLayout(new BorderLayout());

			this.setMinimumSize(MIN_PANEL_SIZE);
			this.setMaximumSize(MAX_PANEL_SIZE);

			this.add(this.checkBox, BorderLayout.EAST);
			this.add(this.typeLabel, BorderLayout.WEST);

			this.setBorder(BorderFactory.createEtchedBorder());

			for (MouseListener defaultListener : this.checkBox
					.getMouseListeners())
				this.checkBox.removeMouseListener(defaultListener);

			this.addMouseListener(new CheckBoxPanelListener(this));
			this.checkBox.addMouseListener(new CheckBoxPanelListener(this));
		}

		/**
		 * Sets the colour to the specified colour. Sets checkPanelColour to the
		 * colour if setDefault is true.
		 * 
		 * @param colour
		 * @param setCurrent
		 */
		private void setColour(Color colour, boolean setCurrent) {
			this.setBackground(colour);
			this.checkBox.setBackground(colour);

			if (setCurrent)
				this.currentColour = colour;
		}

		/**
		 * Returns the current colour of the panel.
		 * 
		 * @return
		 */
		private Color getCurrentColour() {
			return this.currentColour;
		}

		/**
		 * Returns the keyword of the type. This should not be visible to the
		 * user and should be unique.
		 * 
		 * @return
		 */
		private String getTypeKeyword() {
			return this.typeKeyword;
		}

		/**
		 * Sets the selected state of the checkbox and also the colour of the
		 * JPanel.
		 * 
		 * @param isSelected
		 */
		private void setSelected(boolean isSelected) {
			this.checkBox.setSelected(isSelected);
			if (isSelected)
				this.setColour(this.selectedColour, true);
			else
				this.setColour(this.defaultColour, true);
		}
	}
}
