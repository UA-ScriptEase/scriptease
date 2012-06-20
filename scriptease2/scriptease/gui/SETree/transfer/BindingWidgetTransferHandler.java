package scriptease.gui.SETree.transfer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModelPool;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * The Transfer Handler for all BindingWidgets. Performs all of the
 * BindingWidget-specific drag and drop logic. Singleton class.
 * 
 * @author graves
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
			
			
			//this be causing errors on the game objects...
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
		} else if (this.isKnowIt(support)) {
			final KnowIt knowIt = extractKnowIt(support);
			final Collection<String> bindingTypes = knowIt.getAcceptableTypes();

			final Collection<String> destinationTypes = destinationKnowIt
					.getTypes();

			// If any of the bindings types matchs any of the destinations
			// types, accept the binding
			for (String type : bindingTypes) {
				if (destinationTypes.contains(type))
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

		// Switch between dropping a StoryComponent and a BindingWidget
		if (isKnowIt(support)) {
			StoryComponent sourceStoryComponent;
			sourceStoryComponent = this.extractKnowIt(support);
			// clone the describing KnowIt
			sourceStoryComponent = sourceStoryComponent.clone();
			addDescribeItFor(destinationKnowIt, (KnowIt) sourceStoryComponent);
			return true;
		} else {
			final KnowItBinding sourceBinding;
			// Get the source data from the Transferable.
			sourceBinding = this.extractBinding(support);

			// Set the history to the active model
			UndoManager.getInstance().setActiveHistory(
					StoryModelPool.getInstance().getActiveModel());
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
		}
		return false;
	}

	/**
	 * Adds a new KnowIt describer to the given knowIt's owner. returns this
	 * knowIt bound to the given DoIt
	 * 
	 * @param knowIt
	 */
	private KnowIt addDescribeItFor(final KnowIt knowIt, final KnowIt describeIt) {
		final ComplexStoryComponent complexStoryComponent;

		// herp derp make a method for me
		StoryComponent owner = knowIt.getOwner();
		StoryComponent subOwner = owner;

		while (!(owner instanceof ComplexStoryComponent)) {
			subOwner = owner;
			owner = owner.getOwner();
		}
		complexStoryComponent = (ComplexStoryComponent) owner;
		complexStoryComponent.addStoryChildBefore(describeIt, subOwner);
		knowIt.setBinding(describeIt);
		return knowIt;
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
		destinationKnowIt.getBinding().process(
				new AbstractNoOpBindingVisitor(){
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

	@SuppressWarnings("unchecked")
	private boolean isKnowIt(TransferHandler.TransferSupport support) {
		StoryComponent component = null;
		try {
			component = ((Collection<StoryComponent>) support
					.getTransferable()
					.getTransferData(
							StoryComponentPanelTransferHandler.storyCompFlavour))
					.iterator().next();
			;
			return component != null && component instanceof KnowIt;
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	protected KnowIt extractKnowIt(TransferHandler.TransferSupport support) {
		KnowIt data = null;

		if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			try {
				data = (KnowIt) ((Collection<StoryComponent>) support
						.getTransferable()
						.getTransferData(
								StoryComponentPanelTransferHandler.storyCompFlavour))
						.iterator().next();
			} catch (UnsupportedFlavorException e) {
				return null;
			} catch (IOException e) {
				System.err
						.println("Augh! TransferHandler IO problem?! I don't even know what this MEANS!");
				return null;
			}
		}
		return data;
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
