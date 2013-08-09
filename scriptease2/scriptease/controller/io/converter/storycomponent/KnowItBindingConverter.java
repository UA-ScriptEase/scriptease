package scriptease.controller.io.converter.storycomponent;

import java.util.Arrays;

import scriptease.controller.BindingVisitor;
import scriptease.controller.io.XMLNode;
import scriptease.controller.io.converter.model.StoryModelConverter;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

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

	// TODO See LibraryModelConverter class for an example of how to refactor
	// this class. However, since we're moving to YAML eventually, we don't need
	// to waste anymore time on refactoring these.
	private static final String TAG_VALUE = "Value";

	private static final String ATTRIBUTE_BINDING_FLAVOUR = "flavour";
	private static final String ATTRIBUTE_VALUE_CONSTANT_FLAVOUR = "constant";
	private static final String ATTRIBUTE_VALUE_RESOURCE_FLAVOUR = "resource";
	private static final String ATTRIBUTE_VALUE_FUNCTION_FLAVOUR = "function";
	private static final String ATTRIBUTE_VALUE_REFERENCE_FLAVOUR = "reference";
	private static final String ATTRIBUTE_VALUE_NULL_FLAVOUR = "null";
	private static final String ATTRIBUTE_VALUE_AUTOMATIC_FLAVOUR = "automatic";
	private static final String ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR = "storyPoint";

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
				if (constant.getFirstType().equals(StoryPoint.STORY_POINT_TYPE)) {
					// deal with it B-->:)
					KnowItBindingConverter.this.marshallConstantBinding(
							constant, writer);
				} else if (constant.isIdentifiableGameConstant())
					KnowItBindingConverter.this
							.marshallIdentifiableGameConstantBinding(constant,
									writer);
				else
					KnowItBindingConverter.this.marshallConstantBinding(
							constant, writer);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				KnowItBindingConverter.this.marshallFunctionBinding(function,
						writer, context);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				KnowItBindingConverter.this.marshallReferenceBinding(reference,
						writer, context);
			}

			@Override
			public void processAutomatic(KnowItBindingAutomatic automatic) {
				KnowItBindingConverter.this.marshallAutomaticBinding(automatic,
						writer, context);
			}

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
						ATTRIBUTE_VALUE_NULL_FLAVOUR);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				KnowItBindingConverter.this.marshallStoryPointBinding(
						storyPoint, writer, context);
			}
		});
	}

	/*
	 * Converts a Game Constant to XML
	 */
	private void marshallConstantBinding(KnowItBindingResource binding,
			HierarchicalStreamWriter writer) {
		final Resource constant = binding.getValue();

		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_CONSTANT_FLAVOUR);

		writer.startNode(TypedComponent.TAG_TYPE);
		writer.setValue(constant.getTypes().iterator().next());
		writer.endNode();

		writer.startNode(TAG_VALUE);
		writer.setValue(constant.getCodeText());
		writer.endNode();
	}

	/*
	 * Converts a Game Constant to XML
	 */
	private void marshallIdentifiableGameConstantBinding(
			KnowItBindingResource binding, HierarchicalStreamWriter writer) {
		final Resource resource = binding.getValue();

		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_RESOURCE_FLAVOUR);

		writer.setValue(resource.getTemplateID());
	}

	/*
	 * Converts a KnowItBindingAutomatic to XML
	 */
	private void marshallAutomaticBinding(KnowItBindingAutomatic binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_AUTOMATIC_FLAVOUR);
	}

	/*
	 * Converts a Function Reference to XML
	 */
	private void marshallFunctionBinding(KnowItBindingFunction binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_FUNCTION_FLAVOUR);

		writer.startNode(ScriptItConverter.TAG_SCRIPTIT);
		context.convertAnother(binding.getValue());
		writer.endNode();
	}

	/*
	 * Converts a KnowIt Reference to XML
	 */
	private void marshallReferenceBinding(KnowItBindingReference binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_REFERENCE_FLAVOUR);

		writer.startNode(KnowItConverter.TAG_KNOWIT);
		context.convertAnother(binding.getValue());
		writer.endNode();
	}

	/*
	 * Converts a Story Point reference to XML
	 */
	private void marshallStoryPointBinding(KnowItBindingStoryPoint binding,
			HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.addAttribute(ATTRIBUTE_BINDING_FLAVOUR,
				ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR);

		writer.startNode(StoryPointConverter.TAG_STORYPOINT);
		final StoryPoint value = binding.getValue();
		if (value == null)
			System.err.println("Bug track: Null value assigned to binding "
					+ binding);
		context.convertAnother(value);
		writer.endNode();
	}

	// ====================== IN ======================

	/**
	 * Unmarshals the KnowItBinding and returns it. returns null if a problem
	 * occured while marshalling.
	 */
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String flavour = reader.getAttribute(ATTRIBUTE_BINDING_FLAVOUR);
		final KnowItBinding binding;

		reader.getNodeName();

		// Let's figure out which subtype of KnowItBinding we want.
		if (flavour == null
				|| flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_NULL_FLAVOUR))
			binding = null;
		else {
			reader.getNodeName();

			if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_CONSTANT_FLAVOUR))
				binding = this.unmarshallConstantBinding(reader);
			else if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_RESOURCE_FLAVOUR))
				binding = this.unmarshallResourceBinding(reader);
			else if (flavour.equalsIgnoreCase(ATTRIBUTE_VALUE_FUNCTION_FLAVOUR))
				binding = this.unmarshallFunctionBinding(reader, context);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_REFERENCE_FLAVOUR))
				binding = this.unmarshallReferenceBinding(reader, context);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_AUTOMATIC_FLAVOUR))
				binding = this.unmarshallAutomaticBinding(reader, context);
			else if (flavour
					.equalsIgnoreCase(ATTRIBUTE_VALUE_STORY_POINT_FLAVOUR))
				binding = this.unmarshallStoryPointBinding(reader, context);
			else
				// VizziniAmazementException - remiller
				throw new ConversionException("Inconceivable binding type: "
						+ flavour);
		}

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

	private KnowItBindingAutomatic unmarshallAutomaticBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		return new KnowItBindingAutomatic();
	}

	private KnowItBindingFunction unmarshallFunctionBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		KnowItBindingFunction binding = new KnowItBindingFunction(null);
		final ScriptIt scriptIt;
		final KnowItBindingFunction knowItBindingFunction;

		// move down and read as a doIt
		reader.moveDown();

		scriptIt = (ScriptIt) context.convertAnother(binding, ScriptIt.class);

		knowItBindingFunction = new KnowItBindingFunction(scriptIt);

		reader.moveUp();

		return knowItBindingFunction;
	}

	private KnowItBindingReference unmarshallReferenceBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final KnowIt referent;

		KnowItBindingReference binding = new KnowItBindingReference(null);

		// move down and read as a knowIt
		reader.moveDown();

		referent = (KnowIt) context.convertAnother(binding, KnowIt.class);

		reader.moveUp();

		binding = new KnowItBindingReference(referent);

		return binding;
	}

	private KnowItBindingStoryPoint unmarshallStoryPointBinding(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		final StoryPoint storyPoint;

		// move down and read as a story point
		reader.moveDown();
		storyPoint = (StoryPoint) context.convertAnother(null,
				StoryPoint.class);
		reader.moveUp();

		return new KnowItBindingStoryPoint(storyPoint);
	}
}
