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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import scriptease.gui.WindowFactory;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.TypeWidget;
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
	private static final int PANEL_WIDTH;
	private static final Dimension MAX_PANEL_SIZE;
	private static final Dimension MIN_PANEL_SIZE;
	private static final Dimension MAX_SCROLLPANE_SIZE;

	static {
		PANEL_WIDTH = 450;
		MAX_PANEL_SIZE = new Dimension(PANEL_WIDTH, 5000);
		MIN_PANEL_SIZE = new Dimension(PANEL_WIDTH, 150);
		MAX_SCROLLPANE_SIZE = new Dimension(PANEL_WIDTH + 100, 500);
	}

	private final Map<String, Boolean> typesToSelected;
	private final List<CheckBoxPanel> checkBoxPanels;
	private final JButton allButton;

	private boolean accepting = false;

	private Runnable closeAction;

	/**
	 * Creates a new TypeSelectionDialogBuilder, intializing the variables.
	 */
	public TypeDialogBuilder(Translator translator) {
		this.allButton = new JButton("Deselect All");
		this.typesToSelected = new HashMap<String, Boolean>();
		this.checkBoxPanels = new ArrayList<CheckBoxPanel>();

		final Collection<String> types;
		// Create the translator and populate lists.

		if (translator == null)
			return;

		types = translator.getGameTypeManager().getKeywords();

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
	public TypeDialogBuilder(Translator translator, Runnable closeAction) {
		this(translator);
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
		final JButton okButton;
		final JButton cancelButton;
		final JSeparator separator;
		final JDialog typeDialog;

		final Map<String, Boolean> previousSelected;

		final GroupLayout groupLayout;

		typeDialog = WindowFactory.getInstance().buildDialog("Type Selection");

		typesPanel = this.buildTypesPanel();
		content = new JPanel();
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");
		separator = new JSeparator(SwingConstants.HORIZONTAL);

		groupLayout = new GroupLayout(content);

		previousSelected = new HashMap<String, Boolean>(this.typesToSelected);

		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);

		// Set up the action listeners for the buttons.
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TypeDialogBuilder.this.accepting = true;
				if (TypeDialogBuilder.this.closeAction != null)
					TypeDialogBuilder.this.closeAction.run();
				typeDialog.dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				typeDialog.dispose();
			}
		});

		typeDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (!accepting) {
					TypeDialogBuilder.this.typesToSelected.clear();
					TypeDialogBuilder.this.typesToSelected
							.putAll(previousSelected);
				} else
					accepting = false;
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
								.addComponent(okButton)
								.addComponent(cancelButton)));

		groupLayout.setVerticalGroup(groupLayout
				.createSequentialGroup()
				.addComponent(typesPanel)
				.addComponent(separator)
				.addGroup(
						groupLayout.createParallelGroup()
								.addComponent(this.allButton)
								.addComponent(okButton)
								.addComponent(cancelButton)));

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
		final JPanel typesPanel;

		final List<CheckBoxPanel> checkBoxPanels;
		final GameTypeManager typeManager;

		typesPanel = new JPanel();

		checkBoxPanels = new ArrayList<CheckBoxPanel>();
		typeManager = TranslatorManager.getInstance()
				.getActiveGameTypeManager();

		if (typeManager == null)
			return new JScrollPane();

		// create a menu item for each type
		for (String type : this.typesToSelected.keySet()) {
			final CheckBoxPanel checkBoxPanel;
			final Boolean typeBool;

			checkBoxPanel = new CheckBoxPanel(type);
			typeBool = this.typesToSelected.get(type);

			this.checkBoxPanels.add(checkBoxPanel);

			if (typeBool != null)
				checkBoxPanel.setSelected(typeBool.booleanValue());

			checkBoxPanels.add(checkBoxPanel);
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

		final JPanel gameConstantPanel;
		final JPanel gameObjectPanel;
		final JPanel listPanel;

		gameConstantPanel = this.createParentPanel(gameConstantCBPanels);
		gameObjectPanel = this.createParentPanel(gameObjectCBPanels);
		listPanel = this.createParentPanel(listCBPanels);

		if (gameConstantPanel != null) {
			final JLabel label = new JLabel("Game Constants");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);

			typesPanel.add(label);
			typesPanel.add(gameConstantPanel);
		}

		if (gameObjectPanel != null) {
			final JLabel label = new JLabel("Game Objects");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);

			typesPanel.add(label);
			typesPanel.add(gameObjectPanel);
		}

		if (listPanel != null) {
			final JLabel label = new JLabel("Lists");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);

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

	private JPanel createParentPanel(Collection<CheckBoxPanel> cbPanels) {

		final JPanel panel;
		final JPanel subPanel1;
		final JPanel subPanel2;
		final JPanel subPanel3;

		panel = new JPanel();
		subPanel1 = new JPanel();
		subPanel2 = new JPanel();
		subPanel3 = new JPanel();

		panel.setMinimumSize(MIN_PANEL_SIZE);
		panel.setMaximumSize(MAX_PANEL_SIZE);
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		panel.add(subPanel1);
		panel.add(subPanel2);
		panel.add(subPanel3);

		for (Component component : panel.getComponents()) {
			final JComponent subPanel = (JComponent) component;

			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
			subPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		}

		int objectCount = 0;
		int size = cbPanels.size();
		for (CheckBoxPanel gameObjectCBPanel : cbPanels) {
			if (objectCount <= size / 3)
				subPanel1.add(gameObjectCBPanel);
			else if (objectCount <= 2 * size / 3)
				subPanel2.add(gameObjectCBPanel);
			else
				subPanel3.add(gameObjectCBPanel);
			objectCount++;
		}

		if (subPanel1.getComponents().length > 0) {
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);

			return panel;
		}

		return null;
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
	private class CheckBoxPanel extends JPanel {
		private final Color SELECTED_COLOUR = GUIOp.scaleWhite(
				Color.LIGHT_GRAY, 1.2);
		private final Color UNSELECTED_COLOUR = GUIOp.scaleWhite(
				Color.LIGHT_GRAY, 0.8);

		private final Color TYPE_WIDGET_SELECTED_COLOUR = GUIOp.scaleColour(
				Color.GREEN, 1.2);
		private final Color TYPE_WIDGET_UNSELECTED_COLOUR = Color.GRAY;

		// private final JCheckBox checkBox;
		private final TypeWidget typeWidget;
		private final String typeKeyword;

		private CheckBoxPanel(String typeKeyword) {
			super();
			final Dimension MAX_PANEL_SIZE = new Dimension(2400, 25);
			final Dimension MIN_PANEL_SIZE = new Dimension(10, 25);

			final String typeDisplayText;

			final JLabel typeLabel;
			final JPanel typePanel;

			typeDisplayText = TranslatorManager.getInstance()
					.getActiveGameTypeManager().getDisplayText(typeKeyword);

			this.typeKeyword = typeKeyword;
			this.typeWidget = ScriptWidgetFactory.getTypeWidget(typeKeyword);

			typeLabel = new JLabel(" " + typeDisplayText);
			typePanel = new JPanel();

			typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));
			typePanel.add(typeWidget);
			typePanel.add(typeLabel);

			typePanel.setOpaque(false);

			this.setLayout(new BorderLayout());

			this.setMinimumSize(MIN_PANEL_SIZE);
			this.setMaximumSize(MAX_PANEL_SIZE);

			this.add(typePanel, BorderLayout.CENTER);

			this.setOpaque(false);

			this.addMouseListener(new MouseListener() {
				@Override
				public void mousePressed(MouseEvent e) {
					CheckBoxPanel.this.setBackground(GUIOp.scaleColour(
							CheckBoxPanel.this.getBackground(), 0.7));
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					e.consume();
				}

				// On hover behaviour
				@Override
				public void mouseEntered(MouseEvent e) {
					CheckBoxPanel.this.setBackground(GUIOp.scaleWhite(
							CheckBoxPanel.this.getBackground(), 1.1));
				}

				// On exit hover behaviour
				@Override
				public void mouseExited(MouseEvent e) {
					CheckBoxPanel.this.resetUI();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					final JComponent source = (JComponent) e.getSource();
					final Point mouseLoc = MouseInfo.getPointerInfo()
							.getLocation();
					/*
					 * Only respond to releases that happen over this component.
					 * The default is to respond to releases if the press
					 * occurred in this component. This seems to be a Java bug,
					 * but I can't find any kind of complaint for it. Either
					 * way, we want this behaviour, not the default. - remiller
					 */
					if (!source.contains(
							mouseLoc.x - source.getLocationOnScreen().x,
							mouseLoc.y - source.getLocationOnScreen().y))
						return;

					final boolean selected;

					selected = TypeDialogBuilder.this.typesToSelected
							.get(CheckBoxPanel.this.typeKeyword);

					TypeDialogBuilder.this.selectType(
							CheckBoxPanel.this.typeKeyword, !selected);
				}
			});

		}

		@Override
		protected void paintComponent(Graphics grphcs) {
			final Graphics2D g2d;

			g2d = (Graphics2D) grphcs;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			GradientPaint gp = new GradientPaint(0, 0, this.getBackground(), 0,
					getHeight(), GUIOp.scaleWhite(this.getBackground(), 1.3));

			g2d.setPaint(gp);

			g2d.fillRect(0, 0, getWidth(), getHeight());

			super.paintComponent(grphcs);

		}

		private void resetUI() {
			if (TypeDialogBuilder.this.typesToSelected.get(this.typeKeyword)) {
				this.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(Color.gray),
						BorderFactory.createEmptyBorder(1, 1, 1, 1)));
				this.setBackground(SELECTED_COLOUR);
				this.typeWidget.setBackground(TYPE_WIDGET_SELECTED_COLOUR);
				this.typeWidget.setSelected(true);
			} else {
				this.setBorder(BorderFactory.createRaisedBevelBorder());
				this.setBackground(UNSELECTED_COLOUR);
				this.typeWidget.setBackground(TYPE_WIDGET_UNSELECTED_COLOUR);
				this.typeWidget.setSelected(false);
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
			this.resetUI();
		}
	}
}
