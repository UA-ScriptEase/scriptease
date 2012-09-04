package scriptease.translator.codegenerator.code.contexts;

import java.util.Iterator;

import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.quests.QuestNode;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;

/**
 * Context used for accessing GraphNodes in CodeGeneration
 * 
 * @author mfchurch
 * 
 */
public class GraphNodeContext extends Context {
	protected GraphNode node;

	public GraphNodeContext(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator,
			LocationInformation locationInfo) {
		super(model, indent, existingNames, translator);
		this.setLocationInfo(locationInfo);
	}

	public GraphNodeContext(Context other) {
		this(other.getModel(), other.getIndent(), other.getNamifier(), other
				.getTranslator(), other.getLocationInfo());
	}

	public GraphNodeContext(Context other, GraphNode source) {
		this(other);
		this.node = source;
	}

	@Override
	public Iterator<GraphNode> getChildrenNodes() {
		return this.node.getChildren().iterator();
	}

	@Override
	public Iterator<GraphNode> getParentNodes() {
		return this.node.getParents().iterator();
	}
}
