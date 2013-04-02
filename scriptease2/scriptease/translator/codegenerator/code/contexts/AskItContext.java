package scriptease.translator.codegenerator.code.contexts;

import java.util.Iterator;

import scriptease.controller.get.VariableGetter;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * AskItContext is Context for a AskIt object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class AskItContext extends StoryComponentContext {

	public AskItContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public AskItContext(Context other) {
		this(other.getStartStoryPoint(), other.getIndent(),
				other.getNamifier(), other.getTranslator(), other
						.getLocationInfo());
	}

	public AskItContext(Context other, AskIt source) {
		this(other);
		this.setComponent(source);
	}

	/**
	 * Get the AskIt's Condition
	 */
	@Override
	public String getCondition() {
		final KnowIt condition = this.getComponent().getCondition();
		if (condition != null)
			return this.getNameOf(condition);
		return "";
	}

	/**
	 * Get the AskIt's IfChild
	 */
	@Override
	public StoryItemSequence getIfChild() {
		return this.getComponent().getIfBlock();
	}

	/**
	 * Get the AskIt's ElseChild
	 */
	@Override
	public StoryItemSequence getElseChild() {
		return this.getComponent().getElseBlock();
	}

	/**
	 * Get the AskIt's condition and it's dependencies
	 */
	@Override
	public Iterator<KnowIt> getVariables() {
		final VariableGetter knowItGetter = new VariableGetter();

		this.getComponent().process(knowItGetter);

		return knowItGetter.getObjects().iterator();
	}

	@Override
	protected AskIt getComponent() {
		return (AskIt) super.getComponent();
	}
}
