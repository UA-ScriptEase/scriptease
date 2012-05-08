package scriptease.gui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JToolBar;

import scriptease.gui.graph.editor.GraphEditorButton;
import scriptease.gui.graph.editor.GraphEditorButton.GraphEditorButtonType;

public class ToolBarFactory {
	
	private static ArrayList<GraphEditorButton> graphEditorToolButtons = new ArrayList<GraphEditorButton>();
	
	public static JButton propButton = new JButton("Properties");

	public static JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar = new JToolBar();
		
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);
		
		GraphEditorButton select = new GraphEditorButton(GraphEditorButtonType.SELECT);
		graphEditorToolButtons.add(select);
		
		GraphEditorButton insert = new GraphEditorButton(GraphEditorButtonType.INSERT);
		graphEditorToolButtons.add(insert);
		
		GraphEditorButton delete = new GraphEditorButton(GraphEditorButtonType.DELETE);
		graphEditorToolButtons.add(delete);
		
		GraphEditorButton connect = new GraphEditorButton(GraphEditorButtonType.CONNECT);
		graphEditorToolButtons.add(connect);
		
		GraphEditorButton disconnect = new GraphEditorButton(GraphEditorButtonType.DISCONNECT);
		graphEditorToolButtons.add(disconnect);
		
		graphEditorToolBar.add(select);
		graphEditorToolBar.add(insert);
		graphEditorToolBar.add(delete);
		graphEditorToolBar.add(connect);
		graphEditorToolBar.add(disconnect);
		
		return graphEditorToolBar;
	}
	
	/**
	 * Creates a JToolBar for the quest editor. It adds all
	 * of the graph editor buttons from the GraphEditorToolbar,
	 * and then adds Quest specific options for the user
	 * after a separator.
	 * 
	 * @return
	 */
	public static JToolBar buildQuestEditorToolBar() {
		final JToolBar questEditorToolBar = buildGraphEditorToolBar();
		
		questEditorToolBar.addSeparator();
		
		//Temporary Hacks
		questEditorToolBar.add(propButton);
		questEditorToolBar.add(new JButton("Save Changes"));
		
		return questEditorToolBar;
	}
	
	public static Collection<GraphEditorButton> getGraphEditorToolButtons(){
		return graphEditorToolButtons;
	}	


}
