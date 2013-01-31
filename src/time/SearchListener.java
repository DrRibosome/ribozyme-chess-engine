package time;

public interface SearchListener {
	/**
	 * called immediately after a ply has been searched
	 * @param move best move chosen at the current searched ply
	 * @param ply
	 */
	public void plySearched(long move, int ply);
	public void failLow(int ply);
	public void failHigh(int ply);
}
