package scriptease.gui.quests;

import java.awt.BorderLayout;

import javax.swing.JToolBar;

import scriptease.gui.ToolBarFactory;
import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.editor.GraphEditor;
import scriptease.gui.graph.nodes.GraphNode;

/**
 * Editor used for building Quests. Users select the active tool, which dictates
 * what actions should be taken when a GraphNode is clicked. Observes all of the
 * GraphNodes in the graph in order to know when nodes are clicked.
 * 
 * @author mfchurch
 * @author graves (refactored)
 * @author kschenk (refactored further)
 */
@SuppressWarnings("serial")
public class QuestEditor extends GraphEditor {

	private JToolBar questToolBar; 

	public QuestEditor(final GraphNode start) {
		super();
		this.setHeadNode(start);
		this.buildPanels();

		ToolBarFactory factory = new ToolBarFactory();
		
		questToolBar = factory.buildQuestEditorToolBar(this);

		this.add((ToolBarFactory.buildGraphEditorToolBar(this).add(
				questToolBar)), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

	}
}
