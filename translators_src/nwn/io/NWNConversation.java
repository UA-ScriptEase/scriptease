package io;

import io.genericfileformat.GenericFileFormat;
import io.genericfileformat.GenericFileFormat.GffStruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scriptease.translator.io.model.Resource;
import scriptease.util.FileOp;

/**
 * Conversation scriptease-side game resource implementation.
 * 
 * @author remiller
 */
public class NWNConversation extends NWNGameConstant {
	public static final String TYPE_DIALOGUE = "Dialogue";
	private final GenericFileFormat convo;
	private List<Resource> children;

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
		return super.hashCode() + this.convo.hashCode();
	}

	public boolean equals(Object obj) {
		return obj instanceof NWNConversation && super.equals(obj);
	}

	@Override
	public List<Resource> getChildren() {
		if (this.children == null) {
			final List<GffStruct> rootStructs;

			rootStructs = this.convo.getTopLevelStruct()
					.getList("StartingList");

			this.children = new ArrayList<Resource>(rootStructs.size());

			int i = 0;
			for (GffStruct rootStruct : rootStructs) {
				final String name;

				name = this.convo.resolveSyncStruct(rootStruct, false)
						.getString("Text");
				if (name != null)
					this.children.add(new NWNDialogueLine(this.convo,
							rootStruct, false, Arrays.asList(Integer
									.toString(i)), name));
				i++;
			}
		}
		return this.children;
	}
}
