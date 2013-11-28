package scriptease.controller.groupvisitor;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * GroupVisitor keeps track of the group of KnowIts that have the same binding
 * as the passed in component in the constructor.
 * 
 * @author kschenk
 * @author jyuen
 */
public abstract class GroupVisitor extends StoryAdapter {

	private List<KnowIt> group;
	protected KnowIt original;

	public List<KnowIt> getGroup() {
		return this.group;
	}

	public GroupVisitor(KnowIt component) {
		this.group = new ArrayList<KnowIt>();
		this.original = component;
		final ScriptIt scriptIt = this.getParentCause(component);
		if (scriptIt != null)
			scriptIt.process(this);
	}

	/**
	 * Returns the parent cause of the given component. Assumes the component
	 * has a cause owner.
	 * 
	 * @param component
	 * @return
	 */
	private ScriptIt getParentCause(StoryComponent component) {
		StoryComponent owner = component;

		while (owner != null && !(owner instanceof CauseIt)) {
			owner = owner.getOwner();
		}
		return (ScriptIt) owner;
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		// add the KnowIt parameters:
		scriptIt.processParameters(this);
		scriptIt.processChildren(this);
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		if (this.isPartOfGroup(knowIt))
			this.group.add(knowIt);

		// Handle the knowIt's resolved binding
		KnowItBinding binding = knowIt.getBinding();

		binding.process(new BindingAdapter() {
			@Override
			public void processFunction(KnowItBindingFunction function) {
				ScriptIt referenced = function.getValue();
				referenced.process(GroupVisitor.this);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				KnowIt referenced = reference.getValue();
				referenced.process(GroupVisitor.this);
			}
		});
	}

	@Override
	public void processAskIt(AskIt askIt) {
		askIt.getCondition().process(this);
		
		this.defaultProcessComplex(askIt);
	}

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		for (StoryComponent child : complex.getChildren()) {
			child.process(this);
		}
	}

	/**
	 * Checks if the given knowIt's binding matches the group's Binding
	 * 
	 * @param knowIt
	 * @return
	 */
	protected abstract boolean isPartOfGroup(KnowIt knowIt);
}
