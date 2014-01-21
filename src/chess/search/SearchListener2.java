package chess.search;

public interface SearchListener2 {
	/**
	 * called immediately after a ply has been searched
	 * @param move best move chosen at the current searched ply
	 * @param ply
	 * @param score determined score after evalueating this ply
	 */
	public void plySearched(long move, int ply, int score);
	public void failLow(int ply);
	public void failHigh(int ply);
}
