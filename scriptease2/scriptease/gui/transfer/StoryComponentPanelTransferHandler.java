package scriptease.gui.transfer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.component.BindingWidget;
import scriptease.gui.libraryeditor.EffectHolderPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelManager;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.util.GUIOp;

/**
 * StoryComponentPanelTransferHandler is a more specific TransferHandler that is
 * intended to control Drag and Drop operations on StoryComponentPanels.<br>
 * <br>
 * It is a Singleton class; get the sole instance from {@link #getInstance()}.
 * 
 * If any other components ever need to be added that contain Story Components,
 * add a check to the "getSourceActions" method, and then add to the
 * "createTransferable" method to get the StoryComponentPanels out of whatever
 * container component was used. This is what we are doing with JLists that
 * contain Story Component Panels.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @see TransferHandler
 */
@SuppressWarnings("serial")
public class StoryComponentPanelTransferHandler extends TransferHandler {
	private static final StoryComponentPanelTransferHandler instance = new StoryComponentPanelTransferHandler();
	protected static DataFlavor storyCompFlavour;

	public static StoryComponentPanelTransferHandler getInstance() {
		return StoryComponentPanelTransferHandler.instance;
	}

	private StoryComponentPanelTransferHandler() {
		if (StoryComponentPanelTransferHandler.storyCompFlavour == null) {
			try {
				String storyComponentFlavour = DataFlavor.javaJVMLocalObjectMimeType
						+ ";class="
						+ scriptease.model.StoryComponent.class
								.getCanonicalName();
				StoryComponentPanelTransferHandler.storyCompFlavour = new DataFlavor(
						storyComponentFlavour);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		if (c instanceof StoryComponentPanel) {
			StoryComponentPanel panel = (StoryComponentPanel) c;
			if (panel.isRemovable()) {
				return TransferHandler.COPY_OR_MOVE;
			} else
				return TransferHandler.COPY;
		} else if (c instanceof JList) {
			return TransferHandler.COPY;
		}
		return TransferHandler.NONE;
	}

	/**
	 * Creates a transferable from the given component. Returns null if
	 * selectedNodes is null since calling control-x without a selected node
	 * will still fire this.
	 */
	@Override
	protected Transferable createTransferable(JComponent comp) {
		final List<StoryComponent> data;

		data = new ArrayList<StoryComponent>();

		if (comp instanceof StoryComponentPanel) {
			final StoryComponentPanel panel;
			// Get the parent selected StoryComponents, since the children will
			// be grabbed implicitly from the model
			panel = (StoryComponentPanel) comp;
			final StoryComponentPanelManager selectionManager = panel
					.getSelectionManager();
			if (selectionManager != null) {
				for (StoryComponentPanel aPanel : selectionManager
						.getSelectedParents())
					data.add(aPanel.getStoryComponent());
			}

		} else if (comp instanceof JList) {
			for (Object panelObject : ((JList) comp).getSelectedValues()) {
				if (panelObject instanceof StoryComponentPanel) {
					final StoryComponentPanel panel;
					panel = (StoryComponentPanel) panelObject;
					data.add(panel.getStoryComponent());
				}
			}

			comp.requestFocusInWindow();
		} else
			return null;

		return new StoryComponentPanelTransferable(data);
	}

	/**
	 * Scrolls the story component tree if we are hovering over one.
	 */
	private void scrollForMousePosition(Component component) {
		/*
		 * Scrolls the StoryComponentTree if we are hovering over one.
		 */
		Container parent = component.getParent();
		while (parent != null) {
			if (parent instanceof JSplitPane) {
				final Component bottomComponent;

				bottomComponent = ((JSplitPane) parent).getBottomComponent();
				if (bottomComponent instanceof StoryComponentPanelTree) {
					final JScrollPane pane = (StoryComponentPanelTree) bottomComponent;

					GUIOp.scrollJScrollPaneToMousePosition(pane);

					break;
				}
			}
			parent = parent.getParent();
		}
	}

	private StoryComponentPanel hoveredPanel;

	@Override
	public boolean canImport(TransferSupport support) {
		final Component supportComponent = support.getComponent();

		this.scrollForMousePosition(supportComponent);

		if (isBinding(support)) {
			// Handles the case where the user drags a Binding (delete)
			return true;
		} else {
			if (supportComponent instanceof StoryComponentPanel) {
				final StoryComponentPanel acceptingPanel;
				final StoryComponent acceptingStoryComponent;

				acceptingPanel = (StoryComponentPanel) supportComponent;
				acceptingStoryComponent = acceptingPanel.getStoryComponent();

				// Only import to complex story components which are editable
				if (acceptingPanel.isEditable()
						&& acceptingStoryComponent instanceof ComplexStoryComponent) {

					final Collection<StoryComponent> potentialChildren;

					potentialChildren = this.extractStoryComponents(support);

					if (potentialChildren != null
							&& this.canAcceptChildren(acceptingStoryComponent,
									potentialChildren)) {

						if (this.hoveredPanel != null
								&& this.hoveredPanel.getSelectionManager() != null)
							this.hoveredPanel.getSelectionManager()
									.updatePanelBackgrounds();
						acceptingPanel.setBackground(Color.LIGHT_GRAY);

						this.hoveredPanel = acceptingPanel;

						return true;
					}
				}

			} else if (supportComponent instanceof EffectHolderPanel) {
				final EffectHolderPanel effectHolder;
				final StoryComponent component;

				effectHolder = (EffectHolderPanel) supportComponent;
				component = this.extractStoryComponents(support).iterator()
						.next();

				if (component instanceof ScriptIt) {
					final ScriptIt scriptIt;

					scriptIt = (ScriptIt) component;

					if (scriptIt.isCause())
						return false;

					for (String type : scriptIt.getTypes()) {
						if (!effectHolder.getAllowableTypes().contains(type))
							return false;
					}
					return true;
				}
			}
		}

		if (this.hoveredPanel != null)
			this.hoveredPanel.updatePanelBackgrounds();
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		return importData(new TransferSupport(comp, t));
	}

	@Override
	public boolean importData(TransferSupport support) {
		// sanity check
		if (!this.canImport(support))
			return false;

		final Component supportComponent = support.getComponent();

		if (supportComponent instanceof StoryComponentPanel) {

			// variables
			int insertionIndex;
			boolean success = false;

			final Collection<StoryComponent> transferData;
			final StoryComponentPanel panel;
			final ComplexStoryComponent parent;

			panel = (StoryComponentPanel) supportComponent;
			parent = (ComplexStoryComponent) panel.getStoryComponent();

			// get transfer data
			transferData = this.extractStoryComponents(support);
			if (transferData == null)
				return false;

			if (support.isDrop()) {
				// drops have user input to decide where to place things
				insertionIndex = this.getInsertionIndex(panel, support);
			} else {
				// pastes just always go to the end of the parent's child list
				insertionIndex = ((ComplexStoryComponent) panel
						.getStoryComponent()).getChildCount();
				// Record Pasting so it can be undone
				if (!UndoManager.getInstance().hasOpenUndoableAction())
					UndoManager.getInstance().startUndoableAction("Paste");
			}

			// handle invalid child indexes
			if (insertionIndex == -1) {
				// Insert to the end of the parent's child list in the case of
				// an
				// illegal index.
				insertionIndex = ((ComplexStoryComponent) panel
						.getStoryComponent()).getChildCount();
			}

			// Now we actually add the transfer data
			for (StoryComponent newChild : transferData) {
				final StoryComponent clone;
				clone = newChild.clone();

				StoryComponent sibling = parent.getChildAt(insertionIndex);
				if (sibling != null) {
					// add in the middle
					success = parent.addStoryChildBefore(clone, sibling);
				} else {
					success = parent.addStoryChild(clone);
				}

				clone.revalidateKnowItBindings();

				if (!success)
					throw new IllegalStateException("Was unable to add "
							+ newChild + " to " + parent
							+ ". This should have been prevented by canImport.");
			}

			// End the recording of the paste
			if (!support.isDrop()
					&& UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().endUndoableAction();

			return true;
		} else if (supportComponent instanceof EffectHolderPanel) {
			final StoryComponent component;
			final EffectHolderPanel effectHolder;

			component = this.extractStoryComponents(support).iterator().next();
			effectHolder = (EffectHolderPanel) supportComponent;

			if (!(component instanceof ScriptIt))
				return false;

			return effectHolder.setEffect((ScriptIt) component);
		}

		return false;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// Start an undoable action for cut, but not for copy.
		if (!UndoManager.getInstance().hasOpenUndoableAction()
				&& action == TransferHandler.MOVE)
			UndoManager.getInstance().startUndoableAction("Cut");
		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// Start an undoable action for the move.
		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Move");
		super.exportAsDrag(comp, e, action);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		final List<StoryComponent> removedComponents = new ArrayList<StoryComponent>();

		if (data != null) {
			// if we're moving, we need to clean up the old nodes from the model
			if (action == TransferHandler.MOVE) {
				Collection<StoryComponent> components = this
						.extractStoryComponents(data);
				for (StoryComponent component : components) {
					removedComponents.add(component);
				}

				for (StoryComponent child : removedComponents) {
					if (child.getOwner() != null) {
						((ComplexStoryComponent) child.getOwner())
								.removeStoryChild(child);
					}
				}
			}

			if (this.hoveredPanel != null) {
				this.hoveredPanel.updatePanelBackgrounds();
			}
			// Close any open UndoableActions.
			if (UndoManager.getInstance().hasOpenUndoableAction()) {
				UndoManager.getInstance().endUndoableAction();
			}
		}
	}

	/**
	 * Determines whether the potential parent can accept the potential
	 * children. Recommended to call before attempting to add children to
	 * parent.
	 * 
	 * @param potentialParent
	 * @param potentialChildren
	 * @return
	 */
	private boolean canAcceptChildren(StoryComponent potentialParent,
			Collection<StoryComponent> potentialChildren) {
		boolean acceptable = true;

		for (StoryComponent child : potentialChildren) {
			acceptable &= potentialParent instanceof ComplexStoryComponent
					&& ((ComplexStoryComponent) potentialParent)
							.canAcceptChild(child)
					&& !(child instanceof StoryItemSequence);

			// TODO Shouldn't be necessary once CauseIts are created
			if (potentialParent instanceof ControlIt)
				acceptable &= !(child instanceof ScriptIt && ((ScriptIt) child)
						.isCause());
		}

		return acceptable;
	}

	/**
	 * Determines where the transfer will be inserting into.
	 * 
	 * @param support
	 *            The transfer context.
	 * @param panel
	 *            The StoryComponentPanel that will be receiving the transfer.
	 * @return The child index in the parent that would receive the drop.
	 */
	private int getInsertionIndex(StoryComponentPanel panel,
			TransferSupport support) {
		final StoryComponent parent = panel.getStoryComponent();
		final Point mouseLocation = support.getDropLocation().getDropPoint();

		// if the mouse is within the panel's boundaries
		if (mouseLocation != null && parent instanceof ComplexStoryComponent) {
			double yMouseLocation = mouseLocation.getY();
			if (((ComplexStoryComponent) parent).getChildCount() > 0) {
				StoryComponentPanel closest = this.findClosestChildPanel(
						yMouseLocation, panel);
				if (closest != null) {
					final StoryComponent child = closest.getStoryComponent();
					return ((ComplexStoryComponent) parent)
							.getChildIndex(child) + 1;
				}
			}
		}
		return 0;
	}

	/**
	 * Get the closest child StoryComponentPanel to the given parentPanel based
	 * on the given yLocation. May return null if it is unable to find a child
	 * panel within MAX_Y pixels of given yLocation
	 * 
	 * @param yLocation
	 * @param parentPanel
	 * @return
	 */
	private StoryComponentPanel findClosestChildPanel(double yLocation,
			StoryComponentPanel parentPanel) {
		// tracking variables used to maintain which panel is closest
		StoryComponentPanel closestPanel = null;

		final Collection<StoryComponentPanel> children = parentPanel
				.getChildrenPanels();
		// for each child, check if it is closer than the current closest

		for (StoryComponentPanel child : children) {
			double yChildLocation = child.getLocation().getY();
			if (yChildLocation < yLocation) {
				closestPanel = child;
			} else
				break;
		}
		return closestPanel;
	}

	/**
	 * Returns true if the given support contains a Binding
	 * 
	 * @param support
	 * @return
	 */
	private boolean isBinding(TransferSupport support) {
		Transferable transferable = support.getTransferable();
		KnowItBinding binding = null;

		try {
			binding = ((BindingWidget) transferable
					.getTransferData(BindingWidgetTransferHandler.KnowItBindingFlavor))
					.getBinding();
			return binding != null;
		} catch (UnsupportedFlavorException e) {
			// No chocolate for you!
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Attempts to extract StoryComponents from the Transfer Support. Returns
	 * <code>null</code> on failure.
	 * 
	 * @param support
	 *            The support to extract from.
	 * @return the StoryComponent extracted from the support, or
	 *         <code>null</code> if one cannot be found.
	 */
	protected Collection<StoryComponent> extractStoryComponents(
			TransferSupport support) {
		return extractStoryComponents(support.getTransferable());
	}

	/**
	 * Attempts to extract StoryComponents from the Transferable. Returns null
	 * on failure.
	 * 
	 * @param transferData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Collection<StoryComponent> extractStoryComponents(
			Transferable transferData) {
		Collection<StoryComponent> data = null;

		if (transferData
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			try {
				data = (Collection<StoryComponent>) transferData
						.getTransferData(StoryComponentPanelTransferHandler.storyCompFlavour);
			} catch (UnsupportedFlavorException e) {
				// data flavour is incompatible, the import is impossible
				return null;
			} catch (IOException e) {
				System.err.println("Augh! TransferHandler IO problem?! "
						+ "I don't even know what this MEANS!" + e);
				return null;
			}
		}
		return data;
	}

	/**
	 * SETreeTransfer holds a list of StoryComponents that have been pulled from
	 * the SETree in a transfer operation.
	 * 
	 * @author remiller
	 * @author mfchurch
	 */
	protected class StoryComponentPanelTransferable implements Transferable {
		private List<StoryComponent> data;

		public StoryComponentPanelTransferable(List<StoryComponent> nodes) {
			this.data = nodes;
		}

		@Override
		public List<StoryComponent> getTransferData(DataFlavor flavour)
				throws UnsupportedFlavorException, IOException {
			if (flavour.getRepresentationClass() == StoryComponent.class) {
				return this.data;
			} else
				throw new UnsupportedFlavorException(flavour);
		}

		/**
		 * Returns the data flavours that a StoryComponentPanelTransferable
		 * supports. A flavour is just an interpretation of the data that this
		 * object contains. <BR>
		 * <BR>
		 * Currently, only the StoryComponent flavour is supported, but could
		 * potentially be expanded to include other flavours, like a string
		 * flavour for example.
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { StoryComponentPanelTransferHandler.storyCompFlavour };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor
					.equals(StoryComponentPanelTransferHandler.storyCompFlavour);
		}

		@Override
		public String toString() {
			return "StoryComponentPanelTransferable [" + this.data + "]";
		}
	}
}
