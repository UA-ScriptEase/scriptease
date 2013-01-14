package scriptease.translator.codegenerator.code.fragments;

import java.util.regex.Pattern;

import scriptease.translator.codegenerator.CharacterRange;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.CodeGenerationKeywordConstants;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * Represents a location where a single special code token must be generated by
 * the code generator.
 * 
 * An example of these code tokens are a DoIt's name, type, etc that appear in
 * the language dictionary, although anything defined as a fragment in the
 * language dictionary is a simple fragment.
 * 
 * @author remiller
 */
public class SimpleDataFragment extends AbstractFragment {
	private String defaultText = "";
	private String legalRange = "";

	public SimpleDataFragment() {
		super("");
	}

	/**
	 * See:
	 * {@link AbstractFragment#FormatFragment(String, CharacterRange, char[])}
	 * 
	 * @param label
	 *            The specific simple directive.
	 * @param legalRange
	 *            The allowed range of alphanumeric characters.
	 */
	public SimpleDataFragment(String label, String legalRange) {
		super(label);
		this.legalRange = legalRange;
	}

	/**
	 * Creates a clone of the given directive fragment.
	 * 
	 * @param other
	 *            the fragment to copy.
	 */
	public SimpleDataFragment(SimpleDataFragment other) {
		this(other.getDirectiveText(), other.getLegalRange());
	}

	/**
	 * Creates a new instance that uses the given directive, and the range and
	 * specials from the other instance.
	 * 
	 * @param label
	 *            The specific simple directive.
	 * @param other
	 *            the fragment to copy.
	 */
	public SimpleDataFragment(String label, SimpleDataFragment other) {
		this(label, other.getLegalRange());
	}

	@Override
	public String resolve(Context context) {
		super.resolve(context);
		final String dataLabel = this.getDirectiveText();
		String resolveString = "";
		try {
			// IF+ELSE BLOCK (fragment data = <dataLabel>)
			if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.NAME
							.name()))
				resolveString = context.getUniqueName(Pattern
						.compile(this.legalRange));
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.TYPE
							.name())) {
				try {
					resolveString = context.getType();
				} catch (CodeGenerationException e) {
					if (!this.defaultText.isEmpty())
						return this.defaultText;
					else
						throw e;
				}
			} else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.CODE
							.name()))
				resolveString = context.getCode();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.TEMPLATEID
							.name()))
				resolveString = context.getTemplateID();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.VALUE
							.name()))
				resolveString = context.getValue();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.CONDITION
							.name()))
				resolveString = context.getCondition();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.CONTROLITFORMAT
							.name())) {
				resolveString = context.getControlItFormat();
			} else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.CURRENTSTORYPOINT
							.name()))
				resolveString = context.getUnique32CharName();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.FORMATTEDVALUE
							.name()))
				resolveString = context.getFormattedValue();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.INCLUDE
							.name()))
				resolveString = context.getInclude();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.SUBJECT
							.name()))
				resolveString = context.getSubject().getBinding()
						.getScriptValue();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.FANIN
							.name()))
				resolveString = context.getFanIn();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.NOTE
							.name()))
				resolveString = context.getDisplayText();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.UNIQUEID
							.name()))
				resolveString = context.getUniqueID();
			else if (dataLabel
					.equalsIgnoreCase(CodeGenerationKeywordConstants.DataTypes.UNIQUE32CHARNAME
							.name()))
				resolveString = context.getUnique32CharName();
			else
				throw (new CodeGenerationException(
						"Simple Data Fragment was unable to be resolved for data: "
								+ dataLabel + ">"));

			return resolveString;
		} catch (CodeGenerationException e) {
			return "Error when inserting new simple fragment: " + dataLabel
					+ " with message: " + e.getMessage();
		}
	}

	public final String getLegalRange() {
		return this.legalRange;
	}

	/**
	 * Set the legal range of the simple fragment. This needs to be in regex
	 * format.
	 * 
	 * @param legalRange
	 */
	public void setLegalRange(String legalRange) {
		this.legalRange = legalRange;
	}

	/**
	 * Sets the default text.
	 * 
	 * @param defaultText
	 *            The new text to use as a default.
	 */
	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}

	public String getDefaultText() {
		return this.defaultText;
	}

	@Override
	public String toString() {
		return "SimpleFragment [" + this.getDirectiveText() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleDataFragment) {
			return this.hashCode() == obj.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + this.defaultText.hashCode()
				+ this.legalRange.hashCode();
	}
}
