package scriptease.gui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.Timer;

import scriptease.gui.SETree.GameObjectTreeModel;
import scriptease.gui.SETree.filters.Filterable;
import scriptease.gui.SETree.filters.GameConstantSearchFilter;
import scriptease.gui.SETree.filters.StoryComponentSearchFilter;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;

@SuppressWarnings("serial")
public class FilterableSearchField extends JTextField {

	public final static String SEARCH_FILTER_LABEL = Il8nResources
			.getString("Search_Filter_");

	public FilterableSearchField(Filterable filterable, int size) {
		super(size);
		KeyAdapter timedSearcher = new TimedSearcher(this, filterable);
		this.addKeyListener(timedSearcher);
	}
	
	public void addFilter(Filterable filterable) {
		KeyAdapter timedSearcher = new TimedSearcher(this, filterable);
		this.addKeyListener(timedSearcher);
	}

	private final class TimedSearcher extends KeyAdapter {
		private int TIME_BUFFER = 200;
		Timer timer;

		private TimedSearcher(final JTextField searchField,
				final Filterable filterable) {

			timer = new Timer(TIME_BUFFER, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (filterable instanceof GameObjectTreeModel)
						filterable.updateFilter(new GameConstantSearchFilter(
								searchField.getText()));
					else if (filterable instanceof StoryComponentPanelTree)
						filterable.updateFilter(new StoryComponentSearchFilter(
								searchField.getText()));
				};
			});
			timer.setRepeats(false);
		}

		@Override
		// Change in Search Field
		public void keyReleased(KeyEvent arg0) {
			timer.restart();
		}
	}
}
