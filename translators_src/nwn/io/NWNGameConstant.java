package io;

import java.util.Arrays;
import java.util.Collection;

import scriptease.translator.io.model.Resource;

/**
 * Superclass for all for the NWN implementations of the GameConstant hierarchy.
 * 
 * @author remiller
 */
public abstract class NWNGameConstant extends Resource {
	private final String resRef;
	private final Collection<String> types;
	private final String name;
	private final String tag;

	public NWNGameConstant(String resRef, Collection<String> types,
			String name, String tag) {
		this.resRef = resRef;
		this.types = types;
		this.name = name;
		this.tag = tag;
	}

	public NWNGameConstant(String resRef, String type, String name, String tag) {
		this(resRef, Arrays.asList(type), name, tag);
	}

	/**
	 * Returns the ResRef of the NWNObject.
	 */
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
	public int hashCode() {
		return this.name.hashCode() + this.resRef.hashCode()
				+ this.tag.hashCode() + this.types.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof NWNGameConstant
				&& this.hashCode() == obj.hashCode();
	}

	@Override
	public abstract String toString();

}