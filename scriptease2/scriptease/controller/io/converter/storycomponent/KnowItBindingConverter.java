package scriptease.controller.io.converter.storycomponent;

import java.util.Arrays;

import scriptease.controller.BindingVisitor;
import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.model.StoryModelConverter;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryGroup;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.ListOp;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts any KnowItBinding to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * @author jyuen
 */
public class KnowItBindingConverter implements Converter {
	private static final String FLAVOUR_CONSTANT = "constant";
	private static final String FLAVOUR_RESOURCE = "resource";
	private static final String FLAVOUR_FUNCTION = "function";
	private static final String FLAVOUR_REFERENCE = "reference";
	private static final String FLAVOUR_UNINITIALIZED = "uninitialized";
	private static final String FLAVOUR_NULL = "null";
	private static final String FLAVOUR_AUTOMATIC = "automatic";
	private static final String FLAVOUR_STORY_POINT = "storyPoint";
	private static final String FLAVOUR_STORY_GROUP = "storyGroup";

	/**
	 * Can convert any subclass of KnowItBinding
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		/*
		 * This is intentionally ignoring the possibility that a KnowItBinding
		 * subclass could indirectly implement KnowItBinding. Doing it this way
		 * avoids running up the inheritance hierarchy of every single class we
		 * want to convert, thus giving more efficient and clearer code. Feel
		 * free to change it if it causes problems.
		 * 
		 * - remiller
		 */
		return type.equals(KnowItBinding.class)
				|| type.getSuperclass().equals(KnowItBinding.class)
				|| Arrays.asList(type.getInterfaces()).contains(
						KnowItBinding.class);
	}

	// ====================== OUT ======================

	@Override
	public void marshal(final Object source,
			final HierarchicalStreamWriter writer,
			final MarshallingContext context) {
		final KnowItBinding binding = (KnowItBinding) source;

		// redirect to the appropriate writing method.
		binding.process(new BindingVisitor() {
			@Override
			public void processResource(KnowItBindingResource constant) {
				final Resource value = constant.getValue();

				if (constant.isIdentifiableGameConstant()
						&& !constant.getFirstType().equals(
								GameType.STORY_POINT_TYPE)) {
					XMLAttribute.FLAVOUR.write(writer, FLAVOUR_RESOURCE);
					writer.setValue(value.getTemplateID());
				} else {
					XMLAttribute.FLAVOUR.write(writer, FLAVOUR_CONSTANT);
					XMLNode.TYPE.writeString(writer,
							ListOp.getFirst(value.getTypes()));
					XMLNode.VALUE.writeString(writer, value.getCodeText());
				}
			}

			@Override
			public void processFunction(KnowItBindingFunction fxn) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_FUNCTION);
				XMLNode.SCRIPTIT.writeObject(writer, context, fxn.getValue());
			}

			@Override
			public void processReference(KnowItBindingReference ref) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_REFERENCE);
				XMLNode.KNOWIT.writeObject(writer, context, ref.getValue());
			}

			@Override
			public void processUninitialized(KnowItBindingUninitialized un) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_UNINITIALIZED);
				XMLNode.KNOWIT.writeObject(writer, context, un.getValue());
			}

			@Override
			public void processAutomatic(KnowItBindingAutomatic automatic) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_AUTOMATIC);
			}

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_NULL);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint sp) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_STORY_POINT);
				XMLNode.STORY_POINT.writeObject(writer, context, sp.getValue());
			}

			@Override
			public void processStoryGroup(KnowItBindingStoryGroup sg) {
				XMLAttribute.FLAVOUR.write(writer, FLAVOUR_STORY_GROUP);
				XMLNode.STORY_GROUP.writeObject(writer, context, sg.getValue());
			}
		});
	}

	// ====================== IN ======================

	/**
	 * Unmarshals the KnowItBinding and returns it. returns null if a problem
	 * occured while marshalling.
	 */
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String flavour = XMLAttribute.FLAVOUR.read(reader);
		final KnowItBinding binding;

		reader.getNodeName();

		// Let's figure out which subtype of KnowItBinding we want.
		if (flavour == null || flavour.equalsIgnoreCase(FLAVOUR_NULL))
			binding = null;
		else if (flavour.equalsIgnoreCase(FLAVOUR_CONSTANT))
			binding = this.unmarshallConstantBinding(reader);
		else if (flavour.equalsIgnoreCase(FLAVOUR_RESOURCE))
			binding = this.unmarshallResourceBinding(reader);
		else if (flavour.equalsIgnoreCase(FLAVOUR_FUNCTION))
			binding = this.unmarshallFunctionBinding(reader, context);
		else if (flavour.equalsIgnoreCase(FLAVOUR_REFERENCE))
			binding = this.unmarshallReferenceBinding(reader, context);
		else if (flavour.equalsIgnoreCase(FLAVOUR_UNINITIALIZED))
			binding = this.unmarshallUninitializedBinding(reader, context);
		else if (flavour.equalsIgnoreCase(FLAVOUR_AUTOMATIC))
			binding = new KnowItBindingAutomatic();
		else if (flavour.equalsIgnoreCase(FLAVOUR_STORY_POINT))
			binding = this.unmarshallStoryPointBinding(reader, context);
		else if (flavour.equalsIgnoreCase(FLAVOUR_STORY_GROUP))
			binding = this.unmarshallStoryGroupBinding(reader, context);
		else
			// VizziniAmazementException - remiller
			throw new ConversionException("Inconceivable binding type: "
					+ flavour);

		if (binding == null || binding.getValue() == null)
			return new KnowItBindingNull();
		else
			return binding;
	}

	private KnowItBindingResource unmarshallConstantBinding(
			HierarchicalStreamReader reader) {
		final Resource constant;
		final String value;
		final String type;

		type = XMLNode.TYPE.readString(reader);
		value = XMLNode.VALUE.readString(reader);

		constant = SimpleResource.buildSimpleResource(type, value);

		return new KnowItBindingResource(constant);
	}

	/**
	 * Unmarshalls the {@link Resource} binding and returns a
	 * {@link KnowItBindingResource}.
	 * 
	 * @param reader
	 * @return null if the {@link Resource} failed to read.
	 */
	private KnowItBindingResource unmarshallResourceBinding(
			HierarchicalStreamReader reader) {
		final String id;
		Resource resource = null;

		id = reader.getValue();

		// Ew. Gross. - remiller
		final GameModule currentModule = StoryModelConverter.currentStory
				.getModule();

		if (currentModule == null) {
			throw new IllegalStateException(
					"Cannot unmarshall a Resource binding without a module loaded");
		}

		resource = currentModule.getInstanceForObjectIdentifier(id);

		if (resource != null)
			return new KnowItBindingResource(resource);

		for (DialogueLine line : StoryModelConverter.currentStory
				.getDialogueRoots()) {
			if (line.getTemplateID().equals(id))
				return new KnowItBindingResource(line);

			for (Resource descendant : line.getDescendants()) {
				if (descendant.getTemplateID().equals(id))
					return new KnowItBindingResource(descendant);
			}
		}

		System.err.println("Binding lookup failed for id " + id
				+ ", assigning null instead.");
		return null;
	}

	private KnowItBindingFunction unmarshallFunctionBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final ScriptIt scriptIt;

		scriptIt = XMLNode.SCRIPTIT.readObject(reader, context, ScriptIt.class);

		return new KnowItBindingFunction(scriptIt);
	}

	private KnowItBindingReference unmarshallReferenceBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final KnowIt referent;

		referent = XMLNode.KNOWIT.readObject(reader, context, KnowIt.class);

		return new KnowItBindingReference(referent);
	}

	private KnowItBindingUninitialized unmarshallUninitializedBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final KnowIt knowIt;

		knowIt = XMLNode.KNOWIT.readObject(reader, context, KnowIt.class);

		return new KnowItBindingUninitialized(
				new KnowItBindingReference(knowIt));
	}

	private KnowItBindingStoryPoint unmarshallStoryPointBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final StoryPoint storyPoint;

		storyPoint = XMLNode.STORY_POINT.readObject(reader, context,
				StoryPoint.class);

		return new KnowItBindingStoryPoint(storyPoint);
	}

	private KnowItBindingStoryGroup unmarshallStoryGroupBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final StoryGroup storyGroup;

		storyGroup = XMLNode.STORY_GROUP.readObject(reader, context,
				StoryGroup.class);

		return new KnowItBindingStoryGroup(storyGroup);
	}
}
