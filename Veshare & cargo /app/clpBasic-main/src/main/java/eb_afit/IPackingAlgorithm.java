package eb_afit;

import java.util.List;

import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 24-Aug-2021
 * 
 *         Interface for a CLP algorithm
 */
public interface IPackingAlgorithm {
	AlgorithmPackingResult Run(Container container, List<Consignment> items, Point3D containerBase, int initialPackedWeight);
}
