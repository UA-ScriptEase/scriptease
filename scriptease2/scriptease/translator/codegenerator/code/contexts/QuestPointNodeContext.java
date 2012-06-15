package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * Context representing a QuestPointNode (also QuestPoint)
 * 
 * @author mfchurch
 * 
 */
public class QuestPointNodeContext extends GraphNodeContext {

	public QuestPointNodeContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public QuestPointNodeContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public QuestPointNodeContext(Context other, QuestPointNode source) {
		this(other);
		node = source;
	}

	@Override
	public String getName() {
		QuestPoint questPoint = ((QuestPointNode) node).getQuestPoint();
		return this.getNameOf(questPoint);
	}

	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		QuestPoint questPoint = ((QuestPointNode) node).getQuestPoint();
		return this.getNamifier().getUniqueName(questPoint, legalFormat);
	}

	@Override
	public String getCommitting() {
		QuestPoint questPoint = ((QuestPointNode) node).getQuestPoint();
		Boolean committing = questPoint.getCommitting();
		return committing ? "1" : "0";
	}

	@Override
	public String getFormattedValue() {
		final Collection<FormatFragment> typeFormat;

		typeFormat = this.translator.getGameTypeManager().getFormat(
				QuestPoint.QUEST_POINT_TYPE);
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return FormatFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public String getValue() {
		return this.getName();
	}

	@Override
	public String getFanIn() {
		QuestPoint questPoint = ((QuestPointNode) node).getQuestPoint();
		return questPoint.getFanIn().toString();
	}

	@Override
	public String getQuestContainer() {
		QuestPoint questPoint = ((QuestPointNode) node).getQuestPoint();
		return questPoint.getQuestContainer().getName();
	}
}
