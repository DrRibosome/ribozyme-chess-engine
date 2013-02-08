package eval.expEvalV1;

import util.board4.BitUtil;
import util.board4.Masks;
import util.board4.State4;

/** extracts board features*/
public final class FeatureExtractor {
	private final static int[] zeroi = new int[8];
	
	/**
	 * feature set
	 * <p>
	 * All piece lists sorted by least sig. bit. All piece lists may have
	 * unused spots filled with bad values if there are not enough
	 * pieces to fill the lists.
	 */
	public final static class FeatureSet{
		//pawn values
		public final int[] pawnRow = new int[8];
		public final int[] pawnCol = new int[8];
		/** records passed pawns, 0 = not passed, 1 = passed*/
		public final long[] pawnPassed = new long[8];
		/** records unopposed pawns, passed pawns not counted for this*/
		public final long[] pawnUnopposed = new long[8];
		/** total doubled pawns*/
		public int doubledPawns;
		/** total tripled pawns*/
		public int tripledPawns;
		/** total pawns in each col col*/
		public final int[] pawnColCount = new int[8];
		/** pawn has a pawn defender to either the right or left*/
		public final int[] supportedPawn = new int[8];
		
		/** mobility of minor and major pieces, cummulative mobility stored at index 0*/
		public final int[] mobility = new int[7];
	}
	
	public static void loadFeatures(FeatureSet f, final int player, final State4 s, boolean processPawns){
		if(processPawns){
			processPawns(f, player, s);
		}
		processMobility(f, player, s);
	}
	
	public static void processMobility(FeatureSet f, int player, State4 s){
		f.mobility[State4.PIECE_TYPE_EMPTY] = 0;
		
		f.mobility[State4.PIECE_TYPE_BISHOP] = 0;
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long p = State4.getBishopMoves(player, s.pieces, bishops&-bishops);
			f.mobility[State4.PIECE_TYPE_BISHOP] += BitUtil.getSetBits(p);
		}
		f.mobility[State4.PIECE_TYPE_EMPTY] += f.mobility[State4.PIECE_TYPE_BISHOP];
		
		f.mobility[State4.PIECE_TYPE_KNIGHT] = 0;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long p = State4.getKnightMoves(player, s.pieces, knights&-knights);
			f.mobility[State4.PIECE_TYPE_KNIGHT] += BitUtil.getSetBits(p);
		}
		f.mobility[State4.PIECE_TYPE_EMPTY] += f.mobility[State4.PIECE_TYPE_KNIGHT];
		
		f.mobility[State4.PIECE_TYPE_QUEEN] = 0;
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long p = State4.getQueenMoves(player, s.pieces, queens&-queens);
			f.mobility[State4.PIECE_TYPE_QUEEN] += BitUtil.getSetBits(p);
		}
		f.mobility[State4.PIECE_TYPE_EMPTY] += f.mobility[State4.PIECE_TYPE_QUEEN];
		
		f.mobility[State4.PIECE_TYPE_ROOK] = 0;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long p = State4.getRookMoves(player, s.pieces, rooks&-rooks);
			f.mobility[State4.PIECE_TYPE_ROOK] += BitUtil.getSetBits(p);
		}
		f.mobility[State4.PIECE_TYPE_EMPTY] += f.mobility[State4.PIECE_TYPE_ROOK];
	}
	
	public static void processPawns(FeatureSet f, int player, State4 s){
		f.doubledPawns = 0;
		f.tripledPawns = 0;
		System.arraycopy(zeroi, 0, f.pawnColCount, 0, 8);
		
		final int len = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		final long enemyPawns = s.pawns[1-player];
		final long allied = s.pawns[player];
		int a = 0;
		for(long pawns = s.pawns[player]; a < len; pawns &= pawns-1, a++){
			final long p = pawns&-pawns;
			final int index = BitUtil.lsbIndex(p);
			final int col = index%8;
			f.pawnRow[a] = index/8;
			f.pawnCol[a] = col;
			
			f.supportedPawn[a] = 0;
			f.supportedPawn[a] += BitUtil.isDef(PositionMasks.supportedPawn[player][index] & allied);
			
			f.pawnColCount[col]++;
			switch(f.pawnColCount[col]){
			case 2:
				f.doubledPawns++;
			case 3:
				f.doubledPawns--;
				f.tripledPawns++;
			}
			
			//passed and not behind an allied pawn
			f.pawnPassed[a] = (1-BitUtil.isDef(Masks.passedPawnMasks[player][index] & enemyPawns)) *
					(1-BitUtil.isDef(Masks.unopposePawnMasks[player][index] & allied));
			f.pawnUnopposed[a] = (1-BitUtil.isDef(Masks.unopposePawnMasks[player][index] & enemyPawns))-f.pawnPassed[a];
		}
	}
}
