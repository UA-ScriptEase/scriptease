package io;

import java.util.Arrays;
import java.util.Collection;

import scriptease.translator.io.model.Resource;

/**
 * Hack-E-Bot game object. Eedit
 * 
 * @author ehill
 */
public class HackEBotResource extends Resource {
	private final String id;
	private final Collection<String> types;
	private final String name;
	private final String tag;

	public HackEBotResource(String id, String type, String name, String tag) {
		this.id = id;
		this.types = Arrays.asList(type);
		this.name = name;
		this.tag = tag;
	}

	/**
	 * Returns the ID of the Hack-E-Bot object.
	 */
	@Override
	public String getTemplateID() {
		return this.id;
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	public String getName() {
		if (this.name == null || this.name.isEmpty())
			return this.id + "*";
		return this.name;
	}
	

	@Override
	public String getTag() {
		return this.tag;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() + this.id.hashCode()
				+ this.tag.hashCode() + this.types.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof HackEBotResource
				&& this.hashCode() == obj.hashCode();
	}

	@Override
	public String toString(){
		return this.id;
	}
	
	@Override
	public String getCodeText() {
		return this.getTag();
	}

}