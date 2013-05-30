package scriptease.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import scriptease.gui.component.SlotPanel;

/**
 * Implementation of Transferable used in SlotPanelTransferHandler components being dragged
 * and dropped into Slot Panels.
 * @author jyuen
 */
public class SlotPanelTransferable implements Transferable {
	private SlotPanel data;
	
	public SlotPanelTransferable(SlotPanel slotPanel) {
		this.data = slotPanel;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		if (flavor.getRepresentationClass() == SlotPanel.class) {
			return this.data;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { SlotPanelTransferHandler.SlotPanelFlavour};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(SlotPanelTransferHandler.SlotPanelFlavour);
	}
}
