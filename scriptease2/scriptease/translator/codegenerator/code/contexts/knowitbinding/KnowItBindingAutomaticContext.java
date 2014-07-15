package scriptease.translator.codegenerator.code.contexts.knowitbinding;

import java.util.Collection;

import scriptease.controller.StoryComponentUtils;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.KnowItContext;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;

public class KnowItBindingAutomaticContext extends KnowItBindingContext {
	final Context previous;

	public KnowItBindingAutomaticContext(Context other, KnowItBinding source) {
		super(other, source);
		previous = other;
	}

	public String getUnique32CharName() {
		final StoryPoint val;

		val = StoryComponentUtils
				.getParentStoryPoint(((KnowItContext) this.previous)
						.getComponent());

		return val.getUnique32CharName();
	};

	/**
	 * Get the KnowItBinding's value
	 */
	@Override
	public String getValue() {
		final String value;
		final Object binding;

		binding = this.binding.getValue();

		if (binding instanceof Resource)
			value = ((Resource) binding).getCodeText();
		else {
			final String errorString;
			final String typeKeyword;
			final GameType type;

			errorString = "Unimplemented type in getValue of KnowItBindingAutomatic: "
					+ this.binding;

			typeKeyword = this.binding.getFirstType();
			type = this.getModel().getType(typeKeyword);

			if (type != null) {
				final Collection<AbstractFragment> typeFormat;

				typeFormat = type.getFormat();

				if (typeFormat != null && !typeFormat.isEmpty())
					value = AbstractFragment.resolveFormat(typeFormat, this);
				else
					value = errorString;
			} else
				value = errorString;
		}

		return value;
	}
}
