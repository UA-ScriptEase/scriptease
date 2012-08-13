package io;

import io.GenericFileFormat.GffField;
import io.GenericFileFormat.GffStruct;
import io.GenericFileFormat.NWNConversation;
import io.GenericFileFormat.NWNConversation.DialogueLine;

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
import java.util.Comparator;
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
import scriptease.translator.TranslatorManager;
import scriptease.translator.Translator.DescriptionKeys;
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
	 * OriginalOffsetToResourceData stores the location of the Data segment upon
	 * loading. It should never, ever change. It really should be final, but
	 * java won't let me. - remiller
	 */
	private long OriginalOffsetToResourceData;

	/**
	 * Location of the ErfFile.
	 */
	private File location;
	/**
	 * previousLocation exists to enable us to do lazy loading of data upon
	 * save.
	 */
	private File previousLocation;

	private ScriptEaseFileAccess fileAccess;

	/**
	 * Header data
	 */
	private String fileType;
	private String version;
	private int entryCount;
	private long OffsetToKeyList;
	private long OffsetToResourceList;

	// CONSTANTS
	private static final String SCRIPT_FILE_PREFIX = "se_";
	private static final String INCLUDE_FILE_PREFIX = "i_se_";
	private static final int RESREF_MAX_LENGTH = 16;
	private static final int HEADER_RESERVED_BYTES = 116;

	public ErfFile() {
		this.resources = new ArrayList<NWNResource>();
		this.uncompiledScripts = new ArrayList<NWNResource>();
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		this.resources.clear();

		String readOnlyTag;

		// "r" = read-only, "rw" = read-write
		if (readOnly)
			readOnlyTag = "r";
		else
			readOnlyTag = "rw";

		this.fileAccess = new ScriptEaseFileAccess(this.location, readOnlyTag);

		// read header info
		this.readHeader();

		// read all of the erf data
		List<ErfKey> keys = this.readErfKeys();
		List<ResourceListElement> elementIndexes = this.readResourceIndexList();

		// create NWNResources for each ErfKey/ResourceListElement pair
		for (int index = 0; index < this.entryCount; index++) {
			NWNResource resource = new NWNResource(keys.get(index),
					elementIndexes.get(index));

			this.resources.add(resource);
		}
	}

	/**
	 * Reads all of the header information
	 * 
	 * @throws IOException
	 */
	private void readHeader() throws IOException {
		this.fileAccess.seek(0);

		this.fileType = this.fileAccess.readString(4);
		this.version = this.fileAccess.readString(4);
		// ignore Language Count
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		// ignore Localised String Size
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.entryCount = this.fileAccess.readInt(true);
		// ignore Localised String List Offset
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.OffsetToKeyList = this.fileAccess.readInt(true);
		this.OffsetToResourceList = this.fileAccess.readInt(true);
		// ignore Build Year, Build Day, DescriptionStrRef
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);

		// Last 116 bytes are supposedly reserved. Skip them.
		this.fileAccess.skipBytes(ErfFile.HEADER_RESERVED_BYTES);

		// not technically header data in the file format, but we treat it
		// like it is - remiller
		this.OriginalOffsetToResourceData = this.OffsetToResourceList
				+ (this.entryCount * ResourceListElement.BYTE_LENGTH);
	}

	private List<ErfKey> readErfKeys() throws IOException {
		final int entryCount = this.entryCount;
		final List<ErfKey> keyList = new ArrayList<ErfKey>(entryCount);

		this.fileAccess.seek(this.OffsetToKeyList);

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

	private List<ResourceListElement> readResourceIndexList()
			throws IOException {
		final int entryCount = this.entryCount;
		final List<ResourceListElement> resourceIndexList = new ArrayList<ResourceListElement>(
				entryCount);

		// skip ahead to the location of the resource list
		this.fileAccess.seek(this.OffsetToResourceList);

		// the Erf documentation states that there must be equal number of
		// keys and ResourceListElements equal to entryCount
		for (int i = 0; i < entryCount; i++) {
			resourceIndexList.add(new ResourceListElement(this.fileAccess));
		}

		return resourceIndexList;
	}

	/**
	 * Determines whether the resource with the given resRef is ScriptEase
	 * generated.
	 * 
	 * @return True if this resource is something that was generated by
	 *         ScriptEase.
	 */
	public static boolean isScriptEaseGenerated(String resRef) {
		// TODO: Determine this based on a saved resref list in the the
		// module's associated story file. That way
		// we're not guessing: we know exactly which are generated.
		return resRef.startsWith(ErfFile.SCRIPT_FILE_PREFIX)
				|| resRef.startsWith(ErfFile.INCLUDE_FILE_PREFIX);
	}

	/**
	 * Returns a List of GameObjects which match the given ScriptEase GameType.
	 */
	public List<GameConstant> getInstancesOfType(String type) {
		List<GameConstant> filteredObjects = new ArrayList<GameConstant>();

		for (NWNResource resource : this.resources) {
			if (resource != null && resource.isGFF()) {
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

		if (this.location == null)
			this.location = location;
		// if it changed, we need to remember that for the save to read from the
		// previous location
		else if (!location.equals(this.location)) {
			this.previousLocation = this.location;
			this.location = location;
		}
	}

	@Override
	public String getName() {
		return this.location.getName();
	}

	@Override
	public void addIncludeFiles(Collection<File> includeList) {
		int scriptCounter = 0;

		for (File include : includeList) {
			if (include.getName().startsWith("."))
				continue;

			String code;
			try {
				code = FileOp.readFileAsString(include);
			} catch (IOException e) {
				System.err.println("Error reading include file "
						+ include.getPath() + ". Skipping include.");
				continue;
			}

			String scriptResRef = FileOp.removeExtension(include).getName();
			scriptCounter++;
			this.addResource(scriptResRef, code);
		}

		this.updateHeader(scriptCounter);
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
	private NWNResource addResource(String scriptResRef, String code) {
		// pear the name down to the max resref length if necessary
		if (scriptResRef.length() > ErfFile.RESREF_MAX_LENGTH)
			scriptResRef.substring(0, ErfFile.RESREF_MAX_LENGTH);

		// resRefs must be lower case
		scriptResRef = scriptResRef.toLowerCase();

		// TODO: Richard wants more meaningful resRef names for scripts
		NWNResource scriptResource;
		scriptResource = new NWNResource(scriptResRef,
				ErfKey.SCRIPT_SOURCE_TYPE, code.getBytes());

		this.resources.add(scriptResource);

		return scriptResource;
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		List<NWNResource> generatedResources = new ArrayList<NWNResource>();

		for (NWNResource resource : this.resources) {
			if (resource.isScriptEaseGenerated()) {
				generatedResources.add(resource);
			}
		}
		// Remove everything ScriptEase related that exists
		this.removeScriptEaseReferences(generatedResources);

		int scriptCounter = 0;

		this.uncompiledScripts.clear();

		for (ScriptInfo scriptInfo : scriptList) {
			if (scriptInfo != null) {
				String code = scriptInfo.getCode();

				String scriptResRef = ErfFile.SCRIPT_FILE_PREFIX
						+ Integer.toString(scriptCounter, 36);
				scriptCounter++;

				NWNResource scriptResource = this.addResource(scriptResRef,
						code);
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

		this.updateHeader(scriptCounter);
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
			if (resource.isGFF()) {
				GenericFileFormat gitFileGFF = resource.getGFF();
				String fileType = gitFileGFF.getFileType().trim();
				if (fileType
						.equalsIgnoreCase(GenericFileFormat.TYPE_GAME_INSTANCE_FILE)) {

					GffStruct gitFileStruct = gitFileGFF.getTopLevelStruct();

					List<GffField> gitFileFields;

					try {
						// The list of lists in a Git File. e.g. Creature List
						gitFileFields = gitFileStruct
								.readGffFields(this.fileAccess);

						// Go through the lists of lists in a GIT File.
						for (GffField gitFileField : gitFileFields) {

							String gitFileFieldLabel = gitFileGFF
									.getLabelArray().get(
											(int) gitFileField.getLabelIndex());

							// Find the correct list. The rest of the comments
							// will use the creature list as an example.
							if (gitFileFieldLabel.equals(receiverResourceGFF
									.getGITListLabel())) {

								// List of all (e.g.) creature structs in
								// creature list.
								List<GffStruct> gitIndividualFieldStructList = gitFileField
										.readList(this.fileAccess);

								// Parses the individual creatures from the
								// list.
								for (GffStruct individualFieldStruct : gitIndividualFieldStructList) {

									List<GffField> individualFieldStructFields = individualFieldStruct
											.readGffFields(this.fileAccess);

									// The individual creature fields, such as
									// the resref we will use.
									for (GffField individualFieldStructField : individualFieldStructFields) {
										// Checks if the field equals
										// TemplateResRef. This means the
										// individualFieldStruct creature is the
										// same as the
										// individualFieldStructField creature.
										final List<String> fieldLabels = individualFieldStructField
												.getGFF().getLabelArray();
										final int labelIndex = (int) individualFieldStructField
												.getLabelIndex();
										if (fieldLabels.get(labelIndex).equals(
												"TemplateResRef")) {
											if (individualFieldStructField
													.readString(this.fileAccess)
													.equals(receiverResourceGFF
															.getResRef())) {
												// Look for the field to place
												// the script into

												for (GffField individualFieldStructField2 : individualFieldStructFields) {
													final List<String> fieldLabels2 = individualFieldStructField2
															.getGFF()
															.getLabelArray();
													final int labelIndex2 = (int) individualFieldStructField2
															.getLabelIndex();
													if (fieldLabels2.get(
															labelIndex2)
															.equals(scriptInfo
																	.getSlot())) {
														individualFieldStructField2
																.getGFF()
																.setField(
																		individualFieldStructField2,
																		scriptResRef);
														break;
													}
												}
											}
										}
									}
								}
							}
						}
					} catch (IOException e) {
						Thread.currentThread()
								.getUncaughtExceptionHandler()
								.uncaughtException(
										Thread.currentThread(),
										new IOException(
												"Exception when accessing GFFFields:"
														+ e));
					}
				}
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
		for (NWNResource resource : this.resources) {
			if (resource.getResRef().equals(receiverResRef)) {
				return resource;
			}
		}

		return null;
	}

	/**
	 * Updates the in-memory header data. It is vitally important this method is
	 * called every time resources are added or removed. <br>
	 * <br>
	 * <code>updateHeader(int)</code> does not touch the file. To write out
	 * header data to disk, use {@link #writeHeader()}.
	 * 
	 * @param addOrRemoveCount
	 *            Because there is one key per resource, this is the number of
	 *            resources added or removed. Positive integers are interpreted
	 *            as resource being added, negatives as removed. If
	 *            <code>addOrRemoveCount<code> is zero, this method has no effect.
	 * @see #writeHeader()
	 */
	private void updateHeader(int addOrRemoveCount) {
		this.entryCount += addOrRemoveCount;
		this.OffsetToResourceList += (ErfKey.ERF_KEY_BYTE_LENGTH * addOrRemoveCount);
	}

	@Override
	public void close() throws IOException {
		this.fileAccess.close();
	}

	@Override
	public void save(boolean compile) throws IOException {
		if (compile) {
			try {
				this.compile();
			} catch (FileNotFoundException e) {
				System.err.println("Compilation failed due to missing file.");
				throw new IOException("Compilation failed.", e);
			}
		}
		List<NWNResource> generatedResources = new ArrayList<NWNResource>();

		this.writeHeader();
		this.writeResources();

		for (NWNResource resource : this.resources) {
			if (resource.isScriptEaseGenerated()) {
				generatedResources.add(resource);
			}
		}
		/*
		 * This traverses the list again because if you start removing things
		 * from a list while it's being traversed, the save method breaks.
		 */
		for (NWNResource resource : generatedResources) {
			this.resources.remove(resource);
		}

		this.updateHeader(-generatedResources.size());

		System.out.println("Size after saving Generated: " + resources.size());// +
																				// " Ungenerated: "
																				// +
																				// ungeneratedResources.size());
	}

	/**
	 * Removes all ScriptEase files that are not on the whitelist. Should be
	 * called as often as necessary so no garbage is left behind.
	 * 
	 * @param whiteList
	 */
	private void removeScriptEaseReferences(List<NWNResource> whiteList) {
		List<NWNResource> blackList = new ArrayList<NWNResource>();

		for (NWNResource resource : this.resources) {
			if (!whiteList.contains(resource)) {
				blackList.add(resource);
			}
		}

		Map<String, String> customObjectResRefFieldMap = new HashMap<String, String>();
		/*
		 * Remove the scriptease code form all instances.
		 */
		for (NWNResource resource : blackList) {
			if (resource.isGFF()) {
				GenericFileFormat resourceGFF = resource.getGFF();
				String fileType = resourceGFF.getFileType().trim();

				// Check if it's a GIT file
				if (fileType
						.equalsIgnoreCase(GenericFileFormat.TYPE_GAME_INSTANCE_FILE)) {

					GffStruct gitFileStruct = resourceGFF.getTopLevelStruct();

					List<GffField> gitFileFields;

					try {
						// The list of lists in a Git File. e.g. Creature List
						gitFileFields = gitFileStruct
								.readGffFields(this.fileAccess);

						// Go through the lists of lists in a GIT File.
						for (GffField gitFileField : gitFileFields) {
							String gitFileFieldLabel = resourceGFF
									.getLabelArray().get(
											(int) gitFileField.getLabelIndex());

							// Ignore AreaProperties field.
							if (!gitFileFieldLabel.equals("AreaProperties")) {
								List<GffStruct> individualFieldStructList = gitFileField
										.readList(this.fileAccess);
								// Individual structs (e.g. Creatures)
								for (GffStruct individualFieldStruct : individualFieldStructList) {
									List<GffField> individualFieldStructFields = individualFieldStruct
											.readGffFields(this.fileAccess);
									// Individual fields (e.g. resref, onUsed,
									// etc.)

									String templateName = "";

									for (GffField individualFieldStructField : individualFieldStructFields) {
										String fieldStructFieldLabel = resourceGFF
												.getLabelArray()
												.get((int) individualFieldStructField
														.getLabelIndex());

										if (fieldStructFieldLabel
												.equals("TemplateResRef")) {
											templateName = individualFieldStructField
													.readString(this.fileAccess);
										}
										// Only go through "Script" fields. This
										// updates the instances:
										else if (fieldStructFieldLabel
												.startsWith("Script")) {
											String scriptName = individualFieldStructField
													.readString(this.fileAccess);

											if (scriptName.startsWith("se_")) {
												customObjectResRefFieldMap.put(
														templateName,
														fieldStructFieldLabel);
												individualFieldStructField
														.getGFF()
														.setField(
																individualFieldStructField,
																"");
											}
										}
									}
								}
							}
						}
					} catch (IOException e) {
						Thread.currentThread()
								.getUncaughtExceptionHandler()
								.uncaughtException(
										Thread.currentThread(),
										new IOException(
												"Exception when accessing GFFFields:"
														+ e));
					}
				} else if (fileType
						.equalsIgnoreCase(GenericFileFormat.TYPE_DIALOGUE_BP)) {

					GffStruct dlgFileStruct = resourceGFF.getTopLevelStruct();

					List<GffField> dlgFileFields;

					try {
						// The list of lists in a Git File. e.g. Creature List
						dlgFileFields = dlgFileStruct
								.readGffFields(this.fileAccess);

						for (GffField dlgFilefield : dlgFileFields) {
							final String dlgFieldlabel = resourceGFF
									.getLabelArray().get(
											(int) dlgFilefield.getLabelIndex());

							// Handle fields:
							if (dlgFieldlabel.equals("EndConverAbort")
									|| dlgFieldlabel.equals("EndConversation")) {
								final String scriptName = dlgFilefield
										.readString(this.fileAccess);

								if (scriptName
										.startsWith(ErfFile.SCRIPT_FILE_PREFIX)) {
									resourceGFF.setField(dlgFilefield, "");
								}

								// Handle lists:
							} else if (dlgFieldlabel.equals("EntryList")
									|| dlgFieldlabel.equals("ReplyList")
									|| dlgFieldlabel.equals("StartingList")) {
								List<GffStruct> readList = dlgFilefield
										.readList(this.fileAccess);
								for (GffStruct aStruct : readList) {
									final List<GffField> listFields = aStruct
											.readGffFields(this.fileAccess);
									for (GffField listField : listFields) {
										String listFieldLabel = resourceGFF
												.getLabelArray()
												.get((int) listField
														.getLabelIndex());

										if (listFieldLabel.equals("Script")
												|| listFieldLabel
														.equals("Active")) {
											final String scriptName = listField
													.readString(this.fileAccess);

											if (scriptName
													.startsWith(ErfFile.SCRIPT_FILE_PREFIX))
												resourceGFF.setField(listField,
														"");
										}
										// This is for the sync structs inside
										// of reply and entry dialogues.
										if (listFieldLabel
												.equals("EntriesList")
												|| listFieldLabel
														.equals("RepliesList")) {
											List<GffStruct> syncList = listField
													.readList(this.fileAccess);

											for (GffStruct syncStruct : syncList) {
												final List<GffField> syncFields = syncStruct
														.readGffFields(this.fileAccess);

												for (GffField syncField : syncFields) {
													String syncFieldLabel = resourceGFF
															.getLabelArray()
															.get((int) syncField
																	.getLabelIndex());
													if (syncFieldLabel
															.equals("Active")) {
														final String scriptName = syncField
																.readString(this.fileAccess);
														if (scriptName
																.startsWith(ErfFile.SCRIPT_FILE_PREFIX))
															resourceGFF
																	.setField(
																			syncField,
																			"");
													}
												}
											}
										}
									}
								}
							}
						}
					} catch (IOException e) {
						Thread.currentThread()
								.getUncaughtExceptionHandler()
								.uncaughtException(
										Thread.currentThread(),
										new IOException(
												"Exception when accessing GFFFields:"
														+ e));
					}
				}
			}
		}
	}

	/**
	 * Checks a list of resources for the passed resref.
	 * 
	 * @param resourceList
	 * @param resRef
	 * @return
	 */
	public boolean containsResourceByResRef(List<NWNResource> resourceList,
			String resRef) {
		for (NWNResource resource : resourceList) {
			if (resource.getResRef().equals(resRef))
				return true;
		}
		return false;
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

			out.write(uncompiled.getData());
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

		// TODO: Safety. When a file isn't found below, we need to notice that
		// and throw a compiler exception to let the user know that compilation
		// failed. We should then abort the compile step.

		// get all of the compiler's compiled byte code output into resources

		int compiledScriptsCount = 0;

		for (String resRef : resrefsToFiles.keySet()) {
			final String fileName = FileOp.removeExtension(
					resrefsToFiles.get(resRef)).getAbsolutePath();
			final File byteCodeFile = new File(fileName + ".ncs");

			if (!byteCodeFile.exists()) {
				// compiler error
				System.err.println("Script " + fileName
						+ " failed to compile, script will not function.");
				SEFrame.getInstance().setStatus("Compilation Failed");
				continue;
			} else
				SEFrame.getInstance().setStatus("Compilation Succeeded!");

			final byte[] byteCode = FileOp.readFileAsBytes(byteCodeFile);

			NWNResource compiledResource = new NWNResource(resRef,
					ErfKey.SCRIPT_COMPILED_TYPE, byteCode);

			this.resources.add(compiledResource);

			compiledScriptsCount++;
		}

		this.updateHeader(compiledScriptsCount);

		System.out.println("Compiled Update Header: "
				+ this.OffsetToResourceList);

		FileManager.getInstance().deleteTempFile(compilationDir);
	}

	/**
	 * Writes all of the ERF header information
	 * 
	 * @throws IOException
	 */
	private void writeHeader() throws IOException {
		this.fileAccess.seek(0);

		this.fileAccess.writeString(this.fileType, 4);
		this.fileAccess.writeString(this.version, 4);
		// ignore Language Count
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		// ignore Localised String Size
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.fileAccess.writeInt(this.entryCount, true);
		// ignore Localised String List Offset
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		this.fileAccess.writeInt((int) (this.OffsetToKeyList), true);
		this.fileAccess.writeInt((int) (this.OffsetToResourceList), true);
		// ignore Build Year
		GregorianCalendar cal = new GregorianCalendar();

		int year = cal.get(Calendar.YEAR) - 1900;

		this.fileAccess.writeInt((int) year, true);

		// ignore Build Day
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		if (cal.isLeapYear(year))
			dayOfYear--;

		// Change the date. 1 = Jan 1. 2 = Jan 2. 158 = Jun 7.
		this.fileAccess.writeInt((int) (dayOfYear), true);
		// ignore DescriptionStrRef
		this.fileAccess.skipBytes(ScriptEaseFileAccess.INT_BYTE_LENGTH);
		// Last 116 bytes are supposedly reserved. Skip them.
		this.fileAccess.skipBytes(116);
	}

	/**
	 * Writes all of the resources to appropriate files in Neverwinter Nights.
	 * 
	 * @throws IOException
	 */
	private void writeResources() throws IOException {
		int resourceDataLocation = (int) this.OffsetToResourceList
				+ (this.entryCount * ResourceListElement.BYTE_LENGTH);

		// first, we need to cache all of the data before we start
		// clobbering the file's contents.

		// this.fileAccess.seek(this.OriginalOffsetToResourceData);

		ScriptEaseFileAccess oldFile = this.fileAccess;
		if (this.previousLocation != null)
			this.fileAccess = new ScriptEaseFileAccess(this.location,
					oldFile.isReadOnly() ? "r" : "rw");

		// Sort the NWNresources by resref.
		Comparator<NWNResource> nwnResourceComparator = new Comparator<NWNResource>() {

			@Override
			public int compare(NWNResource nwnResource1,
					NWNResource nwnResource2) {

				String nwnResourceString1 = nwnResource1.getResRef()
						.toUpperCase();

				String nwnResourceString2 = nwnResource2.getResRef()
						.toUpperCase();

				int resRefComparison = nwnResourceString1
						.compareTo(nwnResourceString2);

				if (resRefComparison == 0) {
					String extension1 = ErfKey.extensionMap.get(new Integer(
							nwnResource1.getResType()));
					String extension2 = ErfKey.extensionMap.get(new Integer(
							nwnResource2.getResType()));

					return extension1.trim().compareTo(extension2.trim());
				} else {
					return resRefComparison;
				}
			}
		};
		Collections.sort(this.resources, nwnResourceComparator);

		for (NWNResource resource : this.resources) {
			resource.setResID(this.resources.indexOf(resource));
		}

		for (NWNResource resource : this.resources) {
			resource.cacheData(oldFile);
		}

		// Writes out the ErfKey segment (i.e. palcuses, se_* file names etc)
		this.fileAccess.seek(this.OffsetToKeyList);
		for (NWNResource resource : this.resources) {
			resource.writeErfKey(this.fileAccess);
		}

		// calculate changes if the resource is a GFF
		// and if the GFF has been modified.
		for (NWNResource resource : this.resources) {
			if (resource.isGFF() && resource.getGFF().hasChanges()) {
				resource.updateForChanges(this.fileAccess);
			}
		}

		// Write null bytes at end of erf key segment until resourcelist.

		long erfKeySize = this.OffsetToKeyList + this.resources.size() * 24;
		long nullCount = this.OffsetToResourceList - erfKeySize;
		this.fileAccess.seek(erfKeySize);
		this.fileAccess.writeNullBytes((int) (nullCount));

		// then, write out the ResourceListElements
		this.fileAccess.seek(this.OffsetToResourceList);
		for (NWNResource resource : this.resources) {
			resource.writeResourceListEntry(this.fileAccess,
					resourceDataLocation);
			resourceDataLocation += resource.getResourceSize();
		}

		// and finally, write out the data segment
		this.fileAccess.seek(this.OffsetToResourceList
				+ (this.entryCount * ResourceListElement.BYTE_LENGTH));
		for (NWNResource resource : this.resources) {
			resource.write(this.fileAccess);
		}
	}

	/**
	 * This class doesn't directly represent anything in the BioWare
	 * documentation, so don't look for it.<br>
	 * <br>
	 * It's a wrapper class that is part Chain of Responsibility in that it has
	 * an ErfKey and ResourceListElement pair that it forwards requests to, and
	 * it is partly a Proxy-style class in that it is a proxy for the data in
	 * the module. It being a Proxy allows us to do lazy loading of data (like
	 * terrain) that isn't used while running ScriptEase, but still must be read
	 * so that we can move it once we are writing the module back out.
	 * 
	 * @author remiller
	 * 
	 */
	private final class NWNResource {
		/**
		 * Stores the key from the Key List segment of the Erf. This ErfKey
		 * corresponds to the ResourceListElement stored in
		 * <code>this.resourceListEntry</code>
		 */
		private final ErfKey key;
		private final ResourceListElement resourceListEntry;

		// at most one of these two will be null depending on the type of this
		// resource
		/**
		 * byteData is the contents of the file as bytes.
		 */
		private byte[] byteData;
		private final GenericFileFormat gff;

		/**
		 * Builds a NWNResource that contains the two bits of indexing
		 * information it needs to retrieve data from the file. Use this
		 * constructor for representing data already written to the file.
		 * 
		 * @param key
		 *            The ErfKey that matches <code>entry</code>
		 * @param entry
		 *            The entry that points to the location in the Data Segment
		 *            that this NWNREsource is proxy for.
		 * @throws IOException
		 * @see #ErfFile(String, ErfKey, ResourceListElement)
		 */
		public NWNResource(ErfKey key, ResourceListElement entry)
				throws IOException {
			this.key = key;
			this.resourceListEntry = entry;

			if (this.key.isGFF())
				this.gff = new GenericFileFormat(this.key.getResRef(),
						ErfFile.this.fileAccess, entry.offsetToResource);
			else
				this.gff = null;

			this.byteData = null;
		}

		public byte[] getData() {
			if (this.isGFF())
				throw new IllegalStateException(
						"Tried to get byte data from a Resource that is a GFF.");

			return this.byteData;
		}

		/**
		 * Calls write methods that are appropriate to the type of resource. For
		 * example, if it has a GFF, writer writes to the GFF.
		 * 
		 * @param writer
		 * @throws IOException
		 */
		public void write(ScriptEaseFileAccess writer) throws IOException {
			// ByteData != null for scripts and area. May need to switch this
			// check with the gff check, since the area might have a gff.
			if (this.byteData != null) {
				writer.seek(this.getOffsetToResource());
				writer.write(this.byteData);
				// GFF is used for everything else.
			} else if (this.gff != null) {
				// NOTE: Remember that this is for individual resources
				this.gff.write(writer, this.getOffsetToResource());
			} else
				throw new IllegalStateException("NWNResource has no data!");
		}

		/**
		 * Builds a new NWNResource that contains the two bits of indexing
		 * information required by the file format as well as the data it
		 * represents. Use this constructor for ScriptEase-generated data that
		 * is not a GFF. For GFF data, use
		 * {@link #NWNResource(ErfKey, ResourceListElement, GenericFileFormat)}.
		 * 
		 * @param resRef
		 *            The unique ResRef that represents this resource. The
		 *            constructor does not enforce the uniqueness; that
		 *            responsibility is left up to the caller.
		 * @param fileType
		 *            The type of this resource as per the ErfKey file type
		 *            constants.
		 * @param byteData
		 *            The byte sequence of the byteData as it will appear on
		 *            disk.
		 * @throws IOException
		 */
		public NWNResource(String resRef, short fileType, byte[] data) {
			final ResourceListElement newEntry;
			final ErfKey newKey;

			// the next resourceID is the same as the number of entries (IDs go
			// up to entryCount-1)
			newKey = new ErfKey(resRef, ErfFile.this.entryCount, fileType);

			// this resourceOffset isn't the final one. It's replaced by a real
			// offset on save. The value I'm initialising it to here is just a
			// defensive placeholder that should point to a location past the
			// end of the original file, so we don't accidentally clobber
			// something
			int resourceOffset;
			long fileSize = 0;
			try {
				fileSize = ErfFile.this.fileAccess.length();
			} catch (IOException e) {
				// somehow failed to read the file length. Just use zero.
				fileSize = 0;
			}
			resourceOffset = (int) (fileSize - ErfFile.this.OriginalOffsetToResourceData);

			newEntry = new ResourceListElement(resourceOffset, data.length);

			this.key = newKey;
			this.resourceListEntry = newEntry;
			this.byteData = data;
			this.gff = null;
		}

		/**
		 * Reads this NWNResource's data as a byte array. If the data is already
		 * in memory, then that is returned and no reading is performed. <br>
		 * <br>
		 * This method exists to allow for lazy loading of data, since there is
		 * no point in storing irrelevant game data in memory. An area's tiles
		 * are an example of irrelevant data.
		 * 
		 * @param fileAccess
		 *            The source to read from.
		 * @throws IOException
		 */
		public void cacheData(ScriptEaseFileAccess reader) throws IOException {
			if (this.gff != null) {
				this.gff.cacheData(reader, this.getResourceSize());
			} else if ((this.byteData == null) && (this.gff == null)) {
				reader.seek(this.resourceListEntry.getOffsetToResource());

				this.byteData = reader.readBytes(this.resourceListEntry
						.getResourceSize());
			}
		}

		/**
		 * Gets this NWNResource's data as a GFF file.
		 * 
		 * @return GFF representation of this NWNResource's data.
		 * @throws IllegalStateException
		 *             if this is called on a Resource that is not listed as
		 *             being a GFF type by its ErfKey.
		 */
		public GenericFileFormat getGFF() {
			if (!this.isGFF())
				throw new IllegalStateException(
						"Tried to get the GFF for a resource "
								+ this.getResRef() + " that wasn't a GFF.");

			return this.gff;
		}

		/**
		 * Gets whether this resource is something that was generated by
		 * ScriptEase.
		 * 
		 * @return True if this resource is something that was generated by
		 *         ScriptEase.
		 */
		public boolean isScriptEaseGenerated() {
			return ErfFile.isScriptEaseGenerated(this.key.getResRef());
		}

		public void writeErfKey(ScriptEaseFileAccess writer) throws IOException {
			this.key.write(writer);
		}

		/**
		 * Writes this NWNResource's ResourceListElement to disk after updating
		 * the ResourceListElement's offset location to the supplied value
		 * 
		 * @param writer
		 */
		public void writeResourceListEntry(ScriptEaseFileAccess writer,
				int resourceDataLocation) throws IOException {
			this.resourceListEntry.setOffsetToResource(resourceDataLocation);
			this.resourceListEntry.write(writer);
		}

		/**
		 * Updates the size of the resourceListEntry
		 * 
		 * @param reader
		 */
		public void updateForChanges(ScriptEaseFileAccess reader) {
			final int sizeDifference = this.gff.calculateSizeDifference(reader);

			if (sizeDifference > 0) {
				this.resourceListEntry.setResourceSize(this.resourceListEntry
						.getResourceSize() + sizeDifference);

				// grow the field data count to match the new size
				this.gff.setFieldDataCount(this.gff.getFieldDataCount()
						+ sizeDifference);

				// shift down the fieldIndicesArray
				this.gff.setFieldIndicesOffset(this.gff.getFieldIndicesOffset()
						+ sizeDifference);
				// shift down the listIndicesArray
				this.gff.setListIndicesOffset(this.gff.getListIndicesOffset()
						+ sizeDifference);
			}
		}

		public void setResID(int resID) {
			this.key.setResId(resID);
		}

		// forwarded methods
		/**
		 * @return Whether this resource is a GFF or not.
		 * @see {@link ErfKey#isGFF()}
		 */
		public boolean isGFF() {
			return this.key.isGFF();
		}

		public short getResType() {
			return this.key.getResType();
		}

		public String getResRef() {
			return this.key.getResRef();
		}

		/**
		 * @return The offset to the start of the resource data. Measured from
		 *         the start of the Data Segment.
		 * @see {@link ResourceListElement#getOffsetToResource()}
		 */
		public long getOffsetToResource() {
			return this.resourceListEntry.getOffsetToResource();
		}

		public int getResourceSize() {
			return this.resourceListEntry.getResourceSize();
		}

	}

	/**
	 * ErfKey is a simple class that represents the ErfKey struct as described
	 * in BioWare's Erf file documentation
	 * 
	 * @author jtduncan
	 * @author remiller
	 * 
	 */
	private final static class ErfKey {

		private static final int UNUSED_BYTES = 2;

		/**
		 * The size of any ErfKey in bytes
		 */
		public static final short ERF_KEY_BYTE_LENGTH = 32;
		// These types come from the Key/BIF documentation, table 1.3.1
		public static final short SCRIPT_COMPILED_TYPE = 2010;
		public static final short SCRIPT_SOURCE_TYPE = 2009;
		public static final short MODULE_IFO_TYPE = 2014;
		public static final short CREATURE_INSTANCE_TYPE = 2015;
		public static final short GAME_INSTANCE_FILE_TYPE = 2023;
		public static final short ITEM_BLUEPRINT_TYPE = 2025;
		public static final short CREATURE_BLUEPRINT_TYPE = 2027;
		public static final short CONVERSATION_FILE_TYPE = 2029;
		public static final short TILE_OR_BLUEPRINT_PALETTE_TYPE = 2030;
		public static final short TRIGGER_BLUEPRINT_TYPE = 2032;
		public static final short SOUND_BLUEPRINT_TYPE = 2035;
		public static final short GENERAL_GFF_FILE_TYPE = 2037;
		public static final short FACTION_FILE_TYPE = 2038;
		public static final short ENCOUNTER_BLUEPRINT_TYPE = 2040;
		public static final short DOOR_BLUEPRINT_TYPE = 2042;
		public static final short PLACEABLE_BLUEPRINT_TYPE = 2044;
		public static final short GAME_INSTANCE_COMMENTS_TYPE = 2046;
		public static final short GUI_LAYOUT_FILE_TYPE = 2047;
		public static final short STORE_BLUEPRINT_TYPE = 2051;
		public static final short JOURNAL_FILE_TYPE = 2056;
		public static final short WAYPOINT_BLUEPRINT_TYPE = 2058;
		public static final short PLOT_INSTANCE_TYPE = 2065;

		public static final Map<Integer, String> extensionMap = new HashMap<Integer, String>();

		static {
			extensionMap.put(0xFFFF, "N/A");
			extensionMap.put(1, "bmp");
			extensionMap.put(3, "tga");
			extensionMap.put(4, "wav");
			extensionMap.put(6, "plt");
			extensionMap.put(7, "ini");
			extensionMap.put(10, "txt");
			extensionMap.put(2002, "mdl");
			extensionMap.put(2009, "nss");
			extensionMap.put(2010, "ncs");
			extensionMap.put(2012, "are");
			extensionMap.put(2013, "set");
			extensionMap.put(2014, "ifo");
			extensionMap.put(2015, "bic");
			extensionMap.put(2016, "wok");
			extensionMap.put(2017, "2da");
			extensionMap.put(2022, "txi");
			extensionMap.put(2023, "git");
			extensionMap.put(2025, "uti");
			extensionMap.put(2027, "utc");
			extensionMap.put(2029, "dlg");
			extensionMap.put(2030, "itp");
			extensionMap.put(2032, "utt");
			extensionMap.put(2033, "dds");
			extensionMap.put(2035, "uts");
			extensionMap.put(2036, "ltr");
			extensionMap.put(2037, "gff");
			extensionMap.put(2038, "fac");
			extensionMap.put(2040, "ute");
			extensionMap.put(2042, "utd");
			extensionMap.put(2044, "utp");
			extensionMap.put(2045, "dft");
			extensionMap.put(2046, "gic");
			extensionMap.put(2047, "gui");
			extensionMap.put(2051, "utm");
			extensionMap.put(2052, "dwk");
			extensionMap.put(2053, "pwk");
			extensionMap.put(2056, "jrl");
			extensionMap.put(2058, "utw");
			extensionMap.put(2060, "ssf");
			extensionMap.put(2064, "ndb");
			extensionMap.put(2065, "ptm");
			extensionMap.put(2066, "ptt");
			extensionMap.put(2023, "git");
			extensionMap.put(2025, "uti");
			extensionMap.put(2027, "utc");
			extensionMap.put(2029, "dlg");
			extensionMap.put(2030, "itp");
			extensionMap.put(2032, "utt");
			extensionMap.put(2033, "dds");
			extensionMap.put(2035, "uts");
			extensionMap.put(2036, "ltr");
			extensionMap.put(2037, "gff");
			extensionMap.put(2038, "fac");
			extensionMap.put(2040, "ute");
			extensionMap.put(2042, "utd");
			extensionMap.put(2044, "utp");
			extensionMap.put(2045, "dft");
			extensionMap.put(2046, "gic");
			extensionMap.put(2047, "gui");
			extensionMap.put(2051, "utm");
			extensionMap.put(2052, "dwk");
			extensionMap.put(2053, "pwk");
			extensionMap.put(2056, "jrl");
			extensionMap.put(2058, "utw");
			extensionMap.put(2060, "ssf");
			extensionMap.put(2064, "ndb");
			extensionMap.put(2065, "ptm");
			extensionMap.put(2066, "ptt");
		}

		private final String resRef;
		private int resId;
		private final short resType;

		@Override
		public String toString() {
			return "ErfKey [" + resRef + ", " + resId + ", " + resType + "]";
		}

		/**
		 * Reads an ErfKey from the file.
		 * 
		 * @param stream
		 *            The stream from which the ErfKey is read.
		 * @throws IOException
		 */
		public ErfKey(ScriptEaseFileAccess stream) throws IOException {
			this(stream.readString(ErfFile.RESREF_MAX_LENGTH), stream
					.readInt(true), stream.readShort(true));
			stream.skipBytes(ErfKey.UNUSED_BYTES);// Unused space
		}

		/**
		 * Creates a new ErfKey.
		 * 
		 * @param resRef
		 *            The unique name of the resource.
		 * @param resourceID
		 *            The resource ID number. This is equivalent to
		 *            <code>( ErfKeyFileLocation - OffSetToKeyList ) / entryCount</code>
		 * @param resourceType
		 *            The type of the resource from table 1.3.1 in the Key/BIF
		 *            documentation
		 */
		public ErfKey(String resRef, int resourceID, short resourceType) {
			this.resRef = resRef;
			this.resId = resourceID;
			this.resType = resourceType;
		}

		/**
		 * Writes an ErfKey to the file.
		 * 
		 * @param stream
		 *            The stream that will perform the writing.
		 * @throws IOException
		 */
		public void write(ScriptEaseFileAccess stream) throws IOException {
			stream.writeString(this.resRef, ErfFile.RESREF_MAX_LENGTH);
			stream.writeInt(this.resId, true);
			stream.writeShort(this.resType, true);
			stream.writeNullBytes(ErfKey.UNUSED_BYTES);// Unused space
		}

		public void setResId(int resID) {
			this.resId = resID;
		}

		public short getResType() {
			return this.resType;
		}

		public String getResRef() {
			return this.resRef;
		}

		/**
		 * Determines whether the key represents a GFF file. This test is based
		 * off of the Key/Bif documentation, table 1.3.1
		 * 
		 * @return True if the key represents a GFF file
		 */
		public boolean isGFF() {
			return (this.resType == MODULE_IFO_TYPE)
					|| (this.resType == CREATURE_INSTANCE_TYPE)
					|| (this.resType == GAME_INSTANCE_FILE_TYPE)
					|| (this.resType == ITEM_BLUEPRINT_TYPE)
					|| (this.resType == CREATURE_BLUEPRINT_TYPE)
					|| (this.resType == CONVERSATION_FILE_TYPE)
					|| (this.resType == TILE_OR_BLUEPRINT_PALETTE_TYPE)
					|| (this.resType == TRIGGER_BLUEPRINT_TYPE)
					|| (this.resType == SOUND_BLUEPRINT_TYPE)
					|| (this.resType == GENERAL_GFF_FILE_TYPE)
					|| (this.resType == FACTION_FILE_TYPE)
					|| (this.resType == ENCOUNTER_BLUEPRINT_TYPE)
					|| (this.resType == DOOR_BLUEPRINT_TYPE)
					|| (this.resType == PLACEABLE_BLUEPRINT_TYPE)
					|| (this.resType == GAME_INSTANCE_COMMENTS_TYPE)
					|| (this.resType == GUI_LAYOUT_FILE_TYPE)
					|| (this.resType == STORE_BLUEPRINT_TYPE)
					|| (this.resType == JOURNAL_FILE_TYPE)
					|| (this.resType == WAYPOINT_BLUEPRINT_TYPE)
					|| (this.resType == PLOT_INSTANCE_TYPE);
		}
	}

	/**
	 * This is simply indexing information into the Resource Data block in the
	 * input file. Think of it as a pointer from C if that helps. <br>
	 * <br>
	 * The name is misleading, but you can blame Bioware for that. I just wanted
	 * to be consistent with their documentation. - jtduncan
	 * 
	 * @author jtduncan
	 * @author remiller
	 */
	private final class ResourceListElement {
		/**
		 * The size of any ResourceListElement in bytes
		 */
		public static final short BYTE_LENGTH = 8;

		private int offsetToResource;
		private int resourceSize;

		/**
		 * Builds a ResourceListElement by reading from the file.
		 * 
		 * @param fileAccess
		 *            The file to read from.
		 * @throws IOException
		 */
		public ResourceListElement(ScriptEaseFileAccess reader)
				throws IOException {
			this(reader.readInt(true), reader.readInt(true));
		}

		/**
		 * Writes a ResourceListElement to disk.
		 * 
		 * @param offset
		 *            The offset into the Resource Data segment of the ErfFile
		 *            for the resource this ResourceListElement points to.
		 * @param size
		 *            The size of this resource.
		 */
		public ResourceListElement(int offset, int size) {
			this.offsetToResource = offset;
			this.resourceSize = size;
		}

		/**
		 * @param writer
		 *            The file to write to.
		 * @throws IOException
		 */
		private void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeInt(this.offsetToResource, true);
			writer.writeInt(this.resourceSize, true);
		}

		/**
		 * Get the offset into the Resource Data segment. <br>
		 * <br>
		 * NOTE: This is <b>different from the documentation</b>. In the docs,
		 * it says this is an offset from the start of the file, but that
		 * becomes a nightmare later when we're writing our own stuff piecemeal.
		 * 
		 * @return Offset into the Resource Data segment
		 */
		public int getOffsetToResource() {
			return this.offsetToResource;
		}

		/**
		 * Updates the offset into the Resource Data segment to the given value.
		 * Only use this when saving the entire Erf. <br>
		 * <br>
		 * NOTE: This is <b>different from the documentation</b>. In the docs,
		 * it says this is an offset from the start of the file, but that
		 * becomes a nightmare later when we're writing our own stuff piecemeal.
		 * 
		 * @return Offset into the Resource Data segment
		 */
		public void setOffsetToResource(int offset) {
			this.offsetToResource = offset;
		}

		/**
		 * Updates the size of the resource.
		 * 
		 * @param newSize
		 */
		public void setResourceSize(int newSize) {
			this.resourceSize = newSize;
		}

		/**
		 * Gets the resource size.
		 * 
		 * @return The resource size.
		 */
		public int getResourceSize() {
			return this.resourceSize;
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
		NWNResource resource = this.getResourceByResRef(id);

		// check to see if it is a dialog line
		if (resource == null) {
			if (id.matches(DialogueLine.DIALOG_LINE_REF_REGEX)) {
				final String[] split = id.split("#", 2);
				final String conversationResRef = split[0];
				final String dialogResRef = split[1];

				final NWNResource convoResource = this
						.getResourceByResRef(conversationResRef);
				if (convoResource != null) {
					NWNConversation conversation = (NWNConversation) convoResource
							.getGFF().getObjectRepresentation();
					return conversation.getDialogLine(dialogResRef);
				}
			}
		}

		if (resource != null && resource.isGFF())
			return resource.getGFF().getObjectRepresentation();
		else
			return null;

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

	public List<GameConstant> getResourcesOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}
}
