package scriptease.controller.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Enumeration of all of the nodes we can write to XML. Also contains various
 * methods to read and write these nodes. This class makes it easier to do some
 * tasks in XML, but we may sometimes have to do things manually.
 * 
 * @author kschenk
 * 
 */
public enum XMLNode {
	SCRIPTIT("ScriptIt"),

	AUDIO("Audio"),

	AUTHOR("Author"),

	CHILDREN("Children"),

	CAUSEIT("CauseIt"),

	CAUSES("Causes", CAUSEIT),

	CODESYMBOL("CodeSymbol"),

	CONDITION("Condition"),

	CONTROLIT("ControlIt"),

	CONTROLITS("ControlIts", CONTROLIT),

	DESCRIPTION("Description"),

	TASK("Task"),

	TASKS("Tasks", TASK),

	BEHAVIOUR("Behaviour"),

	BEHAVIOURS("Behaviours", BEHAVIOUR),

	BINDING("Binding"),

	ACTIVITYIT("ActivityIt"),

	ACTIVITYITS("ActivityIts", ACTIVITYIT),

	CHANCE("Chance"),

	CHOICE("Choice"),

	CHOICES("Choices"),

	CHOICE_COUNTER("ChoiceCounter"),

	COLLABORATIVE_TASK("CollaborativeTask"),

	CODEBLOCKREFERENCE("CodeBlockReference"),

	CODEBLOCKSOURCE("CodeBlockSource"),

	CODE("Code"),

	CODEBLOCKS("CodeBlocks"),

	INDEPENDENT_TASK("IndependentTask"),

	DIALOGUE_LINE("DialogueLine"),

	DIALOGUES("Dialogues", DIALOGUE_LINE),

	EFFECTS("Effects", SCRIPTIT),

	ENABLED("Enabled"),

	ASKIT("AskIt"),

	PICKIT("PickIt"),

	ENUM("Enum"),

	ENTRY("Entry"),

	ESCAPE("Escape"),

	ESCAPES("Escapes", ESCAPE),

	DESCRIBEITNODE("DescribeItNode"),

	DESCRIBEIT("DescribeIt"),

	DESCRIBEITS("DescribeIts", DESCRIBEIT),

	FAN_IN("FanIn"),

	FORMAT("Format"),

	FORMATS("Formats", FORMAT),

	FUNCTION_CALL_FORMAT("FunctionCallFormat"),

	GAME_MODULE("GameModule"),

	GUI("GUI"),

	ID("ID"),

	IMAGE("Image"),

	IMPLICITS("Implicits"),

	INCLUDE("Include"),

	INCLUDES("Includes", INCLUDE),

	INCLUDE_FILE("IncludeFile"),

	INCLUDE_FILES("IncludeFiles", INCLUDE_FILE),

	INDENT_STRING("IndentString"),

	LINE_BREAK("LineBreak"),

	KEYWORD("Keyword"),

	KNOWIT("KnowIt"),

	LABEL("Label"),

	LABELS("Labels", LABEL),

	LEGAL_VALUES("LegalValues"),

	NAME("Name"),

	NOTE("Note"),

	OPTIONAL_LIBRARY("OptionalLibrary"),

	OPTIONAL_LIBRARIES("OptionalLibraries", OPTIONAL_LIBRARY),

	PATH("Path", DESCRIBEITNODE),

	PATHMAP("PathMap"),

	PARAMETERS("Parameters"),

	READONLY("ReadOnly"),

	RESERVED_WORD("Word"),

	RESERVED_WORDS("ReservedWords", RESERVED_WORD),

	SLOT("Slot"),

	SLOTS("Slots", SLOT),

	START_TASK("StartTask"),

	START_STORY_POINT("StartStoryPoint"),

	START_NODE("StartNode"),

	STORY_COMPONENT_CONTAINER("StoryComponentContainer"),

	EXIT_NODE("ExitNode"),

	EXPANDED("Expanded"),

	STORY_GROUP("StoryGroup"),

	STORY_POINT("StoryPoint"),

	SUBJECT("Subject"),

	SUCCESSORS("Successors"),

	TARGET_ID("TargetId"),

	TITLE("Title"),

	TRANSLATOR("Translator"),

	TYPE("Type"),

	TYPES("Types", TYPE),

	TYPECONVERTERS("TypeConverters", SCRIPTIT),

	VERSION("Version"),

	VALUE("Value"),

	VISIBLE("Visible"),

	WIDGETNAME("WidgetName"),

	;

	private final String name;
	private final XMLNode child;

	private XMLNode(String name) {
		this(name, null);
	}

	private XMLNode(String name, XMLNode child) {
		this.name = name;
		this.child = child;
	}

	/**
	 * Writes a node with String data to the passed in writer.
	 * 
	 * @param writer
	 * @param nodeName
	 * @param data
	 */
	public void writeString(HierarchicalStreamWriter writer, String data) {
		writer.startNode(this.name);
		if (data != null)
			writer.setValue(data);
		writer.endNode();
	}

	/**
	 * Writes an integer as a string to the passed in writer.
	 * 
	 * @param writer
	 * @param bool
	 */
	public void writeInteger(HierarchicalStreamWriter writer, int integer) {
		this.writeString(writer, Integer.toString(integer));
	}

	/**
	 * Writes a boolean as a string to the passed in writer.
	 * 
	 * @param writer
	 * @param bool
	 */
	public void writeBoolean(HierarchicalStreamWriter writer, boolean bool) {
		this.writeString(writer, Boolean.toString(bool));
	}

	/**
	 * Writes a node and it's object data converted into XML to the passed in
	 * writer. A Context is necessary to convert the object.
	 * 
	 * @param write
	 * @param context
	 * @param object
	 */
	public void writeObject(HierarchicalStreamWriter writer,
			MarshallingContext context, Object object) {
		writer.startNode(this.name);
		context.convertAnother(object);
		writer.endNode();
	}

	/**
	 * Writes a node that also has an attribute attached to it.
	 * 
	 * 
	 * @param writer
	 * @param context
	 * @param object
	 * @param attribute
	 * @param attributeData
	 */
	public void writeObject(HierarchicalStreamWriter writer,
			MarshallingContext context, Object object, XMLAttribute attribute,
			String attributeData) {
		writer.startNode(this.name);
		attribute.write(writer, attributeData);
		context.convertAnother(object);
		writer.endNode();
	}

	/**
	 * Writes a collection of data, with each piece of data written to a child
	 * node.<br>
	 * <br>
	 * In XML, this will have this structure:
	 * 
	 * <code>
	 * <pre>&lt;ThisNodeName&gt;
	 *     &lt;ChildNodeName&gt;
	 *         &lt;ChildNodeData&gt;
	 *     &lt;ChildNodeName&gt;
	 *         &lt;ChildNodeData&gt;</pre></pre>
	 * </code>
	 * 
	 * @param writer
	 * @param data
	 */
	public void writeChildren(HierarchicalStreamWriter writer,
			Collection<String> data) {
		writer.startNode(this.name);

		this.checkChild();

		for (String string : data) {
			this.child.writeString(writer, string);
		}

		writer.endNode();
	}

	/**
	 * Reads a simple text value from the reader, provided that the XML element
	 * that we're reading from actually has the correct tag (ignoring case).
	 * 
	 * @param reader
	 *            the reader to read from.
	 * @return the value read in from the reader.
	 */
	public String readString(HierarchicalStreamReader reader) {
		final String value;

		reader.moveDown();
		this.checkNodeName(reader);

		value = reader.getValue();
		reader.moveUp();

		return value;
	}

	/**
	 * Reads an object converted by the context using the parent. The cool thing
	 * about using generics like in this method is that it will always return
	 * the type passed into the Class c argument.
	 * 
	 * @param reader
	 * @param context
	 * @param current
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> E readObject(HierarchicalStreamReader reader,
			UnmarshallingContext context, Class<E> c) {
		final E e;

		reader.moveDown();

		this.checkNodeName(reader);

		e = (E) context.convertAnother(null, c);

		reader.moveUp();

		return e;
	}

	/**
	 * Reads a simple collection of text values from the reader.
	 * 
	 * @param reader
	 *            the reader to read from.
	 */
	public Collection<String> readStringCollection(
			HierarchicalStreamReader reader) {
		final Collection<String> values = new ArrayList<String>();

		reader.moveDown();

		this.checkNodeName(reader);
		this.checkChild();

		while (reader.hasMoreChildren()) {
			reader.moveDown();

			this.child.checkNodeName(reader);

			values.add(reader.getValue());

			reader.moveUp();
		}

		reader.moveUp();

		return values;
	}

	/**
	 * Reads the children into a list of types of the passed in class. Reads
	 * children as the node's default child.
	 * 
	 * @param reader
	 * @param context
	 * @param parent
	 * @param c
	 * @return
	 */
	public <E> Collection<E> readCollection(HierarchicalStreamReader reader,
			UnmarshallingContext context, Class<E> c) {
		this.checkChild();
		return this.readCollection(this.child, reader, context, c);
	}

	/**
	 * Reads the children into a list of types of the passed in class.
	 * 
	 * @param childNode
	 * @param reader
	 * @param context
	 * @param parent
	 * @param c
	 * @return
	 */
	public <E> Collection<E> readCollection(XMLNode childNode,
			HierarchicalStreamReader reader, UnmarshallingContext context,
			Class<E> c) {
		final Collection<E> collection = new ArrayList<E>();

		reader.moveDown();

		this.checkNodeName(reader);

		while (reader.hasMoreChildren()) {
			collection.add(childNode.readObject(reader, context, c));
		}
		reader.moveUp();

		return collection;
	}

	/**
	 * Reads a collection that has attributes attached to it.
	 * 
	 * @param reader
	 * @param context
	 * @param c
	 * @param attributes
	 * @return
	 */
	public <E> XMLNodeData<Collection<E>> readAttributedCollection(
			HierarchicalStreamReader reader, UnmarshallingContext context,
			Class<E> c, XMLAttribute... attributes) {
		final Collection<E> collection = new ArrayList<E>();
		final Map<XMLAttribute, String> attributeMap;

		attributeMap = new HashMap<XMLAttribute, String>();

		reader.moveDown();

		this.checkChild();
		this.checkNodeName(reader);

		for (XMLAttribute attribute : attributes) {
			attributeMap.put(attribute, attribute.read(reader));
		}

		while (reader.hasMoreChildren()) {
			collection.add(this.child.readObject(reader, context, c));
		}
		reader.moveUp();

		return new XMLNodeData<Collection<E>>(attributeMap, collection);
	}

	/**
	 * Returns the XML name of the node.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Checks that the current node name is the same as the name of this XML
	 * node.
	 * 
	 * @param reader
	 */
	private void checkNodeName(HierarchicalStreamReader reader) {
		final String tag = reader.getNodeName();
		if (!tag.equalsIgnoreCase(this.name))
			throw new ConversionException(
					"XML element was not as expected. Expected " + this.name
							+ ", but received " + tag);
	}

	/**
	 * Checks if the child is null, and if so, throws an exception.
	 */
	private void checkChild() {
		if (this.child == null) {
			throw new NullPointerException("Null Child Node found for XMLNode "
					+ this);
		}
	}

	/**
	 * Returns a bundle of data for the node. This is used for nodes that have
	 * attributes, but contain child nodes.
	 * 
	 * @author kschenk
	 * 
	 * @param <Z>
	 */
	public class XMLNodeData<Z> {
		private final Map<XMLAttribute, String> attributes;
		private final Z data;

		public XMLNodeData(Map<XMLAttribute, String> attributes, Z data) {
			this.attributes = attributes;
			this.data = data;
		}

		public Map<XMLAttribute, String> getAttributes() {
			return attributes;
		}

		public String getAttribute(XMLAttribute attribute) {
			return this.attributes.get(attribute);
		}

		public Z getData() {
			return data;
		}
	}
}
