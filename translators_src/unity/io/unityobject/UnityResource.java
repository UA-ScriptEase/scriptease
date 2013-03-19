package io.unityobject;

import io.Scene;
import io.UnityProject;
import io.UnityConstants.UnityField;
import io.UnityConstants.UnityType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

public class UnityResource extends Resource {
	@SuppressWarnings("serial")
	private static final Collection<String> acceptedTypes = new ArrayList<String>() {
		{
			this.add(UnityType.GAMEOBJECT.getName());
		}
	};

	private final Scene scene;

	private final int uniqueID;
	private final String name;
	private final String tag;

	private Resource owner;
	private List<Resource> children;

	private final Map<String, PropertyValue> topLevelPropertyMap;

	public UnityResource(int uniqueID, String tag, Scene scene,
			Map<String, PropertyValue> propertyMap) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.topLevelPropertyMap = propertyMap;
		this.scene = scene;

		final PropertyValue subMap;

		subMap = this.topLevelPropertyMap.get(UnityType.GAMEOBJECT.getName());

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
	}

	/**
	 * Returns the tag of the object. Tags always start with {@link #UNITY_TAG}.
	 * Tags for UnityObjects are not unique and only serve to define the type.
	 */
	@Override
	public String getTag() {
		return this.getName();
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
		return this.topLevelPropertyMap.get(this.getType()).getMap();
	}

	public String getType() {
		return UnityType.getNameForID(this.getTypeNumber());
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types = new ArrayList<String>();

		types.add(this.getType());

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
	public String getCodeText() {
		return "Find Object with Name";
	}

	public Integer getTypeNumber() {
		return new Integer(this.tag.split(UnityProject.UNITY_TAG)[1]);
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

	public void initializeChildren() {

	}

	@Override
	public List<Resource> getChildren() {
		if (this.children == null) {
			this.children = new ArrayList<Resource>();

			for (UnityResource resource : this.scene.getResources()) {
				final String type = resource.getType();
				final Resource owner = resource.getOwner();
				if (owner == this)
					if (acceptedTypes.contains(type)) {
						this.children.add(resource);
					} else if (type.equals(UnityType.ANIMATION.getName())) {
						this.children.addAll(this
								.getAnimationChildren(resource));
					}
			}
		}
		return this.children;
	}

	private List<Resource> getAnimationChildren(UnityResource resource) {
		final List<Resource> animationChildren = new ArrayList<Resource>();
		final List<PropertyValue> animations;

		animations = resource.getFirstOccuranceOfField(
				UnityField.M_ANIMATIONS.getName()).getList();

		final Map<String, File> guidsToMetaFiles;

		guidsToMetaFiles = UnityProject.getActiveProject()
				.getGUIDsToMetaFiles();

		for (PropertyValue animation : animations) {
			final Map<String, PropertyValue> animationMap;
			final String guid;
			final String fileID;
			final File metaFile;
			final BufferedReader reader;

			animationMap = animation.getMap();
			fileID = animationMap.get(UnityField.FILEID.getName()).getString();
			guid = animationMap.get(UnityField.GUID.getName()).getString();
			metaFile = guidsToMetaFiles.get(guid);
			try {
				reader = new BufferedReader(new FileReader(metaFile));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains(fileID)) {
						final String animationName = line.split(": ")[1];

						if (animationName.contains("@")
								&& animationName.contains("_")) {
							// The string looks like this: d@anim_222-222

							// TODO Maybe we don't need this.. try without
							// first.
							// Get the string after the @
							final String firstPart = animationName.split("@")[1];

						}

						animationChildren.add(SimpleResource
								.buildSimpleResource("AnimationElement",
										animationName));
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

	/**
	 * Gets the unique ID of the owner of the component.
	 * 
	 * @return
	 */
	@Override
	public Resource getOwner() {
		if (this.owner == null) {
			final int uniqueID;

			if (this.getType().equals(UnityType.GAMEOBJECT.getName())) {
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

				attachedTransform = this.scene.getObjectByUnityID(Integer
						.parseInt(transformIDNumber));

				fatherMap = attachedTransform
						.getFirstOccuranceOfField(UnityField.M_FATHER.getName());

				fatherID = Integer.parseInt(fatherMap.getMap()
						.get(UnityField.FILEID.getName()).getString());

				if (fatherID != 0) {
					final UnityResource fatherTransform;
					final PropertyValue mGameObjectMapValue;

					fatherTransform = this.scene.getObjectByUnityID(fatherID);

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
						.getFirstOccuranceOfField(UnityField.M_GAMEOBJECT
								.getName());

				if (gameObjectMapValue != null) {
					uniqueID = Integer.parseInt(gameObjectMapValue.getMap()
							.get(UnityField.FILEID.getName()).getString());
				} else
					uniqueID = -1;
			}

			if (uniqueID != -1)
				this.owner = this.scene.getObjectByUnityID(uniqueID);
			else
				this.owner = this.scene;
		}

		return this.owner;
	}

	@Override
	public String toString() {
		return "UnityResource [" + this.getName() + ", " + this.getType()
				+ ", " + this.getUniqueID() + "]";
	}
}
