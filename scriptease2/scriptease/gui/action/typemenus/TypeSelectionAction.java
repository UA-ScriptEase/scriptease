package scriptease.gui.action.typemenus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.gui.dialog.TypeSelectionDialogBuilder;
import scriptease.model.LibraryManager;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * The Action for showing the Select Type selection dialog. This action is added
 * to a button to make the type selection dialog pop up. An action must be set
 * either in the constructor or with {@link #setAction(Runnable)} so that the
 * type dialog knows what to do when closed.
 * 
 * @see {@link #ShowTypeMenuAction}
 * 
 * @author remiller
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class TypeSelectionAction extends AbstractAction implements
		LibraryManagerObserver, TranslatorObserver {

	private Runnable action;
	private TypeSelectionDialogBuilder typeBuilder;

	public TypeSelectionAction() {
		this(null);
	}

	/**
	 * Creates a new instance of the action for selecting the types
	 * 
	 * @param action
	 *            the runnable action to be performed when the type selection
	 *            changes
	 * @param libraries
	 *            the libraries to go through.
	 */
	public TypeSelectionAction(Runnable action) {
		super();

		setAction(action);
		// add self as observers of the translator and library
		LibraryManager.getInstance().addLibraryManagerObserver(this);
		TranslatorManager.getInstance().addTranslatorObserver(this);

		this.updateName();
		this.putValue(SHORT_DESCRIPTION, "Select Type");
		this.updateEnabledState();
	}

	/**
	 * Sets the action for pressing "OK" in the TypeSelectionDialog. This method
	 * decorates the passed action so that the name of the ShowTypeMenuAction is
	 * updated when the OK button is pressed.
	 * 
	 * @param action
	 */
	public void setAction(final Runnable action) {
		Runnable newAction = new Runnable() {
			@Override
			public void run() {
				if (action != null)
					action.run();
				updateName();
			}
		};

		this.action = newAction;

		if (this.typeBuilder != null)
			this.typeBuilder.setCloseAction(newAction);
		else {
			this.typeBuilder = new TypeSelectionDialogBuilder(newAction);
		}

		this.updateName();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JDialog typeDialog;

		typeDialog = this.typeBuilder.buildTypeDialog();
		typeDialog.setVisible(true);
	}

	/**
	 * Returns the TypeSelectionDialogBuilder currently associated with the
	 * ShowTypeMenuAction.
	 * 
	 * @return
	 */
	public TypeSelectionDialogBuilder getTypeSelectionDialogBuilder() {
		return this.typeBuilder;
	}

	/**
	 * Updates the name of this action to the amount of types shown. One type
	 * selected will display the selected types name, all types will display
	 * "All Types", and no types will display "No Types". Any number of types
	 * will display the number of types plus " Types".
	 */
	public void updateName() {
		final int selectedCount = this.typeBuilder.getSelectedTypes().size();
		String name;
		final Translator activeTranslator = TranslatorManager.getInstance()
				.getActiveTranslator();

		if (selectedCount <= 0) {
			name = "No Types";
		} else if (selectedCount >= activeTranslator.getGameTypeManager()
				.getGameTypes().size()) {
			name = "All Types";
		} else if (selectedCount == 1) {
			// show just the first one
			name = activeTranslator.getGameTypeManager().getDisplayText(
					this.typeBuilder.getSelectedTypes().iterator().next());
		} else {
			// show the number of selected types
			name = selectedCount + " Types";
		}

		this.putValue(NAME, name);
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
					if (event != null)
						if (event.getEventType() == LibraryEvent.STORYCOMPONENT_ADDED
								|| event.getEventType() == LibraryEvent.STORYCOMPONENT_REMOVED) {
							TypeSelectionAction.this.updateEnabledState();
						}
				}
			}
		});
	}

	@Override
	public void translatorLoaded(Translator newTranslator) {
		this.updateEnabledState();
		/*
		 * All types default to on. Future feature: this could potentially be
		 * saved to the user preferences file for reloading. - remiller
		 */
		this.typeBuilder = new TypeSelectionDialogBuilder(this.action);
		this.updateName();
	}
}
