package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;

/**
 * Management class for handling the I/O and memory contents of a .unity scene
 * file that has been saved in YAML format.
 * 
 * @author remiller
 * @author kschenk
 */
public class Scene {
	private static final Yaml parser = new Yaml();
	static {
		parser.setName("Unity Scene YAML Parser");
	}

	private final BufferedReader reader;
	private final List<UnityObject> yamlData;
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

		this.yamlData = new ArrayList<UnityObject>();
		this.location = location;

		this.read(location);
	}

	public List<UnityObject> getObjects() {
		return this.yamlData;
	}

	private void read(File location) throws IOException {
		final Iterable<Event> eventIterable;
		final UnityObjectBuilder builder;

		eventIterable = parser.parse(reader);

		builder = new UnityObjectBuilder(eventIterable.iterator());

		this.yamlData.addAll(builder.getObjects());

		System.out.println("Scene File Read with " + this.yamlData.size()
				+ " objects");
	}

	public void addObject(UnityObject object) {
		this.yamlData.add(object);
	}

	/**
	 * Writes its contents to the file it represents.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		final BufferedWriter writer;

		writer = new BufferedWriter(new FileWriter(location));

		writer.write("%YAML 1.1\n" + "%TAG !u! tag:unity3d.com,2011:\n");

		// Add an arbitrary number
		for (Object data : this.yamlData) {
			if (data instanceof UnityObject) {
				final UnityObject unityObj = (UnityObject) data;

				final String number = "" + unityObj.getTypeNumber();

				writer.write("--- !u!" + number + " &" + unityObj.getUniqueID()
						+ "\n");

				parser.dump(PropertyValue.convertToValueMap(unityObj
						.getPropertyMap()), writer);
			}
		}
		writer.close();
	}

	public UnityObject getObjectByTemplateID(String templateID) {
		for (UnityObject object : this.yamlData) {
			if (object.getTemplateID().equals(templateID))
				return object;
		}

		return null;
	}

	/**
	 * Closes the streams for Scene file I/O.
	 * 
	 * @throws IOException
	 *             if there is a problem closing either stream.
	 */
	public void close() throws IOException {
		this.reader.close();
	}

	@Override
	public String toString() {
		return "Scene <Location:" + this.location + ", Data:"
				+ this.yamlData.toString() + ">";
	}
}
