package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JPopupMenu;



public abstract class SEMultiSelector extends Observable implements ActionListener {
	protected String TYPES_LABELS;
	//protected MultiSelectorContext selectorContext;
	protected JButton rootTypeLabelMenu;
	protected ArrayList<String> rootData;
	protected ArrayList<String> data;
	
	public SEMultiSelector(ArrayList<String> rootData){
		//selectorContext = context;
		rootTypeLabelMenu = new JButton(setLabel());
		rootTypeLabelMenu.addActionListener(this);
		//data = new ArrayList<String>();
		
		this.rootData = new ArrayList<String>();
		this.rootData = rootData;
	}
	
	protected abstract String setLabel();
	protected abstract void populateMenu(final JPopupMenu a);
	
	public void setData(ArrayList<String> theData){
		data = theData;
	}
	
	public JButton getRootButton(){
		return rootTypeLabelMenu;
	}
	
	protected JPopupMenu popMeUp(){
		final JPopupMenu a = new JPopupMenu(setLabel());
		populateMenu(a);
		return a;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/*if(e.getSource() instanceof JButton)
			popMeUp().show((JComponent)e.getSource(), ((JComponent)e.getSource()).getWidth(), 0);
		
		if(e.getSource() instanceof JCheckBoxMenuItem){
			if(((JCheckBoxMenuItem)e.getSource()).isSelected())
				data.add(((JCheckBoxMenuItem)e.getSource()).getText());
			else
				data.remove(((JCheckBoxMenuItem)e.getSource()).getText());
			setChanged();
			notifyObservers(data);
		}*/
	}
	
	protected final class MenuVisibilityHandler implements ActionListener {
		private final JPopupMenu menu;
		
		public MenuVisibilityHandler(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.menu.setVisible(true);
		}
	}
	
	
}
