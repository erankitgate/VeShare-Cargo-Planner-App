package clpBasic.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.opencsv.CSVWriter;

import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;
import clpBasic.utilities.StaticMemory.STD_BOX_NAME;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Class for random test data generation
 */
public class DataGenerator {
	static String NULL = "NULL", NAVY = "NAVY";
	static int uniqueId = 1;

	static String[] containerHeader = new String[] { "unique_container_id", "operator_id", "carrier_id", "roof",
			"roof_material", "type_of_cargo_supported", "external_length", "external_width", "external_height",
			"internal_length", "internal_width", "internal_height", "overhang_length", "overhang_width",
			"overhang_height", "material", "tare_weight", "tonnage", "container_description" };
	static String[] consignmentMovementHeader = new String[] { "unique_consignment_id", "sequence_hop",
			"start_coordinate_id", "end_coordinate_id", "start_address_id", "end_address_id", "x", "y", "z", "status",
			"pickup_timestamp", "dropoff_timestamp", "wall", "layer", "position_in_layer", "container_id", "priority" };
	static String[] consignmentsHeaderNew = new String[] { "Unique_consignment_id", "Date_added",
			"Delivery_deadline_date", "Goods_type", "Goods_quantity", "Shipment_charges", "Max_temperature",
			"Min_temperature", "Special_handling", "Packaging_type", "Pickup_time_window_start",
			"Pickup_time_window_end", "Dropoff_time_window_start", "Dropoff_time_window_end", "Pickup_coordinate_id",
			"Dropoff_coordinate_id", "Pickup_address_id", "Dropoff_address_id", "Current_operator_id", "Status",
			"Pickup_timestamp", "Delivered_timestamp", "Box_description", "length", "width", "height", "weight",
			"is_floor_only", "length_upright_allowed", "width_upright_allowed", "height_upright_allowed",
			"is_stackable", "stack_weight_length_upright", "stack_weight_width_upright", "stack_weight_height_upright",
			"color" };

	/**
	 * Method that returns particular type of container
	 * 
	 * @param id         - Container ID
	 * @param type_index - Index of container type
	 * @return
	 */
	private static Container getContainer(int id, int type_index) {
		Container container = null;
		switch (type_index) {
		case 1:
			container = new Container(id, "20ft Standard", 590, 235, 239, 2300, 25000);
			break;
		case 2:
			container = new Container(id, "40ft Standard", 1203, 240, 239, 3750, 27600);
			break;
		case 3:
			container = new Container(id, "40ft High Cube", 1202, 235, 270, 3900, 28600);
			break;
		case 4:
			container = new Container(id, "45ft High Cube", 1355, 235, 270, 4800, 27700);
			break;
		case 5:
			container = new Container(id, "3t", 203, 118, 217, 0, 2177);
			break;
		}
		return container;
	}

	/**
	 * Method that returns particular type of box
	 * 
	 * @param id         - Box ID
	 * @param type_index - Index of box type
	 * @return
	 */
	private static Consignment getConsignment(int id, int type_index) {
		StandardBox StdBox = StaticMemory.StandardBoxes[type_index];
		return new Consignment(id, StdBox.getDescription(), StdBox.getLength(), StdBox.getWidth(), StdBox.getHeight(),
				StdBox.getWeight(), 0, 1, 1, 1, 1, StdBox.getStack_weight_length_upright(),
				StdBox.getStack_weight_width_upright(), StdBox.getStack_weight_height_upright(), NAVY);
	}

	/**
	 * Method that generates random test data
	 * 
	 * @param numContainers - #Containers for which data is to be generated
	 * @throws IOException
	 */
	public static int generateRandomData(String outputDirectoryPath, String pickupDate, int numContainers,
			int startContainerID, int startBoxID, int numPriorityValues) throws IOException {
		uniqueId = startBoxID;
		int CONT_TYPE_COUNT = 4;
		String containersFilePath = outputDirectoryPath
				+ String.format("Containers [%d containers, %d priorities].csv", numContainers, numPriorityValues);
		String consignmentsFilePath = outputDirectoryPath
				+ String.format("Consignments [%d containers, %d priorities].csv", numContainers, numPriorityValues);
		String consignmentMovementFilePath = outputDirectoryPath + String
				.format("ConsignmentMovement [%d containers, %d priorities].csv", numContainers, numPriorityValues);

		// First create file object for file placed at location specified by file path
		File file = new File(consignmentMovementFilePath);
		Files.deleteIfExists(file.toPath());
		file = new File(consignmentsFilePath);
		Files.deleteIfExists(file.toPath());
		file = new File(containersFilePath);
		Files.deleteIfExists(file.toPath());

		// create FileWriter object with file as parameter
		FileWriter outputfile = new FileWriter(file);

		// create CSVWriter object file writer object as parameter
		CSVWriter writer = new CSVWriter(outputfile, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		// create a List which contains String array
		List<String[]> containerData = new ArrayList<String[]>();
		containerData.add(containerHeader);

		for (int id = startContainerID; id < startContainerID + numContainers; id++) {
			Container container = getContainer(id, getRandomIntInRange(1, CONT_TYPE_COUNT));
			generateConsignments(container, consignmentsFilePath, consignmentMovementFilePath, numPriorityValues,
					pickupDate);
			containerData.add(new String[] { Integer.toString(id), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
					Integer.toString(container.getLength()), Integer.toString(container.getWidth()),
					Integer.toString(container.getHeight()), NULL, NULL, NULL, NULL,
					Integer.toString(container.getTareWeight()), Integer.toString(container.getTonnage() / 1000),
					container.getDescription() });
		}
		writer.writeAll(containerData);

		// closing writer connection
		writer.close();

		System.out.println("Successfully created " + containersFilePath);
		System.out.println("Total boxes added: " + (uniqueId - startBoxID));
		return uniqueId - startBoxID;
	}

	/**
	 * Method to generate random orders for a given container
	 * 
	 * @param container
	 * @return #orders generated
	 * @throws IOException
	 */
	private static void generateConsignments(Container container, String consignmentsFilePath,
			String consignmentMovementFilePath, int numPriorityValues, String pickupTimeStamp) throws IOException {
		int BOX_TYPE_COUNT = 18;

		// First create file object for file placed at location specified by filepath
		File consignmentsFile = new File(consignmentsFilePath);
		File consignmentMovementFile = new File(consignmentMovementFilePath);

		// Create a List which contains String array
		List<String[]> consignmentData = new ArrayList<String[]>();
		List<String[]> consignmentMovementData = new ArrayList<String[]>();

		// Create FileWriter object with file as parameter
		FileWriter outputConsignmentMovementFile = null, outputConsignmentsFile = null;

		if (consignmentMovementFile.isFile()) {
			System.out.println("ConsignmentMovement file already exists, appending new consignmentMovements");
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile, true);

			if (consignmentsFile.isFile()) {
				System.out.println("Consignments file already exists, appending new consignments");
				outputConsignmentsFile = new FileWriter(consignmentsFile, true);
			}
		} else {
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile);
			outputConsignmentsFile = new FileWriter(consignmentsFile);

			consignmentMovementData.add(consignmentMovementHeader);
			consignmentData.add(consignmentsHeaderNew);
		}

		// create CSVWriter object file writer object as parameter
		CSVWriter consignmentMovementWriter = new CSVWriter(outputConsignmentMovementFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		CSVWriter consignmentsWriter = new CSVWriter(outputConsignmentsFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		int container_id = container.getID(), consignmentMovement_count = 0;
		int containerVolume = container.getVolume(), containerWeight = container.getTonnage();
		int loadVolume = 0, loadWeight = 0;

		while (true) {
			Consignment box = getConsignment(uniqueId, getRandomIntInRange(0, BOX_TYPE_COUNT - 1));
			loadVolume += box.getVolume();
			loadWeight += box.getWeight();

			consignmentMovement_count++;

			int priority = getRandomIntInRange(1, numPriorityValues);
			String color = StaticMemory.colors[priority - 1];
			consignmentMovementData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, pickupTimeStamp, NULL, NULL, NULL, NULL, Integer.toString(container_id),
					Integer.toString(priority) });

			consignmentData.add(new String[] { Integer.toString(box.getBoxID()), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, pickupTimeStamp, NULL,
					box.getBoxDescription(), Integer.toString(box.getLength()), Integer.toString(box.getWidth()),
					Integer.toString(box.getHeight()), Integer.toString(box.getWeight()),
					Boolean.toString(box.getIsFloorOnly()), Boolean.toString(box.getLength_upright_allowed()),
					Boolean.toString(box.getWidth_upright_allowed()), Boolean.toString(box.getHeight_upright_allowed()),
					Boolean.toString(box.getIsStackable()), Integer.toString(box.getStack_weight_length_upright()),
					Integer.toString(box.getStack_weight_width_upright()),
					Integer.toString(box.getStack_weight_height_upright()), color });

			uniqueId++;
			if (loadVolume > containerVolume || loadWeight > containerWeight)
				break;
		}

		consignmentMovementWriter.writeAll(consignmentMovementData);
		consignmentsWriter.writeAll(consignmentData);

		// closing writer connection
		consignmentMovementWriter.close();
		consignmentsWriter.close();

		System.out.println(
				String.format("Successfully added %d consignments in %s\nVolume = %d/%d = %.3f\nWeight = %d/%d = %.3f",
						consignmentMovement_count, consignmentMovementFilePath, loadVolume, containerVolume,
						loadVolume * 100.0 / containerVolume, loadWeight, containerWeight,
						loadWeight * 100.0 / containerWeight));
	}

	public static void generateBRData(String inputBRDataDirectoryPath, String pickupTimeStamp, boolean split)
			throws IOException {
		// final Path path = Paths.get(System.getProperty("user.dir"), "db\\BR Raw
		// Data");

		final Path path = Paths.get(inputBRDataDirectoryPath);

		for (int k = 1; k <= 7; k++) {
			final Path txt = path.resolve("thpack" + k + ".txt");

			// create a List which contains String array
			List<String[]> containerData = new ArrayList<String[]>();
			containerData.add(containerHeader);

			List<String[]> consignmentMovementData = new ArrayList<String[]>();
			List<String[]> consignmentData = new ArrayList<String[]>();

			if (!split) {
				consignmentMovementData.add(consignmentMovementHeader);
				consignmentData.add(consignmentsHeaderNew);
			}

			final Scanner scanner = new Scanner(Files.newBufferedReader(txt, Charset.defaultCharset()));

			int setCount = Integer.parseInt(scanner.nextLine().replaceAll("\\s+", ""));
			System.out.println(String.format("Generating %d sets", setCount));
			int boxTypeCount = 0;

			String[] splited;
			for (int i = (k - 1) * 100 + 1; i <= (k - 1) * 100 + setCount; i++) {
				// Display.printSolidLine();
				splited = scanner.nextLine().split("\\s+");
				int containerID = Integer.parseInt(splited[1]);

				splited = scanner.nextLine().split("\\s+");
				int length = Integer.parseInt(splited[1]);
				int width = Integer.parseInt(splited[2]);
				int height = Integer.parseInt(splited[3]);
				// System.out.println(String.format("Container #%d\t%d x %d x %d", containerID,
				// length, width, height));

				containerData.add(new String[] { Integer.toString(i), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
						Integer.toString(length), Integer.toString(width), Integer.toString(height), NULL, NULL, NULL,
						NULL, Integer.toString(0), Integer.toString(0), String.format("BR#%d-%d", k, i) });

				if (split) {
					consignmentMovementData = new ArrayList<String[]>();
					consignmentData = new ArrayList<String[]>();

					consignmentMovementData.add(consignmentMovementHeader);
					consignmentData.add(consignmentsHeaderNew);
				}

				splited = scanner.nextLine().split("\\s+");
				boxTypeCount = Integer.parseInt(splited[1]);
				// System.out.println("boxTypeCount\t" + boxTypeCount);

				for (int j = 1; j <= boxTypeCount; j++) {
					splited = scanner.nextLine().split("\\s+");
					int boxID = Integer.parseInt(splited[1]);
					int boxLength = Integer.parseInt(splited[2]);
					int boxWidth = Integer.parseInt(splited[4]);
					int boxHeight = Integer.parseInt(splited[6]);
					int boxQuantity = Integer.parseInt(splited[8]);
					// System.out.println(String.format("Box #%d\t%d x %d x %d, Qty = %d", boxID,
					// boxLength, boxWidth, boxHeight, boxQuantity));

					for (int l = 0; l < boxQuantity; l++) {
						consignmentMovementData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL,
								NULL, NULL, NULL, NULL, NULL, pickupTimeStamp, NULL, NULL, NULL, NULL,
								Integer.toString(i), Integer.toString(j) });

						consignmentData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL, NULL,
								NULL, NULL, NULL, NULL, Integer.toString(boxLength), Integer.toString(boxWidth),
								Integer.toString(boxHeight), "0", "0", "1", "0", "STACK_TYPE_NONE", "1", "ROTATE_ANY",
								StaticMemory.colors[j - 1], NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
								NULL, NULL, String.format("BR#%d-%d - Box #%d", k, i, boxID) });

						uniqueId++;
					}
				}

				if (split) {
					final Path csvContainers = path
							.resolve(String.format("thpack%d_Containers_%d [%d priorities].csv", k, i, boxTypeCount));
					final Path csvConsignments = path
							.resolve(String.format("thpack%d_Consignments_%d [%d priorities].csv", k, i, boxTypeCount));
					final Path csvConsignmentMovement = path.resolve(
							String.format("thpack%d_ConsignmentMovement_%d [%d priorities].csv", k, i, boxTypeCount));
					Path[] paths = { csvContainers, csvConsignments, csvConsignmentMovement };

					List<List<String[]>> data = new ArrayList<List<String[]>>();
					data.add(containerData);
					data.add(consignmentData);
					data.add(consignmentMovementData);

					for (int l = 0; l < 3; l++) {
						File file = paths[l].toFile();
						Files.deleteIfExists(file.toPath());

						FileWriter outputFile = new FileWriter(file);
						CSVWriter writer = new CSVWriter(outputFile, CSVWriter.DEFAULT_SEPARATOR,
								CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
								CSVWriter.DEFAULT_LINE_END);
						writer.writeAll(data.get(l));

						writer.close();

						System.out.println("Successfully created " + file.toPath());
					}
				}
			}

			if (!split) {
				final Path csvContainers = path.resolve(String
						.format("thpack%d_Containers [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				final Path csvConsignments = path.resolve(String
						.format("thpack%d_Consignments [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				final Path csvConsignmentMovement = path.resolve(String.format(
						"thpack%d_ConsignmentMovement [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				Path[] paths = { csvContainers, csvConsignments, csvConsignmentMovement };

				List<List<String[]>> data = new ArrayList<List<String[]>>();
				data.add(containerData);
				data.add(consignmentData);
				data.add(consignmentMovementData);

				for (int l = 0; l < 3; l++) {
					File file = paths[l].toFile();
					Files.deleteIfExists(file.toPath());

					FileWriter outputFile = new FileWriter(file);
					CSVWriter writer = new CSVWriter(outputFile, CSVWriter.DEFAULT_SEPARATOR,
							CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
							CSVWriter.DEFAULT_LINE_END);
					writer.writeAll(data.get(l));

					writer.close();

					System.out.println("Successfully created " + file.toPath());
				}
			}
		}
	}

	public static void generateBRDataWithWeights(String inputBRDataDirectoryPath, String pickupTimeStamp, boolean split)
			throws IOException {
		// final Path path = Paths.get(System.getProperty("user.dir"), "db\\BR Raw
		// Data");

		final Path path = Paths.get(inputBRDataDirectoryPath);

		for (int k = 1; k <= 7; k++) {
			final Path txt = path.resolve("wtpack" + k + ".txt");

			// create a List which contains String array
			List<String[]> containerData = new ArrayList<String[]>();
			containerData.add(containerHeader);

			List<String[]> consignmentMovementData = new ArrayList<String[]>();
			List<String[]> consignmentData = new ArrayList<String[]>();

			if (!split) {
				consignmentMovementData.add(consignmentMovementHeader);
				consignmentData.add(consignmentsHeaderNew);
				// System.out.println("Columns:"+consignmentsWithWeightsHeader.length);
			}

			final Scanner scanner = new Scanner(Files.newBufferedReader(txt, Charset.defaultCharset()));

			// int setCount = Integer.parseInt(scanner.nextLine().replaceAll("\\s+", ""));
			int setCount = 100;
			System.out.println(String.format("Generating %d sets", setCount));
			int boxTypeCount = 0;

			String[] splited;
			for (int i = (k - 1) * 100 + 1; i <= (k - 1) * 100 + setCount; i++) {
				// Display.printSolidLine();

				int containerID = i;

				splited = scanner.nextLine().split("\\s+");
				int length = Integer.parseInt(splited[1]);
				int width = Integer.parseInt(splited[2]);
				int height = Integer.parseInt(splited[3]);
				int tonnage = (int) (1.5 * length * width * height / 1000);
				System.out.println(String.format("Container #%d\t%d x %d x %d, Tonnage = %d", containerID, length,
						width, height, tonnage));

				containerData.add(new String[] { Integer.toString(i), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
						Integer.toString(length), Integer.toString(width), Integer.toString(height), NULL, NULL, NULL,
						NULL, Integer.toString(0), Integer.toString(tonnage), String.format("BR#%d-%d", k, i) });

				if (split) {
					consignmentMovementData = new ArrayList<String[]>();
					consignmentData = new ArrayList<String[]>();

					consignmentMovementData.add(consignmentMovementHeader);
					consignmentData.add(consignmentsHeaderNew);
				}

				splited = scanner.nextLine().split("\\s+");
				boxTypeCount = Integer.parseInt(splited[1]);
				System.out.println("boxTypeCount\t" + boxTypeCount);

				for (int j = 1; j <= boxTypeCount; j++) {
					splited = scanner.nextLine().split("\\s+");
					int boxID = j;
					int boxLength = Integer.parseInt(splited[1]);
					int verticalLengthAllowed = Integer.parseInt(splited[2]);
					//verticalLengthAllowed = 1;
					int boxWidth = Integer.parseInt(splited[3]);
					int verticalWidthAllowed = Integer.parseInt(splited[4]);
					//verticalWidthAllowed = 1;
					int boxHeight = Integer.parseInt(splited[5]);
					int verticalHeightAllowed = Integer.parseInt(splited[6]);
					//verticalHeightAllowed = 1;
					int boxQuantity = Integer.parseInt(splited[7]);
					int boxWeight = (int) (Float.parseFloat(splited[8]) * 1000);
					int verticalLengthStackWeight = (int) (1000 * Float.parseFloat(splited[9]) * boxWidth * boxHeight);
					int verticalWidthStackWeight = (int) (1000 * Float.parseFloat(splited[10]) * boxLength * boxHeight);
					int verticalHeightStackWeight = (int) (1000 * Float.parseFloat(splited[11]) * boxLength * boxWidth);
					System.out.println(
							String.format("Box #%d\t%d x %d x %d, Qty = %d, Weight = %d, Orientation = [%d,%d,%d]",
									boxID, boxLength, boxWidth, boxHeight, boxQuantity, boxWeight,
									verticalLengthAllowed, verticalWidthAllowed, verticalHeightAllowed));
					System.out.println(String.format("Weight = %d, StackWeight = [%d,%d,%d]\n", boxWeight,
							verticalLengthStackWeight, verticalWidthStackWeight, verticalHeightStackWeight));

					for (int l = 0; l < boxQuantity; l++) {
						consignmentMovementData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL,
								NULL, NULL, NULL, NULL, NULL, pickupTimeStamp, NULL, NULL, NULL, NULL,
								Integer.toString(i), Integer.toString(j) });

						consignmentData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL, NULL,
								NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
								NULL, NULL, String.format("BR#%d-%d - Box #%d", k, i, boxID),
								Integer.toString(boxLength), Integer.toString(boxWidth), Integer.toString(boxHeight),
								Integer.toString(boxWeight), "0", Integer.toString(verticalLengthAllowed),
								Integer.toString(verticalWidthAllowed), Integer.toString(verticalHeightAllowed), "1",
								Integer.toString(verticalLengthStackWeight), Integer.toString(verticalWidthStackWeight),
								Integer.toString(verticalHeightStackWeight), StaticMemory.colors[j - 1] });

						uniqueId++;
					}
				}

				if (split) {
					final Path csvContainers = path
							.resolve(String.format("wtpack%d_Containers_%d [%d priorities].csv", k, i, boxTypeCount));
					final Path csvConsignments = path
							.resolve(String.format("wtpack%d_Consignments_%d [%d priorities].csv", k, i, boxTypeCount));
					final Path csvConsignmentMovement = path.resolve(
							String.format("wtpack%d_ConsignmentMovement_%d [%d priorities].csv", k, i, boxTypeCount));
					Path[] paths = { csvContainers, csvConsignments, csvConsignmentMovement };

					List<List<String[]>> data = new ArrayList<List<String[]>>();
					data.add(containerData);
					data.add(consignmentData);
					data.add(consignmentMovementData);

					for (int l = 0; l < 3; l++) {
						File file = paths[l].toFile();
						Files.deleteIfExists(file.toPath());

						FileWriter outputFile = new FileWriter(file);
						CSVWriter writer = new CSVWriter(outputFile, CSVWriter.DEFAULT_SEPARATOR,
								CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
								CSVWriter.DEFAULT_LINE_END);
						writer.writeAll(data.get(l));

						writer.close();

						System.out.println("Successfully created " + file.toPath());
					}
				}
			}

			if (!split) {
				final Path csvContainers = path.resolve(String
						.format("wtpack%d_Containers [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				final Path csvConsignments = path.resolve(String
						.format("wtpack%d_Consignments [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				final Path csvConsignmentMovement = path.resolve(String.format(
						"wtpack%d_ConsignmentMovement [%d containers, %d priorities].csv", k, setCount, boxTypeCount));
				Path[] paths = { csvContainers, csvConsignments, csvConsignmentMovement };

				List<List<String[]>> data = new ArrayList<List<String[]>>();
				data.add(containerData);
				data.add(consignmentData);
				data.add(consignmentMovementData);

				for (int l = 0; l < 3; l++) {
					File file = paths[l].toFile();
					Files.deleteIfExists(file.toPath());

					FileWriter outputFile = new FileWriter(file);
					CSVWriter writer = new CSVWriter(outputFile, CSVWriter.DEFAULT_SEPARATOR,
							CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
							CSVWriter.DEFAULT_LINE_END);
					writer.writeAll(data.get(l));

					writer.close();

					System.out.println("Successfully created " + file.toPath());
				}
			}
		}
	}

	public static int generateRandomDataWithWeights(String outputDirectoryPath, String pickupDate, int numContainers,
			int startContainerID, int startBoxID, int gridSize, int numCities, int numCitiesOnPath,
			boolean percentSpecified, List<StdBoxPercent> stdBoxesPercentages) throws IOException {
		uniqueId = startBoxID;
		int CONT_TYPE_COUNT = 4;
		String containersFilePath = outputDirectoryPath
				+ String.format("ContainersW [%d containers, %d priorities].csv", numContainers, numCitiesOnPath - 1);
		String consignmentsFilePath = outputDirectoryPath
				+ String.format("ConsignmentsW [%d containers, %d priorities].csv", numContainers, numCitiesOnPath - 1);
		String consignmentMovementFilePath = outputDirectoryPath + String
				.format("ConsignmentMovementW [%d containers, %d priorities].csv", numContainers, numCitiesOnPath - 1);

		// First create file object for file placed at location specified by file path
		File file = new File(consignmentMovementFilePath);
		Files.deleteIfExists(file.toPath());
		file = new File(consignmentsFilePath);
		Files.deleteIfExists(file.toPath());
		file = new File(containersFilePath);
		Files.deleteIfExists(file.toPath());

		// create FileWriter object with file as parameter
		FileWriter outputfile = new FileWriter(file);

		// create CSVWriter object file writer object as parameter
		CSVWriter writer = new CSVWriter(outputfile, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		// create a List which contains String array
		List<String[]> containerData = new ArrayList<String[]>();
		containerData.add(containerHeader);

		for (int id = startContainerID; id < startContainerID + numContainers; id++) {
			Container container = getContainer(id, getRandomIntInRange(1, CONT_TYPE_COUNT));
			if (!percentSpecified) {
				generateConsignmentsWithWeights(container, consignmentsFilePath, consignmentMovementFilePath,
						pickupDate, gridSize, numCities, numCitiesOnPath);
			} else {
				generateConsignmentsWithWeightsAndGivenPercent(container, consignmentsFilePath,
						consignmentMovementFilePath, pickupDate, stdBoxesPercentages, gridSize, numCities,
						numCitiesOnPath);
			}
			containerData.add(new String[] { Integer.toString(id), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
					Integer.toString(container.getLength()), Integer.toString(container.getWidth()),
					Integer.toString(container.getHeight()), NULL, NULL, NULL, NULL,
					Integer.toString(container.getTareWeight()), Integer.toString(container.getTonnage() / 1000),
					container.getDescription() });
		}
		writer.writeAll(containerData);

		// closing writer connection
		writer.close();

		System.out.println("Successfully created " + containersFilePath);
		System.out.println("Total boxes added: " + (uniqueId - startBoxID));
		return uniqueId - startBoxID;
	}

	private static void generateConsignmentsWithWeights(Container container, String consignmentsFilePath,
			String consignmentMovementFilePath, String pickupTimeStamp, int gridSize, int numCities,
			int numCitiesOnPath) throws IOException {
		int BOX_TYPE_COUNT = 18;
		DistributedRandomNumberGenerator drngStdBox = new DistributedRandomNumberGenerator();
		Map<STD_BOX_NAME, Integer> boxCountMap = new LinkedHashMap<STD_BOX_NAME, Integer>();
		for (int i = 0; i < BOX_TYPE_COUNT; i++) {
			STD_BOX_NAME e = STD_BOX_NAME.values()[i];
			drngStdBox.addNumber(i, 100.0 / BOX_TYPE_COUNT);
			boxCountMap.put(e, 0);
		}

		// First create file object for file placed at location specified by filepath
		File consignmentsFile = new File(consignmentsFilePath);
		File consignmentMovementFile = new File(consignmentMovementFilePath);

		// Create a List which contains String array
		List<String[]> consignmentData = new ArrayList<String[]>();
		List<String[]> consignmentMovementData = new ArrayList<String[]>();

		// Create FileWriter object with file as parameter
		FileWriter outputConsignmentMovementFile = null, outputConsignmentsFile = null;

		if (consignmentMovementFile.isFile()) {
			System.out.println("ConsignmentMovementW file already exists, appending new consignmentMovements");
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile, true);

			if (consignmentsFile.isFile()) {
				System.out.println("ConsignmentsW file already exists, appending new consignments");
				outputConsignmentsFile = new FileWriter(consignmentsFile, true);
			}
		} else {
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile);
			outputConsignmentsFile = new FileWriter(consignmentsFile);

			consignmentMovementData.add(consignmentMovementHeader);
			consignmentData.add(consignmentsHeaderNew);
		}

		// create CSVWriter object file writer object as parameter
		CSVWriter consignmentMovementWriter = new CSVWriter(outputConsignmentMovementFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		CSVWriter consignmentsWriter = new CSVWriter(outputConsignmentsFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		int container_id = container.getID(), consignmentMovement_count = 0;
		int containerVolume = container.getVolume(), containerWeight = container.getTonnage();
		int loadVolume = 0, loadWeight = 0;

		RandomGeoMapper rgm = new RandomGeoMapper(gridSize);
		rgm.createRandomCitiesInGrid(numCities);
		List<City> randomPath = rgm.getRandomPath(numCitiesOnPath);

		Map<Integer, Integer> cityCountMap = new LinkedHashMap<Integer, Integer>();
		for (City e : randomPath) {
			int cityID = e.getCityID();
			if (cityID > 1) {
				cityCountMap.put(cityID, 0);
			}
		}

		while (true) {
			int randomStdBox = drngStdBox.getDistributedRandomNumber();
			Consignment box = getConsignment(uniqueId, randomStdBox);
			STD_BOX_NAME key = STD_BOX_NAME.values()[randomStdBox];
			boxCountMap.computeIfPresent(key, (k, v) -> v + 1); // boxCount.put(key, boxCount.get(key) + 1);
			loadVolume += box.getVolume();
			loadWeight += box.getWeight();

			consignmentMovement_count++;

			int randomCityPathIndex = getRandomIntInRange(1, randomPath.size() - 1);
			City randomDestinationCity = randomPath.get(randomCityPathIndex);
			int randomCityID = randomDestinationCity.getCityID();
			cityCountMap.computeIfPresent(randomCityID, (k, v) -> v + 1);
			int priority = randomDestinationCity.getPriorityOnPath();
			String color = StaticMemory.colors[priority - 1];
			consignmentMovementData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, pickupTimeStamp, NULL, NULL, NULL, NULL, Integer.toString(container_id),
					Integer.toString(priority) });

			int verticalLengthAllowed = 1;
			int verticalWidthAllowed = 1;
			int verticalHeightAllowed = 1;
			int isStackable = 1;

			consignmentData.add(new String[] { Integer.toString(box.getBoxID()), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
					box.getBoxDescription(), Integer.toString(box.getLength()), Integer.toString(box.getWidth()),
					Integer.toString(box.getHeight()), Integer.toString(box.getWeight()),
					box.getIsFloorOnly() ? "1" : "0", Integer.toString(verticalLengthAllowed),
					Integer.toString(verticalWidthAllowed), Integer.toString(verticalHeightAllowed),
					Integer.toString(isStackable), Integer.toString(box.getStack_weight_length_upright()),
					Integer.toString(box.getStack_weight_width_upright()),
					Integer.toString(box.getStack_weight_height_upright()), color });

			uniqueId++;
			if (loadVolume > containerVolume || loadWeight > containerWeight)
				break;
		}

		consignmentMovementWriter.writeAll(consignmentMovementData);
		consignmentsWriter.writeAll(consignmentData);

		// closing writer connection
		consignmentMovementWriter.close();
		consignmentsWriter.close();

		Display.printSolidLineShort();
		for (STD_BOX_NAME key : boxCountMap.keySet()) {
			int boxCount = boxCountMap.get(key);
			System.out.println(String.format("%-22s:\t%d\t%.2f%%", key.toString(), boxCount,
					boxCount * 100.0 / consignmentMovement_count));
		}
		Display.printSolidLineShort();
		for (Integer key : cityCountMap.keySet()) {
			int cityCount = cityCountMap.get(key);
			System.out.println(String.format("CityID: %s\t%d\t%.2f%%", key.toString(), cityCount,
					cityCount * 100.0 / consignmentMovement_count));
		}
		Display.printSolidLineShort();

		System.out.println(
				String.format("Successfully added %d consignments in %s\nVolume = %d/%d = %.3f\nWeight = %d/%d = %.3f",
						consignmentMovement_count, consignmentMovementFilePath, loadVolume, containerVolume,
						loadVolume * 100.0 / containerVolume, loadWeight, containerWeight,
						loadWeight * 100.0 / containerWeight));
	}

	private static void generateConsignmentsWithWeightsAndGivenPercent(Container container, String consignmentsFilePath,
			String consignmentMovementFilePath, String pickupTimeStamp, List<StdBoxPercent> stdBoxesPercentages,
			int gridSize, int numCities, int numCitiesOnPath) throws IOException {
		Display.printDashedLine();
		System.out.println("Generating Data using DistributedRandomNumberGenerator");

		DistributedRandomNumberGenerator drngStdBox = new DistributedRandomNumberGenerator();
		Map<STD_BOX_NAME, Integer> boxCountMap = new LinkedHashMap<STD_BOX_NAME, Integer>();
		for (StdBoxPercent e : stdBoxesPercentages) {
			drngStdBox.addNumber(e.getDescription().ordinal(), e.getPercentOfTotalBoxes());
			boxCountMap.put(e.getDescription(), 0);
		}

		// First create file object for file placed at location specified by filepath
		File consignmentsFile = new File(consignmentsFilePath);
		File consignmentMovementFile = new File(consignmentMovementFilePath);

		// Create a List which contains String array
		List<String[]> consignmentData = new ArrayList<String[]>();
		List<String[]> consignmentMovementData = new ArrayList<String[]>();

		// Create FileWriter object with file as parameter
		FileWriter outputConsignmentMovementFile = null, outputConsignmentsFile = null;

		if (consignmentMovementFile.isFile()) {
			System.out.println("ConsignmentMovementW file already exists, appending new consignmentMovements");
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile, true);

			if (consignmentsFile.isFile()) {
				System.out.println("ConsignmentsW file already exists, appending new consignments");
				outputConsignmentsFile = new FileWriter(consignmentsFile, true);
			}
		} else {
			outputConsignmentMovementFile = new FileWriter(consignmentMovementFile);
			outputConsignmentsFile = new FileWriter(consignmentsFile);

			consignmentMovementData.add(consignmentMovementHeader);
			consignmentData.add(consignmentsHeaderNew);
		}

		// create CSVWriter object file writer object as parameter
		CSVWriter consignmentMovementWriter = new CSVWriter(outputConsignmentMovementFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		CSVWriter consignmentsWriter = new CSVWriter(outputConsignmentsFile, CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

		int container_id = container.getID(), consignmentMovement_count = 0;
		int containerVolume = container.getVolume(), containerWeight = container.getTonnage();
		int loadVolume = 0, loadWeight = 0;

		RandomGeoMapper rgm = new RandomGeoMapper(gridSize);
		rgm.createRandomCitiesInGrid(numCities);
		List<City> randomPath = rgm.getRandomPath(numCitiesOnPath);

		Map<Integer, Integer> cityCountMap = new LinkedHashMap<Integer, Integer>();
		for (City e : randomPath) {
			int cityID = e.getCityID();
			if (cityID > 1) {
				cityCountMap.put(cityID, 0);
			}
		}

		while (true) {
			int randomStdBox = drngStdBox.getDistributedRandomNumber();
			Consignment box = getConsignment(uniqueId, randomStdBox);
			STD_BOX_NAME key = STD_BOX_NAME.values()[randomStdBox];
			boxCountMap.computeIfPresent(key, (k, v) -> v + 1); // boxCount.put(key, boxCount.get(key) + 1);
			loadVolume += box.getVolume();
			loadWeight += box.getWeight();

			consignmentMovement_count++;

			int randomCityPathIndex = getRandomIntInRange(1, randomPath.size() - 1);
			City randomDestinationCity = randomPath.get(randomCityPathIndex);
			int randomCityID = randomDestinationCity.getCityID();
			cityCountMap.computeIfPresent(randomCityID, (k, v) -> v + 1);
			int priority = randomDestinationCity.getPriorityOnPath();
			String color = StaticMemory.colors[priority - 1];
			consignmentMovementData.add(new String[] { Integer.toString(uniqueId), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, pickupTimeStamp, NULL, NULL, NULL, NULL, Integer.toString(container_id),
					Integer.toString(priority) });

			int verticalLengthAllowed = 1;
			int verticalWidthAllowed = 1;
			int verticalHeightAllowed = 1;
			int isStackable = 1;

			int stackWeight = 0;
			int boxWeight = box.getWeight();
			if (boxWeight <= 5000) {
				stackWeight = 6 * boxWeight;
			} else if (boxWeight <= 15000) {
				stackWeight = 4 * boxWeight;
			} else if (boxWeight <= 30000) {
				stackWeight = 3 * boxWeight;
			} else {
				stackWeight = 2 * boxWeight;
			}

			consignmentData.add(new String[] { Integer.toString(box.getBoxID()), NULL, NULL, NULL, NULL, NULL, NULL,
					NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
					box.getBoxDescription(), Integer.toString(box.getLength()), Integer.toString(box.getWidth()),
					Integer.toString(box.getHeight()), Integer.toString(box.getWeight()),
					box.getIsFloorOnly() ? "1" : "0", Integer.toString(verticalLengthAllowed),
					Integer.toString(verticalWidthAllowed), Integer.toString(verticalHeightAllowed),
					Integer.toString(isStackable), Integer.toString(stackWeight), Integer.toString(stackWeight),
					Integer.toString(stackWeight), color });

			uniqueId++;
			if (loadVolume > containerVolume || loadWeight > containerWeight)
				break;
		}

		consignmentMovementWriter.writeAll(consignmentMovementData);
		consignmentsWriter.writeAll(consignmentData);

		// closing writer connection
		consignmentMovementWriter.close();
		consignmentsWriter.close();

		Display.printSolidLineShort();
		for (STD_BOX_NAME key : boxCountMap.keySet()) {
			int boxCount = boxCountMap.get(key);
			System.out.println(String.format("%-22s:\t%d\t%.2f%%", key.toString(), boxCount,
					boxCount * 100.0 / consignmentMovement_count));
		}
		Display.printSolidLineShort();
		for (Integer key : cityCountMap.keySet()) {
			int cityCount = cityCountMap.get(key);
			System.out.println(String.format("CityID: %s\t%d\t%.2f%%", key.toString(), cityCount,
					cityCount * 100.0 / consignmentMovement_count));
		}
		Display.printSolidLineShort();

		System.out.println(
				String.format("Successfully added %d consignments in %s\nVolume = %d/%d = %.3f\nWeight = %d/%d = %.3f",
						consignmentMovement_count, consignmentMovementFilePath, loadVolume, containerVolume,
						loadVolume * 100.0 / containerVolume, loadWeight, containerWeight,
						loadWeight * 100.0 / containerWeight));
	}

	public static int getRandomIntInRange(int min, int max) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}
}
