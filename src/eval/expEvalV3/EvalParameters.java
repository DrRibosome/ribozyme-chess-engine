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
	
	public int[] dangerKingAttacks;
	public Weight[] kingDangerValues;
	
	//-------------------------------------------------
	//pawns weights
	
	/** passed pawn weight, indexed [row] from white perspective*/
	public Weight[][] passedPawnRowWeight;
	public Weight doubledPawnsWeight;
	public Weight tripledPawnsWeight;
}
