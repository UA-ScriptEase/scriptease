package scriptease.gui.SETree;

import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;


/**
 * Tree like representation of objects used  representation of a tree, 
 *
 * 
 **/ 
enum ElementTreeType{
	GAME_OBJECT, STORY_COMPONENT;
}

@SuppressWarnings("serial")
public abstract class SEElementTree extends JScrollPane implements Observer {
	//Pane has a one-to-one match with this Model
	protected SETreeModel treeModel;
	
	public abstract JComponent getRoot();
	public abstract void setRoot();
	public abstract void filterTree();
	public abstract void updatePaneToMatchModel();
	
	
}
