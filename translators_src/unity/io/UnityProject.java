package io;

import io.unityconstants.UnityField;
import io.unityconstants.UnityType;
import io.unityresource.UnityResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import scriptease.gui.WindowFactory;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryModel;
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
public final class UnityProject extends GameModule {
	/**
	 * All files generated by ScriptEase should have this prefix.
	 */
	public static final String SCRIPTEASE_FILE_PREFIX = "se_";

	/**
	 * The prefix for all tags. This is followed immediately by the type number.
	 */
	public static final String UNITY_TAG = "tag:unity3d.com,2011:";

	// Note: this used to be static, but we can't make it static since we want
	// to be able to work on multiple projects at the same time.
	private final Map<String, File> guidsToMetaFiles;

	private File projectLocation;
	// The nice thing about Unity is that it uses multiple files instead of one,
	// gigantic, binary gigafile (cough NWN cough). So we have can have a
	// directory where all of our ScriptEase generated stuff is stored.
	private File scripteaseGeneratedDirectory;

	private final Collection<File> includeFiles;
	private final Collection<Scene> scenes;
	private final Collection<UnityScript> scripts;

	/**
	 * Creates a new UnityProjects with no scenes or scripts added.
	 */
	public UnityProject() {
		this.includeFiles = new ArrayList<File>();
		this.scenes = new ArrayList<Scene>();
		this.scripts = new ArrayList<UnityScript>();
		this.guidsToMetaFiles = new HashMap<String, File>();
	}

	/**
	 * Returns the active unity project based on the active model in ScriptEase.
	 * Be careful when using this, as the Unity Project must have fully loaded
	 * first.
	 * 
	 * @return
	 */
	public static UnityProject getActiveProject() {
		final SEModel model;

		model = SEModelManager.getInstance().getActiveModel();

		if (model instanceof StoryModel) {
			final GameModule module = ((StoryModel) model).getModule();

			if (module instanceof UnityProject)
				return (UnityProject) module;
		}

		throw new NullPointerException("Attempted to get active Unity Project "
				+ "when there is no Unity Project active. Active model is "
				+ model);
	}

	/**
	 * Create a random 32 char random UUID that does not already exist.
	 * 
	 * @return
	 */
	public String generateGUIDForFile(File file) {
		final Collection<String> existingGUIDs = this.guidsToMetaFiles.keySet();
		String id;
		do {
			id = UUID.randomUUID().toString().replace("-", "");
		} while (existingGUIDs.contains(id));

		this.guidsToMetaFiles.put(id, file);

		return id;
	}

	@Override
	public Collection<Resource> getAutomaticHandlers() {
		final Collection<Resource> automaticHandlers = new ArrayList<Resource>();

		for (Scene scene : this.scenes) {
			automaticHandlers.add(scene.getScriptEaseObject());
		}

		return automaticHandlers;
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
			for (Scene scene : this.scenes) {
				if (scene.getObjectByTemplateID(scriptInfo.getSubject()
						.getTemplateID()) != null) {
					this.scripts.add(new UnityScript(scriptInfo, scene));
				}
			}
		}
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
		// TODO Add any other resources we need to load (e.g. textures) here
		final List<Resource> resources;
		resources = new ArrayList<Resource>();
		if (type.equals(UnityType.SCENE.getName()))
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
			private final String SCENE_FILE_EXTENSION = ".unity";

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCENE_FILE_EXTENSION);
			}
		};
		final FileFilter metaFileFilter = new FileFilter() {
			private static final String META_EXTENSION = ".meta";

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(META_EXTENSION);
			}
		};

		final Collection<File> sceneFiles;
		final Collection<File> metaFiles;

		// sniff out .unity files and read them all into memory
		sceneFiles = FileOp.findFiles(this.projectLocation, sceneFileFilter);
		metaFiles = FileOp.findFiles(this.projectLocation, metaFileFilter);

		for (File metaFile : metaFiles) {
			final BufferedReader reader;

			reader = new BufferedReader(new FileReader(metaFile));

			String line;
			while ((line = reader.readLine()) != null) {
				final String guid = UnityField.GUID.getName();
				// Format: [guid: 3d8e5b1dcb8f4f6c86fb7422b2e687df]
				if (line.startsWith(guid)) {
					final String guidValue = line.substring(guid.length() + 2);

					this.guidsToMetaFiles.put(guidValue, metaFile);
				}
			}
			reader.close();
		}

		for (File sceneFile : sceneFiles) {
			final Scene scene;

			scene = Scene.buildScene(sceneFile, this.guidsToMetaFiles);

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

		// TODO Load other objects in "Resources" folders.

		final FileFilter resourceFolderFilter = new FileFilter() {
			private static final String RESOURCE_FOLDER_NAME = "Resources";

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(RESOURCE_FOLDER_NAME)
						&& file.isDirectory();
			}
		};

		final Collection<String> imageExtensions = new ArrayList<String>();
		// PSD, TIFF, JPG, TGA, PNG, GIF, BMP, IFF, PICT

		imageExtensions.add("psd");
		imageExtensions.add("tiff");
		imageExtensions.add("jpg");
		imageExtensions.add("tga");
		imageExtensions.add("png");
		imageExtensions.add("gif");
		imageExtensions.add("bmp");
		imageExtensions.add("iff");
		imageExtensions.add("pict");

		final FileFilter textureFilter = new FileFilter() {

			@Override
			public boolean accept(File file) {
				final String extension;

				extension = FileOp.getExtension(file).toLowerCase();

				return imageExtensions.contains(extension);
			}
		};

		final Collection<File> resourceFolders;

		resourceFolders = FileOp.findFiles(this.projectLocation,
				resourceFolderFilter);

		final Collection<File> textureFiles = new ArrayList<File>();

		for (File resourceFolder : resourceFolders) {
			textureFiles
					.addAll(FileOp.findFiles(resourceFolder, textureFilter));
		}

		for (File textureFile : textureFiles) {
			System.out.println(textureFile);
		}

		// These may be in other paths! e.g. folder/file

		// We do not need the file extension in code, but we do need
		// their type.

		// Load texture2da files
		// Should show the extension on game object.

		// Load .guiskin files
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

		for (File includeFile : this.includeFiles) {
			final File includeCopy = new File(this.scripteaseGeneratedDirectory
					+ "/" + includeFile.getName());
			FileOp.copyFile(includeFile, includeCopy);
		}

		// Reset the story to the state it was at before the save.
		this.scripts.clear();
		this.includeFiles.clear();
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
