package scriptease.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.storycomponentpanel.StoryComponentPanel;

public class PanelFactory {

	public static JPanel buildQuestPanel(final GraphNode start) {
		JPanel questPanel = new JPanel(new BorderLayout(), true);
		questPanel.setOpaque(true);
		questPanel.setBackground(StoryComponentPanel.UNSELECTED_COLOUR);

		GraphPanel graphPanel = new GraphPanel(start);

		ToolBarButtonAction.addJComponent(graphPanel);

		ToolBarFactory factory = new ToolBarFactory();
		JToolBar graphToolBar = factory.buildGraphEditorToolBar(graphPanel);
		JToolBar questToolBar = factory.buildQuestEditorToolBar(graphPanel);
		
		questPanel.add(graphToolBar.add(questToolBar), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);
		
		return questPanel;
	}

}
