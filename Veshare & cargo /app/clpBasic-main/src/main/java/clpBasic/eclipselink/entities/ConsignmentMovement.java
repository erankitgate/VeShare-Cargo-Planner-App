package clpBasic.eclipselink.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         JPA class entity corresponding to Orders table in the DB
 */
@Entity
@Table(name = "consignment_movement")
@NamedQuery(query = "Select e.unique_consignment_id,e.priority from ConsignmentMovement e where e.container_id = :container_id AND e.pickup_timestamp = :timestamp", name = "find consignmentIDs by containerIDAndTimeStamp")
public class ConsignmentMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)

	private int unique_consignment_id;
	private int container_id;
	private String pickup_timestamp;
	private int priority;

	public ConsignmentMovement(int unique_consignment_id, int container_id) {
		super();
		this.unique_consignment_id = unique_consignment_id;
		this.container_id = container_id;
	}

	public ConsignmentMovement() {
		super();
	}

	public int getUniqueConsignmentID() {
		return unique_consignment_id;
	}

	public int getContainerID() {
		return container_id;
	}

	@Override
	public String toString() {
		return String.format("Order ID = %d\n" + "Container ID = %d\n", unique_consignment_id, container_id);
	}
}
