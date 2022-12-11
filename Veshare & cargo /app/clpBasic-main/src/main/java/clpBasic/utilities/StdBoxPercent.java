package clpBasic.utilities;

import clpBasic.utilities.StaticMemory.STD_BOX_NAME;

public class StdBoxPercent {
	private STD_BOX_NAME description;
	private int percentOfTotalBoxes;
	
	public StdBoxPercent(STD_BOX_NAME description,int percentOfContainerVolume) {
		this.description = description;
		this.percentOfTotalBoxes = percentOfContainerVolume;
	}
	
	public STD_BOX_NAME getDescription() {
		return description;
	}
	
	public int getPercentOfTotalBoxes() {
		return percentOfTotalBoxes;
	}
}
