package scriptease.translator.codegenerator.code.contexts.knowitbindingcontext;

import java.util.Collection;

import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingQuestPoint;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.ContextFactory;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * Code generation Context for a KnowItBindingQuestPoint object.
 * 
 * @see Context
 * @see KnowItBindingContext
 * @author remiller
 */
public class KnowItBindingQuestPointContext extends KnowItBindingContext {

	public KnowItBindingQuestPointContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public KnowItBindingQuestPointContext(Context other) {
		super(other);
	}

	public KnowItBindingQuestPointContext(Context other, KnowItBinding source) {
		this(other);
		binding = source;
	}
	
	@Override
	public String getFormattedValue() {
		final Collection<FormatFragment> typeFormat;
		
		System.out.println("derp");
		
		typeFormat = this.translator.getGameTypeManager().getFormat(
				QuestPoint.QUEST_POINT_TYPE);
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return FormatFragment.resolveFormat(typeFormat, this);
	}

	/**
	 * Get the KnowItBinding's QuestPoint Name
	 */
	@Override
	public String getValue() {
		final QuestPoint qp = ((KnowItBindingQuestPoint) this.binding)
				.getValue();
		final Context knowItContext;

		knowItContext = ContextFactory.getInstance().createContext(this, qp);
		
		return knowItContext.getName();
	}
}