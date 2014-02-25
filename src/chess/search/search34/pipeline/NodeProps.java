package chess.search.search34.pipeline;

import chess.eval.e9.pipeline.EvalResult;

final class NodeProps {
	final long zkey;

	/** static eval for current node*/
	final int eval;
	/** static node state evaluation outputed via an {@linkplain chess.eval.Evaluator}*/
	final EvalResult staticScore;
	final boolean alliedKingAttacked, pawnPrePromotion, hasNonPawnMaterial, nonMateScore;

	/** records presence of transposition table lookup move*/
	final boolean hasTTMove;
	/** encoding of move retrieved from transposition table*/
	final long tteMoveEncoding;

	NodeProps(long zkey, int eval, EvalResult staticScore, boolean alliedKingAttacked,
			  boolean pawnPrePromotion, boolean hasNonPawnMaterial,
			  boolean nonMateScore, boolean hasTTMove, long tteMoveEncoding){
		this.zkey = zkey;
		this.eval = eval;
		this.staticScore = staticScore;
		this.alliedKingAttacked = alliedKingAttacked;
		this.pawnPrePromotion = pawnPrePromotion;
		this.hasNonPawnMaterial = hasNonPawnMaterial;
		this.nonMateScore = nonMateScore;
		this.hasTTMove = hasTTMove;
		this.tteMoveEncoding = tteMoveEncoding;
	}

	/** convenience method for regenerating a node props object with tte move included*/
	NodeProps addTTEMove(long tteMoveEncoding){
		return new NodeProps(zkey, eval, staticScore,
				alliedKingAttacked, pawnPrePromotion,
				hasNonPawnMaterial, nonMateScore, true, tteMoveEncoding);
	}
}
