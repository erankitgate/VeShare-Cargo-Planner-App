package clp5;

import eb_afit.ContainerPackingResult.PRIORITY_BASED_PACKING_TYPE;

public class LogEntry {
	private String datasetDescription;
	private int totalBoxes, packedBoxes, unpackedBoxes;
	private double containerVolumeUtilization, boxVolumePercent;
	private double containerTonnageUtilization, boxWeightPercent;
	private String solutionTime;
	private int numPriorities;
	private PRIORITY_BASED_PACKING_TYPE priorityType;

	public LogEntry(String datasetDescription, int totalBoxes, int packedBoxes, int unpackedBoxes,
			double containerVolumeUtilization, double boxVolumePercent, double containerTonnageUtilization,
			double boxWeightPercent, String solutionTime, int numPriorities, PRIORITY_BASED_PACKING_TYPE priorityType) {
		this.datasetDescription = datasetDescription;
		this.totalBoxes = totalBoxes;
		this.packedBoxes = packedBoxes;
		this.unpackedBoxes = unpackedBoxes;
		this.containerVolumeUtilization = containerVolumeUtilization;
		this.boxVolumePercent = boxVolumePercent;
		this.containerTonnageUtilization = containerTonnageUtilization;
		this.boxWeightPercent = boxWeightPercent;
		this.solutionTime = solutionTime;
		this.numPriorities = numPriorities;
		this.priorityType = priorityType;
	}

	public String getDatasetDescription() {
		return datasetDescription;
	}

	public int getTotalBoxes() {
		return totalBoxes;
	}

	public int getPackedBoxes() {
		return packedBoxes;
	}

	public int getUnpackedBoxes() {
		return unpackedBoxes;
	}

	public double getContainerVolumeUtilization() {
		return containerVolumeUtilization;
	}

	public double getBoxVolumePercent() {
		return boxVolumePercent;
	}

	public double getContainerTonnageUtilization() {
		return containerTonnageUtilization;
	}

	public double getBoxWeightPercent() {
		return boxWeightPercent;
	}

	public void setContainerTonnageUtilization(double containerTonnageUtilization) {
		this.containerTonnageUtilization = containerTonnageUtilization;
	}

	public void setBoxWeightPercent(double boxWeightPercent) {
		this.boxWeightPercent = boxWeightPercent;
	}

	public String getSolutionTime() {
		return solutionTime;
	}

	public int getNumPriorities() {
		return numPriorities;
	}

	public PRIORITY_BASED_PACKING_TYPE getPriorityType() {
		return priorityType;
	}
}
