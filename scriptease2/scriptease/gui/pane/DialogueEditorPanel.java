package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.SEGraph.observers.SEGraphAdapter;
import scriptease.gui.component.ComponentFactory;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.util.StringOp;

/**
 * This panel contains a Dialogue Line Graph.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class DialogueEditorPanel extends JPanel {
	private final StoryModel model;

	public DialogueEditorPanel(StoryModel model) {
		this.model = model;

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	/**
	 * Sets the dialogue line to be edited. A button used to go back to the
	 * story must be passed in.
	 * 
	 * @param dialogueLine
	 * @param backToStory
	 */
	public void setDialogueLine(DialogueLine dialogueLine, JButton backToStory) {
		this.removeAll();

		final SEGraph<DialogueLine> graph;
		final JToolBar graphToolBar;
		final JScrollPane graphScrollPane;

		graph = SEGraphFactory.buildDialogueLineGraph(this.model, dialogueLine);
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();
		graphScrollPane = new JScrollPane(graph);

		graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
				Color.LIGHT_GRAY));
		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());

		if (backToStory != null)
			this.add(backToStory, BorderLayout.EAST);

		graph.addSEGraphObserver(new SEGraphAdapter<DialogueLine>() {
			@Override
			public void nodeAdded(DialogueLine newNode,
					Collection<DialogueLine> children,
					Collection<DialogueLine> parents) {
				final StoryModel story;

				story = SEModelManager.getInstance().getActiveStoryModel();

				if (story != null) {
					final String dialogueType;

					dialogueType = story.getModule().getDialogueType();

					if (StringOp.exists(dialogueType))
						ResourcePanel.getInstance().refreshCategory(
								dialogueType);
				}
			}
		});

		this.add(graphToolBar, BorderLayout.WEST);
		this.add(graphScrollPane);

		this.repaint();
		this.revalidate();
	}
}
