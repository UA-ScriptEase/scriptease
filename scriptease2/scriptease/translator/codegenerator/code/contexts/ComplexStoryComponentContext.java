package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import scriptease.controller.StoryAdapter;
import scriptease.controller.get.VariableGetter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * Context representing a ComplexStoryComponent
 * 
 * @author mfchurch
 * 
 */
public class ComplexStoryComponentContext extends StoryComponentContext {

	public ComplexStoryComponentContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public ComplexStoryComponentContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(),
				other.getNamifier(), other.getTranslator(), other
						.getLocationInfo());
	}

	public ComplexStoryComponentContext(Context other,
			ComplexStoryComponent source) {
		this(other);
		this.setComponent(source);
	}

	/**
	 * Get all the ScriptIt children of the ComplexStoryComponent
	 */
	@Override
	public Iterator<ScriptIt> getScriptIts() {
		final Collection<ScriptIt> scriptIts = new ArrayList<ScriptIt>();
		for (StoryComponent child : this.getComponent().getChildren()) {
			child.process(new StoryAdapter() {
				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					scriptIts.add(scriptIt);
				}
			});
		}
		return scriptIts.iterator();
	}

	/**
	 * Get all of the ComplexStoryComponent's knowIt children
	 */
	@Override
	public Iterator<KnowIt> getVariables() {
		final VariableGetter variableGetter = new VariableGetter();

		for (StoryComponent child : this.getComponent().getChildren()) {
			child.process(variableGetter);
		}

		return variableGetter.getObjects().iterator();
	}

	/**
	 * Get all of the ComplexStoryComponent's children
	 */
	@Override
	public Iterator<StoryComponent> getChildren() {
		return this.getComponent().getChildren().iterator();
	}

	@Override
	protected ComplexStoryComponent getComponent() {
		return (ComplexStoryComponent) super.getComponent();
	}
}
