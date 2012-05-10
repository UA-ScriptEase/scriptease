package scriptease.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import scriptease.gui.graph.editor.GraphEditorButton;
import scriptease.gui.graph.editor.GraphEditorButton.GraphEditorButtonType;

/**
 * ToolBarFactory is responsible for creating JToolBars, most importantly the
 * toolbars for editing graphs.
 * 
 * @author kschenk
 * 
 */
public class ToolBarFactory {
	
	private static JTextField nameField = new JTextField();

	private static ArrayList<GraphEditorButton> graphEditorToolButtons = 
			new ArrayList<GraphEditorButton>();

	public static JButton propButton = new JButton("Properties");

	public static JToolBar buildGraphEditorToolBar() {
		final JToolBar graphEditorToolBar = new JToolBar();
		
		final ButtonGroup graphEditorButtonGroup = new ButtonGroup();

		graphEditorToolBar.setLayout(new BoxLayout(graphEditorToolBar,
				BoxLayout.LINE_AXIS));
		graphEditorToolBar.setRollover(true);
		graphEditorToolBar.setFloatable(false);

		GraphEditorButton select = new GraphEditorButton(
				GraphEditorButtonType.SELECT);
		graphEditorToolButtons.add(select);

		GraphEditorButton insert = new GraphEditorButton(
				GraphEditorButtonType.INSERT);
		graphEditorToolButtons.add(insert);

		GraphEditorButton delete = new GraphEditorButton(
				GraphEditorButtonType.DELETE);
		graphEditorToolButtons.add(delete);

		GraphEditorButton connect = new GraphEditorButton(
				GraphEditorButtonType.CONNECT);
		graphEditorToolButtons.add(connect);

		GraphEditorButton disconnect = new GraphEditorButton(
				GraphEditorButtonType.DISCONNECT);
		graphEditorToolButtons.add(disconnect);

		graphEditorButtonGroup.add(select);
		graphEditorButtonGroup.add(insert);
		graphEditorButtonGroup.add(delete);
		graphEditorButtonGroup.add(connect);
		graphEditorButtonGroup.add(disconnect);
		
		graphEditorToolBar.add(select);
		graphEditorToolBar.add(insert);
		graphEditorToolBar.add(delete);
		graphEditorToolBar.add(connect);
		graphEditorToolBar.add(disconnect);
		
	

		return graphEditorToolBar;
	}

	/**
	 * Creates a JToolBar for the quest editor. It adds all of the graph editor
	 * buttons from the GraphEditorToolbar, and then adds Quest specific 
	 * options for the user after a separator.
	 * 
	 * @return
	 */
	public static JToolBar buildQuestEditorToolBar() {
		final JToolBar questEditorToolBar = buildGraphEditorToolBar();

		questEditorToolBar.addSeparator();

		Dimension minSize = new Dimension(30, 50);
		Dimension prefSize = new Dimension(30, 50);
		Dimension maxSize = new Dimension(Short.MAX_VALUE, 50);
		questEditorToolBar.add(new Box.Filler(minSize, prefSize, maxSize));

		// Temporary Hacks
		questEditorToolBar.add(new JLabel("Name: "));
		questEditorToolBar.add(nameField);
		questEditorToolBar.add(new JButton("><"));
		questEditorToolBar.add(new JSpinner());
		// questEditorToolBar.add(propButton);
		// questEditorToolBar.add(new JButton("Save Changes"));

		return questEditorToolBar;
	}

	public static void updateQuestPointNameField(String textChange) {
		nameField.setText(textChange);
	}

	public static void updateCommittingCheckBox(Boolean committing) {
		if(committing){
			
		}
		else{
			
		}		
	}

	public void updateQuestEditorToolBar(JToolBar questEditorToolBar) {
		// questEditorToolBar.
	}

	public static Collection<GraphEditorButton> getGraphEditorToolButtons() {
		return graphEditorToolButtons;
	}


}
