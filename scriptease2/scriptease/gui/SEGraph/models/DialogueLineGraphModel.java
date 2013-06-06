package scriptease.gui.SEGraph.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;

/**
 * The model for a graph that displays {@link DialogueLine}s.
 * 
 * @author kschenk
 * 
 */
public class DialogueLineGraphModel extends SEGraphModel<DialogueLine> {
	private final StoryModel story;

	public DialogueLineGraphModel(StoryModel story, DialogueLine start) {
		super(start);
		this.story = story;
	}

	@Override
	public DialogueLine createNewNode() {
		return this.story.getModule().createDialogueLine();
	}

	@Override
	public boolean overwriteNodeData(DialogueLine existingNode,
			DialogueLine node) {
		// TODO We don't support this yet.
		return false;
	}

	@Override
	public boolean addChild(DialogueLine child, DialogueLine existingNode) {
		return existingNode.addChild(child);
	}

	@Override
	public Collection<DialogueLine> getChildren(DialogueLine node) {
		return node.getChildren();
	}

	@Override
	public Collection<DialogueLine> getParents(DialogueLine target) {
		final Set<DialogueLine> parents = new HashSet<DialogueLine>();

		for (DialogueLine line : this.getNodes()) {
			for (DialogueLine child : line.getChildren()) {
				if (child == target) {
					parents.add(line);
				}
			}
		}

		return parents;
	}

	@Override
	public boolean removeChild(DialogueLine child, DialogueLine existingNode) {
		return existingNode.removeChild(child);
	}
}
