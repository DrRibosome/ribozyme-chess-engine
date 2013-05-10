package eval.e7;

import eval.Weight;

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
	
	private static Weight S(int start, int end){
		return new Weight(start, end);
	}
	
	public final static Weight[][] isolatedPawns = new Weight[][]{
			{S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25)},
			{S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17)},
	};
	
	public final static Weight[] pawnChain = new Weight[]{
			S(10,0), S(13,0), S(15,1), S(20,5), S(20,5), S(15,1), S(13,0), S(10,0)
	};
	
	public final static Weight[][] doubledPawns = new Weight[][]{
			{S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17)},
			{S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17), S(-17,-17)},
	};
}
