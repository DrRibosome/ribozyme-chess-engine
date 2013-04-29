package eval.legacy.evalV10;

import state4.State4;

final class Weight10{
	public final int startScore, scoreDiff;
	public static int startMaterial, endMaterial, currentMargin, margin; 
	
	static {
		startMaterial = 
				  Eval10Weights.pieceValues[State4.PIECE_TYPE_PAWN]*8
				+ Eval10Weights.pieceValues[State4.PIECE_TYPE_KNIGHT]*2
				+ Eval10Weights.pieceValues[State4.PIECE_TYPE_BISHOP]*2
				+ Eval10Weights.pieceValues[State4.PIECE_TYPE_ROOK]*2
				+ Eval10Weights.pieceValues[State4.PIECE_TYPE_QUEEN];
		startMaterial *= 2;
		
		endMaterial = 
				Eval10Weights.pieceValues[State4.PIECE_TYPE_ROOK]
				+ Eval10Weights.pieceValues[State4.PIECE_TYPE_QUEEN];
		
		endMaterial *= 2;
		
		margin = endMaterial-startMaterial;
		currentMargin = 0;
	}
	
	public Weight10(int s, int e){
		startScore = s;
		scoreDiff = e-s;
	}
	
	public static Weight10 W(int m, int e){
		return new Weight10(m, e);
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
