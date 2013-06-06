package scriptease.gui.pane;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.gui.component.ComponentFactory;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;

@SuppressWarnings("serial")
public class DialogueEditorPanel extends JPanel {
	private final StoryModel model;

	private DialogueLine dialogueLine;

	public DialogueEditorPanel(StoryModel model) {
		this.model = model;

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	public void setDialogueLine(DialogueLine dialogueLine, JButton backToStory) {
		this.dialogueLine = dialogueLine;

		this.removeAll();

		final SEGraph<DialogueLine> graph;
		final JToolBar graphToolBar;
		final JScrollPane graphScrollPane;

		graph = SEGraphFactory.buildDialogueLineGraph(model, dialogueLine);
		graphToolBar = ComponentFactory.buildGraphEditorToolBar();
		graphScrollPane = new JScrollPane(graph);

		graphToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
				Color.LIGHT_GRAY));
		graphScrollPane.setBorder(BorderFactory.createEmptyBorder());

		if (backToStory != null)
			this.add(backToStory, BorderLayout.EAST);

		this.add(graphToolBar, BorderLayout.WEST);
		this.add(graphScrollPane);
	}
}
