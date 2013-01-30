package eval;

import util.board4.BitUtil;
import util.board4.Masks;
import util.board4.State4;

/** extracts board features*/
public final class FeatureExtractor {
	/**
	 * feature set
	 * <p>
	 * All piece lists sorted by least sig. bit. All piece lists may have
	 * unused spots filled with bad values if there are not enough
	 * pieces to fill the lists.
	 */
	public static class FeatureSet{
		final int[] pawnRow = new int[8];
		final int[] pawnCol = new int[8];
		/** records passed pawns, 0 = not passed, 1 = passed*/
		final long[] pawnPassed = new long[8];
		/** records unopposed pawns, passed pawns not counted for this*/
		final long[] pawnUnopposed = new long[8];
	}
	
	public static void loadFeatures(FeatureSet f, final int player, final State4 s, boolean processPawns){
		if(processPawns){
			processPawns(f, player, s);
		}
	}
	
	public static void processPawns(FeatureSet f, int player, State4 s){
		final int len = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		final long enemyPawns = s.pawns[1-player];
		int a = 0;
		for(long pawns = s.pawns[player]; a < len; pawns &= pawns-1, a++){
			final long p = pawns&-pawns;
			final int index = BitUtil.lsbIndex(p);
			f.pawnRow[a] = index/8;
			f.pawnCol[a] = index%8;
			f.pawnPassed[a] = BitUtil.isDef(Masks.passedPawnMasks[player][index] & enemyPawns);
			f.pawnUnopposed[a] = BitUtil.isDef(Masks.unopposePawnMasks[player][index] & enemyPawns)-f.pawnPassed[a];
		}
	}
}
