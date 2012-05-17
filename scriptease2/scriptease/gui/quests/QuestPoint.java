package scriptease.gui.quests;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;

/**
 * QuestPoints are the basic units used to build stories. Each QuestPoint holds
 * the StoryComponents that dictate the behaviour of the game world in the
 * current state of the game.
 * 
 * QuestPoints also have a fanIn property, which is the number of parent
 * QuestPoints which must have succeeded before this point can become active.
 * 
 * QuestPoints can be committing or not, depending on whether they shut off
 * alternative pathways through the quest.
 * 
 * @author mfchurch
 * @author graves
 * 
 */
public class QuestPoint extends StoryComponentContainer {
	public static String QUEST_POINT_TYPE = "questPoint";
	private boolean commiting;
	private int fanIn;

	private static int questPointCounter = 1;
	private final String NEW_QUEST_POINT = "New Quest Point";

	/**
	 * Creates a new QuestPoint.
	 * 
	 * @param name
	 *            If name is null or empty string, it gives a default name of
	 *            NEW_QUEST_POINT and the current quest point count.
	 * @param fanIn
	 * @param commiting
	 */
	public QuestPoint(String name, int fanIn, boolean commiting) {
		super();
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		if ((name.equals("")) || (name == null)) {
			name = NEW_QUEST_POINT + " " + questPointCounter++;
		}

		this.setDisplayText(name);
		this.fanIn = fanIn;
		this.commiting = commiting;
	}

	/**
	 * Only accepts Causes as children.
	 */
	@Override
	public boolean canAcceptChild(StoryComponent potentialChild) {
		// Only accept causes
		if (potentialChild instanceof ScriptIt) {
			if (((ScriptIt) potentialChild).isCause())
				return super.canAcceptChild(potentialChild);
		}
		return false;
	}

	public Boolean getCommitting() {
		return this.commiting;
	}

	public Integer getFanIn() {
		return this.fanIn;
	}

	public void setFanIn(Integer fanIn) {
		this.fanIn = fanIn;
	}

	public void setCommiting(boolean commiting) {
		this.commiting = commiting;
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processQuestPoint(this);
	}

	public void setQuestContainer(QuestNode quest) {
	}

	public QuestNode getQuestContainer() {
		// TODO get the quest which contains this questpoint
		return new QuestNode();
	}

	@Override
	public String toString() {
		return "QuestPoint (\"" + this.getDisplayText() + "\")";
	}
}
