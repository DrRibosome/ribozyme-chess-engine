package chess.eval;

import chess.eval.e9.pipeline.EvalResult;
import chess.state4.State4;

public interface Evaluator {
	
	public EvalResult eval(int player, State4 s);
	
	/**
	 * computes a score for the passed board state
	 * <p> score is returned via encoding defined by {@link ScoreEncoder}
	 * @param player
	 * @param s
	 * @param lowerBound score to cut off chess.eval at when below
	 * @param upperBound score to cut off chess.eval at when above
	 * @return returns score encoding
	 */
	public EvalResult eval(int player, State4 s, int lowerBound, int upperBound);
	
	/**
	 * refines a score produced by {@link #eval(int, State4, int, int)} to
	 * the specified adjusted bounds
	 * @param player
	 * @param s
	 * @param lowerBound
	 * @param upperBound
	 * @param scoreEncoding
	 * @return
	 */
	public EvalResult refine(int player, State4 s, int lowerBound, int upperBound, long scoreEncoding);
	
	/** clear out any stored chess.eval values, reset for new chess.search*/
	public void reset();
}
