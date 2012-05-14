package scriptease.gui.action.story.quests;

import java.awt.event.ActionEvent;

import scriptease.gui.action.ToolBarButtonAction;
import scriptease.gui.quests.QuestPoint;

/**
 * Reperesents and performs the Toggle Committing command, as well as
 * encapsulating its enabled and name display state.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public final class ToggleCommittingAction extends ToolBarButtonAction {

	private Boolean currentState;
	private QuestPoint questPoint;

	private static final String COMMIT_FALSE_TEXT = "commit_false";
	private static final String COMMIT_TRUE_TEXT = "commit_true";

	private static final ToggleCommittingAction instance = new ToggleCommittingAction();

	/**
	 * Defines a <code>InsertQuestPointAction</code> object with an icon.
	 */
	protected ToggleCommittingAction() {
		super(ToggleCommittingAction.COMMIT_FALSE_TEXT);
		this.setCommitting(false);
		setEnabled(false);
	}

	/**
	 * Gets the sole instance of this particular type of Action.
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static ToggleCommittingAction getInstance() {
		return ToggleCommittingAction.instance;
	}

	private void setCommitting(Boolean isCommitting) {
		this.currentState = isCommitting;
	}

	private Boolean getCommitting() {
		return this.currentState;
	}

	/**
	 * Sets the Quest Point to be operated on and enables the action. Also
	 * sets the icon whether the quest point is committing or not.
	 * 
	 * @param questPoint
	 */
	public void setQuestPoint(QuestPoint questPoint) {
		this.questPoint = questPoint;
		Boolean questPointCommitting = questPoint.getCommitting();
		this.setCommitting(questPointCommitting);
		if (!ToggleCommittingAction.getInstance().isEnabled()) {
			ToggleCommittingAction.getInstance().setEnabled(true);
		}

		if (!questPointCommitting) {
			this.updateIcon(ToggleCommittingAction.COMMIT_FALSE_TEXT);
		} else {
			this.updateIcon(ToggleCommittingAction.COMMIT_TRUE_TEXT);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (!this.getCommitting()) {
			this.updateIcon(ToggleCommittingAction.COMMIT_TRUE_TEXT);
			setCommitting(true);
			this.questPoint.setCommiting(true);
		} else {
			this.updateIcon(ToggleCommittingAction.COMMIT_FALSE_TEXT);
			setCommitting(false);
			this.questPoint.setCommiting(false);

		}
	}
}
