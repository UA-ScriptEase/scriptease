package io;

import io.GenericFileFormat.GffField;
import io.NWNConversation.DialogueLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import scriptease.controller.CodeBlockMapper;
import scriptease.controller.FileManager;
import scriptease.controller.modelverifier.rule.ParameterBoundRule;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.exception.GameCompilerException;
import scriptease.gui.SEFrame;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameObject;
import scriptease.translator.io.model.IdentifiableGameConstant;
import scriptease.translator.io.tools.ScriptEaseFileAccess;
import scriptease.util.FileOp;

/**
 * This class represents a NWN ERF file. An ERF file is the file into which
 * everything is packed (its an archive-style file like .zip or .tar). <br>
 * <br>
 * There are several flavours of ERF, most important to us is <code>.mod</code>,
 * but <code>.erf</code>, <code>.sav</code>, <code>.nwm</code>, and
 * <code>.hak</code> are also ERF files. The flavour of ERF is determined by the
 * <code>fileType</code> in the header data. <br>
 * <br>
 * See NWN documentation for more in-depth discussion.
 * 
 * @author jtduncan
 * @author remiller
 */
public final class ErfFile implements GameModule {

	private static final String VERSION = "V1.0";

	// Translator name to be used with the TranslatorManager lookup
	public static final String NEVERWINTER_NIGHTS = "Neverwinter Nights";

	/**
	 * Stores <code>NWNResource</code>s that are script ease generated.
	 */
	private final List<NWNResource> resources;

	/**
	 * Stores <code>NWNResource</code>s, each of which is an uncompiled script.
	 * Once a script is compiled, is should be removed from this list.
	 */
	private final List<NWNResource> uncompiledScripts;

	/**
	 * Location of the ErfFile.
	 */
	private File location;

	private ScriptEaseFileAccess fileAccess;

	/**
	 * Header data
	 */
	private String fileType;
	private long languageCount;
	private long descriptionStrRef;

	// string data isn't used by us, but we need to store it to write out again.
	private byte[] localizedStrings;

	// CONSTANTS
	private static final String SCRIPT_FILE_PREFIX = "se_";
	private static final String INCLUDE_FILE_PREFIX = "i_se_";
	private static final int HEADER_RESERVED_BYTES = 116;
	// header size = 11 entries * 4 bytes each + reserved space
	private static final long HEADER_BYTE_SIZE = 4 * 11 + HEADER_RESERVED_BYTES;

	public ErfFile() {
		this.resources = new ArrayList<NWNResource>();
		this.uncompiledScripts = new ArrayList<NWNResource>();
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		final String version;
		final long localizedStringSize; // size in bytes
		final long entryCount;
		final long offsetToLocalizedStrings;
		final long offsetToKeyList;
		final long offsetToResourceList;
		final List<ErfKey> keys;
		final List<ResourceListElement> elementIndexes;

		// Setup. "r" = read-only, "rw" = read-write
		this.fileAccess = new ScriptEaseFileAccess(this.location,
				readOnly ? "r" : "rw");
		this.resources.clear();

		// read header info
		this.fileAccess.seek(0);

		this.fileType = this.fileAccess.readString(4);
		version = this.fileAccess.readString(4);
		this.languageCount = this.fileAccess.readUnsignedInt(true);
		localizedStringSize = this.fileAccess.readUnsignedInt(true);
		entryCount = this.fileAccess.readInt(true);
		offsetToLocalizedStrings = this.fileAccess.readUnsignedInt(true);
		offsetToKeyList = this.fileAccess.readInt(true);
		offsetToResourceList = this.fileAccess.readInt(true);
		// ignore Build Year, Build Day. We'll recalculate those.
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.descriptionStrRef = this.fileAccess.readUnsignedInt(true);
		// Last 116 bytes are supposedly reserved. Skip them.
		this.fileAccess.skipBytes(ErfFile.HEADER_RESERVED_BYTES);
		// end read header

		if (!version.equals(VERSION)) {
			throw new IllegalStateException(this.getLocation()
					+ " does not have the appropriate ERF version. Expected "
					+ VERSION + " but read " + version);
		}

		// read localized strings
		this.fileAccess.seek(offsetToLocalizedStrings);
		this.localizedStrings = this.fileAccess
				.readBytes((int) localizedStringSize);

		// read all of the erf data
		keys = this.readErfKeys(entryCount, offsetToKeyList);
		elementIndexes = this.readResourceIndexList(entryCount,
				offsetToResourceList);

		// create NWNResources for each ErfKey/ResourceListElement pair
		for (int index = 0; index < entryCount; index++) {
			NWNResource resource = new NWNResource(keys.get(index),
					elementIndexes.get(index), this.fileAccess);

			this.resources.add(resource);
		}

		// get rid of old scriptease-generated stuff from last save.
		this.removeScriptEaseData();
	}

	private List<ErfKey> readErfKeys(long entryCount, long offsetToKeyList)
			throws IOException {
		final List<ErfKey> keyList = new ArrayList<ErfKey>((int) entryCount);

		this.fileAccess.seek(offsetToKeyList);

		// the Erf documentation states that there must be equal number of
		// keys and ResourceListElements equal to entryCount
		try {
			for (int i = 0; i < entryCount; i++) {
				ErfKey erfKey = new ErfKey(this.fileAccess);
				keyList.add(erfKey);
			}
		} catch (Throwable e) {
			System.out.println("Throwable in readErfKeys()");
			Thread.currentThread()
					.getUncaughtExceptionHandler()
					.uncaughtException(
							Thread.currentThread(),
							new IOException("Exception when creating ErfKey :"
									+ e));
		}

		return keyList;
	}

	private List<ResourceListElement> readResourceIndexList(long entryCount,
			long offsetToResourceList) throws IOException {
		final List<ResourceListElement> resourceIndexList;

		resourceIndexList = new ArrayList<ResourceListElement>((int) entryCount);

		// skip ahead to the location of the resource list
		this.fileAccess.seek(offsetToResourceList);

		// the Erf documentation states that there must be equal number of
		// keys and ResourceListElements equal to entryCount
		for (int i = 0; i < entryCount; i++) {
			resourceIndexList.add(new ResourceListElement(this.fileAccess));
		}

		return resourceIndexList;
	}

	/**
	 * Determines whether the given resRef is ScriptEase generated.
	 * 
	 * @return <code>true</code> if the given resref could be something that
	 *         scriptease generated.
	 */
	public static boolean isScriptEaseGenerated(String resRef) {
		return resRef.startsWith(ErfFile.SCRIPT_FILE_PREFIX)
				|| resRef.startsWith(ErfFile.INCLUDE_FILE_PREFIX);
	}

	/**
	 * Returns a List of GameObjects which match the given ScriptEase GameType.
	 */
	@Override
	public List<GameConstant> getResourcesOfType(String type) {
		List<GameConstant> filteredObjects = new ArrayList<GameConstant>();

		for (NWNResource resource : this.resources) {
			if (resource != null && resource.isGFF() && !resource.ignorable()) {
				GameConstant object = resource.getGFF()
						.getObjectRepresentation();
				if (object != null && object.getTypes().contains(type)) {
					filteredObjects.add(object);
				}
			}
		}
		return filteredObjects;
	}

	@Override
	public File getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(File location) {
		if (location == null)
			throw new IllegalArgumentException(
					"Cannot set a GameModule's location to null!");

		this.location = location;
	}

	@Override
	public String getName() {
		return this.location.getName();
	}

	@Override
	public void addIncludeFiles(Collection<File> includeList) {
		for (File include : includeList) {
			if (include.getName().startsWith("."))
				continue;

			String code;
			try {
				code = FileOp.readFileAsString(include);
			} catch (IOException e) {
				System.err.println("Error reading include file "
						+ include.getPath() + ". Skipping.");
				continue;
			}

			String scriptResRef = FileOp.removeExtension(include).getName();
			this.addScript(scriptResRef, code);
		}
	}

	/**
	 * Adds a NWNresource with the given resref and code to the resources list.
	 * 
	 * @param scriptResRef
	 *            the resref of the resource
	 * @param code
	 *            the code of the resource
	 * @return returns the NWNresource that was added to resources
	 */
	private NWNResource addScript(String scriptResRef, String code) {
		// TODO: Richard wants more meaningful resRef names for scripts
		NWNResource scriptResource;
		scriptResource = new NWNResource(scriptResRef,
				ErfKey.SCRIPT_SOURCE_TYPE, code.getBytes());

		this.resources.add(scriptResource);

		return scriptResource;
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		int scriptCounter = 0;

		this.uncompiledScripts.clear();

		for (ScriptInfo scriptInfo : scriptList) {
			if (scriptInfo != null) {
				String code = scriptInfo.getCode();

				String scriptResRef = ErfFile.SCRIPT_FILE_PREFIX
						+ Integer.toString(scriptCounter, 36);
				scriptCounter++;

				NWNResource scriptResource = this.addScript(scriptResRef, code);
				this.uncompiledScripts.add(scriptResource);

				// Manage the script slot references
				final IdentifiableGameConstant object = scriptInfo.getSubject();

				final String receiverResRef;
				final NWNResource receiverResource;

				// Hardcoded for conversations.
				if (object instanceof DialogueLine) {
					final DialogueLine dialogueLine = (DialogueLine) object;
					receiverResRef = dialogueLine.getConversationResRef();

					// Get the parent conversation file resource
					receiverResource = this.getResourceByResRef(receiverResRef);

					/*
					 * Get the appropriate field from the dialogue line, since
					 * we can/ have many dialogue lines in a conversation so
					 * searching by label is no longer acceptable.
					 */
					final GffField field = dialogueLine.getField(scriptInfo
							.getSlot());

					if (receiverResource == null) {
						throw new NoSuchElementException(
								"Script slot update failed. Cannot find resource for ResRef \""
										+ receiverResRef + "\"");
					} else if (receiverResource.isGFF()) {
						receiverResource.getGFF().setField(field, scriptResRef);
					}
				} else {
					receiverResRef = object.getTemplateID();
					receiverResource = this.getResourceByResRef(receiverResRef);

					if (receiverResource == null) {
						throw new NoSuchElementException(
								"Script slot update failed. Cannot find resource for ResRef \""
										+ receiverResRef + "\"");
					} else if (receiverResource.isGFF()) {
						updateAllInstances(receiverResource, scriptInfo,
								scriptResRef);
					}
				}
			}
		}
	}

	/**
	 * Updates all instances for the script and resource passed.
	 * 
	 * @param receiverResource
	 * @param scriptInfo
	 * @param scriptResRef
	 */
	private void updateAllInstances(NWNResource receiverResource,
			ScriptInfo scriptInfo, String scriptResRef) {

		GenericFileFormat receiverResourceGFF = receiverResource.getGFF();
		receiverResourceGFF.setField(scriptInfo.getSlot(), scriptResRef);

		for (NWNResource resource : this.resources) {
			if (!resource.isGFF()) {
				continue;
			}

			GenericFileFormat gff = resource.getGFF();

			if (gff.isInstanceUpdatable()) {
				gff.updateAllInstances(receiverResourceGFF,
						scriptInfo.getSlot(), scriptResRef);
			}
		}
	}

	/**
	 * Finds the resource that matches the given ResRef.
	 * 
	 * @param receiverResRef
	 *            The ResRef (resource reference string ID) for the resource
	 *            desired.
	 * @return The NWNResource matching the given ResRef, or <code>null</code>
	 *         if no such resource exists.
	 */
	private NWNResource getResourceByResRef(String receiverResRef) {
		GameConstant rep;
		String resref;

		for (NWNResource resource : this.resources) {
			if (resource.isGFF()
					&& (rep = resource.getGFF().getObjectRepresentation()) != null)
				resref = rep.getTemplateID();
			else
				resref = resource.getResRef();

			if (resref.equals(receiverResRef)) {
				return resource;
			}
		}

		return null;
	}

	@Override
	public void close() throws IOException {
		this.fileAccess.close();
	}

	@Override
	public void save(boolean compile) throws IOException {
		// size in bytes, not number of entries
		final int localizedStringsSize;
		final long offsetToLocalizedStrings;
		final long offsetToKeyList;
		final long offsetToResourceList;

		if (compile) {
			try {
				this.compile();
			} catch (FileNotFoundException e) {
				System.err.println("Compilation failed due to missing file.");
				throw new IOException("Compilation failed.", e);
			}
		}

		// Sort the NWNresources by resref. Apparently BioWare does this, not
		// that the docs say as much.
		Collections.sort(this.resources);

		// compute stuff we need to know to write.
		localizedStringsSize = this.localizedStrings.length;
		offsetToLocalizedStrings = HEADER_BYTE_SIZE;
		offsetToKeyList = offsetToLocalizedStrings + localizedStringsSize;
		offsetToResourceList = offsetToKeyList + this.resources.size()
				* ErfKey.BYTE_LENGTH;

		// start writing now
		this.writeHeader(localizedStringsSize, offsetToLocalizedStrings,
				offsetToKeyList, offsetToResourceList);

		// write the localized strings
		this.fileAccess.seek(offsetToLocalizedStrings);
		this.fileAccess.writeBytes(this.localizedStrings);

		this.writeKeys(offsetToKeyList, offsetToResourceList);
		this.writeResources(offsetToResourceList);

		// Remove everything ScriptEase related that exists to have a clean
		// slate for next time we add new stuff.
		this.removeScriptEaseData();
	}

	/**
	 * Compiles the scripts and include files with the NWN compiler.
	 * 
	 * @throws IOException
	 */
	private void compile() throws IOException {
		final File compilerLocation = TranslatorManager.getInstance()
				.getActiveTranslator().getCompiler();
		final File compilationDir = FileManager.getInstance()
				.createTempDirectory("scriptease_compile");
		final File nssRegex = new File(compilationDir,
				ErfFile.SCRIPT_FILE_PREFIX + "*.nss");

		final Map<String, File> resrefsToFiles = new HashMap<String, File>();
		final Process compilation;
		final ProcessBuilder procBuilder;

		if (!compilerLocation.exists())
			throw new GameCompilerException(new FileNotFoundException(
					"Compiler does not exist where expected."));

		// write out all of the uncompiled source code to a temp directory for
		// the compiler to grab.
		for (NWNResource uncompiled : this.uncompiledScripts) {
			File scriptFile = FileManager.getInstance().createTempFile(
					ErfFile.SCRIPT_FILE_PREFIX, ".nss", compilationDir, 16);
			resrefsToFiles.put(uncompiled.getResRef(), scriptFile);

			OutputStream out = new FileOutputStream(scriptFile);

			uncompiled.writeData(out);
			out.flush();
			out.close();
		}

		Collection<File> includeList = ((StoryModel) StoryModelPool
				.getInstance().getActiveModel()).getTranslator().getIncludes();
		for (File include : includeList) {
			try {
				String fileName = include.getName();
				FileOp.copyFile(new File(include.getParent(), fileName),
						new File(compilationDir, fileName));
			} catch (IOException e) {
				System.err.println("Error copying include file "
						+ include.getPath() + ". Skipping include.");
				continue;
			}
		}

		procBuilder = new ProcessBuilder(compilerLocation.getAbsolutePath());

		procBuilder.redirectErrorStream(true);
		procBuilder.command(compilerLocation.getAbsolutePath(),
				nssRegex.getAbsolutePath(), compilationDir.getAbsolutePath());
		procBuilder.directory(compilationDir.getAbsoluteFile());

		compilation = procBuilder.start();

		// TODO: Need to properly handle errors printed out by the compiler
		// this is simple compiler debugging output
		String line;
		BufferedReader input = new BufferedReader(new InputStreamReader(
				compilation.getInputStream()));
		while ((line = input.readLine()) != null) {
			System.err.println(line);
		}
		// end test output

		try {
			int exitValue = compilation.waitFor();

			// If the compiler exits with an error (non-zero exit value), abort
			// writing to the module.
			if (exitValue != 0) {
				throw new GameCompilerException("Compiler failed.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// get all of the compiler's compiled byte code output into resources

		for (String resRef : resrefsToFiles.keySet()) {
			final String fileName = FileOp.removeExtension(
					resrefsToFiles.get(resRef)).getAbsolutePath();
			final File byteCodeFile = new File(fileName + ".ncs");

			if (!byteCodeFile.exists()) {
				// compiler error
				throw new GameCompilerException("Compiler failed.");
			} else
				SEFrame.getInstance().setStatus("Compilation Succeeded!");

			final byte[] byteCode = FileOp.readFileAsBytes(byteCodeFile);

			NWNResource compiledResource = new NWNResource(resRef,
					ErfKey.SCRIPT_COMPILED_TYPE, byteCode);

			this.resources.add(compiledResource);
		}

		FileManager.getInstance().deleteTempFile(compilationDir);
	}

	/**
	 * Removes all ScriptEase files and any references to those files. Should be
	 * called as often as necessary so no garbage is left behind.
	 */
	private void removeScriptEaseData() {
		final List<NWNResource> blackList = new ArrayList<NWNResource>();
		GenericFileFormat gff;

		// find all of the previously generated resources
		for (NWNResource resource : this.resources) {
			if (resource.isScriptEaseGenerated()) {
				blackList.add(resource);
			} else if (resource.isGFF()) {
				gff = resource.getGFF();
				gff.removeScriptEaseReferences();
			}
		}

		/*
		 * Remove the Scriptease code from all other instances.
		 */
		for (NWNResource resource : blackList) {
			this.resources.remove(resource);
		}
	}

	/**
	 * Writes all of the ERF header information
	 * 
	 * @param localizedStringsSize
	 * @param offsetToLocalizedStrings
	 * @param offsetToKeyList
	 * @param offsetToResourceList
	 * @param year
	 * @param dayOfYear
	 * 
	 * @throws IOException
	 */
	private void writeHeader(long localizedStringsSize,
			long offsetToLocalizedStrings, long offsetToKeyList,
			long offsetToResourceList) throws IOException {
		final int entryCount = this.resources.size();
		final GregorianCalendar calendar = new GregorianCalendar();
		final int year;
		int dayOfYear;

		// Set the date. ex: Day 1 = Jan 1, 2 = Jan 2, 158 = Jun 7.
		year = calendar.get(Calendar.YEAR) - 1900;
		dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		if (calendar.isLeapYear(year))
			dayOfYear--;

		this.fileAccess.seek(0);

		this.fileAccess.writeString(this.fileType, 4);
		this.fileAccess.writeString(VERSION, 4);
		this.fileAccess.writeUnsignedInt(this.languageCount, true);
		this.fileAccess.writeUnsignedInt(localizedStringsSize, true);
		this.fileAccess.writeUnsignedInt(entryCount, true);
		this.fileAccess.writeUnsignedInt(offsetToLocalizedStrings, true);
		this.fileAccess.writeUnsignedInt(offsetToKeyList, true);
		this.fileAccess.writeUnsignedInt(offsetToResourceList, true);
		this.fileAccess.writeUnsignedInt(year, true);
		this.fileAccess.writeUnsignedInt(dayOfYear, true);
		this.fileAccess.writeUnsignedInt(this.descriptionStrRef, true);
		// Last 116 bytes are supposedly reserved. Skip them.
		/*
		 * Personally, I think this space would be a great place to store secret
		 * messages between spies. NWN was probably a big cover for
		 * international espionage. - remiller
		 */
		this.fileAccess.skipBytes(ErfFile.HEADER_RESERVED_BYTES);
	}

	private void writeKeys(long offsetToKeyList, long offsetToResourceList)
			throws IOException {
		final int entryCount = this.resources.size();
		int id = 0;

		// Writes out the ErfKey segment (i.e. palcuses, se_* file names etc)
		this.fileAccess.seek(offsetToKeyList);
		for (NWNResource resource : this.resources) {
			resource.writeErfKey(this.fileAccess, id++);
		}

		// Write null bytes at end of erf key segment until resourcelist. Again,
		// BioWare seems to do this without telling us.
		long erfKeySize = offsetToKeyList + entryCount * ErfKey.BYTE_LENGTH;
		long nullCount = offsetToResourceList - erfKeySize;
		this.fileAccess.seek(erfKeySize);
		this.fileAccess.writeNullBytes((int) (nullCount));
	}

	/**
	 * Writes all of the resources to appropriate files in Neverwinter Nights.
	 * 
	 * @param offsetToResourceList
	 * 
	 * @throws IOException
	 */
	private void writeResources(long offsetToResourceList) throws IOException {
		final int entryCount = this.resources.size();
		final long resourceListSize;
		final long offsetToResourceData;
		NWNResource resource;
		long elementOffset;
		long dataChunkOffset;

		resourceListSize = entryCount * ResourceListElement.BYTE_LENGTH;
		offsetToResourceData = offsetToResourceList + resourceListSize;

		/*
		 * Write out the ResourceListElements and their related data sections.
		 * dataChunkOffset is the location of the resource's data within the
		 * data segment.
		 */
		dataChunkOffset = offsetToResourceData;
		for (int i = 0; i < entryCount; i++) {
			resource = this.resources.get(i);

			elementOffset = i * ResourceListElement.BYTE_LENGTH;

			dataChunkOffset += resource.writeResourceListData(this.fileAccess,
					offsetToResourceList + elementOffset, dataChunkOffset);
		}
	}

	@Override
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> roots) {
		final Map<String, List<CodeBlock>> codeBlocks;
		final CodeBlockMapper codeBlockMapper;
		final List<Set<CodeBlock>> scriptBuckets;

		scriptBuckets = new ArrayList<Set<CodeBlock>>();

		// Split the story tree into groups by CodeBlock info.
		codeBlockMapper = new CodeBlockMapper();
		for (StoryComponent root : roots) {
			root.process(codeBlockMapper);
		}

		// Now that we've found all the CodeBlockComponents, sort them into
		// groups.
		codeBlocks = codeBlockMapper.getCodeBlocks();
		for (String key : codeBlocks.keySet()) {
			final Set<CodeBlock> codeBlockGroup = new HashSet<CodeBlock>();
			for (CodeBlock codeBlockStoryComponent : codeBlocks.get(key)) {
				codeBlockGroup.add(codeBlockStoryComponent);
			}
			scriptBuckets.add(codeBlockGroup);
		}

		return scriptBuckets;
	}

	@Override
	public void addGameObject(GameObject object) {
	}

	@Override
	public GameConstant getInstanceForObjectIdentifier(String id) {
		GameConstant gameResource = null;
		NWNResource nwResource = this.getResourceByResRef(id);

		// Not found? Check to see if it is a dialog line
		if (nwResource == null) {
			if (id.matches(DialogueLine.DIALOG_LINE_REF_REGEX)) {
				final String[] split = id.split("#", 2);
				final String conversationResRef = split[0];
				final String dialogResRef = split[1];

				nwResource = this.getResourceByResRef(conversationResRef);
				if (nwResource != null) {
					NWNConversation conversation = (NWNConversation) nwResource
							.getGFF().getObjectRepresentation();
					gameResource = conversation.getDialogLine(dialogResRef);
				}
			}
		} else if (nwResource.isGFF()) {
			gameResource = nwResource.getGFF().getObjectRepresentation();
		}

		return gameResource;
	}

	@Override
	public Collection<StoryRule> getCodeGenerationRules() {
		Collection<StoryRule> rules = new ArrayList<StoryRule>();
		// Make sure all parameters are bound before generating code
		rules.add(new ParameterBoundRule());
		return rules;
	}

	@Override
	public List<String> getTestCommand(ProcessBuilder builder)
			throws FileNotFoundException {
		final File nwnRoot = TranslatorManager.getInstance()
				.getTranslator(NEVERWINTER_NIGHTS)
				.getPathProperty(DescriptionKeys.GAME_DIRECTORY);
		final File nwnExec = new File(nwnRoot, "nwmain.exe");
		builder.directory(nwnRoot);

		if (!nwnRoot.exists())
			throw new FileNotFoundException("Could not locate game directory "
					+ nwnRoot.getAbsolutePath());
		else if (!nwnExec.exists())
			throw new FileNotFoundException("Missing nwmain.exe");

		List<String> argsList = new ArrayList<String>();
		argsList.add(nwnExec.getAbsolutePath());
		argsList.add("+TestNewModule");
		argsList.add(FileOp.removeExtension(this.location).getName());

		return argsList;
	}

	@Override
	public String toString() {
		return "Erf [" + this.getName() + ", nResource: "
				+ this.resources.size() + " ]";
	}
}
