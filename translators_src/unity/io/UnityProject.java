package io;

import io.yaml.UnityConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;
import org.yaml.snakeyaml.representer.Representer;

import scriptease.controller.modelverifier.rule.StoryRule;
import scriptease.gui.WindowManager;
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
		return this.location.getName();
	}

	@Override
	public void configureTester(ProcessBuilder builder)
			throws FileNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"The unity translator can't externally test.");
	}

	@Override
	public void load(boolean readOnly) throws IOException {
		final Yaml parser;
		BufferedReader reader;
		final Collection<File> sceneFiles;
		final FileFilter sceneFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(SCENE_FILE_EXTENSION);
			}
		};

		// sniff out .unity files and read them all into memory
		sceneFiles = FileOp.findFiles(this.location, sceneFileFilter);

		parser = new Yaml(new UnityConstructor());
		parser.setName("Unity Scene YAML Parser");

		Representer r = new Representer();

		List<List<Object>> sceneFilesObjects = new ArrayList<List<Object>>();

		for (File sceneFile : sceneFiles) {
			if (!sceneFile.exists())
				throw new FileNotFoundException("Scene file "
						+ sceneFile.getAbsolutePath() + " went missing!");

			reader = new BufferedReader(new FileReader(sceneFile));

			List<Object> objectList = new ArrayList<Object>();

			try {
				/*
				 * TODO: We can't do this while SnakeYAML can't handle
				 * directives spread across multiple documents. See Issue 149
				 * (http://code.google.com/p/snakeyaml/issues/detail?id=149)
				 * Once that bug is fixed, we should be able to use loadAll()
				 * without problem. - remiller
				 */
				// for (Object object : parser.loadAll(reader)) {
				// objectList.add(object);
				// }

				// hack around the above
				Collection<String> componentYamls = this
						.applyDirectivesToDocuments(sceneFile);

				for (String doc : componentYamls) {
					objectList.add(parser.load(doc));
				}
				// end hack
				
			} catch (ReaderException e) {
				final String message;

				message = "I can't load "
						+ this.getName()
						+ ".\n\nThe file "
						+ sceneFile.getPath()
						+ " isn't in the YAML format.\nThe Unity translator can't handle it.";

				WindowManager.getInstance().showProblemDialog("Bad Scene File",
						message);

				throw new IOException("Incorrect format.");
			} finally {
				reader.close();
			}

			sceneFilesObjects.add(objectList);
		}

		System.out.println(sceneFilesObjects);
	}

	/**
	 * This is an awful hack to get around the bug in SnakeYAML in <a
	 * href="http://code.google.com/p/snakeyaml/issues/detail?id=149"/>issue
	 * #149</a>.<br>
	 * <BR>
	 * It splits the given source file into chunks and prepends the directives
	 * from the start of the source file to each internal YAML document.
	 * 
	 * @param source
	 *            The source file to split and fix
	 * @return The YAML documents as strings.
	 * @throws IOException
	 *             If things go south, in a major way bro. Major. Way.
	 */
	private Collection<String> applyDirectivesToDocuments(File source)
			throws IOException {
		final String documentDelimiter = "---";
		final List<String> documents;// = new ArrayList<String>();

		String yamlStr = FileOp.readFileAsString(source);

		// int docStartIndex = yamlStr.indexOf(documentDelimiter);

		// String directives = yamlStr.substring(0, docStartIndex);

		documents = new ArrayList<String>(Arrays.asList(yamlStr
				.split(documentDelimiter)));
		String directives = documents.remove(0);

		// while (!yamlStr.trim().equals("")) {
		//
		//
		// actually get the thing chunked correctly
		//
		// docStartIndex = yamlStr.indexOf("---");
		//
		// yamlStr = yamlStr.substring(docStartIndex);
		// }

		for (int i = 0; i < documents.size(); i++) {
			documents.set(i, directives + documentDelimiter + documents.get(i));
		}

		return documents;
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
