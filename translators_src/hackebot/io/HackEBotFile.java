package io;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import scriptease.controller.BindingAdapter;
import scriptease.controller.FileManager;
import scriptease.controller.StoryComponentUtils;
import scriptease.gui.WindowFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.GameCompilerException;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.tools.ScriptEaseFileAccess;
import scriptease.util.FileOp;

/**
 * This class represents a Hack-E-Bot configuration text file. 
 *  
 * @author ehill
 */

public final class HackEBotFile extends GameModule {

	public static Translator getTranslator() {
		return TranslatorManager.getInstance()
				.getTranslator(HACKEBOT_TRANSLATOR);
	} 

	public static final String SCRIPTEASE_FILE_PREFIX = "se_";
	public static final String INCLUDE_FILE_PREFIX = "i_se_";
	private static final String HACKEBOT_TRANSLATOR = "Hack-E-Bot";
	private static final String ARDUINO_LIBRARY_FOLDER = "/Documents/Arduino/libraries";
	
	private String HEBIncludes;
	
	//This is based on the NWN translator. Might be adaptable. 
	protected static final int RESREF_MAX_LENGTH = 16;
	private static final String VERSION = "V1.0";

	/**
	 * Location of the Hack-E-Bot File.
	 */
	private File location;
	
	private HackEBotResource botFile;
	private HackEBotScript script;
	private Collection<File> includeFiles;

	private ScriptEaseFileAccess fileAccess;
	
	public HackEBotFile() {
		//The tag is a "number" because the apidictionary is being cranky about 
		//generating quotes for some strings - Ehill
		this.botFile = new HackEBotResource("Robot", "Robot", "Robot", "1");
		this.script = null;
		this.includeFiles = new ArrayList<File>();
	}


	/**
	 * Header data
	 */
	private String fileType;
	private long languageCount;
	private long descriptionStrRef;

	// string data isn't used by us, but we need to store it to write out again.
	private byte[] localizedStrings;

	@Override
	public void load(boolean readOnly) throws IOException {

		//At this time we don't need to load anything for Hack-E-Bot
		
		
	}
	
	/**
	 * Returns a List of {@link Resource}s which match the given ScriptEase
	 * GameType.
	 */
	@Override
	public List<Resource> getResourcesOfType(String type) {

		return new ArrayList<Resource>(); 
		
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Collection<Resource>> getAutomaticHandlers() {

		final List<Resource> automatics = new ArrayList<Resource>();
		
		automatics.add(this.botFile);

		return new HashMap<String, Collection<Resource>>() {
			{
				this.put("automatic", automatics);
			}
		};
	}
	
	@Override
	public File getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(File location) {

		//This is the location of the project that you are working on
		if (location == null)
			throw new IllegalArgumentException(
					"Cannot set a Hack-E-Bot File location to null!");
		
		if (location.isDirectory()){
			this.location = location;
		} else {
			// Not sure why this wouldn't be a directory, but let's be safe.
			this.location = location.getParentFile();
		}
		
		//This is the location where we're going to send include files
		//First we'll find the Arduino libraries folder on the users Documents
		String ArduinoLocation = System.getProperty("user.home") + ARDUINO_LIBRARY_FOLDER;
		
		HEBIncludes = (ArduinoLocation + "/HEBIncludes");
		Boolean IncludeDir = (new File(HEBIncludes)).mkdir();
		if(!IncludeDir && !(new File(HEBIncludes).exists())){
			System.out.println("You failed to create a new includes directory");
		}
		
	}

	@Override
	public String getName() {
		return this.location.getName();
	}

	@Override
	public void addIncludeFiles(Collection<File> includeList) {
		for (File include : includeList) {
			this.includeFiles.add(include);
		}
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		for (ScriptInfo scriptInfo : scriptList) {
			final Resource subject = scriptInfo.getSubject();
			if (this.botFile.getTemplateID().equals(subject.getTemplateID())){
				this.script = new HackEBotScript(scriptInfo, this.botFile);
			}
		}
	}

	@Override
	public void close() throws IOException {
		/*System.out.println("why aren't you closing");
		System.out.println(fileAccess);
		System.out.println("this is the location " + this.location);
		System.out.println("This is the botfile " + this.botFile);
		this.fileAccess.close();*/
		//this.location.close();
		//eedit
		
	}
	
	@Override
	public Resource getInstanceForObjectIdentifier(String id) {
		if (id.equals(this.botFile.getTemplateID())){
			return this.botFile;
		}

		return null;
	}

	/*
	 * Eedit 
	 * 
	 */
	
	@Override
	public void save(boolean compile) throws IOException {
		
		//*EEdit put all files together later
		// Write the script files to the ScriptEase folder.
		if(this.script != null){
			this.script.write(this.getLocation()); 
		}
	
		for (File includeFile : this.includeFiles) {
			final String includeName = includeFile.getName();
			final File copyDir;

			copyDir = new File(HEBIncludes);
			FileOp.copyFile(includeFile, new File(copyDir + "/" + includeName));
		}

		// Reset the story to the state it was at before the save.
		// this.script = null;
		HackEBotScript.resetScriptCounter();
	}

	/*
	 * Compile was here. Potentially add for Arduino *EExplain*
	 * 
	 */

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"The Hack-E-Bot translator can't externally test.");
	}

	@Override
	public String toString() {
		return "Hack-E-Bot [" + this.getName() + "]";
	}

	@Override
	public String getDialogueLineType() {
		// No dialogue in Hack-E-Bot.
		return null;
	}

	@Override
	public String getDialogueType() {
		// No dialogue in Hack-E-Bot.
		return null;
	}

	@Override
	public String getImageType() {
		// We don't have images in Hack-E-Bot.
		return null;
	}

	@Override
	public String getAudioType() {
		// We also don't have Audio in Hack-E-Bot.
		return null;
	}
}
