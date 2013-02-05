package io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import scriptease.controller.CodeBlockMapper;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameModule;
import scriptease.util.FileOp;

/**
 * Represents a Unity Project file. Implements the GameModule interface to
 * behave as the facade of the project.
 * 
 * @author remiller
 * @author kschenk
 */
public final class UnityProject implements GameModule {
	private static final String SCENE_FILE_EXTENSION = ".unity";
	private static final String SCRIPT_META_EXTENSION = ".js.meta";

	private static final Collection<String> guids = new ArrayList<String>();;

	private File projectLocation;

	private final Collection<Scene> scenes;
	private final Collection<UnityScript> scripts;

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
	public GameConstant getModule() {
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
		this.scenes.clear();
	}

	@Override
	public GameConstant getInstanceForObjectIdentifier(String id) {
		for (Scene scene : this.scenes) {
			for (UnityObject object : scene.getObjects())
				if (object.getTemplateID().equals(id)) {
					return object;
				}
		}
		return null;
	}

	@Override
	public List<GameConstant> getResourcesOfType(String type) {
		final List<GameConstant> objects;

		objects = new ArrayList<GameConstant>();

		for (Scene scene : this.scenes) {
			objects.addAll(scene.getObjects());
		}
		return objects;
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

		for (File sceneFile : sceneFiles) {
			this.scenes.add(new Scene(sceneFile));
		}

		for (File scriptMetaFile : scriptMetaFiles) {
			// TODO Load used GUIDs into memory.
			// Second line in file is "guid: c7eb21f478d84f3cb4b98675845beb98"
			// Read that and save it.
		}
	}

	@Override
	public void save(boolean compile) throws IOException {
		// TODO Delete all "se_" saved script files
		// ^ we may not have to do this. Looks like Unity is doing this by
		// itself.
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("se_");
			}
		};

		for (File file : FileOp.findFiles(this.projectLocation, filter)) {
			file.delete();
		}

		for (Scene scene : this.scenes) {
			scene.write();
		}

		for (UnityScript script : this.scripts) {
			script.write(this.projectLocation);
			script.removeFromScene();
		}

		UnityScript.resetScriptCounter();
	}

	@Override
	public File getLocation() {
		return new File(this.projectLocation.getAbsolutePath());
	}

	@Override
	public void setLocation(File location) {
		if (this.projectLocation == null) {
			if (!location.isDirectory())
				location = location.getParentFile();
			this.projectLocation = location;
		} else {
			throw new IllegalStateException(
					"Cannot change Unity project location after it is set.");
		}

	}

}
