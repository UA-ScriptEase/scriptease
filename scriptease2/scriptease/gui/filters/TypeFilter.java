package scriptease.gui.filters;

import java.util.Collection;
import java.util.Iterator;

import scriptease.controller.StoryAdapter;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;

/**
 * Filters out all StoryComponents that do not contain some type of reference to
 * the list of types given in the constructor.
 * 
 * @author remiller
 */
public class TypeFilter extends StoryComponentFilter {
	private final Collection<String> types;

	public TypeFilter(Collection<String> types) {
		this.types = types;
	}

	@Override
	public int getMatchCount(StoryComponent component) {
		final TypeFilterVisitor visitor = new TypeFilterVisitor();

		component.process(visitor);

		return visitor.acceptable ? 1 : 0;
	}

	@Override
	public void addRule(Filter newFilter) {
		if (newFilter instanceof TypeFilter) {
			this.types.clear();
			this.types.addAll(((TypeFilter) newFilter).types);
		} else
			super.addRule(newFilter);
	}

	private class TypeFilterVisitor extends StoryAdapter {
		private boolean acceptable = false;

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			final Iterator<String> typeIterator = scriptIt.getTypes()
					.iterator();

			// Iterate over all of the DoIt's types.
			while (typeIterator.hasNext()) {
				String nextType = typeIterator.next();

				// If the type matches, accept it.
				if (TypeFilter.this.types.contains(nextType)) {
					this.acceptable = true;
					return;
				}

				scriptIt.processParameters(this);
			}
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			for (String type : knowIt.getAcceptableTypes()) {
				if (TypeFilter.this.types.contains(type)) {
					this.acceptable = true;
					return;
				}
			}
		}

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer container) {
			this.acceptable = true;
		}

		@Override
		public void processAskIt(AskIt questionIt) {
			questionIt.getCondition().process(this);
		}
	}
}
