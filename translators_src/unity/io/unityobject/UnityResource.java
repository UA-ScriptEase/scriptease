package io.unityobject;

import io.Scene;
import io.UnityConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.translator.io.model.Resource;

public class UnityResource extends Resource {
	private final int uniqueID;
	private final String tag;
	private final Map<String, PropertyValue> topLevelPropertyMap;
	private final Scene scene;

	public UnityResource(int uniqueID, String tag, Scene scene) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.topLevelPropertyMap = new HashMap<String, PropertyValue>();
		this.scene = scene;
	}

	public void setProperties(Map<String, PropertyValue> map) {
		this.topLevelPropertyMap.clear();
		this.topLevelPropertyMap.putAll(map);
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
		return UnityConstants.TYPE_LIST.get(this.getTypeNumber());
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types = new ArrayList<String>();

		types.add(this.getType());

		return types;
	}

	@Override
	public String getName() {
		final PropertyValue subMap = this.topLevelPropertyMap.get("GameObject");
		if (subMap != null && subMap.isMap()) {
			final PropertyValue mName;

			mName = subMap.getMap().get("m_Name");

			if (mName.isString()) {
				final String mNameValueString = mName.getString();
				if (!mNameValueString.isEmpty())
					return mNameValueString;
			}
		}

		for (String key : this.topLevelPropertyMap.keySet())
			return key;
		return "";
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
		return new Integer(this.tag.split(UnityConstants.UNITY_TAG)[1]);
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
	public List<Resource> getChildren() {
		final List<Resource> children = new ArrayList<Resource>();

		for (UnityResource resource : this.scene.getResources()) {
			final UnityResource owner = resource.getOwner();
			if (owner != null && owner.getUniqueID() == this.getUniqueID())
				children.add(resource);
		}

		return children;
	}

	/**
	 * Gets the value of the first occurrence of the passed in field name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public PropertyValue getFirstOccuranceOfField(String fieldName) {
		return this.getFirstOccuranceOfFieldInMap(this.topLevelPropertyMap,
				fieldName);
	}

	private PropertyValue getFirstOccuranceOfFieldInMap(
			Map<String, PropertyValue> map, String fieldName) {
		for (Entry<String, PropertyValue> entry : map.entrySet()) {
			final PropertyValue value = entry.getValue();

			if (entry.getKey().equals(fieldName))
				return entry.getValue();
			else if (value.isList()) {
				final PropertyValue returnValue;

				returnValue = this.getFirstOccuranceOfFieldInList(
						value.getList(), fieldName);

				if (returnValue != null)
					return returnValue;
			} else if (value.isMap()) {
				final PropertyValue returnValue;

				returnValue = this.getFirstOccuranceOfFieldInMap(
						value.getMap(), fieldName);

				if (returnValue != null)
					return returnValue;
			}
		}

		return null;
	}

	private PropertyValue getFirstOccuranceOfFieldInList(
			List<PropertyValue> list, String fieldName) {
		for (PropertyValue value : list) {
			if (value.isMap()) {
				final PropertyValue returnValue;

				returnValue = this.getFirstOccuranceOfFieldInMap(
						value.getMap(), fieldName);

				if (returnValue != null)
					return returnValue;
			} else if (value.isList()) {
				final PropertyValue returnValue;

				returnValue = this.getFirstOccuranceOfFieldInList(
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
	public UnityResource getOwner() {
		final int uniqueID;

		if (this.getType().equals(UnityConstants.TYPE_GAMEOBJECT)) {
			// This is the ID of the Transform object.
			final int transformTypeNumber;
			final PropertyValue transformIDValue;
			final String transformIDNumber;

			transformTypeNumber = UnityConstants.TYPE_LIST
					.indexOf(UnityConstants.TYPE_TRANSFORM);
			transformIDValue = this.getFirstOccuranceOfField(String
					.valueOf(transformTypeNumber));
			transformIDNumber = transformIDValue.getMap()
					.get(UnityConstants.FIELD_FILEID).getString();

			final UnityResource attachedTransform;

			attachedTransform = this.scene.getObjectByUnityID(Integer
					.parseInt(transformIDNumber));

			final PropertyValue fatherMap = attachedTransform
					.getFirstOccuranceOfField(UnityConstants.FIELD_M_FATHER);

			final int fatherID = Integer.parseInt(fatherMap.getMap()
					.get(UnityConstants.FIELD_FILEID).getString());

			if (fatherID != 0) {
				final UnityResource fatherTransform;
				final PropertyValue mGameObjectMapValue;

				fatherTransform = this.scene.getObjectByUnityID(fatherID);

				mGameObjectMapValue = fatherTransform
						.getFirstOccuranceOfField(UnityConstants.FIELD_M_GAMEOBJECT);

				uniqueID = Integer.parseInt(mGameObjectMapValue.getMap()
						.get(UnityConstants.FIELD_FILEID).getString());
			} else
				uniqueID = -1;
		} else {
			final PropertyValue gameObjectMapValue;

			gameObjectMapValue = this
					.getFirstOccuranceOfField(UnityConstants.FIELD_M_GAMEOBJECT);

			if (gameObjectMapValue != null) {
				uniqueID = Integer.parseInt(gameObjectMapValue.getMap()
						.get(UnityConstants.FIELD_FILEID).getString());
			} else
				uniqueID = -1;
		}

		return this.scene.getObjectByUnityID(uniqueID);
	}
}
