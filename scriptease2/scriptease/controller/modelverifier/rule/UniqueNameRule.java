package scriptease.controller.modelverifier.rule;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.groupvisitor.SameNameGroupVisitor;
import scriptease.controller.modelverifier.problem.ModelProblem;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Checks if there exists a KnowIt in scope with the same name as source
 * 
 * @author mfchurch
 * 
 */
public class UniqueNameRule implements StoryRule {
	private Collection<StoryProblem> problems; 

	@Override
	public Collection<StoryProblem> validate(ComplexStoryComponent root,
			final StoryComponent source) {
		this.problems = new ArrayList<StoryProblem>();
		source.process(new AbstractNoOpStoryVisitor() {
			@Override
			public void processKnowIt(KnowIt knowIt) {
				SameNameGroupVisitor groupVisitor = new SameNameGroupVisitor(
						knowIt);
				for (KnowIt sameName : groupVisitor.getGroup())
					addProblem(sameName);
			}
		});
		return this.problems;
	}

	/**
	 * Constructs and adds a ModelProblem to the list of problems
	 * 
	 * @param component
	 */
	private void addProblem(final KnowIt component) {
		final String description = "A KnowIt in scope with the same name already exists. Name will be uniquified";
		final int priority = 2;
		final Runnable solution = new Runnable() {
			@Override
			public void run() {
				int attempt = 0;
				String displayText = component.getDisplayText();
				component.setDisplayText(displayText + attempt);
				Collection<StoryProblem> problems = UniqueNameRule.this
						.validate(null, component);
				while (!problems.isEmpty()) {
					attempt++;
					component.setDisplayText(displayText + attempt);
					problems = UniqueNameRule.this.validate(null, component);
				}
			}
		};
		ModelProblem problem = new ModelProblem(component, description,
				solution, priority);
		this.problems.add(problem);
	}
}
