package scriptease.gui.SEGraph.renderers;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import scriptease.controller.undo.UndoManager;
import scriptease.gui.SEGraph.SEGraph;

@SuppressWarnings("serial")
public class SEGraphNodeTransferHandler<E> extends TransferHandler {
	protected static DataFlavor nodeFlavour;

	private final SEGraph<E> graph;

	public SEGraphNodeTransferHandler(SEGraph<E> graph) {
		this.graph = graph;

		final E startNode = graph.getStartNode();

		try {
			String storyComponentFlavour = DataFlavor.javaJVMLocalObjectMimeType
					+ ";class=" + startNode.getClass().getCanonicalName();
			SEGraphNodeTransferHandler.nodeFlavour = new DataFlavor(
					storyComponentFlavour);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		if (this.graph.getNodeComponents().contains(c))
			return TransferHandler.COPY;
		return TransferHandler.NONE;
	}

	/**
	 * Creates a transferable from the given component. Returns null if
	 * selectedNodes is null since calling control-x without a selected node
	 * will still fire this.
	 */
	@Override
	protected Transferable createTransferable(JComponent comp) {
		if (this.graph.getNodeComponents().contains(comp)) {
			return new SEGraphNodeTransferable(this.graph
					.getNodesToComponentsMap().getKey(comp));
		} else
			return null;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (this.graph.getNodeComponents().contains(support.getComponent())) {
			final JComponent acceptingPanel;
			final E acceptingNode;

			acceptingPanel = (JComponent) support.getComponent();
			acceptingNode = this.graph.getNodesToComponentsMap().getKey(
					acceptingPanel);

			// Only import to complex story components which are editable

			if (acceptingNode != this.graph.getStartNode()) {

				// final Collection<E> potentialChildren;

				/*
				 * potentialChildren = this.extractStoryComponents(support);
				 * 
				 * if (potentialChildren != null &&
				 * this.canAcceptChildren(acceptingStoryComponent,
				 * potentialChildren)) {
				 */

				return true;
				// }
			}
		}
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		// sanity check
		if (!this.canImport(support))
			return false;

		// variables
		final E newNode;
		final JComponent component = (JComponent) support.getComponent();
		final E node = this.graph.getNodesToComponentsMap().getKey(component);

		boolean success = false;

		newNode = this.extractStoryComponents(support.getTransferable());

		if (newNode == null)
			return false;

		// Record Pasting so it can be undone
		if (!support.isDrop()
				&& !UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().startUndoableAction("Paste");

		// Now we actually add the transfer data
		success = this.graph.replaceNode(node, newNode);

		if (!success)
			throw new IllegalStateException("Was unable to replace " + node
					+ " with " + newNode
					+ ". This should have been prevented by canImport.");

		// End the recording of the paste
		if (!support.isDrop()
				&& UndoManager.getInstance().hasOpenUndoableAction())
			UndoManager.getInstance().endUndoableAction();

		return success;
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
		/*
		 * final E removedComponent;
		 * 
		 * if (data != null) { // if we're moving, we need to clean up the old
		 * nodes from the model if (action == TransferHandler.MOVE) {
		 * removedComponent = this.extractStoryComponents(data); if
		 * (child.getOwner() != null) { ((ComplexStoryComponent)
		 * child.getOwner()) .removeStoryChild(child); } } }
		 * 
		 * // Close any open UndoableActions. if
		 * (UndoManager.getInstance().hasOpenUndoableAction()) {
		 * UndoManager.getInstance().endUndoableAction(); } }
		 */
	}

	/**
	 * Attempts to extract StoryComponents from the Transferable. Returns null
	 * on failure.
	 * 
	 * @param transferData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected E extractStoryComponents(Transferable transferData) {
		E data = null;

		if (transferData
				.isDataFlavorSupported(SEGraphNodeTransferHandler.nodeFlavour)) {
			try {
				data = (E) transferData
						.getTransferData(SEGraphNodeTransferHandler.nodeFlavour);
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
	 * SEGraphNodeTransferable holds a node.
	 * 
	 * @author kschenk
	 */
	protected class SEGraphNodeTransferable implements Transferable {
		private E node;

		public SEGraphNodeTransferable(E node) {
			this.node = node;
		}

		@Override
		public E getTransferData(DataFlavor flavour)
				throws UnsupportedFlavorException, IOException {
			if (flavour.getRepresentationClass() == SEGraphNodeTransferHandler.this.graph
					.getStartNode().getClass()) {
				return this.node;
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
			return new DataFlavor[] { SEGraphNodeTransferHandler.nodeFlavour };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(SEGraphNodeTransferHandler.nodeFlavour);
		}

		@Override
		public String toString() {
			return "StoryComponentPanelTransferable [" + this.node + "]";
		}
	}
}
