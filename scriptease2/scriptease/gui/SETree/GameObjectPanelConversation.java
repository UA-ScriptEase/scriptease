package scriptease.gui.SETree;
import java.awt.Color;
import java.awt.event.MouseEvent;

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
public class GameObjectPanelConversation extends GameObjectPanel {
	private boolean isViewShortText;
	
	public GameObjectPanelConversation(GameConstant gameObject, int horStrut) {
		super(gameObject, horStrut);
		this.isViewShortText = true;
	}
	
	private void setText(){
		String set;
		if(this.isViewShortText)
			set = this.shortViewText;
		else
			set = this.regularText;
		
		for(Object jcomponent : this.gameObjectBindingWidget.getComponents()){
			if (jcomponent instanceof JLabel){
				this.gameObjectBindingWidget.remove((JComponent)jcomponent);
			}
		}
		this.gameObjectBindingWidget.add(ScriptWidgetFactory.buildLabel(set,
				Color.WHITE));
		
		this.gameObjectBindingWidget.setBorder(BorderFactory.createEmptyBorder(
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE,
				ScriptWidgetFactory.TOTAL_ROW_BORDER_SIZE));
		
		this.gameObjectBindingWidget.revalidate();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//setChanged();
		//notifyObservers();
				
		this.isViewShortText = !this.isViewShortText;
		if(this.regularText != this.shortViewText){
			setText();
		}
		this.backgroundPanel.setBackground(SELECTED_COLOUR);
		e.consume();
	}
	
}
