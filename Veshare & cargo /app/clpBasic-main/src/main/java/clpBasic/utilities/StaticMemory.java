package clpBasic.utilities;

import clpBasic.utilities.StaticMemory.STD_BOX_NAME;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Some static contents
 */
public class StaticMemory {

	public static enum STD_BOX_NAME {
		DHL_EXPRESS_EASY_BOX_1 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_1";
			}
		},
		DHL_EXPRESS_EASY_BOX_2 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_2";
			}
		},
		DHL_EXPRESS_EASY_BOX_3 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_3";
			}
		},
		DHL_EXPRESS_EASY_BOX_4 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_4";
			}
		},
		DHL_EXPRESS_EASY_BOX_5 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_5";
			}
		},
		DHL_EXPRESS_EASY_BOX_6 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_6";
			}
		},
		DHL_EXPRESS_EASY_BOX_7 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_7";
			}
		},
		DHL_EXPRESS_EASY_BOX_8 {
			public String toString() {
				return "DHL_EXPRESS_EASY_BOX_8";
			}
		},
		DHL_SMALL_BOX {
			public String toString() {
				return "DHL_SMALL_BOX";
			}
		},
		DHL_BRIEFCASE_BOX {
			public String toString() {
				return "DHL_BRIEFCASE_BOX";
			}
		},
		DHL_BIG_BOX {
			public String toString() {
				return "DHL_BIG_BOX";
			}
		},
		DHL_SNAP_BOX {
			public String toString() {
				return "DHL_SNAP_BOX";
			}
		},
		DHL_SNAP_BOX_LARGE {
			public String toString() {
				return "DHL_SNAP_BOX_LARGE";
			}
		},
		ROYAL_SMALL {
			public String toString() {
				return "ROYAL_SMALL";
			}
		},
		ROYAL_MEDIUM {
			public String toString() {
				return "ROYAL_MEDIUM";
			}
		},
		ROYAL_BIG {
			public String toString() {
				return "ROYAL_BIG";
			}
		},
		DART_BLUE_SMART_1 {
			public String toString() {
				return "DART_BLUE_SMART_1";
			}
		},
		DART_BLUE_SMART_2 {
			public String toString() {
				return "DART_BLUE_SMART_2";
			}
		}
	}

	static String[] colors = { "Purple", "Blue", "Crimson", "Green", "Brown", "Indigo", "DarkSlateGray", "DarkBlue",
			"Chocolate", "DarkOrchid", "DeepPink", "DarkCyan", "DarkMagenta", "DarkGoldenRod", "BlueViolet",
			"DarkGreen", "CornflowerBlue", "DarkRed", "DarkOliveGreen", "MidnightBlue" };

	static StandardBox[] StandardBoxes = { new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_1, 35, 27, 2, 500),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_2, 34, 18, 10, 1000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_3, 34, 32, 10, 2000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_4, 34, 32, 18, 5000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_5, 34, 32, 34, 10000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_6, 42, 36, 37, 15000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_7, 48, 40, 39, 20000),
			new StandardBox(STD_BOX_NAME.DHL_EXPRESS_EASY_BOX_8, 54, 44, 41, 30000),
			new StandardBox(STD_BOX_NAME.DHL_SMALL_BOX, 32, 24, 31, 15000),
			new StandardBox(STD_BOX_NAME.DHL_BRIEFCASE_BOX, 46, 34, 9, 10000),
			new StandardBox(STD_BOX_NAME.DHL_BIG_BOX, 48, 33, 32, 20000),
			new StandardBox(STD_BOX_NAME.DHL_SNAP_BOX, 44, 32, 27, 20000),
			new StandardBox(STD_BOX_NAME.DHL_SNAP_BOX_LARGE, 50, 46, 38, 30000),
			new StandardBox(STD_BOX_NAME.ROYAL_SMALL, 45, 35, 16, 2000),
			new StandardBox(STD_BOX_NAME.ROYAL_MEDIUM, 61, 46, 46, 20000),
			new StandardBox(STD_BOX_NAME.ROYAL_BIG, 150, 46, 46, 30000),
			new StandardBox(STD_BOX_NAME.DART_BLUE_SMART_1, 41, 33, 27, 10000),
			new StandardBox(STD_BOX_NAME.DART_BLUE_SMART_2, 48, 43, 34, 25000) };
}

class StandardBox {
	private STD_BOX_NAME description;
	private int length, width, height, weight, stack_weight_length_upright, stack_weight_width_upright, stack_weight_height_upright;

	public StandardBox(STD_BOX_NAME description, int length, int width, int height, int weight) {
		this.description = description;
		this.length = length;
		this.width = width;
		this.height = height;
		this.weight = weight;
	}

	public String getDescription() {
		return description.toString();
	}

	public int getLength() {
		return length;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getWeight() {
		return weight;
	}
	

	public int getStack_weight_length_upright() {
		if (weight <= 5000) {
			stack_weight_length_upright = 6 * weight;
		} else if (weight <= 15000) {
			stack_weight_length_upright = 4 * weight;
		} else if (weight <= 30000) {
			stack_weight_length_upright = 3 * weight;
		} else {
			stack_weight_length_upright = 2 * weight;
		}
		return stack_weight_length_upright;
	}
	

	public int getStack_weight_width_upright() {
		if (weight <= 5000) {
			stack_weight_width_upright = 6 * weight;
		} else if (weight <= 15000) {
			stack_weight_width_upright = 4 * weight;
		} else if (weight <= 30000) {
			stack_weight_width_upright = 3 * weight;
		} else {
			stack_weight_width_upright = 2 * weight;
		}
		return stack_weight_width_upright;
	}
	

	public int getStack_weight_height_upright() {
		if (weight <= 5000) {
			stack_weight_height_upright = 6 * weight;
		} else if (weight <= 15000) {
			stack_weight_height_upright = 4 * weight;
		} else if (weight <= 30000) {
			stack_weight_height_upright = 3 * weight;
		} else {
			stack_weight_height_upright = 2 * weight;
		}
		return stack_weight_height_upright;
	}
}
