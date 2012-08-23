package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.gui.WindowFactory;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
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
	 * Creates a new TypeSelectionDialogueBuilder, initializing the variables.
	 */
	public TypeSelectionDialogBuilder() {
		this.allButton = new JButton("Deselect All");
		this.typesToSelected = new HashMap<String, Boolean>();
		this.checkBoxPanels = new ArrayList<CheckBoxPanel>();
		
		final Translator activeTranslator;

		// Create the translator and populate lists.
		activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		if (activeTranslator != null)
			for (String type : activeTranslator.getGameTypeManager()
					.getKeywords()) {
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
		final JScrollPane typesScrollPane;
		final JPanel typesPanel;
		final JPanel content;
		final JButton closeButton;
		final JSeparator separator;
		final JDialog typeDialog;

		final GroupLayout groupLayout;
		final GridLayout gridLayout;

		typeDialog = WindowFactory.getInstance().buildDialog("Type Selection");

		typesPanel = new JPanel();
		typesScrollPane = new JScrollPane(typesPanel);
		content = new JPanel();
		closeButton = new JButton("Close");
		separator = new JSeparator(SwingConstants.HORIZONTAL);

		groupLayout = new GroupLayout(content);
		gridLayout = new GridLayout(0, 3);

		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);

		gridLayout.setHgap(5);
		gridLayout.setVgap(5);

		// Set up the action listeners for the buttons.
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (closeAction != null)
					closeAction.run();
				typeDialog.setVisible(false);
			}
		});

		typeDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (closeAction != null)
					closeAction.run();
				typeDialog.setVisible(false);
			}
		});
		
		for(ActionListener listener : this.allButton.getActionListeners()) {
			this.allButton.removeActionListener(listener);
		}

		this.allButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectTypes(typesToSelected.keySet(), !isAllSelected());
				updateAllButton();
			}
		});

		typesPanel.setLayout(gridLayout);
		content.setLayout(groupLayout);
		this.populatePanel(typesPanel);

		typesScrollPane.setBorder(BorderFactory.createEmptyBorder());

		groupLayout.setHorizontalGroup(groupLayout
				.createParallelGroup()
				.addComponent(typesScrollPane)
				.addComponent(separator)
				.addGroup(
						groupLayout.createSequentialGroup()
								.addComponent(this.allButton).addGap(325)
								.addComponent(closeButton)));

		groupLayout.setVerticalGroup(groupLayout
				.createSequentialGroup()
				.addComponent(typesScrollPane)
				.addComponent(separator)
				.addGroup(
						groupLayout.createParallelGroup()
								.addComponent(this.allButton)
								.addComponent(closeButton)));

		typeDialog.setContentPane(content);
		typeDialog.pack();
		typeDialog.setResizable(false);
		typeDialog.setLocationRelativeTo(typeDialog.getParent());
		typeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return typeDialog;
	}

	/**
	 * Populates the passed in panel with types in the form of CheckBoxPanels.
	 * 
	 * @param panel
	 * @param activeTranslator
	 */
	private void populatePanel(final JPanel panel) {
		final Map<String, CheckBoxPanel> typeDisplayTextToCheckBoxPanel;
		final Translator activeTranslator;
		
		typeDisplayTextToCheckBoxPanel = new TreeMap<String, CheckBoxPanel>();
		activeTranslator = TranslatorManager.getInstance().getActiveTranslator();
		
		// create a menu item for each type
		if (activeTranslator != null) {
			GameTypeManager typeManager = activeTranslator.getGameTypeManager();
			for (String type : typeManager.getKeywords()) {

				// TODO: icons for types in the type list

				CheckBoxPanel checkBoxPanel = new CheckBoxPanel(type,
						typeManager.getDisplayText(type));

				this.checkBoxPanels.add(checkBoxPanel);

				Boolean typeBool = this.typesToSelected.get(type);
				if (typeBool != null)
					checkBoxPanel.setSelected(typeBool.booleanValue());

				typeDisplayTextToCheckBoxPanel.put(
						typeManager.getDisplayText(type), checkBoxPanel);
			}
		}

		// fill the menu
		for (String typeDisplayText : typeDisplayTextToCheckBoxPanel.keySet()) {
			panel.add(typeDisplayTextToCheckBoxPanel.get(typeDisplayText));
		}

		this.updateAllButton();
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
		typesToSelected.put(type, Boolean.valueOf(isSelected));

		for (CheckBoxPanel panel : checkBoxPanels) {
			if (panel.getTypeText().equals(type))
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
			final String typeText = this.checkPanel.getTypeText();

			boolean selected = typesToSelected.get(typeText);

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
		private final String typeText;
		private final JLabel typeLabel;
		private final Color defaultColour;

		private Color currentColour;

		private CheckBoxPanel(String typeText, String typeDisplayText) {
			super();

			this.setLayout(new BorderLayout());

			this.checkBox = new JCheckBox();
			this.typeText = typeText;
			this.typeLabel = new JLabel(" " + typeDisplayText);
			this.defaultColour = this.getBackground();

			this.add(this.checkBox, BorderLayout.EAST);

			this.add(typeLabel, BorderLayout.WEST);

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
		 * Returns the text, or code symbol of the type. This should not be
		 * visible to the user, and should be unique.
		 * 
		 * @return
		 */
		private String getTypeText() {
			return this.typeText;
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
