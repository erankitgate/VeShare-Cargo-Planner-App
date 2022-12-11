package clpBasic.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Class that defines methods for creating output JSON file for
 *         visualization purpose
 */
public class JSONHandler {

	public String createJSON(Container container, List<Consignment> boxes, int bestOrientation,
			int bestRelativeIteration) throws JSONException, IOException, URISyntaxException, NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		String message;
		JSONObject json = new JSONObject();

		// To make JSON object ordered!
		Field changeMap = json.getClass().getDeclaredField("map");
		changeMap.setAccessible(true);
		changeMap.set(json, new LinkedHashMap<>());
		changeMap.setAccessible(false);

		json.put("container_id", container.getID());
		json.put("container_length", container.getLength());
		json.put("container_width", container.getWidth());
		json.put("container_height", container.getHeight());
		json.put("container_tonnage", container.getTonnage());
		json.put("EBFT_best_orientation", bestOrientation);
		json.put("EBFT_best_relative_iteration", bestRelativeIteration);

		if (!boxes.isEmpty()) {
			if (Objects.isNull(boxes.get(0).getColor()) || boxes.get(0).getColor().isEmpty()) {
				HashMap<Integer, String> hashedPriorityBoxes = getColorsByPriority(boxes);

				for (Consignment box : boxes) {
					box.setColor(hashedPriorityBoxes.get(box.getPriority()));
				}
			}
		}

		JSONArray array = new JSONArray();
		for (int i = 0; i < boxes.size(); i++) {
			JSONObject item = new JSONObject();

			changeMap = item.getClass().getDeclaredField("map");
			changeMap.setAccessible(true);
			changeMap.set(item, new LinkedHashMap<>());
			changeMap.setAccessible(false);

			Consignment box = boxes.get(i);
			item.put("id", box.getBoxID());
			item.put("dim1", box.getLength());
			item.put("dim2", box.getWidth());
			item.put("dim3", box.getHeight());
			item.put("length", box.getPackX());
			item.put("width", box.getPackY());
			item.put("height", box.getPackZ());
			if (box.getPackZ() == box.getLength() && box.getLength_upright_allowed()) {
				if (box.getPackY() == box.getWidth()) {
					item.put("orientation", "width-base, length-up");
				} else {
					item.put("orientation", "height-base, length-up");
				}
			} else if (box.getPackZ() == box.getWidth() && box.getWidth_upright_allowed()) {
				if (box.getPackY() == box.getLength()) {
					item.put("orientation", "length-base, width-up");
				} else {
					item.put("orientation", "height-base, width-up");
				}
			} else if (box.getPackZ() == box.getHeight() && box.getHeight_upright_allowed()) {
				if (box.getPackY() == box.getLength()) {
					item.put("orientation", "length-base, height-up");
				} else {
					item.put("orientation", "width-base, height-up");
				}
			}
			item.put("weight", box.getWeight());
			item.put("x", box.getCOX());
			item.put("y", box.getCOY());
			item.put("z", box.getCOZ());
			item.put("color", box.getColor());
			// item.put("color",cargoColorArray[DataGenerator.generate(0,
			// cargoColorArray.length-1)]);
			item.put("wall", box.getWall());
			item.put("row", box.getLayer());
			item.put("column", box.getPositionInLayer());
			item.put("priority", box.getPriority());

			JSONObject obj = (JSONObject) item;
			array.put(obj);
		}

		json.put("data", array);

		message = json.toString();

		// System.out.println(message);

		return message;
	}

	private static HashMap<Integer, String> getColorsByPriority(List<Consignment> boxes) {
		int i = 0;
		HashMap<Integer, String> result = new HashMap<Integer, String>();

		for (Consignment box : boxes) {
			String l = result.get(box.getPriority());
			// System.out.println(String.format("%d: %s",box.getPriority(), l));
			if (l == null) {
				l = StaticMemory.colors[i++];
				result.put(box.getPriority(), l);
			}
		}

		return result;
	}

	public boolean createJSONFile(String fileFullPath, String message) throws IOException {
		boolean alreadyExists;
		System.out.println(fileFullPath);
		File myObj = new File(fileFullPath);
		if (myObj.createNewFile()) {
			System.out.println("File created: " + myObj.getName());
			alreadyExists = false;
		} else {
			System.out.println("File " + myObj.getName() + " already exists.");
			alreadyExists = true;
		}
		FileWriter myWriter = new FileWriter(fileFullPath);
		myWriter.write(message);
		myWriter.close();
		System.out.println("JSON File Created Successfully!.");
		return alreadyExists;
	}
}
