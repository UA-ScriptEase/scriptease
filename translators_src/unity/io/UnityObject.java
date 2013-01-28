package io;

import java.util.HashMap;
import java.util.Map;

public class UnityObject {
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

	public String getTag() {
		return tag;
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}
}
