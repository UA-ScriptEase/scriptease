package scriptease.translator.codegenerator;

public class TranslatorKeywordManager {

	/*
	 * This is were all the keywords are mapped. All other classes should
	 * reference these constants, rather than referencing the values directly.
	 * That way, the values can be changed without tracking down all the
	 * referencing code.
	 */
	public static final String FUNCTION_HEADER = "functionHeader";
	public static final String IMPORT_CALL = "includes";
	public static final String CONDITONAL_FORMAT = "conditional";
	public static final String FILE_FORMAT = "file";
	public static final String SINGLE_COMMENT_FORMAT = "singleComment";
	public static final String BLOCK_COMMENT_FORMAT = "blockComment";
	public static final String PARAMETER_FORMAT = "parameter";
	public static final String XML_BINDING = "binding";
	public static final String XML_CODE_FORMAT = "code";
	public static final String XML_COMMENT_FORMAT = "comment";
	public static final String XML_CONDITION_FORMAT = "condition";
	public static final String XML_INCLUDE_FORMAT = "include";
	public static final String XML_NAME_FORMAT = "name";
	public static final String XML_TYPE_FORMAT = "type";

	public static final String XML_FORMATS = "formats";
	public static final String XML_FORMAT = "format";
	public static final String XML_ATTRIBUTE_ID = "id";
	public static final String XML_FRAGMENT = "fragment";
	public static final String XML_ATTRIBUTE_DATA = "data";
	public static final String XML_ATTRIBUTE_REF = "ref";
	public static final String XML_ATTRIBUTE_DEFAULT = "default";
	public static final String XML_ATTRIBUTE_LEGAL_FORMAT = "legalValues";
	public static final String XML_SERIES = "series";
	public static final String XML_SEPARATOR = "separator";
	public static final String XML_LITERAL = "literal";
	public static final String XML_LINE = "line";
	public static final String XML_INDENT = "indent";
	public static final String XML_FORMAT_REF = "formatRef";
	public static final String XML_RESERVED_WORDS = "reservedWords";
	public static final String XML_WORD = "word";
	public static final String XML_INDENT_STRING = "indentString";
	public static final String XML_ATTRIBUTE_CASE_SENSITIVE = "caseSensitive";
	public static final String XML_ATTRIBUTE_IS_UNIQUE = "unique";

	public static final String XML_CAUSE_SERIES = "causes";
	public static final String KNOW_IT_SERIES = "knowIts";
	public static final String GAME_OBJECT_SERIES = "gameObjects";
	public static final String ASK_IT_SERIES = "askIts";
	public static final String XML_PARAMETER_SERIES = "parameters";
	public static final String XML_ARGUMENT_SERIES = "arguments";
	public static final String XML_ENCOUNTER_SERIES = "encounters";
	public static final String XML_FUNCTION_SERIES = "functions";
	public static final String XML_IMPLICIT_SERIES = "implicits";
	public static final String XML_INCLUDES_SERIES = "includes";
	public static final String XML_VARIABLE_SERIES = "variables";
	public static final String XML_SCRIPTIT_SERIES = "scriptIts";
	public static final String XML_SCRIPTIT_EFFECT_SERIES = "scriptItEffects";
	public static final String XML_OWNER_SCOPE = "owner";
	public static final String XML_MAP_REF = "mapRef";
	public static final String XML_SUBJECT = "subject";
	public static final String XML_SUBJECT_TYPE = "subjectType";
	public static final String XML_ARGUMENT_TYPE = "argumentType";
	public static final String XML_PARAMETER_TYPE = "parameterType";
	public static final String XML_RETURN_TYPE = "returnType";
	public static final String XML_IMPLICIT_TYPE = "implicitType";
	public static final String XML_ARGUMENT_REF = "argumentRef";
	public static final String XML_SCOPE = "scope";
	public static final String XML_ARGUMENT = "argument";
	public static final String XML_TYPE_REF = "typeRef";
	public static final String XML_DISPLAY_NAME = "displayName";
	public static final String XML_DESCRIPTION = "description";
	public static final String XML_SCRIPTIT_REF = "scriptItRef";
	public static final String XML_SCRIPTIT = "scriptIt";
	public static final String XML_ASKIT = "askIt";
	public static final String XML_KNOWIT = "knowIt";
	public static final String XML_DESCRIBEIT = "describeIt";
	public static final String XML_CAUSE = "cause";
	public static final String XML_IFCHILD_SCOPE = "ifChild";
	public static final String XML_ELSECHILD_SCOPE = "elseChild";
	public static final String XML_VALUE_FORMAT = "value";
	public static final String XML_FORMATTED_VALUE = "formattedValue";
	public static final String XML_EFFECTS_SERIES = "effects";

	public static final String XML_MAP_ENTRY = "entry";
	public static final String XML_MAP_KEY = "key";

	public static final String XML_KEYWORD = "keyword";
	public static final String XML_TYPE_REG = "reg";
	public static final String XML_SLOT_REF = "slotRef";
	public static final String XML_TYPE_GUI = "gui";

	public static final String XML_CODESYMBOL = "codeSymbol";
	public static final String XML_IMPLICIT = "implicit";
	public static final String XML_VISIBLE = "visible";

	public static final String XML_FILTERBY = "filterBy";
	public static final String XML_FILTER = "filter";
	public static final String MAIN_CODEBLOCK = "mainCodeBlock";
	public static final String XML_CODEBLOCK_SERIES = "codeBlocks";
	public static final String XML_CHILDREN_SERIES = "children";
	public static final String XML_QUESTNODES_SERIES = "questNodes";
	public static final String XML_QUESTPOINTNODES_SERIES = "questPointNodes";
	public static final String XML_START = "start";
	public static final String XML_END = "end";
	public static final String XML_COMMITTING = "committing";
	public static final String XML_FANIN = "fanIn";
	public static final String XML_PARENT_NODES_SERIES = "parentNodes";
	public static final String XML_CHILDREN_NODES_SERIES = "childrenNodes";
	public static final String XML_QUEST_CONTAINER = "questContainer";
}
