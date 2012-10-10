package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.BindingAdapter;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.cell.BindingWidget;
import scriptease.gui.cell.ScriptWidgetFactory;
import scriptease.model.PatternModelManager;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;

/**
 * The Transfer Handler for all BindingWidgets. Performs all of the
 * BindingWidget-specific drag and drop logic. Singleton class.
 * 
 * @author graves
 * @author kschenk
 */
@SuppressWarnings("serial")
public class BindingWidgetTransferHandler extends TransferHandler {
	// Singleton instance of the TransferHandler.
	private static final BindingWidgetTransferHandler instance = new BindingWidgetTransferHandler();

	public static DataFlavor KnowItBindingFlavor;

	public static BindingWidgetTransferHandler getInstance() {
		return instance;
	}

	protected BindingWidgetTransferHandler() {
		if (BindingWidgetTransferHandler.KnowItBindingFlavor == null) {
			try {
				KnowItBindingFlavor = new DataFlavor(
						DataFlavor.javaJVMLocalObjectMimeType + ";class="
								+ BindingWidget.class.getCanonicalName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the source actions supported for this TransferHandler.
	 */
	@Override
	public int getSourceActions(JComponent component) {
		return COPY_OR_MOVE;
	}

	/**
	 * Creates and returns a Transferable created from the Binding that was
	 * dragged.
	 */
	@Override
	protected Transferable createTransferable(JComponent source) {
		// component should be a BindingWidget.
		if (!(source instanceof BindingWidget)) {
			throw new IllegalArgumentException(
					"The given JComponent was not a Widget.");
		}

		// Return the binding for the BindingWidget.
		return new BindingTransferable((BindingWidget) source);
	}

	/**
	 * Called when the export is completed. NOTE: <code>component</code> is the
	 * source component for the drag/drop.
	 */
	@Override
	protected void exportDone(JComponent component, Transferable transferable,
			int action) {

		// Only un-bind the source KnowIt if it was not a move operation (NONE
		// action)
		if (action == TransferHandler.NONE) {

			// Get the KnowIt for the Widget.
			KnowIt toRemove = (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(component.getParent());

			if (toRemove == null)
				return;

			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction(
						"Remove " + toRemove.getBinding() + " Binding");

			// Set the KnowItBinding to null.
			toRemove.clearBinding();
			UndoManager.getInstance().endUndoableAction();
		}
	}

	/**
	 * Called whenever something is dragged over a component that has
	 * BindingTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * whether or not this is a valid drop location for the drag.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		// Check the destination.
		// If the destination component is a BindingWidget, and the Transferable
		// is a BindingTransferable.
		final Component destinationComponent = support.getComponent();
		final KnowItBinding sourceBinding;

		// Get the destination KnowIt
		final KnowIt destinationKnowIt = (KnowIt) ScriptWidgetFactory
				.getEditedStoryComponent(destinationComponent.getParent());

		if (destinationComponent instanceof BindingWidget
				&& support.isDataFlavorSupported(KnowItBindingFlavor)) {
			sourceBinding = this.extractBinding(support);

			if (sourceBinding == null || destinationKnowIt == null) {
				return false;
			}

			// Check that the KnowItBinding type matches the destination KnowIt
			if (sourceBinding.compatibleWith(destinationKnowIt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called when a drop is detected on a component that has
	 * BindingTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * true if the import was successful, and false if the import failed.
	 */
	@Override
	public boolean importData(TransferSupport support) {
		final BindingWidget destinationComponent;
		KnowIt destinationKnowIt;

		// Make sure this import is legal.
		if (!canImport(support)) {
			return false;
		}

		// Get the destination component for the transfer.
		destinationComponent = (BindingWidget) support.getComponent();
		// Get the KnowIt for the destination.
		destinationKnowIt = (KnowIt) ScriptWidgetFactory
				.getEditedStoryComponent(destinationComponent.getParent());

		final KnowItBinding sourceBinding;
		// Get the source data from the Transferable.
		sourceBinding = this.extractBinding(support);

		// Set the history to the active model
		UndoManager.getInstance().setActiveHistory(
				PatternModelManager.getInstance().getActiveModel());
		if (sourceBinding != null) {
			// Bind the KnowIt with the source binding.
			KnowItBinding binding = destinationKnowIt.getBinding();
			if (binding != sourceBinding) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Set Binding " + sourceBinding);
				setGroupBindings(sourceBinding, destinationKnowIt, binding);
				destinationKnowIt.setBinding(sourceBinding);
			}
			if (UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().endUndoableAction();
			return true;
		}
		return false;
	}

	/**
	 * Set the bindings of all KnowIts within the destinationKnowIt's scope to
	 * be the sourceBinding
	 * 
	 * @param sourceBinding
	 * @param destinationKnowIt
	 * @param binding
	 */
	private void setGroupBindings(final KnowItBinding sourceBinding,
			final KnowIt destinationKnowIt, KnowItBinding binding) {
		destinationKnowIt.getBinding().process(new BindingAdapter() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				// do nothing for nulls, not even the default.
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				SameBindingGroupVisitor groupVisitor = new SameBindingGroupVisitor(
						destinationKnowIt);
				List<KnowIt> knowIts = groupVisitor.getGroup();
				for (KnowIt knowIt : knowIts) {
					knowIt.setBinding(sourceBinding);
				}
			}
		});
	}

	/*
	 * Attempts to extract a binding from the transfer support. Returns null on
	 * failure.
	 */
	private KnowItBinding extractBinding(TransferHandler.TransferSupport support) {
		Transferable transferable = support.getTransferable();
		KnowItBinding binding = null;

		try {
			binding = ((BindingWidget) transferable
					.getTransferData(KnowItBindingFlavor)).getBinding();
		} catch (UnsupportedFlavorException e) {
			// No chocolate for you!
			return null;
		} catch (IOException e) {
			return null;
		}
		return binding;
	}
}
