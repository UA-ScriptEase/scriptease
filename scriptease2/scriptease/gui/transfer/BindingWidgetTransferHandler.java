package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.BindingAdapter;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.SlotPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.SEModelManager;

/**
 * The Transfer Handler for all BindingWidgets. Performs all of the
 * BindingWidget-specific drag and drop logic. Singleton class.
 * 
 * @author graves
 * @author kschenk
 * @author jyuen
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

			this.repopulateParentOf(component);

			if (UndoManager.getInstance().hasOpenUndoableAction())
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
		boolean canImport = false;

		// Check the destination.
		// If the destination component is a BindingWidget, and the Transferable
		// is a BindingTransferable.
		final Component destinationComponent = support.getComponent();

		if (destinationComponent instanceof BindingWidget
				&& support.isDataFlavorSupported(KnowItBindingFlavor)) {
			// Get the destination KnowIt
			final KnowIt destinationKnowIt;
			final KnowItBinding sourceBinding;

			destinationKnowIt = (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(destinationComponent.getParent());
			sourceBinding = this.extractBinding(support);

			// Check that the KnowItBinding type matches the destination KnowIt
			if (sourceBinding != null && destinationKnowIt != null) {
				if (sourceBinding.compatibleWith(destinationKnowIt)) {
					canImport = true;
				}
			}
		}

		// Special case - to handle where effects, descriptions, and controls
		// can be dragged over binding widgets in order to get re-directed to
		// their parent block.
		canImport |= this.canImportComponentsToParent(support);

		if (canImport) {
			// TODO Set mouse pointer to normal
		} else {
			// TODO Set mouse pointer to invalid operation.
		}

		return canImport;
	}

	/**
	 * Checks whether the data being transfered can be redirected to a parent
	 * StoryComponentContainer - such as effects, descriptions, and controls.
	 * 
	 * @param support
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean canImportComponentsToParent(
			TransferHandler.TransferSupport support) {
		final Component destinationComponent = support.getComponent();

		if (destinationComponent instanceof BindingWidget
				&& (support
						.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour))) {

			// If the binding widget isn't even in a StoryComponentContainer,
			// we shouldn't be dragging stuff there anyway.
			Component panel = support.getComponent();
			while (!(panel instanceof StoryComponentPanel) && panel != null) {
				panel = panel.getParent();
			}
			if (panel == null)
				return false;

			StoryComponent storyComponent = ((StoryComponentPanel) panel)
					.getStoryComponent();

			if (!(storyComponent instanceof StoryComponentContainer)) {
				panel = panel.getParent();
				storyComponent = ((StoryComponentPanel) panel)
						.getStoryComponent();
				if (!(storyComponent instanceof StoryComponentContainer))
					return false;
			}

			// Finally, check whether we have a valid component.
			try {
				final Collection<StoryComponent> components;

				components = (Collection<StoryComponent>) support
						.getTransferable()
						.getTransferData(
								StoryComponentPanelTransferHandler.storyCompFlavour);

				// Check if its one of the acceptable components.
				for (StoryComponent component : components)
					if ((component instanceof ScriptIt && !(component instanceof CauseIt))
							|| (component instanceof KnowIt)
							|| component instanceof AskIt)
						return true;

			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	private static boolean lastDragShiftDown = false;

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		super.exportAsDrag(comp, e, action);
		BindingWidgetTransferHandler.lastDragShiftDown = e.isShiftDown();
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

		// Some other component is being dragged over the binding widget
		// but of course, it isn't a binding widget and shouldn't fit
		// the slot - so lets handle it.
		if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			return importComponentsToParent(support);
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
				SEModelManager.getInstance().getActiveModel());
		if (sourceBinding != null) {
			// Bind the KnowIt with the source binding.
			KnowItBinding binding = destinationKnowIt.getBinding();
			if (binding != sourceBinding) {
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction(
							"Set Binding " + sourceBinding);
				if (BindingWidgetTransferHandler.lastDragShiftDown)
					setGroupBindings(sourceBinding, destinationKnowIt, binding);
				destinationKnowIt.setBinding(sourceBinding);

				this.repopulateParentOf(destinationComponent);

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
			return true;
		}
		return false;
	}

	/**
	 * We handle other components that are being dragged over the binding slots
	 * by putting them where they actually belong (and where the user intended
	 * them to go) - in this case the StoryComponentContainer.
	 * 
	 * @param support
	 */
	@SuppressWarnings("unchecked")
	protected boolean importComponentsToParent(TransferSupport support) {
		final Collection<StoryComponent> components;
		final StoryComponent component;
		final ComplexStoryComponent parent;

		Component panel;
		panel = support.getComponent();

		// Get the first instance of a StoryComponentPanel if there is one.
		while (!(panel instanceof StoryComponentPanel) && panel != null) {
			panel = panel.getParent();
		}

		if (panel == null)
			return false;

		component = ((StoryComponentPanel) panel).getStoryComponent();

		// Check if the component already is a container. If not,
		// We want the StoryComponentContainer panel so... once more.
		if (!(component instanceof StoryComponentContainer))
			panel = panel.getParent();

		parent = (ComplexStoryComponent) ((StoryComponentPanel) panel)
				.getStoryComponent();

		// Now we actually add the transfer data
		try {
			components = (Collection<StoryComponent>) support
					.getTransferable()
					.getTransferData(
							StoryComponentPanelTransferHandler.storyCompFlavour);

			for (StoryComponent newChild : components) {
				parent.addStoryChild(newChild.clone());
			}
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}

		return true;
	}

	private void repopulateParentOf(JComponent destinationComponent) {
		final Container parent;
		parent = destinationComponent.getParent();

		if (parent != null && parent instanceof SlotPanel) {
			parent.removeAll();
			((SlotPanel) parent).populate();
		}
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
				final SameBindingGroupVisitor groupVisitor;
				final List<KnowIt> knowIts;

				groupVisitor = new SameBindingGroupVisitor(destinationKnowIt);
				knowIts = groupVisitor.getGroup();

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
