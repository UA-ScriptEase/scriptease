package scriptease.translator.codegenerator.code.contexts;

import java.util.Collection;
import java.util.regex.Pattern;

import scriptease.gui.quests.StoryPoint;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Context representing a StoryPoint
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public class StoryPointContext extends ComplexStoryComponentContext {

	public StoryPointContext(StoryPoint model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator, locationInfo);
	}

	public StoryPointContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public StoryPointContext(Context other, StoryPoint source) {
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
				StoryPoint.QUEST_POINT_TYPE);
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
		return ((StoryPoint) this.component).getFanIn().toString();
	}
}
