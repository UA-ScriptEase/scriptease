package scriptease.translator.codegenerator;

/**
 * Defines the legal character ranges that may be used in resolving this
 * Fragment.
 * 
 * @author remiller
 */
public enum CharacterRange {
	/**
	 * A through Z
	 */
	ALPHA,
	/**
	 * 0 through 9
	 */
	NUMERIC,
	/**
	 * The union of the ALPHA set and the NUMERIC set.
	 */
	ALPHANUMERIC
}
