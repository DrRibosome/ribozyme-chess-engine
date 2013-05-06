package eval;

import state4.State4;

public interface Evaluator2 {
	/**
	 * computes a score for the passed board state
	 * @param state
	 * @return returns evaluated board state score
	 */
	public int eval(State4 state, int player);
	
	/** quick, but rough, eval*/
	public int lazyEval(State4 state, int player);
	
	/** 
	 * called whenever a piece is moved to allow the evaluator to update
	 * things incrementally
	 * @param encoding move encoding
	 */
	public void processMove(long encoding);
	
	/**
	 * called when a move is undone
	 * @param encoding
	 */
	public void undoMove(long encoding);
	
	public void initialize(State4 state);
	
	public void reset();
}
