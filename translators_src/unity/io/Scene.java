package io;

import io.unityobject.UnityResource;
import io.unityobject.UnityObjectBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private final List<UnityResource> unityObjects;
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

		this.unityObjects = new ArrayList<UnityResource>();
		this.location = location;

		this.read(location);
	}

	public List<UnityResource> getObjects() {
		return this.unityObjects;
	}

	private void read(File location) throws IOException {
		final Iterable<Event> eventIterable;
		final UnityObjectBuilder builder;

		eventIterable = parser.parse(reader);

		builder = new UnityObjectBuilder(eventIterable.iterator());

		this.unityObjects.addAll(builder.getObjects());

		System.out.println("Scene File Read with " + this.unityObjects.size()
				+ " objects");

		// TODO Remove scriptease generated stuff from model
		final int monoBehaviourType;

		monoBehaviourType = UnityTranslatorConstants.TYPE_LIST
				.indexOf("MonoBehaviour");

		for (UnityResource object : this.unityObjects) {
			if (object.getTypeNumber() == monoBehaviourType) {
				final Map<String, PropertyValue> propertyMap;
				final PropertyValue parentValue;
				final PropertyValue scriptMapValue;

				propertyMap = object.getPropertyMap();
				parentValue = propertyMap.get("m_GameObject");
				scriptMapValue = object.getPropertyMap().get("m_Script");

			}
		}
	}

	public void addObject(UnityResource object) {
		this.unityObjects.add(object);
	}

	public void removeObject(UnityResource object) {
		this.unityObjects.remove(object);
	}

	/*
	 * public void removeMonoBehaviourObject(UnityObject object) {
	 * this.removeObject(object);
	 * 
	 * PropertyValue toBeRemoved = null;
	 * 
	 * for (PropertyValue value : this.mComponentList) { if (value.isMap()) {
	 * final Map<String, PropertyValue> firstMap = value.getMap(); final
	 * PropertyValue secondMapValue = firstMap.get("114");
	 * 
	 * if (secondMapValue != null && secondMapValue.isMap()) { final Map<String,
	 * PropertyValue> secondMap = secondMapValue .getMap(); final PropertyValue
	 * fileID = secondMap.get("fileID");
	 * 
	 * if (fileID != null && fileID.equals(this.idNumber)) { // We have found
	 * the correct component. toBeRemoved = value; break; } } } }
	 * 
	 * if (toBeRemoved != null) this.mComponentList.remove(toBeRemoved); }
	 */

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
		for (Object data : this.unityObjects) {
			if (data instanceof UnityResource) {
				final UnityResource unityObj = (UnityResource) data;

				final String number = "" + unityObj.getTypeNumber();

				writer.write("--- !u!" + number + " &" + unityObj.getUniqueID()
						+ "\n");

				parser.dump(PropertyValue.convertToValueMap(unityObj
						.getTopLevelPropertyMap()), writer);
			}
		}
		writer.close();
	}

	public UnityResource getObjectByTemplateID(String templateID) {
		for (UnityResource object : this.unityObjects) {
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
				+ this.unityObjects.toString() + ">";
	}
}
