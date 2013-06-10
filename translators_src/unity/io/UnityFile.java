package io;

import io.constants.UnityField;
import io.constants.UnityType;
import io.unityresource.PropertyValue;
import io.unityresource.UnityResource;
import io.unityresource.UnityResourceFactory;

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
 * Management class for handling the I/O and memory contents of a unity file
 * that has been saved in YAML format.
 * 
 * @author remiller
 * @author kschenk
 */
public class UnityFile extends Resource {
	// The first line in any valid YAML file.
	private static final String YAML_HEADER = "%YAML 1.1";
	private static final String SCRIPTEASE_OBJECT_NAME = "ScriptEase";

	// There's no point of having multiple parsers unless we were reading in
	// unity files multi-threaded, which we aren't, so we just use one.
	private static final Yaml parser = new Yaml();
	static {
		parser.setName("Unity Scene YAML Parser");
	}

	private final BufferedReader reader;
	private final File location;

	private final Collection<String> types;
	private final List<UnityResource> unityResources;
	private final List<Resource> visibleChildren;

	private UnityResource scriptEaseObject = null;

	/**
	 * Builds a new unity file object and loads it into memory.
	 * 
	 * @param seGeneratedGUIDs
	 *            We pass in a list of GUIDs of ScriptEase generated scripts so
	 *            we can remove them as soon as we load the unity file.
	 * @param location
	 *            The unity file to read from.
	 * @param types
	 *            The supported types for the unity file.
	 * @return A unity file. If the unity file could not be read, this returns
	 *         null.
	 * @throws IOException
	 *             if there is a problem during reading or creating the I/O
	 *             streams.
	 */
	public static UnityFile buildUnityFile(File location,
			Map<String, File> guidsToMetaFiles, Collection<String> types)
			throws IOException {

		final String HIDDEN_FILE_PREFIX = "._";
		final String locationName = location.getName();

		if (!locationName.startsWith(HIDDEN_FILE_PREFIX)) {
			final UnityFile unityFile = new UnityFile(location, types);
			if (unityFile.read(guidsToMetaFiles))
				return unityFile;
		} else
			System.err.println("Did not read Unity file at " + locationName
					+ " -- Scene files starting with " + HIDDEN_FILE_PREFIX
					+ " are hidden by Unity, and so we do not read them "
					+ "either.");
		return null;
	}

	/**
	 * Builds a new unity file object and loads it into memory.
	 * 
	 * @param seGeneratedGUIDs
	 *            We pass in a list of GUIDs of ScriptEase generated scripts so
	 *            we can remove them as soon as we load the scene file.
	 * @param location
	 *            The unity file file to read from.
	 * @throws IOException
	 *             if there is a problem during reading or creating the I/O
	 *             streams.
	 */
	protected UnityFile(File location, Collection<String> types)
			throws IOException {
		
		if (!location.exists())
			throw new FileNotFoundException("Unity file "
					+ location.getAbsolutePath() + " went missing!");

		this.reader = new BufferedReader(new FileReader(location));
		this.visibleChildren = new ArrayList<Resource>();
		this.unityResources = new ArrayList<UnityResource>();
		this.types = types;
		this.location = location;
	}

	/**
	 * The ScriptEase object is the invisible placeholder object that has
	 * startup scripts on it.
	 * 
	 * @return
	 */
	public UnityResource getScriptEaseObject() {
		return this.scriptEaseObject;
	}

	/**
	 * Returns a list of {@link UnityResource}s.
	 * 
	 * @return
	 */
	public List<UnityResource> getResources() {
		return this.unityResources;
	}

	/**
	 * Reads in the Unity file from the location. Also finds and removes all
	 * ScriptEase generated content. that existed before.
	 * 
	 * @param location
	 * @param seGeneratedGUIDs
	 * @throws IOException
	 */
	protected boolean read(Map<String, File> guidsToMetaFiles)
			throws IOException {
		// First check if the first line is a valid Unity YAML header
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equals(YAML_HEADER)) {
				break;
			}
			System.err.println("Skipping " + this.location.getName()
					+ "'s invalid line [" + line + "]");
		}

		if (line == null || !line.equals(YAML_HEADER)) {
			System.err
					.println("Could not read .unity file at " + this.location);
			return false;
		}

		final Collection<String> seGeneratedGUIDs;
		final Iterable<Event> eventIterable;
		final Collection<UnityResource> objectsToRemove;

		eventIterable = parser.parse(reader);
		objectsToRemove = new ArrayList<UnityResource>();
		seGeneratedGUIDs = new ArrayList<String>();

		// Build all of the resources
		this.unityResources.addAll(UnityResourceFactory.getInstance()
				.buildResources(eventIterable.iterator()));
		
		// Get all GUIDs from scriptease generated files and save them.
		for (Entry<String, File> entry : guidsToMetaFiles.entrySet()) {
			final String metaName = entry.getValue().getName();
			final String guid = entry.getKey();
			if (metaName.startsWith(UnityProject.SCRIPTEASE_FILE_PREFIX)) {
				seGeneratedGUIDs.add(guid);
			}
		}

		// Go through the unity resources and determine if they should be
		// removed from our list.
		for (UnityResource object : this.unityResources) {
			if (object.getType() == UnityType.MONOBEHAVIOUR) {
				final Map<String, PropertyValue> propertyMap;
				final PropertyValue scriptMapValue;
				final Map<String, PropertyValue> scriptMap;
				final String guid;

				propertyMap = object.getPropertyMap();
				scriptMapValue = propertyMap.get(UnityField.M_SCRIPT.getName());
				scriptMap = scriptMapValue.getMap();
				guid = scriptMap.get(UnityField.GUID.getName()).getString();

				// We remove ScriptEase generated MonoBehaviours
				if (seGeneratedGUIDs.contains(guid)) {
					objectsToRemove.add(object);
				}

				// Initialize the ScriptEase object
			} else if (object.getType().equals(UnityType.GAMEOBJECT)
					&& object.getName().equals(SCRIPTEASE_OBJECT_NAME)) {
				if (this.scriptEaseObject != null) {
					System.err.println("Found more than one ScriptEase Game "
							+ "Object in Scene " + this
							+ ". Removing previous.");
					objectsToRemove.add(object);
				}
				this.scriptEaseObject = object;
			}
		}

		// Create a ScriptEase object if none exists
		if (this.scriptEaseObject == null) {
			final int gameObjectID = this.getNextEmptyID();
			final int transformID = gameObjectID + 1;

			final UnityResource seGameObject;
			final UnityResource seGameObjectTransform;

			seGameObject = UnityResourceFactory.getInstance()
					.buildEmptyGameObject(transformID, SCRIPTEASE_OBJECT_NAME,
							gameObjectID);
			seGameObjectTransform = UnityResourceFactory.getInstance()
					.buildTransformObject(gameObjectID, transformID);

			this.unityResources.add(seGameObject);
			this.unityResources.add(seGameObjectTransform);

			this.scriptEaseObject = seGameObject;
		}

		// Initialize the owners. Needs to be done after all resources loaded
		for (UnityResource resource : this.unityResources) {
			resource.initializeOwner(this);
		}

		// Likewise for children.
		for (UnityResource resource : this.unityResources) {
			resource.initializeChildren(this, guidsToMetaFiles);
		}

		// Initialize the unity file's visible children resources.
		for (UnityResource resource : this.unityResources) {
			if (resource.getOwner() == this
					&& resource.getType() == UnityType.GAMEOBJECT
					&& resource != this.scriptEaseObject) {
				this.visibleChildren.add(resource);
			}
		}

		// Remove all previous ScriptEase generated script references.
		// We do this after initializing the owners because we have to find the
		// owner of the MonoBehaviour objects.
		for (UnityResource object : objectsToRemove) {
			this.unityResources.remove(object);

			final int objectID;
			final Resource ownerObject;
			final PropertyValue mComponentValue;
			final List<PropertyValue> mComponentList;

			objectID = object.getUniqueID();
			ownerObject = object.getOwner();

			if (ownerObject instanceof UnityResource) {
				mComponentValue = ((UnityResource) ownerObject)
						.getPropertyMap().get(UnityField.M_COMPONENT.getName());
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
						if (key == UnityType.MONOBEHAVIOUR.getID()) {

							final Map<String, PropertyValue> refMap;
							final int fileID;

							refMap = entry.getValue().getMap();
							fileID = Integer.parseInt(refMap.get(
									UnityField.FILEID.getName()).getString());

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
	 * anything to the scene's code or change the model in any other way. Does
	 * not initialize children or owner of the new resource. This is primarily
	 * used to add script objects.
	 * 
	 * @param object
	 */
	public void addResource(UnityResource object) {
		this.unityResources.add(object);
	}

	/**
	 * Removes a UnityResource from the list of resources in the scene. Does not
	 * handle any code changes.
	 * 
	 * @param object
	 */
	public void removeResource(UnityResource object) {
		this.unityResources.remove(object);
	}

	/**
	 * Writes its contents to the file it represents.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		final BufferedWriter writer;
		final String unityFileHeader = "%YAML 1.1\n" + "%TAG !u! "
				+ UnityProject.UNITY_TAG + "\n";

		writer = new BufferedWriter(new FileWriter(location));

		writer.write(unityFileHeader);

		// Add an arbitrary number
		for (Object data : this.unityResources) {
			if (data instanceof UnityResource) {
				final UnityResource resource = (UnityResource) data;

				final String number = "" + resource.getType().getID();

				writer.write("--- !u!" + number + " &" + resource.getUniqueID()
						+ "\n");

				parser.dump(PropertyValue.convertToValueMap(resource
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
		for (UnityResource object : this.unityResources) {
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
		for (UnityResource object : this.unityResources) {
			if (object.getTemplateID().equals(templateID))
				return object;
		}

		return null;
	}

	/**
	 * Closes the streams for Unity file I/O.
	 * 
	 * @throws IOException
	 *             if there is a problem closing either stream.
	 */
	public void close() throws IOException {
		this.reader.close();
	}

	/**
	 * Gets the next available ID number to assign to a unity resource.
	 * 
	 * @return
	 */
	public int getNextEmptyID() {
		int idNumber = 0;
		for (UnityResource object : this.unityResources) {
			final int objectID = object.getUniqueID();
			if (objectID >= idNumber) {
				idNumber = objectID + 1;
			}
		}

		return idNumber;
	}

	public File getLocation() {
		return this.location;
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
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
	public List<Resource> getChildren() {
		// Only returns the visible resources so they can be seen in the
		// game object pane.
		return this.visibleChildren;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnityFile) {
			return this.getTemplateID().equals(
					((UnityFile) obj).getTemplateID());
		}
		return false;
	}

	@Override
	public String toString() {
		return "UnityFile <Location:" + this.location + ", Data:"
				+ this.unityResources.toString() + ">";
	}
}
