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
public interface CodeGenerationConstants {

	// Used in: KnowItBindingFunctionContext
	public static enum FunctionConstant {
		FUNCTION_AS_VALUE
	}

	// Used in ScopeFragment.
	public static enum ScopeType {
		ARGUMENT,
		ASKIT,
		AUDIO,
		BINDING,
		CAUSE,
		ELSECHILD,
		END,
		FIRSTCAUSE,
		IMAGE,
		IFCHILD,
		MAINCODEBLOCK,
		OWNER,
		SLOTPARAMETER,
		SCRIPTIT,
		START,
		SUBJECT, 
		RESOURCE,
		TEMPLATEID
	}

	// Used in SimpleDataFragment
	public static enum DataType {
		CODE,
		CONDITION,
		CONTROLITFORMAT,
		ENABLED,
		FANIN,
		FORMATTEDVALUE,
		ID,
		INCLUDE,
		NAME,
		NOTE,
		PARENTNAME,
		SLOTCONDITIONAL,
		SPEAKER,
		SUBJECT,
		TEMPLATEID,
		TEXT,
		TYPE,
		UNIQUEID,
		UNIQUE32CHARNAME,
		DISPLAYTEXT,
		VALUE,
		CURRENTSTORYPOINT, 
		TOTALCHOICEPROBABILITY, 
		INDEX, 
		CHOICEPROBABILITYLOWERBOUND, 
		CHOICEPROBABILITYUPPERBOUND, 
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
		CONTROLIT,
		PICKIT,
		FUNCTIONIT
	}

	// Used in SeriesFragment
	public static enum SeriesType {
		ARGUMENTS,
		CAUSES,
		CHILDLINES,
		CHILDREN,
		CHILDRENNODES,
		CODEBLOCKS,
		DIALOGUEROOTS,
		IDENTICALCAUSES,
		IMPLICITS,
		INCLUDES,
		PARAMETERS,
		PARENTNODES,
		SLOTPARAMETERS,
		VARIABLES, 
		PARAMETERSWITHSLOT,
		ORDEREDDIALOGUELINES,
		ORDEREDSTORYPOINTS, 
		STORYNODES, 
		STORYPOINTS, 
		ORDEREDSTORYNODES, 
		CHOICES
	}
}
