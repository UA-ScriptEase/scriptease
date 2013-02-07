package io.unityobject;

import io.PropertyValue;
import io.UnityTranslatorConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.translator.io.model.Resource;

public class UnityResource extends Resource {
	public static final String UNITY_TAG = "tag:unity3d.com,2011:";

	private final int uniqueID;
	private final String tag;
	private final Map<String, PropertyValue> propertyMap;

	public UnityResource(int uniqueID, String tag) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.propertyMap = new HashMap<String, PropertyValue>();
	}

	public void setProperties(Map<String, PropertyValue> map) {
		this.propertyMap.clear();
		this.propertyMap.putAll(map);
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
		return this.propertyMap;
	}

	/**
	 * Returns the map of various properties of a unity object.
	 * 
	 * @return
	 */
	public Map<String, PropertyValue> getPropertyMap() {
		final Integer typeNumber;
		final String type;
		final PropertyValue objectMapValue;

		typeNumber = this.getTypeNumber();
		type = UnityTranslatorConstants.TYPE_LIST.get(typeNumber);
		objectMapValue = this.propertyMap.get(type);

		return objectMapValue.getMap();
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types;
		final Integer typeNumber;
		final String type;

		types = new ArrayList<String>();
		typeNumber = this.getTypeNumber();
		type = UnityTranslatorConstants.TYPE_LIST.get(typeNumber);

		types.add(type);

		return types;
	}

	@Override
	public String getName() {
		// TODO This got accessed 120 times on load for ONE unique object. This
		// may be why we take forever to load large files... Investigate
		final PropertyValue subMap = this.propertyMap.get("GameObject");
		if (subMap != null && subMap.isMap()) {
			final PropertyValue mName;

			mName = subMap.getMap().get("m_Name");

			if (mName.isString()) {
				final String mNameValueString = mName.getString();
				if (!mNameValueString.isEmpty())
					return mNameValueString;
			}
		}

		for (String key : this.propertyMap.keySet())
			return key;
		return "";
	}

	@Override
	public String getTemplateID() {
		return this.tag + " &" + this.uniqueID;
	}

	@Override
	public String getCodeText() {
		return "Find Object with Name";
	}

	public Integer getTypeNumber() {
		return new Integer(this.tag.split(UNITY_TAG)[1]);
	}

	@Override
	public boolean equals(Object obj) {
		// FIXME
		// XXX
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Resource> getChildren() {
		// FIXME
		// XXX
		// TODO Auto-generated method stub
		return super.getChildren();
	}

	@Override
	public boolean isLink() {
		// FIXME
		// XXX
		// TODO Auto-generated method stub
		return super.isLink();
	}

	@Override
	public String getOwnerName() {
		// FIXME
		// XXX
		// TODO Auto-generated method stub
		return super.getOwnerName();
	}
}
