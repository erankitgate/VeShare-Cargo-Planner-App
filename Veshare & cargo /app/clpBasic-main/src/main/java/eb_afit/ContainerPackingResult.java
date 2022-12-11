package eb_afit;

import java.util.ArrayList;
import java.util.List;

import clpBasic.eclipselink.entities.Container;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 24-Aug-2021
 * 
 *         Class for holding details of container and its
 *         AlgorithmPackingResults, used in PackingServices
 * 
 *         Referenced C# repository
 *         https://github.com/davidmchapman/3DContainerPacking
 */
public class ContainerPackingResult {
	private Container Container;
	private List<AlgorithmPackingResult> AlgorithmPackingResults;
	private PRIORITY_BASED_PACKING_TYPE packingPriorityType;

	public static enum PRIORITY_BASED_PACKING_TYPE {
		IGNORE_PRIORITY {
			public String toString() {
				return "IGNORE_PRIORITY";
			}
		},

		// Equivalent USE_PRIORITY_PACK_COMPLETE_HIGHER_FIRST
		USE_PRIORITY_PACK_IN_CONTAINER_VOLUME_STEPS {
			public String toString() {
				return "USE_PRIORITY_PACK_IN_CONTAINER_VOLUME_STEPS";
			}
		},

		USE_PRIORITY_PACK_BEST_OF_EACH {
			public String toString() {
				return "USE_PRIORITY_PACK_BEST_OF_EACH";
			}
		},
		
		USE_PRIORITY_PACK_BEST_OF_EACH_WITH_STACK {
			public String toString() {
				return "USE_PRIORITY_PACK_BEST_OF_EACH_WITH_STACK";
			}
		},
		
		USE_PRIORITY_PACK_EACH_GROUP_WITH_STACK {
			public String toString() {
				return "USE_PRIORITY_PACK_EACH_GROUP_WITH_STACK";
			}
		}
	}
	
	public ContainerPackingResult(PRIORITY_BASED_PACKING_TYPE packingPriorityType) {
		AlgorithmPackingResults = new ArrayList<AlgorithmPackingResult>();
		setPackingPriorityType(packingPriorityType);
	}

	public Container getContainer() {
		return Container;
	}

	public void setContainer(Container container) {
		this.Container = container;
	}

	public List<AlgorithmPackingResult> getAlgorithmPackingResults() {
		return AlgorithmPackingResults;
	}

	public void setAlgorithmPackingResults(List<AlgorithmPackingResult> algorithmPackingResults) {
		this.AlgorithmPackingResults = algorithmPackingResults;
	}

	public PRIORITY_BASED_PACKING_TYPE getPackingPriorityType() {
		return packingPriorityType;
	}

	public void setPackingPriorityType(PRIORITY_BASED_PACKING_TYPE packingPriorityType) {
		this.packingPriorityType = packingPriorityType;
	}
}
