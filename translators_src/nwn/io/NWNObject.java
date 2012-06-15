package io;

import java.util.Collection;

import scriptease.translator.io.model.GameObject;

public final class NWNObject implements GameObject {
	private final int objectID;
	private int resolutionMethod;
	private final String resRef; // short for Resource Reference
	private Collection<String> types;
	private final String name; // name and tag are for display purposes
	private final String tag;
	private Collection<GameObject> attributes; // I haven't removed this, since
												// attributes sound important,
												// so we might need them later.

	public static final int SEARCH_TYPE_NEAREST = 0;
	public static final int SEARCH_TYPE_RANDOM = 1;
	public static final int SEARCH_TYPE_NEAREST_INCLUDE_SELF = 2;
	public static final int SEARCH_TYPE_RANDOM_INCLUDE_SELF = 3;

	public NWNObject(String resRef, Collection<String> types, String name,
			String tag) {
		this.resRef = resRef;
		this.types = types;
		this.name = name;
		this.tag = tag;
		this.objectID = 0;
		this.resolutionMethod = NWNObject.SEARCH_TYPE_NEAREST_INCLUDE_SELF;
	}

	@Override
	public int getObjectID() {
		return this.objectID;
	}

	@Override
	public String getResolutionText() {
		String code;

		switch (this.resolutionMethod) {
		case SEARCH_TYPE_NEAREST:
			code = "Get Nearest Object Without Self";
			break;
		case SEARCH_TYPE_RANDOM:
			code = "Get Random Object Without Self";
			break;
		case SEARCH_TYPE_NEAREST_INCLUDE_SELF:
			code = "Get Nearest Object By Tag Include Self";
			break;
		case SEARCH_TYPE_RANDOM_INCLUDE_SELF:
			code = "Get Random Object With Self";
			break;
		default:
			code = "";
		}

		return code;
	}

	@Override
	public void setResolutionMethod(int methodType) {
		this.resolutionMethod = methodType;
	}

	@Override
	public int getResolutionMethod() {
		return this.resolutionMethod;
	}

	@Override
	public String getTemplateID() {
		return this.resRef;
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	public String getName() {
		if (this.name == null || this.name.isEmpty())
			return this.resRef + "*";
		return this.name;
	}

	@Override
	public String getTag() {
		return this.tag;
	}

	@Override
	public String toString() {
		return this.resRef;
	}
}
