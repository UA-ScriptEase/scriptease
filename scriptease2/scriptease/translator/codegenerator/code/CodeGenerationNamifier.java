package scriptease.translator.codegenerator.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.ScriptIt;
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
	private static final int ARBITRARY_STOP_SIZE = 10000;

	private final CodeGenerationNamifier parentNamifier;
	private final LanguageDictionary languageDictionary;
	private final Map<StoryComponent, String> componentsToNames;
	private final Map<CodeBlock, String> codeBlocksToNames;

	public CodeGenerationNamifier(LanguageDictionary languageDictionary) {
		this(null, languageDictionary);
	}

	public CodeGenerationNamifier(CodeGenerationNamifier existingNames,
			LanguageDictionary languageDictionary) {
		this.parentNamifier = existingNames;
		this.languageDictionary = languageDictionary;

		this.componentsToNames = new HashMap<StoryComponent, String>();
		this.codeBlocksToNames = new HashMap<CodeBlock, String>();
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
		String currentName = "";

		if (legalFormat == null || legalFormat.pattern().isEmpty())
			legalFormat = Pattern.compile(defaultPattern);

		currentName = this.getGeneratedNameFor(component);

		if (currentName == null || currentName.isEmpty()) {
			if (component instanceof CodeBlock) {
				currentName = this.buildLegalName(component.getOwner(),
						legalFormat);
			} else {
				currentName = this.buildLegalName(component, legalFormat);
			}

			// Propagate the name to the parents.
			CodeGenerationNamifier ownerNamifier = this;
			StoryComponent owner = component;

			while (owner != null && ownerNamifier != null) {
				// Propogate one level regardless of owner type.
				if (component instanceof CodeBlock) {
					ownerNamifier.codeBlocksToNames.put((CodeBlock) component,
							currentName);
					ownerNamifier.componentsToNames.put(component, currentName);
				} else {
					ownerNamifier.componentsToNames.put(component, currentName);
					owner = owner.getOwner();
				}

				ownerNamifier = ownerNamifier.parentNamifier;
			}
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
		boolean isUnique = !this.languageDictionary.isReservedWord(name);

		if (isUnique)
			for (Entry<StoryComponent, String> entry : this.componentsToNames
					.entrySet()) {
				if (entry.getValue().equals(name)
						&& !entry.getKey().equals(component)) {
					isUnique = false;
					break;
				}
			}

		if (isUnique)
			for (Entry<CodeBlock, String> entry : this.codeBlocksToNames
					.entrySet()) {
				if (entry.getValue().equals(name)
						&& !entry.getKey().equals(component)) {
					isUnique = false;
					break;
				}
			}

		if (isUnique && this.parentNamifier != null)
			isUnique = this.parentNamifier.isNameUnique(name, component);

		return isUnique;
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
		int counter = 0;
		String name;

		name = StringOp.removeIllegalCharacters(displayText, legalFormat, true);

		name = StringOp.removeNonCharPrefix(name);

		while (!this.isNameUnique(name, component)
				&& counter < ARBITRARY_STOP_SIZE) {
			// tack on a counter to the end of the name,
			name = displayText + "_" + Integer.toString(counter++, 36);

			name = StringOp.removeIllegalCharacters(name, legalFormat, true);
		}

		if (counter >= ARBITRARY_STOP_SIZE) {
			// We can't uniquify with letters either. Give up. And cry.
			throw new IndexOutOfBoundsException("The name " + name
					+ " already exists, and could not be uniquified"
					+ " given the naming rules in the current translator. "
					+ "Please choose a different name.");
		}

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
		String name = "";
		if (component instanceof CodeBlock) {
			name = this.codeBlocksToNames.get((CodeBlock) component);
		} else {
			name = this.componentsToNames.get(component);

			/*
			 * This lovely hack is in here for one reason:
			 * 
			 * If we add implicits to two identical causes, the second one will
			 * have it's name changed even though it should reference the same
			 * code. All implicits should have the same variable name since
			 * we're not defining them every time.
			 * 
			 * This obviously represents a larger issue with how code is
			 * generated, and should be looked into.
			 */
			if (component instanceof KnowIt
					&& !this.componentsToNames.isEmpty()) {
				final KnowItBinding binding = ((KnowIt) component).getBinding();

				if (binding instanceof KnowItBindingFunction
						&& ((ScriptIt) binding.getValue()).getCause()
								.getImplicits().contains(component)) {

					for (Entry<StoryComponent, String> entry : this.componentsToNames
							.entrySet()) {
						if (entry.getKey().equals(component)) {
							name = entry.getValue();
							break;
						}
					}
				}
			}
		}
		if (name != null)
			return name;

		return this.parentNamifier != null ? this.parentNamifier
				.getGeneratedNameFor(component) : null;
	}

	@Override
	public String toString() {
		return "CodeGenerationNamifier [" + this.codeBlocksToNames.values()
				+ ", " + this.componentsToNames.values() + "]";
	}
}