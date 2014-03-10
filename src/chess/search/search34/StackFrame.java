package chess.search.search34;

import chess.eval.e9.pipeline.EvalResult;
import chess.search.search34.moveGen.MoveList;

public class StackFrame {
	private final static int defSize = 128;

	public final MoveList mlist = new MoveList(defSize);
	/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
	public final long[] killer = new long[2];
	public boolean skipNullMove;


	public long zkey;

	/** static eval for current node*/
	public int eval;
	/** static node state evaluation outputed via an {@linkplain chess.eval.Evaluator}*/
	public EvalResult staticScore;
	public boolean alliedKingAttacked, pawnPrePromotion, hasNonPawnMaterial, nonMateScore;

	/** records presence of transposition table lookup move*/
	public boolean hasTTMove;
	/** encoding of move retrieved from transposition table*/
	public long tteMoveEncoding;
}
