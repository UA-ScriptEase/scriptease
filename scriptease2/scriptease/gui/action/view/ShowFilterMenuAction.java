package scriptease.gui.action.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.controller.observer.LibraryEvent;
import scriptease.controller.observer.LibraryManagerEvent;
import scriptease.controller.observer.LibraryManagerObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.LibraryManager;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * The Action for showing the Filter by Type pop-up menu underneath the
 * component that was pressed. The Filter by Type menu is a list of
 * JCheckBoxMenuItems, one for every known data type. If that type is checked,
 * then atoms containing a parameter of that type will appear.<br>
 * <br>
 * This Action implements the singleton design pattern.
 * 
 * @author remiller
 */
@SuppressWarnings("serial")
public final class ShowFilterMenuAction extends AbstractAction implements
		LibraryManagerObserver, TranslatorObserver {
	private final Map<String, Boolean> typesToAcceptance = new HashMap<String, Boolean>();

	private Runnable action;
	private JCheckBoxMenuItem allButton;

	public ShowFilterMenuAction() {
		this(null);
	}

	/**
	 * Creates a new instance of the action for filtering the types
	 * 
	 * @param action
	 *            the runnable action to be performed when the type selection
	 *            changes
	 * @param libraries
	 *            the libraries to filter through.
	 */
	public ShowFilterMenuAction(Runnable action) {
		super();
		this.action = action;

		// add self as observers of the translator and library
		LibraryManager.getInstance().addLibraryManagerListener(this);
		TranslatorManager.getInstance().addTranslatorObserver(this);

		this.updateName();
		this.putValue(SHORT_DESCRIPTION, "Filter by Type");
		this.updateEnabledState();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component source = (Component) e.getSource();
		final JPopupMenu menu;

		menu = buildTypeFilterMenu();
		menu.show(source, source.getWidth(), 0);
	}

	private JPopupMenu buildTypeFilterMenu() {
		final JPopupMenu menu;
		menu = new JPopupMenu("Filter by Type");
		
		this.populateMenu(menu);
		return menu;
	}
	


	private void populateMenu(final JPopupMenu menu) {
		final List<JCheckBoxMenuItem> buttons;
		JCheckBoxMenuItem item;
		ItemListener selectionUpdater;
		final MenuVisibilityHandler menuVisHandler;

		buttons = new ArrayList<JCheckBoxMenuItem>();
		this.allButton = new JCheckBoxMenuItem(new SelectAllAction(buttons));

		/*
		 * ensures that the menu doesn't disappear when the menu items are
		 * selected. This allows users to select multiple types at a time,
		 * without it disappearing every time. - remiller
		 */
		menuVisHandler = new MenuVisibilityHandler(menu);

		this.allButton.addActionListener(menuVisHandler);

		// create a menu item for each type
		Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		if (activeTranslator != null) {
			GameTypeManager typeManager = activeTranslator.getGameTypeManager();
			for (String type : typeManager.getKeywords()) {
				item = new JCheckBoxMenuItem(typeManager.getDisplayText(type));
				selectionUpdater = new SelectionUpdater(type);

				Boolean typeBool = this.typesToAcceptance.get(type);
				if (typeBool != null)
					item.setSelected(typeBool.booleanValue());

				// TODO: icons for types in the type filter list
				item.setIcon(null);

				item.addActionListener(menuVisHandler);
				item.addItemListener(selectionUpdater);

				buttons.add(item);
			}
		}

		// sort the menu items
		Collections.sort(buttons, new Comparator<JCheckBoxMenuItem>() {
			@Override
			public int compare(JCheckBoxMenuItem item1, JCheckBoxMenuItem item2) {
				return item1.getText().compareTo(item2.getText());
			}
		});

		// fill the menu
		menu.add(this.allButton);
		menu.addSeparator();
		// I wish there was an addAll(). -remiller
		for (JCheckBoxMenuItem newItem : buttons) {
			menu.add(newItem);
		}
	}

	private boolean isAllSelected() {
		return this.countAcceptedTypes() >= this.typesToAcceptance.size();
	}

	/**
	 * Updates the name of this action
	 */
	public void updateName() {
		final int selectedCount = this.countAcceptedTypes();
		final int typesCount = this.typesToAcceptance.size();
		String name;
		final StoryModel activeModel = StoryModelPool.getInstance()
				.getActiveModel();

		if (selectedCount >= typesCount) {
			name = "All Types";
		} else if (selectedCount == 1) {
			// show just the first one
			name = activeModel.getTranslator().getGameTypeManager()
					.getDisplayText(this.getAcceptedTypes().iterator().next());

		} else if (selectedCount <= 0) {
			name = "No Types";
		} else {
			// show the number of selected types
			name = selectedCount + " Only";
		}

		this.putValue(NAME, name);
	}

	/**
	 * Gets a collection of selected type check boxes
	 * 
	 * @return a collection of selected type check boxes
	 */
	public Collection<String> getAcceptedTypes() {
		final List<String> checked = new ArrayList<String>();
		Boolean isAccepted;

		for (String key : this.typesToAcceptance.keySet()) {
			isAccepted = this.typesToAcceptance.get(key);

			if (isAccepted.booleanValue())
				checked.add(key);
		}

		return checked;
	}

	/**
	 * Counts how many types we're including in the filter
	 * 
	 * @return
	 */
	private int countAcceptedTypes() {
		return this.getAcceptedTypes().size();
	}

	/**
	 * Updates the action to either be enabled or disabled depending on the
	 * definition of {@link #isLegal()}.
	 */
	protected final void updateEnabledState() {
		this.setEnabled(this.isLegal());
	}

	/**
	 * Determines if this action is a legal action to perform at the current
	 * time. This information is used to determine if it should be enabled
	 * and/or visible.<br>
	 * <br>
	 * 
	 * @return True if this action is legal.
	 */
	protected boolean isLegal() {
		Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();
		return (activeTranslator != null
				&& !activeTranslator.getGameTypeManager().getKeywords()
						.isEmpty() && LibraryManager.getInstance()
				.hasLibraries());
	}

	/**
	 * Handles changes to the LibraryManager
	 */
	@Override
	public void modelChanged(final LibraryManagerEvent managerEvent) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (managerEvent.getEventType() == LibraryManagerEvent.LIBRARYMODEL_CHANGED) {
					final LibraryEvent event = managerEvent.getEvent();
					if (event.getEventType() == LibraryEvent.STORYCOMPONENT_ADDED
							|| event.getEventType() == LibraryEvent.STORYCOMPONENT_REMOVED) {
						ShowFilterMenuAction.this.updateEnabledState();
					}
				}
			}
		});
	}

	@Override
	public void translatorLoaded(Translator newTranslator) {
		this.updateEnabledState();

		/*
		 * All type filters default to on. Future feature: this could
		 * potentially be saved to the user preferences file for reloading. -
		 * remiller
		 */
		this.typesToAcceptance.clear();
		if (newTranslator != null)
			for (String type : newTranslator.getGameTypeManager().getKeywords()) {
				this.typesToAcceptance.put(type, Boolean.TRUE);
			}

		this.updateName();
	}

	/**
	 * Updates the selection map and selected items as necessary
	 * 
	 * @author remiller
	 */
	private class SelectionUpdater implements ItemListener {
		private final String type;

		private SelectionUpdater(String type) {
			this.type = type;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			boolean isSelected = e.getStateChange() == ItemEvent.SELECTED;
			selectType(this.type, isSelected);
		}
	}

	public void selectTypes(Collection<String> types, boolean isSelected) {
		for (String type : types)
			selectType(type, isSelected);
	}

	public void selectType(String type, boolean isSelected) {
		if (this.allButton != null) {
			boolean allAccepted = countAcceptedTypes() + 1 >= typesToAcceptance
					.size();
			this.allButton.setSelected(isSelected && allAccepted);
		}

		typesToAcceptance.put(type, Boolean.valueOf(isSelected));

		updateName();
		// run the provided update action
		if (action != null)
			action.run();
	}

	/**
	 * Selects/deselects all of the other items in the type filter list.
	 * 
	 * @author remiller
	 */
	private class SelectAllAction extends AbstractAction {
		private final Collection<JCheckBoxMenuItem> buttons;

		private SelectAllAction(Collection<JCheckBoxMenuItem> buttons) {
			super(Il8nResources.getString("All"));
			this.buttons = buttons;

			this.putValue(SELECTED_KEY, isAllSelected());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean isSelected;

			isSelected = ((AbstractButton) e.getSource()).isSelected();

			// select or deselect all of the other buttons
			for (AbstractButton button : this.buttons) {
				button.setSelected(isSelected);
			}
		}
	}

	/**
	 * Ensures that the menu doesn't disappear when the menu items are selected.
	 * This allows users to select multiple types at a time, without it
	 * disappearing every time. The menu will instead disappear when the mouse
	 * leaves it.
	 * 
	 * @author remiller
	 */
	private final class MenuVisibilityHandler implements ActionListener {
		private final JPopupMenu menu;

		private MenuVisibilityHandler(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.menu.setVisible(true);
		}
	}

	/**
	 * Set the runnable action to be performed when the type selection changes
	 * 
	 * @param action
	 */
	public void setSelectionChangedAction(Runnable action) {
		this.action = action;
	}
}
