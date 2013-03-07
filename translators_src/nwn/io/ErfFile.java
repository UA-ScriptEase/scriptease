package io;

import io.genericfileformat.GeneratedJournalGFF;
import io.genericfileformat.GenericFileFormat;

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

import scriptease.controller.BindingAdapter;
import scriptease.controller.CodeBlockMapper;
import scriptease.controller.FileManager;
import scriptease.controller.ModelAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.gui.StatusManager;
import scriptease.model.CodeBlock;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.GameCompilerException;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
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
 * @author kschenk
 */
public final class ErfFile implements GameModule {

	public static Translator getTranslator() {
		return TranslatorManager.getInstance().getTranslator(
				"Neverwinter Nights");
	}

	/**
	 * Matches a resref. Alphanumeric and underscore. Min 1, max 16 characters.
	 * Optional 3 character extension.
	 */
	protected static final String RESREF_REGEX = "[a-zA-Z0-9_]{1,16}(\\.[a-zA-Z0-9]{,3})?$";

	private static final String NEVERWINTER_NIGHTS = "Neverwinter Nights";
	private static final String VERSION = "V1.0";

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

	/**
	 * Adds a journal category for the scriptIt. Also makes sure that any
	 * subsequent additions to the scriptIt are observed. This method is where a
	 * journal is created if one does not already exist.
	 * 
	 * @param scriptIt
	 */
	private void addJournalCategory(final ScriptIt scriptIt) {
		final GeneratedJournalGFF journal;

		if (this.getJournalGFF() == null) {
			final ErfKey erfKey;
			final NWNResource resource;

			journal = new GeneratedJournalGFF(scriptIt);
			erfKey = new ErfKey(journal.getResRef(), ErfKey.JOURNAL_FILE_TYPE);
			resource = new NWNResource(erfKey, journal);

			ErfFile.this.resources.add(resource);
		} else {
			journal = this.getJournalGFF();
			journal.addCategory(scriptIt);
		}

		// Check if we have anything bound, and if so, adjust categories.
		for (final KnowIt parameter : scriptIt.getParameters()) {
			parameter.getBinding().process(new BindingAdapter() {
				@Override
				public void processStoryPoint(
						KnowItBindingStoryPoint storyPointBinding) {
					if (parameter.getDisplayText().equals(
							GeneratedJournalGFF.PARAMETER_STORY_POINT_TEXT)) {
						final StoryPoint storyPoint;

						storyPoint = storyPointBinding.getValue();

						if (!journal.setStoryPoint(storyPoint, scriptIt)) {
							// If set tag fails, remove binding.
							try {
								parameter.clearBinding();
							} catch (Exception e) {
								System.err
										.println("EXCEPTION! at " + parameter);
							}
						}
					}
				}

				@Override
				public void processNull(KnowItBindingNull nullBinding) {
					// Do nothing
				}
			});
		}
	}

	/**
	 * Adds journal categories for any existing effects. This is called when we
	 * load a module, so that we can create journals for all of these ScriptIts.
	 * 
	 * @param observer
	 */
	private void parseModelForJournals(final StoryPoint root) {
		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				for (StoryComponent child : storyPoint.getChildren())
					child.process(this);

				for (StoryPoint successor : storyPoint.getSuccessors())
					successor.process(this);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer storyComponentContainer) {
				this.defaultProcessComplex(storyComponentContainer);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				if (scriptIt.getDisplayText().equals(
						GeneratedJournalGFF.EFFECT_CREATE_JOURNAL_TEXT))
					ErfFile.this.addJournalCategory(scriptIt);

				scriptIt.processSubjects(this);
				scriptIt.processParameters(this);
				this.defaultProcessComplex(scriptIt);
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				KnowItBinding binding = knowIt.getBinding();
				final StoryAdapter outerAnonInnerClass = this;
				binding.process(new BindingAdapter() {
					@Override
					public void processReference(
							KnowItBindingReference reference) {
						KnowIt referenced = reference.getValue();
						referenced.process(outerAnonInnerClass);
					}

					@Override
					public void processFunction(KnowItBindingFunction function) {
						ScriptIt referenced = function.getValue();
						referenced.process(outerAnonInnerClass);
					}
				});
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIt.getCondition().process(this);
				this.defaultProcessComplex(askIt);
			}

			@Override
			public void processStoryItemSequence(StoryItemSequence sequence) {
				this.defaultProcessComplex(sequence);
			}
		};

		root.process(adapter);
	}

	/**
	 * Returns the GeneratedJournalGFF associated with the module. Note that
	 * this can return null if nothing is found.
	 * 
	 * @return
	 */
	private GeneratedJournalGFF getJournalGFF() {
		final Collection<GeneratedJournalGFF> journals;

		journals = new ArrayList<GeneratedJournalGFF>();

		for (NWNResource resource : this.resources) {
			if (resource.isGFF()) {
				final GenericFileFormat gff;

				gff = resource.getGFF();

				if (gff.getFileType().trim()
						.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP))
					if (gff instanceof GeneratedJournalGFF) {
						journals.add((GeneratedJournalGFF) gff);
					} else
						throw new IllegalArgumentException(
								"Journal resource for non-"
										+ "GeneratedJournalGFF journal "
										+ "found.");
			}
		}

		if (journals.size() > 1) {
			System.err.println("Found more than one Journal object in " + this);
			throw new IllegalArgumentException(
					"More than one journal object found!");
		}

		for (GeneratedJournalGFF journal : journals) {
			return journal;
		}

		return null;
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

		final Collection<NWNResource> journalResources;

		journalResources = new ArrayList<NWNResource>();

		// Remove any journals that may have been created.
		for (NWNResource resource : this.resources) {
			if (resource.isGFF()) {
				final GenericFileFormat gff;

				gff = resource.getGFF();

				if (gff.getFileType().trim()
						.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
					journalResources.add(resource);
				}
			}
		}
		this.resources.removeAll(journalResources);

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
	 * Returns a List of {@link Resource}s which match the given ScriptEase
	 * GameType.
	 */
	@Override
	public List<Resource> getResourcesOfType(String type) {
		final List<Resource> filteredObjects = new ArrayList<Resource>();
		GenericFileFormat gff;
		String gffType;

		for (NWNResource resource : this.resources) {
			if (resource != null && resource.isGFF()
					&& resource.generatesObject()) {
				gff = resource.getGFF();
				gffType = gff.getScriptEaseType();

				if (gffType != null && gffType.equalsIgnoreCase(type)) {
					filteredObjects.add(gff.getObjectRepresentation());
				}
			}
		}

		return filteredObjects;
	}

	@SuppressWarnings("serial")
	@Override
	public Collection<Resource> getAutomaticHandlers() {
		final Resource module;
		final List<Resource> modules = this
				.getResourcesOfType(GenericFileFormat.TYPE_MODULE);
		if (modules.size() > 0) {
			module = modules.iterator().next();
		} else {
			throw new IllegalStateException("Cannot retrieve Module");
		}

		return new ArrayList<Resource>() {
			{
				this.add(module);
			}
		};
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
		NWNResource scriptResource;
		scriptResource = new NWNResource(scriptResRef,
				ErfKey.SCRIPT_SOURCE_TYPE, code.getBytes());

		this.resources.add(scriptResource);

		return scriptResource;
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		// the 36 here is to get a number in a base 36 number system. Think of
		// it like hex that uses the whole alphabet.
		final int radix = 36;

		this.uncompiledScripts.clear();

		int scriptCounter = 0;
		for (ScriptInfo scriptInfo : scriptList) {
			if (scriptInfo == null)
				continue;

			final String code;
			final String idNum;
			final NWNResource scriptResource;
			final String receiverResRef;
			String scriptResRef;

			code = scriptInfo.getCode();
			receiverResRef = scriptInfo.getSubject().getTemplateID();

			idNum = "_" + Integer.toString(scriptCounter++, radix);

			scriptResRef = receiverResRef;

			// remove indexing info if its there
			if (scriptResRef.contains(GenericFileFormat.RESREF_SEPARATOR))
				scriptResRef = scriptResRef
						.split(GenericFileFormat.RESREF_SEPARATOR)[0];

			// same with extension.
			scriptResRef = FileOp.removeExtension(scriptResRef);

			// prepend our prefix and append our unique ID
			scriptResRef = ErfFile.SCRIPT_FILE_PREFIX + scriptResRef + idNum;

			// enforce max length on resrefs
			if (scriptResRef.length() > ErfKey.RESREF_MAX_LENGTH) {
				scriptResRef = scriptResRef.substring(0,
						ErfKey.RESREF_MAX_LENGTH - idNum.length()) + idNum;
			}

			scriptResource = this.addScript(scriptResRef, code);
			this.uncompiledScripts.add(scriptResource);

			this.update(receiverResRef, scriptResRef, scriptInfo);
		}
	}

	/**
	 * Updates all the given blueprint GFF's script slot to the given script
	 * resref. If it has instances that can be updated (ex: creatures), they are
	 * updated to reflect the new change.
	 * 
	 * @param receiverResRef
	 *            The resource resref to update.
	 * @param scriptResRef
	 *            the resref of the script to attach.
	 * @param scriptInfo
	 *            The other information about the script file.
	 */
	private void update(String receiverResRef, String scriptResRef,
			ScriptInfo scriptInfo) {
		final NWNResource receiverResource;
		String[] split;
		String index = null;

		split = receiverResRef.split(GenericFileFormat.RESREF_SEPARATOR);

		receiverResRef = split[0];
		index = split.length > 1 ? split[1] : null;

		receiverResource = this.getResourceByResRef(receiverResRef);

		if (receiverResource == null) {
			throw new NoSuchElementException(
					"Script slot update failed. Cannot find resource for ResRef \""
							+ receiverResRef + "\"");
		} else if (!receiverResource.isGFF()) {
			throw new NoSuchElementException(
					"Script slot update failed. Resource \"" + receiverResRef
							+ "\" is not a GFF file.");
		}

		GenericFileFormat blueprintGFF = receiverResource.getGFF();
		GenericFileFormat gff;

		blueprintGFF.setField(index, scriptInfo.getSlot(), scriptResRef);

		if (!blueprintGFF.isInstanceUpdatable()) {
			for (NWNResource resource : this.resources) {
				if (!resource.isGFF()) {
					continue;
				}

				gff = resource.getGFF();

				if (gff.isInstanceUpdatable()) {
					gff.updateAllInstances(blueprintGFF, scriptInfo.getSlot(),
							scriptResRef);
				}
			}
		}
	}

	/**
	 * Finds the resource that matches the given ResRef.
	 * 
	 * @param keyResRef
	 *            The ResRef (resource reference string ID) for the resource
	 *            desired.
	 * @return The NWNResource matching the given ResRef, or <code>null</code>
	 *         if no such resource exists.
	 */
	private NWNResource getResourceByResRef(String keyResRef) {
		keyResRef = keyResRef.trim();

		String resref;

		for (NWNResource resource : this.resources) {
			resref = resource.getExtendedResRef().trim();

			if (resref.equalsIgnoreCase(keyResRef)) {
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

		// Parse the model for create journal effects before it's saved.
		PatternModelManager.getInstance().getActiveModel()
				.process(new ModelAdapter() {
					@Override
					public void processStoryModel(StoryModel storyModel) {
						final StoryPoint root;

						root = storyModel.getRoot();

						ErfFile.this.parseModelForJournals(root);
					}
				});

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

		this.createBackup();

		// start writing now
		this.writeHeader(localizedStringsSize, offsetToLocalizedStrings,
				offsetToKeyList, offsetToResourceList);

		// write the localized strings
		this.fileAccess.seek(offsetToLocalizedStrings);
		this.fileAccess.writeBytes(this.localizedStrings);

		this.writeKeys(offsetToKeyList, offsetToResourceList);
		this.writeResources(offsetToResourceList);

		this.removeScriptEaseData();

		// Remove all journals from the list of resources so that we create them
		// new next time.
		final Collection<NWNResource> journalResources;

		journalResources = new ArrayList<NWNResource>();

		for (NWNResource resource : ErfFile.this.resources) {
			if (resource.isGFF()
					&& resource.getGFF() instanceof GeneratedJournalGFF)
				journalResources.add(resource);
		}
		ErfFile.this.resources.removeAll(journalResources);
	}

	private void createBackup() throws IOException {
		final File backupLocation;

		backupLocation = FileOp.replaceExtension(this.location, "SE_BackupMod");

		if (backupLocation.exists())
			backupLocation.delete();

		FileOp.copyFile(this.location, backupLocation);

		// zero length to drop all the old data.
		this.fileAccess.setLength(0);
	}

	/**
	 * Compiles the scripts and include files with the NWN compiler.
	 * 
	 * @throws IOException
	 */
	private void compile() throws IOException {
		final File compilerLocation;
		final File compilationDir;
		final File nssRegex;
		final Map<String, File> resrefsToFiles = new HashMap<String, File>();
		final Process compilation;
		final ProcessBuilder procBuilder;

		compilerLocation = ErfFile.getTranslator().getCompiler();
		compilationDir = FileManager.getInstance().createTempDirectory(
				"scriptease_compile");
		nssRegex = new File(compilationDir, ErfFile.SCRIPT_FILE_PREFIX
				+ "*.nss");

		if (!compilerLocation.exists())
			throw new GameCompilerException(new FileNotFoundException(
					"Compiler does not exist where expected."));

		// write out all of the uncompiled source code to a temp directory for
		// the compiler to grab.
		File scriptFile;
		OutputStream out;
		for (NWNResource uncompiled : this.uncompiledScripts) {
			scriptFile = new File(compilationDir, uncompiled
					.getExtendedResRef().toLowerCase());

			resrefsToFiles.put(uncompiled.getResRef(), scriptFile);

			out = new FileOutputStream(scriptFile);

			uncompiled.writeData(out);
			out.flush();
			out.close();
		}

		Collection<File> includeList = ErfFile.getTranslator().getIncludes();

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
			throw new GameCompilerException("Compiler thread was interrupted.");
		}

		// get all of the compiler's compiled byte code output into resources
		File byteCodeFile;
		byte[] byteCode;
		NWNResource compiledResource;

		for (String resRef : resrefsToFiles.keySet()) {
			byteCodeFile = FileOp.replaceExtension(resrefsToFiles.get(resRef),
					"ncs").getAbsoluteFile();

			if (!byteCodeFile.exists()) {
				// compiler error
				throw new GameCompilerException(
						"Compiler failed to create NCS file " + byteCodeFile
								+ ".");
			} else
				StatusManager.getInstance().setStatus("Compilation Succeeded!");

			byteCode = FileOp.readFileAsBytes(byteCodeFile);

			compiledResource = new NWNResource(resRef,
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

				if (!(gff instanceof GeneratedJournalGFF)) {
					gff.removeScriptEaseReferences();
				}
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
	public Resource getInstanceForObjectIdentifier(String id) {
		final Resource gameResource;
		final NWNResource nwResource;
		final String resref;
		final String index;
		final String[] split;

		// extract indexing info if it's available
		split = id.split(GenericFileFormat.RESREF_SEPARATOR, 2);
		resref = split[0];
		index = (split.length > 1) ? split[1] : null;

		nwResource = this.getResourceByResRef(resref);

		if (nwResource != null && nwResource.isGFF()) {
			gameResource = nwResource.getGFF().getObjectRepresentation(index);
		} else {
			gameResource = null;
		}

		return gameResource;
	}

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		final File nwnRoot = TranslatorManager.getInstance()
				.getTranslator(NEVERWINTER_NIGHTS)
				.getPathProperty(Translator.DescriptionKeys.GAME_DIRECTORY);
		final File nwnExec = new File(nwnRoot, "nwmain.exe");
		final List<String> argsList;
		builder.directory(nwnRoot);

		if (!nwnRoot.exists())
			throw new FileNotFoundException("Could not locate game directory "
					+ nwnRoot.getAbsolutePath());
		else if (!nwnExec.exists())
			throw new FileNotFoundException("Missing nwmain.exe");

		argsList = new ArrayList<String>();
		argsList.add(nwnExec.getAbsolutePath());
		argsList.add("+TestNewModule");
		argsList.add(FileOp.removeExtension(this.location).getName());

		builder.command(argsList);
	}

	@Override
	public String toString() {
		return "Erf [" + this.getName() + ", nResource: "
				+ this.resources.size() + " ]";
	}
}
