package eval.expEvalV2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import state4.State4;

public final class Weight {
	private final int startMaterial, endMaterial, margin;
	private final int start, end;
	
	Weight(final int start, final int end, final int[] materialWeights){
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
	}
	
	public int score(final int totalMaterialScore){
		final double p = 1-(endMaterial-totalMaterialScore)*1./margin;
		return start + (int)((end-start)*p);
	}
	
	public static void writeWeight(final Weight w, final DataOutputStream dos) throws IOException{
		dos.writeShort(w.start);
		dos.writeShort(w.end);
	}
	
	public Weight readWeight(final DataInputStream dis, final int[] materialWeights) throws IOException{
		int start = dis.readShort();
		int end = dis.readShort();
		return new Weight(start, end, materialWeights);
	}
}
