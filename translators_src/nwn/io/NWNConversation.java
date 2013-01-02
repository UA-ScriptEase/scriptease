package io;

import io.genericfileformat.GenericFileFormat;
import io.genericfileformat.GenericFileFormat.GffStruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;
import scriptease.util.FileOp;

/**
 * Conversation scriptease-side game resource implementation.
 * 
 * @author remiller
 */
public class NWNConversation extends NWNGameConstant implements
		GameConversation {
	public static final String TYPE_DIALOGUE = "dialogue";
	private final GenericFileFormat convo;

	public NWNConversation(String resref, GenericFileFormat conversation) {
		super(resref, TYPE_DIALOGUE, conversation.getResRef(), "");
		this.convo = conversation;
	}

	@Override
	public String getCodeText() {
		return "\"" + FileOp.removeExtension(this.getTemplateID()) + "\"";
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public int hashCode() {
		return super.hashCode() + convo.hashCode();
	}

	public boolean equals(Object obj) {
		return obj instanceof NWNConversation && super.equals(obj);
	}

	@Override
	public List<GameConversationNode> getConversationRoots() {
		final List<GameConversationNode> roots;
		final List<GffStruct> rootStructs;
		int i = 0;

		rootStructs = this.convo.getTopLevelStruct().getList("StartingList");

		roots = new ArrayList<GameConversationNode>(rootStructs.size());

		for (GffStruct rootStruct : rootStructs) {
			roots.add(new NWNDialogueLine(this.convo, rootStruct, false, Arrays
					.asList(Integer.toString(i))));
			i++;
		}

		return roots;
	}
}
