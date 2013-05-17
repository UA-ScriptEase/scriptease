package scriptease.gui.action.search;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.GameObjectSearchPanel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;

/**
 * Listens for prompt to open a global search (and optional replace) window for
 * game objects in the current active story model.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class GameObjectSearchAction extends ActiveModelSensitiveAction {
	private static final String GAME_OBJECT_SEARCH = "Search for Game Objects";

	// Singleton
	private static GameObjectSearchAction instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static GameObjectSearchAction getInstance() {
		if (instance == null) {
			instance = new GameObjectSearchAction();
		}

		return GameObjectSearchAction.instance;
	}

	/**
	 * Defines a <code>GameObjectSearchAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private GameObjectSearchAction() {
		super(GameObjectSearchAction.GAME_OBJECT_SEARCH);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
	}

	@Override
	/**
	 * Create the search and replace frame and process the components in the 
	 * current story point so we can retrieve our game objects!
	 */
	public void actionPerformed(ActionEvent arg0) {
		createSearchFrame();
	}

	private void createSearchFrame() {
		WindowFactory.getInstance().buildAndShowCustomFrame(
				new GameObjectSearchPanel(this.getExistingGameObjects()),
				"Find/Replace Game Objects", true);
	}

	private List<KnowIt> getExistingGameObjects() {
		final StoryPoint root = SEModelManager.getInstance().getActiveRoot();
		final StoryAdapter adapter;

		final List<KnowIt> gameObjects = new ArrayList<KnowIt>();

		adapter = new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				this.defaultProcessComplex(storyPoint);
				for (StoryPoint successor : storyPoint.getSuccessors())
					successor.process(this);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processCauseIt(CauseIt causeIt) {
				causeIt.processParameters(this);
				this.defaultProcessComplex(causeIt);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				scriptIt.processParameters(this);
				this.defaultProcessComplex(scriptIt);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				DescribeItManager describeItManager = TranslatorManager
						.getInstance().getActiveDescribeItManager();

				if (describeItManager.getDescribeIt(knowIt) == null) {

					knowIt.getBinding().process(new BindingAdapter() {

						@Override
						public void processReference(
								KnowItBindingReference reference) {
							reference.getValue().getBinding().process(this);
						}

						public void processConstant(
								KnowItBindingResource resource) {
							if (resource.isIdentifiableGameConstant())
								gameObjects.add(knowIt);
						};
					});
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				this.defaultProcessComplex(container);
			}
		};

		root.process(adapter);
		return gameObjects;
	}
}
