package clpBasic.utilities;

import java.util.Collections;
import java.util.List;
import clpBasic.eclipselink.entities.Consignment;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Some static utility methods that are not used as of now
 */
public class CargoStaticMethods {
	public static void sortBoxesByVolume(List<Consignment> boxes) {
		Collections.sort(boxes, (o1, o2) -> o2.getVolume() - o1.getVolume());
	}

	public static void sortBoxesByHeight(List<Consignment> boxes) {
		Collections.sort(boxes, (o1, o2) -> o2.getHeight() - o1.getHeight());
	}

	public static void displayBoxes(String title, List<Consignment> boxes) {
		System.out.println(title);
		for (Consignment c : boxes) {
			System.out.println(String.format("Box #%s (%d,%d,%d) Pos:(%d,%d,%d) Area:%d Vol:%d Weight:%d", c.getBoxID(),
					c.getLength(), c.getWidth(), c.getHeight(), c.getCOX(), c.getCOY(), c.getCOZ(),
					c.getLength() * c.getWidth(), c.getVolume(), c.getWeight()));
		}
	}
}
