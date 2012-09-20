package io;

import io.GenericFileFormat.GffStruct;

import java.util.ArrayList;
import java.util.List;

import scriptease.translator.io.model.GameConversationNode;
import scriptease.util.StringOp;

/**
 * Represents a Neverwinter Nights conversation line. <br>
 * <br>
 * Dialog line indexing format:
 * <i>dialogResRef</i><b>#</b><i>index</i><b>&gt;</b
 * ><i>index</i><b>&gt;</b><i>index</i><b>&gt;</b>...
 * 
 * @author remiller
 */
public class NWNDialogueLine extends NWNGameConstant implements
		GameConversationNode {
	protected static final String INDEXER_SEPARATOR = ">";

	private static final String TYPE_DIALOG_LINE = "dialogue_line";

	private final GffStruct dialogueSyncStruct;
	private final boolean isPlayerLine;
	private final GenericFileFormat conversation;
	private final List<String> indexes;

	/**
	 * Builds a new dialogue line using the given information. <br>
	 * <br>
	 * Resref for the dialog line, as needed by {@link #getTemplateID()}, is
	 * constructed since dialogue lines don't actually have one in the toolset.
	 * 
	 * @param convoResRef
	 *            The resRef of the containing conversation file.
	 * @param listLabel
	 *            which list this line exists in. One of
	 *            <code>EntriesList</code> or <code>RepliesList</code>
	 * @param index
	 *            the index into <code>list</code>.
	 * @param text
	 *            The line's display text, to be returned in {@link #getName()}.
	 * @param dialogueSyncStruct
	 *            The GFF struct that describes this Dialogue Line.
	 * @param indexes
	 */
	public NWNDialogueLine(GenericFileFormat convo,
			GffStruct dialogueSyncStruct, boolean isPlayerLine,
			List<String> indexes) {
		super(constructResRef(convo, dialogueSyncStruct, indexes),
				TYPE_DIALOG_LINE, convo.resolveSyncStruct(dialogueSyncStruct,
						isPlayerLine).getString("Text"), "");

		this.conversation = convo;
		this.dialogueSyncStruct = dialogueSyncStruct;
		this.isPlayerLine = isPlayerLine;
		this.indexes = indexes;
	}

	@Override
	public List<? extends GameConversationNode> getChildren() {
		final List<GameConversationNode> children;
		final String childListLabel;
		final List<GffStruct> childSyncStructs;
		List<String> indexes;
		int index = 0;

		children = new ArrayList<GameConversationNode>();

		if (!this.isLink()) {
			if (this.isPlayerLine)
				childListLabel = "EntriesList";
			else
				childListLabel = "RepliesList";

			childSyncStructs = this.conversation.resolveSyncStruct(
					this.dialogueSyncStruct, this.isPlayerLine).getList(
					childListLabel);

			for (GffStruct syncStruct : childSyncStructs) {
				indexes = new ArrayList<String>(this.indexes);
				indexes.add(Integer.toString(index));
				index++;

				children.add(new NWNDialogueLine(this.conversation, syncStruct,
						!this.isPlayerLine, indexes));
			}
		}

		return children;
	}

	@Override
	public boolean isTerminal() {
		return this.getChildren().size() == 0;
	}

	@Override
	public boolean isLink() {
		final String isChild;
		final String label = "IsChild";

		if (this.dialogueSyncStruct.hasField(label)) {
			isChild = this.dialogueSyncStruct.getString(label);

			// Field 'IsChild' is 1 when it is a link. Because that
			// totally makes sense, BioWare.
			return isChild.equals("1");
		} else {
			return false;
		}
	}

	@Override
	public String getCodeText() {
		// this shouldn't ever matter, since dialogue lines can't be referenced
		// in scripts. Liskov would be proud.
		return "";
	}

	@Override
	public String toString() {
		return this.getName();
	}

	private static String constructResRef(GenericFileFormat convo,
			GffStruct dialogueSyncStruct, List<String> indexes) {
		String lineResRef;

		lineResRef = convo.getResRef() + "." + convo.getFileType().trim()
				+ GenericFileFormat.RESREF_SEPARATOR;
		lineResRef += StringOp.join(indexes, INDEXER_SEPARATOR);

		return lineResRef;
	}
}