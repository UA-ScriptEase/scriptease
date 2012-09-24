package scriptease.gui.transfer;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import scriptease.gui.cell.BindingWidget;

/**
 * A TransferHandler for the JTree in the Default Game Object picker panel. This
 * only allows dragging of Game Objects from the tree, and does not allow
 * dropping Game Objects onto the tree or moving them around within the tree. It
 * is a Singleton class for efficiency.
 * 
 * @author graves
 */
@SuppressWarnings("serial")
public class DefaultPickerTransferHandlerExportOnly extends
		BindingTransferHandlerExportOnly {
	// Singleton instance of the TransferHandler.
	private static final DefaultPickerTransferHandlerExportOnly instance = new DefaultPickerTransferHandlerExportOnly();

	// Getter for the Singleton instance.
	public static DefaultPickerTransferHandlerExportOnly getInstance() {
		return instance;
	}

	/**
	 * Creates a Transferable for the node dragged.
	 * 
	 * <ul>
	 * <li>If the node houses a GameObject, a BindingTransferable is created,
	 * which allows the user to drop the GameObject onto a BindingWidget.</li>
	 * <li>If the node doesn't house a GameObject, null is returned, which ends
	 * the drag and drop operation. This is done so that users cannot interact
	 * with non-GameObject parts of the tree.</li>
	 * </ul>
	 */
	@Override
	protected Transferable createTransferable(JComponent sourceComponent) {
		JTree tree = (JTree) sourceComponent;
		TreePath selectedPath = tree.getSelectionPath();
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath
				.getLastPathComponent();

		if (!(selectedNode.getUserObject() instanceof BindingWidget))
			return null;
		//GameObjectPanel a = ((GameObjectPanel)sourceComponent.get;
		
	

		BindingWidget BindingWidget = (BindingWidget) selectedNode
				.getUserObject();

		return new BindingTransferable(BindingWidget);
	}
}
