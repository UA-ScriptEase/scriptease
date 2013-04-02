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
	public static enum FunctionConstants {
		FUNCTION_AS_VALUE
	}

	// Used in ScopeFragment.
	public static enum ScopeTypes {
		ACTIVECHILD,
		ARGUMENT,
		ASKIT,
		BINDING,
		CAUSE,
		ELSECHILD,
		END,
		IFCHILD,
		INACTIVECHILD,
		MAINCODEBLOCK,
		OWNER,
		SLOTPARAMETER,
		SCRIPTIT,
		START,
		SUBJECT
	}

	// Used in SimpleDataFragment
	public static enum DataTypes {
		CODE,
		CONDITION,
		CONTROLITFORMAT,
		FANIN,
		FORMATTEDVALUE,
		INCLUDE,
		NAME,
		NOTE,
		PARENTNAME,
		SLOTCONDITIONAL,
		SUBJECT,
		TEMPLATEID,
		TYPE,
		UNIQUEID,
		UNIQUE32CHARNAME,
		DISPLAYTEXT,
		VALUE,
		CURRENTSTORYPOINT
	}

	// Used in SeriesFragment
	public static enum SeriesFilterType {
		NAME,
		SLOT,
		NONE,
	}

	public static enum FormatReferenceType {
		NONE,
		ASKIT,
		KNOWIT,
		NOTE,
		SCRIPTIT,
		CONTROLIT
	}

	// Used in SeriesFragment
	public static enum SeriesTypes {
		ARGUMENTS,
		CAUSES,
		CHILDREN,
		CHILDRENNODES,
		CODEBLOCKS,
		IDENTICALCAUSES,
		IMPLICITS,
		INCLUDES,
		ORDEREDSTORYPOINTS,
		PARAMETERS,
		PARENTNODES,
		STORYPOINTS,
		SLOTPARAMETERS,
		VARIABLES, 
		PARAMETERSWITHSLOT
	}
}
