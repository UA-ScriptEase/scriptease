package scriptease.controller.io.converter.storycomponent;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.UIManager;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.XMLNode;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only KnowIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * 
 * @see StoryComponentConverter
 */
public class KnowItConverter extends StoryComponentConverter {

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	private static final String TAG_BINDING = "Binding";
	public static final String TAG_KNOWIT = "KnowIt";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final KnowIt knowIt = (KnowIt) source;

		super.marshal(source, writer, context);

		writer.startNode(TypedComponent.TAG_TYPES);
		for (String type : knowIt.getTypes()) {
			writer.startNode(TypedComponent.TAG_TYPE);
			writer.setValue(type);
			writer.endNode();
		}
		writer.endNode();

		// Don't bother recording a null binding
		final KnowItBinding binding = knowIt.getBinding();
		if (!(binding instanceof KnowItBindingNull)) {
			writer.startNode(TAG_BINDING);
			context.convertAnother(binding);
			writer.endNode();
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final KnowIt knowIt;
		final Collection<String> typeKeys = new ArrayList<String>();
		KnowItBinding binding = null;

		knowIt = (KnowIt) super.unmarshal(reader, context);

		// read the legal types list
		reader.moveDown();
		if (!reader.getNodeName().equalsIgnoreCase(TypedComponent.TAG_TYPES))
			System.err.println("Failed to read type list for KnowIt with key "
					+ knowIt.getDisplayText());
		else {
			// read all of the types
			while (reader.hasMoreChildren()) {
				typeKeys.add(XMLNode.TYPE.readString(reader));
			}
		}
		reader.moveUp();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals(TAG_BINDING)) {
				try {

					binding = (KnowItBinding) context.convertAnother(knowIt,
							KnowItBinding.class);
				} catch (ConversionException e) {
					// TODO #55240202 This shouldn't even be happening. Find out
					// what's up and fix it. Consult ticket for more info.
					e.printStackTrace();
					WindowFactory
							.getInstance()
							.showExceptionDialog(
									"Loading Problems",
									"Loading Problem",
									"I had some trouble loading the file.<br>"
											+ "One of the bindings didn't save properly for a slot called \""
											+ knowIt.getDisplayText()
											+ "\".<br>You will need to rebind it."
											+ " Sorry! Please send us a report.",
									UIManager.getIcon("OptionPane.warningIcon"),
									e);
					binding = null;
				}

				// Check if a DescribeIt exists for the binding. If so, map it
				if (binding instanceof KnowItBindingFunction) {
					final ScriptIt bindingScriptIt;

					bindingScriptIt = (ScriptIt) binding.getValue();

					bindingScriptIt.setOwner(knowIt);

					for (CodeBlock block : bindingScriptIt.getCodeBlocks()) {
						final LibraryModel library = block.getLibrary();

						if (library != null) {
							final DescribeIt describeIt;

							describeIt = library
									.findDescribeItWithScriptIt(bindingScriptIt);

							if (describeIt != null) {
								library.addDescribeIt(describeIt, knowIt);
								break;
							}
						}
					}
				}
			} else {
				System.out
						.println("Binding not specified. Defaulting to Null Binding for "
								+ knowIt);
				binding = new KnowItBindingNull();
			}
			reader.moveUp();
		}

		// Set the allowable types of the binding.
		knowIt.setTypes(typeKeys);

		if (binding != null) {
			knowIt.setBinding(binding);
		}
		// Stories are the only ones that care about nulls.
		else if (FileIO.getInstance().getMode() == FileIO.IoMode.STORY)
			System.err.println("No story binding read for " + knowIt);

		return knowIt;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(KnowIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new KnowIt();
	}
}
