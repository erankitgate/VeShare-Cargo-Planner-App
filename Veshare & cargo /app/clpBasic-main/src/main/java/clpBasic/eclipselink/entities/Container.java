package clpBasic.eclipselink.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 23-Aug-2021
 * 
 *         JPA class entity corresponding to Vehicles table in the DB
 * 
 *         Only relevant fields from DB table are considered
 */
@Entity
@Table(name = "containers_on_platform")
@NamedQuery(query = "Select e.unique_container_id from Container e", name = "get unique_container_ids")
public class Container {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)

	private int unique_container_id;
	private String container_description;
	private int internal_length, internal_width, internal_height;
	private int tare_weight;
	private int tonnage;
	
	@Transient
	private int sub_internal_length, sub_internal_height;

	public Container(int vehicle_id, String container_description, int container_length, int container_width,
			int container_height, int container_tare_weight, int container_tonnage) {
		super();
		this.unique_container_id = vehicle_id;
		this.container_description = container_description;
		this.internal_length = container_length;
		this.internal_width = container_width;
		this.internal_height = container_height;
		this.tare_weight = container_tare_weight;
		this.tonnage = container_tonnage;
	}

	public Container() {
		super();
	}

	public int getID() {
		return unique_container_id;
	}

	/*
	 * public void setID(int ID) { this.vehicle_id = ID; }
	 */

	public String getDescription() {
		return container_description;
	}

	public int getLength() {
		return internal_length;
	}

	public int getSub_internal_length() {
		return sub_internal_length;
	}

	public void setSub_internal_length(int sub_internal_length) {
		this.sub_internal_length = sub_internal_length;
	}

	public int getSub_internal_height() {
		return sub_internal_height;
	}

	public void setSub_internal_height(int sub_internal_height) {
		this.sub_internal_height = sub_internal_height;
	}

	public int getWidth() {
		return internal_width;
	}

	public int getHeight() {
		return internal_height;
	}

	public int getTareWeight() {
		return tare_weight;
	}

	public int getTonnage() {
		return tonnage * 1000;
	}

	public int getVolume() {
		return internal_length * internal_width * internal_height;
	}
	
	public int getSubVolume() {
		return sub_internal_length * internal_width * internal_height;
	}

	@Override
	public String toString() {
		return String.format(
				"Container ID = %d\n" + "DESC = %s\n" + "Dimensions = %s\n" + "TareWeight = %d kg\n"
						+ "Tonnage = %d kg",
				unique_container_id, container_description, getDimensionsToString(), tare_weight,
				tonnage);
	}

	private String getDimensionsToString() {
		return String.format("%d x %d x %d cm", internal_length, internal_width,
				internal_height);
	}
}
