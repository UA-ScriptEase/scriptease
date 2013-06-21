package scriptease.controller.io;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Enumeration of all of the XML attributes we can write out. Also includes
 * methods to read and write the attributes.
 * 
 * @author kschenk
 * 
 */
public enum XMLAttribute {
	NAME("name")

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
