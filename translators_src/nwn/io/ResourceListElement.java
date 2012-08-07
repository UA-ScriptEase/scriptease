package io;

import java.io.IOException;

import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * This is simply indexing information into the Resource Data block in the input
 * file. Think of it as a pointer from C if that helps. <br>
 * <br>
 * The name is misleading, but you can blame Bioware for that. I just wanted to
 * be consistent with their documentation. - jtduncan
 * 
 * @author jtduncan
 * @author remiller
 */
public class ResourceListElement {
	/**
	 * The size of any ResourceListElement in bytes
	 */
	public static final short BYTE_LENGTH = 8;

	private int offsetToResource;
	private int resourceSize;

	/**
	 * Builds a ResourceListElement by reading from the file.
	 * 
	 * @param fileAccess
	 *            The file to read from.
	 * @throws IOException
	 */
	public ResourceListElement(ScriptEaseFileAccess reader) throws IOException {
		this(reader.readInt(true), reader.readInt(true));
	}

	/**
	 * Writes a ResourceListElement to disk.
	 * 
	 * @param offset
	 *            The offset from the start of the ErfFile to this resource's
	 *            data.
	 * @param size
	 *            The size of this resource.
	 */
	public ResourceListElement(int offset, int size) {
		this.offsetToResource = offset;
		this.resourceSize = size;
	}

	/**
	 * @param writer
	 *            The file to write to.
	 * @param elementOffset
	 * 
	 * @throws IOException
	 */
	protected void write(ScriptEaseFileAccess writer, long elementOffset)
			throws IOException {
		// go to the resource list element's location and write it.
		writer.seek(elementOffset);

		writer.writeInt(this.offsetToResource, true);
		writer.writeInt(this.resourceSize, true);
	}

	/**
	 * Get the offset into the Resource Data segment relative to the start of
	 * the ERF file.
	 * 
	 * @return Offset into the Resource Data segment
	 */
	public int getOffsetToResource() {
		return this.offsetToResource;
	}

	/**
	 * Updates the offset into the Resource Data segment (relative to the start
	 * of the ERF file) to the given value. Only use this when saving the entire
	 * Erf.
	 * 
	 * @return Offset into the Resource Data segment
	 */
	protected void setOffsetToResource(int offset) {
		this.offsetToResource = offset;
	}

	/**
	 * Gets the resource size.
	 * 
	 * @return The resource size.
	 */
	protected int getResourceSize() {
		return this.resourceSize;
	}

	protected void setResourceSize(int newSize) {
		this.resourceSize = newSize;
	}
}
