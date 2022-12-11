package eb_afit;

import java.util.ArrayList;
import java.util.List;

import clpBasic.eclipselink.entities.Consignment;

/**
 * @author Windows Vikram Manjare (CS20M070 IITM 2021) 24-Aug-2021
 * 
 *         Class for holding details of output of CLP algorithm, used as part of
 *         ContainerPackingResult
 * 
 *         Referenced C# repository
 *         https://github.com/davidmchapman/3DContainerPacking
 */
public class AlgorithmPackingResult {
	private int AlgorithmID, TotalBoxCount;
	private String AlgorithmName;
	private boolean IsCompletePack;
	private List<Consignment> PackedItems, UnpackedItems;
	private long PackTimeInMilliseconds;
	private double PercentContainerVolumePacked, PercentItemVolumePacked, PercentContainerTonnagePacked,
			PercentItemWeightPacked;

	// EB_AFIT specific properties
	private int apr_itenum, apr_bestite, apr_bestvariant, apr_totalboxvol, apr_bestvolume, apr_px, apr_py, apr_pz,
			apr_totalboxweight, apr_bestweight;
	private int packedLength, packedHeight, packedWeight;

	public AlgorithmPackingResult() {
		PackedItems = new ArrayList<Consignment>();
		UnpackedItems = new ArrayList<Consignment>();
	}

	public int getAlgorithmID() {
		return AlgorithmID;
	}

	public void setAlgorithmID(int algorithmID) {
		AlgorithmID = algorithmID;
	}

	public String getAlgorithmName() {
		return AlgorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		AlgorithmName = algorithmName;
	}

	public boolean isIsCompletePack() {
		return IsCompletePack;
	}

	public void setIsCompletePack(boolean isCompletePack) {
		IsCompletePack = isCompletePack;
	}

	public List<Consignment> getPackedItems() {
		return PackedItems;
	}

	public void setPackedItems(List<Consignment> packedItems) {
		if (PackedItems != null) {
			PackedItems.addAll(packedItems);
			return;
		}
		PackedItems = packedItems;
	}

	public List<Consignment> getUnpackedItems() {
		return UnpackedItems;
	}

	public void setUnpackedItems(List<Consignment> unpackedItems) {
		if (UnpackedItems != null) {
			UnpackedItems.addAll(unpackedItems);
			return;
		}
		UnpackedItems = unpackedItems;
	}

	public long getPackTimeInMilliseconds() {
		return PackTimeInMilliseconds;
	}

	public void setPackTimeInMilliseconds(long packTimeInMilliseconds) {
		PackTimeInMilliseconds = packTimeInMilliseconds;
	}

	public double getPercentContainerVolumePacked() {
		return PercentContainerVolumePacked;
	}

	public void setPercentContainerVolumePacked(double percentContainerVolumePacked) {
		PercentContainerVolumePacked = percentContainerVolumePacked;
	}

	public double getPercentItemVolumePacked() {
		return PercentItemVolumePacked;
	}

	public void setPercentItemVolumePacked(double percentItemVolumePacked) {
		PercentItemVolumePacked = percentItemVolumePacked;
	}

	public int getApr_itenum() {
		return apr_itenum;
	}

	public void setApr_itenum(int apr_itenum) {
		this.apr_itenum = apr_itenum;
	}

	public int getApr_bestite() {
		return apr_bestite;
	}

	public void setApr_bestite(int apr_bestite) {
		this.apr_bestite = apr_bestite;
	}

	public int getApr_bestvariant() {
		return apr_bestvariant;
	}

	public void setApr_bestvariant(int apr_bestvariant) {
		this.apr_bestvariant = apr_bestvariant;
	}

	public int getApr_totalboxvol() {
		return apr_totalboxvol;
	}

	public void setApr_totalboxvol(int apr_totalboxvol) {
		this.apr_totalboxvol = apr_totalboxvol;
	}

	public int getApr_bestvolume() {
		return apr_bestvolume;
	}

	public void setApr_bestvolume(int apr_bestvolume) {
		this.apr_bestvolume = apr_bestvolume;
	}

	public int getApr_px() {
		return apr_px;
	}

	public void setApr_px(int apr_px) {
		this.apr_px = apr_px;
	}

	public int getApr_py() {
		return apr_py;
	}

	public void setApr_py(int apr_py) {
		this.apr_py = apr_py;
	}

	public int getApr_pz() {
		return apr_pz;
	}

	public void setApr_pz(int apr_pz) {
		this.apr_pz = apr_pz;
	}

	public double getPercentContainerTonnagePacked() {
		return PercentContainerTonnagePacked;
	}

	public void setPercentContainerTonnagePacked(double percentContainerTonnagePacked) {
		PercentContainerTonnagePacked = percentContainerTonnagePacked;
	}

	public int getApr_totalboxweight() {
		return apr_totalboxweight;
	}

	public void setApr_totalboxweight(int apr_totalboxweight) {
		this.apr_totalboxweight = apr_totalboxweight;
	}

	public int getApr_bestweight() {
		return apr_bestweight;
	}

	public void setApr_bestweight(int apr_bestweight) {
		this.apr_bestweight = apr_bestweight;
	}

	public double getPercentItemWeightPacked() {
		return PercentItemWeightPacked;
	}

	public void setPercentItemWeightPacked(double percentItemWeightPacked) {
		PercentItemWeightPacked = percentItemWeightPacked;
	}

	public int getPackedLength() {
		return packedLength;
	}

	public void setPackedLength(int packedLength) {
		this.packedLength = packedLength;
	}

	public int getPackedHeight() {
		return packedHeight;
	}

	public void setPackedHeight(int packedHeight) {
		this.packedHeight = packedHeight;
	}

	public int getPackedWeight() {
		return packedWeight;
	}

	public void setPackedWeight(int packedWeight) {
		this.packedWeight = packedWeight;
	}

	public int getTotalBoxCount() {
		return TotalBoxCount;
	}

	public void setTotalBoxCount(int totalBoxCount) {
		TotalBoxCount = totalBoxCount;
	}
}
