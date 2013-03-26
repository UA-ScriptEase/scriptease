package io;

import java.util.Arrays;
import java.util.Collection;

/**
 * Neverwinter Nights implementation of {@link Resource}.
 * 
 * @author remiller
 */
public final class NWNObject extends NWNGameConstant {
	public NWNObject(String resRef, String type, String name, String tag) {
		this(resRef, Arrays.asList(type), name, tag);
	}

	public NWNObject(String resRef, Collection<String> types, String name,
			String tag) {
		super(resRef, types, name, tag);
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
