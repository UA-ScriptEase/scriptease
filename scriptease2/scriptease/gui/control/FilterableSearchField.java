package scriptease.gui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	
	private Filterable filterable;

	public FilterableSearchField(Filterable filterable, int size) {
		super(size);
		this.addFilter(filterable);
	}
	
	public void addFilter(Filterable filterable) {
		this.filterable = filterable;
		DocumentListener listener = new SearchListener(this, filterable);
		this.getDocument().addDocumentListener(listener);
	}

	/*
	 * This is with a timer. Doesn't look like timer is causing the memory leak,
	 * since removing the timer made ScriptEase explode faster.
	 */
	private final class SearchListener implements DocumentListener {
		private final int TIME_BUFFER = 800;
		private final Timer timer;
		
		public SearchListener(final JTextField searchField,
				final Filterable filterable) {
			timer = new Timer(TIME_BUFFER, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					/*
					 * TODO why are we checking instanceof here? The methods
					 * should be common for both.
					 * 
					 * Create a filter class that has two constructors, one with
					 * GameObjectTreeModel, and the other with a
					 * StoryComponentPanelTree. 
					 * 
					 * Then we can just call:
					 * 
					 * filterable.updateFilter(new ComponentFilter(searchField.getText());
					 */
					if (filterable instanceof GameObjectTreeModel)
						filterable.updateFilter(new GameConstantSearchFilter(
								searchField.getText()));
					else if (filterable instanceof StoryComponentPanelTree)
						filterable.updateFilter(new StoryComponentSearchFilter(
								searchField.getText()));
					timer.stop();
				}
			});
			timer.setRepeats(false);
		}
		
		private void executeSearch() {
			timer.restart();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			this.executeSearch();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.executeSearch();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.executeSearch();
		}
	}
	
	/*
	 * Without a timer. Timer should not be the issue, so this code can likely
	 * get removed later. Unless it's so dang fast that we don't need no stinkin' 
	 * timers.
	 */
	
/*	private final class SearchListener implements DocumentListener {
		private final JTextField searchField;
		private final Filterable filterable;
		
		public SearchListener(final JTextField searchField,
				final Filterable filterable) {
			this.searchField = searchField;
			this.filterable = filterable;
		}
		
		private void executeSearch() {
			if (filterable instanceof GameObjectTreeModel)
				filterable.updateFilter(new GameConstantSearchFilter(
						searchField.getText()));
			else if (filterable instanceof StoryComponentPanelTree)
				filterable.updateFilter(new StoryComponentSearchFilter(
						searchField.getText()));		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			this.executeSearch();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.executeSearch();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.executeSearch();
		}
	}*/

	@Override
	public String toString(){ 
		return "FilterableSearchField ["+filterable.getClass().getName()+"]";
	}
}
