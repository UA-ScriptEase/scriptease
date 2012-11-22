package translators.Pinball;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import scriptease.ScriptEase;
import scriptease.controller.StartItMapper;
import scriptease.controller.modelverifier.rule.ParameterBoundRule;
import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.controller.modelverifier.rule.SubjectBoundRule;
import scriptease.gui.SEFrame;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModelPool;
import scriptease.model.complex.StartIt;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameObject;
import scriptease.translator.io.tools.ScriptEaseFileAccess;
import scriptease.util.FileOp;

public class PinballGameModule implements GameModule {

	private File location;
	private ScriptEaseFileAccess seWriter;
	private Collection<ScriptInfo> scripts = new ArrayList<ScriptInfo>();
	private Map<String, GameObject> objects = new HashMap<String, GameObject>();

	@Override
	public void addScripts(Collection<ScriptInfo> scripts) {
		this.scripts.addAll(scripts);
	}

	@Override
	public Collection<Set<StartIt>> aggregateScripts(StoryComponent root) {
		final Map<String, List<StartIt>> startIts;
		final StartItMapper startItGetter;
		final List<Set<StartIt>> scriptBuckets;
		Set<StartIt> startItGroups;

		scriptBuckets = new ArrayList<Set<StartIt>>();

		// Split the story tree into groups by startIt info.
		startItGetter = new StartItMapper();
		root.process(startItGetter);

		// Now that we've found all the StartIts, sort them into groups.
		startIts = startItGetter.getStartIts();
		startItGroups = new HashSet<StartIt>();
		for (String key : startIts.keySet()) {
			for (StartIt startIt : startIts.get(key)) {
				startItGroups.add(startIt);
			}

		}
		scriptBuckets.add(startItGroups);

		return scriptBuckets;
	}

	@Override
	public void close() throws IOException {
		if (this.seWriter != null)
			this.seWriter.close();
	}

	@Override
	public File getLocation() {
		return this.location;
	}

	@Override
	public String getName() {
		return this.location.getName();
	}

	/**
	 * We aren't worried about importing existing game scripts for pinball, so
	 * load only sets up the game objects.
	 */
	@Override
	public void load(boolean readOnly) throws IOException,
			FileNotFoundException {
		if (!this.location.exists())
			throw new FileNotFoundException();

		// Determines all the files to be read in.
		Map<String, String> typeToFileMap = this.getObjectFiles(this.location);

		// Reads in the game object files and creates scriptease game objects
		for (Entry<String, String> typeToFile : typeToFileMap.entrySet()) {
			File objectMap = new File(typeToFile.getValue());
			BufferedReader breader = buildReader(objectMap);
			gameObjectBuilder(breader, typeToFile.getKey());
		}

		// hackity hack haaaaack since there is no such thing as a game object
		Collection<String> types = new ArrayList<String>();
		types.add("game");
		LOTRPBGameObject game = new LOTRPBGameObject("Game", types);
		this.objects.put("Game", game);
	}

	/**
	 * Reads the key/value pairs, stored in the module, which map game types
	 * (switches, lamps, etc) to the files in which the type specific data is
	 * stored.
	 * 
	 * @param mainFile
	 * @return
	 */
	private Map<String, String> getObjectFiles(File mainFile) {
		Map<String, String> typeToFileMap = new HashMap<String, String>();
		BufferedReader breader = buildReader(mainFile);
		String line = "";

		do {
			try {
				line = breader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				line = null;
			}
			if (line == null)
				continue;

			int equalsSign = line.indexOf("=");
			String objectType = line.substring(0, equalsSign);
			String objectFile = line.substring(equalsSign + 1);
			typeToFileMap.put(objectType, objectFile);

		} while (line != null && !line.isEmpty());

		return typeToFileMap;
	}

	/**
	 * Builds, and returns, a buffered reader for the input file.
	 * 
	 * @param objectMap
	 * @return
	 */
	private BufferedReader buildReader(File objectMap) {
		InputStreamReader reader;
		InputStream stream = null;
		BufferedReader breader = null;

		// Open the file
		try {
			stream = new FileInputStream(objectMap);
			reader = new InputStreamReader(stream);
			breader = new BufferedReader(reader);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return breader;
	}

	/**
	 * Parses an input file, referenced by breader, and builds scriptease game
	 * objects based on the data.
	 * 
	 * @param breader
	 * @param objectType
	 * @return
	 * @throws IOException
	 */
	private String gameObjectBuilder(BufferedReader breader, String objectType)
			throws IOException {
		String line = null;

		try {
			line = breader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Button Parser
		while (line != null) {
			// Parse the coords
			int coordStart = line.indexOf("coords=");
			if (coordStart == -1) {
				try {
					line = breader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			coordStart = line.indexOf("\"", coordStart) + 1;

			int coordEnd = line.indexOf("\"", coordStart);

			// Parse the name
			int nameStart = line.indexOf("\"",
					line.indexOf("title=\"", coordEnd + 1)) + 1;
			int nameEnd = line.indexOf("\"", nameStart + 1);
			String name = line.substring(nameStart, nameEnd);

			// Create a corresponding game object
			LOTRPBGameObject gameObject;

			// Build the type list for the game object.
			ArrayList<String> typeList = new ArrayList<String>(1);
			typeList.add(objectType);

			// Handle the ONE case where the switch name does not match the
			// desired resolution code... SIGH
			if (!name.equals("Sword Lock High"))
				gameObject = new LOTRPBGameObject(name, typeList);
			else
				gameObject = new LOTRPBGameObject(name, "Sword Lock Release",
						typeList);
			this.objects.put(name, gameObject);
			line = breader.readLine();
		}
		return line;
	}

	/**
	 * Simply dumps the code from scripts to the output file.
	 * 
	 * @param compile
	 * @throws IOException
	 */
	@Override
	public void save(boolean compile) throws IOException {
		String fileName = StoryModelPool.getInstance().getActiveModel()
				.getName();
		String compilerPath = StoryModelPool.getInstance().getActiveModel()
				.getTranslator().getProperty("compilerLocation");
		String pathname = compilerPath + "/software/trunk/games/";

		if (compilerPath == null || !(new File(compilerPath).exists())) {
			pathname = ScriptEase.getInstance().getPreference(
					ScriptEase.OUTPUT_DIRECTORY_KEY)
					+ "/";
			System.out
					.println("Compiler path not found in translator.ini, defaulting output folder to ["
							+ pathname + "]");
		}

		new File(pathname + fileName).mkdirs();
		File outputFile = new File(pathname + fileName + "/" + fileName
				+ ".cpp");

		// Get rid of the old script file
		if (outputFile != null)
			outputFile.delete();

		// Write the script to the file
		this.seWriter = new ScriptEaseFileAccess(outputFile, "rw");
		for (ScriptInfo script : this.scripts) {
			seWriter.writeString(script == null ? "" : script.getCode());
		}

		System.out.println("Writting script to ["
				+ outputFile.getAbsolutePath() + "]");

		// Clean up
		this.scripts.clear();
		seWriter.close();

		// Copy necessary include files
		for (File include : TranslatorManager.getInstance()
				.getActiveTranslator().getIncludes()) {
			File includeFile = new File(pathname + fileName + "/"
					+ include.getName());
			FileOp.copyFile(include, includeFile);
			System.out.println("Copying include [" + include.getAbsolutePath()
					+ "] to [" + includeFile.getAbsolutePath() + "]");
		}

		if (compile) {
			// Get a handle to use to read compiler output
			final Process compilation;

			// Generate a makefile, used in compilation.
			// I hate that this is all hard-coded, but this hierarchy must
			// conform
			// to the prebuilt Make system that was written for the pinball API.
			String makeFileDir = compilerPath + "/software/trunk/gmake/";
			String makeFileGameDir = makeFileDir + "games/";
			File GameMakeFile = new File(makeFileGameDir + fileName);
			if (!GameMakeFile.exists() && !GameMakeFile.mkdirs()) {
				System.err
						.println("Could not create necessary directory structure for code compilation. Aborting.");
			} else {
				String makeFile = "translators/Pinball/Makefile";
				FileOp.copyFile(new File(makeFile), new File(makeFileGameDir
						+ fileName + "/Makefile"));
				FileOp.copyFile(
						new File("translators/Pinball/Makefile.prj.inc"),
						new File(makeFileGameDir + fileName
								+ "/Makefile.prj.inc"));
				FileWriter output = new FileWriter(makeFileGameDir + fileName
						+ "/Makefile.prj.inc", true);
				BufferedWriter makeFileWriter = new BufferedWriter(output);
				makeFileWriter.write("\tgames/" + fileName + "/" + fileName
						+ ".cpp");
				makeFileWriter.write("\nNAME = " + fileName);
				makeFileWriter.close();

				// Append games/fileName into the MakeFile
				try {
					updateMakeFile(makeFileDir, fileName);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}

				String[] runCommand = { "make", "-C",
						makeFileGameDir + fileName };
				try {
					compilation = Runtime.getRuntime().exec(runCommand);

					// TODO: Need to properly handle errors printed out by the
					// compiler
					// this is simple compiler debugging output
					String line;
					BufferedReader input = new BufferedReader(
							new InputStreamReader(compilation.getInputStream()));
					while ((line = input.readLine()) != null) {
						System.err.println(line);
					}

					try {
						int exitValue = compilation.waitFor();
						if (exitValue > 0) {
							String message = "Compilation Failed!";
							System.out.println(message);
							SEFrame.getInstance().setStatus(message);
						} else {
							String message = "Compilation Succeeded!";
							System.out.println(message);
							SEFrame.getInstance().setStatus(message);
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.err.println("Could not perform compilation");
				}
			}
		} else {
			String message = "Compilation Skipped";
			System.out.println(message);
			SEFrame.getInstance().setStatus(message);
		}
	}

	/**
	 * The pinball API requires a very specific configuration for a new game to
	 * be added to the makefile system that is used to compile the game scripts.
	 * This method updates the master makefile to include the new game, and then
	 * creates the game specific makefile that is required by the system.
	 * 
	 * @param path
	 * @param fileName
	 * @throws Exception
	 */
	private void updateMakeFile(String path, String fileName) throws Exception {
		String projectName = " \\\n  games/" + fileName;

		// Read in the makefile
		final FileInputStream fis = new FileInputStream(path + "Makefile");
		final BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		String makeFile = "";
		String line = null;
		while ((line = in.readLine()) != null) {
			makeFile += line + "\n";
		}

		// Parse the makefile
		Collection<String> missing = new ArrayList<String>();
		int location = makeFile.indexOf("PROJECTS =");

		location = makeFile.indexOf("\n", location) + 1;

		String aLine = makeFile.substring(location,
				makeFile.indexOf("\n", location) + 1);

		while (!aLine.trim().isEmpty()) {
			String aPath = aLine.trim().split(" ")[0];
			File aFile = new File(path + aPath);
			if (!aFile.exists())
				missing.add(aLine);
			// If the project is already in the list, don't add it
			if (aPath.equals("games/" + fileName))
				projectName = "";
			location = makeFile.indexOf("\n", location) + 1;
			aLine = makeFile.substring(location,
					makeFile.indexOf("\n", location) + 1);
		}
		location -= 1;
		String newMakeFile = makeFile.substring(0, location);
		newMakeFile += (projectName + makeFile.substring(location));

		// Remove all the missing directories
		for (String missingLine : missing) {
			newMakeFile = newMakeFile.replace(missingLine, "");
		}

		// Write out the makefile, Don't make war, makefile.
		FileWriter makeFileOut = new FileWriter(path + "Makefile", false);
		BufferedWriter makeFileOutWriter = new BufferedWriter(makeFileOut);
		makeFileOutWriter.write(newMakeFile);
		makeFileOutWriter.close();
		makeFileOut.close();
	}

	@Override
	public void setLocation(File location) {
		if (location == null)
			throw new IllegalArgumentException(
					"Cannot set a Game Module's location to null!");

		if (this.location == null)
			this.location = location;
		// if it changed, we need to remember that for the save to read from the
		// previous location
		else if (!location.equals(this.location)) {
			this.location = location;
		}
	}

	@Override
	public void addGameObject(GameObject object) {
		this.objects.put(object.getTemplateID(), object);
	}

	@Override
	public void addIncludeFiles(Collection<File> scriptList) {
		// No Op
	}

	@Override
	public GameObject getInstanceForObjectIdentifier(String id) {
		return objects.get(id);
	}

	@Override
	public List<GameObject> getInstancesOfType(String type) {
		List<GameObject> matches = new ArrayList<GameObject>();
		for (GameObject object : this.objects.values()) {
			if (object.getTypes().contains(type)) {
				matches.add(object);
			}
		}
		return matches;
	}
	
	@Override
	public GameConstant getModule()
	{
		return null;
	}

	@Override
	public Collection<StoryRule> getCodeGenerationRules() {
		Collection<StoryRule> rules = new ArrayList<StoryRule>();
		// Make sure all parameters are bound before generating code
		rules.add(new ParameterBoundRule());
		// Make sure all subjects are bound before generating code
		rules.add(new SubjectBoundRule());
		return rules;
	}

	@Override
	public String toString() {
		return "Pinball Game Module[" + this.location + "]";
	}
}
