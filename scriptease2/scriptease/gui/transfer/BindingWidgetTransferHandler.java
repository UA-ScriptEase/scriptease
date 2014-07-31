package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryComponentUtils;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.SlotPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ComplexStoryComponent;
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
				try {
					KnowItBindingFlavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType + ";class="
									+ SlotPanel.class.getCanonicalName());
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();

				}
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
		if (!(source instanceof BindingWidget)
				&& !(source instanceof SlotPanel))
			throw new IllegalArgumentException("The given JComponent, "
					+ source + " was not compatible.");

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
			final KnowIt toRemove = (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(component.getParent());

			if (toRemove == null)
				return;

			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction(
						"Remove " + toRemove.getBinding() + " Binding");

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
		final KnowIt knowIt = this.getKnowIt(support);

		if (knowIt != null
				&& support.isDataFlavorSupported(KnowItBindingFlavor)) {
			// Get the destination KnowIt

			final KnowItBinding binding = this.extractBinding(support);

			// Special case for KnowItBindingUninitialized - they
			// shouldn't be dragged into their own referenced KnowIt
			if (binding instanceof KnowItBindingUninitialized) {
				final KnowItBindingUninitialized uninit = (KnowItBindingUninitialized) binding;
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

					final List<StoryComponent> descendants = complex
							.getDescendentStoryComponents();

					StoryComponent destOwner = knowIt.getOwner();
					while (!(destOwner instanceof ComplexStoryComponent))
						destOwner = destOwner.getOwner();

					if (!descendants.contains(destOwner))
						return false;
				}
			}

			// Check that the KnowItBinding type matches the destination KnowIt
			if (binding != null && knowIt != null) {
				if (binding.compatibleWith(knowIt)) {
					canImport = true;
				}
			}

		}

		// Special case - to handle where effects, descriptions, and controls
		// can be dragged over binding widgets in order to get re-directed to
		// their parent block.
		return canImport |= StoryComponentTransferUtils
				.canImportToParent(support);
	}

	protected KnowIt getKnowIt(TransferSupport support) {
		final Component destinationComponent = support.getComponent();

		if (destinationComponent instanceof BindingWidget) {
			return (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(destinationComponent.getParent());
		} else if (destinationComponent instanceof SlotPanel) {
			return ((SlotPanel) destinationComponent).getKnowIt();
		}

		return null;
	}

	protected static boolean lastDragShiftDown = false;

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
		// Make sure this import is legal.
		if (!canImport(support)) {
			return false;
		} else if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			// Some other component is being dragged over the binding widget
			// but of course, it isn't a binding widget and shouldn't fit
			// the slot - so lets handle it.
			return StoryComponentTransferUtils.importToParent(support);
		}

		final KnowIt knowIt;

		knowIt = this.getKnowIt(support);

		// Set the history to the active model
		UndoManager.getInstance().setActiveHistory(
				SEModelManager.getInstance().getActiveModel());

		final KnowItBinding sourceBinding = this.extractBinding(support);
		final KnowItBinding binding = knowIt.getBinding();

		if (sourceBinding == null || binding == sourceBinding)
			return false;

		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction(
					"Set Binding " + sourceBinding);

		if (BindingWidgetTransferHandler.lastDragShiftDown)
			setGroupBindings(sourceBinding, knowIt, binding);

		final ActivityIt activity = StoryComponentUtils.getActivityIt(knowIt);

		if (activity != null) {
			KnowIt existing = null;
			// dest = activity
			for (KnowIt param : activity.getParameters()) {
				if (param.getBinding() == sourceBinding) {
					existing = param;
					break;
				}
			}

			if (existing != null) {
				knowIt.setBinding(new KnowItBindingUninitialized(
						new KnowItBindingReference(existing)));
			} else
				knowIt.setBinding(sourceBinding);
		} else
			knowIt.setBinding(sourceBinding);

		// Check if the source binding is disabled. If it is, we should
		// disable this component too.
		if (this.isWidgetOwnerDisabled(support)) {
			knowIt.disableOwner();
		}

		repopulateParentOf((JComponent) support.getComponent());

		if (UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().endUndoableAction();

		return true;
	}

	protected void repopulateParentOf(JComponent destinationComponent) {
		if (destinationComponent instanceof SlotPanel)
			((SlotPanel) destinationComponent).populate();
		else {
			final Container parent;
			parent = destinationComponent.getParent();

			if (parent != null && parent instanceof SlotPanel) {
				((SlotPanel) parent).populate();
			}
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
	protected void setGroupBindings(final KnowItBinding sourceBinding,
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

	/**
	 * Attempts to extract a binding from the transfer support. Returns null on
	 * failure.
	 * 
	 * @param support
	 * @return
	 */
	protected KnowItBinding extractBinding(
			TransferHandler.TransferSupport support) {
		KnowItBinding sourceBinding = null;

		try {
			sourceBinding = ((BindingWidget) support.getTransferable()
					.getTransferData(
							BindingWidgetTransferHandler.KnowItBindingFlavor))
					.getBinding();
		} catch (UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return sourceBinding;
	}

	/**
	 * Check if the source binding is a disabled description - if it is, disable
	 * the destination owner effect.
	 */
	protected boolean isWidgetOwnerDisabled(
			TransferHandler.TransferSupport support) {
		try {
			final BindingWidget bindingWidget;
			bindingWidget = (BindingWidget) support.getTransferable()
					.getTransferData(KnowItBindingFlavor);

			if (bindingWidget.getBinding() instanceof KnowItBindingReference) {
				final KnowItBindingReference reference = (KnowItBindingReference) bindingWidget
						.getBinding();
				final KnowIt knowIt = reference.getValue();

				if (!knowIt.isEnabled())
					return true;
			}
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return false;
	}
}
