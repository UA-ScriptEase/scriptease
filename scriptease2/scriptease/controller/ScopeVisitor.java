package scriptease.controller;

import java.util.ArrayList;
import java.util.List;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/**
 * A visitor class that determines the <code>KnowIt</code>s in scope from any
 * given <code>StoryComponent</code>.<br>
 * <br>
 * The <code>KnowIt</code>s in scope at a given <code>StoryComponent</code> are
 * defined recursively as:<br>
 * The <code>KnowIt</code> siblings of the given <code>StoryComponent</code>,
 * the given <code>StoryComponent</code>'s parent's parameters, and the
 * <code>KnowIt</code>s in scope from the parent.
 * 
 * @author graves
 * @author mfchurch
 */
public class ScopeVisitor extends AbstractNoOpStoryVisitor {
	private List<KnowIt> scope;
	private StoryComponent originalComponent;

	public static List<KnowIt> getScope(StoryComponent component) {
		ArrayList<KnowIt> finalScope = new ArrayList<KnowIt>();
		ScopeVisitor scopeVisitor = new ScopeVisitor(finalScope, component);

		component.process(scopeVisitor);
		return finalScope;
	}

	private ScopeVisitor(List<KnowIt> newScope, StoryComponent component) {
		scope = newScope;
		originalComponent = component;
	}

	@Override
	public void processAskIt(AskIt askIt) {
		scope.add(askIt.getCondition());
		processStoryComponent(askIt);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		processStoryComponent(scriptIt);

		if (scriptIt.isCause()) {
			for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
				final KnowIt subject = codeBlock.getSubject();
				// if the originalComponent was not the subject of the startIt
				if (!originalComponent.equals(subject)) {
					scope.addAll(scriptIt.getImplicits());
					scope.add(subject);
				}
			}
		}
	}

	@Override
	public void processKnowIt(KnowIt knowIt) {
		StoryComponent owner = knowIt.getOwner();
		if (owner != null) {
			if (owner instanceof ScriptIt
					&& !((ScriptIt) owner).getParameters().contains(knowIt)) {
				// add all sibling KnowIts
				scope.addAll(this.getKnowItSiblings(knowIt));
				// add all parameters of the parent
				scope.addAll(((ScriptIt) owner).getParameters());
			}
			owner.process(this);
		}
	}

	@Override
	public void processStoryItemSequence(StoryItemSequence sequence) {
		processStoryComponent(sequence);
	}

	@Override
	public void processStoryComponentContainer(StoryComponentContainer component) {
		processStoryComponent(component);
	}

	/**
	 * Gets the children of the parent of the given StoryComponent, filters out
	 * the KnowIt children that are different from the given StoryComponent, and
	 * returns them.
	 * 
	 * @param component
	 *            The component whose siblings should be returned.
	 * @return The List of KnowIt siblings for the given component.
	 */
	private List<KnowIt> getKnowItSiblings(StoryComponent component) {
		StoryComponent owner = component.getOwner();
		List<KnowIt> knowItChildren = new ArrayList<KnowIt>();
		if (owner != null && owner instanceof ComplexStoryComponent) {
			List<StoryComponent> children = ((ComplexStoryComponent) owner)
					.getChildren();
			if (children != null && children.contains(component)) {
				for (StoryComponent child : children) {
					if (child instanceof KnowIt && child != component) {
						// Add the knowIt's BEFORE component is reached, since
						// the order is important!
						knowItChildren.add((KnowIt) child);
					} else if (child == component)
						break;
				}
			}
		}
		return knowItChildren;
	}

	private void processStoryComponent(StoryComponent component) {
		StoryComponent owner = component.getOwner();
		if (owner != null) {
			// add the KnowIt siblings:
			scope.addAll(this.getKnowItSiblings(component));
			// add the parent's parameters:
			if (owner instanceof ScriptIt)
				scope.addAll(((ScriptIt) owner).getParameters());
			// get the scope of the parent:
			owner.process(this);
		}
	}
}
