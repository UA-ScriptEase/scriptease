package scriptease.translator.codegenerator.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
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
 */
public class CodeGenerationNamifier {
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
		// This is an identity hash map to use == instead of .equals to compare
		this.componentsToNames = new IdentityHashMap<StoryComponent, String>();
		this.codeBlocksToNames = new HashMap<CodeBlock, String>();
		this.languageDictionary = languageDictionary;
	}

	/**
	 * Checks if the given name is unique in the current scope.
	 * 
	 * @param name
	 *            the name to check if unique
	 * @return true if name is unique in scope
	 */
	protected boolean isNameUnique(String name) {
		Collection<String> componentNameList = new ArrayList<String>(this.componentsToNames.values());
		Collection<String> codeBlockNameList = new ArrayList<String>(this.codeBlocksToNames.values());
		
		boolean isUniqueInScope =  !(componentNameList.contains(name)) &&
				 !(codeBlockNameList.contains(name));
		
		boolean isUnique = isUniqueInScope && !this.languageDictionary.isReservedWord(name);
		if (isUnique && this.parentNamifier != null)
			isUnique = this.parentNamifier.isNameUnique(name);
		return isUnique;
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
			legalFormat = Pattern.compile("[a-zA-Z_0-9]+");

		currentName = getGeneratedNameFor(component);
		if (currentName == null || currentName.isEmpty()) {
			currentName = buildLegalName(component, legalFormat);
			propogateComponentName(component, currentName);
		}

		return currentName;
	}

	public String getUniqueName(CodeBlock codeBlock, Pattern legalFormat) {
		String currentName = "";

		if (legalFormat == null || legalFormat.pattern().isEmpty())
			legalFormat = Pattern.compile("[a-zA-Z_0-9]+");

		currentName = getGeneratedNameFor(codeBlock);
		if (currentName == null || currentName.isEmpty()) {
			currentName = buildLegalName(codeBlock.getOwner(), legalFormat);
			propogateCodeBlockName(codeBlock, currentName);
		}

		return currentName;
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
		String name;
		int counter;

		// there isn't already a name, we need to generate one.
		counter = 0;

		name = StringOp.removeIllegalCharacters(component.getDisplayText(),
				legalFormat);

		while (!this.isNameUnique(name) && counter < ARBITRARY_STOP_SIZE) {
			// tack on a counter to the end of the name,
			name = component.getDisplayText() + "_"
					+ Integer.toString(counter++, 36).toUpperCase();

			name = this.removeIllegalCharacters(name, legalFormat);
		}

		if (counter >= ARBITRARY_STOP_SIZE) {
			// We can't uniquify with letters either. Give up. And cry.
			String message = "The name "
					+ name
					+ " already exists, and could not be uniquified given the naming rules in the current translator. Please choose a different name.";

			WindowFactory.getInstance().showProblemDialog("Name exists",
					message);
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
	public String getGeneratedNameFor(StoryComponent component) {
		String name = "";
		name = this.componentsToNames.get(component);
		if (name != null)
			return name;

		return this.parentNamifier != null ? this.parentNamifier
				.getGeneratedNameFor(component) : null;
	}

	public String getGeneratedNameFor(CodeBlock codeBlock) {
		String name = "";
		name = this.codeBlocksToNames.get(codeBlock);
		if (name != null)
			return name;

		return this.parentNamifier != null ? this.parentNamifier
				.getGeneratedNameFor(codeBlock) : null;
	}

	/**
	 * Recursively removes characters which do not match the legalFormat regex.
	 * If 'source' does not match as is, the first offending character is
	 * removed. The system recurses, removing one character at a time, until the
	 * result matches, or is empty.
	 * 
	 * @param source
	 * @param legalFormat
	 * @return
	 */
	private String removeIllegalCharacters(String source, Pattern legalFormat) {
		Matcher legalMatcher = legalFormat.matcher(source);

		if (source.isEmpty()) {
			// The given name is completely useless.
			WindowFactory
					.getInstance()
					.showProblemDialog(
							"Bad Name Given",
							"The name "
									+ source
									+ " is illegal in the target game language, and has no legal substrings. Please change the name and try again.");
			return "seBATMAN";
		}
		if (legalMatcher.matches()) {
			return source;
		} else {
			String newSource = "";
			for (char curChar : source.toCharArray()) {
				legalMatcher = legalFormat.matcher(newSource + curChar);
				newSource += legalMatcher.matches() ? curChar : "";
			}

			return newSource;
		}
	}

	private void propogateCodeBlockName(CodeBlock codeBlock, String name) {
		CodeGenerationNamifier ownerNamifier = this;

		while (ownerNamifier != null) {
			ownerNamifier.codeBlocksToNames.put(codeBlock, name);
			ownerNamifier = ownerNamifier.parentNamifier;
		}
	}

	// used by process methods to propagate component up to the ScriptIt parent
	private void propogateComponentName(StoryComponent component, String name) {
		StoryComponent owner = component;
		CodeGenerationNamifier ownerNamifier = this;

		// Put it in the current componentsToNames, then iterate up to a
		// ComplexStoryComponent and break after
		while (owner != null && ownerNamifier != null) {
			// Propogate one level regardless of owner type.
			ownerNamifier.componentsToNames.put(component, name);

			// if (owner instanceof ScriptIt)
			// break;

			ownerNamifier = ownerNamifier.parentNamifier;
			owner = owner.getOwner();
		}
	}

	@Override
	public String toString() {
		return "CodeGenerationNamifier [" + this.codeBlocksToNames.values()
				+ ", " + this.componentsToNames.values() + "]";
	}
}