package scriptease.controller.io.converter.storycomponent;

import java.util.Collection;

import scriptease.controller.io.FileIO;
import scriptease.controller.io.XMLNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;

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
	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final KnowIt knowIt = (KnowIt) source;
		final KnowItBinding binding = knowIt.getBinding();

		super.marshal(source, writer, context);

		XMLNode.TYPES.writeChildren(writer, knowIt.getTypes());

		// Don't bother recording a null binding
		if (!(binding instanceof KnowItBindingNull)) {
			XMLNode.BINDING.writeObject(writer, context, binding);
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final KnowIt knowIt;
		final Collection<String> typeNames;

		KnowItBinding binding;

		knowIt = (KnowIt) super.unmarshal(reader, context);
		typeNames = XMLNode.TYPES.readStringCollection(reader);

		if (reader.hasMoreChildren()) {
			try {
				binding = XMLNode.BINDING.readObject(reader, context,
						KnowItBinding.class);
			} catch (Exception e) {
				System.err.println("Binding read error for " + knowIt);
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
			} else if (binding instanceof KnowItBindingAutomatic)
				((KnowItBindingAutomatic) binding).setOwner(knowIt);
		} else
			binding = null;

		// Set the allowable types of the binding.
		knowIt.setTypesByName(typeNames);

		if (binding != null && knowIt.getLibrary() != null) {
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
			UnmarshallingContext context, LibraryModel library) {
		return new KnowIt(library);
	}
}
