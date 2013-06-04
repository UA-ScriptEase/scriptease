package scriptease.gui.SEGraph.renderers;

import javax.swing.JComponent;

import scriptease.gui.SEGraph.SEGraph;
import scriptease.model.semodel.dialogue.DialogueLine;

public class DialogueLineRenderer extends SEGraphNodeRenderer<DialogueLine> {

	public DialogueLineRenderer(SEGraph<DialogueLine> graph) {
		super(graph);
	}

	@Override
	protected void configureInternalComponents(JComponent component,
			DialogueLine node) {
		// TODO Auto-generated method stub
		super.configureInternalComponents(component, node);
	}

}
