package io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
 */
public final class UnityProject implements GameModule {
	private static final String SCENE_FILE_EXTENSION = ".unity";

	private File projectLocation;

	private final Collection<Scene> scenes = new ArrayList<Scene>();

	/*
	 * @Override public void addGameObject(GameObject object) { // TODO
	 * Auto-generated method stub }
	 */

	@Override
	public GameConstant getModule() {
		return null;
	}

	@Override
	public void addIncludeFiles(Collection<File> scriptList) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addScripts(Collection<ScriptInfo> scripts) {
		// TODO Auto-generated method stub
	}

	@Override
	public Collection<Set<CodeBlock>> aggregateScripts(
			Collection<StoryComponent> root) {
		return new ArrayList<Set<CodeBlock>>();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameConstant> getResourcesOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return this.projectLocation.getName();
	}

	@Override
	public List<String> getTestCommand(ProcessBuilder builder)
			throws FileNotFoundException {
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
	}

	@Override
	public void save(boolean compile) throws IOException {
		for (Scene scene : this.scenes) {
			scene.write();
		}
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
