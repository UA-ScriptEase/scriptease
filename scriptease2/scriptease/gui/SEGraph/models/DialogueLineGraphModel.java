package scriptease.gui.SEGraph.models;

import java.util.Collection;

import scriptease.model.semodel.dialogue.DialogueLine;

public class DialogueLineGraphModel extends SEGraphModel<DialogueLine> {

	public DialogueLineGraphModel(DialogueLine start) {
		super(start);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean addChild(DialogueLine child, DialogueLine existingNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DialogueLine createNewNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DialogueLine> getChildren(DialogueLine node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DialogueLine> getParents(DialogueLine node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean overwriteNodeData(DialogueLine existingNode,
			DialogueLine node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeChild(DialogueLine child, DialogueLine existingNode) {
		// TODO Auto-generated method stub
		return false;
	}

}
