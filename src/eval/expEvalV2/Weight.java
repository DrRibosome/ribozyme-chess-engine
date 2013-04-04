package eval.expEvalV2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Weight {
	//private final int startMaterial, endMaterial, margin;
	public final int start, end;
	
	/*Weight(final int start, final int end, final int[] materialWeights){
		startMaterial = (
				  materialWeights[State4.PIECE_TYPE_PAWN]*8
				+ materialWeights[State4.PIECE_TYPE_KNIGHT]*2
				+ materialWeights[State4.PIECE_TYPE_BISHOP]*2
				+ materialWeights[State4.PIECE_TYPE_ROOK]*2
				+ materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		endMaterial = (
				materialWeights[State4.PIECE_TYPE_ROOK]
				+ materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		
		margin = endMaterial-startMaterial;
		this.start = start;
		this.end = end;
	}*/

	public Weight(final int start, final int end){
		this.start = start;
		this.end = end;
	}
	
	/**  calculates the margin to use in {@link #getScale(int, int, int)}*/
	public static int margin(final int startMaterial, final int endMaterial){
		return endMaterial-startMaterial;
	}
	
	/** gets the interpolatino factor for the weight*/
	public static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
		return min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
	}
	
	/** calculates score, interpolating with scale calculated from {@link #getScale(int, int, int)}*/
	public int score(final double p){
		return start + (int)((end-start)*p);
	}
	
	private static double min(final double d1, final double d2){
		return d1 < d2? d1: d2;
	}
	
	public void writeWeight(final DataOutputStream dos) throws IOException{
		dos.writeShort(start);
		dos.writeShort(end);
	}
	
	public Weight readWeight(final DataInputStream dis, final int[] materialWeights) throws IOException{
		int start = dis.readShort();
		int end = dis.readShort();
		return new Weight(start, end);
	}
}
