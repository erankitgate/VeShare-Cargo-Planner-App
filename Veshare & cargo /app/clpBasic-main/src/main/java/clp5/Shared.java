package clp5;

public class Shared {
	public static enum CLP_MODE {
		PACK {
			public String toString() {
				return "PACK";
			}
		},

		GENERATE_DATA {
			public String toString() {
				return "GENERATE_DATA";
			}
		}
	}
}
