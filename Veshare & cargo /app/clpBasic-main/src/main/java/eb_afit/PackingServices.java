package eb_afit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;
import clpBasic.utilities.Display;
import eb_afit.AlgorithmType.AlgorithmTypes;
import eb_afit.ContainerPackingResult.PRIORITY_BASED_PACKING_TYPE;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 24-Aug-2021
 * 
 *         Class defining packing service, which will call CLP algorithm
 * 
 *         Referenced C# repository
 *         https://github.com/davidmchapman/3DContainerPacking
 */
public class PackingServices {

	public static ContainerPackingResult Pack(Container container, List<Consignment> boxesToPack,
			AlgorithmTypes algorithmTypeID, PRIORITY_BASED_PACKING_TYPE priorityType, int stepsVolumePercent)
			throws Exception {
		int length = container.getLength();
		int width = container.getWidth();
		int height = container.getHeight();

		ContainerPackingResult containerPackingResult = new ContainerPackingResult(priorityType);
		containerPackingResult.setContainer(container);

		AlgorithmPackingResult apr = new AlgorithmPackingResult();
		apr.setTotalBoxCount(boxesToPack.size());

		IPackingAlgorithm algorithm = GetPackingAlgorithmFromTypeID(algorithmTypeID);

		int itemVolumePacked = 0, itemWeightPacked = 0, itemWeightPackedCurrent = 0;
		int itemVolumeUnpacked = 0, itemWeightUnpacked = 0;
		int itemTotalVolume = 0, itemTotalWeight = 0;
		long start, end, elapsedTime = 0;
		int containerVolume = container.getVolume();

		if (priorityType == PRIORITY_BASED_PACKING_TYPE.IGNORE_PRIORITY) {
			System.out.println("Packing without using priority");
			List<Consignment> boxes = new ArrayList<Consignment>();

			for (Consignment box : boxesToPack) {
				boxes.add(box);
			}
			System.out.println(String.format("Very Trivial target count: %d", boxes.size()));

			container.setSub_internal_length(length);
			container.setSub_internal_height(height);

			start = System.currentTimeMillis();
			apr = algorithm.Run(container, boxes, new Point3D(0, 0, 0), itemWeightPackedCurrent);
			itemWeightPackedCurrent = apr.getPackedWeight();
			end = System.currentTimeMillis();
			elapsedTime = end - start;

			apr.setTotalBoxCount(boxesToPack.size());
		} else {
			container.setSub_internal_height(height);

			Collections.sort(boxesToPack, (o1, o2) -> o1.getPriority() - o2.getPriority());

			if (priorityType == PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_IN_CONTAINER_VOLUME_STEPS) {
				// int stepsVolumePercent = 20;
				int counter = 1, packedLength = 0;

				int stepLength = (int) Math.ceil(containerVolume * stepsVolumePercent / 100.0 / width / height);

				while (counter * stepsVolumePercent <= 100) {
					if (packedLength + stepLength > length) {
						stepLength = length - packedLength;
					}

					int targetVolume = stepLength * width * height;

					System.out.print(String.format("\nStep: %d\nStepLength: %d\t\tPackedLength: %d\nTargetVolume: %d",
							counter, stepLength, packedLength, targetVolume));

					List<Consignment> boxes = new ArrayList<Consignment>();

					int subVolume = 0;
					for (Consignment box : boxesToPack) {
						int boxVolume = box.getVolume();
						if (subVolume + boxVolume > targetVolume)
							break;
						subVolume += boxVolume;
						boxes.add(box);
					}

					System.out.println(String.format("\tSubVolume: %d", subVolume));

					if (boxes != null) {
						container.setSub_internal_length(stepLength);

						start = System.currentTimeMillis();
						AlgorithmPackingResult tempApr = algorithm.Run(container, boxes,
								new Point3D(packedLength, 0, 0), itemWeightPackedCurrent);
						itemWeightPackedCurrent = tempApr.getPackedWeight();
						end = System.currentTimeMillis();
						elapsedTime += end - start;

						for (Consignment box : tempApr.getPackedItems()) {
							for (Consignment box2 : boxesToPack) {
								if (box2.getBoxID() == box.getBoxID()) {
									boxesToPack.remove(box2);
									break;
								}
							}
						}

						apr.getPackedItems().addAll(tempApr.getPackedItems());

						if (tempApr.getPackedItems().size() == 0) {
							break;
						}

						int EBFTPackedLength = tempApr.getPackedLength();
						packedLength += EBFTPackedLength;

						System.out.print(String.format("TotalPackedlength: %d\tPackedCount: %d\n", packedLength,
								tempApr.getPackedItems().size()));
					}
					counter++;
				}

				int additionalLength = length - packedLength;
				int targetVolume = additionalLength * width * height;

				System.out.println(
						String.format("\nStep: %d\nAdditional Length: %d\tPrePackedLength: %d\nTargetVolume: %d",
								counter, additionalLength, packedLength, targetVolume));

				boolean isSuitableBoxAvailable = false;
				for (Consignment box : boxesToPack) {
					if ((box.getLength() <= additionalLength || box.getWidth() <= additionalLength
							|| box.getHeight() <= additionalLength) && box.getWeight() + itemWeightPackedCurrent <= container.getTonnage()) {
						isSuitableBoxAvailable = true;
						break;
					}
				}

				if (isSuitableBoxAvailable) {
					container.setSub_internal_length(additionalLength);
					System.out.println(String.format("To Pack ID: %d", boxesToPack.get(0).getBoxID()));

					start = System.currentTimeMillis();
					AlgorithmPackingResult tempApr = algorithm.Run(container, boxesToPack,
							new Point3D(packedLength, 0, 0), itemWeightPackedCurrent);
					itemWeightPackedCurrent = tempApr.getPackedWeight();
					end = System.currentTimeMillis();
					elapsedTime += end - start;

					apr.getPackedItems().addAll(tempApr.getPackedItems());
					apr.getUnpackedItems().addAll(tempApr.getUnpackedItems());

					int EBFTPackedLength = tempApr.getPackedLength();
					packedLength += EBFTPackedLength;

					System.out.print(String.format("TotalPackedlength: %d\tPackedCount: %d\n", packedLength,
							tempApr.getPackedItems().size()));
				} else {
					System.out.println("Additional length is useless");
					apr.getUnpackedItems().addAll(boxesToPack);
				}

				containerPackingResult.getAlgorithmPackingResults().add(apr);
			} else {
				HashMap<Integer, ConsignmentsVolumePair> hashedPriorityBoxes = getPriorityList(boxesToPack);
				List<Integer> priorities = new ArrayList<Integer>(hashedPriorityBoxes.keySet());

				// Sort in reverse to have packing in reverse order
				// Collections.sort(priorities, Collections.reverseOrder());

				if (priorityType == PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_BEST_OF_EACH) {
					int packedLength = 0, additionalLength = 0;

					for (int p : priorities) {
						ConsignmentsVolumePair e = hashedPriorityBoxes.get(p);
						int subVolume = e.getVolume();
						int minLength = e.getMinLength();
						int minWidth = e.getMinWidth();
						int minHeight = e.getMinHeight();
						System.out.println(
								String.format("\nVolume: %d\t(%d,%d,%d)", subVolume, minLength, minWidth, minHeight));

						int targetLength = (int) Math.ceil(subVolume * 1.0 / width / height);

						if (minLength > targetLength)
							targetLength = minLength;
						if (minWidth > targetLength)
							targetLength = minWidth;
						if (minHeight > targetLength)
							targetLength = minHeight;

						int desiredLength = targetLength + additionalLength;
						int possibleLength = length - packedLength;
						int finalTargetLength = getMin(desiredLength, possibleLength);

						System.out.println(String.format(
								"Target length:%d\tAdditional length:%d\nDesired length: %d\tPossible length:%d",
								targetLength, additionalLength, desiredLength, finalTargetLength));

						List<Consignment> boxes = null;
						List<Consignment> boxList = e.getBoxes();
						if (finalTargetLength >= desiredLength) {
							boxes = boxList;
							System.out.println(String.format("Trivial target count: %d", boxes.size()));
						} else {
							boxes = new ArrayList<Consignment>();
							int tempTargetVolume = finalTargetLength * width * height;
							int tempCurrentVolume = 0;
							for (Consignment box : boxList) {
								if (tempCurrentVolume + box.getVolume() <= tempTargetVolume) {
									tempCurrentVolume += box.getVolume();
									boxes.add(box);
								} else {
									apr.getUnpackedItems().add(box);
								}
							}
							System.out.println(String.format("Target count: %d\tUnpacked: %d", boxes.size(),
									apr.getUnpackedItems().size()));
						}

						boolean isSuitableBoxAvailable = false;
						for (Consignment box : boxes) {
							if (box.getLength() <= finalTargetLength || box.getWidth() <= finalTargetLength
									|| box.getHeight() <= finalTargetLength) {
								isSuitableBoxAvailable = true;
								break;
							}
						}

						if (boxes != null && boxes.size() > 0 && isSuitableBoxAvailable) {
							container.setSub_internal_length(finalTargetLength);

							start = System.currentTimeMillis();
							AlgorithmPackingResult tempApr = algorithm.Run(container, boxes,
									new Point3D(packedLength, 0, 0), itemWeightPackedCurrent);
							itemWeightPackedCurrent = tempApr.getPackedWeight();
							end = System.currentTimeMillis();
							elapsedTime += end - start;

							apr.getPackedItems().addAll(tempApr.getPackedItems());
							apr.getUnpackedItems().addAll(tempApr.getUnpackedItems());

							if (tempApr.getPackedItems().size() == 0) {
								// break;
							}
							int EBFTPackedLength = tempApr.getPackedLength();
							additionalLength = targetLength + additionalLength - EBFTPackedLength;
							packedLength += EBFTPackedLength;
							System.out.print(String.format("TotalPackedlength: %d\n", packedLength));
						} else {
							System.out.println("Possible length is not sufficient");
							apr.getUnpackedItems().addAll(boxList);
						}
					}
				} else if (priorityType == PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_BEST_OF_EACH_WITH_STACK) {
					int targetLength = 0, additionalLength = 0;
					int packedLength = 0, bigPackedLength = 0, packedHeight = 0;
					int EBFTPackedLength = 0, EBFTPackedHeight = 0;
					boolean packedNone = false;

					for (int p : priorities) {
						ConsignmentsVolumePair e = hashedPriorityBoxes.get(p);
						int subVolume = e.getVolume();
						int minLength = e.getMinLength();
						int minWidth = e.getMinWidth();
						int minHeight = e.getMinHeight();
						System.out.println(
								String.format("\nVolume: %d\t(%d,%d,%d)", subVolume, minLength, minWidth, minHeight));

						targetLength = (int) Math.ceil(subVolume * 1.0 / width / height);

						if (minLength > targetLength)
							targetLength = minLength;
						if (minWidth > targetLength)
							targetLength = minWidth;
						if (minHeight > targetLength)
							targetLength = minHeight;

						int desiredLength = targetLength + additionalLength;
						int possibleLength = length - packedLength;
						int finalTargetLength = getMin(desiredLength, possibleLength);

						System.out.println(String.format("Target length:%d\tAdditional length:%d", targetLength,
								additionalLength));
						System.out.println(String.format("Desired length: %d\tPossible length:%d", desiredLength,
								finalTargetLength));

						int desiredHeight = minLength;
						if (minWidth > desiredHeight)
							desiredHeight = minWidth;
						if (minHeight > desiredHeight)
							desiredHeight = minHeight;
						int possibleHeight = height - packedHeight;
						System.out.println(
								String.format("Desired height: %d\tPossible height:%d", desiredHeight, possibleHeight));

						if (!packedNone) {
							if (desiredHeight <= possibleHeight) {
								packedLength -= EBFTPackedLength;
							} else {
								packedHeight = 0;
								packedLength = bigPackedLength;
								possibleHeight = height;
							}
							// desiredLength = targetLength + additionalLength;
							possibleLength = length - packedLength;
							finalTargetLength = getMin(desiredLength, possibleLength);
						} else {
							packedNone = false;
						}
						System.out
								.println(String.format("Updated next Base: (%d,%d,%d)", packedLength, 0, packedHeight));
						System.out.println(String.format("Updated Target length:%d\tAdditional length:%d", targetLength,
								additionalLength));
						System.out.println(String.format("Updated Desired length: %d\tPossible length:%d",
								desiredLength, finalTargetLength));

						List<Consignment> boxes = null;
						List<Consignment> boxList = e.getBoxes();
						if (finalTargetLength >= desiredLength) {
							boxes = boxList;
							System.out.println(String.format("Trivial target count: %d", boxes.size()));
						} else {
							boxes = new ArrayList<Consignment>();
							int tempTargetVolume = finalTargetLength * width * height;
							int tempCurrentVolume = 0;
							for (Consignment box : boxList) {
								if (tempCurrentVolume + box.getVolume() <= tempTargetVolume) {
									tempCurrentVolume += box.getVolume();
									boxes.add(box);
								} else {
									apr.getUnpackedItems().add(box);
								}
							}
							System.out.println(String.format("Target count: %d\tUnpacked: %d", boxes.size(),
									apr.getUnpackedItems().size()));
						}

						boolean isSuitableBoxAvailable = false;
						for (Consignment box : boxes) {
							if (box.getLength() <= finalTargetLength || box.getWidth() <= finalTargetLength
									|| box.getHeight() <= finalTargetLength) {
								isSuitableBoxAvailable = true;
								break;
							}
						}

						if (boxes != null && boxes.size() > 0 && isSuitableBoxAvailable) {
							container.setSub_internal_length(finalTargetLength);
							container.setSub_internal_height(possibleHeight);
							System.out.println(String.format("next Container Dimensions: (%d,%d,%d)", finalTargetLength,
									width, possibleHeight));

							start = System.currentTimeMillis();
							AlgorithmPackingResult tempApr = algorithm.Run(container, boxes,
									new Point3D(packedLength, 0, packedHeight), itemWeightPackedCurrent);
							itemWeightPackedCurrent = tempApr.getPackedWeight();
							end = System.currentTimeMillis();
							elapsedTime += end - start;

							apr.getPackedItems().addAll(tempApr.getPackedItems());
							apr.getUnpackedItems().addAll(tempApr.getUnpackedItems());

							EBFTPackedLength = tempApr.getPackedLength();
							EBFTPackedHeight = tempApr.getPackedHeight();

							additionalLength = targetLength + additionalLength - EBFTPackedLength;
							packedLength += EBFTPackedLength;
							if (bigPackedLength < packedLength)
								bigPackedLength = packedLength;
							packedHeight += EBFTPackedHeight;
							System.out.println(String.format("Possible length is  sufficient, packed priotity: %d", p));
							System.out.print(String.format("Next Base: (%d,%d,%d)\n", packedLength, 0, packedHeight));
						} else {
							System.out.println(
									String.format("Possible length is not sufficient, not packed priotity: %d", p));
							apr.getUnpackedItems().addAll(boxList);
							packedNone = true;
						}
					}
				} else if (priorityType == PRIORITY_BASED_PACKING_TYPE.USE_PRIORITY_PACK_EACH_GROUP_WITH_STACK) {
					int possibleLength = length, possibleHeight = height;
					int packedLength = 0, wallLength = 0, bigPackedLength = 0, packedHeight = 0;
					int EBFTPackedLength = 0, EBFTPackedHeight = 0;
					boolean packedNone = false;

					for (int p : priorities) {
						ConsignmentsVolumePair e = hashedPriorityBoxes.get(p);
						int subVolume = e.getVolume();
						int minLength = e.getMinLength();
						int minWidth = e.getMinWidth();
						int minHeight = e.getMinHeight();
						Display.printSolidLine();
						System.out.println(String.format("Priority: %d, Volume: %d\t(%d,%d,%d)", p, subVolume,
								minLength, minWidth, minHeight));

						List<Consignment> unpackedBoxes = e.getBoxes();
						while (true) {
							if (unpackedBoxes.size() == 0)
								break;

							possibleLength = length - packedLength;
							possibleHeight = height - packedHeight;
							Display.printDashedLineShort();

							int desiredHeight = unpackedBoxes.get(0).getLength();
							for (Consignment box : unpackedBoxes) {
								int boxLength = box.getLength();
								int boxWidth = box.getWidth();
								int boxHeight = box.getHeight();
								if (boxLength > desiredHeight)
									desiredHeight = boxLength;
								if (boxWidth > desiredHeight)
									desiredHeight = boxWidth;
								if (boxHeight > desiredHeight)
									desiredHeight = boxHeight;
							}
							System.out.println(String.format("Possible L:%d\tPossible H:%d\tDesired height: %d",
									possibleLength, possibleHeight, desiredHeight));

							if (!packedNone) {
								if (desiredHeight <= possibleHeight) {
									packedLength -= EBFTPackedLength;
								} else {
									packedHeight = 0;
									packedLength = bigPackedLength;
									wallLength = 0;
									possibleHeight = height;
								}
								possibleLength = length - packedLength;
								System.out.println(
										String.format("Updated Next Base: (%d,%d,%d)", packedLength, 0, packedHeight));
							} else {
								packedNone = false;
							}

							boolean isSuitableBoxAvailable = false;
							for (Consignment box : unpackedBoxes) {
								if ((box.getLength() <= possibleLength || box.getWidth() <= possibleLength
										|| box.getHeight() <= possibleLength) && box.getWeight() + itemWeightPackedCurrent <= container.getTonnage()) {
									isSuitableBoxAvailable = true;
									System.out.println(box);
									break;
								}
							}

							if (isSuitableBoxAvailable) {
								for (Consignment box : unpackedBoxes) {
									if (wallLength < box.getLength())
										wallLength = box.getLength();
									if (wallLength < box.getWidth())
										wallLength = box.getWidth();
									if (wallLength < box.getHeight())
										wallLength = box.getHeight();
								}

								System.out.println(String.format("Updated possible L:%d\tPossible H:%d", possibleLength,
										possibleHeight));
								System.out.println(String.format("Big L: %d\tWall L: %d", bigPackedLength, wallLength));

								wallLength = getMin(possibleLength, wallLength);
								container.setSub_internal_length(wallLength);
								container.setSub_internal_height(possibleHeight);
								System.out.println(String.format("Next Compartment Dimensions: (%d,%d,%d)", wallLength,
										width, possibleHeight));

								System.out.println(String.format("Passing %d unpacked boxes", unpackedBoxes.size()));

								start = System.currentTimeMillis();
								AlgorithmPackingResult tempApr = algorithm.Run(container, unpackedBoxes,
										new Point3D(packedLength, 0, packedHeight), itemWeightPackedCurrent);
								itemWeightPackedCurrent = tempApr.getPackedWeight();
								end = System.currentTimeMillis();
								elapsedTime += end - start;

								List<Consignment> packedItems = tempApr.getPackedItems();
								List<Consignment> unpackedItems = tempApr.getUnpackedItems();

								for (Consignment box : packedItems) {
									for (Consignment box2 : unpackedBoxes) {
										if (box2.getBoxID() == box.getBoxID()) {
											unpackedBoxes.remove(box2);
											break;
										}
									}
								}

								apr.getPackedItems().addAll(packedItems);
								// apr.getUnpackedItems().addAll(unpackedItems);

								EBFTPackedLength = tempApr.getPackedLength();
								EBFTPackedHeight = tempApr.getPackedHeight();

								packedLength += EBFTPackedLength;
								packedHeight += EBFTPackedHeight;
								possibleHeight = height - packedHeight;

								if (wallLength < EBFTPackedLength)
									wallLength = EBFTPackedLength;

								if (bigPackedLength < packedLength)
									bigPackedLength = packedLength;
								System.out.println("Possible length is  sufficient");
								System.out.println(String.format("Priotity: %d\tPacked %d\tRemaining: %d", p,
										packedItems.size(), unpackedItems.size()));
								System.out
										.println(String.format("Next Base: (%d,%d,%d)", packedLength, 0, packedHeight));
								if(packedItems.size() == 0) break;
							} else {
								packedNone = true;
								System.out.println("Possible length or height is NOT sufficient");
								System.out.println(String.format("Priotity: %d\tUnpacked %d", p, unpackedBoxes.size()));
								apr.getUnpackedItems().addAll(unpackedBoxes);
								break;
							}
						}
					}
				}
			}
		}

		for (Consignment box : apr.getPackedItems()) {
			itemVolumePacked += box.getVolume();
			itemWeightPacked += box.getWeight();
		}

		for (Consignment box : apr.getUnpackedItems()) {
			itemVolumeUnpacked += box.getVolume();
			itemWeightUnpacked += box.getWeight();
		}

		itemTotalVolume = itemVolumePacked + itemVolumeUnpacked;
		itemTotalWeight = itemWeightPacked + itemWeightUnpacked;

		double temp = 100.0 * itemVolumePacked / containerVolume;
		apr.setApr_bestvolume(itemVolumePacked);
		apr.setPercentContainerVolumePacked(temp);

		temp = itemVolumePacked * 100.0 / itemTotalVolume;
		apr.setApr_totalboxvol(itemTotalVolume);
		apr.setPercentItemVolumePacked(temp);

		temp = 100.0 * itemWeightPacked / container.getTonnage();
		apr.setApr_bestweight(itemWeightPacked);
		apr.setPercentContainerTonnagePacked(temp);

		temp = itemWeightPacked * 100.0 / itemTotalWeight;
		apr.setApr_totalboxweight(itemTotalWeight);
		apr.setPercentItemWeightPacked(temp);

		apr.setPackTimeInMilliseconds(elapsedTime);
		containerPackingResult.getAlgorithmPackingResults().add(apr);

		setRowColumnsForPackedBoxes(container, apr.getPackedItems());

		return containerPackingResult;
	}

	private static void setRowColumnsForPackedBoxes(Container container, List<Consignment> itemsPackedInOrder) {
		int wall, row, column;
		wall = row = column = 1;

		int cx = container.getLength();
		int cy = container.getWidth();
		int cz = container.getHeight();

		int x1, y1, z1;

		for (int i = 0; i < itemsPackedInOrder.size(); i++) {
			Consignment box = itemsPackedInOrder.get(i);
			x1 = box.getCOX();
			y1 = box.getCOY();
			z1 = box.getCOZ();
			box.setWall(wall);
			box.setLayer(row);
			box.setPositionInLayer(column);

			column++;

			if (i != itemsPackedInOrder.size() - 1) {
				box = itemsPackedInOrder.get(i + 1);

				// Increment layer if next box is above current
				if (box.getCOZ() > z1) {
					row++;
					column = 1;
				}

				// Increment wall if next box is below current
				if (box.getCOZ() < z1) {
					wall++;
					row = column = 1;
				}
			}
		}
	}

	private static int getMin(int a, int b) {
		return a <= b ? a : b;
	}

	private static HashMap<Integer, ConsignmentsVolumePair> getPriorityList(List<Consignment> boxes) {
		HashMap<Integer, ConsignmentsVolumePair> result = new HashMap<Integer, ConsignmentsVolumePair>();

		for (Consignment box : boxes) {
			ConsignmentsVolumePair l = result.get(box.getPriority());
			if (l == null) {
				l = new ConsignmentsVolumePair();
				result.put(box.getPriority(), l);
			}
			l.addBox(box);
		}

		return result;
	}

	public static IPackingAlgorithm GetPackingAlgorithmFromTypeID(AlgorithmTypes algorithmTypeID) throws Exception {
		switch (algorithmTypeID) {
		case EB_AFIT:
			return new EB_AFIT();

		default:
			throw new Exception("Invalid algorithm type.");
		}
	}
}

class ConsignmentsVolumePair {
	private int minLength, minWidth, minHeight, volume;

	private List<Consignment> boxes;

	public ConsignmentsVolumePair() {
		volume = 0;
		boxes = new ArrayList<Consignment>();
		minLength = minWidth = minHeight = Integer.MAX_VALUE;
	}

	public void addBox(Consignment box) {
		boxes.add(box);

		int length = box.getLength();
		if (minLength > length)
			minLength = length;
		int width = box.getWidth();
		if (minWidth > length)
			minWidth = width;
		int height = box.getHeight();
		if (minHeight > length)
			minHeight = height;

		volume += box.getVolume();
	}

	public int getVolume() {
		return volume;
	}

	public List<Consignment> getBoxes() {
		return boxes;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public int getMinHeight() {
		return minHeight;
	}
}
