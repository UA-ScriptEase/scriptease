package scriptease.gui.SETree.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * TransferHandler used to proxy all events to the given JComponent's
 * TransferHandler
 * 
 * Great way to work around Java's horrible lack of layers in TransferHandling
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class ProxyTransferHandler extends TransferHandler {
	private JComponent proxy;

	public ProxyTransferHandler(JComponent object) {
		if (object == null)
			throw new IllegalArgumentException("object cannot be null");
		if (object.getTransferHandler() == null)
			throw new IllegalArgumentException(
					"object must have a transfer handler");
		this.proxy = object;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return this.proxy.getTransferHandler().canImport(comp, transferFlavors);
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		return this.proxy.getTransferHandler().importData(comp, t);
	}

	@Override
	public boolean canImport(TransferSupport support) {
		TransferSupport proxySupport = new TransferSupport(this.proxy,
				support.getTransferable());
		return this.proxy.getTransferHandler().canImport(proxySupport);
	}

	@Override
	public boolean importData(TransferSupport support) {
		TransferSupport proxySupport = new TransferSupport(this.proxy,
				support.getTransferable());
		return this.proxy.getTransferHandler().importData(proxySupport);
	}
}
