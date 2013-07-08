package io;

import scriptease.translator.io.model.Resource;

/**
 * Neverwinter Nights implementation of {@link Resource}.
 * 
 * @author remiller
 */
public final class NWNObject extends NWNGameConstant {
	public NWNObject(String resRef, String type, String name, String tag) {
		super(resRef, type, name, tag);
	}

	@Override
	public String getCodeText() {
		return "SE_AUX_GetNearestObjectByTagIncludeSelf(\"" + this.getTag()
				+ "\")";
	}

	@Override
	public String toString() {
		return this.getTemplateID();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NWNObject)
			return this.hashCode() == obj.hashCode();

		return false;
	}
}
