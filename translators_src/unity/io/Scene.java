package io;

import io.unityobject.PropertyValue;
import io.unityobject.UnityResource;
import io.unityobject.UnityResourceBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;

import scriptease.translator.io.model.Resource;

/**
 * Management class for handling the I/O and memory contents of a .unity scene
 * file that has been saved in YAML format.
 * 
 * @author remiller
 * @author kschenk
 */
public class Scene extends Resource {
	// There's no point of having multiple parsers unless we were reading in
	// scene files multi-threaded, which we aren't, so we just use one.
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
	 * @param seGeneratedGUIDs
	 *            We pass in a list of GUIDs of ScriptEase generated scripts so
	 *            we can remove them as soon as we load the scene file.
	 * @param location
	 *            The scene file to read from.
	 * @return A scene file. If the scene file could not be read, this returns
	 *         null.
	 * @throws IOException
	 *             if there is a problem during reading or creating the I/O
	 *             streams.
	 */
	public static Scene buildScene(File location,
			Collection<String> seGeneratedGUIDs) throws IOException {
		final Scene scene = new Scene(location);
		if (scene.read(seGeneratedGUIDs))
			return scene;
		return null;
	}

	/**
	 * Builds a new scene object and loads it into memory.
	 * 
	 * @param seGeneratedGUIDs
	 *            We pass in a list of GUIDs of ScriptEase generated scripts so
	 *            we can remove them as soon as we load the scene file.
	 * @param location
	 *            The scene file to read from.
	 * @throws IOException
	 *             if there is a problem during reading or creating the I/O
	 *             streams.
	 */
	private Scene(File location) throws IOException {
		if (!location.exists())
			throw new FileNotFoundException("Scene file "
					+ location.getAbsolutePath() + " went missing!");

		this.reader = new BufferedReader(new FileReader(location));

		this.unityObjects = new ArrayList<UnityResource>();
		this.location = location;
	}

	/**
	 * Returns a list of {@link UnityResource}s.
	 * 
	 * @return
	 */
	public List<UnityResource> getResources() {
		return this.unityObjects;
	}

	/**
	 * Reads in the Scene file from the location. Also finds and removes all
	 * ScriptEase generated content. that existed before.
	 * 
	 * @param location
	 * @param seGeneratedGUIDs
	 * @throws IOException
	 */
	private boolean read(Collection<String> seGeneratedGUIDs)
			throws IOException {
		final Iterable<Event> eventIterable;
		final UnityResourceBuilder builder;
		final Collection<UnityResource> objectsToRemove;

		String line;

		while ((line = reader.readLine()) != null) {
			if (line.equals(UnityConstants.YAML_HEADER)) {
				break;
			}
			System.err.println("Scene file has invalid line [" + line
					+ "]. Skipping.");
		}

		if (line == null || !line.equals(UnityConstants.YAML_HEADER)) {
			System.err
					.println("Could not read .unity file at " + this.location);
			return false;
		}

		eventIterable = parser.parse(reader);
		builder = new UnityResourceBuilder(eventIterable.iterator(), this);
		objectsToRemove = new ArrayList<UnityResource>();

		this.unityObjects.addAll(builder.getObjects());

		for (UnityResource object : this.unityObjects) {
			if (object.getType().equals(UnityConstants.TYPE_MONOBEHAVIOUR)) {
				final Map<String, PropertyValue> propertyMap;
				final PropertyValue scriptMapValue;
				final Map<String, PropertyValue> scriptMap;
				final String guid;

				propertyMap = object.getPropertyMap();
				scriptMapValue = propertyMap.get(UnityConstants.FIELD_M_SCRIPT);
				scriptMap = scriptMapValue.getMap();
				guid = scriptMap.get(UnityConstants.FIELD_GUID).getString();

				if (seGeneratedGUIDs.contains(guid)) {
					objectsToRemove.add(object);
				}
			}
		}

		// Remove all previous ScriptEase generated script references
		for (UnityResource object : objectsToRemove) {
			this.unityObjects.remove(object);

			final int objectID;
			final Resource ownerObject;
			final PropertyValue mComponentValue;
			final List<PropertyValue> mComponentList;

			objectID = object.getUniqueID();
			ownerObject = object.getOwner();

			if (ownerObject instanceof UnityResource) {
				mComponentValue = ((UnityResource) ownerObject)
						.getPropertyMap().get(UnityConstants.FIELD_M_COMPONENT);
				mComponentList = mComponentValue.getList();

				PropertyValue mComponentToRemove = null;

				for (PropertyValue value : mComponentList) {
					if (mComponentToRemove != null)
						break;

					final Map<String, PropertyValue> valueMap;

					valueMap = value.getMap();

					for (Entry<String, PropertyValue> entry : valueMap
							.entrySet()) {
						final int key = Integer.parseInt(entry.getKey());
						if (key == UnityConstants.TYPE_LIST
								.indexOf(UnityConstants.TYPE_MONOBEHAVIOUR)) {

							final Map<String, PropertyValue> refMap;
							final int fileID;

							refMap = entry.getValue().getMap();
							fileID = Integer.parseInt(refMap.get(
									UnityConstants.FIELD_FILEID).getString());

							if (fileID == objectID) {
								mComponentToRemove = value;
								break;
							}
						}
					}
				}

				if (mComponentToRemove != null) {
					mComponentList.remove(mComponentToRemove);
				}
			}
		}

		return true;
	}

	/**
	 * Adds a UnityResource to the list of resources in the scene. Does not add
	 * anything to the scene's code or change the model in any other way.
	 * 
	 * @param object
	 */
	public void addObject(UnityResource object) {
		this.unityObjects.add(object);
	}

	/**
	 * Removes a UnityResource from the list of resources in the scene. Does not
	 * handle any code changes.
	 * 
	 * @param object
	 */
	public void removeObject(UnityResource object) {
		this.unityObjects.remove(object);
	}

	/**
	 * Writes its contents to the file it represents.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		final BufferedWriter writer;
		final String sceneHeader = "%YAML 1.1\n" + "%TAG !u! "
				+ UnityConstants.UNITY_TAG + "\n";
		writer = new BufferedWriter(new FileWriter(location));

		writer.write(sceneHeader);

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

	/**
	 * Returns a UnityResource with a matching ID.
	 * 
	 * @param unityID
	 * @return
	 */
	public UnityResource getObjectByUnityID(int unityID) {
		for (UnityResource object : this.unityObjects) {
			if (object.getUniqueID() == unityID) {
				return object;
			}
		}

		return null;
	}

	/**
	 * Returns a UnityResource by its TemplateID.
	 * 
	 * @param templateID
	 * @return
	 */
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

	@Override
	public Collection<String> getTypes() {
		final Collection<String> type;
		type = new ArrayList<String>();
		type.add(UnityConstants.TYPE_SCENE);
		return type;
	}

	@Override
	public String getName() {
		return this.location.getName();
	}

	@Override
	public String getTag() {
		return this.location.getPath();
	}

	@Override
	public String getTemplateID() {
		return this.location.getPath();
	}

	@Override
	public String getCodeText() {
		return this.location.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Scene) {
			return this.getTemplateID().equals(((Scene) obj).getTemplateID());
		}

		return false;
	}

	@Override
	public List<Resource> getChildren() {
		final List<Resource> resources = new ArrayList<Resource>();

		for(Resource resource : this.unityObjects) {
			if(resource.getOwner() == this) {
				resources.add(resource);
			}
		}

		return resources;
	}
}
