package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameObject;
import scriptease.util.FileOp;

public final class UnityProject implements GameModule {
	private static final String SCENE_FILE_EXTENSION = ".unity";

	public static final class FileIDKey {
		public final int SCENE = 29;
		public final int GAME_OBJECT = 1;
		public final int TRANSFROM = 4;
		public final int RENDER_SETTINGS = 104;
		public final int GAME_MANAGER = 127;
		public final int LIGHT = 108;
	}

	private File location;

	@Override
	public void addGameObject(GameObject object) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<StoryRule> getCodeGenerationRules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameConstant getInstanceForObjectIdentifier(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GameConstant> getInstancesOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"The unity translator can't externally test.");
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		final Yaml parser;
		final BufferedReader reader;
		final Collection<File> unityFiles = new ArrayList<File>();
		final Collection <File> sceneFiles;
		final FileFilter sceneFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCENE_FILE_EXTENSION);
			}
		};
		
		// sniff out .unity files and read them all into memory
		sceneFiles = FileOp.findFiles(this.location, sceneFileFilter);
		
		System.out.println(sceneFiles);
		
		parser = new Yaml();
		
		reader = new BufferedReader(new FileReader(this.location));
		
		//parser.load(reader);
	}

	@Override
	public void save(boolean compile) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public File getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(File location) {
		this.location = location;
	}
}
