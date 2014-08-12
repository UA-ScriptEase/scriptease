package scriptease.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import scriptease.gui.component.BindingWidget;

/**
 * Implementation of Transferable used in BindingTransferHandler for
 * BindingWidget drag and drop functionality.
 * 
 * @author graves
 */
public class BindingTransferable implements Transferable {
	private Object data;

	public BindingTransferable(Object data) {
		this.data = data;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.getRepresentationClass() == BindingWidget.class) {
			return this.data;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { BindingWidgetTransferHandler.KnowItBindingFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(BindingWidgetTransferHandler.KnowItBindingFlavor);
	}
}
