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
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
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
	private static final String ARDUINO_SKETCHPAD_FOLDER = "/Documents/Arduino/Sketchpad";
	
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
		//The fourth element tag is a "string as a number" because the apidictionary is being cranky about 
		//generating quotes for some strings - Ehill
		
		String fileName = "hello"; 
		this.botFile = new HackEBotResource("Robot", "Robot", fileName, "1");
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
		//@todo: get this information from the translator.ini document to prevent user error
		String ArduinoLocation = System.getProperty("user.home") + ARDUINO_LIBRARY_FOLDER;
		String ArduinoSketchLocation = System.getProperty("user.home") + ARDUINO_SKETCHPAD_FOLDER;
		
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
	
	/**
	 * @todo: Currently closes without error?? Should probably investigate that.
	 */

	@Override
	public void close() throws IOException {
		
		//this.fileAccess.close();
		//this.location.close();
		
	}
	
	@Override
	public Resource getInstanceForObjectIdentifier(String id) {
		if (id.equals(this.botFile.getTemplateID())){
			return this.botFile;
		}

		return null;
	}

	/*
	 * Saving the .ses file and include files.
	 * 
	 */
	
	@Override
	public void save(boolean compile) throws IOException {		
		// Write the .ses file to the location of the mod file
		if(this.script != null){
			System.out.println(this.getLocation());
			this.script.write(this.getLocation()); 
		}
	
		// Saves the include files to the Documents/Arduino folder
		for (File includeFile : this.includeFiles) {
			final String includeName = includeFile.getName();
			final File copyDir;
			copyDir = new File(HEBIncludes);
			FileOp.copyFile(includeFile, new File(copyDir + "/" + includeName));
		}

		HackEBotScript.resetScriptCounter();
	}

	/*
	 * This is the tester for Arduino. When you press F9 or navigate to the
	 * Test Story option in the File menu of ScriptEase, it will open up 
	 * Arduino with the .ino file of your current story. 
	 * 
	 *  @todo: pressing the F9 key always opens a new Arduino window. Instead if
	 *  it is the same story, there should be a 'refresh' option that keeps the
	 *  same story window open with new info.
	 * 
	 */

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		
		final File hebRoot = TranslatorManager.getInstance()
				.getTranslator(HACKEBOT_TRANSLATOR)
				.getPathProperty(Translator.DescriptionKeys.GAME_DIRECTORY);

		final SEModel activeModel = SEModelManager.getInstance()
				.getActiveModel();
		
		final File hebExec = new File(hebRoot, "arduino.exe");
		
		//@todo: This is stinky but works for now. Gets location of title and location 
		//of the modfile and saves the .ino file to it. Should not be so hardcoded		 
		String title = activeModel.getTitle();
		final String sketch_pad = this.location + "\\" + title + "\\" + title + ".ino";

		final List<String> argsList;
		
		if (!hebRoot.exists())
			throw new FileNotFoundException("Could not locate game directory "
					+ hebRoot.getAbsolutePath());
		else if (!hebExec.exists())
			throw new FileNotFoundException("Missing Arduino.exe");

		//This is the list of instructions sent to Windows cmd that open Arduino with the correct
		//.ino file. 
		argsList = new ArrayList<String>();
		argsList.add("cmd");
		argsList.add("/c");
		argsList.add(hebExec.getAbsolutePath());
		argsList.add(sketch_pad);
		builder.command(argsList);
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
