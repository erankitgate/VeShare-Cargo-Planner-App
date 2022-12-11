package clpBasic.eclipselink.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
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
 *         JPA class entity corresponding to Consignments table in the DB
 */
@Entity
@Table(name = "consignments")
//@NamedQuery(query = "Select e from Consignment e where e.unique_consignment_id = :unique_consignment_id", name = "find consignnment by consignnmentID")
public class Consignment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int unique_consignment_id;

	@Column(name = "length")
	private int dim1;

	@Column(name = "width")
	private int dim2;

	@Column(name = "height")
	private int dim3;

	private int weight;

	private int is_floor_only;

	private int length_upright_allowed, width_upright_allowed, height_upright_allowed;

	private int is_stackable, stack_weight_length_upright, stack_weight_width_upright, stack_weight_height_upright;

	private String color;

	private String box_description;

	@Transient
	private boolean is_packed;

	@Transient
	private int quantity, volume;

	// Packing co-ordinates
	@Transient
	private int cox, coy, coz;

	// Packing dimensions
	@Transient
	private int packx, packy, packz;

	@Transient
	private int wall, layer, positionInLayer;

	@Transient
	private int priority;

	public Consignment(int Unique_consignment_id, String box_description, int length, int width, int height, int weight,
			int is_floor_only, int length_upright_allowed, int width_upright_allowed, int height_upright_allowed,
			int is_stackable, int stack_weight_length_upright, int stack_weight_width_upright,
			int stack_weight_height_upright, String color) {
		super();
		this.unique_consignment_id = Unique_consignment_id;

		this.dim1 = length;
		this.dim2 = width;
		this.dim3 = height;
		this.weight = weight;

		this.is_floor_only = is_floor_only;

		this.length_upright_allowed = length_upright_allowed;
		this.width_upright_allowed = width_upright_allowed;
		this.height_upright_allowed = height_upright_allowed;

		this.is_stackable = is_stackable;
		this.stack_weight_length_upright = stack_weight_length_upright;
		this.stack_weight_width_upright = stack_weight_width_upright;
		this.stack_weight_height_upright = stack_weight_height_upright;

		this.color = color;
		this.box_description = box_description;

		is_packed = false;
		cox = coy = coz = 0;
		volume = length * width * height;
		quantity = 1;
	}

	public Consignment() {
		super();
	}

	public Consignment copy() {
		Consignment c = new Consignment(unique_consignment_id, box_description, dim1, dim2, dim3, weight, is_floor_only,
				length_upright_allowed, width_upright_allowed, height_upright_allowed, is_stackable,
				stack_weight_length_upright, stack_weight_width_upright, stack_weight_height_upright, color);
		c.setPriority(priority);
		return c;
	}

	public String getBoxDescription() {
		return box_description;
	}

	public int getLength() {
		return dim1;
	}

	public int getWidth() {
		return dim2;
	}

	public int getHeight() {
		return dim3;
	}

	public int getWeight() {
		return weight;
	}

	public boolean getIsFloorOnly() {
		return is_floor_only == 0 ? false : true;
	}

	public boolean getIsStackable() {
		return is_stackable == 0 ? false : true;
	}

	public boolean getLength_upright_allowed() {
		return length_upright_allowed == 0 ? false : true;
	}

	public boolean getWidth_upright_allowed() {
		return width_upright_allowed == 0 ? false : true;
	}

	public boolean getHeight_upright_allowed() {
		return height_upright_allowed == 0 ? false : true;
	}

	public int getStack_weight_length_upright() {
		return stack_weight_length_upright;
	}

	public int getStack_weight_width_upright() {
		return stack_weight_width_upright;
	}

	public int getStack_weight_height_upright() {
		return stack_weight_height_upright;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getVolume() {
		return volume;
	}

	public boolean getIsPacked() {
		return is_packed;
	}

	public void setIsPacked(boolean val) {
		is_packed = val;
	}

	public int getCOX() {
		return cox;
	}

	public int getCOY() {
		return coy;
	}

	public int getCOZ() {
		return coz;
	}

	public void setCOX(int val) {
		// System.out.println("Cox: " + val);
		cox = val;
	}

	public void setCOY(int val) {
		// System.out.println("Coy: " + val);
		coy = val;
	}

	public void setCOZ(int val) {
		// System.out.println("Coz: " + val);
		coz = val;
	}

	public void setCOXYZ(int valX, int valY, int valZ) {
		setCOX(valX);
		setCOY(valY);
		setCOZ(valZ);
	}

	public int getPackX() {
		return packx;
	}

	public void setPackX(int packedDim1) {
		packx = packedDim1;
	}

	public int getPackY() {
		return packy;
	}

	public void setPackY(int packedDim2) {
		packy = packedDim2;
	}

	public int getPackZ() {
		return packz;
	}

	public void setPackZ(int packedDim3) {
		packz = packedDim3;
	}

	public void setPackXYZ(int dim1, int dim2, int dim3) {
		// System.out.println(String.format("packXyz: %d,%d,%d" ,dim1,dim2, dim3));
		packx = dim1;
		packy = dim2;
		packz = dim3;
	}

	public int getBoxID() {
		return unique_consignment_id;
	}

	@Override
	public String toString() {
		return String.format("Box ID = %s\nDescription = %s\nDimensions = %s\nVolume = %d cubic cm\nWeight = %d gram", getBoxID(),
				getBoxDescription(), getDimensionsToString(), getVolume(), getWeight());
	}

	private String getDimensionsToString() {
		return String.format("%.2f x %.2f x %.2f m", dim1 / 100.0, dim2 / 100.0, dim3 / 100.0);
	}

	public int getWall() {
		return wall;
	}

	public void setWall(int wall) {
		this.wall = wall;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getPositionInLayer() {
		return positionInLayer;
	}

	public void setPositionInLayer(int positionInLayer) {
		this.positionInLayer = positionInLayer;
	}

	public void setVolume() {
		volume = getLength() * getWidth() * getHeight();
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
