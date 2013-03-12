package eval;

public interface Evaluator2<BoardRepresentation> {
	/**
	 * computes a score for the passed board state
	 * @param state
	 * @return returns evaluated board state score
	 */
	public int eval(BoardRepresentation state, int player);
	
	/** quick, but rough, eval*/
	public int lazyEval(BoardRepresentation state, int player);
	
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
	
	public void initialize(BoardRepresentation state);
}
