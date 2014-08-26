package scriptease.translator.codegenerator.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.translator.LanguageDictionary;
import scriptease.util.StringOp;

/**
 * Handles all the appropriate name uniquification/legalization for code gen.
 * Scope is handled here through use of the decorator pattern. Each
 * CodeGenerationNamifier decorates the namifier of it's parent scope, thereby
 * facilitating scope passing while simultaneously providing access to higher
 * scopes.
 * 
 * @author jason
 * @author mfchurch
 * @author kschenk
 */
public class CodeGenerationNamifier {
	private static final String defaultPattern = "^[a-zA-Z]+[0-9a-zA-Z_]*";
	private static final int UNIQUIFY_LIMIT = 10000;

	private final LanguageDictionary languageDictionary;
	private final Map<StoryComponent, String> componentsToNames;

	public CodeGenerationNamifier(LanguageDictionary languageDictionary) {
		this.languageDictionary = languageDictionary;

		this.componentsToNames = new HashMap<StoryComponent, String>();
	}

	/**
	 * Generates a code-safe, unique string based off of the given
	 * StoryComponent's name.
	 * 
	 * @param component
	 *            The component that will be given a name in code.
	 * @param legalFormat
	 *            The range of legal alphanumeric characters
	 * @return a code-safe, unique string for the given StoryComponent
	 */
	public String getUniqueName(StoryComponent component, Pattern legalFormat) {
		if (legalFormat == null || legalFormat.pattern().isEmpty())
			legalFormat = Pattern.compile(defaultPattern);

		String currentName = this.getGeneratedNameFor(component);

		if (currentName == null || currentName.isEmpty()) {
			currentName = this.buildLegalName(component, legalFormat);

			this.componentsToNames.put(component, currentName);
		}

		return currentName;
	}

	/**
	 * Checks if the given name is unique in the current scope.
	 * 
	 * @param name
	 *            the name to check if unique
	 * @return true if name is unique in scope
	 */
	private boolean isNameUnique(String name, StoryComponent component) {
		if (this.languageDictionary.isReservedWord(name))
			return false;

		for (Entry<StoryComponent, String> entry : this.componentsToNames
				.entrySet()) {
			if (entry.getValue().equals(name) && entry.getKey() != (component)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Builds a unique 'legal' name based on the storycomponent and the
	 * legalFormat given.
	 * 
	 * @param component
	 * @param legalFormat
	 * @return
	 */
	private String buildLegalName(StoryComponent component, Pattern legalFormat) {
		final String displayText = component.getDisplayText();
		String name;

		if (StringOp.exists(displayText)) {
			name = displayText;
		} else
			name = "noname";

		name = StringOp.removeIllegalCharacters(name, legalFormat, true);

		name = StringOp.removeNonCharPrefix(name);

		int counter = 0;
		while (!this.isNameUnique(name, component) && counter < UNIQUIFY_LIMIT) {
			// tack on a counter to the end of the name,
			name = displayText + "_" + Integer.toString(counter++, 36);

			name = StringOp.removeIllegalCharacters(name, legalFormat, true);
		}

		if (counter >= UNIQUIFY_LIMIT)
			// We can't uniquify with letters either. Give up. And cry.
			throw new IndexOutOfBoundsException("The name " + name
					+ " already exists, and could not be uniquified"
					+ " given the naming rules in the current translator.");

		return name;
	}

	/**
	 * Gets the generated name of the given storycomponent from the current
	 * scope.
	 * 
	 * @param component
	 * @return
	 */
	private String getGeneratedNameFor(StoryComponent component) {
		/*
		 * Hack for Implicits:
		 * 
		 * If we add implicits to two identical causes, the second one will have
		 * it's name changed even though it should reference the same code. All
		 * implicits should have the same variable name since we're not defining
		 * them every time.
		 * 
		 * This obviously represents a larger issue with how code is generated,
		 * and should be looked into.
		 */
		if (component instanceof KnowIt) {
			final KnowItBinding binding = ((KnowIt) component).getBinding();
			final String name = component.getDisplayText();

			if ((binding instanceof KnowItBindingFunction && ((ScriptIt) binding
					.getValue()).getCause().getImplicits().contains(component))
					||

					((name.equalsIgnoreCase(Behaviour.INITIATOR) || name
							.equalsIgnoreCase(Behaviour.RESPONDER)) && component
							.getOwner().getOwner() instanceof Behaviour)) {

				for (Entry<StoryComponent, String> entry : this.componentsToNames
						.entrySet()) {
					if (entry.getKey().equals(component)) {
						return entry.getValue();
					}
				}
			}
		}

		final String name = this.componentsToNames.get(component);

		if (StringOp.exists(name) && name.equalsIgnoreCase("Initiator_0"))
			System.out.println("blarg");
		return name;
	}

	@Override
	public String toString() {
		return "CodeGenerationNamifier [" + this.componentsToNames.values()
				+ "]";
	}
}