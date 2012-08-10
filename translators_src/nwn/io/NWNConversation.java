package io;

import io.GenericFileFormat.GffField;
import io.GenericFileFormat.GffStruct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;

/**
 * Conversation Struct as defined in Table 2.1 of the Conversation documentation
 * 
 * @author mfchurch
 */
public class NWNConversation implements GameConversation {
	public static final String DIALOGUE = "dialogue";
	// ResRef of script to run when the conversation is aborted, such as by
	// combat, hitting the ESC key, or saving a game in the middle of
	// conversation.
	private String endConverAbort;
	// ResRef of script to run when the conversation ends normally.
	private String endConversation;
	// List of NPC Dialog Structs. StructID = list index.
	private List<NPCEntryDialogue> entryList;
	// The startingList is the list of all lines of dialog that appear at
	// the root level of the conversation tree.
	private List<EntriesSyncStruct> startingList;
	// List of Player Dialog Structs. StructID = list index.
	private List<PlayerReplyDialogue> replyList;
	private String resRef;

	public NWNConversation(String resRef, GffStruct struct) {
		this.resRef = resRef;
		// parse the important conversation fields
		this.construct(struct);
	}

	/**
	 * Gets the NWNDialog specified by the given resRef. Assumes the resRef is
	 * in the format: list#index
	 * 
	 * @param resRef
	 * @return
	 */
	public DialogueLine getDialogLine(String resRef) {
		final String[] split = resRef.split("#");
		final String list = split[0];
		final Integer index = new Integer(split[1]);
		if (list.equalsIgnoreCase(PlayerReplyDialogue.PLAYER_REPLY_LIST)) {
			return this.replyList.get(index);
		} else if (list.equalsIgnoreCase(NPCEntryDialogue.NPC_ENTRY_LIST)) {
			return this.entryList.get(index);
		} else
			throw new IllegalArgumentException("Invalid NWNDialog resRef");
	}

	/**
	 * Parse the conversation fields we care about from the stored fields.
	 * 
	 * @param reader
	 */
	private void construct(GffStruct struct) {
		final List<GffField> fields = struct.getGffFields();

		// parse the fields that we care about
		for (GffField field : fields) {
			final String label = field.getLabel();

			// Conversation Fields
			if (label.equals("EndConverAbort")) {
				this.endConverAbort = field.getStringData();
			} else if (label.equals("EndConversation")) {
				this.endConversation = field.getStringData();
			} else if (label.equals("EntryList")) {
				this.entryList = new ArrayList<NPCEntryDialogue>();
				List<GffStruct> readList = field.getList();
				for (GffStruct aStruct : readList) {
					this.entryList.add(new NPCEntryDialogue(aStruct));
				}
			} else if (label.equals("ReplyList")) {
				this.replyList = new ArrayList<PlayerReplyDialogue>();
				List<GffStruct> readList = field.getList();
				for (GffStruct aStruct : readList) {
					this.replyList.add(new PlayerReplyDialogue(aStruct));
				}
			} else if (label.equals("StartingList")) {
				this.startingList = new ArrayList<EntriesSyncStruct>();
				List<GffStruct> readList = field.getList();
				for (GffStruct aStruct : readList) {
					this.startingList.add(new EntriesSyncStruct(aStruct));
				}
			}
		}

		for (NPCEntryDialogue entry : this.entryList) {
			entry.resolveReplies();
		}

		for (PlayerReplyDialogue reply : this.replyList) {
			reply.resolveEntries();
		}

		for (EntriesSyncStruct entry : this.startingList) {
			entry.updateReference();
		}
	}

	private NPCEntryDialogue getElementFromEntryList(int index) {
		if (this.entryList != null && index >= 0
				&& index < this.entryList.size()) {
			return this.entryList.get(index);
		}
		return null;
	}

	private PlayerReplyDialogue getElementFromReplyList(int index) {
		if (this.replyList != null && index >= 0
				&& index < this.replyList.size()) {
			return this.replyList.get(index);
		}
		throw new IllegalStateException("Invalid Reply Reference");
	}

	@Override
	public String getResolutionText() {
		return this.resRef;
	}

	@Override
	public String getName() {
		return this.resRef;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public Collection<String> getTypes() {
		ArrayList<String> types = new ArrayList<String>();
		types.add(DIALOGUE);
		return types;
	}

	@Override
	public List<GameConversationNode> getConversationRoots() {
		List<GameConversationNode> list = new ArrayList<GameConversationNode>(
				this.startingList.size());
		for (EntriesSyncStruct entry : this.startingList) {
			list.add(entry.getReference());
		}
		return list;
	}

	@Override
	public String getTemplateID() {
		return this.resRef;
	}

	/**
	 * SyncStruct as defined in section 2.3 of the Conversation documentation.
	 * 
	 * @author mfchurch
	 * 
	 */
	public abstract class DialogSyncStruct {
		protected int index;
		protected GffField active;
		private boolean isLink;

		public DialogSyncStruct(GffStruct struct) {
			// parse the important dialog fields
			this.build(struct);
		}

		private void build(GffStruct struct) {
			final List<GffField> fields = struct.getGffFields();

			// Read the reference fields
			for (GffField field : fields) {
				final String label = field.getLabel();

				// Dialog Fields
				if (label.equals("Active")) {
					this.active = field;
				}
				// Index into the Top-Level Struct EntryList
				else if (label.equals("Index")) {
					this.index = new Integer(field.getStringData());
				}
				// Field 'IsChild' is 1 when it is a link. Because that
				// totally makes sense, BioWare.
				else if (label.equals("IsChild")) {
					this.isLink = field.getStringData().equals("1");
				}
			}
		}

		protected abstract void updateReference();

		public abstract DialogueLine getReference();

		public boolean isLink() {
			return this.isLink;
		}
	}

	/**
	 * Represents a EntriesList Sync Struct as detailed in Table 2.3.3 of the
	 * Conversation documentation.
	 * 
	 * @author mfchurch
	 * 
	 */
	public class EntriesSyncStruct extends DialogSyncStruct {

		public EntriesSyncStruct(GffStruct struct) {
			super(struct);
		}

		@Override
		protected void updateReference() {
			// append the data to the existing entry
			final NPCEntryDialogue entry = this.getReference();
			if (active != null && entry != null) {
				entry.active = this.active;
				entry.index = this.index;
				entry.isLink = this.isLink();
			} else
				throw new IllegalStateException("Invalid EntriesSyncStruct");
		}

		@Override
		public NPCEntryDialogue getReference() {
			return getElementFromEntryList(index);
		}
	}

	/**
	 * Represents a RepliesList Sync Struct as detailed in Table 2.3.2 of the
	 * Conversation documentation.
	 * 
	 * @author mfchurch
	 * 
	 */
	public class RepliesSyncStruct extends DialogSyncStruct {

		public RepliesSyncStruct(GffStruct struct) {
			super(struct);
		}

		@Override
		protected void updateReference() {
			// append the data to the existing entry
			final PlayerReplyDialogue reply = getReference();
			if (active != null && reply != null) {
				reply.active = this.active;
				reply.index = this.index;
				reply.isLink = this.isLink();
			} else
				throw new IllegalStateException("Invalid RepliesSyncStruct");
		}

		@Override
		public PlayerReplyDialogue getReference() {
			return getElementFromReplyList(index);
		}
	}

	/**
	 * Dialog Struct as defined in Table 2.2.1 of the Conversation
	 * documentation.
	 * 
	 * @author mfchurch
	 * 
	 */
	public abstract class DialogueLine implements GameConversationNode {
		private static final String DIALOG_LINE = "dialogue_line";
		// ResRef of conditional script to run to determine if this line of
		// conversation appears to the player.
		protected GffField active;
		// ResRef of script to run when showing this line
		protected GffField script;
		// Localized text to display to the user for this line of dialog
		protected String text;
		// Index representing the location of the Dialog in the conversation
		protected int index;

		protected GffStruct struct;

		protected boolean isLink;

		public DialogueLine(GffStruct struct) {
			this.struct = struct;
			// parse the important dialog fields
			this.build(struct);
		}

		public GffStruct getStruct() {
			return struct;
		}

		public String getConversationResRef() {
			return resRef;
		}

		/**
		 * Parse the dialog fields we care about from the stored fields
		 * 
		 */
		protected void build(GffStruct struct) {
			final List<GffField> fields = struct.getGffFields();

			for (GffField field : fields) {
				final String label = field.getLabel();

				// Dialog Fields
				if (label.equals("Text")) {
					text = field.getStringData();
				}
				// 'script' = when dialogue line is reached
				else if (label.equals("Script")) {
					script = field;
				}
			}

			if (text == null)
				throw new IllegalStateException(
						"Failed to read text for dialogue line.");
		}

		@Override
		public Collection<String> getTypes() {
			ArrayList<String> arrayList = new ArrayList<String>(1);
			arrayList.add(DIALOG_LINE);
			return arrayList;
		}

		@Override
		public String getResolutionText() {
			return getTemplateID();
		}

		@Override
		public String getName() {
			return text;
		}

		public GffField getField(String field) {
			// 'active' = Should dialogue line be displayed (filter)
			if (field.equalsIgnoreCase("Active")) {
				return this.active;
			}
			// 'script' = When dialogue line is displayed
			else if (field.equalsIgnoreCase("Script")) {
				return this.script;
			} else
				throw new IllegalArgumentException(
						"Dialog does not have the field " + field);
		}

		@Override
		public boolean isLink() {
			return false;
		}

		@Override
		public boolean isTerminal() {
			return this.getChildren().size() == 0;
		}
	}

	/**
	 * A Dialog Struct contained in the Player ReplyList contains all the Fields
	 * listed in Table 2.2.1, plus those Fields listed in Table 2.2.2 of the
	 * Conversation documentation
	 * 
	 * @author mfchurch
	 * 
	 */
	public class PlayerReplyDialogue extends DialogueLine {
		// List of Sync Structs describing the list of possible NPC replies
		// to this line of player dialog.. Struct ID = list index.
		private List<EntriesSyncStruct> entryPointers;

		public final static String PLAYER_REPLY_LIST = "replylist";

		public PlayerReplyDialogue(GffStruct struct) {
			super(struct);
		}

		public void resolveEntries() {
			if (entryPointers == null)
				throw new IllegalStateException(
						"EntryList has not been initialized yet!");
			for (EntriesSyncStruct entry : entryPointers) {
				entry.updateReference();
			}
		}

		@Override
		protected void build(GffStruct struct) {
			super.build(struct);

			final List<GffField> fields = struct.getGffFields();

			entryPointers = new ArrayList<EntriesSyncStruct>();

			for (GffField field : fields) {
				final String label = field.getLabel();

				// List of Sync Structs describing the list of possible NPC
				// replies to this line of player dialog
				if (label.equals("EntriesList")) {
					List<GffStruct> readList = field.getList();
					for (GffStruct aStruct : readList) {
						final EntriesSyncStruct sync = new EntriesSyncStruct(
								aStruct);
						entryPointers.add(sync);
					}
				}
			}
		}

		@Override
		public String toString() {
			return "PlayerReplyDialog [" + this.text + "]";
		}

		/**
		 * Returns a copy of the npc entry list
		 */
		@Override
		public List<NPCEntryDialogue> getChildren() {
			final List<NPCEntryDialogue> list = new ArrayList<NPCEntryDialogue>(
					this.entryPointers.size());
			for (EntriesSyncStruct entry : this.entryPointers) {
				list.add(entry.getReference());
			}
			return list;
		}

		/**
		 * Resref for the dialog line since they don't actually have one
		 * 
		 * @see DIALOG_LINE_REF_REGEX
		 */
		@Override
		public String getTemplateID() {
			return resRef + "#" + PLAYER_REPLY_LIST + "#" + index;
		}

		@Override
		public String getTag() {
			return resRef;
		}
	}

	/**
	 * An NPC Entry Dialog Struct contained in the NPC EntryList. It contains
	 * all of the Fields found in a Dialog Struct as detailed in Table 2.2.1,
	 * plus those Fields listed in Table 2.2.3 of the Conversation
	 * documentation.
	 * 
	 * @author mfchurch
	 * 
	 */
	public class NPCEntryDialogue extends DialogueLine {
		// List of Sync Structs describing the list of possible Player
		// replies to this line of NPC dialog.
		// Struct ID = list index.
		private List<RepliesSyncStruct> replyPointers;

		public final static String NPC_ENTRY_LIST = "entrylist";

		public NPCEntryDialogue(GffStruct struct) {
			super(struct);
		}

		/**
		 * Triggers all of the replyList Pointers to push their Active and Index
		 * values to what they are referencing. This should be done _after_ the
		 * EntryList and ReplyList have been read in, otherwise we cannot
		 * guarantee the index is valid.
		 */
		public void resolveReplies() {
			if (replyPointers == null)
				throw new IllegalStateException(
						"ReplyList has not been initialized yet!");
			for (RepliesSyncStruct reply : replyPointers) {
				reply.updateReference();
			}
		}

		@Override
		protected void build(GffStruct struct) {
			super.build(struct);

			replyPointers = new ArrayList<RepliesSyncStruct>();

			final List<GffField> fields = struct.getGffFields();

			for (GffField field : fields) {
				final String label = field.getLabel();

				// List of Sync Structs describing the list of possible
				// Player replies to this line of NPC dialog.
				if (label.equals("RepliesList")) {
					List<GffStruct> readList = field.getList();
					for (GffStruct aStruct : readList) {
						final RepliesSyncStruct sync = new RepliesSyncStruct(
								aStruct);
						replyPointers.add(sync);
					}
				}
				// label can also equal "Speaker", which may be important.
			}
		}

		@Override
		public String toString() {
			return "NPCEntryDialog [" + getName() + "]";
		}

		/**
		 * Returns a copy of the player replies list
		 */
		@Override
		public List<PlayerReplyDialogue> getChildren() {
			final List<PlayerReplyDialogue> list = new ArrayList<PlayerReplyDialogue>(
					this.replyPointers.size());
			for (RepliesSyncStruct reply : this.replyPointers) {
				list.add(reply.getReference());
			}
			return list;
		}

		/**
		 * Resref for the dialog line since they don't actually have one
		 * 
		 * @see DIALOG_LINE_REF_REGEX
		 */
		@Override
		public String getTemplateID() {
			return resRef + "#" + NPC_ENTRY_LIST + "#" + index;
		}

		@Override
		public String getTag() {
			return resRef;
		}
	}

	@Override
	public String getTag() {
		return resRef;
	}
}
