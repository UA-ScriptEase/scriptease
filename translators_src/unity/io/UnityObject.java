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

	@Override
	public String getTag() {
		return this.tag;
	}

	public int getUniqueID() {
		return this.uniqueID;
	}

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
		// TODO We should actually return the value of "m_name" if it's a top
		// level Game Object.
		for (String key : this.propertyMap.keySet())
			return key;
		return "";
	}

	@Override
	public String getTemplateID() {
		return this.tag;
	}

	@Override
	public String getCodeText() {
		return this.getName();
	}

	public int getTypeNumber() {
		return Integer.parseInt(this.tag.split(":")[2]);
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
