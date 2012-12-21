package io;

import java.util.Arrays;
import java.util.Collection;

import scriptease.translator.io.model.GameObject;

/**
 * Neverwinter Nights implementation of {@link GameObject}.
 * 
 * @author remiller
 */
public final class NWNObject extends NWNGameConstant implements GameObject {
	private int resolutionMethod;
	public static final int SEARCH_TYPE_NEAREST = 0;
	public static final int SEARCH_TYPE_RANDOM = 1;
	public static final int SEARCH_TYPE_NEAREST_INCLUDE_SELF = 2;
	public static final int SEARCH_TYPE_RANDOM_INCLUDE_SELF = 3;

	public NWNObject(String resRef, String type, String name, String tag) {
		this(resRef, Arrays.asList(type), name, tag);
	}

	public NWNObject(String resRef, Collection<String> types, String name,
			String tag) {
		super(resRef, types, name, tag);
		this.resolutionMethod = NWNObject.SEARCH_TYPE_NEAREST_INCLUDE_SELF;
	}

	@Override
	public String getCodeText() {
		String code = "DefaultResolutionText";

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
	public String toString() {
		return this.getTemplateID();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NWNObject))
			return false;

		final NWNObject other = (NWNObject) obj;
		boolean equals = true;

		equals &= this.getName().equals(other.getName());
		equals &= this.getTag().equals(other.getTag());
		equals &= this.getTemplateID().equals(other.getTemplateID());
		equals &= this.getTypes().equals(other.getTypes());

		return equals;
	}
}
