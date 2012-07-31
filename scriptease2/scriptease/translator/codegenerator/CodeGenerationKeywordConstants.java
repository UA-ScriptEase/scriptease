package scriptease.translator.codegenerator;

/**
 * A class that provides various enumerations for different keywords that need
 * to exist. As of now, these are used once per class, but there is a
 * possibility that they will need to be used elsewhere eventually.
 * 
 * To compare to the keyword, call
 * <code>TranslatorKeywordConstants.Subcategory.KEYWORD.name();</code>
 * 
 * Comparison methods should always either ignore case, or, if that's not
 * possible, convert the String compared to the Keyword to upper case.
 * 
 * @author kschenk
 */
public interface CodeGenerationKeywordConstants {

	// Used in: KnowItBindingFunctionContext
	public static enum Function {
		FUNCTION_AS_VALUE
	}

	// Used in ScopeFragment.
	public static enum Scope {
		ARGUMENT,
		ASKIT,
		BINDING,
		ELSECHILD,
		END,
		IFCHILD,
		MAINCODEBLOCK,
		OWNER,
		SCRIPTIT,
		START,
		SUBJECT
	}

	// Used in SimpleFragment
	public static enum DataTypes {
		CODE,
		CONDITION,
		FANIN,
		FORMATTEDVALUE,
		INCLUDE,
		NAME,
		QUESTCONTAINER,
		SUBJECT,
		TEMPLATEID,
		TYPE,
		VALUE
	}

	// Used in AbstractSeriesFragment
	public static enum Series {
		ARGUMENTS,
		CAUSES,
		CHILDREN,
		CHILDRENNODES,
		CODEBLOCKS,
		EFFECTS,
		IMPLICITS,
		INCLUDES,
		PARAMETERS,
		PARENTNODES,
		QUESTPOINTNODES,
		QUESTNODES,
		SCRIPTITEFFECTS,
		SCRIPTITS,
		VARIABLES
	}
}
