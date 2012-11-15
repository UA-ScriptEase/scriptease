package scriptease.translator.io.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.translator.codegenerator.ScriptInfo;

/**
 * This is the facade in a Facade pattern. All requests for game data should go
 * through this class.<br>
 * <br>
 * Part of the GameModule contract is that it must not handle any I/O-specific
 * exceptions (file not found, read errors), so that ScriptEase can deal with
 * them.
 * 
 * @author jtduncan
 * @author remiller
 */
public interface GameModule {
	/**
	 * Reads the game data into memory.<br>
	 * <br>
	 * For a GameModule implementation to honour the interface contract, it must
	 * perform its file access exclusively in the <code>load</code> and
	 * <code>{@link #save(boolean)}</code> methods.<br>
	 * <br>
	 * {@link #setLocation(File)} must be called before <code>load</code> is
	 * invoked.
	 * 
	 * @param readOnly
	 *            True if this module must be read as a read-only file, false
	 *            otherwise.
	 * @throws FileNotFoundException
	 *             if {@link #setLocation(File)} has not been called before, or
	 *             has been called with a file argument that does not exist.
	 * @throws IOException
	 *             if an error during reading occurs or if {@link #close()} has
	 *             been called before <code>load</code>.
	 * @see #close()
	 * @see #setLocation(File)
	 */
	public void load(boolean readOnly) throws IOException;

	/**
	 * Writes the GameModule to disk. If the module is a database, then this is
	 * where the change set would be pushed to the database.<br>
	 * <br>
	 * For a GameModule implementer to honour the interface contract, it must
	 * perform its file access exclusively in the {@link #load(boolean)} and
	 * <code>save</code> methods. <br>
	 * <br>
	 * If the GameModule's location file does not yet exist, then a new one is
	 * created on save, but if a file at <code>location</code> already exists,
	 * then it is simply overwritten without question.
	 * 
	 * @param compile
	 * 
	 * @see #getLocation()
	 * @see #setLocation(File)
	 */
	public void save(boolean compile) throws IOException;

	/**
	 * Closes this GameModule. If the module is a file, then this will free the
	 * file for other programs to use. If the module is a database, then it will
	 * close the connection to that database. <br>
	 * <br>
	 * If the location has not been previously set for this GameModule, then
	 * calling <code>close</code> has no effect.
	 * 
	 * @see #load(boolean)
	 * @see #getLocation()
	 * @see #setLocation(File)
	 */
	public void close() throws IOException;

	/**
	 * Gets a list of all <code>GameConstant</code>s that have the type
	 * <code>type</code>.
	 * 
	 * @param type
	 *            The type to filter by.
	 * @return A list of all <code>GameObject</code>s that have the supplied
	 *         type.
	 */
	public List<GameConstant> getResourcesOfType(String type);

	/**
	 * Retrieves the GameConstant object that represents the game data that is
	 * uniquely identified by its identifier string.
	 * 
	 * @return the GameConstant whose identifier matches the given identifier.
	 */
	public GameConstant getInstanceForObjectIdentifier(String id);

	/**
	 * Gets the location of the GameModule
	 * 
	 * @return The file path that points to this GameModule
	 */
	public File getLocation();

	/**
	 * Gets a name for this GameModule. This could be the specific module's
	 * name, or simply the file name. Unique names are preferred, but not
	 * required.
	 * 
	 * @return The name for this GameModule.
	 */
	public String getName();

	/**
	 * Sets the file system location of this GameModule to <code>location</code>
	 * . Since this location is used for reading and writing, it must be set
	 * before {@link #save(boolean)} or {@link #load(boolean)} are called, or
	 * else those methods will throw an exception.
	 * 
	 * @param location
	 *            The new location of this GameModule. Cannot be null.
	 * @throws IllegalArgumentException
	 *             if <code>location</code> is null.
	 */
	public void setLocation(File location);

	/**
	 * Writes the given script to this GameModule module, and attaches a script
	 * reference to the template supplied via ScriptInfo into the given slot,
	 * also stored in ScriptInfo. It is up to the specific implementation of
	 * GameModule to distinguish between different types of Templates.
	 * 
	 * @param scripts
	 *            A list of scripts to be written to file.
	 * @see ScriptInfo
	 */
	public void addScripts(Collection<ScriptInfo> scripts);

	/**
	 * Breaks a story tree into groups of components whose output is related.
	 * That is, if there are multiple pieces of code that need to be written to
	 * the same file, this is where the code generating components are matched
	 * up with each other.
	 * 
	 * @param root
	 *            The root of the tree to aggregate from.
	 * @return The collection of StartItOwner objects that should all be
	 *         generating into the same file.
	 * @see CodeBlock
	 * @see StoryComponent
	 */
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> root);

	/**
	 * Adds the collection of include files to the resources list. It does this
	 * in a very similar fashion to addScripts.
	 * 
	 * @see #addScripts(Collection)
	 */
	public void addIncludeFiles(Collection<File> scriptList);

	/**
	 * Creates a list of String arguments for running a process that executes
	 * this GameModule in the game. The list is as would be given to a
	 * ProcessBuilder. For example:
	 * 
	 * <code>[ "C:\\Program Files\\NWN\\nwmain.exe", "+TestNewModule", 
	 * "C:\\Program Files\\NWN\\modules\\Story"]</code>
	 * 
	 * Implementations may expect that the translator.ini knows where the
	 * executable resides.
	 * 
	 * @param builder
	 *            is the process builder that will be used to spawn the game
	 *            executable. Implementations will not spawn the process
	 *            themselves, but will instead configure the ProcessBuilder as
	 *            needed.
	 * @throws FileNotfoundException
	 *             if one or more of the required file for game execution could
	 *             not be located.
	 */
	public List<String> getTestCommand(ProcessBuilder builder)
			throws FileNotFoundException;
}
