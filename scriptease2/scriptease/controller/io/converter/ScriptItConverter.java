package scriptease.controller.io.converter;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.controller.io.FileIO;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts only ScriptIts to/from XML.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * 
 * @see ComplexStoryComponentConverter
 */
public class ScriptItConverter extends ComplexStoryComponentConverter {
	public static final String TAG_SCRIPTIT = "ScriptIt";
	private static final String TAG_CODEBLOCKS = "CodeBlocks";

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final ScriptIt scriptIt = (ScriptIt) source;

		super.marshal(source, writer, context);

		// CodeBlocks
		writer.startNode(TAG_CODEBLOCKS);
		context.convertAnother(scriptIt.getCodeBlocks());
		writer.endNode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final ScriptIt scriptIt;

		scriptIt = (ScriptIt) super.unmarshal(reader, context);

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final Collection<CodeBlock> codeBlocks;

			codeBlocks = ((Collection<CodeBlock>) context.convertAnother(
					scriptIt, ArrayList.class));

			if (codeBlocks.isEmpty())
				throw new IllegalStateException(
						"Unable to read CodeBlocks for " + scriptIt);

			scriptIt.setCodeBlocks(codeBlocks);
			reader.moveUp();
		}

		/*
		 * A wild hack appears!
		 */
		if (scriptIt.isCause()
				&& FileIO.getInstance().getMode() == FileIO.IoMode.STORY) {
			// Go down through and rebind references to what they should really
			// be bound to: implicits
			this.rebindKnowItBindingReferences(scriptIt);
		}

		// Search for KnowItBindingReferences.

		return scriptIt;
	}

	/**
	 * This goes through the entire ScriptIt, looking for
	 * KnowItBindingReferences in all of the nooks and crannies. Then we check
	 * if these are supposed to be implicits, in which case we replace them with
	 * actual implicits. Before, we were generating new knowits to be bound
	 * here, which obliterated code gen..
	 * 
	 * 
	 * TODO Abstract this somehow, according to ticket #40870537. We only call
	 * this method on causes while in story mode. However, we also go over the
	 * entire model in the NWN translator to find journal effects. We should
	 * just make one pass through the entire module and run a bunch of runnables
	 * at appropriate times.
	 * 
	 * @param scriptIt
	 */
	private void rebindKnowItBindingReferences(final ScriptIt scriptIt) {
		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer storyComponentContainer) {
				this.defaultProcessComplex(storyComponentContainer);
			}

			@Override
			public void processScriptIt(ScriptIt s) {
				s.processSubjects(this);
				s.processParameters(this);
				this.defaultProcessComplex(s);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				KnowItBinding binding = knowIt.getBinding();
				final StoryAdapter outerAnonInnerClass = this;
				binding.process(new BindingAdapter() {
					@Override
					public void processReference(
							KnowItBindingReference reference) {

						for (KnowIt implicit : scriptIt.getImplicits()) {
							if (implicit.equals(reference.getValue())) {
								knowIt.setBinding(new KnowItBindingReference(
										implicit));
							}
						}
					}

					@Override
					public void processFunction(KnowItBindingFunction function) {
						ScriptIt referenced = function.getValue();
						referenced.process(outerAnonInnerClass);
					}
				});
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIt.getCondition().process(this);
				this.defaultProcessComplex(askIt);
			}

			@Override
			public void processStoryItemSequence(StoryItemSequence sequence) {
				this.defaultProcessComplex(sequence);
			}
		};

		scriptIt.process(adapter);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ScriptIt.class);
	}

	@Override
	protected StoryComponent buildComponent(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return new ScriptIt("");
	}
}
