package scriptease.gui.action.typemenus;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.translator.io.model.GameType;
import scriptease.util.ListOp;

/**
 * The Action for showing the Select Type selection dialog. This action is added
 * to a button to make the type selection dialog pop up. An action must be set
 * either in the constructor or with {@link #setAction(Runnable)} so that the
 * type dialog knows what to do when closed.
 * 
 * @see {@link TypeDialogBuilder}
 * 
 * @author remiller
 * @author kschenk
 */
@SuppressWarnings("serial")
public final class TypeAction extends AbstractAction {

	private Runnable action;
	private TypeDialogBuilder typeBuilder;

	/**
	 * Creates a new instance of the action for selecting the types.
	 * 
	 */
	public TypeAction() {
		this(null);
	}

	/**
	 * Creates a new instance of the action for selecting the types
	 * 
	 * @param action
	 *            the runnable action to be performed when the type selection
	 *            changes
	 */
	public TypeAction(Runnable action) {
		super("Type");

		this.setAction(action);

		final SEModelObserver modelObserver;

		modelObserver = new SEModelObserver() {
			@Override
			public void modelChanged(SEModelEvent event) {
				if (event.getEventType() == SEModelEvent.Type.ACTIVATED) {
					TypeAction.this.updateEnabledState();

					final SEModel model = event.getPatternModel();

					TypeAction.this.typeBuilder = new TypeDialogBuilder(
							model.getTypes(), TypeAction.this.action);
				}
			}
		};

		SEModelManager.getInstance().addSEModelObserver(this, modelObserver);

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

		if (this.typeBuilder != null) {
			this.typeBuilder.setCloseAction(newAction);
		} else {
			final SEModel model = SEModelManager.getInstance().getActiveModel();

			if (model != null)
				this.typeBuilder = new TypeDialogBuilder(model.getTypes(),
						newAction);
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
	 * Sets selection state of types based on the keywords entered.
	 * 
	 * @param keywords
	 * @param isSelected
	 */
	public void selectTypesByKeyword(Collection<String> keywords,
			boolean isSelected) {
		this.typeBuilder.selectTypesByKeyword(keywords, isSelected);
	}

	/**
	 * Deselects all types in the type builder.
	 */
	public void deselectAll() {
		this.typeBuilder.deselectAll();
	}

	/**
	 * Returns the TypeSelectionDialogBuilder currently associated with the
	 * ShowTypeMenuAction.
	 * 
	 * @return
	 */
	public TypeDialogBuilder getTypeSelectionDialogBuilder() {
		return this.typeBuilder;
	}

	/**
	 * Updates the name of this action to the amount of types shown. One type
	 * selected will display the selected types name, all types will display
	 * "All Types", and no types will display "No Types". Any number of types
	 * will display the number of types plus " Types".
	 */
	public void updateName() {
		if (this.typeBuilder == null)
			return;

		final int selectedCount = this.typeBuilder.getSelectedTypes().size();

		String name;

		if (selectedCount <= 0) {
			name = "No Types";
		} else if (selectedCount >= this.typeBuilder.getTypes().size()) {
			name = "All Types";
		} else if (selectedCount == 1) {
			// show just the first one
			name = ListOp.head(this.typeBuilder.getSelectedTypes()).getName();
		} else {
			// show the number of selected types
			name = selectedCount + " Types";
		}

		this.putValue(NAME, name);
	}

	/**
	 * Returns the current selected types. Forwarded method to TypeBuilder.
	 * 
	 * @return
	 */
	public Collection<GameType> getSelectedTypes() {
		return this.typeBuilder.getSelectedTypes();
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
		return SEModelManager.getInstance().hasActiveModel();
	}
}
