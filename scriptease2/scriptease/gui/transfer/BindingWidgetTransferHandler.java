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
import scriptease.controller.StoryAdapter;
import scriptease.controller.groupvisitor.SameBindingGroupVisitor;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.SlotPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
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

		KnowIt activityParameter = null;
		String parameterName = "";
		// Only un-bind the source KnowIt if it was not a move operation (NONE
		// action)
		if (action == TransferHandler.NONE) {

			// Get the KnowIt for the Widget.
			final KnowIt toRemove = (KnowIt) ScriptWidgetFactory
					.getEditedStoryComponent(component.getParent());

			if (toRemove == null)
				return;

			//Revert the display text to the name of the parameter
			String removedOriginalDisplayText = toRemove.getOriginalDisplayText();
			toRemove.setDisplayText(removedOriginalDisplayText);

			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction(
						"Remove " + toRemove.getBinding() + " Binding");

	
			
			//Check if we're an activity parameter usage
			if(toRemove.ownerComponent.ownerComponent.ownerComponent != null &&
					toRemove.ownerComponent.ownerComponent.ownerComponent instanceof ActivityIt){
				//Get the parameter from that activity which matches the thing we removed
				ActivityIt activityIt = (ActivityIt) toRemove.ownerComponent.ownerComponent.ownerComponent;
 				for (KnowIt activityParam : activityIt.getParameters()) {					
					if (toRemove.getBinding() instanceof KnowItBindingResource){
						parameterName = ((KnowItBindingResource) toRemove.getBinding()).getOriginalParameterText();
					} else if (toRemove.getBinding() instanceof KnowItBindingReference){
						KnowItBindingReference ref = (KnowItBindingReference) toRemove.getBinding();
						parameterName = ref.getValue().getOriginalDisplayText();
					} else if (toRemove.getBinding() instanceof KnowItBindingUninitialized){
						parameterName = toRemove.getOriginalDisplayText();
					}
					
					if (activityParam.getOriginalDisplayText().equals(parameterName)){
						activityParameter = activityParam;
						break;
					}
				}
				
				if (activityParameter != null){
					BindingWidget uninitializedWidget = ScriptWidgetFactory
							.buildBindingWidget(new KnowItBindingUninitialized(
									new KnowItBindingReference(
											activityParameter)));
					KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) uninitializedWidget.getBinding();
					uninitialized.getValue().setOriginalDisplayText(activityParameter.getOriginalDisplayText());
					uninitialized.getValue().setDisplayText(activityParameter.getDisplayText());
					toRemove.setBinding(uninitialized);
				}
			} else {
				// Set the KnowItBinding to null.
				toRemove.clearBinding();
			}
			
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

			// Special case for KnowItBindingUninitialized - they
			// shouldn't be dragged into their own referenced KnowIt
			if (sourceBinding instanceof KnowItBindingUninitialized) {
				final KnowItBindingUninitialized uninit = (KnowItBindingUninitialized) sourceBinding;
				if (uninit.getValue() == destinationKnowIt)
					return false;

				// the destinationKnowIt should also be a child of the component
				// that has the value the KnowItBindingUninitialized is
				// referencing.

				// TODO ScriptIt KnowIts don't know their owners right
				// now...can't do this.

				StoryComponent owner = uninit.getValue().getOwner();
				while (!(owner instanceof ComplexStoryComponent))
					owner = owner.getOwner();

				if (owner instanceof ComplexStoryComponent) {
					final ComplexStoryComponent complex = (ComplexStoryComponent) owner;

					final List<StoryComponent> descendants = complex
							.getDescendentStoryComponents();

					StoryComponent destOwner = destinationKnowIt.getOwner();
					while (!(destOwner instanceof ComplexStoryComponent))
						destOwner = destOwner.getOwner();

					if (!descendants.contains(destOwner))
						return false;
				}
			}

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
		canImport |= StoryComponentTransferUtils.canImportToParent(support);

		if (canImport) {
			// TODO Set mouse pointer to normal
		} else {
			// TODO Set mouse pointer to invalid operation.
		}

		return canImport;
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
		final BindingWidget destinationComponent;
		final KnowIt destinationKnowIt;

		// Make sure this import is legal.
		if (!canImport(support)) {
			return false;
		}

		// Some other component is being dragged over the binding widget
		// but of course, it isn't a binding widget and shouldn't fit
		// the slot - so lets handle it.
		if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			return StoryComponentTransferUtils.importToParent(support);
		}

		// Get the destination component for the transfer.
		destinationComponent = (BindingWidget) support.getComponent();
		// Get the KnowIt for the destination.
		destinationKnowIt = (KnowIt) ScriptWidgetFactory
				.getEditedStoryComponent(destinationComponent.getParent());

		final KnowItBinding sourceBinding;
		// Get the source data from the Transferable.
		sourceBinding = this.extractBinding(support);

		// Update the text of all usages of this if it's an Activity Parameter
		if (destinationKnowIt.getOwner().getOwner() instanceof ActivityIt) {
			final ActivityIt activityIt = (ActivityIt) destinationKnowIt
					.getOwner().getOwner();
			final List<StoryComponent> children = activityIt.getChildren();

			for (StoryComponent child : children) {
				child.process(new StoryAdapter() {

					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						this.defaultProcessComplex(scriptIt);
						scriptIt.processParameters(this);
					}

					@Override
					public void processKnowIt(KnowIt knowIt) {
						final KnowItBinding binding = knowIt.getBinding();

						if (binding instanceof KnowItBindingFunction) {
							final KnowItBindingFunction function = (KnowItBindingFunction) binding;

							function.getValue().process(this);

						} else if (binding instanceof KnowItBindingUninitialized) {
							KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) binding;

							for (KnowIt activityParam : activityIt
									.getParameters()) {
								if (uninitialized.getValue().getOriginalDisplayText().equals(activityParam.getOriginalDisplayText())) {
									// We need to handle both
									// KnowItBindingResources (Game Objects)
									// And KnowItBindingReferences (Implicits et
									// al.)
									if (sourceBinding instanceof KnowItBindingResource &&
										activityParam.getOriginalDisplayText().equals(destinationKnowIt.getOriginalDisplayText())	) {
										uninitialized
												.getValue()
												.setDisplayText(
														((KnowItBindingResource) sourceBinding)
																.getName());
									} else if (sourceBinding instanceof KnowItBindingReference) {
										KnowItBindingReference ref = (KnowItBindingReference) sourceBinding;
										if(activityParam.getOriginalDisplayText().equals(destinationKnowIt.getOriginalDisplayText())){
											uninitialized
											.getValue()
											.setDisplayText(
													ref.getValue()
															.getDisplayText());
										}
									}
								}
							}
						} else if (binding instanceof KnowItBindingReference) {
							KnowItBindingReference ref = (KnowItBindingReference) binding;
							for (KnowIt activityParam : activityIt
									.getParameters()) {
								if (ref.getValue()
										.getOriginalDisplayText()
										.equals(activityParam
												.getOriginalDisplayText())) {
									// Construct a new KnowItBindingUninitialized we will use for the activity
									BindingWidget uninitializedWidget = ScriptWidgetFactory
											.buildBindingWidget(new KnowItBindingUninitialized(
													new KnowItBindingReference(
															activityParam)));
									KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) uninitializedWidget.getBinding();
									uninitialized.getValue().setOriginalDisplayText(activityParam.getOriginalDisplayText());

									if (sourceBinding instanceof KnowItBindingReference){
										KnowItBindingReference sourceRef = (KnowItBindingReference) sourceBinding;
										uninitialized.getValue().setDisplayText(sourceRef.getValue().getDisplayText());

									} else if (sourceBinding instanceof KnowItBindingResource){
										KnowItBindingResource sourceRes = (KnowItBindingResource) sourceBinding;
										uninitialized.getValue().setDisplayText(sourceRes.getName());
									}
									
									knowIt.setBinding(uninitialized);
								}
							}
						} else if (binding instanceof KnowItBindingResource) {
							KnowItBindingResource res = (KnowItBindingResource) binding;
							for (KnowIt activityParam : activityIt.getParameters()) {
								if(res.getOriginalParameterText().equals(activityParam.getOriginalDisplayText())){
									// Construct a new KnowItBindingUninitialized we will use for the activity
									BindingWidget uninitializedWidget = ScriptWidgetFactory
											.buildBindingWidget(new KnowItBindingUninitialized(
													new KnowItBindingReference(
															activityParam)));
									KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) uninitializedWidget.getBinding();
									uninitialized.getValue().setOriginalDisplayText(activityParam.getOriginalDisplayText());
									
									if (sourceBinding instanceof KnowItBindingReference){
										KnowItBindingReference sourceRef = (KnowItBindingReference) sourceBinding;
										uninitialized.getValue().setDisplayText(sourceRef.getValue().getDisplayText());

									} else if (sourceBinding instanceof KnowItBindingResource){
										KnowItBindingResource sourceRes = (KnowItBindingResource) sourceBinding;
										uninitialized.getValue().setDisplayText(sourceRes.getName());
									}
									
									knowIt.setBinding(uninitialized);
									
								}
								
							}
						}

					}

					@Override
					protected void defaultProcessComplex(
							ComplexStoryComponent complex) {
						for (StoryComponent child : complex.getChildren()) {
							child.process(this);
						}
					}
				});
			}
		} else {
			//If we're dragging an object into something that's not an Activity parameter
			//Then we should update the new object's original display text to match whatever used to be there.
			//This will allow us to put real binding widgets into activities and let them be overwritten if one re-adds the parameter
			
			
			String pastOriginalDisplayText = "";
			
			if (destinationKnowIt.getBinding() instanceof KnowItBindingUninitialized){
				//This is the case where we're dragging on top of an Activity Parameter Usage
				KnowIt pastKnowIt = (KnowIt) destinationKnowIt.getBinding().getValue();
				pastOriginalDisplayText = pastKnowIt.getOriginalDisplayText();
			} else if (destinationKnowIt.getBinding() instanceof KnowItBindingResource){
				KnowItBindingResource res = (KnowItBindingResource) destinationKnowIt.getBinding();
				pastOriginalDisplayText = res.getOriginalParameterText();
			} else if (destinationKnowIt.getBinding() instanceof KnowItBindingReference){
				KnowItBindingReference ref = (KnowItBindingReference) destinationKnowIt.getBinding();
				pastOriginalDisplayText = ref.getValue().getOriginalDisplayText();
			}
			
			if (sourceBinding instanceof KnowItBindingResource) {
				((KnowItBindingResource)sourceBinding).setOriginalParameterText(pastOriginalDisplayText);
				
				
			} else if (sourceBinding instanceof KnowItBindingReference) {
				((KnowItBindingReference)sourceBinding).getValue().setOriginalDisplayText(pastOriginalDisplayText);
			}	
		}

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

				// Check if the source binding is disabled. If it is, we should
				// disable this component too.
				if (this.isWidgetOwnerDisabled(support)) {
					destinationKnowIt.disableOwner();
				}

				this.repopulateParentOf(destinationComponent);

				if (UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().endUndoableAction();
			}
			return true;
		}
		return false;
	}

	protected void repopulateParentOf(JComponent destinationComponent) {
		final Container parent;
		parent = destinationComponent.getParent();

		if (parent != null && parent instanceof SlotPanel) {
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
