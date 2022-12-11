package clpBasic.eclipselink.services;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import clpBasic.eclipselink.entities.Consignment;
import clpBasic.eclipselink.entities.Container;
import clpBasic.utilities.Display;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         Defines various methods for fetching input from the DB
 */
public class DBQueryHandler {
	private EntityManager entitymanager;

	public DBQueryHandler(EntityManager entitymanager) {
		this.entitymanager = entitymanager;
	}
	
	public List<Integer> getContainerIDs() {
		Query query = entitymanager.createNamedQuery("get unique_container_ids");

		return query.getResultList();
	}

	public Container getContainerByID(int containerID) {
		Container container = entitymanager.find(Container.class, containerID);

		Display.printSolidLine();
		System.out.println(container);
		Display.printSolidLine();

		return container;
	}

	public List<Consignment> getConsignmentsForGivenContainerAndDate(int containerID, String timestamp) {
		Query query = entitymanager.createNamedQuery("find consignmentIDs by containerIDAndTimeStamp");

		query.setParameter("container_id", containerID);
		query.setParameter("timestamp", timestamp);
		List<Object[]> boxIDs = query.getResultList();
		
		List<Consignment> boxes = new ArrayList<Consignment>();
		for (Object[] e : boxIDs) {
			//System.out.println(e);
			Consignment box = entitymanager.find(Consignment.class, e[0]);
			box.setVolume();
			box.setQuantity(1);
			box.setPriority((int) e[1]);
			boxes.add(box);
		}

		//Display.printSolidLine();
		//for (Consignment e : boxes) {
		//	System.out.println(e);
		//}
		//Display.printSolidLine();

		return boxes;
	}
}
