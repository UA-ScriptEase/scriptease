package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.gui.quests.QuestPoint;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context representing a QuestPoint
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class QuestPointContext extends ComplexStoryComponentContext {

	public QuestPointContext(QuestPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public QuestPointContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public QuestPointContext(Context other, QuestPoint source) {
		this(other);
		this.component = source;
	}

	@Override
	public String getName() {
		return this.getNameOf(this.component);
	}

	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(this.component,
				legalFormat);
	}

	@Override
	public String getFormattedValue() {
		final Collection<AbstractFragment> typeFormat;

		typeFormat = this.translator.getGameTypeManager().getFormat(
				QuestPoint.QUEST_POINT_TYPE);
		if (typeFormat == null || typeFormat.isEmpty())
			return this.getValue();

		return AbstractFragment.resolveFormat(typeFormat, this);
	}

	@Override
	public String getValue() {
		return this.getName();
	}

	@Override
	public String getFanIn() {
		return ((QuestPoint) this.component).getFanIn().toString();
	}
}
