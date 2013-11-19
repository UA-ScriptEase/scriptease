package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.SlotPanel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
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

			final SlotPanel destinationSlotPanel;
			final KnowIt destinationKnowIt;
			final KnowItBinding sourceBinding;

			// Get the KnowItBinding being transferred.
			sourceBinding = this.extractBinding(support);

			// Get the destination KnowIt
			destinationSlotPanel = (SlotPanel) destinationComponent;
			destinationKnowIt = (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(destinationSlotPanel
							.getBindingWidget().getParent());

			// Special case for KnowItBindingUninitialized - they
			// shouldn't be dragged into their own referenced KnowIt
			if (sourceBinding instanceof KnowItBindingUninitialized) {
				final KnowItBindingUninitialized uninit = (KnowItBindingUninitialized) sourceBinding;
				if (uninit.getValue() == destinationKnowIt)
					return false;
			}
			
			// Check that the KnowItBinding type matches the destination
			// KnowIt
			if (sourceBinding != null && destinationKnowIt != null) {
				if (sourceBinding.compatibleWith(destinationKnowIt)) {
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
		final SlotPanel destinationSlotPanel;
		final BindingWidget destinationWidget;
		final KnowIt destinationKnowIt;
		final KnowItBinding sourceBinding;

		// Make sure this import is legal.
		if (!canImport(support)) {
			return false;
		}

		// Some other component is being dragged over the SlotPanel
		// but of course, it isn't a binding widget and shouldn't fit
		// the slot - so lets handle it.
		if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			return StoryComponentTransferUtils.importToParent(support);
		}

		// Get the destination widget for the transfer.
		destinationSlotPanel = (SlotPanel) support.getComponent();
		destinationWidget = destinationSlotPanel.getBindingWidget();
		destinationKnowIt = (KnowIt) ScriptWidgetFactory
				.getEditedStoryComponent(destinationWidget.getParent());

		// Get the KnowItBindingWidget being transferred.
		sourceBinding = this.extractBinding(support);

		// Set the history to the active model
		UndoManager.getInstance().setActiveHistory(
				SEModelManager.getInstance().getActiveModel());

		if (sourceBinding != null) {
			// Bind the KnowIt with the source binding.
			final KnowItBinding binding = destinationKnowIt.getBinding();
			if (binding != sourceBinding) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Set Binding " + sourceBinding);

				if (BindingWidgetTransferHandler.lastDragShiftDown)
					setGroupBindings(sourceBinding, destinationKnowIt, binding);
				destinationKnowIt.setBinding(sourceBinding);

				// Check if the source binding is disabled. If it is, we should
				// disable this component too.
				if (this.isWidgetOwnerDisabled(support)) {
					destinationKnowIt.disableOwner();
				}

				this.repopulateParentOf(destinationWidget);

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
			return true;
		}

		return false;
	}
}
