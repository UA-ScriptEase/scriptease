package scriptease.gui.action.typemenus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
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
import scriptease.gui.component.ComponentFactory;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.TypeWidget;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.GameType;
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
class TypeDialogBuilder {
	private static final int PANEL_WIDTH;
	private static final Dimension MAX_PANEL_SIZE;
	private static final Dimension MIN_PANEL_SIZE;
	private static final Dimension MAX_SCROLLPANE_SIZE;

	static {
		PANEL_WIDTH = 485;
		MAX_PANEL_SIZE = new Dimension(PANEL_WIDTH, 5000);
		MIN_PANEL_SIZE = new Dimension(PANEL_WIDTH, 150);
		MAX_SCROLLPANE_SIZE = new Dimension(PANEL_WIDTH + 100, 500);
	}

	private final Map<GameType, Boolean> typesToSelected;
	private final List<CheckBoxPanel> checkBoxPanels;
	private final JButton allButton;

	private boolean accepting = false;

	private Runnable closeAction;

	/**
	 * Creates a new TypeSelectionDialogBuilder. The passed okAction will run
	 * when "Ok" is pressed on the dialog.
	 * 
	 * @param closeAction
	 */
	protected TypeDialogBuilder(Collection<GameType> types, Runnable closeAction) {
		this.closeAction = closeAction;
		this.allButton = ComponentFactory.buildFlatButton(ScriptEaseUI.SE_BLUE,
				"Deselect All");
		this.typesToSelected = new HashMap<GameType, Boolean>();
		this.checkBoxPanels = new ArrayList<CheckBoxPanel>();

		for (GameType type : types) {
			this.typesToSelected.put(type, Boolean.TRUE);
		}
	}

	/**
	 * Sets the close action to the passed runnable. This will run when the
	 * dialog box is closed.
	 * 
	 * @param closeAction
	 */
	protected void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	/**
	 * Returns the dialog box that represents Type Selection.
	 * 
	 * @return
	 */
	protected JDialog buildTypeDialog() {
		final JScrollPane typesPanel;
		final JPanel content;
		final JButton okButton;
		final JButton cancelButton;
		final JSeparator separator;
		final JDialog typeDialog;

		final Map<GameType, Boolean> previousSelected;

		final GroupLayout layout;

		typeDialog = WindowFactory.getInstance().buildDialog("Type Selection");

		typesPanel = this.buildTypesPanel();
		content = new JPanel();
		okButton = ComponentFactory
				.buildFlatButton(ScriptEaseUI.SE_GREEN, "Ok");
		cancelButton = ComponentFactory.buildFlatButton(
				ScriptEaseUI.SE_BURGUNDY, "Cancel");
		separator = new JSeparator(SwingConstants.HORIZONTAL);

		layout = new GroupLayout(content);

		previousSelected = new HashMap<GameType, Boolean>(this.typesToSelected);

		typeDialog.setUndecorated(true);

		content.setBackground(ScriptEaseUI.SECONDARY_UI);
		separator.setBackground(ScriptEaseUI.PRIMARY_UI);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

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

		content.setLayout(layout);

		layout.setHorizontalGroup(layout
				.createParallelGroup()
				.addComponent(typesPanel)
				.addComponent(separator)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addComponent(this.allButton)
								.addComponent(cancelButton)
								.addComponent(okButton)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(typesPanel)
				.addComponent(separator)
				.addGroup(
						layout.createParallelGroup()
								.addComponent(this.allButton)
								.addComponent(cancelButton)
								.addComponent(okButton)));

		typeDialog.setContentPane(content);
		typeDialog.pack();
		typeDialog.setSize(TypeDialogBuilder.PANEL_WIDTH,
				typeDialog.getSize().height);
		typeDialog.setResizable(false);
		typeDialog.setLocationRelativeTo(typeDialog.getParent());

		okButton.requestFocus();

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
		final SEModel model = SEModelManager.getInstance().getActiveModel();

		typesPanel = new JPanel();

		checkBoxPanels = new ArrayList<CheckBoxPanel>();

		if (model == null)
			return new JScrollPane();

		// create a menu item for each type
		for (GameType type : this.typesToSelected.keySet()) {
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
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getType()
						.getName(), o2.getType().getName());
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
			final GameType type;

			type = checkBoxPanel.getType();

			if (type.hasEnum()) {
				listCBPanels.add(checkBoxPanel);
			} else if (type.getSlots().size() > 0) {
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
			label.setForeground(ScriptEaseUI.PRIMARY_UI);
			label.setOpaque(false);

			gameConstantPanel.setOpaque(false);

			typesPanel.add(label);
			typesPanel.add(gameConstantPanel);
		}

		if (gameObjectPanel != null) {
			final JLabel label = new JLabel("Game Objects");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			label.setForeground(ScriptEaseUI.PRIMARY_UI);
			label.setOpaque(false);

			gameObjectPanel.setOpaque(false);

			typesPanel.add(label);
			typesPanel.add(gameObjectPanel);
		}

		if (listPanel != null) {
			final JLabel label = new JLabel("Lists");

			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			label.setForeground(ScriptEaseUI.PRIMARY_UI);
			label.setOpaque(false);

			listPanel.setOpaque(false);

			typesPanel.add(label);
			typesPanel.add(listPanel);
		}

		this.updateAllButton();

		final JScrollPane typeScrollPane;

		typeScrollPane = new JScrollPane(typesPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		typesPanel.setBackground(ScriptEaseUI.SECONDARY_UI);

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
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		panel.setOpaque(false);
		subPanel1.setOpaque(false);
		subPanel2.setOpaque(false);
		subPanel3.setOpaque(false);

		panel.add(subPanel1);
		panel.add(subPanel2);
		panel.add(subPanel3);

		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 10, 0,
				ScriptEaseUI.SECONDARY_UI));

		for (Component component : panel.getComponents()) {
			final JComponent subPanel = (JComponent) component;

			subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
			subPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		}

		int objectCount = 0;
		int size = cbPanels.size();

		int firstLimit = size / 3;
		int secondLimit = 2 * size / 3;
		
		if(size % 3 == 0)  {
			firstLimit--;
			secondLimit--;
		}

		for (CheckBoxPanel gameObjectCBPanel : cbPanels) {
			if (objectCount <= firstLimit)
				subPanel1.add(gameObjectCBPanel);
			else if (objectCount <= secondLimit)
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
	protected Collection<GameType> getSelectedTypes() {
		final List<GameType> checked = new ArrayList<GameType>();

		for (GameType key : this.typesToSelected.keySet()) {
			final Boolean isAccepted = this.typesToSelected.get(key);

			if (isAccepted)
				checked.add(key);
		}
		return checked;
	}

	protected void deselectAll() {
		for (GameType type : this.typesToSelected.keySet()) {
			selectType(type, false);
		}
	}

	protected Collection<GameType> getTypes() {
		return this.typesToSelected.keySet();
	}

	/**
	 * Selects a collection of types.
	 * 
	 * @param types
	 * @param isSelected
	 */
	protected void selectTypes(Collection<GameType> types, boolean isSelected) {
		for (GameType type : types)
			selectType(type, isSelected);
	}

	/**
	 * Selects a collection of types based on keywords
	 * 
	 * @param types
	 * @param isSelected
	 */
	protected void selectTypesByKeyword(Collection<String> keywords,
			boolean isSelected) {
		for (GameType type : this.typesToSelected.keySet()) {
			if (keywords.contains(type.getName())) {
				selectType(type, isSelected);
			}
		}
	}

	/**
	 * Sets the passed type to the passed boolean value, and deals with the
	 * "all" button.
	 * 
	 * @param type
	 * @param isSelected
	 */
	protected void selectType(GameType type, boolean isSelected) {
		this.typesToSelected.put(type, Boolean.valueOf(isSelected));

		for (CheckBoxPanel panel : this.checkBoxPanels) {
			if (panel.getType().equals(type))
				panel.setSelected(isSelected);
		}

		this.updateAllButton();
	}

	protected boolean isAllSelected() {
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
		private final TypeWidget typeWidget;
		private final GameType typeKeyword;

		private CheckBoxPanel(GameType type) {
			super();
			final Dimension PREFERRED_PANEL_SIZE = new Dimension(100, 25);
			final Dimension MAX_PANEL_SIZE = new Dimension(2400, 25);
			final Dimension MIN_PANEL_SIZE = new Dimension(10, 25);

			final String typeName = type.getName();

			final JLabel typeLabel;
			final JPanel typePanel;

			this.typeKeyword = type;
			this.typeWidget = ScriptWidgetFactory.buildTypeWidget(typeName);

			typeLabel = new JLabel(" " + typeName);
			typePanel = new JPanel();

			typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));
			typePanel.add(typeWidget);
			typePanel.add(typeLabel);

			typeLabel.setForeground(ScriptEaseUI.PRIMARY_UI);

			typePanel.setOpaque(false);

			this.setLayout(new BorderLayout());

			this.setPreferredSize(PREFERRED_PANEL_SIZE);
			this.setMinimumSize(MIN_PANEL_SIZE);
			this.setMaximumSize(MAX_PANEL_SIZE);

			this.setToolTipText(typeName);

			this.add(typePanel, BorderLayout.CENTER);

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
							CheckBoxPanel.this.getBackground(), 1.4));
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

		private void resetUI() {
			if (TypeDialogBuilder.this.typesToSelected.get(this.typeKeyword)) {
				this.setBackground(ScriptEaseUI.SE_BLUE);
				this.typeWidget.setBackground(GUIOp.scaleColour(Color.GREEN,
						1.2));
				this.typeWidget.setSelected(true);
			} else {
				this.setBackground(ScriptEaseUI.SE_BLACK);
				this.typeWidget.setBackground(ScriptEaseUI.TERTIARY_UI);
				this.typeWidget.setSelected(false);
			}
		}

		/**
		 * Returns the keyword of the type. This should not be visible to the
		 * user and should be unique.
		 * 
		 * @return
		 */
		private GameType getType() {
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
