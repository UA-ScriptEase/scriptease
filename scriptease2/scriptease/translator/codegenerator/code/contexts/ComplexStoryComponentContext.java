package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.AskItGetter;
import scriptease.controller.ImplicitGetter;
import scriptease.controller.VariableGetter;
import scriptease.gui.quests.QuestNode;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
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

	public ComplexStoryComponentContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public ComplexStoryComponentContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public ComplexStoryComponentContext(Context other,
			ComplexStoryComponent source) {
		this(other);
		component = source;
	}

	/**
	 * Get all the ScriptIt children of the ComplexStoryComponent
	 */
	@Override
	public Iterator<ScriptIt> getScriptIts() {
		final Collection<ScriptIt> scriptIts = new ArrayList<ScriptIt>();
		for (StoryComponent child : ((ComplexStoryComponent) component)
				.getChildren()) {
			child.process(new AbstractNoOpStoryVisitor() {
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
		VariableGetter variableGetter = new VariableGetter();
		Collection<StoryComponent> children = ((ComplexStoryComponent) component)
				.getChildren();
		for (StoryComponent child : children) {
			child.process(variableGetter);
		}
		return variableGetter.getObjects().iterator();
	}

	@Override
	public Iterator<KnowIt> getImplicits() {
		ImplicitGetter implicitGetter = new ImplicitGetter();
		Collection<StoryComponent> children = ((ComplexStoryComponent) component)
				.getChildren();
		for (StoryComponent child : children) {
			child.process(implicitGetter);
		}
		return implicitGetter.getObjects().iterator();
	}

	/**
	 * Get all of the ComplexStoryComponent's children
	 */
	@Override
	public Iterator<StoryComponent> getChildren() {
		return ((ComplexStoryComponent) component).getChildren().iterator();
	}

	/**
	 * Get all the AskIt children of the ComplexStoryComponent
	 */
	@Override
	public Iterator<AskIt> getAskIts() {
		AskItGetter askItGetter = new AskItGetter();
		Collection<StoryComponent> children = ((ComplexStoryComponent) component)
				.getChildren();
		for (StoryComponent child : children) {
			child.process(askItGetter);
		}
		return askItGetter.getObjects().iterator();
	}
}
