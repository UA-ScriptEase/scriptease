package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPointNode;

/**
 * JUnit tests relating to QuestNodes for ScriptEase 2
 * 
 * @author mfchurch
 * 
 */
public class QuestNodeTests {

	@Test
	public void isTerminalWithoutChildren() {
		QuestNode questNode = new QuestNode();
		assertTrue(questNode.isTerminalNode());
	}

	@Test
	public void isNotTerminalWithChildren() {
		QuestNode questNode = new QuestNode();
		questNode.addChild(QuestPointNodeTests.createQuestPointNode());
		assertFalse(questNode.isTerminalNode());
	}
	
	@Test
	public void canShrinkWhenNotSmallest() {
		QuestPointNode start = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode mid = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode end = QuestPointNodeTests.createQuestPointNode();
		start.addChild(mid);
		mid.addChild(end);
		
		QuestNode questNode = new QuestNode("test", start, end, false);
		assertTrue(questNode.canShrink());
	}
	
	@Test
	public void cannotShrinkWhenSmallest() {
		QuestPointNode start = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode end = QuestPointNodeTests.createQuestPointNode();
		start.addChild(end);
		
		QuestNode questNode = new QuestNode("test", start, end, false);
		assertFalse(questNode.canShrink());
	}
	
	@Test
	public void canGrowWhenNotBiggest() {
		QuestPointNode start = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode mid = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode end = QuestPointNodeTests.createQuestPointNode();
		start.addChild(mid);
		mid.addChild(end);
		
		QuestNode questNode = new QuestNode("test", start, mid, false);
		assertTrue(questNode.canGrow());
	}
	
	@Test
	public void cannotGrowWhenBiggest() {
		QuestPointNode start = QuestPointNodeTests.createQuestPointNode();
		QuestPointNode end = QuestPointNodeTests.createQuestPointNode();
		start.addChild(end);
		
		QuestNode questNode = new QuestNode("test", start, end, false);
		assertFalse(questNode.canGrow());
	}
}
