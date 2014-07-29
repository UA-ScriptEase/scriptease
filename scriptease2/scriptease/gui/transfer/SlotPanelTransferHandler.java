package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.StoryComponentUtils;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.SlotPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.semodel.SEModelManager;

/**
 * SlotPanelTransferHandler is responsible for the importing of BindingWidgets
 * into SlotPanels, and handles other SlotPanel drag and drop logic including
 * cases where StoryComponentPanels are being dragged over it and should be
 * redirected to the parent block instead.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class SlotPanelTransferHandler extends BindingWidgetTransferHandler {

	// Singleton instance of the TransferHandler.
	private static final SlotPanelTransferHandler instance = new SlotPanelTransferHandler();

	public static DataFlavor SlotPanelFlavour;

	public static SlotPanelTransferHandler getInstance() {
		return instance;
	}

	protected SlotPanelTransferHandler() {
		if (SlotPanelTransferHandler.SlotPanelFlavour == null) {
			try {
				SlotPanelFlavour = new DataFlavor(
						DataFlavor.javaJVMLocalObjectMimeType + ";class="
								+ SlotPanel.class.getCanonicalName());

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates and returns a Transferable from the Slot panel that was dragged.
	 */
	@Override
	protected Transferable createTransferable(JComponent source) {
		if (!(source instanceof SlotPanel)) {
			throw new IllegalArgumentException(
					"The given JComponent was not a Slot Panel.");
		}

		return new SlotPanelTransferable((SlotPanel) source);
	}

	/**
	 * Called whenever something is dragged over a component that has
	 * SlotPanelTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * whether or not this is a valid drop location for the drag.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		boolean canImport = false;

		final Component destinationComponent = support.getComponent();

		// Check the destination.
		// If the destination component is a SlotPanel, and the Transferable
		// is a SlotPanelTransferable.
		if (destinationComponent instanceof SlotPanel
				&& support
						.isDataFlavorSupported(BindingWidgetTransferHandler.KnowItBindingFlavor)) {

			final SlotPanel slotPanel;
			final KnowIt knowIt;
			final KnowItBinding sourceBinding;

			// Get the KnowItBinding being transferred.
			sourceBinding = this.extractBinding(support);

			// Get the destination KnowIt
			slotPanel = (SlotPanel) destinationComponent;
			knowIt = slotPanel.getKnowIt();

			// Special case for KnowItBindingUninitialized - they
			// shouldn't be dragged into their own referenced KnowIt
			if (sourceBinding instanceof KnowItBindingUninitialized) {
				final KnowItBindingUninitialized uninit = (KnowItBindingUninitialized) sourceBinding;
				if (uninit.getValue() == knowIt)
					return false;

				// the destinationKnowIt should also be a child of the component
				// that has the value the KnowItBindingUninitialized is
				// referencing.

				StoryComponent owner = uninit.getValue().getOwner();
				while (!(owner instanceof ComplexStoryComponent))
					owner = owner.getOwner();

				if (owner instanceof ComplexStoryComponent) {
					final ComplexStoryComponent complex = (ComplexStoryComponent) owner;

					final Collection<StoryComponent> descendants;
					descendants = StoryComponentUtils
							.getAllDescendants(complex);

					StoryComponent destOwner = knowIt.getOwner();
					while (!(destOwner instanceof ComplexStoryComponent))
						destOwner = destOwner.getOwner();

					if (!descendants.contains(destOwner))
						return false;
				}
			}

			// Check that the KnowItBinding type matches the destination
			// KnowIt
			if (sourceBinding != null && knowIt != null) {
				if (sourceBinding.compatibleWith(knowIt)) {
					canImport = true;
				}
			}
		}

		// Special case - to handle where effects, descriptions, and controls
		// can be dragged over binding widgets in order to get re-directed to
		// their parent block.
		if (!canImport)
			canImport = StoryComponentTransferUtils.canImportToParent(support);

		return canImport;
	}

	/**
	 * Called when a drop is detected on a component that has
	 * SlotPanelTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * true if the import was successful, and false if the import failed.
	 */
	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			// Make sure this import is legal.
			return false;
		} else if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			/*
			 * Some other component is being dragged over the SlotPanel but of
			 * course, it isn't a binding widget and shouldn't fit the slot - so
			 * lets handle it.
			 */
			return StoryComponentTransferUtils.importToParent(support);
		}

		final SlotPanel slotPanel;
		final KnowIt knowIt;
		final KnowItBinding sourceBinding;

		slotPanel = (SlotPanel) support.getComponent();
		knowIt = slotPanel.getKnowIt();

		// Get the KnowItBindingWidget being transferred.
		sourceBinding = this.extractBinding(support);

		// Set the history to the active model
		UndoManager.getInstance().setActiveHistory(
				SEModelManager.getInstance().getActiveModel());

		if (sourceBinding != null) {
			// Bind the KnowIt with the source binding.
			final KnowItBinding binding = knowIt.getBinding();
			if (binding != sourceBinding) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Set Binding " + sourceBinding);

				if (BindingWidgetTransferHandler.lastDragShiftDown)
					setGroupBindings(sourceBinding, knowIt, binding);
				knowIt.setBinding(sourceBinding);

				// Check if the source binding is disabled. If it is, we should
				// disable this component too.
				if (this.isWidgetOwnerDisabled(support)) {
					knowIt.disableOwner();
				}

				slotPanel.populate();

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
			return true;
		}

		return false;
	}
}
