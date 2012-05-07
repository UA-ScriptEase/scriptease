package scriptease.translator.codegenerator.code.contexts;

import java.util.regex.Pattern;

import scriptease.gui.quests.QuestNode;
import scriptease.model.StoryComponent;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * StoryComponentContext is Context for a StoryComponent.
 * 
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class StoryComponentContext extends Context {
	protected StoryComponent component;

	public StoryComponentContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
	}

	public StoryComponentContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public StoryComponentContext(Context other, StoryComponent source) {
		this(other);
		component = source;
	}

	/**
	 * Get the StoryComponent's generated name
	 * 
	 * @see getGeneratedName()
	 */
	@Override
	public String getName() {
		return this.getNameOf(component);
	}

	/**
	 * Get the StoryComponent's owner returns null if no owner
	 */
	@Override
	public StoryComponent getOwner() {
		return component.getOwner();
	}

	/**
	 * Get the StoryComponent's unique name
	 * 
	 * @see getGeneratedName()
	 */
	@Override
	public String getUniqueName(Pattern legalFormat) {
		return this.getNamifier().getUniqueName(component, legalFormat);
	}

	/**
	 * Get the StoryComponent's generated name
	 * 
	 * @see getUniqueName()
	 */
	@Override
	public String getNameOf(StoryComponent component) {
		return this.getNamifier().getUniqueName(component, null);
	}

	public void setComponent(StoryComponent component) {
		this.component = component;
	}
}
