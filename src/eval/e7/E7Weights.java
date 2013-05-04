package eval.e7;

public final class E7Weights {
	/**
	 * gives bonus multiplier to the value of sliding pieces
	 * mobilitiy scores based on how cluttered the board is
	 * <p>
	 * sliding pieces with high movement on a clutterd board
	 * are more valuable
	 * <p>
	 * indexed [num-pawn-attacked-squares]
	 */
	final static double[] clutterIndex; 
	
	static{
		//linear interpolation
		clutterIndex = new double[64];
		final double start = .9;
		final double end = 1.1;
		final double diff = end-start;
		for(int a = 0; a < 64; a++){
			clutterIndex[a] = start + diff*(a/64.);
		}
	}
}
