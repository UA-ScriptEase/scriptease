package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;

import scriptease.gui.quests.StoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Code generation Context for a KnowItBindingQuestPoint object.
 * 
 * @see Context
 * @see KnowItBindingContext
 * @author remiller
 */
public class KnowItBindingQuestPointContext extends KnowItBindingContext {

	public KnowItBindingQuestPointContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingQuestPointContext(Context other) {
		super(other);
	}

	public KnowItBindingQuestPointContext(Context other, KnowItBinding source) {
		this(other);
		this.binding = source;
	}
	
	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;
		
		typeFormat = this.translator.getGameTypeManager().getFormat(
				StoryPoint.QUEST_POINT_TYPE);
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's QuestPoint Name
	 */
	@Override
	public String getValue() {
		final StoryPoint qp = ((KnowItBindingStoryPoint) this.binding)
				.getValue();
		final Context knowItContext;

		knowItContext = ContextFactory.getInstance().createContext(this, qp);
		
		return knowItContext.getName();
	}
}