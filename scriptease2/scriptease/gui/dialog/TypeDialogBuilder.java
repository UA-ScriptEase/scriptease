package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JComponent;
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
 * @see {@link #TypeAction}
 * 
 * @author kschenk
 * 
 */
public class TypeDialogBuilder {
	private final Map<String, Boolean> typesToSelected;
	private final List<CheckBoxPanel> checkBoxPanels;
	private final JButton allButton;

	private Runnable closeAction;

	/**
	 * Creates a new TypeSelectionDialogBuilder, intializing the variables.
	 */
	public TypeDialogBuilder() {
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
	public TypeDialogBuilder(Runnable closeAction) {
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
		final JScrollPane typesPanel;
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
				if (TypeDialogBuilder.this.closeAction != null)
					TypeDialogBuilder.this.closeAction.run();
				typeDialog.setVisible(false);
			}
		});

		typeDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (TypeDialogBuilder.this.closeAction != null)
					TypeDialogBuilder.this.closeAction.run();
				typeDialog.setVisible(false);
			}
		});

		for (ActionListener listener : this.allButton.getActionListeners()) {
			this.allButton.removeActionListener(listener);
		}

		this.allButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectTypes(TypeDialogBuilder.this.typesToSelected.keySet(),
						!isAllSelected());
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
	private JScrollPane buildTypesPanel() {
		final int PANEL_WIDTH = 450;
		final Dimension MAX_PANEL_SIZE = new Dimension(PANEL_WIDTH, 5000);
		final Dimension MIN_PANEL_SIZE = new Dimension(PANEL_WIDTH, 150);
		final Dimension MAX_SCROLLPANE_SIZE = new Dimension(PANEL_WIDTH + 100,
				500);

		/*
		 * Yo dawg, I heard you like panels
		 * 
		 * So we put three panels in yo panel thats in yo scrolling panel that
		 * is in yo panel thats in yo panel in yo dialog box.
		 * 
		 * No seriously, if you want to preserve your sanity, do NOT look at
		 * this method. Just look at the output: a beautiful type panel.
		 */
		final JPanel typesPanel;

		final JPanel gameObjectPanel;
		final JPanel gameConstantPanel;
		final JPanel listPanel;

		final JPanel gameObjectPanel1;
		final JPanel gameObjectPanel2;
		final JPanel gameObjectPanel3;
		final JPanel gameConstantPanel1;
		final JPanel gameConstantPanel2;
		final JPanel gameConstantPanel3;
		final JPanel listPanel1;
		final JPanel listPanel2;
		final JPanel listPanel3;

		final List<CheckBoxPanel> checkBoxPanels;
		final Translator activeTranslator;
		final GameTypeManager typeManager;

		typesPanel = new JPanel();

		gameObjectPanel = new JPanel();
		gameConstantPanel = new JPanel();
		listPanel = new JPanel();

		gameObjectPanel1 = new JPanel();
		gameObjectPanel2 = new JPanel();
		gameObjectPanel3 = new JPanel();
		gameConstantPanel1 = new JPanel();
		gameConstantPanel2 = new JPanel();
		gameConstantPanel3 = new JPanel();
		listPanel1 = new JPanel();
		listPanel2 = new JPanel();
		listPanel3 = new JPanel();

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

		typesPanel.setLayout(new BoxLayout(typesPanel, BoxLayout.PAGE_AXIS));

		gameObjectPanel1.setLayout(new BoxLayout(gameObjectPanel1,
				BoxLayout.PAGE_AXIS));
		gameObjectPanel2.setLayout(new BoxLayout(gameObjectPanel2,
				BoxLayout.PAGE_AXIS));
		gameObjectPanel3.setLayout(new BoxLayout(gameObjectPanel3,
				BoxLayout.PAGE_AXIS));
		gameConstantPanel1.setLayout(new BoxLayout(gameConstantPanel1,
				BoxLayout.PAGE_AXIS));
		gameConstantPanel2.setLayout(new BoxLayout(gameConstantPanel2,
				BoxLayout.PAGE_AXIS));
		gameConstantPanel3.setLayout(new BoxLayout(gameConstantPanel3,
				BoxLayout.PAGE_AXIS));
		listPanel1.setLayout(new BoxLayout(listPanel1, BoxLayout.PAGE_AXIS));
		listPanel2.setLayout(new BoxLayout(listPanel2, BoxLayout.PAGE_AXIS));
		listPanel3.setLayout(new BoxLayout(listPanel3, BoxLayout.PAGE_AXIS));

		gameObjectPanel1.setAlignmentY(Component.TOP_ALIGNMENT);
		gameObjectPanel2.setAlignmentY(Component.TOP_ALIGNMENT);
		gameObjectPanel3.setAlignmentY(Component.TOP_ALIGNMENT);
		gameConstantPanel1.setAlignmentY(Component.TOP_ALIGNMENT);
		gameConstantPanel2.setAlignmentY(Component.TOP_ALIGNMENT);
		gameConstantPanel3.setAlignmentY(Component.TOP_ALIGNMENT);
		listPanel1.setAlignmentY(Component.TOP_ALIGNMENT);
		listPanel2.setAlignmentY(Component.TOP_ALIGNMENT);
		listPanel3.setAlignmentY(Component.TOP_ALIGNMENT);

		gameObjectPanel.setMinimumSize(MIN_PANEL_SIZE);
		gameConstantPanel.setMinimumSize(MIN_PANEL_SIZE);
		listPanel.setMinimumSize(MIN_PANEL_SIZE);

		gameObjectPanel.setMaximumSize(MAX_PANEL_SIZE);
		gameConstantPanel.setMaximumSize(MAX_PANEL_SIZE);
		listPanel.setMaximumSize(MAX_PANEL_SIZE);

		gameObjectPanel.setOpaque(false);
		gameConstantPanel.setOpaque(false);
		listPanel.setOpaque(false);

		gameObjectPanel.setLayout(new BoxLayout(gameObjectPanel,
				BoxLayout.LINE_AXIS));
		gameConstantPanel.setLayout(new BoxLayout(gameConstantPanel,
				BoxLayout.LINE_AXIS));
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.LINE_AXIS));

		// Fill the panels. Again, my apologies about how absolutely disgusting
		// this code is, but there was no other way.
		final Collection<CheckBoxPanel> gameObjectCBPanels;
		final Collection<CheckBoxPanel> gameConstantCBPanels;
		final Collection<CheckBoxPanel> listCBPanels;

		gameObjectCBPanels = new ArrayList<CheckBoxPanel>();
		gameConstantCBPanels = new ArrayList<CheckBoxPanel>();
		listCBPanels = new ArrayList<CheckBoxPanel>();

		for (CheckBoxPanel checkBoxPanel : checkBoxPanels) {
			final String type;

			type = checkBoxPanel.getTypeKeyword();

			if (typeManager.hasEnum(type)) {
				listCBPanels.add(checkBoxPanel);
			} else if (typeManager.getSlots(type).size() > 0) {
				gameObjectCBPanels.add(checkBoxPanel);
			} else {
				gameConstantCBPanels.add(checkBoxPanel);
			}
		}

		int objectCount = 0;
		int size = gameObjectCBPanels.size();
		for (CheckBoxPanel gameObjectCBPanel : gameObjectCBPanels) {
			if (objectCount <= size / 3)
				gameObjectPanel1.add(gameObjectCBPanel);
			else if (objectCount <= 2 * size / 3)
				gameObjectPanel2.add(gameObjectCBPanel);
			else
				gameObjectPanel3.add(gameObjectCBPanel);
			objectCount++;
		}

		objectCount = 0;
		size = gameConstantCBPanels.size();
		for (CheckBoxPanel gameConstantCBPanel : gameConstantCBPanels) {
			if (objectCount <= size / 3)
				gameConstantPanel1.add(gameConstantCBPanel);
			else if (objectCount <= 2 * size / 3)
				gameConstantPanel2.add(gameConstantCBPanel);
			else
				gameConstantPanel3.add(gameConstantCBPanel);
			objectCount++;
		}

		objectCount = 0;
		size = listCBPanels.size();
		for (CheckBoxPanel listCBPanel : listCBPanels) {
			if (objectCount <= size / 3)
				listPanel1.add(listCBPanel);
			else if (objectCount <= size * 2 / 3)
				listPanel2.add(listCBPanel);
			else
				listPanel3.add(listCBPanel);
			objectCount++;
		}

		if (gameObjectPanel1.getComponents().length > 0) {
			gameObjectPanel.add(gameObjectPanel1);
			gameObjectPanel.add(gameObjectPanel2);
			gameObjectPanel.add(gameObjectPanel3);
		}
		if (gameConstantPanel1.getComponents().length > 0) {
			gameConstantPanel.add(gameConstantPanel1);
			gameConstantPanel.add(gameConstantPanel2);
			gameConstantPanel.add(gameConstantPanel3);
		}
		if (listPanel1.getComponents().length > 0) {
			listPanel.add(listPanel1);
			listPanel.add(listPanel2);
			listPanel.add(listPanel3);
		}

		if (gameObjectPanel.getComponents().length > 0) {
			final JLabel label;

			label = new JLabel("Game Objects");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			gameObjectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			typesPanel.add(label);
			typesPanel.add(gameObjectPanel);
		}

		if (gameConstantPanel.getComponents().length > 0) {
			final JLabel label;

			label = new JLabel("Game Constants");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			gameConstantPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			typesPanel.add(label);
			typesPanel.add(gameConstantPanel);
		}

		if (listPanel.getComponents().length > 0) {
			final JLabel label;

			label = new JLabel("Lists");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			typesPanel.add(label);
			typesPanel.add(listPanel);
		}
		this.updateAllButton();

		final JScrollPane typeScrollPane;

		typeScrollPane = new JScrollPane(typesPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		typeScrollPane.setMaximumSize(MAX_SCROLLPANE_SIZE);
		typeScrollPane.setBorder(BorderFactory.createEmptyBorder());
		typeScrollPane.getVerticalScrollBar().setUnitIncrement(
				ScriptEaseUI.VERTICAL_SCROLLBAR_INCREMENT);

		return typeScrollPane;
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
	 * A CheckBoxPanel is a JPanel with a JCheckBox and JLabel that is used to
	 * represent a type and whether it is selected or not.
	 * 
	 * @author kschenk
	 * 
	 */
	@SuppressWarnings("serial")
	private class CheckBoxPanel extends JPanel implements MouseListener {
		private final Color SELECTED_COLOUR = GUIOp.scaleWhite(
				Color.LIGHT_GRAY, 1.2);
		private final Color UNSELECTED_COLOUR = GUIOp.scaleWhite(
				Color.LIGHT_GRAY, 0.8);

		private final JCheckBox checkBox;
		private final String typeKeyword;
		private final JLabel typeLabel;

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

			this.setLayout(new BorderLayout());

			this.setMinimumSize(MIN_PANEL_SIZE);
			this.setMaximumSize(MAX_PANEL_SIZE);

			this.add(this.checkBox, BorderLayout.EAST);
			this.add(this.typeLabel, BorderLayout.WEST);
			this.checkBox.setOpaque(false);
			this.setOpaque(false);

			for (MouseListener defaultListener : this.checkBox
					.getMouseListeners())
				this.checkBox.removeMouseListener(defaultListener);

			this.addMouseListener(this);
			this.checkBox.addMouseListener(this);
		}

		private void resetUI() {
			if (this.checkBox.isSelected()) {
				this.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(Color.gray),
						BorderFactory.createEmptyBorder(1, 1, 1, 1)));
				this.setBackground(SELECTED_COLOUR);
			} else {
				this.setBorder(BorderFactory.createRaisedBevelBorder());
				this.setBackground(UNSELECTED_COLOUR);
			}
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
			this.resetUI();
		}

		@Override
		protected void paintComponent(Graphics grphcs) {
			Graphics2D g2d = (Graphics2D) grphcs;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			GradientPaint gp = new GradientPaint(0, 0, this.getBackground(), 0,
					getHeight(), GUIOp.scaleWhite(this.getBackground(), 1.3));

			g2d.setPaint(gp);

			g2d.fillRect(0, 0, getWidth(), getHeight());

			super.paintComponent(grphcs);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.setBackground(GUIOp.scaleColour(this.getBackground(), 0.7));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			e.consume();
		}

		// On hover behaviour
		@Override
		public void mouseEntered(MouseEvent e) {
			this.setBackground(GUIOp.scaleWhite(this.getBackground(), 1.1));
		}

		// On exit hover behaviour
		@Override
		public void mouseExited(MouseEvent e) {
			this.resetUI();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			final JComponent source = (JComponent) e.getSource();
			final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();

			/*
			 * Only respond to releases that happen over this component. The
			 * default is to respond to releases if the press occurred in this
			 * component. This seems to be a Java bug, but I can't find any kind
			 * of complaint for it. Either way, we want this behaviour, not the
			 * default. - remiller
			 */
			if (!source.contains(mouseLoc.x - source.getLocationOnScreen().x,
					mouseLoc.y - source.getLocationOnScreen().y))
				return;

			final String typeText = this.getTypeKeyword();

			boolean selected = TypeDialogBuilder.this.typesToSelected
					.get(typeText);

			selectType(typeText, !selected);
		}
	}
}
