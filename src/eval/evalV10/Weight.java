package eval.evalV10;

import state4.State4;

final class Weight{
	public final int startScore, scoreDiff;
	public static int startMaterial, endMaterial, currentMargin, margin; 
	
	static {
		startMaterial = 
				  EvalWeights.pieceValues[State4.PIECE_TYPE_PAWN]*8
				+ EvalWeights.pieceValues[State4.PIECE_TYPE_KNIGHT]*2
				+ EvalWeights.pieceValues[State4.PIECE_TYPE_BISHOP]*2
				+ EvalWeights.pieceValues[State4.PIECE_TYPE_ROOK]*2
				+ EvalWeights.pieceValues[State4.PIECE_TYPE_QUEEN];
		startMaterial *= 2;
		
		endMaterial = 
				EvalWeights.pieceValues[State4.PIECE_TYPE_ROOK]
				+ EvalWeights.pieceValues[State4.PIECE_TYPE_QUEEN];
		
		endMaterial *= 2;
		
		margin = endMaterial-startMaterial;
		currentMargin = 0;
	}
	
	public Weight(int s, int e){
		startScore = s;
		scoreDiff = e-s;
	}
	
	public static Weight W(int m, int e){
		return new Weight(m, e);
	} 
	
	public static void updateWeightScaling(int materialTotal){
		currentMargin = materialTotal-startMaterial;
	}

	public int getScore(){
		final double scale = currentMargin/margin;
		return (int)(startScore + scale*scoreDiff);
	}
	
	public String toString(){
		return "getScore: " + getScore() + " startScore: " + startScore + "scoreDiff: " + scoreDiff;
	}
}
