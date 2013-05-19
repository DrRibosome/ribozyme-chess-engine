package debug;

/** problem positions from 'chess, tactics for advanced players'*/
public final class AdvancedTactics {
	/** bm probably Bd2+, cant take rook because leads to check*/
	public final static String apPos20 = "6r1/8/7k/p2B3P/1B6/8/8/7K w - - - -";
	/** bm probably Ra2, which keeps all pieces locked up, allowing easy pawn win for white*/
	public final static String apPos21 = "5k2/p5pp/r7/2pB2p/b1P5/6P1/4PP1P/R5K1 w - - - -";
	
	
	//mating positions
	public final static String kbbMate1 = "BBK5/8/8/8/8/8/8/k7 w - - - -"; 
	public final static String kqMate1 = "1QK5/8/8/8/4k3/8/8/8 w - - - -"; 
	public final static String knbMate1 = "2NBK3/8/8/8/4k3/8/8/8 w - - - -"; 
}
