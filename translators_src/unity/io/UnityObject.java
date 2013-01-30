package io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.translator.io.model.GameObject;

public class UnityObject implements GameObject {
	public static final String UNITY_TAG = "tag:unity3d.com,2011:";

	private final int uniqueID;
	private final String tag;
	private final Map<String, Object> propertyMap;

	public UnityObject(int uniqueID, String tag) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.propertyMap = new HashMap<String, Object>();
	}

	public void setProperties(Map<String, Object> map) {
		this.propertyMap.clear();
		this.propertyMap.putAll(map);
	}

	/**
	 * Returns the tag of the object. Tags always start with {@link #UNITY_TAG}.
	 * Tags for UnityObjects are not unique and only serve to define the type.
	 */
	@Override
	public String getTag() {
		return this.tag;
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
	 * Returns the map of various properties of a unity object.
	 * 
	 * @return
	 */
	public Map<String, Object> getPropertyMap() {
		return this.propertyMap;
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types = new ArrayList<String>();

		types.add(UnityTranslatorConstants.TYPE_MAP.get(this.getTypeNumber()));

		return types;
	}

	@Override
	public String getName() {
		// TODO This got accessed 120 times on load for ONE unique object. This
		// may be why we take forever to load large files.
		final Object subMap = this.propertyMap.get("GameObject");
		if (subMap instanceof Map) {
			final Object mName = ((Map<?, ?>) subMap).get("m_Name");
			if (mName instanceof String && !((String) mName).isEmpty())
				return (String) mName;
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
		return this.getName();
	}

	public int getTypeNumber() {
		return Integer.parseInt(this.tag.split(UNITY_TAG)[1]);
	}

	@Override
	public void setResolutionMethod(int methodType) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getResolutionMethod() {
		// TODO Auto-generated method stub
		return 0;
	}
}
