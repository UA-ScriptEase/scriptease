package scriptease.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.action.ToolBarButtonAction.ToolBarButtonMode;
import scriptease.gui.graph.GraphPanel;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.pane.GameObjectPane;
import scriptease.gui.pane.LibraryPane;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.GameObjectPicker;

/**
 * A factory class for different panels. All major panel construction should go
 * in here.
 * 
 * @author kschenk
 * 
 */
public class PanelFactory {
	private static PanelFactory instance = new PanelFactory();

	public static PanelFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a panel for editing Quests.
	 * 
	 * @param start
	 *            Start Point of the graph.
	 * @return
	 */
	public JPanel buildQuestPanel(final GraphNode start) {
		final JPanel questPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);
		final JToolBar questToolBar = ToolBarFactory
				.buildQuestEditorToolBar(graphPanel);

		questPanel.add(graphToolBar.add(questToolBar), BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		questPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return questPanel;
	}

	/**
	 * Creates a panel for editing DescribeIts.
	 * 
	 * @param start
	 *            Start Point of the graph
	 * @return
	 */
	public JPanel buildDescribeItPanel(final GraphNode start,
			final DescribeIt describeIt) {
		final JPanel describeItPanel = new JPanel(new BorderLayout(), true);
		final GraphPanel graphPanel = new GraphPanel(start);

		DescribeIt editedDescribeIt = describeIt.clone();
		editedDescribeIt.clearSelection();

		graphPanel.setHeadNode(editedDescribeIt.getHeadNode());

		ToolBarButtonAction.addJComponent(graphPanel);

		final JToolBar graphToolBar = ToolBarFactory
				.buildGraphEditorToolBar(graphPanel);

		final JToolBar describeItToolBar = ToolBarFactory
				.buildDescribeItToolBar(editedDescribeIt, graphPanel);

		describeItPanel.add(graphToolBar.add(describeItToolBar),
				BorderLayout.PAGE_START);

		ToolBarButtonAction.setMode(ToolBarButtonMode.SELECT_GRAPH_NODE);

		describeItPanel.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

		return describeItPanel;
	}

	/**
	 * Builds a pane containing all game objects in the active module, organized
	 * by category, allowing the user to drag them onto bindings in a Story.
	 * 
	 * @return A JPanel GameObject picker.
	 * @author graves
	 * @author mfchurch
	 */
	public JPanel buildGameObjectPane(StoryModel model) {
		GameObjectPicker picker;

		if (model != null) {
			Translator translator = model.getTranslator();
			if (translator != null) {
				// Get the picker
				if ((picker = translator.getCustomGameObjectPicker()) == null) {
					picker = new GameObjectPane();
				}
				return picker.getPickerPanel();
			}
		}
		// otherwise return an empty hidden JPanel
		JPanel jPanel = new JPanel();
		jPanel.setVisible(false);
		return jPanel;
	}
	
	public JPanel buildLibraryPane(boolean showInvisible) {
		return new LibraryPane(showInvisible);
	}
}