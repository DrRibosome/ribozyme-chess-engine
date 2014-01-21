package chess.eval;

import chess.state4.State4;

public interface Evaluator {
	
	public int eval(int player, State4 s);
	
	/**
	 * computes a score for the passed board state
	 * <p> score is returned via encoding defined by {@link ScoreEncoder}
	 * @param player
	 * @param s
	 * @param lowerBound score to cut off chess.eval at when below
	 * @param upperBound score to cut off chess.eval at when above
	 * @return returns score encoding
	 */
	public int eval(int player, State4 s, int lowerBound, int upperBound);
	
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
	public int refine(int player, State4 s, int lowerBound, int upperBound, int scoreEncoding);
	
	/** 
	 * called whenever a piece is moved to allow the evaluator to update
	 * things incrementally
	 * @param encoding move encoding
	 */
	public void makeMove(long encoding);
	
	/**
	 * called when a move is undone
	 * @param encoding
	 */
	public void undoMove(long encoding);
	
	/** initialize chess.eval, called at the beginning of a new chess.search before descent*/
	public void initialize(State4 state);
	
	/** clear out any stored chess.eval values, reset for new chess.search*/
	public void reset();
}
