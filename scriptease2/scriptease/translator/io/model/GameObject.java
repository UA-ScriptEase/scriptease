package scriptease.translator.io.model;




/**
 * A GameObject is the "instance" side of a class-instance type of
 * relationship in terms of game objects.<br>
 * <br>
 * 
 * For example, Neverwinter Nights has the concept of a 'Blueprint', and areas
 * contain 'Instances' of that Blueprint. So a possible implementation of the
 * NWN translator would have a <code>NWNObject.java</code> class that implements
 * <code>GameObject</code>.
 * 
 * @see GameDataTemplate
 * 
 * @author remiller
 */
public interface GameObject extends IdentifiableGameConstant {

	/**
	 * Sets the style of object resolution that will be used to get the object
	 * in code gen.
	 * 
	 * @param methodType
	 *            Integer constant that represents a method of resolving to an
	 *            object. This constant should come from the GameObject's
	 *            implementing class.
	 */
	public void setResolutionMethod(int methodType);

	/**
	 * Gets the style of object resolution that will be used to get the object
	 * in code gen.
	 * 
	 * @return Integer constant that represents a method of resolving to an
	 *         object. This constant should come from the GameObject's
	 *         implementing class.
	 */
	public int getResolutionMethod();
	
	/**
	 * Gets the game-assigned object ID for this instance. This exists for
	 * future expansion. Most likely there will be no point in storing an object
	 * ID.
	 * 
	 * @return The object's ID.
	 */
	public int getObjectID();


	/**
	 * Determines whether an object is equal to this GameDataTemplate. The
	 * equals implementation should check for unique identifiers; the return
	 * value from {@link #getIdentifier()} should be useful here.
	 */
	public boolean equals(Object obj);
}
