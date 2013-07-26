package scriptease.translator.io.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scriptease.controller.StoryAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.dialogue.DialogueLine;
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
 * @author kschenk
 */
public abstract class GameModule {
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
	public abstract void load(boolean readOnly) throws IOException;

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
	public abstract void save(boolean compile) throws IOException;

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
	public abstract void close() throws IOException;

	/**
	 * Gets a list of all {@link Resource}s that have the specified type.
	 * 
	 * @param type
	 *            The type to filter by.
	 * @return A list of all {@link Resource}s that have the supplied type.
	 */
	public abstract List<Resource> getResourcesOfType(String type);

	/**
	 * Retrieves the Resource object that represents the game data that is
	 * uniquely identified by its identifier string.
	 * 
	 * @return the Resource whose identifier matches the given identifier.
	 */
	public abstract Resource getInstanceForObjectIdentifier(String id);

	/**
	 * Retrieves the resources that will have automatics attached to them. Make
	 * sure that these match the types for the parameters of the automatic
	 * causes, or else exceptions will be thrown.
	 * 
	 * @return
	 */
	public abstract Collection<Resource> getAutomaticHandlers();

	/**
	 * Gets the location of the GameModule.
	 * 
	 * @return The file path that points to this GameModule. Must never be
	 *         <code>null</code>. If there is no location, this method returns a
	 *         <code>File("")</code>.
	 */
	public abstract File getLocation();

	/**
	 * Gets a name for this GameModule. This could be the specific module's
	 * name, or simply the file name. Unique names are preferred, but not
	 * required.
	 * 
	 * @return The name for this GameModule.
	 */
	public abstract String getName();

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
	public abstract void setLocation(File location);

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
	public abstract void addScripts(Collection<ScriptInfo> scripts);

	/**
	 * Adds the collection of include files to the resources list. It does this
	 * in a very similar fashion to addScripts.
	 * 
	 * @see #addScripts(Collection)
	 */
	public abstract void addIncludeFiles(Collection<File> includeFiles);

	/**
	 * Handles the dialogue roots present in the story model. This happens right
	 * before {@link #addScripts(Collection)} is called.
	 * 
	 * @param dialogueRoots
	 */
	public abstract void handleDialogues(Collection<DialogueLine> dialogueRoots);

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
	 *            executable. Implementations do not spawn the process
	 *            themselves, but instead configure the ProcessBuilder as
	 *            needed.
	 * @throws FileNotfoundException
	 *             if one or more of the required file for game execution could
	 *             not be located.
	 * @throws UnsupportedOperationException
	 *             if the translator does not support testing. Testing support
	 *             should be declared in translator.ini under SUPPORTS_TESTING.
	 */
	public abstract void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException;

	/**
	 * Breaks a story tree into groups of components whose output is related.
	 * That is, if there are multiple pieces of code that need to be written to
	 * the same file, this is where the code generating components are matched
	 * up with each other. <br>
	 * <br>
	 * The default behaviour aggregates scripts by slot and subject, which will
	 * usually work. This method likely won't need to be overwritten apart from
	 * strange circumstances.
	 * 
	 * @param root
	 *            The root of the tree to aggregate from.
	 * @return The collection of StartItOwner objects that should all be
	 *         generating into the same file. This must <b>never</b> be
	 *         <code>null</code>, but may be empty.
	 */
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> roots) {
		final Map<String, List<CodeBlock>> subjectToCodeBlocks;
		final StoryAdapter codeBlockMapper;
		final List<Set<CodeBlock>> scriptBuckets;

		subjectToCodeBlocks = new HashMap<String, List<CodeBlock>>();
		scriptBuckets = new ArrayList<Set<CodeBlock>>();

		// Split the story tree into groups by CodeBlock info.
		codeBlockMapper = new StoryAdapter() {

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				complex.processChildren(this);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
					final String slot;
					final KnowIt subject;
					final String key;
					final List<CodeBlock> bucket;
					final List<CodeBlock> existingBucket;

					slot = codeBlock.getSlot();
					subject = codeBlock.getSubject();
					key = subject.getBinding().toString() + slot;

					// GameModule.this.getAutomaticHandlers();

					bucket = new ArrayList<CodeBlock>();

					existingBucket = subjectToCodeBlocks.get(key);

					if (existingBucket != null)
						bucket.addAll(existingBucket);

					bucket.add(codeBlock);

					subjectToCodeBlocks.put(key, bucket);
				}
				this.defaultProcessComplex(scriptIt);
			}
		};

		for (StoryComponent root : roots) {
			root.process(codeBlockMapper);
		}

		// Sort CodeBlocks into groups.
		for (String key : subjectToCodeBlocks.keySet()) {
			final Set<CodeBlock> codeBlockGroup = new HashSet<CodeBlock>();
			for (CodeBlock codeBlock : subjectToCodeBlocks.get(key)) {
				codeBlockGroup.add(codeBlock);
			}
			scriptBuckets.add(codeBlockGroup);
		}

		return scriptBuckets;
	}

	/**
	 * This returns the keyword for the image type. It is used in
	 * {@link DialogueLine}s right now but may have other functionality in the
	 * future. If your game does not have an image type, you can return null or
	 * an empty string and ScriptEase II will handle it for you.
	 * 
	 * @return
	 */
	public abstract String getImageType();

	/**
	 * This returns the keyword for the audio type. It is used in
	 * {@link DialogueLine}s right now, but may have other functionality in the
	 * future. If your game does not have an audio type, you can return null or
	 * an empty string and ScriptEase II will handle it for you.
	 * 
	 * @return
	 */
	public abstract String getAudioType();

	/**
	 * This returns the keyword for the dialogue line type. It is used in
	 * {@link DialogueLine}s right now, but may have other functionality in the
	 * future. If your game does not have an audio type, you can return null or
	 * an empty string and ScriptEase II will handle it for you.
	 * 
	 * @return
	 */
	public abstract String getDialogueLineType();

	/**
	 * This returns the keyword for the dialogue type, a container of
	 * {@link DialogueLine}s. It is used with {@link DialogueLine}s right now,
	 * but may have other functionality in the future. If your game does not
	 * have an audio type, you can return null or an empty string and ScriptEase
	 * II will handle it for you.
	 * 
	 * @return
	 */
	public abstract String getDialogueType();

	/**
	 * This returns the keyword for the question type. It is used in
	 * {@link AskIt}s right now, but may have other functionality in the future.
	 * If your game does not have an audio type, you can return null or an empty
	 * string and ScriptEase II will handle it for you.
	 * 
	 * @return
	 */
	public abstract String getQuestionType();
}
