package scriptease.controller.observer;

import scriptease.gui.libraryeditor.ParameterPanel;

/**
 * Listens to changes for ParameterPanel {@link ParameterPanel}
 * 
 * @author jyuen
 */
public interface ParameterPanelObserver {
	/**
	 * Notifies the receiver that the parameterPanel has changed
	 */
	public void parameterPanelChanged();
}
