package clp5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.json.JSONException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.mrebhan.crogamp.cli.TableList;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.opencsv.CSVWriter;

import clp5.Shared.CLP_MODE;
import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;
import clpBasic.eclipselink.services.DBQueryHandler;
import clpBasic.utilities.DataGenerator;
import clpBasic.utilities.Display;
import clpBasic.utilities.JSONHandler;
import clpBasic.utilities.RandomGeoMapper;
import clpBasic.utilities.StaticMemory;
import clpBasic.utilities.StdBoxPercent;
import clpBasic.utilities.StaticMemory.STD_BOX_NAME;
import eb_afit.AlgorithmPackingResult;
import eb_afit.ContainerPackingResult;
import eb_afit.EB_AFIT;
import eb_afit.PackingServices;
import eb_afit.AlgorithmType.AlgorithmTypes;
import eb_afit.ContainerPackingResult.PRIORITY_BASED_PACKING_TYPE;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Main class file
 */
public class CLPBasicMain5 implements NativeKeyListener {

	// [start] Global screen
	public static volatile boolean qquit;

	public void nativeKeyPressed(NativeKeyEvent e) {
		// System.out.println("Key Pressed: " +
		// NativeKeyEvent.getKeyText(e.getKeyCode()));
		if (e.getKeyCode() == NativeKeyEvent.VC_Q) {
			System.out.println("Stopping please wait...");
			qquit = true;
		}
	}

	private static void startGlobalScreenService() {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			System.exit(1);
		}
	}

	private static void endGlobalScreenService() {
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException nativeHookException) {
			nativeHookException.printStackTrace();
		}
	}

	// [end] Global screen

	// [start] Common methods

	private static String getTimeString(long elapsedtime) {
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedtime),
				TimeUnit.MILLISECONDS.toMinutes(elapsedtime) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(elapsedtime) % TimeUnit.MINUTES.toSeconds(1));
	}

	// [end] Common methods

	static String packModeFormat = "clpmain \n" + "-mode 		pack \n" + "-container 	<int>		//containerID\n"
			+ "-pickup 	<YYYY-MM-DD>	//pickupDate\n" + "-ptype 		<int>		//priorityBasedPackingType\n"
			+ "-outjson 	<true/false>	//generateJSONOutput\n"
			+ "-outpdf  	<true/false>	//generatePDFReport\n" + "-outdir 	<string>	//outputDirectoryPath\n"
			+ "-outsepdir 	<true/false>	//separateJSONPDFFoldersW\n" + "-savedebug 	<true/false>	//debugModeON";

	static String generateDataModeFormat = "clpmain\n" + "-mode 	generateData\n"
			+ "-brdata <true/false>	//generateBRData\n" + "-wtdata <true/false>	//generateDataWithWeights";

	static String generateBRDataModeFormat = "clpmain\n" + "-mode 	generateData\n" + "-brdata true\n"
			+ "-wtdata <true/false>	//generateDataWithWeights\n" + "-inpath	<string> 	//inputBRDataDirectoryPath\n"
			+ "-split	<true/false>	//singleContainerPerFile";

	static String generateSimpleRandomDataModeFormat = "clpmain\n" + "-mode 		generateData\n"
			+ "-brdata 	false		//generateBRData\n" + "-wtdata 	false		//generateDataWithWeights\n"
			+ "-outpath	<string>	//outputDirectoryPath\n" + "-pickup 	<YYYY-MM-DD>	//pickupDate\n"
			+ "-numcontainers 	<int>		//numContainers 5\n" + "-sbid 		<int>		//startBoxID 11\n"
			+ "-init		<int>		//currentPriorityValue\n" + "-incr		<int>		//incrementPriorityBy\n"
			+ "-max		<int>		//maxPriorityValue";

	static String generateWeightedRandomDataModeFormat = "clpmain\n" + "-mode 		generateData\n"
			+ "-brdata 	false		//generateBRData\n" + "-wtdata 	true		//generateDataWithWeights\n"
			+ "-outpath	<string>	//outputDirectoryPath\n" + "-pickup 	<YYYY-MM-DD>	//pickupDate\n"
			+ "-numcontainers 	<int>		//numContainers 5\n" + "-sbid 		<int>		//startBoxID 11\n"
			+ "-init		<int>		//currentPriorityValue\n" + "-incr		<int>		//incrementPriorityBy\n"
			+ "-max		<int>		//maxPriorityValue\n" + "-gridSize 	<int>		//gridSize	5\n"
			+ "-numcities 	<int>		//numCities 15\n" + "-numstd		<int>		//numstdBoxesPercentages\n"
			+ "<list<#stdbox, percentage>>	//space separated 2x numstd integers";

	/**
	 * Starting point of program
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		startGlobalScreenService();

		GlobalScreen.addNativeKeyListener(new CLPBasicMain5());

		boolean getBatchResults = false;
		if (getBatchResults) {
			PRIORITY_BASED_PACKING_TYPE pType = PRIORITY_BASED_PACKING_TYPE.IGNORE_PRIORITY;
			String pickupDate = "2021-10-11";

			// Creating JPA entity factory/manager
			EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("clpBasicMain_JPA");
			EntityManager entitymanager = emfactory.createEntityManager();

			// Query handler to fetch input from DB using JPA
			DBQueryHandler dbQueryHandler = new DBQueryHandler(entitymanager);

			// brDataGetResults(pType, pickupDate, dbQueryHandler);

			int startContainerID = 1, numContainerPerPriority = 5, startPriority = 1, priorityStep = 2, endPriority = 9;
			randomDataGetResults(pType, pickupDate, dbQueryHandler, startContainerID, numContainerPerPriority, startPriority,
					priorityStep, endPriority);
			System.exit(0);
		}

		if (args.length == 0 || !args[0].equals("-mode")) {
			System.out.println("Please provide mode as -mode pack | generateData");
			System.exit(1);
		}

		CLP_MODE mode = null;
		String arg1 = args[1];
		if (arg1.equals("pack")) {
			mode = CLP_MODE.PACK;
		} else if (arg1.equals("generateData")) {
			mode = CLP_MODE.GENERATE_DATA;
		} else {
			System.out.println("Invalid mode! Please provide mode as -mode pack | generateData");
			System.exit(1);
		}
		System.out.println(String.format("Found Mode:\t%s", mode));

		if (mode == CLP_MODE.PACK) {
			if (args.length < 18) {
				System.out.println(String.format("Insufficient %d #parameters passed!", args.length));
				for (int i = 2; i < args.length; i++) {
					System.out.println(String.format("arg:%-30sval:%s", args[i], args[++i]));
				}
				displayPackParametersError();
			}

			int containerID = 0;
			String pickupDate = null;
			PRIORITY_BASED_PACKING_TYPE priorityBasedPackingType = null;
			boolean generateJSONOutput = false, generatePDFReport = false;
			String outputDirectoryPath = null;
			boolean separateJSONPDFFolders = false, printfToLogFile = false;
			boolean runAllMethods = false;
			for (int i = 2; i < 18; i++) {
				if (args[i].equals("-container")) {
					containerID = Integer.parseInt(args[++i]);
					System.out.println(String.format("%-40s%d", "Found containerID", containerID));
				} else if (args[i].equals("-pickup")) {
					pickupDate = args[++i];
					System.out.println(String.format("%-40s%s", "Found pickupDate", pickupDate));
				} else if (args[i].equals("-ptype")) {
					int arg = Integer.parseInt(args[++i]);
					if (arg == PRIORITY_BASED_PACKING_TYPE.values().length) {
						runAllMethods = true;
						System.out.println(String.format("%-40s%s", "Found pType", "runAllMethods"));
					} else if (arg > PRIORITY_BASED_PACKING_TYPE.values().length) {
						System.out.println(String.format("Error: %d is not a valid pType value", arg));
						displayPTypeValues();
						System.exit(1);
					} else {
						priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.values()[arg];
						System.out.println(String.format("%-40s%s", "Found pType", priorityBasedPackingType));
					}
				} else if (args[i].equals("-outjson")) {
					generateJSONOutput = Boolean.parseBoolean(args[++i]);
					System.out.println(String.format("%-40s%s", "Found generateJSONOutput", generateJSONOutput));
				} else if (args[i].equals("-outpdf")) {
					generatePDFReport = Boolean.parseBoolean(args[++i]);
					System.out.println(String.format("%-40s%s", "Found generatePDFReport", generatePDFReport));
				} else if (args[i].equals("-outdir")) {
					outputDirectoryPath = args[++i];
					System.out.println(String.format("%-40s%s", "Found outputDirectoryPath", outputDirectoryPath));
				} else if (args[i].equals("-outsepdir")) {
					separateJSONPDFFolders = Boolean.parseBoolean(args[++i]);
					System.out
							.println(String.format("%-40s%s", "Found separateJSONPDFFolders", separateJSONPDFFolders));
				} else if (args[i].equals("-savedebug")) {
					printfToLogFile = Boolean.parseBoolean(args[++i]);
					System.out.println(String.format("%-40s%s", "Found debugModeON", printfToLogFile));
				} else {
					System.out.println("Invalid parameter passed!");
					displayPackParametersError();
				}
			}

			if (printfToLogFile) {
				FileOutputStream f = new FileOutputStream(String.format("container#%d-pickup %s-%s.log.txt",
						containerID, pickupDate, priorityBasedPackingType));
				System.setOut(new PrintStream(f, true, "UTF-8"));
				// AbstractSessionLog.getLog().log(SessionLog.INFO, DatabaseLogin.getVersion());
			}

			Scanner sc = new Scanner(System.in);
			// sc.nextLine();

			// Creating JPA entity factory/manager
			EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("clpBasicMain_JPA");
			EntityManager entitymanager = emfactory.createEntityManager();

			// Query handler to fetch input from DB using JPA
			DBQueryHandler dbQueryHandler = new DBQueryHandler(entitymanager);

			if (runAllMethods) {
				priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.IGNORE_PRIORITY;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);

				sc.nextLine();
				priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_IN_CONTAINER_VOLUME_STEPS;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);

				sc.nextLine();
				priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_BEST_OF_EACH;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);

				sc.nextLine();
				priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_BEST_OF_EACH_WITH_STACK;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);

				sc.nextLine();
				priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_EACH_GROUP_WITH_STACK;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);
			} else {
				// priorityBasedPackingType = PRIORITY_BASED_PACKING_TYPE.IGNORE_PRIORITY;
				packContainerByID(dbQueryHandler, containerID, pickupDate, priorityBasedPackingType, generateJSONOutput,
						generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);
			}
			sc.close();
		} else {
			if (args.length < 6) {
				System.out.println("Insufficient #parameters passed!");
				displayGenerateDataParametersError("generateData", generateDataModeFormat);
			}

			boolean generateBRData = false, generateDataWithWeights = false;
			for (int i = 2; i < 6; i++) {
				if (args[i].equals("-brdata")) {
					generateBRData = Boolean.parseBoolean(args[++i]);
					System.out.println(String.format("%-40s%s", "Found generateBRData", generateBRData));
				} else if (args[i].equals("-wtdata")) {
					generateDataWithWeights = Boolean.parseBoolean(args[++i]);
					System.out.println(
							String.format("%-40s%s", "Found generateDataWithWeights", generateDataWithWeights));
				} else {
					System.out.println("Invalid parameter passed!");
					displayGenerateDataParametersError("generateData", generateDataModeFormat);
				}
			}

			// Test data generation, generated CSV files need to be manually imported in DB
			if (generateBRData) {
				String pickupDate = "2021-10-11";
				if (args.length < 10) {
					System.out.println("Insufficient #parameters passed!");
					displayGenerateDataParametersError("generateBRData", generateBRDataModeFormat);
				}

				String inputBRDataDirectoryPath = null;
				boolean split = false;
				for (int i = 6; i < 10; i++) {
					if (args[i].equals("-inpath")) {
						inputBRDataDirectoryPath = args[++i];
						System.out.println(
								String.format("%-40s%s", "Found inputBRDataDirectoryPath", inputBRDataDirectoryPath));
					} else if (args[i].equals("-split")) {
						split = Boolean.parseBoolean(args[++i]);
						System.out.println(String.format("%-40s%s", "Found split", split));
					} else {
						System.out.println("Invalid parameter passed!");
						displayGenerateDataParametersError("generateBRData", generateBRDataModeFormat);
					}
				}

				generateBRData(inputBRDataDirectoryPath, pickupDate, generateDataWithWeights, split);
			} else {
				if (!generateDataWithWeights) {
					if (args.length < 20) {
						System.out.println("Insufficient #parameters passed!");
						displayGenerateDataParametersError("generateSimpleRandomData",
								generateSimpleRandomDataModeFormat);
					}

					String outputDirectoryPath = null, pickupDate = null;
					int numContainers = 0, startBoxID = 0, currentPriorityValue = 0, incrementPriorityBy = 0,
							maxPriorityValue = 0;
					for (int i = 6; i < 20; i++) {
						if (args[i].equals("-outpath")) {
							outputDirectoryPath = args[++i];
							System.out.println(
									String.format("%-40s%s", "Found outputDirectoryPath", outputDirectoryPath));
						} else if (args[i].equals("-pickup")) {
							pickupDate = args[++i];
							System.out.println(String.format("%-40s%s", "Found pickupDate", pickupDate));
						} else if (args[i].equals("-numcontainers")) {
							numContainers = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numContainers", numContainers));
						} else if (args[i].equals("-sbid")) {
							startBoxID = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found startBoxID", startBoxID));
						} else if (args[i].equals("-init")) {
							currentPriorityValue = Integer.parseInt(args[++i]);
							System.out.println(
									String.format("%-40s%d", "Found currentPriorityValue", currentPriorityValue));
						} else if (args[i].equals("-incr")) {
							incrementPriorityBy = Integer.parseInt(args[++i]);
							System.out.println(
									String.format("%-40s%d", "Found incrementPriorityBy", incrementPriorityBy));
						} else if (args[i].equals("-max")) {
							maxPriorityValue = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numContainers", maxPriorityValue));
						} else {
							System.out.println("Invalid parameter passed!");
							displayGenerateDataParametersError("generateSimpleRandomData",
									generateSimpleRandomDataModeFormat);
						}
					}

					generateRandomData(outputDirectoryPath, pickupDate, numContainers, startBoxID, currentPriorityValue,
							incrementPriorityBy, maxPriorityValue);
				} else {
					if (args.length < 26) {
						System.out.println("Insufficient #parameters passed!");
						displayGenerateDataParametersError("generateWeightedRandomData",
								generateWeightedRandomDataModeFormat);
					}

					String outputDirectoryPath = null, pickupDate = null;
					int numContainers = 0, startBoxID = 0, currentPriorityValue = 0, incrementPriorityBy = 0,
							maxPriorityValue = 0, gridSize = 0, numCities = 0, numstd;
					List<StdBoxPercent> stdBoxesPercentages = new ArrayList<StdBoxPercent>();
					for (int i = 6; i < 26; i++) {
						if (args[i].equals("-outpath")) {
							outputDirectoryPath = args[++i];
							System.out.println(
									String.format("%-40s%s", "Found outputDirectoryPath", outputDirectoryPath));
						} else if (args[i].equals("-pickup")) {
							pickupDate = args[++i];
							System.out.println(String.format("%-40s%s", "Found pickupDate", pickupDate));
						} else if (args[i].equals("-numcontainers")) {
							numContainers = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numContainers", numContainers));
						} else if (args[i].equals("-sbid")) {
							startBoxID = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found startBoxID", startBoxID));
						} else if (args[i].equals("-init")) {
							currentPriorityValue = Integer.parseInt(args[++i]);
							System.out.println(
									String.format("%-40s%d", "Found currentPriorityValue", currentPriorityValue));
						} else if (args[i].equals("-incr")) {
							incrementPriorityBy = Integer.parseInt(args[++i]);
							System.out.println(
									String.format("%-40s%d", "Found incrementPriorityBy", incrementPriorityBy));
						} else if (args[i].equals("-max")) {
							maxPriorityValue = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numContainers", maxPriorityValue));
						} else if (args[i].equals("-gridSize")) {
							gridSize = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found gridSize", gridSize));
						} else if (args[i].equals("-numcities")) {
							numCities = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numCities", numCities));
						} else if (args[i].equals("-numstd")) {
							numstd = Integer.parseInt(args[++i]);
							System.out.println(String.format("%-40s%d", "Found numstd", numstd));

							for (int j = 0; j < numstd; j++) {
								if (i + 2 >= args.length) {
									System.out.println("Insufficient data for std boxes!");
									displayGenerateDataParametersError("generateWeightedRandomDataModeFormat",
											generateWeightedRandomDataModeFormat);
								}
								int stdBoxNumber = Integer.parseInt(args[++i]);
								int stdBoxPercentage = Integer.parseInt(args[++i]);
								System.out.println(String.format("%-40s<%d, %d>", "Found <stdBox#, %>", stdBoxNumber,
										stdBoxPercentage));
								if (stdBoxNumber >= STD_BOX_NAME.values().length) {
									System.out.println("Invalid stdBoxNumber!");
									displayGenerateDataParametersError("generateWeightedRandomDataModeFormat",
											generateWeightedRandomDataModeFormat);
								}
								stdBoxesPercentages
										.add(new StdBoxPercent(STD_BOX_NAME.values()[stdBoxNumber], stdBoxPercentage));
							}
						} else {
							System.out.println("Invalid parameter passed!");
							displayGenerateDataParametersError("generateWeightedRandomDataModeFormat",
									generateWeightedRandomDataModeFormat);
						}
					}

					generateRandomDataWt(outputDirectoryPath, pickupDate, numContainers, startBoxID,
							currentPriorityValue, incrementPriorityBy, maxPriorityValue, gridSize, numCities,
							stdBoxesPercentages);
				}
			}
		}

		endGlobalScreenService();
	}

	private static void displayPackParametersError() {
		System.out.println("\nPlease provide correct/all parameters for pack mode:");
		System.out.println(String.format("%s", packModeFormat));
		displayPTypeValues();
		System.exit(1);
	}

	private static void displayPTypeValues() {
		System.out.println("\npType Values:");
		int i = 0;
		for (PRIORITY_BASED_PACKING_TYPE packingType : PRIORITY_BASED_PACKING_TYPE.values()) {
			System.out.println(String.format("%d\t%s", i++, packingType));
		}
	}

	private static void displayGenerateDataParametersError(String title, String format) {
		System.out.println(String.format("\nPlease provide correct/all parameters for %s mode:", title));
		System.out.println(String.format("%s", format));
		System.exit(1);
	}

	private static void packContainerByID(DBQueryHandler dbQueryHandler, int containerID, String pickupDate,
			PRIORITY_BASED_PACKING_TYPE priorityBasedPackingType, boolean generateJSONOutput, boolean generatePDFReport,
			String outputDirectoryPath, boolean separateJSONPDFFolders) throws Exception, IOException, JSONException,
			URISyntaxException, NoSuchFieldException, IllegalAccessException {
		// Get container
		Container container = dbQueryHandler.getContainerByID(containerID);

		// Get orders for the selected container
		List<Consignment> consignments = dbQueryHandler.getConsignmentsForGivenContainerAndDate(containerID,
				pickupDate);

		// Pack container
		if (consignments.size() > 0) {
			ContainerPackingResult cpr = packContainer(container, pickupDate, consignments, priorityBasedPackingType,
					generateJSONOutput, generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);
		} else {
			System.out.println(
					String.format("No consignments found for the container #%d for given date %s as pickup timestamp",
							containerID, pickupDate));
		}
	}

	private static ContainerPackingResult packContainer(Container container, String pickupDate,
			List<Consignment> consignments, PRIORITY_BASED_PACKING_TYPE pType, boolean generateJSONOutput,
			boolean generatePDFReport, String outputDirectoryPath, boolean separateJSONPDFFolders) throws Exception,
			IOException, JSONException, URISyntaxException, NoSuchFieldException, IllegalAccessException {

		// Run algorithm
		int stepsVolumePercent = 20;
		ContainerPackingResult cpr = PackingServices.Pack(container, consignments, AlgorithmTypes.EB_AFIT, pType,
				stepsVolumePercent);

		// if (container.getID() % 10 == 1)
		report(cpr, pickupDate, generateJSONOutput, generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);

		return cpr;
	}

	/**
	 * Method to generate report + output file
	 * 
	 * @param cpr - ContainerPackingResult (output from algorithm)
	 * @throws IOException
	 * @throws JSONException
	 * @throws URISyntaxException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static void report(ContainerPackingResult cpr, String pickupDate, boolean generateJSONOutput,
			boolean generatePDFReport, String outputDirectoryPath, boolean separateJSONPDFFolders)
			throws IOException, JSONException, URISyntaxException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {

		AlgorithmPackingResult apr = cpr.getAlgorithmPackingResults().get(0);

		long elapsedtime = apr.getPackTimeInMilliseconds();
		String hms = getTimeString(elapsedtime);

		int itenum = apr.getApr_itenum();
		int bestite = apr.getApr_bestite();
		int bestvariant = apr.getApr_bestvariant();

		int bestpackednum = apr.getPackedItems().size();
		int bestunpackednum = apr.getUnpackedItems().size();
		int tbn = apr.getTotalBoxCount();

		int totalboxvol = apr.getApr_totalboxvol();
		int totalvolume = cpr.getContainer().getVolume();
		int bestvolume = apr.getApr_bestvolume();
		double percentageUsedVolume = apr.getPercentContainerVolumePacked();
		double percentageUsedItemVolume = apr.getPercentItemVolumePacked();

		int totalboxweight = apr.getApr_totalboxweight();
		int tonnage = cpr.getContainer().getTonnage();
		int bestweight = apr.getApr_bestweight();
		double percentageUsedTonnage = apr.getPercentContainerTonnagePacked();
		double percentageUsedItemWeight = apr.getPercentItemWeightPacked();

		int px = apr.getApr_px();
		int py = apr.getApr_py();
		int pz = apr.getApr_pz();

//		AsciiTable at = new AsciiTable();

//		System.out.println("List of packed boxes:");
//		at.addRule();
//		at.addRow("", "L", "W", "H", "ID", "X", "Y", "Z", "L1", "W1", "H1", "P");
//
//		int i = 1;
//		for (Consignment box : apr.getPackedItems()) {
//			at.addRule();
//			at.addRow(i++, box.getLength(), box.getWidth(), box.getHeight(), box.getBoxID(), box.getCOX(), box.getCOY(),
//					box.getCOZ(), box.getPackX(), box.getPackY(), box.getPackZ(), box.getPriority());
//		}
//		at.addRule();
//		System.out.println(at.render());
//
//		System.out.println("List of unpacked boxes:");
//		at = new AsciiTable();
//		at.addRule();
//		at.addRow("", "ID", "L", "W", "H");
//
//		i = 1;
//		for (Consignment box : apr.getUnpackedItems()) {
//			at.addRule();
//			at.addRow(i++, box.getBoxID(), box.getLength(), box.getWidth(), box.getHeight());
//		}
//		at.addRule();
//		System.out.println(at.render());

		String rows[][] = { { "PACKING PRIORITY TYPE", String.format("%s", cpr.getPackingPriorityType()) },
				{ "ELAPSED TIME (HH:MM:SS)", String.format("Almost %s", hms) },
				{ "TOTAL NUMBER OF ITERATIONS DONE", String.valueOf(itenum) },
				{ "BEST SOLUTION FOUND AT ITERATION", String.format("%d OF VARIANT %d", bestite, bestvariant) },
				{ "TOTAL NUMBER OF BOXES", String.valueOf(tbn) },
				{ "PACKED NUMBER OF BOXES", String.valueOf(bestpackednum) },
				{ "UNPACKED NUMBER OF BOXES", String.valueOf(bestunpackednum) },
				{ "TOTAL VOLUME OF ALL BOXES", String.valueOf(totalboxvol) },
				{ "CONTAINER VOLUME", String.valueOf(totalvolume) },
				{ "BEST SOLUTION'S VOLUME UTILIZATION", String.format("%d OUT OF %d", bestvolume, totalvolume) },
				{ "PERCENTAGE OF CONTAINER VOLUME USED", String.format("%.2f", percentageUsedVolume) },
				{ "PERCENTAGE OF PACKED BOXES (VOLUME)", String.format("%.2f", percentageUsedItemVolume) },
				{ "TOTAL WEIGHT OF ALL BOXES", String.valueOf(totalboxweight) },
				{ "CONTAINER TONNAGE", String.valueOf(tonnage) },
				{ "BEST SOLUTION'S WEIGHT UTILIZATION", String.format("%d OUT OF %d", bestweight, tonnage) },
				{ "PERCENTAGE OF CONTAINER TONNAGE USED", String.format("%.2f", percentageUsedTonnage) },
				{ "PERCENTAGE OF PACKED BOXES (WEIGHT)", String.format("%.2f", percentageUsedItemWeight) },
				{ "WHILE CONTAINER ORIENTATION (X,Y,Z)", String.format("(%d,%d,%d)", px, py, pz) } };

//		at = new AsciiTable();
//		at.addRule();
//		at.addRow(null, "*** REPORT ***")
//				.setTextAlignment(de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment.CENTER);
//		for (int j = 0; j < rows.length; j++) {
//			at.addRule();
//			at.addRow(rows[j][0], rows[j][1]);
//		}
//		at.addRule();
//		System.out.println(at.render());

		TableList tl = new TableList(2, "Property", "Output").withUnicode(false);
		for (int j = 0; j < rows.length; j++) {
			tl.addRow(rows[j][0], rows[j][1]);
		}

		Display.printDashedLine();
		System.out.println("Report:");
		Display.printSolidLine();
		tl.print();
		Display.printDashedLine();

		if (generateJSONOutput) {
			String jsonDirectoryPath = outputDirectoryPath;
			if (separateJSONPDFFolders) {
				jsonDirectoryPath += "JSON\\";
			}
			Files.createDirectories(Paths.get(jsonDirectoryPath));
			createJSONFile(jsonDirectoryPath, cpr.getContainer(), pickupDate, cpr.getPackingPriorityType(),
					apr.getPackedItems(), bestvariant, bestite, apr.getPercentContainerVolumePacked());
		}
		if (generatePDFReport) {
			String pdfDirectoryPath = outputDirectoryPath;
			if (separateJSONPDFFolders) {
				pdfDirectoryPath += "PDF\\";
			}
			Files.createDirectories(Paths.get(pdfDirectoryPath));
			createPDF(pdfDirectoryPath, cpr, pickupDate);
		}
		System.out.println(String.format("%.2f", apr.getPercentContainerVolumePacked()));
	}

	static void createPDF(String directoryPath, ContainerPackingResult cpr, String pickupDate) throws IOException {
		String pdfFileName = String.format("container-%d-pickup-%s-ptype-%s-%.2f.pdf", cpr.getContainer().getID(),
				pickupDate, cpr.getPackingPriorityType(),
				cpr.getAlgorithmPackingResults().get(0).getPercentContainerVolumePacked());

		// Step-1 Create a PdfDocument object
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(directoryPath + pdfFileName));
		pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new Footer());

		// Step-2 Create a Document object
		// PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);
		Document doc = new Document(pdfDoc); // .setFontSize(10f); //.setFont(font)

		Container container = cpr.getContainer();
		Paragraph title = new Paragraph(String.format("Container %d Loading Plan", container.getID()))
				.setFontColor(new DeviceRgb(8, 73, 117)).setFontSize(20f).setTextAlignment(TextAlignment.CENTER);
		Paragraph subtitle = new Paragraph("Cargo Load Planner - Report Document")
				.setFontColor(new DeviceRgb(8, 73, 117)).setFontSize(13f).setTextAlignment(TextAlignment.CENTER);
		doc.add(title);
		doc.add(subtitle);

		// doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

		// Step-3 Create a table
		Table tableReport = new Table(2).setMargin(10);

		AlgorithmPackingResult apr = cpr.getAlgorithmPackingResults().get(0);
		int bestpackednum = apr.getPackedItems().size();
		int UnpackedItemsCount = apr.getUnpackedItems().size();
		int tbn = apr.getTotalBoxCount();
		long elapsedtime = apr.getPackTimeInMilliseconds();

		String hms = getTimeString(elapsedtime);

		// Step-4 Adding cells to the table
		tableReport.addCell(new Cell().add("ELAPSED TIME  (HH:MM:SS)"));
		tableReport.addCell(new Cell().add(String.format("Almost %s", hms)));
		tableReport.addCell(new Cell().add("CONTAINER ID"));
		tableReport.addCell(new Cell().add(String.format("%d", container.getID())));
		tableReport.addCell(new Cell().add("PACKING PRIORITY TYPE"));
		tableReport.addCell(new Cell().add(String.format("%s", cpr.getPackingPriorityType())));
		tableReport.addCell(new Cell().add("CONTAINER DIMENSIONS"));
		tableReport.addCell(new Cell().add(
				String.format("%d x %d x %d cm", container.getLength(), container.getWidth(), container.getHeight())));

		tableReport.addCell(new Cell().add("TOTAL NUMBER OF BOXES"));
		tableReport.addCell(new Cell().add(String.format("%d", tbn)));
		tableReport.addCell(new Cell().add("PACKED NUMBER OF BOXES"));
		tableReport.addCell(new Cell().add(String.format("%d", bestpackednum)));
		tableReport.addCell(new Cell().add("UNPACKED NUMBER OF BOXES"));
		tableReport.addCell(new Cell().add(String.format("%d", UnpackedItemsCount)));

		int totalboxvol = apr.getApr_totalboxvol();
		int containerVolume = container.getVolume();
		int bestvolume = apr.getApr_bestvolume();
		double percentageUsedVolume = apr.getPercentContainerVolumePacked();
		double percentageUsedItemVolume = apr.getPercentItemVolumePacked();

		tableReport.addCell(new Cell().add("CONTAINER VOLUME"));
		tableReport.addCell(new Cell().add(String.format("%d cubic cm", containerVolume)));
		tableReport.addCell(new Cell().add("TOTAL VOLUME OF ALL BOXES"));
		tableReport.addCell(new Cell().add(String.format("%d cubic cm", totalboxvol)));
		tableReport.addCell(new Cell().add("BEST SOLUTION'S VOLUME UTILIZATION"));
		tableReport.addCell(new Cell().add(String.format("%d OUT OF %d cubic cm", bestvolume, containerVolume)));
		tableReport.addCell(new Cell().add("PERCENTAGE OF CONTAINER VOLUME USED"));
		tableReport.addCell(new Cell().add(String.format("%.2f", percentageUsedVolume)));
		tableReport.addCell(new Cell().add("PERCENTAGE OF PACKED BOXES (VOLUME)"));
		tableReport.addCell(new Cell().add(String.format("%.2f", percentageUsedItemVolume)));

		int totalboxweight = apr.getApr_totalboxweight();
		int tonnage = container.getTonnage();
		int bestweight = apr.getApr_bestweight();
		double percentageUsedTonnage = apr.getPercentContainerTonnagePacked();
		double percentageUsedItemWeight = apr.getPercentItemWeightPacked();

		tableReport.addCell(new Cell().add("CONTAINER TONNAGE"));
		tableReport.addCell(new Cell().add(String.format("%.3f kg", tonnage / 1000.0)));
		tableReport.addCell(new Cell().add("TOTAL WEIGHT OF ALL BOXES"));
		tableReport.addCell(new Cell().add(String.format("%.3f kg", totalboxweight / 1000.0)));
		tableReport.addCell(new Cell().add("BEST SOLUTION'S TONNAGE UTILIZATION"));
		tableReport
				.addCell(new Cell().add(String.format("%.3f OUT OF %.3f kg", bestweight / 1000.0, tonnage / 1000.0)));
		tableReport.addCell(new Cell().add("PERCENTAGE OF CONTAINER TONNAGE USED"));
		tableReport.addCell(new Cell().add(String.format("%.2f", percentageUsedTonnage)));
		tableReport.addCell(new Cell().add("PERCENTAGE OF PACKED BOXES (WEIGHT)"));
		tableReport.addCell(new Cell().add(String.format("%.2f", percentageUsedItemWeight)));

		// Step-6 Adding Table to document
		doc.add(tableReport);

		doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

		Paragraph packedStr = new Paragraph("List of packed boxes").setFontColor(new DeviceRgb(8, 73, 117))
				.setFontSize(16f).setTextAlignment(TextAlignment.CENTER);
		doc.add(packedStr);

		Paragraph p = new Paragraph().setMargin(10);
		p.add(new Text("ID"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("BOX ID\n"));

		p.add(new Text("P1,P2,P3"));
		p.add(new Tab());
		p.add(new Text("WALL, LAYER, IN-LAYER POSITION\n"));

		p.add(new Text("L,W,H"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("GIVEN DIMENSIONS (Length, Width, Height) OF THE BOX\n"));

		p.add(new Text("X,Y,Z"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("PACKED XYZ CO-ORDINATES OF THE BOX\n"));

		p.add(new Text("L1,W1,H1"));
		p.add(new Tab());
		p.add(new Text("PACKED DIMENSIONS OF THE BOX\n"));

		p.add(new Text("P"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("BOX PRIORITY\n"));
		doc.add(p);

		Table tablePackedBoxes = new Table(14).setMargin(10);

		tablePackedBoxes.addCell(new Cell().add(""));
		tablePackedBoxes.addCell(new Cell().add("ID"));
		tablePackedBoxes.addCell(new Cell().add("P1"));
		tablePackedBoxes.addCell(new Cell().add("P2"));
		tablePackedBoxes.addCell(new Cell().add("P3"));
		tablePackedBoxes.addCell(new Cell().add("L"));
		tablePackedBoxes.addCell(new Cell().add("W"));
		tablePackedBoxes.addCell(new Cell().add("H"));
		tablePackedBoxes.addCell(new Cell().add("X"));
		tablePackedBoxes.addCell(new Cell().add("Y"));
		tablePackedBoxes.addCell(new Cell().add("Z"));
		tablePackedBoxes.addCell(new Cell().add("L1"));
		tablePackedBoxes.addCell(new Cell().add("W1"));
		tablePackedBoxes.addCell(new Cell().add("H1"));

		int i = 1;
		for (Consignment box : apr.getPackedItems()) {
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", i++)));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getBoxID())));

			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getWall())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getLayer())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getPositionInLayer())));

			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getLength())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getWidth())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getHeight())));

			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getCOX())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getCOY())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getCOZ())));

			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getPackX())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getPackY())));
			tablePackedBoxes.addCell(new Cell().add(String.format("%d", box.getPackZ())));
		}

		doc.add(tablePackedBoxes);

		doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

		Paragraph unpackedStr = new Paragraph("List of unpacked boxes").setFontColor(new DeviceRgb(8, 73, 117))
				.setFontSize(16f).setTextAlignment(TextAlignment.CENTER);
		doc.add(unpackedStr);

		p = new Paragraph().setMargin(10);
		p.add(new Text("ID"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("BOX ID\n"));

		p.add(new Text("L,W,H"));
		p.add(new Tab());
		p.add(new Tab());
		p.add(new Text("GIVEN DIMENSIONS (Length, Width, Height) OF THE BOX\n"));
		doc.add(p);

		Table tableUnpackedBoxes = new Table(5).setMargin(10);

		tableUnpackedBoxes.addCell(new Cell().add(""));
		tableUnpackedBoxes.addCell(new Cell().add("ID"));
		tableUnpackedBoxes.addCell(new Cell().add("L"));
		tableUnpackedBoxes.addCell(new Cell().add("W"));
		tableUnpackedBoxes.addCell(new Cell().add("H"));

		i = 1;
		for (Consignment box : apr.getUnpackedItems()) {
			tableUnpackedBoxes.addCell(new Cell().add(String.format("%d", i++)));
			tableUnpackedBoxes.addCell(new Cell().add(String.format("%d", box.getBoxID())));

			tableUnpackedBoxes.addCell(new Cell().add(String.format("%d", box.getLength())));
			tableUnpackedBoxes.addCell(new Cell().add(String.format("%d", box.getWidth())));
			tableUnpackedBoxes.addCell(new Cell().add(String.format("%d", box.getHeight())));
		}

		doc.add(tableUnpackedBoxes);

		doc.add(new Paragraph("END OF REPORT DOCUMENT").setTextAlignment(TextAlignment.CENTER).setMargin(10));

		// Step-7 Closing the document
		doc.close();
		System.out.println(directoryPath + pdfFileName);
		System.out.println("PDF Report Created Successfully!");
	}

	/**
	 * Method to generate output JSON file for visualization purpose
	 * 
	 * @param algorithmName - Name of the algorithm used
	 * @param container
	 * @param boxes
	 * @throws IOException
	 * @throws JSONException
	 * @throws URISyntaxException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	static void createJSONFile(String jsonDirectoryPath, Container container, String pickupDate,
			PRIORITY_BASED_PACKING_TYPE packingPriorityType, List<Consignment> boxes, int bestOrientation,
			int bestRelativeIteration, double efficiency) throws IOException, JSONException, URISyntaxException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		JSONHandler jsonHandler = new JSONHandler();

		String jsonStr = jsonHandler.createJSON(container, boxes, bestOrientation, bestRelativeIteration);
		String jsonFileName = String.format("container-%d-pickup-%s-ptype-%s-%.2f.json", container.getID(), pickupDate,
				packingPriorityType, efficiency);
		jsonHandler.createJSONFile(jsonDirectoryPath + jsonFileName, jsonStr);
	}

	private static void generateRandomData(String outputDirectoryPath, String pickupDate, int numContainers,
			int startBoxID, int currentPriorityValue, int incrementPriorityBy, int maxPriorityValue)
			throws IOException {
		int priorityCount = (maxPriorityValue - currentPriorityValue) / incrementPriorityBy + 1;
		int numBoxes = 0;

		for (int i = 0; i < priorityCount; i++) {
			numBoxes += DataGenerator.generateRandomData(outputDirectoryPath, pickupDate, numContainers,
					numContainers * i + 1, numBoxes + startBoxID, currentPriorityValue);
			currentPriorityValue += incrementPriorityBy;
		}
		Display.printDashedLine();
		System.out
				.println(String.format("Total Containers:%d\nTotal boxes:%d", numContainers * priorityCount, numBoxes));
	}

	private static void generateRandomDataWt(String outputDirectoryPath, String pickupDate, int numContainers,
			int startBoxID, int numCitiesOnPath, int incrementPriorityBy, int maxPriorityValue, int gridSize,
			int numCities, List<StdBoxPercent> stdBoxesPercentages) throws IOException {
		int priorityCount = (maxPriorityValue - numCitiesOnPath) / incrementPriorityBy + 1;
		int numBoxes = 0;

		for (int i = 0; i < priorityCount; i++) {
			numBoxes += DataGenerator.generateRandomDataWithWeights(outputDirectoryPath, pickupDate, numContainers,
					numContainers * i + 1, numBoxes + startBoxID, gridSize, numCities, numCitiesOnPath,
					!stdBoxesPercentages.isEmpty(), stdBoxesPercentages);
			Display.printDashedLine();
			numCitiesOnPath += incrementPriorityBy; // currentPriorityValue = numCitiesOnPath
		}
		Display.printDashedLine();
		System.out
				.println(String.format("Total Containers:%d\nTotal boxes:%d", numContainers * priorityCount, numBoxes));
	}

	private static void generateBRData(String inputBRDataDirectoryPath, String pickupTimeStamp,
			boolean generateDataWithWeights, boolean split) throws IOException {
		if (!generateDataWithWeights) {
			DataGenerator.generateBRData(inputBRDataDirectoryPath, pickupTimeStamp, split);
		} else {
			DataGenerator.generateBRDataWithWeights(inputBRDataDirectoryPath, pickupTimeStamp, split);
		}
	}

	public static void addLogEntries(String clpLogFileName, List<LogEntry> entries) throws IOException {
		String logFilePath = clpLogFileName;
		String[] logHeader = new String[] { "Dataset", "Total #Boxes", "Packed #Boxes", "Unpacked #Boxes",
				"% Container Volume Utilization", "% Packed Volume of All Boxes", "% Container Tonnage Utilization",
				"% Packed Weight of All Boxes", "Solution Time", "#Priorities", "Priority Type", "TimeStamp" };

		// Create file object for file placed at location specified by file path
		File logFile = new File(logFilePath);
		// Files.deleteIfExists(file.toPath());

		FileWriter outputfile;

		// Create a List which contains String array
		List<String[]> logData = new ArrayList<String[]>();

		if (!logFile.isFile()) {
			outputfile = new FileWriter(logFile, false);
			System.out.println("Successfully created file: " + logFilePath);
			logData.add(logHeader);
		} else {
			outputfile = new FileWriter(logFile, true);
			System.out.println("File already exists, appending log entry");
		}

		String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

		for (LogEntry e : entries) {
			logData.add(new String[] { e.getDatasetDescription(), Integer.toString(e.getTotalBoxes()),
					Integer.toString(e.getPackedBoxes()), Integer.toString(e.getUnpackedBoxes()),
					String.format("%.2f", e.getContainerVolumeUtilization()),
					String.format("%.2f", e.getBoxVolumePercent()),
					String.format("%.2f", e.getContainerTonnageUtilization()),
					String.format("%.2f", e.getBoxWeightPercent()), e.getSolutionTime(),
					Integer.toString(e.getNumPriorities()), e.getPriorityType().toString(), timeStamp });
		}

		// Create CSVWriter object file writer object as parameter
		CSVWriter writer = new CSVWriter(outputfile);
		writer.writeAll(logData);
		writer.close();

		System.out.println("Successfully added log entries to file: " + logFilePath);
	}

	private static void randomDataGetResults(PRIORITY_BASED_PACKING_TYPE pType, String pickupDate,
			DBQueryHandler dbQueryHandler, int startContainerID, int numContainerPerPriority, int startPriority,
			int priorityStep, int endPriority) throws Exception, IOException, JSONException, URISyntaxException,
			NoSuchFieldException, IllegalAccessException {
		// Fetch container IDs
		List<Integer> containerIDs = dbQueryHandler.getContainerIDs();
		System.out.println(containerIDs);

		// Check if data exists in the database
		if (containerIDs.size() > 0) {
			int numPriority = (endPriority - startPriority) / priorityStep + 1;
			for (int i = 1, numPriorities = startPriority; i <= numPriority; i++, numPriorities += priorityStep) {
				List<LogEntry> entries = new ArrayList<LogEntry>();
				for (int containerID = (i - 1) * numContainerPerPriority + startContainerID; containerID < (i - 1)
						* numContainerPerPriority + startContainerID + numContainerPerPriority; containerID++) {
					// Get Container
					Container container = dbQueryHandler.getContainerByID(containerID);

					// Get orders for the selected container
					List<Consignment> consignments = dbQueryHandler.getConsignmentsForGivenContainerAndDate(containerID,
							pickupDate);

					// Run algorithm if data exists
					if (consignments.size() > 0) {
						Display.printSolidLine();
						boolean generateJSONOutput = false;
						boolean generatePDFReport = false;
						String outputDirectoryPath = null;
						boolean separateJSONPDFFolders = false;
						ContainerPackingResult cpr = packContainer(container, pickupDate, consignments, pType,
								generateJSONOutput, generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);
						System.out.println(String.format("Packed Container %d - %s", containerID, pType));

						String datasetDescription = consignments.get(0).getBoxDescription();

						AlgorithmPackingResult apr = cpr.getAlgorithmPackingResults().get(0);

						int totalBoxes = apr.getTotalBoxCount();
						int packedBoxes = apr.getPackedItems().size();
						int unpackedBoxes = apr.getUnpackedItems().size();

						double containerVolumeUtilization = apr.getPercentContainerVolumePacked();
						double boxVolumePercent = apr.getPercentItemVolumePacked();
						double containerTonnageUtilization = apr.getPercentContainerTonnagePacked();
						double boxWeightPercent = apr.getPercentItemWeightPacked();

						long elapsedtime = apr.getPackTimeInMilliseconds();
						String hms = getTimeString(elapsedtime);

						LogEntry logEntry = new LogEntry(datasetDescription, totalBoxes, packedBoxes, unpackedBoxes,
								containerVolumeUtilization, boxVolumePercent, containerTonnageUtilization,
								boxWeightPercent, hms, numPriorities, pType);

						entries.add(logEntry);
					} else {
						System.out.println(String.format(
								"No consignments found for the container #%d for given date %s as pickup timestamp",
								containerID, pickupDate));
					}
				}
				addLogEntries(String.format("clpLog_Db_r#%d_%s.csv", numPriorities, pType), entries);
			}
		} else {
			System.out.println("No containers found, please check if data exists in the database");
		}
	}

	private static void brDataGetResults(PRIORITY_BASED_PACKING_TYPE pType, String pickupDate,
			DBQueryHandler dbQueryHandler) throws Exception, IOException, JSONException, URISyntaxException,
			NoSuchFieldException, IllegalAccessException {
		// Fetch container IDs
		List<Integer> containerIDs = dbQueryHandler.getContainerIDs();
		System.out.println(containerIDs);

		// Check if data exists in the database
		if (containerIDs.size() > 0) {
			for (int thpack = 1; thpack <= 7; thpack++) {
				List<LogEntry> entries = new ArrayList<LogEntry>();
				for (int containerID = (thpack - 1) * 100 + 1; containerID <= (thpack - 1) * 100 + 100; containerID++) {
					// Get Container
					Container container = dbQueryHandler.getContainerByID(containerID);

					// Get orders for the selected container
					List<Consignment> consignments = dbQueryHandler.getConsignmentsForGivenContainerAndDate(containerID,
							pickupDate);

					// Run algorithm if data exists
					if (consignments.size() > 0) {
						Display.printSolidLine();
						boolean generateJSONOutput = false;
						boolean generatePDFReport = false;
						String outputDirectoryPath = null;
						boolean separateJSONPDFFolders = false;
						ContainerPackingResult cpr = packContainer(container, pickupDate, consignments, pType,
								generateJSONOutput, generatePDFReport, outputDirectoryPath, separateJSONPDFFolders);
						System.out.println(String.format("Packed Container %d - %s", containerID, pType));

						String datasetDescription = consignments.get(0).getBoxDescription();

						AlgorithmPackingResult apr = cpr.getAlgorithmPackingResults().get(0);

						int totalBoxes = apr.getTotalBoxCount();
						int packedBoxes = apr.getPackedItems().size();
						int unpackedBoxes = apr.getUnpackedItems().size();

						double containerVolumeUtilization = apr.getPercentContainerVolumePacked();
						double boxVolumePercent = apr.getPercentItemVolumePacked();
						double containerTonnageUtilization = apr.getPercentContainerTonnagePacked();
						double boxWeightPercent = apr.getPercentItemWeightPacked();

						long elapsedtime = apr.getPackTimeInMilliseconds();
						String hms = getTimeString(elapsedtime);

						int numPriorities = 1;

						LogEntry logEntry = new LogEntry(datasetDescription, totalBoxes, packedBoxes, unpackedBoxes,
								containerVolumeUtilization, boxVolumePercent, containerTonnageUtilization,
								boxWeightPercent, hms, numPriorities, pType);

						entries.add(logEntry);
					} else {
						System.out.println(String.format(
								"No consignments found for the container #%d for given date %s as pickup timestamp",
								containerID, pickupDate));
					}
				}
				addLogEntries(String.format("clpLog_Db_wBR#%d_%s.csv", thpack, pType), entries);
			}
		} else {
			System.out.println("No containers found, please check if data exists in the database");
		}
	}
}

class Footer implements IEventHandler {

	@Override
	public void handleEvent(Event event) {
		PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
		PdfDocument pdf = docEvent.getDocument();
		PdfPage page = docEvent.getPage();
		Rectangle pageSize = page.getPageSize();
		PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
		Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
		float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
		float y = pageSize.getBottom() + 15;
		canvas.showTextAligned(String.valueOf(pdf.getPageNumber(page)), x, y, TextAlignment.CENTER);
	}
}
