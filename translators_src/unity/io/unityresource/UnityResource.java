package io.unityresource;

import io.Scene;
import io.UnityProject;
import io.unityconstants.UnityField;
import io.unityconstants.UnityType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

public class UnityResource extends Resource {
	private final UnityType type;
	private final int uniqueID;
	private final String name;
	private final String tag;

	private Resource owner;
	private List<Resource> children;

	private final Map<String, PropertyValue> topLevelPropertyMap;

	/**
	 * Creates a UnityResource with the Unique ID, tag, and PropertyMap.
	 * UnityResources should generally only be made in the
	 * {@link UnityResourceFactory}.
	 * 
	 * @param uniqueID
	 * @param tag
	 * @param propertyMap
	 */
	protected UnityResource(int uniqueID, String tag,
			Map<String, PropertyValue> propertyMap) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.topLevelPropertyMap = propertyMap;

		final PropertyValue subMap;
		final int typeID;

		subMap = this.topLevelPropertyMap.get(UnityType.GAMEOBJECT.getName());
		typeID = new Integer(this.tag.split(UnityProject.UNITY_TAG)[1]);

		if (subMap != null && subMap.isMap()) {
			final PropertyValue mName;

			mName = subMap.getMap().get(UnityField.M_NAME.getName());

			final String mNameValueString = mName.getString();
			if (mNameValueString != null && !mNameValueString.isEmpty())
				this.name = mNameValueString;
			else
				this.name = UnityType.GAMEOBJECT.getName();
		} else {
			this.name = (String) this.topLevelPropertyMap.keySet().toArray()[0];
		}

		this.type = UnityType.getTypeForID(typeID);
	}

	/**
	 * The unique identifier for the object. In YAML, it looks like "&#####".
	 * 
	 * @return
	 */
	public int getUniqueID() {
		return this.uniqueID;
	}

	/**
	 * Returns the map of various properties of a unity object. This always
	 * starts with just one value that has the name of the type as the key and
	 * the actual properties as a map in it's value. If you know that the object
	 * has a map of other properties, use {@link #getPropertyMap()}.
	 * 
	 * @return
	 */
	public Map<String, PropertyValue> getTopLevelPropertyMap() {
		return this.topLevelPropertyMap;
	}

	/**
	 * Returns the map of various properties of a unity object. This is not the
	 * top level map, which would be accessed via
	 * {@link #getTopLevelPropertyMap()}.
	 * 
	 * @return
	 */
	public Map<String, PropertyValue> getPropertyMap() {
		return this.topLevelPropertyMap.get(this.getType().getName()).getMap();
	}

	public UnityType getType() {
		return this.type;
	}

	/**
	 * Initializes the owner of the UnityResource. Must be called after loading
	 * the entire scene in order to detect all children.
	 */
	public void initializeOwner(Scene scene) {
		final int uniqueID;

		if (this.getType() == UnityType.GAMEOBJECT) {
			// This is the ID of the Transform object.
			final int transformTypeNumber;
			final PropertyValue transformIDValue;
			final String transformIDNumber;
			final UnityResource attachedTransform;
			final PropertyValue fatherMap;
			final int fatherID;

			transformTypeNumber = UnityType.TRANSFORM.getID();
			transformIDValue = this.getFirstOccuranceOfField(String
					.valueOf(transformTypeNumber));
			transformIDNumber = transformIDValue.getMap()
					.get(UnityField.FILEID.getName()).getString();

			attachedTransform = scene.getObjectByUnityID(Integer
					.parseInt(transformIDNumber));

			fatherMap = attachedTransform
					.getFirstOccuranceOfField(UnityField.M_FATHER.getName());

			fatherID = Integer.parseInt(fatherMap.getMap()
					.get(UnityField.FILEID.getName()).getString());

			if (fatherID != 0) {
				final UnityResource fatherTransform;
				final PropertyValue mGameObjectMapValue;

				fatherTransform = scene.getObjectByUnityID(fatherID);

				mGameObjectMapValue = fatherTransform
						.getFirstOccuranceOfField(UnityField.M_GAMEOBJECT
								.getName());

				uniqueID = Integer.parseInt(mGameObjectMapValue.getMap()
						.get(UnityField.FILEID.getName()).getString());
			} else
				uniqueID = -1;
		} else {
			final PropertyValue gameObjectMapValue;

			gameObjectMapValue = this
					.getFirstOccuranceOfField(UnityField.M_GAMEOBJECT.getName());

			if (gameObjectMapValue != null) {
				uniqueID = Integer.parseInt(gameObjectMapValue.getMap()
						.get(UnityField.FILEID.getName()).getString());
			} else
				uniqueID = -1;
		}

		if (uniqueID != -1)
			this.owner = scene.getObjectByUnityID(uniqueID);
		else
			this.owner = scene;
	}

	/**
	 * Initializes the children of the resource. Must be called after all
	 * resources have their owners initialized. Sorry.
	 * 
	 * @param scene
	 */
	public void initializeChildren(Scene scene, Map<String, File> guidsToMetas) {
		this.children = new ArrayList<Resource>();

		for (UnityResource resource : scene.getResources()) {
			final UnityType type = resource.getType();
			final Resource owner = resource.getOwner();
			if (owner == this)
				if (type == UnityType.GAMEOBJECT) {
					this.children.add(resource);
				} else if (type == UnityType.ANIMATION) {
					this.children.addAll(this.getAnimationChildren(resource,
							guidsToMetas));
				}
		}
	}

	private List<Resource> getAnimationChildren(UnityResource resource,
			Map<String, File> guidsToMetas) {
		final List<Resource> animationChildren = new ArrayList<Resource>();
		final List<PropertyValue> animations;

		animations = resource.getFirstOccuranceOfField(
				UnityField.M_ANIMATIONS.getName()).getList();

		for (PropertyValue animation : animations) {
			final Map<String, PropertyValue> animationMap;
			final String guid;
			final String fileID;
			final File metaFile;
			final BufferedReader reader;

			animationMap = animation.getMap();
			fileID = animationMap.get(UnityField.FILEID.getName()).getString();
			guid = animationMap.get(UnityField.GUID.getName()).getString();
			metaFile = guidsToMetas.get(guid);
			try {
				reader = new BufferedReader(new FileReader(metaFile));
				String line;
				final String animType = "AnimationElement";
				final String nameStart = "@";
				final String nameEnd = "_";

				while ((line = reader.readLine()) != null) {
					if (line.contains(fileID)) {
						final String originalName = line.split(": ")[1];

						final Resource animationChild;
						// TODO Change this into a regex.

						if (originalName.contains(nameStart)) {
							// Get the string after the @. It's now anim_222-222
							final String splitName = originalName
									.split(nameStart)[1];

							// The string looks like this: d@anim_222-222
							if (splitName.contains(nameEnd)) {
								// Get the string before the _. It's now just
								// anims
								final String name = splitName.split(nameEnd)[0];

								animationChild = SimpleResource
										.buildSimpleResource(animType, name);
							} else
								animationChild = SimpleResource
										.buildSimpleResource(
												"AnimationElement",
												originalName);
						}

						else
							animationChild = SimpleResource
									.buildSimpleResource("AnimationElement",
											originalName);

						animationChildren.add(animationChild);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return animationChildren;
	}

	/**
	 * Gets the value of the first occurrence of the passed in field name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public PropertyValue getFirstOccuranceOfField(String fieldName) {
		return UnityResource.getFirstOccuranceOfFieldInMap(
				this.topLevelPropertyMap, fieldName);
	}

	/**
	 * Gets the value of the first occurrence of the passed in field name.
	 * 
	 * @param map
	 * @param fieldName
	 * @return
	 */
	private static PropertyValue getFirstOccuranceOfFieldInMap(
			Map<String, PropertyValue> map, String fieldName) {
		for (Entry<String, PropertyValue> entry : map.entrySet()) {
			final PropertyValue value = entry.getValue();

			if (entry.getKey().equals(fieldName))
				return entry.getValue();
			else if (value.isList()) {
				final PropertyValue returnValue;

				returnValue = UnityResource.getFirstOccuranceOfFieldInList(
						value.getList(), fieldName);

				if (returnValue != null)
					return returnValue;
			} else if (value.isMap()) {
				final PropertyValue returnValue;

				returnValue = UnityResource.getFirstOccuranceOfFieldInMap(
						value.getMap(), fieldName);

				if (returnValue != null)
					return returnValue;
			}
		}

		return null;
	}

	/**
	 * Gets the value of the first occurrence of the passed in field name.
	 * 
	 * @param list
	 * @param fieldName
	 * @return
	 */
	private static PropertyValue getFirstOccuranceOfFieldInList(
			List<PropertyValue> list, String fieldName) {
		for (PropertyValue value : list) {
			if (value.isMap()) {
				final PropertyValue returnValue;

				returnValue = UnityResource.getFirstOccuranceOfFieldInMap(
						value.getMap(), fieldName);

				if (returnValue != null)
					return returnValue;
			} else if (value.isList()) {
				final PropertyValue returnValue;

				returnValue = UnityResource.getFirstOccuranceOfFieldInList(
						value.getList(), fieldName);

				if (returnValue != null)
					return returnValue;
			}
		}

		return null;
	}

	@Override
	public String getCodeText() {
		String name = this.name;
		Resource owner = this.owner;

		while (!(owner instanceof Scene)) {
			if (owner.getTypes().contains(UnityType.GAMEOBJECT.getName())) {
				name = owner.getName() + "/" + name;
			}
			owner = owner.getOwner();
		}

		return "GameObject.Find(\"" + name + "\")";
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types = new ArrayList<String>();

		types.add(this.type.getName());

		return types;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * This combines the tag and uniqueID to provide the strongest
	 * representation of the object as a String.
	 */
	@Override
	public String getTemplateID() {
		return this.tag + " &" + this.uniqueID;
	}

	@Override
	public Resource getOwner() {
		return this.owner;
	}

	@Override
	public List<Resource> getChildren() {
		return this.children;
	}

	/**
	 * Returns the tag of the object. Tags always start with {@link #UNITY_TAG}.
	 * Tags for UnityObjects are not unique and only serve to define the type.
	 */
	@Override
	public String getTag() {
		return this.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnityResource) {
			final UnityResource other = (UnityResource) obj;

			return this.topLevelPropertyMap.equals(other.topLevelPropertyMap)
					&& this.tag.equals(other.tag)
					&& this.uniqueID == other.uniqueID;
		}
		return false;
	}

	@Override
	public String toString() {
		return "UnityResource [" + this.getName() + ", " + this.getType()
				+ ", " + this.getUniqueID() + "]";
	}
}
