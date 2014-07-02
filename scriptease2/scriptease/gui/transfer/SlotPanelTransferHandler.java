package scriptease.gui.transfer;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.StoryAdapter;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.component.ScriptWidgetFactory;
import scriptease.gui.component.SlotPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
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

					final List<StoryComponent> descendants = complex
							.getDescendents();

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
		
		//Update the text of all usages of the imported KnowIt if it is an Activity Parameter
		if (knowIt.getOwner().getOwner() instanceof ActivityIt){
			final ActivityIt activityIt = (ActivityIt) knowIt.getOwner().getOwner();			
			final List<StoryComponent> children = activityIt.getChildren();
			
			for (StoryComponent child : children) {
				child.process(new StoryAdapter() {
					
					@Override
					public void processScriptIt(ScriptIt scriptIt) {
						this.defaultProcessComplex(scriptIt);
						scriptIt.processParameters(this);
					}

					@Override
					public void processKnowIt(KnowIt childKnowIt) {
						final KnowItBinding binding = childKnowIt.getBinding();

						if (binding instanceof KnowItBindingFunction) {
							final KnowItBindingFunction function = (KnowItBindingFunction) binding;

							function.getValue().process(this);

						} else if (binding instanceof KnowItBindingUninitialized) {
							KnowItBindingUninitialized uninitialized = (KnowItBindingUninitialized) binding;

							for (KnowIt activityParam : activityIt.getParameters()) {
								if (uninitialized.getValue().getOriginalDisplayText()
										.equals(activityParam.getOriginalDisplayText())) {									
									if(sourceBinding instanceof KnowItBindingResource && activityParam.getOriginalDisplayText().equals(knowIt.getOriginalDisplayText())) {
										
										uninitialized.getValue().setDisplayText(((KnowItBindingResource) sourceBinding).getName());
									} else if (sourceBinding instanceof KnowItBindingReference){
										KnowItBindingReference ref = (KnowItBindingReference) sourceBinding;
										if (activityParam.getOriginalDisplayText().equals(knowIt.getOriginalDisplayText())){
											uninitialized.getValue().setDisplayText(ref.getValue().getDisplayText());									
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
									
									childKnowIt.setBinding(uninitialized);
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
									
									childKnowIt.setBinding(uninitialized);
									
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
			
			if (knowIt.getBinding() instanceof KnowItBindingUninitialized){
				//This is the case where we're dragging on top of an Activity Parameter Usage
				KnowIt pastKnowIt = (KnowIt) knowIt.getBinding().getValue();
				pastOriginalDisplayText = pastKnowIt.getOriginalDisplayText();
			} else if (knowIt.getBinding() instanceof KnowItBindingResource){
				KnowItBindingResource res = (KnowItBindingResource) knowIt.getBinding();
				pastOriginalDisplayText = res.getOriginalParameterText();
			} else if (knowIt.getBinding() instanceof KnowItBindingReference){
				KnowItBindingReference ref = (KnowItBindingReference) knowIt.getBinding();
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
