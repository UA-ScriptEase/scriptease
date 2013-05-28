package scriptease.gui.transfer;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * The Transfer Handler for BindingWidgets that cannot be rebound or dropped
 * onto. Only allows dragging from, not dropping onto. Singleton class via inheritance.
 */
@SuppressWarnings("serial")
public class BindingTransferHandlerExportOnly extends BindingWidgetTransferHandler {
	private static final BindingTransferHandlerExportOnly instance = new BindingTransferHandlerExportOnly();

	// Getter for the Singleton instance.
	public static BindingTransferHandlerExportOnly getInstance() {
		return instance;
	}

	/**
	 * Returns the source actions supported for this TransferHandler.
	 */
	@Override
	public int getSourceActions(JComponent component) {
		return COPY_OR_MOVE;
	}

	/**
	 * Called when the export is completed. NOTE: <code>component</code> is the
	 * source component for the drag/drop.
	 */
	@Override
	protected void exportDone(JComponent component, Transferable transferable, int action) {
		// Never unbind the source when exporting from a picker.
	}
	
	/**
	 * Called whenever something is dragged over a component that has
	 * BindingTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * whether or not this is a valid drop location for the drag.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		// In normal circumstances this method would just return false,
		// but because we want it to be acceptable for effects, descriptions,
		// and controls that wish to get redirected to the parent
		// StoryComponentContainer, we will make an exception.
		return this.canImportComponentsToParent(support);
	}

	/**
	 * Called when a drop is detected on a component that has
	 * BindingTransferHandler set as its TransferHandler. NOTE: The component
	 * contained in <code>support</code> is the destination component. Returns
	 * true if the import was successful, and false if the import failed.
	 */
	@Override
	public boolean importData(TransferSupport support) {
		if (support
				.isDataFlavorSupported(StoryComponentPanelTransferHandler.storyCompFlavour)) {
			return this.importComponentsToParent(support);
		}
		
		return false;
	}
}
