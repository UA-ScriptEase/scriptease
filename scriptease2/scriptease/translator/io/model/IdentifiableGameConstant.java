package scriptease.translator.io.model;

/**
 * Interface for an Identifiable Game Constant - meaning a Game Constant which
 * can be identified by a game internally unique ID.
 * 
 * @author mfchurch
 * 
 */
public interface IdentifiableGameConstant extends GameConstant {

	/**
	 * Gets the string that is the game's internally unique ID for this objects
	 * template. For example, in Neverwinter Nights, it is the ResRef.<br>
	 * <br>
	 * The uniqueness of this property is not enforced by ScriptEase, but
	 * instead we rely of the game to enforce the constraint.
	 * 
	 * @return A String that represents the unique label for this template.
	 *         Uniqueness is not enforced by ScriptEase; we rely on the game
	 *         doing this for the most part.
	 */
	public String getTemplateID();

}
