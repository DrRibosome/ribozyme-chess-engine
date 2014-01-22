package chess.search.search34.pipeline;

class NodeProps {
	/** static eval for current node*/
	final int eval;
	final boolean alliedKingAttacked, pawnPrePromotion, hasNonPawnMaterial, nonMateScore;
	/** records presence of transposition table lookup move*/
	final boolean hasTTMove;

	NodeProps(int eval, boolean alliedKingAttacked,
			  boolean pawnPrePromotion, boolean hasNonPawnMaterial,
			  boolean nonMateScore, boolean hasTTMove){
		this.eval = eval;
		this.alliedKingAttacked = alliedKingAttacked;
		this.pawnPrePromotion = pawnPrePromotion;
		this.hasNonPawnMaterial = hasNonPawnMaterial;
		this.nonMateScore = nonMateScore;
		this.hasTTMove = hasTTMove;
	}
}
