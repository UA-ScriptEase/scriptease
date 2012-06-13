package scriptease.translator.codegenerator.code.contexts;

import java.util.Iterator;

import scriptease.controller.VariableGetter;
import scriptease.gui.quests.QuestNode;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.StoryItemSequence;
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

	public AskItContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public AskItContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public AskItContext(Context other, AskIt source) {
		this(other);
		component = source;
	}

	/**
	 * Get the AskIt's Condition
	 */
	@Override
	public String getCondition() {
		KnowIt condition = ((AskIt) component).getCondition();
		if (condition != null)
			return this.getNameOf(condition);
		return "";
	}

	/**
	 * Get the AskIt's IfChild
	 */
	@Override
	public StoryItemSequence getIfChild() {
		return ((AskIt) component).getIfBlock();
	}

	/**
	 * Get the AskIt's ElseChild
	 */
	@Override
	public StoryItemSequence getElseChild() {
		return ((AskIt) component).getElseBlock();
	}

	/**
	 * Get the AskIt's condition and it's dependencies
	 */
	@Override
	public Iterator<KnowIt> getVariables() {
		VariableGetter knowItGetter = new VariableGetter();
		((AskIt) component).process(knowItGetter); 

		return knowItGetter.getObjects().iterator();
	}
}
