package chess.eval.e9.pipeline;

/** collection class for basic eval primitives*/
public class BasicAttributes {
	/** all material score, from perspective of eval player*/
	final int materialScore;
	/** non pawn material score, from perspective of eval player*/
	final int nonPawnMaterialScore;
	/** sum of scores for all material regardless of side*/
	final int totalMaterialScore;

	public BasicAttributes(int materialScore, int nonPawnMaterialScore, int totalMaterialScore) {
		this.materialScore = materialScore;
		this.nonPawnMaterialScore = nonPawnMaterialScore;
		this.totalMaterialScore = totalMaterialScore;
	}
}
