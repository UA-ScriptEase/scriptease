package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import scriptease.gui.quests.QuestPoint;
import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.codegenerator.code.fragments.FormatFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;

/**
 * JUnit tests relating to QuestPoints for ScriptEase 2
 * 
 * @author mfchurch
 * 
 */
public class QuestPointTests {

	@Test
	public void questPointCanAcceptCause() {
		QuestPoint createQuestPoint = createQuestPoint();

		ArrayList<FormatFragment> code = new ArrayList<FormatFragment>();
		code.add(new LiteralFragment(""));

		// Create a cause scriptit (subject/slot)
		ScriptIt cause = new ScriptIt("cause");

		CodeBlock causeCodeBlock = new CodeBlock("subject", "slot",
				new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<KnowIt>(), code);
		cause.addCodeBlock(causeCodeBlock);

		// Create an effect scriptIt (not subject/slot)
		ScriptIt effect = new ScriptIt("effect");
		CodeBlock effectCodeBlock = new CodeBlock("", "",
				new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<KnowIt>(), code);
		effect.addCodeBlock(effectCodeBlock);

		// check if questpoints only accept causes
		assertTrue(createQuestPoint.canAcceptChild(cause));
		assertFalse(createQuestPoint.canAcceptChild(effect));
	}

	private QuestPoint createQuestPoint() {
		return new QuestPoint("test", 1, false);
	}
}
