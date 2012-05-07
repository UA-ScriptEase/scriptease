package scriptease.gui.storycomponentbuilder.propertypanel;

import javax.swing.Icon;
import javax.swing.JButton;

import scriptease.model.StoryComponent;

public class ExpansionButtonSCB extends JButton{
	private StoryComponent component;
	private StoryComponent parent;
	
	public ExpansionButtonSCB(Icon icon){
		super(icon);
	}
	
	public StoryComponent getComp(){
		return component;
	}
	
	public StoryComponent getParentComp(){
		return parent;
	}
	
	public void setComp(StoryComponent comp){
		component = comp;
	}
	
	public void setParent(StoryComponent parentComp){
		parent = parentComp;
	}
}