package scriptease.gui.SETree.cell;

import java.awt.Graphics;
import java.util.Collection;

import javax.swing.JPanel;

import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;

public class TypeWidgetCircleContainer extends JPanel{
	private JPanel visualTypeContainer;
	private final KnowIt knowIt;
	
	public TypeWidgetCircleContainer(KnowIt kI){
		knowIt = kI;
		
		final KnowItBinding binding = knowIt.getBinding();
		final Collection<String> types = knowIt.getAcceptableTypes();
		
	}
	
	public void paintComponent(Graphics g){
		int x = 1;
		int y = 1;
		int radius = 20;
		g.drawOval(x, y, radius, radius);
	}
	
	/*public static JComponent populateLegalTypesPanel(JPanel typePanel,
			KnowIt knowIt) {
		typePanel.removeAll();
		TypeWidget slotTypeWidget;
		final KnowItBinding binding = knowIt.getBinding();

		final Collection<String> types = knowIt.getAcceptableTypes();

		// for each type the KnowIt can accept
		//This is types for the other thing
		for (String type : types) {
			slotTypeWidget = ScriptWidgetFactory.buildTypeWidget(type);
			slotTypeWidget.setSelected(true);

			// the colour depends on the actual binding of the KnowIt
			if (!binding.isBound()) {
				slotTypeWidget.setBackground(ScriptEaseUI.COLOUR_UNBOUND);
			} else
				slotTypeWidget.setBackground(ScriptEaseUI.COLOUR_BOUND);

			// only show types that are unbound possibilities
			if (!binding.isBound() || !binding.getTypes().contains(type)) {
				typePanel.add(slotTypeWidget);
			}
		}

		return typePanel;
	}*/

}
