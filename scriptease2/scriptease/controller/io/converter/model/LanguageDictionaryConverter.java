package scriptease.controller.io.converter.model;

import java.util.Collection;

import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for the two types of Pattern Model converters.
 * 
 * @author remiller
 */
public class LanguageDictionaryConverter implements Converter {
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final LanguageDictionary languageDictionary = (LanguageDictionary) source;

		XMLAttribute.NAME.write(writer, languageDictionary.getName());

		XMLNode.INDENT_STRING.writeString(writer,
				languageDictionary.getIndent());

		XMLNode.LINE_BREAK.writeString(writer,
				languageDictionary.getLineBreak());

		XMLNode.RESERVED_WORDS.writeChildren(writer,
				languageDictionary.getReservedWords());

		XMLNode.FUNCTION_CALL_FORMAT.writeObject(writer, context,
				languageDictionary.getFunctionCallFormatName());
		
		XMLNode.FORMATS.writeObject(writer, context,
				languageDictionary.getFormats());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String name;
		final String indentString;
		final String lineBreak;
		String functionCallFormat;
		final Collection<String> reservedWords;
		final Collection<FormatDefinitionFragment> fragments;

		final LanguageDictionary languageDictionary;

		System.out.println("Unmarshaling LanguageDictionary");

		name = XMLAttribute.NAME.read(reader);

		indentString = XMLNode.INDENT_STRING.readString(reader);
		lineBreak = XMLNode.LINE_BREAK.readString(reader);
		reservedWords = XMLNode.RESERVED_WORDS.readStringCollection(reader);
		functionCallFormat = XMLNode.FUNCTION_CALL_FORMAT.readString(reader);
		
		fragments = XMLNode.FORMATS.readCollection(reader, context,
				FormatDefinitionFragment.class);

		languageDictionary = new LanguageDictionary(name, indentString,
				lineBreak, functionCallFormat, reservedWords, fragments);

		return languageDictionary;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(LanguageDictionary.class);
	}
}
