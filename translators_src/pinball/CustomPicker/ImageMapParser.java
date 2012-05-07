package translators.Pinball.CustomPicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import scriptease.gui.SETree.cell.BindingWidget;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import translators.Pinball.LOTRPBGameObject;

public class ImageMapParser {

	private List<BindingWidget> labels;

	public ImageMapParser(File mapFile, String desiredObject,
			String objectType) {
		InputStreamReader reader;
		InputStream stream = null;
		labels = new ArrayList<BindingWidget>();

		// Open the file
		try {
			stream = new FileInputStream(mapFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = new InputStreamReader(stream);
		BufferedReader breader = new BufferedReader(reader);
		String line = null;
		try {
			line = breader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Button Parser
		while (line != null) {
			// Parse the coords
			int coordStart = line.indexOf("coords=");
			if (coordStart == -1) {
				try {
					line = breader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			coordStart = line.indexOf("\"", coordStart) + 1;

			int coordEnd = line.indexOf("\"", coordStart);
			String coordString = line.substring(coordStart, coordEnd);
			String[] coordSplit = coordString.split(",");
			int xSize = Integer.valueOf(coordSplit[2])
			- Integer.valueOf(coordSplit[0]);
			int ySize = Integer.valueOf(coordSplit[3])
			- Integer.valueOf(coordSplit[1]);

			// Parse the name
			int nameStart = line.indexOf("\"",
					line.indexOf("title=\"", coordEnd + 1)) + 1;
			int nameEnd = line.indexOf("\"", nameStart + 1);
			String name = line.substring(nameStart, nameEnd);

			// if the desiredObject string is empty, then we will show all
			// widgets.
			// otherwise, we only show the widget that matches
			// desiredObject.
			if (desiredObject.isEmpty() || desiredObject.equals(name)) {
				
				// Build the typeList for the game object.
				ArrayList<String> typeList = new ArrayList<String>(1);
				typeList.add(objectType);
				
				// Create a corresponding game object
				KnowItBindingConstant gameObject = new KnowItBindingConstant(new LOTRPBGameObject(name, typeList));

				// Create the BindingWidget
				BindingWidget label = new BindingWidget(gameObject);
				label.setSize(xSize, ySize);
				label.setLocation(Integer.valueOf(coordSplit[0]),
						Integer.valueOf(coordSplit[1]));

				// Store the BindingWidget
				this.labels.add(label);

				if (desiredObject.equals(name))
					break;
			}

			try {
				line = breader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Cleanup
		try {
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<BindingWidget> getLabels() {
		return this.labels;
	}
}