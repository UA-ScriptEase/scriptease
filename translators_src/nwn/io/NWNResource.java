package io;

import io.genericfileformat.GenericFileFormat;

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
	 * Builds a new NWN Resource with a provided GFF.
	 * 
	 * @param resRef
	 * @param fileType
	 * @param gff
	 */
	protected NWNResource(String resRef, short fileType, GenericFileFormat gff) {
		this(resRef, fileType, null, gff);
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
	protected NWNResource(String resRef, short fileType, byte[] data) {
		this(resRef, fileType, data, null);
	}

	/**
	 * Builds a new NWNResource with a provided GFF and data. This isn't
	 * currently used anywhere apart from this class, so if you do use it, make
	 * sure it works.
	 * 
	 * @param resRef
	 * @param fileType
	 * @param data
	 * @param gff
	 */
	private NWNResource(String resRef, short fileType, byte[] data,
			GenericFileFormat gff) {
		final int listElementSize;

		if (resRef.length() > ErfKey.RESREF_MAX_LENGTH)
			resRef.substring(0, ErfKey.RESREF_MAX_LENGTH);

		if (data != null)
			listElementSize = data.length;
		else
			listElementSize = 0;

		this.key = new ErfKey(resRef.toLowerCase(), fileType);

		this.resourceListEntry = new ResourceListElement(-1, listElementSize);

		this.byteData = data;
		this.gff = gff;

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
	 * @param offsetToResourceList
	 *            The offset to the Resource List segment.
	 * @param elementOffset
	 *            The offset to the intended location of the Resource List
	 *            entry, relative to the start of the ERF file.
	 * @param dataOffset
	 *            The offset to the resource data, relative to the start of the
	 *            ERF file.
	 * @throws IOException
	 * @return the number of bytes written in the data segment.
	 */
	protected long writeResourceListData(ScriptEaseFileAccess writer,
			long elementOffset, long dataLocation) throws IOException {
		long bytesWritten;

		// ByteData != null for data scriptease doesn't interpret
		if (this.byteData != null) {
			writer.seek(dataLocation);
			writer.write(this.byteData);
			bytesWritten = this.byteData.length;
		}
		// GFF is used for everything else.
		else if (this.gff != null) {
			bytesWritten = this.gff.write(writer, dataLocation);
		} else {
			throw new IllegalStateException("NWNResource has no data!");
		}

		// now that the data segment is in, update and write the resource
		// list entry
		this.resourceListEntry.setOffsetToResource((int) dataLocation);
		this.resourceListEntry.setResourceSize((int) bytesWritten);

		this.resourceListEntry.write(writer, elementOffset);

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
		final String resRef = this.getResRef();
		final short type = this.key.getResType();
		final boolean rightType;

		// TODO: Determine this based on a saved resref list in the the module's
		// associated story file. That way we're not guessing: we know exactly
		// which are generated.

		rightType = type == ErfKey.SCRIPT_COMPILED_TYPE
				|| type == ErfKey.SCRIPT_SOURCE_TYPE;

		return rightType && ErfFile.isScriptEaseGenerated(resRef);
	}

	/**
	 * @param resourceId
	 *            The resource ID number. This is equivalent to
	 *            <code>( ErfKeyFileLocation - OffSetToKeyList ) / entryCount</code>
	 *            or to its index in the resources list.
	 */
	protected void writeErfKey(ScriptEaseFileAccess writer, int resourceId)
			throws IOException {
		this.key.write(writer, resourceId);
	}

	/**
	 * @return Whether this resource is a GFF or not.
	 * @see {@link ErfKey#isGFF()}
	 */
	public boolean isGFF() {
		return this.key.isGFF();
	}

	/**
	 * Whether the resource is a journal GFF. If it's not even a GFF, this will
	 * return false.
	 * 
	 * @return
	 */
	public boolean isJournalGFF() {
		if (this.isGFF()) {
			System.out.println("DEBUG");
			
			final GenericFileFormat GFF = this.getGFF();
			return GFF.getFileType().trim()
					.equals(GenericFileFormat.TYPE_JOURNAL_BP);
		}

		return false;
	}

	/**
	 * Gets the refref for this resource.
	 * 
	 * @return The unique-by-category Resource Reference id for this resource.
	 * @see #getExtendedResRef()
	 */
	public String getResRef() {
		return this.key.getResRef();
	}

	/**
	 * Gets the resref of this GFF concatenated with its file extension. For
	 * example, the creature resref "watson" would return
	 * <code>watson.utc</code>. The returned string is in lower case. This is a
	 * completely unique identifier.
	 * 
	 * @return The resref with its file extension.
	 * @see #getResRef()
	 */
	protected String getExtendedResRef() {
		return (this.getResRef() + "." + this.key.getExtension()).toLowerCase();
	}

	/**
	 * Gets the file extension for this resource.
	 * 
	 * @return the appropriate file extension for this resource.
	 */
	private String getExtension() {
		return this.key.getExtension();
	}

	/**
	 * Not all GFFs have an Object Representation.
	 * 
	 * @return
	 */
	protected boolean generatesObject() {
		return this.isGFF() && this.getGFF().generatesObject();
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
