package eval.legacy.evalV9;

import state4.State4;

final class Weight9
{
	public final double startScore, scoreDiff;
	public static double startMaterial, endMaterial, currentMargin, margin; 
	
	static 
	{
		startMaterial = 
				  Eval9Weights.pieceValues[State4.PIECE_TYPE_PAWN]*8
				+ Eval9Weights.pieceValues[State4.PIECE_TYPE_KNIGHT]*2
				+ Eval9Weights.pieceValues[State4.PIECE_TYPE_BISHOP]*2
				+ Eval9Weights.pieceValues[State4.PIECE_TYPE_ROOK]*2
				+ Eval9Weights.pieceValues[State4.PIECE_TYPE_QUEEN];
		startMaterial *= 2;
		
		endMaterial = 
				Eval9Weights.pieceValues[State4.PIECE_TYPE_ROOK]
				+ Eval9Weights.pieceValues[State4.PIECE_TYPE_QUEEN];
		
		endMaterial *= 2;
		
		margin = endMaterial-startMaterial;
		currentMargin = 0;
	}
	
	public Weight9(int s, int e){
		startScore = s;
		scoreDiff = e-s;
	}
	
	public static Weight9 W(int m, int e)
	{ 
		return new Weight9(m, e);
	} 
	
	public static void updateWeightScaling(int materialTotal){
		currentMargin = materialTotal-startMaterial;
	}
	
	public static boolean isEndgame()
	{
		return currentMargin == margin;
	}

	public int getScore(){
		double scale = currentMargin/margin;
		return (int)(startScore + scale*scoreDiff);
		/*double score = scoreDiff * (1-(currentMargin / margin));
		return (int) (startScore - score);*/
	}
	
	public String toString()
	{
		return "getScore: " + getScore() + " startScore: " + startScore + "scoreDiff: " + scoreDiff;
	}
}
