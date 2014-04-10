package scriptease.controller.io;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Enumeration of all of the XML attributes we can write out. Also includes
 * methods to read and write the attributes.
 * 
 * @author kschenk
 * @author jyuen
 * 
 */
public enum XMLAttribute {
	AUTHOR("author"),
	
	CONTROL("control"),

	DEFAULT("default"),

	DEFAULT_FORMAT("defaultFormat"),
	
	FLAVOUR("flavour"),
	
	FORMAT("format"),

	NAME("name"),

	DESCRIPTION("description"),

	VALUE("value"),

	ID("id"),

	INITIATE("Initiate"),
	
	READONLY("readonly"),
	
	RESPOND("Respond"),
	
	REF("ref"),
	
	LIBRARY("library"),
	
	PROBABILITY("Probability"),

	DATA("data"),

	UNIQUE("unique"),

	FILTER("filter"),

	FILTERBY("filterBy"),

	SEPARATOR("separator"),
	
	TYPE("type"),
	
	PRIORITY("priority"),

	LEGALVALUES("legalValues");

	private final String name;

	private XMLAttribute(String name) {
		this.name = name;
	}

	public void write(HierarchicalStreamWriter writer, String data) {
		writer.addAttribute(this.name, data);
	}

	public String read(HierarchicalStreamReader reader) {
		return reader.getAttribute(this.name);
	}
}
