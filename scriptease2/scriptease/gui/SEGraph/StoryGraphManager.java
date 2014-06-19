package scriptease.gui.SEGraph;

import java.util.ArrayList;

import scriptease.model.complex.StoryNode;

public class StoryGraphManager {
	
	private static final StoryGraphManager instance = new StoryGraphManager();
	private ArrayList<SEGraph<StoryNode>> storyGraphs = new ArrayList<SEGraph<StoryNode>>();
	
	public static StoryGraphManager getInstance(){
		return StoryGraphManager.instance;
	}
	
	public void addGraph(SEGraph<StoryNode> graph){
		storyGraphs.add(graph);
	}
	
	public ArrayList<SEGraph<StoryNode>> getStoryGraphs(){
		return storyGraphs;
	}

	
}

