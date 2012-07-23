package scriptease.controller;

import java.util.ArrayList;
import java.util.List;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A visitor class that determines the <code>KnowIt</code>s in scope from any
 * given <code>StoryComponent</code>, <i>component</i>.<br>
 * <br>
 * The <code>KnowIt</code>s in scope at a given <code>StoryComponent</code> are
 * defined recursively as:
 * <ol>
 * <li><code>KnowIt</code>s in scope from the parent.</li>
 * <li>Prior <code>KnowIt</code> siblings of <i>component</i></li>
 * <li><i>Component</i>'s parent's parameters and AskIt conditions</li>
 * </ul> Use {@link #getScope(StoryComponent)} to get the scope of that
 * StoryComponent using this visitor.
 * 
 * @author graves
 * @author mfchurch
 * @author remiller
 */
public class ScopeVisitor extends AbstractNoOpStoryVisitor {
	private final List<KnowIt> scope;
	private final StoryComponent targetComponent;

	/**
	 * Gets the scope of the given Story Component recursively, as defined in
	 * {@link ScopeVisitor}
	 * 
	 * @param component
	 *            The component whose scope is to be determined.
	 * @return The scope of the given component.
	 */
	public static List<KnowIt> getScope(StoryComponent component) {
		final ScopeVisitor scopeVisitor = new ScopeVisitor(component);

		component.process(scopeVisitor);

		return scopeVisitor.scope;
	}

	/**
	 * Private constructor because clients are supposed to use
	 * {@link #getScope(StoryComponent)}.
	 */
	private ScopeVisitor(StoryComponent component) {
		this.scope = new ArrayList<KnowIt>();
		this.targetComponent = component;
	}

	/**
	 * Gets the KnowIt siblings that precede the given StoryComponent.
	 * 
	 * @param component
	 *            The component whose prior siblings should be returned.
	 * @return The List of KnowIt siblings for the given component.
	 */
	private List<KnowIt> getPriorKnowIts(StoryComponent component) {
		final StoryComponent owner = component.getOwner();
		final List<KnowIt> priorKnowIts = new ArrayList<KnowIt>();
		final List<StoryComponent> children;

		if (owner != null && owner instanceof ComplexStoryComponent) {
			children = ((ComplexStoryComponent) owner).getChildren();

			/*
			 * Sanity check: only get siblings if the given component is
			 * actually owned by the owner as a child. Parameters are owned, but
			 * not children, for example.
			 */
			if (children != null && children.contains(component)) {

				// now we can get each prior sibling
				for (StoryComponent child : children) {
					if (child == component) {
						// stop once we reach the given component, since
						// children after it can't be in scope.
						break;
					} else if (child instanceof KnowIt) {
						priorKnowIts.add((KnowIt) child);
					}
				}
			}
		}

		return priorKnowIts;
	}
	
	@Override
	protected void defaultProcess(StoryComponent component) {
		final StoryComponent owner = component.getOwner();
		
		if (owner != null) {
			owner.process(this);
		}
		
		this.scope.addAll(this.getPriorKnowIts(component));
	}

	@Override
	public void processAskIt(AskIt askIt) {
		this.defaultProcess(askIt);

		if (askIt != this.targetComponent)
			this.scope.add(askIt.getCondition());
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		KnowIt subject;

		this.defaultProcess(scriptIt);

		if (scriptIt != this.targetComponent && scriptIt.isCause()) {

			for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
				subject = codeBlock.getSubject();

				// if the originalComponent was not the subject of the
				// startIt
				if (!this.targetComponent.equals(subject)) {
					// only inherit parameters from Causes because you can't add
					// things to scope within an Effect's parameter list.
					this.scope.addAll(codeBlock.getParameters());
					
					this.scope.addAll(scriptIt.getImplicits());
					this.scope.add(subject);
				}
			}
		}
	}
}
