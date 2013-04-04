package eval.expEvalV3;

public final class EvalParameters {

	//-------------------------------------------------
	//general weights
	
	public int[] materialWeights;
	/** mobility weights, indexed [piece-type][movement]*/
	public Weight[][] mobilityWeights;
	public Weight tempo;
	public Weight bishopPair;
	
	//-------------------------------------------------
	//king danger
	
	/** danger for attacks on the king, indexed [piece-type]*/
	public int[] dangerKingAttacks;
	/** king danger index, indexed [danger]*/
	public Weight[] kingDangerValues;
	/** king danger based off location of the king*/
	public int[][] kingDangerSquares;
	
	//-------------------------------------------------
	//pawns weights
	
	/** passed pawn weight, indexed [row] from white perspective*/
	public Weight[][] passedPawnRowWeight;
	public Weight doubledPawnsWeight;
	public Weight tripledPawnsWeight;
}
