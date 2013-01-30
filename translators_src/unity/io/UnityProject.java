package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private File projectLocation;

	private final Collection<Scene> scenes;
	private final Map<String, String> namesToScripts;

	public UnityProject() {
		this.scenes = new ArrayList<Scene>();
		this.namesToScripts = new HashMap<String, String>();
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
		final int radix = 36;

		int scriptCounter = 0;
		for (ScriptInfo scriptInfo : scriptList) {
			if (scriptInfo == null)
				continue;

			final String code;
			final String idNum;
			final String receiverName;

			code = scriptInfo.getCode();
			idNum = "_" + Integer.toString(scriptCounter++, radix);

			receiverName = "se" + idNum + "_"
					+ scriptInfo.getSubject().getName();

			this.namesToScripts.put(receiverName, code);
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
		Scene scene;
		final Collection<File> sceneFiles;
		final FileFilter sceneFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCENE_FILE_EXTENSION);
			}
		};

		// sniff out .unity files and read them all into memory
		sceneFiles = FileOp.findFiles(this.projectLocation, sceneFileFilter);
		for (File sceneFile : sceneFiles) {
			scene = new Scene(sceneFile);

			this.scenes.add(scene);
		}

		// TODO Read all meta files for scripts. Read all scripts that exist,
		// too.
	}

	@Override
	public void save(boolean compile) throws IOException {
		for (Scene scene : this.scenes) {
			scene.write();
		}

		for (Entry<String, String> entry : this.namesToScripts.entrySet()) {
			final BufferedWriter writer;
			final File scriptFile = new File(this.projectLocation,
					entry.getKey() + ".js");

			writer = new BufferedWriter(new FileWriter(scriptFile));

			writer.write(entry.getValue());

			writer.close();

			// TODO Write a meta file Wolololol
		}
	}

	@Override
	public File getLocation() {
		return new File(this.projectLocation.getAbsolutePath());
	}

	@Override
	public void setLocation(File location) {
		if (this.projectLocation == null) {
			// TODO Do we need this?
			if (!location.isDirectory())
				location = location.getParentFile();
			this.projectLocation = location;
		} else {
			throw new IllegalStateException(
					"Cannot change Unity project location after it is set.");
		}

	}

}
