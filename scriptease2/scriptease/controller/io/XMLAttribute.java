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

	DEFAULT_FORMAT("defaultFormat"),
	
	NAME("name"),

	DESCRIPTION("description")
	;
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
