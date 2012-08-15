package scriptease.gui.SETree.transfer;

import java.awt.Component;
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
import javax.swing.TransferHandler;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * StoryComponentPanelListTransferHandler is a more specific TransferHandler
 * that is intended to control Drag and Drop operations on JLists of
 * StoryComponentPanels.<br>
 * <br>
 * It is a Singleton class; get the sole instance from {@link #getInstance()}.
 * 
 * @author kschenk
 * @see StoryComponentTransferHandler
 * @see TransferHandler
 */
@SuppressWarnings("serial")
public class StoryComponentListTransferHandler extends TransferHandler {
	private static final StoryComponentListTransferHandler instance = new StoryComponentListTransferHandler();
	public static DataFlavor storyCompListFlavour;

	public static StoryComponentListTransferHandler getInstance() {
		return StoryComponentListTransferHandler.instance;
	}

	protected StoryComponentListTransferHandler() {
		if (StoryComponentListTransferHandler.storyCompListFlavour == null) {
			try {
				String storyComponentFlavour = DataFlavor.javaJVMLocalObjectMimeType
						+ ";class=" + JList.class.getCanonicalName();
				StoryComponentListTransferHandler.storyCompListFlavour = new DataFlavor(
						storyComponentFlavour);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

/*	*//**
	 * Creates a transferable from the given component. Returns null if
	 * selectedNodes is null since calling control-x without a selected node
	 * will still fire this.
	 *//*
	@Override
	protected Transferable createTransferable(JComponent source) {

		final List<StoryComponent> data;

		data = new ArrayList<StoryComponent>();

		if (source instanceof JList) {
			for (Object panelObject : ((JList) source).getSelectedValues()) {
				if (panelObject instanceof StoryComponentPanel)
					data.add(((StoryComponentPanel) panelObject)
							.getStoryComponent());
			}
		} else
			return null;

		// Create a Transferable with those StoryComponents
		StoryComponentPanelTransferable storyComponentPanelTransferable = new StoryComponentPanelTransferable(
				data);
		return storyComponentPanelTransferable;
	}*/

	@Override
	public boolean canImport(TransferSupport support) {
		// Handles the case where the user drags a Binding (delete)
		if (isBinding(support)) {
			return true;
		} else {
			final Component supportComponent;
			final List<StoryComponentPanel> acceptingPanels;

			supportComponent = support.getComponent();
			acceptingPanels = new ArrayList<StoryComponentPanel>();

			if (supportComponent instanceof StoryComponentPanel)
				acceptingPanels.add((StoryComponentPanel) support
						.getComponent());
			else if (supportComponent instanceof JList) {
				for (Object panelObject : ((JList) support.getComponent())
						.getSelectedValues()) {
					if (panelObject instanceof StoryComponentPanel)
						acceptingPanels.add((StoryComponentPanel) panelObject);
				}
			} else
				return false;

			for (StoryComponentPanel acceptingPanel : acceptingPanels) {
				final StoryComponent acceptingStoryComponent = acceptingPanel
						.getStoryComponent();
				// Only import to complex story components which are editable
				if (/*
					 * acceptingPanel.isEditable() &&
					 */acceptingStoryComponent instanceof ComplexStoryComponent) {

					final Collection<StoryComponent> potentialChildren = this
							.extractStoryComponents(support);

					if (potentialChildren == null)
						return false;
					else if (!this.canAcceptChildren(acceptingStoryComponent,
							potentialChildren))
						return false;
					// return potentialChildren != null ?
					// this.canAcceptChildren(
					// acceptingStoryComponent, potentialChildren) : false;

				} else
					return false;
			}
			return true;
		}
	}

	private boolean canAcceptChildren(StoryComponent potentialParent,
			Collection<StoryComponent> potentialChildren) {
		boolean acceptable = true;

		for (StoryComponent child : potentialChildren) {
			acceptable &= (potentialParent instanceof ComplexStoryComponent)
					&& ((ComplexStoryComponent) potentialParent)
							.canAcceptChild(child);
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
		int index = -1;

		// if the mouse is within the panel's boundries
		if (mouseLocation != null && parent instanceof ComplexStoryComponent) {
			double yMouseLocation = mouseLocation.getY();
			if (((ComplexStoryComponent) parent).getChildCount() > 0) {
				StoryComponentPanel closest = this.findClosestChildPanel(
						yMouseLocation, panel);
				if (closest != null) {
					final StoryComponent child = closest.getStoryComponent();
					index = ((ComplexStoryComponent) parent)
							.getChildIndex(child);
				}
			} else
				index = 0;
		}
		return index;
	}

	// Don't look further than MAX_Y pixels for the closest panel
	private static final int MAX_Y = 1000;

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
		double closest = MAX_Y;
		StoryComponentPanel closestPanel = null;

		final Collection<StoryComponentPanel> children = parentPanel
				.getChildrenPanels();
		// for each child, check if it is closer than the current closest
		for (StoryComponentPanel child : children) {
			double yChildLocation = child.getLocation().getY();
			double yDifference = Math.abs(yChildLocation - yLocation);
			if (yDifference < closest) {
				closest = yDifference;
				closestPanel = child;
			}
		}
		return closestPanel;
	}

	/**
	 * Returns if the given support contains a Binding
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

		// FLAVA FAAVVVEE
		if (transferData
				.isDataFlavorSupported(StoryComponentListTransferHandler.storyCompListFlavour)) {
			try {
				data = (Collection<StoryComponent>) transferData
						.getTransferData(StoryComponentListTransferHandler.storyCompListFlavour);
			} catch (UnsupportedFlavorException e) {
				// data flavour is incompatible, the import is impossible
				return null;
			} catch (IOException e) {
				System.err
						.println("Augh! TransferHandler IO problem?! I don't even know what this MEANS!"
								+ e);
				return null;
			}
		}
		return data;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// Start an undoable action for cut, but not for copy.
		if (!UndoManager.getInstance().hasOpenUndoableAction()
				&& action == TransferHandler.MOVE)
			UndoManager.getInstance().startUndoableAction("Cut");

		if (comp instanceof JList)
			comp = (JComponent) ((JList) comp).getSelectedValue();

		super.exportToClipboard(comp, clip, action);
	}

	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// Start an undoable action for the move.
		if (!UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Move");

		if (comp instanceof JList)
			comp = (JComponent) ((JList) comp).getSelectedValue();

		super.exportAsDrag(comp, e, action);
	}

	@Override
	public boolean importData(TransferSupport support) {
		// sanity check
		if (!this.canImport(support))
			return false;

		// variables
		final Collection<StoryComponent> transferData;
		int insertionIndex;
		final StoryComponentPanel panel = (StoryComponentPanel) support
				.getComponent();
		final ComplexStoryComponent parent = (ComplexStoryComponent) panel
				.getStoryComponent();
		boolean success = false;

		// get transfer data
		transferData = this.extractStoryComponents(support);
		if (transferData == null)
			return false;

		if (support.isDrop()) {
			// drops have user input to decide where to place things
			insertionIndex = this.getInsertionIndex(panel, support);
		} else {
			// pastes just always go to the end of the parent's child list
			insertionIndex = ((ComplexStoryComponent) panel.getStoryComponent())
					.getChildCount();
			// Record Pasting so it can be undone
			if (!UndoManager.getInstance().hasOpenUndoableAction())
				UndoManager.getInstance().startUndoableAction("Paste");
		}

		// handle invalid child indexes
		if (insertionIndex == -1) {
			// Insert to the end of the parent's child list in the case of an
			// illegal index.
			insertionIndex = ((ComplexStoryComponent) panel.getStoryComponent())
					.getChildCount();
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

			if (!success)
				throw new IllegalStateException("Was unable to add " + newChild
						+ " to " + parent
						+ ". This should have been prevented by canImport.");
		}

		// End the recording of the paste
		if (!support.isDrop()
				&& UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().endUndoableAction();

		return true;
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

			// Close any open UndoableActions.
			if (UndoManager.getInstance().hasOpenUndoableAction()) {
				UndoManager.getInstance().endUndoableAction();
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		if (c instanceof JList)
			c = (StoryComponentPanel) ((JList) c).getSelectedValue();
		if (c instanceof StoryComponentPanel) {
			StoryComponentPanel panel = (StoryComponentPanel) c;
			if (panel.isRemovable()) {
				return TransferHandler.COPY_OR_MOVE;
			} else
				return TransferHandler.COPY;
		}
		return TransferHandler.NONE;
	}
}
