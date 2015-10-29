package io;

import io.HackEBotFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.Resource;
import scriptease.util.StringOp;

/**
 * This class represents a Hack-E-Bot. When a Script is created, it puts
 * in a text file to Arduino *Eedit* 
 * 
 * @author ehill
 * 
 */
public class HackEBotScript {

	private static final int NAME_RADIX = 36;
	private static final String SCRIPT_EXTENSION = ".txt";

	private final String code;
	private final String fileName;

	private final HackEBotResource botFile;

	// Added in front of the script name to prevent duplicate identical names
	// from occurring.
	private static int scriptCounter = 0;

	/**
	 * Creates a new Hack-E-Bot Script file from the script info and attaches it to
	 * the passed in Hack-E-Bot resource file. 
	 * 
	 * @param scriptInfo
	 * @param unityFile
	 */
	public HackEBotScript(final ScriptInfo scriptInfo, final HackEBotResource botFile) {
		final Resource subject;

		subject = scriptInfo.getSubject();

		this.botFile = botFile;
		this.code = scriptInfo.getCode();
		this.fileName = HackEBotFile.SCRIPTEASE_FILE_PREFIX
				+ Integer.toString(scriptCounter++, NAME_RADIX) + "_"
				+ StringOp.makeAlphaNumeric(subject.getName());

		//this.addToUnityFile();
		//potentially change for hackebot eedit recopy addToUnityFile from Unity iff....
	}

	/**
	 * Writes the script file and meta file to the passed in directory.
	 * 
	 * @param directory
	 */
	public void write(File directory) throws IOException {
		final File scriptFile;
		final BufferedWriter scriptWriter;
		final BufferedWriter writer; 
		
		writer = new BufferedWriter(new FileWriter(new File(directory, "Errors.txt")));
		writer.write(directory.toString());
		writer.close();
		
		scriptFile = new File(directory, this.fileName + SCRIPT_EXTENSION);
		scriptWriter = new BufferedWriter(new FileWriter(scriptFile));
		scriptWriter.write(this.code);
		scriptWriter.close();
	}

	/**
	 * Reset the script counter.
	 */
	public static void resetScriptCounter() {
		scriptCounter = 0;
	}
}
