package io;

import io.yaml.UnityConstructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;

import scriptease.gui.WindowFactory;
import scriptease.util.FileOp;

/**
 * Management class for handling the I/O and memory contents of a scene file.
 * 
 * @author remiller
 */
public class Scene {
	private static final Yaml parser = new Yaml(new UnityConstructor());
	static {
		parser.setName("Unity Scene YAML Parser");
	}

	private final BufferedReader reader;
	private final BufferedWriter writer;

	private final List<Object> yamlData;
	private final File location;

	/**
	 * Builds a new scene object and loads it into memory.
	 * 
	 * @param location
	 *            The scene file to read from.
	 * @throws IOException
	 *             if there is a problem during reading or creating the I/O
	 *             streams.
	 */
	public Scene(File location) throws IOException {
		if (!location.exists())
			throw new FileNotFoundException("Scene file "
					+ location.getAbsolutePath() + " went missing!");

		this.reader = new BufferedReader(new FileReader(location));
		this.writer = new BufferedWriter(new FileWriter(location));
		this.yamlData = new ArrayList<Object>();
		this.location = location;

		this.read(location);
	}

	private void read(File location) throws IOException {
		try {
			/*
			 * TODO: We can't use parser.loadAll() while SnakeYAML can't handle
			 * directives spread across multiple documents. See SnakeYAML Issue
			 * 149 (http://code.google.com/p/snakeyaml/issues/detail?id=149).
			 * Once that bug is fixed, we should be able to use the code below
			 * without problem. - remiller
			 */
			for (Object object : parser.loadAll(reader)) {
				this.yamlData.add(object);
			}

			/*
			 * // hack around the above final Collection<String> componentYamls;
			 * 
			 * componentYamls =
			 * this.applyDirectivesAcrossDocuments(this.location);
			 * 
			 * for (String doc : componentYamls) {
			 * this.yamlData.add(Scene.parser.load(doc)); } // end hack
			 */
		} catch (ReaderException e) {
			final String message;

			message = "The file "
					+ location.getPath()
					+ " isn't in the YAML format.\nThe Unity translator can't handle it.";

			WindowFactory.getInstance().showProblemDialog("Bad Scene File",
					message);

			throw new IOException("Incorrect format.");
		}
		
		System.out.println("File read.");
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
	 *             If things go south, in a major way. Velociraptor attacks and
	 *             the like.
	 */
	private Collection<String> applyDirectivesAcrossDocuments(File source)
			throws IOException {
		final String documentDelimiter = "---";
		final List<String> documents;

		final String yamlStr = FileOp.readFileAsString(source);

		documents = new ArrayList<String>(Arrays.asList(yamlStr
				.split(documentDelimiter)));
		final String directives = documents.remove(0);

		for (int i = 0; i < documents.size(); i++) {
			documents.set(i, directives + documentDelimiter + documents.get(i));
		}

		return documents;
	}

	/**
	 * Writes its contents to the file it represents.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		parser.dumpAll(this.yamlData.iterator(), this.writer);
	}

	/**
	 * Closes the streams for Scene file I/O.
	 * 
	 * @throws IOException
	 *             if there is a problem closing either stream.
	 */
	public void close() throws IOException {
		this.reader.close();
		this.writer.close();
	}

	@Override
	public String toString() {
		return "Scene <Location:" + this.location + ", Data:"
				+ this.yamlData.toString() + ">";
	}
}
