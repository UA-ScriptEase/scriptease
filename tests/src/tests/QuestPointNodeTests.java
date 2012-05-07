package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;

/**
 * JUnit tests relating to QuestPointNodes for ScriptEase 2
 * 
 * @author mfchurch
 * 
 */
public class QuestPointNodeTests {

	@Test
	public void isTerminalWithoutChildren() {
		QuestPointNode questPointNode = createQuestPointNode(createQuestPoint());
		assertTrue(questPointNode.isTerminalNode());
	}

	@Test
	public void isNotTerminalWithChildren() {
		QuestPointNode questPointNode = createQuestPointNode(createQuestPoint());
		questPointNode.addChild(createQuestPointNode(createQuestPoint()));
		assertFalse(questPointNode.isTerminalNode());
	}

	@Test
	public void representsTheQuestPoint() {
		QuestPoint createQuestPoint = createQuestPoint();
		QuestPointNode questPointNode = createQuestPointNode(createQuestPoint);
		assertTrue(questPointNode.represents(createQuestPoint));
	}

	public static QuestPointNode createQuestPointNode(QuestPoint questPoint) {
		return new QuestPointNode(questPoint);
	}
	
	public static QuestPointNode createQuestPointNode() {
		return new QuestPointNode(createQuestPoint());
	}

	private static QuestPoint createQuestPoint() {
		return new QuestPoint("test", 1, false);
	}
}
