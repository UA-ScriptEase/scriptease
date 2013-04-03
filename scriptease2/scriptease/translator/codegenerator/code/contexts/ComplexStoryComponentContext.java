package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryAdapter;
import scriptease.controller.get.VariableGetter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * Context representing a ComplexStoryComponent
 * 
 * @author mfchurch
 * 
 */
public class ComplexStoryComponentContext extends StoryComponentContext {

	/**
	 * Creates a new ComplexStoryComponentContext.
	 * 
	 * @param other
	 * @param source
	 */
	public ComplexStoryComponentContext(Context other,
			ComplexStoryComponent source) {
		super(other, source);
	}

	/**
	 * Get all of the ComplexStoryComponent's children
	 */
	@Override
	public final Collection<StoryComponent> getChildren() {
		return this.getComponent().getChildren();
	}

	/**
	 * Get all the ScriptIt children of the ComplexStoryComponent. This is used
	 * to get all of the Causes.
	 */
	@Override
	public final Collection<ScriptIt> getScriptIts() {
		final Collection<ScriptIt> scriptIts = new ArrayList<ScriptIt>();
		for (StoryComponent child : this.getChildren()) {
			child.process(new StoryAdapter() {
				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					scriptIts.add(scriptIt);
				}
			});
		}
		return scriptIts;
	}

	/**
	 * Get all of the ComplexStoryComponent's knowIt children
	 */
	@Override
	public Collection<KnowIt> getVariables() {
		final VariableGetter variableGetter = new VariableGetter();

		for (StoryComponent child : this.getChildren()) {
			child.process(variableGetter);
		}

		return variableGetter.getObjects();
	}

	@Override
	protected ComplexStoryComponent getComponent() {
		return (ComplexStoryComponent) super.getComponent();
	}
}
