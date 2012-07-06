package scriptease.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

public abstract class SEMultiSelector extends Observable implements ActionListener {
	protected String TYPES_LABELS;
	protected JButton rootTypeLabelMenu;
	protected ArrayList<String> rootData;
	protected ArrayList<String> data;
	
	public SEMultiSelector(ArrayList<String> rootData){
		rootTypeLabelMenu = new JButton(setLabel());
		rootTypeLabelMenu.addActionListener(this);
		
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
	
	protected JPopupMenu popUpMenu(){
		final JPopupMenu a = new JPopupMenu(setLabel());
		populateMenu(a);
		return a;
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
