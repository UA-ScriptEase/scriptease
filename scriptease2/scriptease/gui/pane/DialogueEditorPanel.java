package scriptease.gui.pane;

import javax.swing.JPanel;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.gui.SEGraph.SEGraphFactory;
import scriptease.model.semodel.dialogue.DialogueLine;

public class DialogueEditorPanel extends JPanel {

	public DialogueEditorPanel(DialogueLine dialogueLine) {
		final SEGraph<DialogueLine> graph;

		graph = SEGraphFactory.buildDialogueLineGraph(dialogueLine);

		this.add(graph);
	}
}
