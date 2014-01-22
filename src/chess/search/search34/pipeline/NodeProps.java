package chess.search.search34.pipeline;

class NodeProps {
	/** static eval for current node*/
	final int eval;
	/** raw score encoding for a node outputed by the evaluator*/
	final int scoreEncoding;
	final boolean alliedKingAttacked, pawnPrePromotion, hasNonPawnMaterial, nonMateScore;
	/** records presence of transposition table lookup move*/
	final boolean hasTTMove;

	NodeProps(int eval, int scoreEncoding, boolean alliedKingAttacked,
			  boolean pawnPrePromotion, boolean hasNonPawnMaterial,
			  boolean nonMateScore, boolean hasTTMove){
		this.eval = eval;
		this.scoreEncoding = scoreEncoding;
		this.alliedKingAttacked = alliedKingAttacked;
		this.pawnPrePromotion = pawnPrePromotion;
		this.hasNonPawnMaterial = hasNonPawnMaterial;
		this.nonMateScore = nonMateScore;
		this.hasTTMove = hasTTMove;
	}
}
