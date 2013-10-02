package scriptease.model.complex.behaviours;

import scriptease.model.complex.ComplexStoryComponent;

/**
 * A Behaviour represents a series of Tasks {@link Task}. A Behaviour can be
 * independent {@link IndependentBehaviour} or collaborative
 * {@link CollaborativeBehaviour}. An independent behaviour is one that is
 * executed by only one subject, while a collaborative behaviour has many
 * respondants to the subject in execution.
 * 
 * For example, an independent behaviour could be a subject walking around
 * randomly by him/herself. A collaborative behaviour could be a tavern patron
 * interacting with the bartender to perform a order drink behaviour. The patron
 * should be able to have unlimited collaborators.
 * 
 * @author jyuen
 */
public abstract class Behaviour extends ComplexStoryComponent {

	public Behaviour(String name) {
		super(name);

		this.registerChildType(ProactiveTaskTree.class, 1);
	}
}
