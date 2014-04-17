package scriptease.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.Manifest;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;

/**
 * Collection of simple File reading and writing routines that don't really
 * belong anywhere. These exist to facilitate reading and writing to an already
 * open file; requests to open or close a file should go through FileManager or
 * be handled internally within a translator's GameModule.<br>
 * <br>
 * Imported from SE1 by remiller.
 * 
 * @author Unknown
 * @author remiller
 * @author graves
 * @author jyuen
 */
public class FileOp {

	/**
	 * Gets the file extension (.xxx) from the given File object's path name,
	 * not including the ".".<br>
	 * <br>
	 * For example, calling <code>getExtension</code> on a file named
	 * "myFile.txt" would yield <code>"txt"</code>.<br>
	 * <br>
	 * If the supplied File has no file extension, then an empty string is
	 * returned.
	 * 
	 * @param target
	 *            The file whose extension will be determined.
	 * @return The file's extension, not including the ".".
	 */
	public static String getExtension(File target) {
		String[] pathParts = target.getName().split("\\.");

		if (pathParts.length > 1)
			return pathParts[pathParts.length - 1];
		else
			return "";
	}

	/**
	 * Removes the file extension (.xxx) from the given File object's path name
	 * and the new file path is returned. The extension notation character (".")
	 * is not included in the result. <br>
	 * <br>
	 * For example, calling <code>removeExtension</code> on a file named
	 * "myFile.txt" would yield a file named "myFile".<br>
	 * <br>
	 * If the supplied File has no file extension, then a copy of the original
	 * file is returned.
	 * 
	 * @param target
	 *            The path to alter.
	 * @return A duplicate File object that lacks the original's file extension.
	 * @see FileOp#removeExtension(String)
	 * @see FileOp#replaceExtension(File, String)
	 */
	public static File removeExtension(File target) {
		final File extensionFree;

		String fileName = target.getName();
		int dotLocation = fileName.lastIndexOf('.');

		if (dotLocation > 0) {
			fileName = fileName.substring(0, dotLocation);
		}

		extensionFree = new File(target.getParentFile(), fileName);

		return extensionFree;
	}

	/**
	 * Removes the file extension (.xxx) from the given File object's path name
	 * and the new file path is returned. The extension notation character (".")
	 * is not included in the result. <br>
	 * <br>
	 * For example, calling <code>removeExtension</code> on a name "myFile.txt"
	 * would yield a file name "myFile".<br>
	 * <br>
	 * If the supplied file name has no file extension, then a copy of the
	 * original file name is returned.
	 * 
	 * @param target
	 *            The path to alter.
	 * @return A duplicate string object that lacks the original's file
	 *         extension.
	 * @see FileOp#removeExtension(File)
	 * @see FileOp#replaceExtension(File, String)
	 */
	public static String removeExtension(String fileName) {
		int dotLocation = fileName.lastIndexOf('.');

		if (dotLocation > 0) {
			fileName = fileName.substring(0, dotLocation);
		}

		return new String(fileName);
	}

	/**
	 * Shorthand method to check if the file is not null and exists.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean exists(File file) {
		return file != null && file.exists();
	}

	/**
	 * Creates a new File with the file extension removed (.xxx) from the given
	 * File object's path name and replaced with the supplied extension. The new
	 * File is returned. <br>
	 * <br>
	 * For example, calling <code>replaceExtension</code> with a file named
	 * "myFile.txt" and the string <code>"ini"</code> as arguments would yield a
	 * new File named "myFile.ini".<br>
	 * <br>
	 * If the supplied File has no file extension, then the supplied extension
	 * is added. <br>
	 * <br>
	 * Calling <code>replaceExtension</code> with a new file extension of
	 * <code>""</code> or <code>null</code> has the exact same effect as
	 * {@link FileOp#removeExtension(File)}
	 * 
	 * @param target
	 *            The path to alter.
	 * @param newExtension
	 *            The new extension to set <code>target</code>'s path to use. If
	 *            this is <code>null</code> or <code>""</code>, then the
	 *            extension of <code>target</code> is just removed.
	 * 
	 * @return A duplicate File object that now ends with the supplied
	 *         extension.
	 * @see FileOp#removeExtension(File)
	 */
	public static File replaceExtension(File target, String newExtension) {
		return new File(target.getParentFile(), FileOp.removeExtension(target)
				.getName() + "." + newExtension);
	}

	/**
	 * Adds the supplied file extension (.xxx) to the given File object's path
	 * name. The new file path is returned. <br>
	 * <br>
	 * For example, calling <code>addExtension</code> with a file named
	 * "myFile.txt" and the string <code>"ini"</code> as arguments would yield a
	 * file named "myFile.txt.ini".<br>
	 * <br>
	 * If the supplied File has no file extension, then the supplied extension
	 * is added. <br>
	 * <br>
	 * Calling <code>addExtension</code> with a new file extension of
	 * <code>""</code> or <code>null</code> has no effect.
	 * 
	 * @param target
	 *            The path to alter.
	 * @param newExtension
	 *            The new extension to set <code>target</code>'s path to use. If
	 *            this is <code>null</code> or <code>""</code>, then the
	 *            extension of <code>target</code> is unchanged.
	 *            removeExtension(
	 * @return A duplicate File object that now ends with the supplied
	 *         extension.
	 */
	public static File addExtension(File target, String newExtension) {
		return new File(target.getParentFile(), target.getName() + "."
				+ newExtension);
	}

	private static final File resourcesDirectory = FileManager.getInstance()
			.createTempDirectory("ScriptEaseResources");

	/**
	 * Determines a file location that is within a JAR or just in the file
	 * system. The location string passed in should be the location that is
	 * expected if the file were not in the JAR. Be aware that this is only
	 * intended to retrieve <i>files</i> and <b>not</b> directories. <br>
	 * <br>
	 * If the file exists both in the file system and the JAR, then a pointer to
	 * the file system version is returned.<br>
	 * 
	 * @param location
	 *            location of the file to be retrieved. Cannot be
	 *            <code>null</code>.
	 * @return The a File object that represents the location for that file. Can
	 *         be <code>null</code> if the location is malformed, if it does not
	 *         exist, or if it cannot be located within the jar.
	 */
	public static File getFileResource(String location) {
		File locationFile;
		final FileOutputStream out;
		final InputStream resourceStream;

		if (location == null)
			throw new IllegalArgumentException(
					"getFileResource cannot get a null file path.");

		locationFile = new File(location);

		if (!locationFile.isAbsolute())
			locationFile = new File(".", locationFile.getPath());

		if (locationFile.exists())
			return locationFile;

		// cannot find as normal file, check if it from the jar
		resourceStream = ScriptEase.class.getResourceAsStream("/" + location);
		if (resourceStream == null) {
			System.err.println("Could not find resource " + location);

			// resource does not exist in JAR
			locationFile = null;
		} else {
			// resource was found in jar
			locationFile = new File(FileOp.resourcesDirectory, location);

			// has it been extracted yet?
			if (!locationFile.exists()) {
				try {
					locationFile.getParentFile().mkdirs();
					locationFile.createNewFile();
					out = new FileOutputStream(locationFile);
					// copy the file from JAR to disk
					out.write(FileOp.readStreamAsBytes(resourceStream));
					System.out.println("Extracted resource " + location
							+ " from JAR to to file system");
					out.close();
				} catch (IOException e) {
					Thread.getDefaultUncaughtExceptionHandler()
							.uncaughtException(Thread.currentThread(), e);
				}
			}
		}

		return locationFile;
	}

	/**
	 * Copies the contents of one file into another file.
	 * 
	 * @param source
	 *            The file to copy from.
	 * @param destination
	 *            The file to write to.
	 * @throws IOException
	 */
	public static void copyFile(File source, File destination)
			throws IOException {
		if (!destination.exists())
			destination.createNewFile();
		final FileInputStream src = new FileInputStream(source);
		final FileOutputStream dest = new FileOutputStream(destination);

		// Credit: http://javaalmanac.com/egs/java.nio/File2File.html
		final FileChannel srcChannel = src.getChannel();
		final FileChannel dstChannel = dest.getChannel();
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		
		src.close();
		dest.close();
		srcChannel.close();
		dstChannel.close();
	}

	/**
	 * Retrieves the part of a file path before the first instance of folder.
	 * 
	 * @param source
	 *            The file to copy from.
	 * @param folder
	 *            The folder that gets cut off.
	 */
	public static String getFileNameUpToIncluding(File source, String folder) {
		final String filepath = source.getAbsolutePath();

		// Check if the filepath even contains the desired cutoff string
		if (!filepath.contains(folder))
			throw new IllegalArgumentException(
					"The requested cut off folder does not exist in the file path");

		int cutoffIndex = filepath.indexOf(folder) + folder.length() + 1;

		return filepath.substring(cutoffIndex);
	}

	/**
	 * Retrieves the part of a file path after the first instance of excluded @param
	 * folder.
	 * 
	 * @param source
	 *            The file to copy from.
	 * @param folder
	 *            The folder that gets cut off.
	 */
	public static String getFileNameUpTo(File source, String folder) {
		final String filepath = source.getAbsolutePath();

		// Check if the filepath even contains the desired cutoff string
		if (!filepath.contains(folder))
			throw new IllegalArgumentException(
					"The requested cut off folder does not exist in the file path");

		int cutoffIndex = filepath.indexOf(folder) + folder.length() + 1;

		return filepath.substring(cutoffIndex);
	}

	/**
	 * Reads the given file into a byte array.
	 * 
	 * @param source
	 *            The file to read from.
	 * @return The byte array containing the bytes from the file.
	 * @throws IOException
	 */
	public static byte[] readFileAsBytes(File source) throws IOException {
		InputStream in = null;
		byte[] contents;
		try {
			in = new FileInputStream(source);

			contents = FileOp.readStreamAsBytes(in);
		} finally {
			if (in != null)
				in.close();
		}

		return contents;
	}

	/**
	 * Reads from an input stream into a byte array.
	 * 
	 * @param source
	 *            the input stream to read from.
	 * @return a byte array containing the data read from the input stream.
	 * @throws IOException
	 */
	public static byte[] readStreamAsBytes(InputStream source)
			throws IOException {
		if (source == null)
			return new byte[0];
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] contents;
		byte buffer[] = new byte[1024];
		int count;

		try {
			while ((count = source.read(buffer)) > 0)
				result.write(buffer, 0, count);
		} finally {
			source.close();
		}

		contents = result.toByteArray();

		result.close();

		return contents;
	}

	/**
	 * Reads a file into a String.
	 * 
	 * @param source
	 *            The file to read from.
	 * @return The string that represents the file's contents.
	 * @throws IOException
	 */
	public static String readFileAsString(File source) throws IOException {
		FileReader reader = new FileReader(source);
		return FileOp.readStreamAsString(reader);
	}

	private static String readStreamAsString(Reader source) throws IOException {
		StringBuffer content = new StringBuffer();
		char buffer[] = new char[1024];
		int count;

		try {
			while ((count = source.read(buffer)) > 0)
				content.append(buffer, 0, count);
		} finally {
			source.close();
		}

		return content.toString();
	}

	/**
	 * Validates the XML document found at <code>sourcePath</code> against the
	 * schema located at <code>schemaPath</code>. This is a ridiculously
	 * over-complicated process, thus the specialized method for it.
	 * 
	 * @throws FileNotFoundException
	 *             if the <code>sourcePath</code> or <code>schemaPath</code> do
	 *             not exist.
	 */
	public static boolean validateXML(File sourcePath, File schemaPath)
			throws FileNotFoundException {
		final SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		final Schema schema;
		final Source source;
		final Validator validator;

		if (!sourcePath.exists() || !schemaPath.exists())
			throw new FileNotFoundException(
					"Either source XML file or schema could not be located.");

		try {
			schema = schemaFactory.newSchema(schemaPath);

			validator = schema.newValidator();

			source = new StreamSource(sourcePath);
			validator.validate(source);
		} catch (SAXException e) {
			System.err.println("XML validation failure: \"" + e.getMessage()
					+ "\" for file " + sourcePath.getPath());
			return false;
		} catch (IOException e) {
			System.err.println("Validation I/O failure: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Gets the manifest file for the ScriptEase jar.
	 * 
	 * @return the ScriptEase manifest file, or <code>null</code> if this is
	 *         invoked when not in a jar.
	 */
	public static Manifest getScriptEaseManifest() {
		final Class<FileOp> clazz = FileOp.class;
		final String className = clazz.getSimpleName() + ".class";
		final String classPath = clazz.getResource(className).toString();
		final String manifestPath;
		Manifest manifest = null;

		// Abort if we're not in a jar.
		if (!classPath.startsWith("jar"))
			return null;

		manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
				+ "/META-INF/MANIFEST.MF";

		try {
			manifest = new Manifest(new URL(manifestPath).openStream());
		} catch (MalformedURLException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		} catch (IOException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		return manifest;
	}

	/**
	 * Recursively finds all files that match the FileFilter in the given
	 * directory and all subdirectories. This may includes the directory that is
	 * given, if it passes the filter. Order is not guaranteed, but each entry
	 * will be unique.
	 * 
	 * @param directory
	 *            The directory to search in.
	 * @param filter
	 *            The file filter to use. If <code>null</code>, then all files
	 *            will be returned.
	 * @return A collection of all files that match the filter.
	 */
	public static Collection<File> findFiles(File directory, FileFilter filter) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("File " + directory
					+ " is not a directory.");
		}

		final Collection<File> matchingFiles = new ArrayList<File>();

		// search subdirectories first
		for (File subdir : directory.listFiles()) {
			if (subdir.isDirectory())
				matchingFiles.addAll(findFiles(subdir, filter));
		}

		// add the files we haven't already gotten
		for (File file : directory.listFiles(filter)) {
			if (!matchingFiles.contains(file))
				matchingFiles.add(file);
		}

		// we need to check the directory specifically so that we don't miss the
		// root directory on the first recursion. - remiller
		if (filter.accept(directory) && !matchingFiles.contains(directory))
			matchingFiles.add(directory);

		return matchingFiles;
	}

	public static FileFilter createExtensionFilter(final String... extensions) {
		return new FileFilter() {

			@Override
			public boolean accept(File pathName) {
				final String name = pathName.getName().toLowerCase();

				for (String extension : extensions) {
					if (name.endsWith(extension.toLowerCase()))
						return true;
				}

				return false;
			}
		};
	}
}
