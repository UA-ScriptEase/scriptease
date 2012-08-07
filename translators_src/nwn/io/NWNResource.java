package io;

import java.io.IOException;
import java.io.OutputStream;

import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * This class doesn't directly represent anything in the BioWare documentation,
 * so don't look for it there.<br>
 * <br>
 * It's a wrapper class that is a sort of Chain of Responsibility pattern in
 * that it has an ErfKey and ResourceListElement pair that it forwards requests
 * to. Primarily, it's for your convenience, so you can treat all resources as a
 * unit, rather than the sum of parts that the documentation implies.
 * 
 * @author remiller
 * 
 */
public class NWNResource implements Comparable<NWNResource> {
	/**
	 * Stores the key from the Key List segment of the Erf. This ErfKey
	 * corresponds to the ResourceListElement stored in
	 * <code>this.resourceListEntry</code>
	 */
	private final ErfKey key;
	private final ResourceListElement resourceListEntry;

	// only one of these two will be null depending on the type of this
	// resource
	/**
	 * byteData is the contents of the file as bytes.
	 */
	private byte[] byteData;
	private final GenericFileFormat gff;

	/**
	 * Builds a NWNResource that contains the two bits of indexing information
	 * it needs to retrieve data from the file. Use this constructor for
	 * representing data already written to the file.
	 * 
	 * @param key
	 *            The ErfKey that matches <code>entry</code>
	 * @param entry
	 *            The entry that points to the location in the Data Segment that
	 *            this NWNREsource is proxy for.
	 * @throws IOException
	 * @see #ErfFile(String, ErfKey, ResourceListElement)
	 */
	protected NWNResource(ErfKey key, ResourceListElement entry,
			ScriptEaseFileAccess reader) throws IOException {
		final int offset;

		this.key = key;
		this.resourceListEntry = entry;

		offset = this.resourceListEntry.getOffsetToResource();

		if (this.key.isGFF()) {
			this.gff = new GenericFileFormat(this.key.getResRef(), reader,
					offset);
		} else {
			this.gff = null;
			reader.seek(offset);

			this.byteData = reader.readBytes(this.resourceListEntry
					.getResourceSize());
		}
	}

	/**
	 * Builds a new NWNResource that contains the two bits of indexing
	 * information required by the file format as well as the data it
	 * represents. Use this constructor for ScriptEase-generated data that is
	 * not a GFF.
	 * 
	 * @param resRef
	 *            The unique ResRef that represents this resource. The
	 *            constructor does not enforce the uniqueness; that
	 *            responsibility is left up to the caller.
	 * @param fileType
	 *            The type of this resource as per the ErfKey file type
	 *            constants.
	 * @param byteData
	 *            The byte sequence of the byteData as it will appear on disk.
	 * @throws IOException
	 */
	protected NWNResource(String resRef, int id, short fileType, byte[] data) {
		final ResourceListElement newEntry;
		final ErfKey newKey;

		// pear the name down to the max resref length if necessary
		if (resRef.length() > ErfKey.RESREF_MAX_LENGTH)
			resRef.substring(0, ErfKey.RESREF_MAX_LENGTH);

		// resRefs must be lower case
		resRef = resRef.toLowerCase();

		// the next resourceID is the same as the number of entries
		newKey = new ErfKey(resRef, id, fileType);
		newEntry = new ResourceListElement(-1, data.length);

		this.key = newKey;
		this.resourceListEntry = newEntry;
		this.byteData = data;
		this.gff = null;
	}

	/**
	 * Writes the raw byte data from the resource to the given stream.
	 * 
	 * @param stream
	 *            The stream to write to.
	 * 
	 * @throws IOException
	 *             if Billie Jean was Michael Jackson's lover.
	 * @throws IllegalStateException
	 *             if the resource this method is called on is a GFF.
	 * @see NWNResource#isGFF()
	 */
	protected void writeData(OutputStream stream) throws IOException {
		if (this.isGFF())
			throw new IllegalStateException(
					"Tried to get byte data from a Resource that is a GFF.");

		stream.write(this.byteData);
	}

	/**
	 * Calls write methods that are appropriate to the type of resource. For
	 * example, if it has a GFF, writer writes to the GFF. This method has the
	 * side-effect of writing the Resource List entry as well as its Resource
	 * Data chunk.
	 * 
	 * @param writer
	 *            The writer to write to.
	 * @param offsetToResourceData
	 *            The offset to the resource data segment.
	 * @param dataOffset
	 *            The offset within the resource data segment to write to.
	 * @throws IOException
	 * @return the number of bytes written in the data segment.
	 */
	protected long writeResourceListData(ScriptEaseFileAccess writer,
			long offsetToResourceList, long elementOffset,
			long offsetToResourceData, int dataOffset) throws IOException {
		final int offset = this.resourceListEntry.getOffsetToResource();
		long bytesWritten;

		// go to the data location and plop it there.
		writer.seek(offsetToResourceData + dataOffset);

		// ByteData != null for data scriptease doesn't interpret
		if (this.byteData != null) {
			writer.seek(offset);
			writer.write(this.byteData);
			bytesWritten = this.byteData.length;
		}
		// GFF is used for everything else.
		else if (this.gff != null) {
			bytesWritten = this.gff.write(writer, offset);
		} else {
			throw new IllegalStateException("NWNResource has no data!");
		}

		// now that the data segment is in, update and write the resource
		// list entry
		this.resourceListEntry.setOffsetToResource(dataOffset);
		this.resourceListEntry.setResourceSize((int) bytesWritten);

		this.resourceListEntry.write(writer, offsetToResourceList,
				elementOffset);

		return bytesWritten;
	}

	/**
	 * Gets this NWNResource's data as a GFF file.
	 * 
	 * @return GFF representation of this NWNResource's data.
	 * @throws IllegalStateException
	 *             if this is called on a Resource that is not listed as being a
	 *             GFF type by its ErfKey.
	 */
	public GenericFileFormat getGFF() {
		if (!this.isGFF())
			throw new IllegalStateException(
					"Tried to get the GFF for a resource " + this.getResRef()
							+ " that wasn't a GFF.");

		return this.gff;
	}

	/**
	 * Gets whether this resource is something that was generated by ScriptEase.
	 * 
	 * @return True if this resource is something that was generated by
	 *         ScriptEase.
	 */
	public boolean isScriptEaseGenerated() {
		return ErfFile.isScriptEaseGenerated(this.key.getResRef());
	}

	protected void writeErfKey(ScriptEaseFileAccess writer) throws IOException {
		this.key.write(writer);
	}

	/**
	 * Sets the resource id.
	 * 
	 * @param resID
	 */
	protected void setResID(int resID) {
		this.key.setResId(resID);
	}

	/**
	 * @return Whether this resource is a GFF or not.
	 * @see {@link ErfKey#isGFF()}
	 */
	public boolean isGFF() {
		return this.key.isGFF();
	}

	public String getResRef() {
		return this.key.getResRef();
	}

	/**
	 * Gets the file extension for this resource.
	 * 
	 * @return the appropriate file extension for this resource.
	 */
	private String getExtension() {
		return this.key.getExtension();
	}

	protected boolean ignorable() {
		return this.key.getResType() == ErfKey.AREA_GAME_INSTANCE_FILE_TYPE;
	}

	@Override
	public String toString() {
		return "NWN Resource[" + this.getResRef() + "." + this.getExtension()
				+ ", Type: " + this.key.getResType() + ", Offset;size: "
				+ this.resourceListEntry.getOffsetToResource() + ";"
				+ this.resourceListEntry.getResourceSize() + "]";
	}

	@Override
	public int compareTo(NWNResource other) {
		int comparison;
		String myResRef;
		String otherResRef;
		String myExtension;
		String otherExtension;

		// Upper case has a different ascii value. This can be important for
		// comparisons to underscores, etc.
		myResRef = this.getResRef().toUpperCase();
		otherResRef = other.getResRef().toUpperCase();

		comparison = myResRef.compareTo(otherResRef);

		if (comparison == 0) {
			myExtension = this.getExtension().trim();
			otherExtension = other.getExtension().trim();

			comparison = myExtension.compareTo(otherExtension);
		}

		return comparison;
	}
}
