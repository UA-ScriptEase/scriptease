package scriptease.translator.codegenerator;

import javax.swing.JPanel;

import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;

public interface GameObjectPicker {
	public JPanel getPickerPanel();

	public void onWidgetClicked(KnowItBindingConstant object);

	public void onWidgetHovered(BindingWidget widget);

	public void onWidgetUnHovered();
}
