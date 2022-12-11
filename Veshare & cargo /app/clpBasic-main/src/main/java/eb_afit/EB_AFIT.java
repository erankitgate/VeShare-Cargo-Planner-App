package eb_afit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clp5.CLPBasicMain5;
import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;
import clpBasic.utilities.Display;
import eb_afit.AlgorithmType.AlgorithmTypes;

//[start] customClasses

class PackedDimensions {
	private int length, height;

	public PackedDimensions(int x, int z) {
		length = x;
		height = z;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}

//[end] customClasses

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Java implementation of Air Force Bin Packing Algorithm Paper
 * 
 *         Original algorithm designer/paper by - Erhan BALTACIOGLU, AIR FORCE
 *         INSTITUTE OF TECHNOLOGY, Wright-Patterson Air Force Base, Ohio
 * 
 *         Referenced C# repository
 *         https://github.com/davidmchapman/3DContainerPacking
 */
public class EB_AFIT implements IPackingAlgorithm {
	// [start] PublicMethods
	/**
	 * Runs the packing algorithm
	 */
	public AlgorithmPackingResult Run(Container container, List<Consignment> boxes, Point3D containerBase,
			int initialPackedWeight) {
		this.initialPackedWeight = initialPackedWeight;
		bestIteration = 0;
		Initialize(container, boxes);
		CLPBasicMain5.qquit = false;
		ExecuteIterations(container);
		Report(container);

		AlgorithmPackingResult result = new AlgorithmPackingResult();
		result.setAlgorithmID(AlgorithmTypes.EB_AFIT.ordinal());
		result.setAlgorithmName("EB-AFIT");

		for (int i = 1; i <= itemsToPackCount; i++) {
			Consignment ithItemToPack = itemsToPack.get(i);
			ithItemToPack.setQuantity(1);

			if (!ithItemToPack.getIsPacked()) {
				result.getUnpackedItems().add(ithItemToPack);
			}
		}

		sortPackedItemsBasedOnCoordinates(itemsPackedInOrder);
		PackedDimensions packedDimensions = getPackedDimensions(container, itemsPackedInOrder);
		System.out.println(String.format("EBFT PackedLength: %d, PackedHeight: %d", packedDimensions.getLength(),
				packedDimensions.getHeight()));
		System.out.println(String.format("Packed Weight: %d out of %d", packedWeight, containerTonnage));

		for (Consignment c : itemsPackedInOrder) {
			int x = c.getCOX();
			int y = c.getCOY();
			int z = c.getCOZ();
			c.setCOXYZ(x + containerBase.getX(), y + containerBase.getY(), z + containerBase.getZ());
		}
		result.setPackedItems(itemsPackedInOrder);

		if (result.getUnpackedItems().size() == 0) {
			result.setIsCompletePack(true);
		}

		result.setApr_itenum(itenum);
		result.setApr_bestite(bestIteration);
		result.setApr_bestvariant(bestVariant);
		result.setApr_px(containerX);
		result.setApr_py(containerY);
		result.setApr_pz(containerZ);
		result.setPackedLength(packedDimensions.getLength());
		result.setPackedHeight(packedDimensions.getHeight());
		result.setPackedWeight(packedWeight);

		return result;
	}
	// [end] PublicMethods

	// [start] DebugOnlyVariables

	private int customOrientation = 4;
	// int customIteration = 4;

	private boolean dllTraceModeON = false, iterationTraceModeON = false, packedBoxTraceModeON = false,
			functionTraceModeON = false, smoothDebugModeON = true, debugModeON = false, detailedDebugModeON = false;
	// [end] DebugOnlyVariables

	// [start] PrivateVariables

	private int containerTonnage, initialPackedWeight, packedWeight;

	private List<Consignment> itemsToPack; // List of items to be packed
	private List<Consignment> itemsPackedInOrder;
	private List<Layer> layers;
	// private ContainerPackingResult result;

	private int itemsToPackCount; // Old tbn, Count of itemsToPack, excludes dummy boxes

	private ScrapPad scrapfirst;
	private ScrapPad smallestZ;
	private ScrapPad trash;

	private boolean evened;
	private boolean hundredPercentPacked;
	private boolean layerDone;
	private boolean packing;
	private boolean packingBest;
	private boolean quit;

	private int itenum;
	private int bestIteration;
	private int bestVariant;

	private int layerListLen;
	private int packedItemCount;

	private int x;
	private int boxi;
	private int bboxi;
	private int currentBoxIndex;

	private int bfx;
	private int bfy;
	private int bfz;
	private int bbfx;
	private int bbfy;
	private int bbfz;
	private int bweight, bbweight;

	private int boxx;
	private int boxy;
	private int boxz;
	private int bboxx;
	private int bboxy;
	private int bboxz;

	private int currentBoxPackX;
	private int currentBoxPackY;
	private int currentBoxPackZ;

	private int layerInLayer;
	private int layerThickness;
	private int lilz;
	private int packedVolume;
	private int packedContainerY;
	private int prelayer;
	private int prepackedy;
	private int preremainpy;

	private int containerX;
	private int containerY;
	private int containerZ;

	private int remainingContainerY;
	private int remainingContainerZ;

	private int containerVolume;
	private int totalItemVolume;

	// [end] PrivateVariables

	/**
	 * PERFORMS INITIALIZATIONS
	 * 
	 * @param container
	 * @param items
	 */
	private void Initialize(Container container, List<Consignment> items) {
		displayFunctionSign("Initialize()", true);
		// Get itemsToPack
		InputBoxList(items);

		// Get volume of container
		containerVolume = container.getSubVolume();
		containerTonnage = container.getTonnage();

		// Get total volume of boxes
		totalItemVolume = 0;
		for (x = 1; x <= itemsToPackCount; x++) {
			totalItemVolume = totalItemVolume + itemsToPack.get(x).getVolume();
		}

		// Initialize DLL
		scrapfirst = new ScrapPad();
		scrapfirst.Pre = null;
		scrapfirst.Post = null;

		packingBest = false;
		hundredPercentPacked = false;
		itenum = 0;
		quit = false;

		displayFunctionSign("Initialize()", false);
	}

	/**
	 * SETS BOX DATA
	 * 
	 * @param items
	 */
	private void InputBoxList(List<Consignment> items) {
		displayFunctionSign("InputBoxList()", true);

		itemsToPack = new ArrayList<Consignment>();
		itemsPackedInOrder = new ArrayList<Consignment>();
		// result = new ContainerPackingResult();

		// The original code uses 1-based indexing everywhere. This fake entry is added
		// to the beginning of the list to make that possible.
		itemsToPack.add(new Consignment(0, "Dummy Box", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ""));

		layers = new ArrayList<Layer>();
		itemsToPackCount = 0;

		for (Consignment item : items) {
			for (int i = 1; i <= item.getQuantity(); i++) {
				Consignment itemCopy = item.copy();
				itemsToPack.add(itemCopy);
			}

			itemsToPackCount += item.getQuantity();
		}

		itemsToPack.add(new Consignment(0, "Dummy Box", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ""));

		displayBoxes(itemsToPack);

		for (int i = 1; i < itemsToPack.size() - 1;) {
			Consignment box = itemsToPack.get(i);
			int start = i, counter = 1;
			Consignment nextBox = itemsToPack.get(i + counter);
			// !Important: Add conditions based on features supported
			while (box.getLength() == nextBox.getLength() && box.getWidth() == nextBox.getWidth()
					&& box.getHeight() == nextBox.getHeight() && box.getWeight() == nextBox.getWeight()) {
				counter++;
				if (i + counter >= itemsToPack.size() - 1)
					break;
				nextBox = itemsToPack.get(i + counter);
			}
			// System.out.println(counter);
			for (int j = start; j < start + counter; j++) {
				itemsToPack.get(j).setQuantity(counter);
			}
			// displayBoxes(itemsToPack);
			i += counter;
			// System.out.println(i);
		}

		displayBoxes(itemsToPack);
		displayFunctionSign("InputBoxList()", false);
	}

	/**
	 * LISTS ALL POSSIBLE LAYER HEIGHTS BY GIVING A WEIGHT VALUE TO EACH OF THEM
	 */
	private void ListCanditLayers() {
		displayFunctionSign("ListCanditLayers()", true);
		boolean same;

		// Height of layer
		int exdim = 0;

		int dimen2 = 0;
		int dimen3 = 0;
		int y;
		int z;
		int k;
		int layereval;

		layerListLen = 0;

		if (detailedDebugModeON) {
			Display.printSolidLine();
			System.out.println("Creating candidate layers");
		}
		for (x = 1; x <= itemsToPackCount; x++) {
			Consignment xthItemToPack = itemsToPack.get(x);
			if (detailedDebugModeON) {
				Display.printStarLine();
				System.out.println(String.format("Selected box %d", xthItemToPack.getBoxID()));
			}

			int length = xthItemToPack.getLength();
			int width = xthItemToPack.getWidth();
			int height = xthItemToPack.getHeight();

			for (y = 1; y <= 3; y++) {
				switch (y) {
				case 1:
					exdim = length;
					dimen2 = width;
					dimen3 = height;
					break;

				case 2:
					exdim = width;
					dimen2 = length;
					dimen3 = height;
					break;

				case 3:
					exdim = height;
					dimen2 = length;
					dimen3 = width;
					break;
				}

				if (exdim > containerY || ((dimen2 > containerX || dimen3 > containerZ)
						&& (dimen3 > containerX || dimen2 > containerZ))) {

					if (detailedDebugModeON) {
						System.out.println(String.format("Selected dimension %d discarded because ", exdim));

						if ((exdim > containerY))
							System.out.println(String.format("\tit exceeds containerY=%d", containerY));

						else {
							if (dimen2 > containerX || dimen3 > containerZ)
								System.out
										.println(String.format("\t%d exceeds containerX=%d or %d exceeds containerY=%d",
												dimen2, containerX, dimen3, containerZ));

							else if (dimen3 > containerX || dimen2 > containerZ)
								System.out
										.println(String.format("\t%d exceeds containerX=%d or %d exceeds containerY=%d",
												dimen3, containerX, dimen2, containerZ));
						}
					}

					continue;
				}

				same = false;
				for (k = 1; k <= layerListLen; k++) {
					if (exdim == layers.get(k).LayerDim) {
						same = true;
						continue;
					}
				}

				if (same) {
					if (detailedDebugModeON) {
						System.out.println(String.format("Selected dimension %d already examined", exdim));
					}
					continue;
				}
				if (detailedDebugModeON) {
					System.out.println(String.format("Selected dimension %d", exdim));
				}

				layereval = 0;

				String layerDesc = "";
				int a, b, c, dimdif;
				for (z = 1; z <= itemsToPackCount; z++) {
					if (!(x == z)) {
						Consignment zthItemToPack = itemsToPack.get(z);
						a = Math.abs(exdim - zthItemToPack.getLength());
						b = Math.abs(exdim - zthItemToPack.getWidth());
						c = Math.abs(exdim - zthItemToPack.getHeight());
						dimdif = getMin(a, b, c);

						layereval = layereval + dimdif;

						if (z < itemsToPackCount)
							layerDesc += String.format("%d+", dimdif);
						else
							layerDesc += String.format("%d", dimdif);

					}
				}
				if (detailedDebugModeON) {
					System.out.println(String.format("\tLayerEval = %s = %d", layerDesc, layereval));
				}

				layerListLen++;

				layers.add(new Layer());
				layers.get(layerListLen).LayerDim = exdim;
				layers.get(layerListLen).LayerEval = layereval;
				layers.get(layerListLen).LayerEvalDesc = layerDesc;
			}
		}
		displayFunctionSign("ListCanditLayers()", false);
	}

	/**
	 * ITERATIONS ARE DONE AND PARAMETERS OF THE BEST SOLUTION ARE FOUND
	 * 
	 * @param container
	 */
	private void ExecuteIterations(Container container) {
		displayFunctionSign("ExecuteIterations()", true);

		int itelayer;
		int layersIndex;
		int bestVolume = 0;

		// For each value of VARIANT get a different orientation of the container to the
		// variables containerX, containerY, containerZ
		for (int containerOrientationVariant = customOrientation; (containerOrientationVariant <= customOrientation)
				&& !quit; containerOrientationVariant++) {
			String containerOrientationVariantStr = "";
			String packingDirectionStr = "";
			switch (containerOrientationVariant) {
			// LWH (LHW)
			case 1:
				containerX = container.getSub_internal_length();
				containerY = container.getWidth();
				containerZ = container.getSub_internal_height();
				containerOrientationVariantStr = "LWH";
				packingDirectionStr = "LHW";

				// containerX = container.getSub_internal_length();
				// containerY = container.getSub_internal_height();
				// containerZ = container.getWidth();
				break;

			// HWL (WHL)
			case 2:
				containerX = container.getSub_internal_height();
				containerY = container.getWidth();
				containerZ = container.getSub_internal_length();
				containerOrientationVariantStr = "HWL";
				packingDirectionStr = "HLW";

				// containerX = container.getWidth();
				// containerY = container.getSub_internal_height();
				// containerZ = container.getSub_internal_length();
				break;

			// HLW (WLH)
			case 3:
				containerX = container.getSub_internal_height();
				containerY = container.getSub_internal_length();
				containerZ = container.getWidth();
				containerOrientationVariantStr = "HLW";
				packingDirectionStr = "HWL";

				// containerX = container.getWidth();
				// containerY = container.getSub_internal_length();
				// containerZ = container.getSub_internal_height();
				break;

			// WLH (HLW)
			case 4:
				containerX = container.getWidth();
				containerY = container.getSub_internal_length();
				containerZ = container.getSub_internal_height();
				containerOrientationVariantStr = "WLH";
				packingDirectionStr = "WHL";

				// containerX = container.getSub_internal_height();
				// containerY = container.getSub_internal_length();
				// containerZ = container.getWidth();
				break;

			// LHW (LWH)
			case 5:
				containerX = container.getSub_internal_length();
				containerY = container.getSub_internal_height();
				containerZ = container.getWidth();
				containerOrientationVariantStr = "LHW";
				packingDirectionStr = "LWH";

				// containerX = container.getSub_internal_length();
				// containerY = container.getWidth();
				// containerZ = container.getSub_internal_height();
				break;

			// WHL (HWL)
			case 6:
				containerX = container.getWidth();
				containerY = container.getSub_internal_height();
				containerZ = container.getSub_internal_length();
				containerOrientationVariantStr = "WHL";
				packingDirectionStr = "WLH";

				// containerX = container.getSub_internal_height();
				// containerY = container.getWidth();
				// containerZ = container.getSub_internal_length();
				break;
			}

			if (debugModeON) {
				Display.printDashedLine();
				System.out.println(String.format(
						"ContainerOrientationVariant %d(%s) Started\n\tcontainerX=%d,containerY=%d,containerZ=%d\n\tpacking direction x-z-y",
						containerOrientationVariant, containerOrientationVariantStr, containerX, containerY,
						containerZ));
			}

			// Dummy layer to start index from 1
			Layer layer = new Layer();
			layer.LayerEval = -1;
			layers.add(layer);

			// Get candidate layer heights
			// List all possible candidate values by calling LISTCANDITLAYERS();
			ListCanditLayers();
			if (iterationTraceModeON) {
				Display.printSolidLine();
				System.out.println("Candidate Layers Created:");
				displayLayersInfo(layers);
			}

			// Sort the array LAYERS in respect to its LAYEREVAL fields
			sortLayersLayerEval(layers);
			if (iterationTraceModeON) {
				System.out.println("\nSorted Candidate Layers:");
				displayLayersInfo(layers);
			}

			// For each layer values in the LAYERS[] array, perform another iteration
			// starting with that layer value as the starting layer thickness
			for (layersIndex = 1; (layersIndex <= layerListLen) && !quit; layersIndex++) {
				if (smoothDebugModeON) {
					System.out.println(
							String.format("\nQuit: %s, Best Efficiency: %.2f%%, Progress: %.2f%%", CLPBasicMain5.qquit,
									100.0 * bestVolume / containerVolume, 100.0 * (layersIndex - 1) / layerListLen));
				}
				if (CLPBasicMain5.qquit) {
					System.out.println("Stopping Iteration...");
					return;
				}

				++itenum;
				/*
				 * if (itenum <= customIteration - 1) continue; if (itenum > customIteration) {
				 * itenum--; return; }
				 */
				packedVolume = 0;
				packedWeight = initialPackedWeight;
				packedContainerY = 0;
				packing = true;

				// Get the first value of the LAYERS array as the starting LAYERTHICKNESS value
				layerThickness = layers.get(layersIndex).LayerDim;
				if (iterationTraceModeON) {
					Display.printDashedLine();
					System.out.println(String.format(
							"VARIANT: %d (ORIENTATION=%s, PACKING DIRECTION=%s)\tITERATION: %d\tLAYERTHICKNESS: %d\t%d,%d,%d",
							containerOrientationVariant, containerOrientationVariantStr, packingDirectionStr, itenum,
							layerThickness, containerX, containerY, containerZ));
					Display.printDashedLine();
					displayVariables();
				}

				itelayer = layersIndex;
				remainingContainerY = containerY;
				remainingContainerZ = containerZ;
				if (debugModeON) {
					System.out.println(String.format("%-30s\t%d,%d", "containerY,containerZ", containerY, containerZ));
				}

				packedItemCount = 0;

				// Set all boxes' packed status to 0
				for (x = 1; x <= itemsToPackCount; x++) {
					itemsToPack.get(x).setIsPacked(false);
				}

				do {
					// if (smoothDebugModeON) {
					// System.out.println(String.format("Packed #items: %d, Progress: %f%%",
					// packedItemCount, 100.0*layersIndex/layerListLen));
					// }

					// Set the variable that shows remaining unpacked potential second layer height
					// in the current layer
					layerInLayer = 0;

					// Set the flag variable that shows packing of the current layer is finished or
					// not
					layerDone = false;

					// Call PACKLAYER(), to pack the layer
					PackLayer(containerOrientationVariant);

					packedContainerY = packedContainerY + layerThickness;
					remainingContainerY = containerY - packedContainerY;

					// If there is a height available for packing in the current layer, perform
					// another layer packing in the current layer
					if (layerInLayer != 0 && !quit) {
						if (debugModeON) {
							Display.printSolidLine();
							System.out.println("LAYER IN LAYER Started");
						}
						prepackedy = packedContainerY;
						preremainpy = remainingContainerY;
						remainingContainerY = layerThickness - prelayer;
						packedContainerY = packedContainerY - layerThickness + prelayer;
						remainingContainerZ = lilz;

						// Get the height available for packing in the current layer as the layer
						// thickness to be packed
						layerThickness = layerInLayer;
						layerDone = false;

						// Call PACKLAYER(), to pack the layer
						PackLayer(containerOrientationVariant);

						packedContainerY = prepackedy;
						remainingContainerY = preremainpy;
						remainingContainerZ = containerZ;
					}

					// Call FINDLAYER(REMAINPY) to determine the most suitable layer height fitting
					// in the remaining unpacked height of the container
					FindLayer(remainingContainerY);
				} while (packing && !quit);

				if (smoothDebugModeON) {
					System.out.println(String.format("Current Iteration Packed #items:%d", packedItemCount));
					System.out.println(
							String.format("EBFT initialPackedWeight: %d, packedWeight: %d, containerTonnage:%d",
									initialPackedWeight, packedWeight, containerTonnage));
				}

				// If the volume utilization of the current iteration is better than the best so
				// far, and the iterations were not quit, keep the parameters: (Container
				// orientation, utilization, and the index of the initial layer height in the
				// LAYERS array);
				if (packedVolume > bestVolume && !quit) {
					bestVolume = packedVolume;
					bestVariant = containerOrientationVariant;
					bestIteration = itelayer;
					System.out.println(String.format("bestIteration:%d", bestIteration));
				}

				// If a hundred percent packing was found, exit doing iteration and RETURN;
				if (hundredPercentPacked)
					break;
			}

			if (hundredPercentPacked)
				break;

			if ((container.getSub_internal_length() == container.getSub_internal_height())
					&& (container.getSub_internal_height() == container.getWidth()))
				containerOrientationVariant = 6;

			layers = new ArrayList<Layer>();
		}

		displayFunctionSign("ExecuteIterations()", false);
	}

	/**
	 * FINDS THE MOST PROPER LAYER HIGHT BY LOOKING AT THE UNPACKED BOXES AND THE
	 * REMAINING EMPTY SPACE AVAILABLE
	 * 
	 * @param thickness
	 */
	private void FindLayer(int thickness) {
		displayFunctionSign(String.format("FindLayer(%d)", thickness), true);
		int exdim = 0;
		int dimdif;
		int dimen2 = 0;
		int dimen3 = 0;
		int y;
		int z;
		int layereval;
		int eval;
		layerThickness = 0;

		// Set the overall evaluation value to a big number: EVAL=1000000;
		eval = 1000000;

		for (x = 1; x <= itemsToPackCount; x++) {
			Consignment xthItemToPack = itemsToPack.get(x);

			// If the box number X has already been packed continue with the next loop
			if (xthItemToPack.getIsPacked())
				continue;

			// Get each dimension of each box, one at a time by doing:
			for (y = 1; y <= 3; y++) {
				switch (y) {
				case 1:
					exdim = xthItemToPack.getLength();
					dimen2 = xthItemToPack.getWidth();
					dimen3 = xthItemToPack.getHeight();
					break;

				case 2:
					exdim = xthItemToPack.getWidth();
					dimen2 = xthItemToPack.getLength();
					dimen3 = xthItemToPack.getHeight();
					break;

				case 3:
					exdim = xthItemToPack.getHeight();
					dimen2 = xthItemToPack.getLength();
					dimen3 = xthItemToPack.getWidth();
					break;
				}

				// If any of the dimensions of the box being examined cannot fit into the
				// container's respective dimensions, exit this loop and continue with the next
				// loop;

				// Set the evaluation value of the EXDIM to 0
				layereval = 0;

				if ((exdim <= thickness) && (((dimen2 <= containerX) && (dimen3 <= containerZ))
						|| ((dimen3 <= containerX) && (dimen2 <= containerZ)))) {
					for (z = 1; z <= itemsToPackCount; z++) {
						Consignment zthItemToPack = itemsToPack.get(z);

						// Get the closest dimension value of each box to the EXDIM by looking at the
						// absolute values of differences between each dimension and EXDIM, and
						// selecting the smallest value; and set the variable DIMDIF to this value.
						if (!(x == z) && !(zthItemToPack.getIsPacked())) {
							dimdif = Math.abs(exdim - zthItemToPack.getLength());

							if (Math.abs(exdim - zthItemToPack.getWidth()) < dimdif) {
								dimdif = Math.abs(exdim - zthItemToPack.getWidth());
							}

							if (Math.abs(exdim - zthItemToPack.getHeight()) < dimdif) {
								dimdif = Math.abs(exdim - zthItemToPack.getHeight());
							}

							// Add those values cumulatively by doing:
							layereval = layereval + dimdif;
						}
					}

					// If the dimension that has just been examined has a smaller evaluation value,
					// keep that dimension:
					if (layereval < eval) {
						eval = layereval;
						layerThickness = exdim;
					}
				}
			}
		}

		if (layerThickness == 0 || layerThickness > remainingContainerY) {
			packing = false;
			if (debugModeON) {
				System.out
						.println(String.format("LayerThickness Discarded, layerThickness: %d, remainingContainerY: %d",
								layerThickness, remainingContainerY));
			}
		}

		displayFunctionSign(String.format("FindLayer(%d)", thickness), false);
	}

	/**
	 * PACKS THE BOXES FOUND AND ARRANGES ALL VARIABLES AND RECORDS PROPERLY
	 */
	private void PackLayer(int variant) {
		// System.out.println("\n");
		displayFunctionSign(String.format("PackLayer(%d,%s)", layerThickness, smallestZ), true);
		int gapX;
		int gapZ;
		int gapContainerZ;

		// If LAYERTHICKNESS=0 do {PACKING=0; RETURN;};
		if (layerThickness == 0) {
			packing = false;
			if (debugModeON) {
				System.out.println("Packing stopped because layer thickness is 0");
			}
			return;
		}

		// Initialize the first and only node to the layer's X and Z values
		scrapfirst.CumX = containerX;
		scrapfirst.CumZ = 0;

		// Perform an infinite loop unless 'Q' is typed to quit
		for (; !quit;) {
			if (dllTraceModeON) {
				TraverseDLL(scrapfirst);
			}

			// To find the gap with the least Z value in the layer call FINDSMALLEST();
			FindSmallestZ();

			if (smallestZ.Pre == null && smallestZ.Post == null) {
				// *** SITUATION-1: NO BOXES ON THE RIGHT AND LEFT SIDES ***
				if (debugModeON) {
					System.out.println("SITUATION-1: NO BOXES ON THE RIGHT AND LEFT SIDES");
				}

				// Calculate the gap's X and Z dimensions
				gapX = smallestZ.CumX;
				gapContainerZ = remainingContainerZ - smallestZ.CumZ;

				if (debugModeON) {
					System.out.println(String.format("gapX: %d, gapContainerZ:%d", gapX, gapContainerZ));
				}

				// To find the most suitable boxes to the gap found, by looking at
				// the X-dimension of the gap: gapX
				// layerThickness of the gap: layerThickness
				// maximum available thickness to the gap: remainingContainerY
				// maximum available Z dimension to the gap: gapZ
				// call FINDBOX(LENX, LAYERTHICKNESS, REMAINPY, LPZ, LPZ);
				FindBox(gapX, layerThickness, remainingContainerY, gapContainerZ, gapContainerZ);

				// Check on the boxes found by the FINDBOX() function by calling CHECKFOUND();
				CheckFound();

				if (debugModeON) {
					System.out.println(String.format("%-30s\t%s,%s", "layerDone,evened", layerDone, evened));
				}

				// If the packing of the layer is finished, exit the loop
				if (layerDone)
					break;

				// If the edge of the layer is evened, go to the first line of the next loop
				if (evened)
					continue;

				Consignment currentBox1 = itemsToPack.get(currentBoxIndex);
				currentBox1.setCOX(0);
				currentBox1.setCOY(packedContainerY);
				currentBox1.setCOZ(smallestZ.CumZ);

				// Add a new node to the linked list showing the topology of the edge of the
				// currently being packed layer after packing a new box, and set all the
				// necessary variables and pointers properly
				if (currentBoxPackX == smallestZ.CumX) {
					smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
				} else {
					smallestZ.Post = new ScrapPad();

					smallestZ.Post.Post = null;
					smallestZ.Post.Pre = smallestZ;
					smallestZ.Post.CumX = smallestZ.CumX;
					smallestZ.Post.CumZ = smallestZ.CumZ;
					smallestZ.CumX = currentBoxPackX;
					smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
				}
				if (packedBoxTraceModeON) {
					Display.printSolidLine();
					Point3D tCoordinates = getTransformedPoint(currentBox1.getCOX(), currentBox1.getCOY(),
							currentBox1.getCOZ(), variant);
					Point3D tPackedXYZ = getTransformedPoint(currentBoxPackX, currentBoxPackY, currentBoxPackZ,
							variant);
					System.out.println(
							String.format("SIT1: Packed box #%d(%d,%d,%d) at(%d,%d,%d) with orientation (%d,%d,%d)",
									currentBox1.getBoxID(), currentBox1.getLength(), currentBox1.getWidth(),
									currentBox1.getHeight(), tCoordinates.getX(), tCoordinates.getY(),
									tCoordinates.getZ(), tPackedXYZ.getX(), tPackedXYZ.getY(), tPackedXYZ.getZ()));
					Display.printSolidLine();
				}
			} else if (smallestZ.Pre == null) {
				// *** SITUATION-2: NO BOXES ON THE LEFT SIDE ***
				if (debugModeON) {
					System.out.println("SITUATION-2: NO BOXES ON THE LEFT SIDE");
				}

				// Calculate the gap's X and Z dimensions
				gapX = smallestZ.CumX;
				gapZ = smallestZ.Post.CumZ - smallestZ.CumZ;
				gapContainerZ = remainingContainerZ - smallestZ.CumZ;

				// To find the most suitable boxes to the gap found, by looking at
				// the X dimension of the gap: gapX
				// layerThickness of the gap: layerThickness
				// maximum available thickness to the gap: remainingContainerY
				// the Z dimension of the gap: gapZ
				// maximum available Z dimension to the gap: gapContainerZ
				// call FINDBOX(LENX, LAYERTHICKNESS, REMAINPY, LENZ, LPZ);
				FindBox(gapX, layerThickness, remainingContainerY, gapZ, gapContainerZ);

				// Check on the boxes found by the FINDBOX() function by calling CHECKFOUND();
				CheckFound();

				// If the packing of the layer is finished, exit the loop
				if (layerDone)
					break;

				// If the edge of the layer is evened, go to the first line of the next loop
				if (evened)
					continue;

				Consignment currentBox2 = itemsToPack.get(currentBoxIndex);
				currentBox2.setCOY(packedContainerY);
				currentBox2.setCOZ(smallestZ.CumZ);

				// Set all the necessary variables and pointers properly to represent the
				// current topology of the edge of the layer that is currently being packed
				// If the edge of the current layer is evened, set all the necessary variables
				// and pointers properly and dispose the unnecessary node
				if (currentBoxPackX == smallestZ.CumX) {
					currentBox2.setCOX(0);

					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Post.CumZ) {
						smallestZ.CumZ = smallestZ.Post.CumZ;
						smallestZ.CumX = smallestZ.Post.CumX;
						trash = smallestZ.Post;
						smallestZ.Post = smallestZ.Post.Post;

						if (smallestZ.Post != null) {
							smallestZ.Post.Pre = smallestZ;
						}
					} else {
						smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				} else {
					currentBox2.setCOX(smallestZ.CumX - currentBoxPackX);

					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Post.CumZ) {
						smallestZ.CumX = smallestZ.CumX - currentBoxPackX;
					} else {
						smallestZ.Post.Pre = new ScrapPad();

						smallestZ.Post.Pre.Post = smallestZ.Post;
						smallestZ.Post.Pre.Pre = smallestZ;
						smallestZ.Post = smallestZ.Post.Pre;
						smallestZ.Post.CumX = smallestZ.CumX;
						smallestZ.CumX = smallestZ.CumX - currentBoxPackX;
						smallestZ.Post.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				}

				if (packedBoxTraceModeON) {
					Display.printSolidLine();
					Point3D tCoordinates = getTransformedPoint(currentBox2.getCOX(), currentBox2.getCOY(),
							currentBox2.getCOZ(), variant);
					Point3D tPackedXYZ = getTransformedPoint(currentBoxPackX, currentBoxPackY, currentBoxPackZ,
							variant);
					System.out.println(
							String.format("SIT2: Packed box #%d(%d,%d,%d) at (%d,%d,%d) with orientation (%d,%d,%d)",
									currentBox2.getBoxID(), currentBox2.getLength(), currentBox2.getWidth(),
									currentBox2.getHeight(), tCoordinates.getX(), tCoordinates.getY(),
									tCoordinates.getZ(), tPackedXYZ.getX(), tPackedXYZ.getY(), tPackedXYZ.getZ()));
					Display.printSolidLine();
				}
			} else if (smallestZ.Post == null) {
				// *** SITUATION-3: NO BOXES ON THE RIGHT SIDE ***
				if (debugModeON) {
					System.out.println("\nSITUATION-3: NO BOXES ON THE RIGHT SIDE");
				}

				// Calculate the gap's X and Z dimensions
				gapX = smallestZ.CumX - smallestZ.Pre.CumX;
				gapZ = smallestZ.Pre.CumZ - smallestZ.CumZ;
				gapContainerZ = remainingContainerZ - smallestZ.CumZ;

				if (debugModeON) {
					System.out
							.println(String.format("gapX: %d, gapZ: %d, gapContainerZ:%d", gapX, gapZ, gapContainerZ));
				}

				// To find the most suitable boxes to the gap found, by looking at;
				// the X dimension of the gap: gapX,
				// layerThickness of the gap: layerThickness,
				// maximum available thickness to the gap: remainingContainerY,
				// the Z dimension of the gap: gapZ,
				// maximum available Z dimension to the gap: gapContainerZ;
				// call FINDBOX(LENX, LAYERTHICKNESS, REMAINPY,LENZ, LPZ);
				FindBox(gapX, layerThickness, remainingContainerY, gapZ, gapContainerZ);

				// Check on the boxes found by the FINDBOXO function by calling CHECKFOUND();
				CheckFound();

				// If the packing of the layer is finished, exit the loop
				if (layerDone)
					break;

				// If the edge of the layer is evened, go to the first line of the next loop
				if (evened)
					continue;

				Consignment currentBox3 = itemsToPack.get(currentBoxIndex);
				currentBox3.setCOY(packedContainerY);
				currentBox3.setCOZ(smallestZ.CumZ);
				currentBox3.setCOX(smallestZ.Pre.CumX);

				// Set all the necessary variables and pointers properly to represent the
				// current topology of the edge of the layer that is currently being packed;
				// If the edge of the current layer is evened, set all the necessary variables
				// and pointers properly and dispose the unnecessary node;
				if (currentBoxPackX == smallestZ.CumX - smallestZ.Pre.CumX) {
					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Pre.CumZ) {
						smallestZ.Pre.CumX = smallestZ.CumX;
						smallestZ.Pre.Post = null;
					} else {
						smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				} else {
					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Pre.CumZ) {
						smallestZ.Pre.CumX = smallestZ.Pre.CumX + currentBoxPackX;
					} else {
						smallestZ.Pre.Post = new ScrapPad();

						smallestZ.Pre.Post.Pre = smallestZ.Pre;
						smallestZ.Pre.Post.Post = smallestZ;
						smallestZ.Pre = smallestZ.Pre.Post;
						smallestZ.Pre.CumX = smallestZ.Pre.Pre.CumX + currentBoxPackX;
						smallestZ.Pre.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				}

				if (packedBoxTraceModeON) {
					Display.printSolidLine();
					Point3D tCoordinates = getTransformedPoint(currentBox3.getCOX(), currentBox3.getCOY(),
							currentBox3.getCOZ(), variant);
					Point3D tPackedXYZ = getTransformedPoint(currentBoxPackX, currentBoxPackY, currentBoxPackZ,
							variant);
					System.out.println(
							String.format("SIT3: Packed box #%d(%d,%d,%d) at (%d,%d,%d) with orientation (%d,%d,%d)",
									currentBox3.getBoxID(), currentBox3.getLength(), currentBox3.getWidth(),
									currentBox3.getHeight(), tCoordinates.getX(), tCoordinates.getY(),
									tCoordinates.getZ(), tPackedXYZ.getX(), tPackedXYZ.getY(), tPackedXYZ.getZ()));
					Display.printSolidLine();
				}
			} else if (smallestZ.Pre.CumZ == smallestZ.Post.CumZ) {
				// *** SITUATION-4: THERE ARE BOXES ON BOTH OF THE SIDES ***
				if (debugModeON) {
					System.out.println("\\nSITUATION-4: THERE ARE BOXES ON BOTH OF THE SIDES");
				}

				// *** SUBSITUATION-4A: SIDES ARE EQUAL TO EACH OTHER ***
				if (debugModeON) {
					System.out.println("SUBSITUATION-4A: SIDES ARE EQUAL TO EACH OTHER");
				}

				// Calculate the gap's X and Z dimensions
				gapX = smallestZ.CumX - smallestZ.Pre.CumX;
				gapZ = smallestZ.Pre.CumZ - smallestZ.CumZ;
				gapContainerZ = remainingContainerZ - smallestZ.CumZ;

				// To find the most suitable boxes to the gap found, by looking at;
				// the X dimension of the gap: gapX,
				// layerThickness of the gap: layerThickness,
				// maximum available thickness to the gap: remainingContainerY,
				// the Z dimension of the gap: gapZ,
				// maximum available Z dimension to the gap: gapContainerZ;
				// call FINDBOX(LENX, LAYERTHICKNESS, REMAINPY, LENZ, LPZ);
				FindBox(gapX, layerThickness, remainingContainerY, gapZ, gapContainerZ);

				// Check on the boxes found by the FINDBOXO function by calling CHECKFOUND();
				CheckFound();

				// If the packing of the layer is finished, exit the loop
				if (layerDone)
					break;

				// If the edge of the layer is evened, go to the first line of the next loop
				if (evened)
					continue;

				Consignment currentBox4A = itemsToPack.get(currentBoxIndex);
				currentBox4A.setCOY(packedContainerY);
				currentBox4A.setCOZ(smallestZ.CumZ);

				// Set all the necessary variables and pointers properly to represent the
				// current topology of the edge of the layer that is currently being packed;
				// While updating the edge of topology information, if a part of the topology is
				// evened, dispose unnecessary nodes, and update the others properly;
				// While updating the edge of topology information, if another gap is added to
				// the topology, add a new node to keep this gap, and update the others
				// properly;
				if (currentBoxPackX == smallestZ.CumX - smallestZ.Pre.CumX) {
					currentBox4A.setCOX(smallestZ.Pre.CumX);

					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Post.CumZ) {
						smallestZ.Pre.CumX = smallestZ.Post.CumX;

						if (smallestZ.Post.Post != null) {
							smallestZ.Pre.Post = smallestZ.Post.Post;
							smallestZ.Post.Post.Pre = smallestZ.Pre;
						} else {
							smallestZ.Pre.Post = null;
						}
					} else {
						smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				} else if (smallestZ.Pre.CumX < containerX - smallestZ.CumX) {
					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Pre.CumZ) {
						smallestZ.CumX = smallestZ.CumX - currentBoxPackX;
						currentBox4A.setCOX(smallestZ.CumX);
					} else {
						currentBox4A.setCOX(smallestZ.Pre.CumX);
						smallestZ.Pre.Post = new ScrapPad();

						smallestZ.Pre.Post.Pre = smallestZ.Pre;
						smallestZ.Pre.Post.Post = smallestZ;
						smallestZ.Pre = smallestZ.Pre.Post;
						smallestZ.Pre.CumX = smallestZ.Pre.Pre.CumX + currentBoxPackX;
						smallestZ.Pre.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				} else {
					if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Pre.CumZ) {
						smallestZ.Pre.CumX = smallestZ.Pre.CumX + currentBoxPackX;
						currentBox4A.setCOX(smallestZ.Pre.CumX);
					} else {
						currentBox4A.setCOX(smallestZ.CumX - currentBoxPackX);
						smallestZ.Post.Pre = new ScrapPad();

						smallestZ.Post.Pre.Post = smallestZ.Post;
						smallestZ.Post.Pre.Pre = smallestZ;
						smallestZ.Post = smallestZ.Post.Pre;
						smallestZ.Post.CumX = smallestZ.CumX;
						smallestZ.Post.CumZ = smallestZ.CumZ + currentBoxPackZ;
						smallestZ.CumX = smallestZ.CumX - currentBoxPackX;
					}
				}
				if (packedBoxTraceModeON) {
					Display.printSolidLine();
					Point3D tCoordinates = getTransformedPoint(currentBox4A.getCOX(), currentBox4A.getCOY(),
							currentBox4A.getCOZ(), variant);
					Point3D tPackedXYZ = getTransformedPoint(currentBoxPackX, currentBoxPackY, currentBoxPackZ,
							variant);
					System.out.println(
							String.format("SIT4A: Packed box #%d(%d,%d,%d) at (%d,%d,%d) with orientation (%d,%d,%d)",
									currentBox4A.getBoxID(), currentBox4A.getLength(), currentBox4A.getWidth(),
									currentBox4A.getHeight(), tCoordinates.getX(), tCoordinates.getY(),
									tCoordinates.getZ(), tPackedXYZ.getX(), tPackedXYZ.getY(), tPackedXYZ.getZ()));
					Display.printSolidLine();
				}
			} else {
				// *** SUBSITUATION-4B: SIDES ARE NOT EQUAL TO EACH OTHER ***
				if (debugModeON) {
					System.out.println("SUBSITUATION-4B: SIDES ARE NOT EQUAL TO EACH OTHER");
				}

				// Calculate the gap's X and Z dimensions
				gapX = smallestZ.CumX - smallestZ.Pre.CumX;
				gapZ = smallestZ.Pre.CumZ - smallestZ.CumZ;
				gapContainerZ = remainingContainerZ - smallestZ.CumZ;

				// To find the most suitable boxes to the gap found, by looking at;
				// the X dimension of the gap: gapX,
				// layerThickness of the gap: layerThickness,
				// maximum available thickness to the gap: remainingContainerY,
				// the Z dimension of the gap: gapZ,
				// maximum available Z dimension to the gap: gapContainerZ;
				// call FINDBOX(LENX, LAYERTHICKNESS, REMAINPY, LENZ, LPZ);
				FindBox(gapX, layerThickness, remainingContainerY, gapZ, gapContainerZ);

				// Check on the boxes found by the FINDBOXO function by calling CHECKFOUND();
				CheckFound();

				// If the packing of the layer is finished, exit the loop
				if (layerDone)
					break;

				// If the edge of the layer is evened, go to the first line of the next loop
				if (evened)
					continue;

				Consignment currentBox4B = itemsToPack.get(currentBoxIndex);
				currentBox4B.setCOY(packedContainerY);
				currentBox4B.setCOZ(smallestZ.CumZ);
				currentBox4B.setCOX(smallestZ.Pre.CumX);

				// Set all the necessary variables and pointers properly to represent the
				// current topology of the edge of the layer that is currently being packed;
				// While updating the edge of topology information, if another gap is added to
				// the topology, add a new node to keep this gap, and update the others
				// properly;
				if (currentBoxPackX == (smallestZ.CumX - smallestZ.Pre.CumX)) {
					if ((smallestZ.CumZ + currentBoxPackZ) == smallestZ.Pre.CumZ) {
						smallestZ.Pre.CumX = smallestZ.CumX;
						smallestZ.Pre.Post = smallestZ.Post;
						smallestZ.Post.Pre = smallestZ.Pre;
					} else {
						smallestZ.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				} else {
					if ((smallestZ.CumZ + currentBoxPackZ) == smallestZ.Pre.CumZ) {
						smallestZ.Pre.CumX = smallestZ.Pre.CumX + currentBoxPackX;
					} else if (smallestZ.CumZ + currentBoxPackZ == smallestZ.Post.CumZ) {
						currentBox4B.setCOX(smallestZ.CumX - currentBoxPackX);
						smallestZ.CumX = smallestZ.CumX - currentBoxPackX;
					} else {
						smallestZ.Pre.Post = new ScrapPad();

						smallestZ.Pre.Post.Pre = smallestZ.Pre;
						smallestZ.Pre.Post.Post = smallestZ;
						smallestZ.Pre = smallestZ.Pre.Post;
						smallestZ.Pre.CumX = smallestZ.Pre.Pre.CumX + currentBoxPackX;
						smallestZ.Pre.CumZ = smallestZ.CumZ + currentBoxPackZ;
					}
				}
				if (packedBoxTraceModeON) {
					Display.printSolidLine();
					Point3D tCoordinates = getTransformedPoint(currentBox4B.getCOX(), currentBox4B.getCOY(),
							currentBox4B.getCOZ(), variant);
					Point3D tPackedXYZ = getTransformedPoint(currentBoxPackX, currentBoxPackY, currentBoxPackZ,
							variant);
					System.out.println(
							String.format("SIT4B: Packed box #%d(%d,%d,%d) at (%d,%d,%d) with orientation (%d,%d,%d)",
									currentBox4B.getBoxID(), currentBox4B.getLength(), currentBox4B.getWidth(),
									currentBox4B.getHeight(), tCoordinates.getX(), tCoordinates.getY(),
									tCoordinates.getZ(), tPackedXYZ.getX(), tPackedXYZ.getY(), tPackedXYZ.getZ()));
					Display.printSolidLine();
				}
			}

			// To check the hundred percent packing condition, call VOLUMECHECK();
			VolumeCheck();
		}
		displayFunctionSign(String.format("PackLayer(%d,%s)", layerThickness, smallestZ), false);
	}

	/**
	 * FINDS THE FIRST TO BE PACKED GAP IN THE LAYER EDGE
	 */
	private void FindSmallestZ() {
		displayFunctionSign("FindSmallestZ()", true);
		ScrapPad scrapmemb = scrapfirst;
		smallestZ = scrapmemb;

		while (scrapmemb.Post != null) {
			if (scrapmemb.Post.CumZ < smallestZ.CumZ) {
				if (debugModeON) {
					System.out.println("Found new CumZ");
				}
				smallestZ = scrapmemb.Post;
			}

			scrapmemb = scrapmemb.Post;
		}
		if (dllTraceModeON) {
			System.out.println("SmallestZ\t" + smallestZ);
		}
		displayFunctionSign("FindSmallestZ()", false);
	}

	/**
	 * FINDS THE MOST PROPER BOXES BY LOOKING AT ALL SIX POSSIBLE ORIENTATIONS,
	 * EMPTY SPACE GIVEN, ADJACENT BOXES, AND PALLET LIMITS
	 * 
	 * @param hmx = Length available
	 * @param hy  = Layer height/thickness
	 * @param hmy = Total height
	 * @param hz  = Layer width
	 * @param hmz = Width available
	 */
	private void FindBox(int hmx, int hy, int hmy, int hz, int hmz) {
		displayFunctionSign(String.format("FindBox(%d,%d,%d,%d,%d)", hmx, hy, hmy, hz, hmz), true);
		if (debugModeON) {
			System.out.println(String.format("FindBox(%d, %d, %d, %d, %d)", hmx, hy, hmy, hz, hmz));
		}
		int y;

		// For the box type fitting in the current layerthickness
		bfx = 32767;
		bfy = 32767;
		bfz = 32767;

		// For the box type that cannot fit in the current layerthickness, but the
		// closest one
		bbfx = 32767;
		bbfy = 32767;
		bbfz = 32767;

		boxi = 0;
		bboxi = 0;

		for (y = 1; y <= itemsToPackCount; y += itemsToPack.get(y).getQuantity()) {
			// Find the first unpacked box
			for (x = y; x < x + itemsToPack.get(y).getQuantity() - 1; x++) {
				if (x >= itemsToPack.size()) {
					System.out.println("No suitable unpacked box found");
					break;
				}
				if (!itemsToPack.get(x).getIsPacked()
						&& itemsToPack.get(x).getWeight() + packedWeight <= containerTonnage)
					break;
			}

			Consignment xthItemToPack = itemsToPack.get(x);
			if (xthItemToPack.getIsPacked()) {
				if (debugModeON) {
					System.out.println(String.format("Box #%s found but is packed already", xthItemToPack.getBoxID()));
				}
				continue;
			}

			if (x > itemsToPackCount) {
				if (debugModeON) {
					System.out.println(String.format("Index x=%d exceeded itemsToPackCount=%d", x, itemsToPackCount));
				}
				return;
			}

			int length = xthItemToPack.getLength();
			int width = xthItemToPack.getWidth();
			int height = xthItemToPack.getHeight();
			int weight = xthItemToPack.getWeight();
			if (debugModeON) {
				System.out.println(String.format("\nUnpacked box #%s(%d,%d,%d) found", xthItemToPack.getBoxID(), length,
						width, height));
			}

			// Analyze all six possible orientations of the box being examined
			if (debugModeON) {
				System.out.println(
						String.format("AnalyzeBox(hmx=%d,hy=%d,hmy=%d,hz=%d,hmz=%d,x,x,x)", hmx, hy, hmy, hz, hmz));
			}
			if (xthItemToPack.getHeight_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, length, width, height, weight);
			}

			if ((length == height) && (height == width)) {
				System.out.println("Perfect cube, skip futher AnalyzeBox");
				continue;
			}

			if (xthItemToPack.getWidth_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, length, height, width, weight);
			}
			if (xthItemToPack.getHeight_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, width, length, height, weight);
			}
			if (xthItemToPack.getLength_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, width, height, length, weight);
			}
			if (xthItemToPack.getWidth_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, height, length, width, weight);
			}
			if (xthItemToPack.getLength_upright_allowed()) {
				AnalyzeBox(hmx, hy, hmy, hz, hmz, height, width, length, weight);
			}
		}
		if (debugModeON) {
			String[] str4 = { "x,boxi,bboxi,cboxi" };
			String[] var4 = { String.format("%d,%d,%d,%d", x, boxi, bboxi, currentBoxIndex) };
			for (int i = 0; i < str4.length; i++) {
				System.out.println(String.format("%-30s\t%s", str4[i], var4[i]));
			}

			String[] str5 = { "bf: x,y,z", "bbf: x,y,z" };
			String[] var5 = { String.format("%d,%d,%d", bfx, bfy, bfz), String.format("%d,%d,%d", bbfx, bbfy, bbfz) };
			for (int i = 0; i < str5.length; i++) {
				System.out.println(String.format("%-30s\t%s", str5[i], var5[i]));
			}

			String[] str6 = { "box: x,y,z", "bbox: x,y,z" };
			String[] var6 = { String.format("%d,%d,%d", boxx, boxy, boxz),
					String.format("%d,%d,%d", bboxx, bboxy, bboxz) };
			for (int i = 0; i < str6.length; i++) {
				System.out.println(String.format("%-30s\t%s", str6[i], var6[i]));
			}
		}
		displayFunctionSign(String.format("FindBox(%d,%d,%d,%d,%d)", hmx, hy, hmy, hz, hmz), false);
	}

	/**
	 * ANALYZES EACH UNPACKED BOX TO FIND THE BEST FITTING ONE TO THE EMPTY SPACE
	 * GIVEN
	 * 
	 * @param hmx  = Length available = Maximum X dimension of the gap
	 * @param hy   = Layer height/thickness = Y dimension of the gap
	 * @param hmy  = Height available = Maximum Y dimension of the gap
	 * @param hz   = Layer width = Z dimension of the gap
	 * @param hmz  = Width available = Maximum Z dimension of the gap
	 * @param dim1
	 * @param dim2
	 * @param dim3
	 */
	private void AnalyzeBox(int hmx, int hy, int hmy, int hz, int hmz, int dim1, int dim2, int dim3, int weight) {
		// displayFunctionSign(String.format("AnalyzeBox(%d,%d,%d,%d,%d,%d,%d,%d)",hmx,
		// hy, hmy, hz, hmz,dim1,dim2,dim3), true);
		String DEBUG = "";
		boolean FLAG = false;
		DEBUG += String.format("%d,%d,%d", dim1, dim2, dim3);

		// Check if dim1 is within Length available &
		// dim2 is within Total height &
		// dim3 is within Available width
		if (dim1 <= hmx && dim2 <= hmy && dim3 <= hmz) {

			if (dim2 <= hy) {
				// Check if dim2 <= Layer height
				if (dim2 < hy)
					DEBUG += "\tUnderThick";
				else
					DEBUG += "\tPerfectThick";

				if (hy - dim2 < bfy) {
					// If the current box is a better fit in respect to its y-dimension compared to
					// the one selected before, keep the index of the current box in the variable
					// BOXI;
					boxx = dim1;
					boxy = dim2;
					boxz = dim3;
					bweight = weight;
					bfx = hmx - dim1;
					bfy = hy - dim2;
					bfz = Math.abs(hz - dim3);
					boxi = x;

					DEBUG += "\tbfy";
					FLAG = true;
				} else if (hy - dim2 == bfy && hmx - dim1 < bfx) {
					// If the current box has the same y-dimension as the y-dimension of the
					// selected one before, and the current box is a better fit in respect to its
					// x-dimension compared to the selected one before, keep the index of the
					// current box in the variable BOXI;
					boxx = dim1;
					boxy = dim2;
					boxz = dim3;
					bweight = weight;
					bfx = hmx - dim1;
					bfy = hy - dim2;
					bfz = Math.abs(hz - dim3);
					boxi = x;

					DEBUG += "\tbfy,bfx";
					FLAG = true;
				} else if (hy - dim2 == bfy && hmx - dim1 == bfx && Math.abs(hz - dim3) < bfz) {
					// If the current box has the same y and x-dimensions as the y and x dimensions
					// of the selected one before, and the current box is a better fit in respect to
					// its z-dimension compared to the one selected before, keep the index of the
					// current box in the variable BOXI;
					boxx = dim1;
					boxy = dim2;
					boxz = dim3;
					bweight = weight;
					bfx = hmx - dim1;
					bfy = hy - dim2;
					bfz = Math.abs(hz - dim3);
					boxi = x;

					DEBUG += "\tbfy,bfx,bfz";
					FLAG = true;
				} else if (hy - dim2 == bfy && hmx - dim1 == bfx && Math.abs(hz - dim3) == bfz && weight > bweight) {
					// If the current box has the all three y, x and z-dimensions same as the y, x and z-dimensions
					// of the selected one before, and the current box has greater weight
					// compared to the one selected before, keep the index of the
					// current box in the variable BOXI;
					boxx = dim1;
					boxy = dim2;
					boxz = dim3;
					bweight = weight;
					bfx = hmx - dim1;
					bfy = hy - dim2;
					bfz = Math.abs(hz - dim3);
					boxi = x;

					DEBUG += "\tbfy,bfx,bfz,bweight";
					FLAG = true;
				} else {
					DEBUG += "\tNA";
				}
			} else {
				// dim2 > Layer height
				DEBUG += "\tOverThick";

				if (debugModeON) {
					System.out.println(String.format("Checking (%d,%d)\t(%d,%d)\t(%d,%d)", dim2 - hy, bbfy, hmx - dim1,
							bbfx, Math.abs(hz - dim3), bbfz));
				}

				if (dim2 - hy < bbfy) {
					// If the current box is a better fit in respect to its y-dimension compared to
					// the one selected before, keep the index of the current box in the variable
					// BBOXI;
					bboxx = dim1;
					bboxy = dim2;
					bboxz = dim3;
					bbweight = weight;
					bbfx = hmx - dim1;
					bbfy = dim2 - hy;
					bbfz = Math.abs(hz - dim3);
					bboxi = x;

					DEBUG += "\tbbfy";
					FLAG = true;
				} else if (dim2 - hy == bbfy && hmx - dim1 < bbfx) {
					// If the current box has the same y-dimension as the y-dimension of the
					// selected one before, and the current box is a better fit in respect to its
					// x-dimension compared to the selected one before, keep the index of the
					// current box in the variable BBOXI;
					bboxx = dim1;
					bboxy = dim2;
					bboxz = dim3;
					bbweight = weight;
					bbfx = hmx - dim1;
					bbfy = dim2 - hy;
					bbfz = Math.abs(hz - dim3);
					bboxi = x;
					DEBUG += "\tbbfy,bbfx";
					FLAG = true;
				} else if (dim2 - hy == bbfy && hmx - dim1 == bbfx && Math.abs(hz - dim3) < bbfz) {
					// If the current box has the same y and x-dimensions as the y and x dimensions
					// of the selected one before, and the current box is a better fit in respect to
					// its z-dimension compared to the one selected before, keep the index of the
					// current box in the variable BBOXI;
					bboxx = dim1;
					bboxy = dim2;
					bboxz = dim3;
					bbweight = weight;
					bbfx = hmx - dim1;
					bbfy = dim2 - hy;
					bbfz = Math.abs(hz - dim3);
					bboxi = x;

					DEBUG += "\tbbfy,bbfx,bbfz";
					FLAG = true;
				} else if (dim2 - hy == bbfy && hmx - dim1 == bbfx && Math.abs(hz - dim3) == bbfz
						&& weight > bbweight) {
					// If the current box has the all three y, x and z-dimensions same as the y, x and z-dimensions
					// of the selected one before, and the current box has greater weight
					// compared to the one selected before, keep the index of the
					// current box in the variable BBOXI;
					bboxx = dim1;
					bboxy = dim2;
					bboxz = dim3;
					bbweight = weight;
					bbfx = hmx - dim1;
					bbfy = dim2 - hy;
					bbfz = Math.abs(hz - dim3);
					bboxi = x;

					DEBUG += "\tbbfy,bbfx,bbfz,bbweight";
					FLAG = true;
				} else {
					if (!(dim2 - hy == bbfy && hmx - dim1 < bbfx))
						DEBUG += "\tNBx";
					else
						DEBUG += "\tNBz";
				}
			}
		} else {
			if (dim1 > hmx)
				DEBUG += "\tOverflowX";
			if (dim2 > hmy)
				DEBUG += "\tOverflowY";
			if (dim3 > hmz)
				DEBUG += "\tOverflowZ";
		}
		if (debugModeON) {
			if (FLAG) {
				DEBUG += String.format("\n%d,%d,%d,%d,%d,%d", bfx, bfy, bfz, bbfx, bbfy, bbfz);
				System.out.println(DEBUG);
			} else {
				DEBUG += "\tReject";
				System.out.println(DEBUG);
			}
		}
		// System.out.println(
		// String.format("bfx=%d,bfy=%d,bfz=%d,bbfx=%d,bbfy=%d,bbfz=%d\n", bfx, bfy,
		// bfz, bbfx, bbfy, bbfz));
		// displayFunctionSign(String.format("AnalyzeBox(%d,%d,%d,%d,%d,%d,%d,%d)",hmx,
		// hy, hmy, hz, hmz,dim1,dim2,dim3), false);
	}

	/**
	 * AFTER FINDING EACH BOX, THE CANDIDATE BOXES AND THE CONDITION OF THE LAYER
	 * ARE EXAMINED
	 */
	private void CheckFound() {
		displayFunctionSign("CheckFound()", true);
		evened = false;

		if (boxi != 0 && itemsToPack.get(boxi).getWeight() + packedWeight <= containerTonnage) {
			// System.out.println(String.format("boxi: %d called", itemsToPack.get(boxi).getBoxID()));
			// If a box fitting in the current layer thickness has been found, keep its
			// index and orientation for packing
			currentBoxIndex = boxi;
			currentBoxPackX = boxx;
			currentBoxPackY = boxy;
			currentBoxPackZ = boxz;
			if (debugModeON) {
				System.out.println(
						String.format("Selected proper box #%d with orientation (%d,%d,%d)", boxi, boxx, boxy, boxz));
				System.out.println(String.format("cboxi: %d\tcboxxyz: (%d,%d,%d)", boxi, boxx, boxy, boxz));
			}
		} else {
			if ((bboxi > 0 && itemsToPack.get(bboxi).getWeight() + packedWeight <= containerTonnage) && (layerInLayer != 0 || (smallestZ.Pre == null && smallestZ.Post == null))) {
				// System.out.println(String.format("bboxi: %d called", itemsToPack.get(bboxi).getBoxID()));
				// If a box with a bigger y-dimension than the current layer thickness has been
				// found and the edge of the current layer is even then select that box and set
				// LAYERINLAYER variable for a second layer packing in the current layer and
				// update the LAYERTHICKNESS=BBOXY;
				if (layerInLayer == 0) {
					prelayer = layerThickness;
					lilz = smallestZ.CumZ;
				}

				currentBoxIndex = bboxi;
				currentBoxPackX = bboxx;
				currentBoxPackY = bboxy;
				currentBoxPackZ = bboxz;
				layerInLayer = layerInLayer + bboxy - layerThickness;
				layerThickness = bboxy;
			} else {
				currentBoxIndex = 0;
				if (smallestZ.Pre == null && smallestZ.Post == null) {
					// If there is no gap in the edge of the current layer, packing of the layer is
					// done:LAYERDONE=l;
					layerDone = true;
				} else {
					// Else: Since there is no fitting box to the currently selected gap, skip that
					// gap and even it by arranging and updating the necessary nodes and variables;
					evened = true;

					if (smallestZ.Pre == null) {
						trash = smallestZ.Post;
						smallestZ.CumX = smallestZ.Post.CumX;
						smallestZ.CumZ = smallestZ.Post.CumZ;
						smallestZ.Post = smallestZ.Post.Post;
						if (smallestZ.Post != null) {
							smallestZ.Post.Pre = smallestZ;
						}
					} else if (smallestZ.Post == null) {
						smallestZ.Pre.Post = null;
						smallestZ.Pre.CumX = smallestZ.CumX;
					} else {
						if (smallestZ.Pre.CumZ == smallestZ.Post.CumZ) {
							smallestZ.Pre.Post = smallestZ.Post.Post;

							if (smallestZ.Post.Post != null) {
								smallestZ.Post.Post.Pre = smallestZ.Pre;
							}

							smallestZ.Pre.CumX = smallestZ.Post.CumX;
						} else {
							smallestZ.Pre.Post = smallestZ.Post;
							smallestZ.Post.Pre = smallestZ.Pre;

							if (smallestZ.Pre.CumZ < smallestZ.Post.CumZ) {
								smallestZ.Pre.CumX = smallestZ.CumX;
							}
						}
					}
				}
			}
		}
		displayFunctionSign("CheckFound()", false);
	}

	/**
	 * AFTER PACKING OF EACH BOX, 100% PACKING CONDITION IS CHECKED
	 */
	private void VolumeCheck() {
		displayFunctionSign("VolumeCheck()", true);

		if (itemsToPack.get(currentBoxIndex).getWeight() + packedWeight <= containerTonnage) {
			// Mark the current box as packed
			itemsToPack.get(currentBoxIndex).setIsPacked(true);
			// System.out.println(String.format("box: %d packed", itemsToPack.get(currentBoxIndex).getBoxID()));

			// Keep the orientation of the current box as it is packed
			itemsToPack.get(currentBoxIndex).setPackXYZ(currentBoxPackX, currentBoxPackY, currentBoxPackZ);

			// Update the total packed volume
			packedVolume = packedVolume + itemsToPack.get(currentBoxIndex).getVolume();
			packedWeight += itemsToPack.get(currentBoxIndex).getWeight();
			if (debugModeON) {
				System.out.println(String.format("Packed Volume: %d out of %d", packedVolume, containerVolume));
				System.out.println(String.format("Packed Weight: %d out of %d", packedWeight, containerTonnage));
			}

			// Update the number of boxes packed
			packedItemCount++;

			// If performing the best so far packing after being done with the iterations
			if (packingBest) {
				if (packedWeight > containerTonnage) {
					packedWeight -= itemsToPack.get(currentBoxIndex).getWeight();
					return;
				}
				// Write the information of the packed box to the report file
				OutputBoxList();
			} else if (packedVolume == containerVolume || packedVolume == totalItemVolume
					|| packedWeight >= containerTonnage) {
				if (packedWeight > containerTonnage) {
					packedWeight -= itemsToPack.get(currentBoxIndex).getWeight();
				}
				// if a hundred percent packing of the container has been reached or the total
				// volume of the packed boxes is equal to the total volume of the input box set

				// Packing is finished
				packing = false;

				// A hundred percent packing has been reached
				hundredPercentPacked = true;
			}
			displayVariables();
		}
		displayFunctionSign("VolumeCheck()", false);
	}

	private void OutputBoxList() {
		displayFunctionSign("OutputBoxList()", true);
		int packCoordX = 0;
		int packCoordY = 0;
		int packCoordZ = 0;
		int packDimX = 0;
		int packDimY = 0;
		int packDimZ = 0;

		// Transform the coordinate system and orientation of every box from the best
		// solution format to the container orientation entered by the user in the input
		// text file by looking at the value of the variable BESTVARIANT
		switch (bestVariant) {
		case 1:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOX();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOY();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOZ();
			packDimX = itemsToPack.get(currentBoxIndex).getPackX();
			packDimY = itemsToPack.get(currentBoxIndex).getPackY();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackZ();
			break;

		case 2:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOZ();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOY();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOX();
			packDimX = itemsToPack.get(currentBoxIndex).getPackZ();
			packDimY = itemsToPack.get(currentBoxIndex).getPackY();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackX();
			break;

		case 3:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOY();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOZ();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOX();
			packDimX = itemsToPack.get(currentBoxIndex).getPackY();
			packDimY = itemsToPack.get(currentBoxIndex).getPackZ();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackX();
			break;

		case 4:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOY();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOX();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOZ();
			packDimX = itemsToPack.get(currentBoxIndex).getPackY();
			packDimY = itemsToPack.get(currentBoxIndex).getPackX();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackZ();
			break;

		case 5:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOX();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOZ();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOY();
			packDimX = itemsToPack.get(currentBoxIndex).getPackX();
			packDimY = itemsToPack.get(currentBoxIndex).getPackZ();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackY();
			break;

		case 6:
			packCoordX = itemsToPack.get(currentBoxIndex).getCOZ();
			packCoordY = itemsToPack.get(currentBoxIndex).getCOX();
			packCoordZ = itemsToPack.get(currentBoxIndex).getCOY();
			packDimX = itemsToPack.get(currentBoxIndex).getPackZ();
			packDimY = itemsToPack.get(currentBoxIndex).getPackX();
			packDimZ = itemsToPack.get(currentBoxIndex).getPackY();
			break;
		}

		itemsToPack.get(currentBoxIndex).setCOXYZ(packCoordX, packCoordY, packCoordZ);
		itemsToPack.get(currentBoxIndex).setPackX(packDimX);
		itemsToPack.get(currentBoxIndex).setPackY(packDimY);
		itemsToPack.get(currentBoxIndex).setPackZ(packDimZ);

		itemsPackedInOrder.add(itemsToPack.get(currentBoxIndex));
		displayFunctionSign("OutputBoxList()", false);
	}

	private void Report(Container container) {
		displayFunctionSign("Report()", true);
		debugModeON = false;
		functionTraceModeON = false;
		quit = false;

		String bestContainerOrientationVariantStr = "";
		String bestPackingDirectionStr = "";

		switch (bestVariant) {

		// LWH (LHW)
		case 1:
			containerX = container.getSub_internal_length();
			containerY = container.getWidth();
			containerZ = container.getSub_internal_height();

			bestContainerOrientationVariantStr = "LWH";
			bestPackingDirectionStr = "LHW";

			// containerX = container.getSub_internal_length();
			// containerY = container.getSub_internal_height();
			// containerZ = container.getWidth();
			break;

		// HWL (WHL)
		case 2:
			containerX = container.getSub_internal_height();
			containerY = container.getWidth();
			containerZ = container.getSub_internal_length();

			bestContainerOrientationVariantStr = "HWL";
			bestPackingDirectionStr = "HLW";

			// containerX = container.getWidth();
			// containerY = container.getSub_internal_height();
			// containerZ = container.getSub_internal_length();
			break;

		// HLW (WLH)
		case 3:
			containerX = container.getSub_internal_height();
			containerY = container.getSub_internal_length();
			containerZ = container.getWidth();

			bestContainerOrientationVariantStr = "HLW";
			bestPackingDirectionStr = "HWL";

			// containerX = container.getWidth();
			// containerY = container.getSub_internal_length();
			// containerZ = container.getSub_internal_height();
			break;

		// WLH (HLW)
		case 4:
			containerX = container.getWidth();
			containerY = container.getSub_internal_length();
			containerZ = container.getSub_internal_height();

			bestContainerOrientationVariantStr = "WLH";
			bestPackingDirectionStr = "WHL";

			// containerX = container.getSub_internal_height();
			// containerY = container.getSub_internal_length();
			// containerZ = container.getWidth();
			break;

		// LHW (LWH)
		case 5:
			containerX = container.getSub_internal_length();
			containerY = container.getSub_internal_height();
			containerZ = container.getWidth();

			bestContainerOrientationVariantStr = "LHW";
			bestPackingDirectionStr = "LWH";

			// containerX = container.getSub_internal_length();
			// containerY = container.getWidth();
			// containerZ = container.getSub_internal_height();
			break;

		// WHL (HWL)
		case 6:
			containerX = container.getWidth();
			containerY = container.getSub_internal_height();
			containerZ = container.getSub_internal_length();

			bestContainerOrientationVariantStr = "WHL";
			bestPackingDirectionStr = "WLH";

			// containerX = container.getSub_internal_height();
			// containerY = container.getWidth();
			// containerZ = container.getSub_internal_length();
			break;
		}

		if (iterationTraceModeON) {
			Display.printDashedLine();
			System.out.println(String.format(
					"REPORT VARIANT: %d (ORIENTATION=%s, PACKING DIRECTION=%s)\tITERATION: %d\tLAYERTHICKNESS: %d\t%d,%d,%d",
					bestVariant, bestContainerOrientationVariantStr, bestPackingDirectionStr, bestIteration,
					layerThickness, containerX, containerY, containerZ));
			Display.printDashedLine();
			displayVariables();
		}

		packingBest = true;
		layers.clear();
		Layer layer = new Layer();
		layer.LayerEval = -1;
		layers.add(layer);
		ListCanditLayers();
		sortLayersLayerEval(layers);
		packedVolume = 0;
		packedWeight = initialPackedWeight;
		packedContainerY = 0;
		packing = true;
		layerThickness = layers.get(bestIteration).LayerDim;
		remainingContainerY = containerY;
		remainingContainerZ = containerZ;

		for (x = 1; x <= itemsToPackCount; x++) {
			itemsToPack.get(x).setIsPacked(false);
		}

		do {
			layerInLayer = 0;
			layerDone = false;
			PackLayer(bestVariant);
			packedContainerY = packedContainerY + layerThickness;
			remainingContainerY = containerY - packedContainerY;

			if (layerInLayer > 0.0001) {
				prepackedy = packedContainerY;
				preremainpy = remainingContainerY;
				remainingContainerY = layerThickness - prelayer;
				packedContainerY = packedContainerY - layerThickness + prelayer;
				remainingContainerZ = lilz;
				layerThickness = layerInLayer;
				layerDone = false;
				PackLayer(bestVariant);
				packedContainerY = prepackedy;
				remainingContainerY = preremainpy;
				remainingContainerZ = containerZ;
			}

			if (!quit) {
				FindLayer(remainingContainerY);
			}
		} while (packing && !quit);
		displayFunctionSign("Report()", false);
	}

	// [start] CustomFunctions

	private Point3D getTransformedPoint(int x, int y, int z, int variant) {
		Point3D result = null;
		switch (variant) {
		case 1:
			result = new Point3D(x, y, z);
			break;
		case 2:
			result = new Point3D(z, y, x);
			break;
		case 3:
			result = new Point3D(y, z, x);
			break;
		case 4:
			result = new Point3D(y, x, z);
			break;
		case 5:
			result = new Point3D(x, z, y);
			break;
		case 6:
			result = new Point3D(z, x, y);
			break;
		}
		return result;
	}

	private void TraverseDLL(ScrapPad start) {
		ScrapPad temp = start;
		System.out.println("Traversing Gap DLL (x and z coordinates of each gap's right corner)");
		while (temp != null) {
			System.out.println(String.format("%s", temp.toString()));
			temp = temp.Post;
		}
	}

	/**
	 * Sort layers ASC by LayerEval
	 * 
	 * @param layers - List<Layer> to be sorted
	 */
	private void sortLayersLayerEval(List<Layer> layers) {
		Collections.sort(layers, (o1, o2) -> o1.LayerEval - o2.LayerEval);
	}

	/**
	 * Print layers info
	 * 
	 * @param layers - List<Layer> to be displayed
	 */
	private void displayLayersInfo(List<Layer> layers) {
		if (iterationTraceModeON) {
			System.out.println("Index\tDim\tEval");
			for (int i = 1; i < layers.size(); i++) {
				Layer layer = layers.get(i);
				System.out.println(String.format("%d\t%s", i, layer));
			}
		}
	}

	private PackedDimensions getPackedDimensions(Container container, List<Consignment> itemsPackedInOrder) {
		int packedLength = 0, packedHeight = 0;

		int x1, y1, z1;

		for (int i = 0; i < itemsPackedInOrder.size(); i++) {
			Consignment box = itemsPackedInOrder.get(i);
			x1 = box.getCOX();
			y1 = box.getCOY();
			z1 = box.getCOZ();
			if (x1 + box.getPackX() > packedLength)
				packedLength = x1 + box.getPackX();
			if (z1 + box.getPackZ() > packedHeight)
				packedHeight = z1 + box.getPackZ();
		}
		return new PackedDimensions(packedLength, packedHeight);
	}

	private void sortPackedItemsBasedOnCoordinates(List<Consignment> itemsPackedInOrder) {
		Collections.sort(itemsPackedInOrder, (o1, o2) -> o1.getCOY() - o2.getCOY());
		Collections.sort(itemsPackedInOrder, (o1, o2) -> o1.getCOZ() - o2.getCOZ());
		Collections.sort(itemsPackedInOrder, (o1, o2) -> o1.getCOX() - o2.getCOX());
	}

	private int getMin(int a, int b, int c) {
		if (b < a) {
			a = b;
		}
		if (c < a) {
			a = c;
		}
		return a;
	}

	private void displayBoxes(List<Consignment> boxes) {
		if (debugModeON) {
			System.out.println("#Items to be packed = " + boxes.size());
			System.out.println("Box\tDim1\tDim2\tDim3\tQty");
			for (x = 0; x < boxes.size(); x++) {
				Consignment box = boxes.get(x);
				System.out.println(String.format("%s\t%d\t%d\t%d\t%d", box.getBoxID(), box.getLength(), box.getWidth(),
						box.getHeight(), box.getQuantity()));
			}
		}
	}

	private void displayVariables() {
		if (debugModeON) {
			String[] str1 = { "evened,100%Packed,layerDone", "packing,packingBest,quit" };
			String[] var1 = { String.format("%s,%s,%s", evened, hundredPercentPacked, layerDone),
					String.format("%s,%s,%s", packing, packingBest, quit) };
			for (int i = 0; i < str1.length; i++) {
				System.out.println(String.format("%-30s\t%s", str1[i], var1[i]));
			}

			String[] str2 = { "itenum,bestIte,bestVariant" };
			String[] var2 = { String.format("%d,%d,%d", itenum, bestIteration, bestVariant) };
			for (int i = 0; i < str2.length; i++) {
				System.out.println(String.format("%-30s\t%s", str2[i], var2[i]));
			}

			String[] str3 = { "layerListLen,packedItemCount" };
			String[] var3 = { String.format("%d,%d", layerListLen, packedItemCount) };
			for (int i = 0; i < str3.length; i++) {
				System.out.println(String.format("%-30s\t%s", str3[i], var3[i]));
			}

			String[] str4 = { "x,boxi,bboxi,cboxi" };
			String[] var4 = { String.format("%d,%d,%d,%d", x, boxi, bboxi, currentBoxIndex) };
			for (int i = 0; i < str4.length; i++) {
				System.out.println(String.format("%-30s\t%s", str4[i], var4[i]));
			}

			String[] str5 = { "bf: x,y,z", "bbf: x,y,z" };
			String[] var5 = { String.format("%d,%d,%d", bfx, bfy, bfz), String.format("%d,%d,%d", bbfx, bbfy, bbfz) };
			for (int i = 0; i < str5.length; i++) {
				System.out.println(String.format("%-30s\t%s", str5[i], var5[i]));
			}

			String[] str6 = { "box: x,y,z", "bbox: x,y,z" };
			String[] var6 = { String.format("%d,%d,%d", boxx, boxy, boxz),
					String.format("%d,%d,%d", bboxx, bboxy, bboxz) };
			for (int i = 0; i < str6.length; i++) {
				System.out.println(String.format("%-30s\t%s", str6[i], var6[i]));
			}

			String[] str7 = { "currentBox: X,Y,Z" };
			String[] var7 = { String.format("%d,%d,%d", currentBoxPackX, currentBoxPackY, currentBoxPackZ) };
			for (int i = 0; i < str7.length; i++) {
				System.out.println(String.format("%-30s\t%s", str7[i], var7[i]));
			}

			String[] str8 = { "layerInLayer,layerThickness", "lilz", "packed: Volume,ContainerY",
					"pre: layer,packedy,remainpy" };
			String[] var8 = { String.format("%d,%d", layerInLayer, layerThickness), lilz + "",
					String.format("%d/%d,%d", packedVolume, containerVolume, packedContainerY),
					String.format("%d,%d,%d", prelayer, prepackedy, preremainpy) };
			for (int i = 0; i < str8.length; i++) {
				System.out.println(String.format("%-30s\t%s", str8[i], var8[i]));
			}

			String[] str9 = { "container: X,Y,Z" };
			String[] var9 = { String.format("%d,%d,%d", containerX, containerY, containerZ) };
			for (int i = 0; i < str9.length; i++) {
				System.out.println(String.format("%-30s\t%s", str9[i], var9[i]));
			}

			String[] str10 = { "remainingContainer: Y,Z" };
			String[] var10 = { String.format("%d,%d", remainingContainerY, remainingContainerZ) };
			for (int i = 0; i < str10.length; i++) {
				System.out.println(String.format("%-30s\t%s", str10[i], var10[i]));
			}

			System.out.println();
		}
	}

	private void displayFunctionSign(String functionName, boolean start) {
		if (functionTraceModeON) {
			if (!start)
				functionName = " End of " + functionName + " ";
			else
				functionName = " Start of " + functionName + " ";

			int stars = 80 - functionName.length();
			stars /= 2;
			for (int i = 0; i < stars; i++)
				System.out.print("*");
			System.out.print(functionName);
			for (int i = 0; i < stars; i++)
				System.out.print("*");
			System.out.println();
		}
	}
	// [end] CustomFunctions

	// [start] PrivateClasses
	private class Layer {
		private int LayerDim, LayerEval;
		private String LayerEvalDesc;

		public String toString() {
			return String.format("%d\t%d (%s)", LayerDim, LayerEval, LayerEvalDesc);
		}
	}

	/**
	 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 11-Nov-2021
	 *
	 *         DLL that keeps the topology of the edge of the current layer under
	 *         construction.We keep the x and z coordinates of each gap's right
	 *         corner. The program looks at those gaps and tries to fill them with
	 *         boxes one at a time while trying to keep the edge of the layer even
	 */
	private class ScrapPad {
		private int CumX, CumZ;
		private ScrapPad Pre, Post;

		public ScrapPad() {
			CumX = CumZ = -1;
		}

		public String toString() {
			String result = "";
			if (Pre == null)
				result += "NULL\t";
			else
				result += "*\t";
			result += String.format("CumX: %d, CumZ: %d", CumX, CumZ);
			if (Post == null)
				result += "\tNULL";
			else
				result += "\t*";
			return result;
		}
	}
	// [end] PrivateClasses
}
