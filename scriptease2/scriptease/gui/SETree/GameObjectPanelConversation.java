package scriptease.gui.SETree;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import scriptease.gui.SETree.cell.ScriptWidgetFactory;
import scriptease.translator.io.model.GameConstant;

/**
 * It looks like this class is specifically for making conversation game objects
 * shorter, so that when you click on them, the longer view appears. We might be
 * able to add the functionality of indenting conversations into this class
 * instead of wherever it is now.
 * 
 */
public class GameObjectPanelConversation extends GameObjectPanel implements MouseListener {
	private boolean isViewShortText;
	
	public GameObjectPanelConversation(GameConstant gameObject, int horStrut) {
		super(gameObject, horStrut);
		isViewShortText = true;
	}
	
	private void setText(){
		String set;
		if(isViewShortText)
			set = shortViewText;
		else
			set = regularText;
		
		for(Object jcomponent : gameObjectBindingWidget.getComponents()){
			if (jcomponent instanceof JLabel){
				gameObjectBindingWidget.remove((JComponent)jcomponent);
			}
		}
		gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(set,
				Color.WHITE));
		
		gameObjectBindingWidget.setBorder(BorderFactory.createEmptyBorder(
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE));
		
		gameObjectBindingWidget.revalidate();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//setChanged();
		//notifyObservers();
				
		isViewShortText = !isViewShortText;
		if(regularText != shortViewText){
			setText();
		}
		backgroundPanel.setBackground(SELECTED_COLOUR);
		e.consume();
	}
	
}
