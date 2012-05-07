package scriptease.gui.action.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JWindow;

//hmm lets see if this is even required!
public class SEPopUpMenu extends AbstractAction{
	//exits when mouse exits hover
	private JWindow popUpWindow;
	private JScrollPane popUpContent;
	//should hold its component data what ever it is so that it can be accessed
	
	//good way to get data from the thingies...
	
	public SEPopUpMenu(){
		popUpContent = new JScrollPane();
		popUpWindow = new JWindow();
		popUpWindow.setVisible(false);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void setContent(ArrayList<Object> objectToFill){
		int amountOfItems = objectToFill.size();
		for(int i=0; i < amountOfItems; i++){
			//Add check to make sure of the right instance	
			if(checkJComponantInstane(objectToFill.get(i)))
					popUpContent.add((Component) objectToFill.get(i));
		}
		
	}
	
	public void popMeUp(){
		popUpWindow.setVisible(true);
	}
	
	private boolean checkJComponantInstane(Object a){
		if(a instanceof Component)
			return true;
		return false;
	}
	
	public JWindow getPopUpWindow(){
		return popUpWindow;
	}
	
	public JScrollPane getPopUpContentPane(){
		return popUpContent;
	}

	
}
