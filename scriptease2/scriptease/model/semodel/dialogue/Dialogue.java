package scriptease.model.semodel.dialogue;

import java.util.Collection;

public class Dialogue {
	private String name;
	private final Collection<DialogueLine> roots;

	public Dialogue(String name, Collection<DialogueLine> roots) {
		this.name = name;
		this.roots = roots;
	}

	public Collection<DialogueLine> getRoots() {
		return this.roots;
	}

	public String getName() {
		return this.name;
	}
}
