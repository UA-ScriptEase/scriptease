package io;

import io.unityobject.UnityResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import scriptease.controller.CodeBlockMapper;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.util.FileOp;

/**
 * Represents a Unity Project file. Implements the GameModule interface to
 * behave as the facade of the project.
 * 
 * Unity Projects, in terms of ScriptEase, are composed of Scene files. A Scene
 * file is similar to a level. Each Scene file contains various objects in it.
 * 
 * @author remiller
 * @author kschenk
 */
public final class UnityProject implements GameModule {
	private static final String SCENE_FILE_EXTENSION = ".unity";
	private static final String SCRIPT_META_EXTENSION = ".js.meta";

	private static final Collection<String> guids = new ArrayList<String>();;

	private File projectLocation;
	// The nice thing about Unity is that it uses multiple files instead of one,
	// gigantic, binary, gigafile (cough NWN cough). So we have can have a
	// directory where all of our ScriptEase generated stuff is stored.
	private File scripteaseGeneratedDirectory;

	private final Collection<Scene> scenes;
	private final Collection<UnityScript> scripts;

	/**
	 * Creates a new UnityProjects with no scenes or scripts added.
	 */
	public UnityProject() {
		this.scenes = new ArrayList<Scene>();
		this.scripts = new ArrayList<UnityScript>();
	}

	/**
	 * Create a random 32 char random UUID that does not already exist.
	 * 
	 * @return
	 */
	public static String generateGUID() {
		String id;
		do {
			id = UUID.randomUUID().toString().replace("-", "");
		} while (guids.contains(id));

		guids.add(id);

		return id;
	}

	@Override
	public Resource getModule() {
		// TODO We need to return something that represents the module... Maybe.
		// Modules are used mostly for "Automatics", so if we don't use those,
		// we may not have to worry about this at all.
		return null;
	}

	@Override
	public void addIncludeFiles(Collection<File> scriptList) {
		// TODO Once we have our own custom libraries, they are added using
		// this.
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scriptList) {
		for (ScriptInfo scriptInfo : scriptList) {
			for (Scene scene : this.scenes) {
				if (scene.getObjectByTemplateID(scriptInfo.getSubject()
						.getTemplateID()) != null) {
					this.scripts.add(new UnityScript(scriptInfo, scene));
				}
			}
		}
	}

	@Override
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> roots) {
		// TODO We may not be aggregating these correctly, since I just ripped
		// this directly from the NWN translator. It's working right now, but
		// will likely break in one or more of these cases:
		// 1. The user adds two same causes
		// 2. The user adds two same causes with the same game object
		// 3. The user adds two different causes with the same game object

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
	public void close() throws IOException {
		for (Scene scene : this.scenes) {
			scene.close();
		}
	}

	@Override
	public Resource getInstanceForObjectIdentifier(String id) {
		for (Scene scene : this.scenes) {
			for (UnityResource object : scene.getResources())
				if (object.getTemplateID().equals(id)) {
					return object;
				}
		}
		return null;
	}

	@Override
	public List<Resource> getResourcesOfType(String type) {
		final List<Resource> resources;

		resources = new ArrayList<Resource>();
		if (type.equals(UnityConstants.TYPE_SCENE))
			resources.addAll(this.scenes);
		return resources;
	}

	@Override
	public String getName() {
		return this.projectLocation.getName();
	}

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"The unity translator can't externally test.");
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		final FileFilter sceneFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCENE_FILE_EXTENSION);
			}
		};
		final FileFilter metaFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCRIPT_META_EXTENSION);
			}
		};
		final Collection<File> sceneFiles;
		final Collection<File> scriptMetaFiles;

		// sniff out .unity files and read them all into memory
		sceneFiles = FileOp.findFiles(this.projectLocation, sceneFileFilter);
		scriptMetaFiles = FileOp
				.findFiles(this.projectLocation, metaFileFilter);

		final Collection<String> seGeneratedGUIDs = new ArrayList<String>();

		for (File scriptMetaFile : scriptMetaFiles) {
			final BufferedReader reader;

			reader = new BufferedReader(new FileReader(scriptMetaFile));

			String line;
			while ((line = reader.readLine()) != null) {
				final String guid = UnityConstants.FIELD_GUID;
				// Format: [guid: 3d8e5b1dcb8f4f6c86fb7422b2e687df]
				if (line.startsWith(guid)) {
					final String guidValue = line.substring(guid.length() + 2);

					guids.add(guidValue);

					if (scriptMetaFile.getName().startsWith(
							UnityConstants.SCRIPTEASE_FILE_PREFIX)) {
						seGeneratedGUIDs.add(guidValue);
					}
				}
			}
			reader.close();
		}

		for (File sceneFile : sceneFiles) {
			final Scene scene = Scene.buildScene(sceneFile, seGeneratedGUIDs);

			if (scene != null)
				this.scenes.add(scene);
		}

		if (this.scenes.size() <= 0)
			WindowFactory
					.getInstance()
					.showInformationDialog(
							"No Scene Files",
							"<html>No Scene files were loaded. Either none exist in "
									+ "the directory, or they were not saved as "
									+ "a text file.<br><br>"
									+ "To save a scene file as text:"
									+ "<ol><li>Close the project in ScriptEase II.</li>"
									+ "<li>Load the scene in a pro version of Unity.</li>"
									+ "<li>Under the <b>Edit Menu</b>, open the <b>Project Settings</b> submenu.</li>"
									+ "<li>Choose <b>Editor</b>. The settings will open up in the <b>Inspector</b>.</li>"
									+ "<li>Change the <b>Version Control</b> mode to <b>Meta Files</b>.</li>"
									+ "<li>Change the <b>Asset Serialization</b> mode to <b>Force Text</b>.</li>"
									+ "<li>Reload the project in ScriptEase.</li>"
									+ "<li>Celebrate with laser tag.</li></ol></html>");
	}

	@Override
	public void save(boolean compile) throws IOException {
		// Delete all files in the ScriptEase folder.
		for (File file : this.scripteaseGeneratedDirectory.listFiles()) {
			file.delete();
		}

		// Write out the scene files.
		for (Scene scene : this.scenes) {
			scene.write();
		}

		// Write the script files to the ScriptEase folder.
		for (UnityScript script : this.scripts) {
			script.write(this.scripteaseGeneratedDirectory);
			// We then remove each script from the model immediately after
			// writing it, for next time.
			script.removeFromScene();
		}

		// Reset the story to the state it was at before the save.
		this.scripts.clear();
		UnityScript.resetScriptCounter();
	}

	@Override
	public File getLocation() {
		return new File(this.projectLocation.getAbsolutePath());
	}

	@Override
	public void setLocation(File location) {
		final String SCRIPTEASE_FOLDER_NAME = "/ScriptEase Scripts";
		if (this.projectLocation == null) {
			if (!location.isDirectory())
				location = location.getParentFile();
			this.projectLocation = location;
			this.scripteaseGeneratedDirectory = new File(
					location.getAbsolutePath() + SCRIPTEASE_FOLDER_NAME);
			// Seriously Java? mkdir()? What kind of name for a method is that!?
			this.scripteaseGeneratedDirectory.mkdir();
		} else {
			throw new IllegalStateException(
					"Cannot change Unity project location after it is set.");
		}

	}

}
