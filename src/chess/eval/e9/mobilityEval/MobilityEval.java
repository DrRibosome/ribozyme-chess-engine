package chess.eval.e9.mobilityEval;

import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.State4;
import chess.eval.PositionMasks;
import chess.eval.e9.Weight;

/** container class for chess.eval pertaining to mobility*/
public final class MobilityEval {

	public final static class MobilityResult{
		public final int score;
		public final long attackMask;

		public MobilityResult(int score, long attackMask) {
			this.score = score;
			this.attackMask = attackMask;
		}
	}

	private final int[] knightMobilityWeights;
	private final int[] bishopMobilityWeights;
	private final int[] rookMobilityWeights;
	private final int[] queenMobilityWeights;

	private final int rookColHalfOpen;
	private final int rookColOpen;
	
	/** compute logistic (sigmoid) function centered around 0*/
	private static double logistic(double x){
		return 1./(1+Math.exp(-x))-.5;
	}
	
	/**
	 * fill passed result store with values from parameterized centered logistic function
	 * @param params parameters as [x offset, y offset, range], where each value is a tuple (x,y)
	 * @param store result store
	 * @see #logistic(double)
	 */
	private static void interpolate(int[] params, int[] store){
		for(int a = 0; a < store.length; a++){
			store[a] = Weight.encode(
					(int)(params[4]*logistic(a+params[0])+params[2]+.5),
					(int)(params[5]*logistic(a+params[1])+params[3]+.5));
		}
	}

	public final static class MobilityWeights{
		public final int[] knightWeights = new int[]{-2, -2, -2, -2, 20, 60};
		public final int[] bishopWeights = new int[]{0, -3, -4, -2, 60, 45};
		public final int[] rookWeights = new int[]{-3,-3, -4,-7, 30,70};
		public final int[] queenWeights = new int[]{-3,-2, -4,-20, 20,60};

		/** weight for rook in half open column*/
		public final int[] rookColHalfOpen = new int[]{6, 15};
		/** weight for rook in fully open column*/
		public final int[] rookColOpen = new int[]{12, 30};
	}

	public MobilityEval(MobilityWeights weights) {
		knightMobilityWeights = new int[9];
		interpolate(weights.knightWeights, knightMobilityWeights);
		
		bishopMobilityWeights = new int[16];
		interpolate(weights.bishopWeights, bishopMobilityWeights);

		rookMobilityWeights = new int[16];
		interpolate(weights.rookWeights, rookMobilityWeights);
		
		queenMobilityWeights = new int[32];
		interpolate(weights.queenWeights, queenMobilityWeights);

		rookColHalfOpen = S(weights.rookColHalfOpen[0], weights.rookColHalfOpen[1]);
		rookColOpen = S(weights.rookColOpen[0], weights.rookColOpen[1]);
	}
	
	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return Weight.encode(start, end);
	}

	/** calculates mobility and danger to enemy king from mobility*/
	public MobilityResult scoreMobility(final int player, final State4 s, final double clutterMult){
		int mobScore = 0;
		
		final long alliedPawns = s.pawns[player];
		final long enemyPawns = s.pawns[1-player];
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, enemyPawns);
		
		final long allied = s.pieces[player];
		final long enemy = s.pieces[1-player];
		final long agg = allied | enemy;
		final long aggPawns = alliedPawns | enemyPawns;
		final long blockedAlliedPawns; //allied pawns whose movement is blocked by another pawn
		final long chainedAlliedPawns; //allied pawns that are supporting other allied pawns
		
		if(player == 0){
			final long movementBlocked = ((alliedPawns << 8) & aggPawns) >>> 8;
			final long supportPawns = (((alliedPawns << 7) & alliedPawns) >>> 7) |
					(((alliedPawns << 9) & alliedPawns) >>> 9);
			blockedAlliedPawns = movementBlocked;
			chainedAlliedPawns = supportPawns;
		} else{
			final long movementBlocked = ((alliedPawns >>> 8) & aggPawns) << 8;
			final long supportPawns = (((alliedPawns >>> 7) & alliedPawns) << 7) |
					(((alliedPawns >>> 9) & alliedPawns) << 9);
			blockedAlliedPawns = movementBlocked;
			chainedAlliedPawns = supportPawns;
		}
		
		long bishopAttackMask = 0;
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long b = bishops & -bishops;
			final long rawMoves = Masks.getRawBishopMoves(agg, b);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			mobScore += Weight.multWeight(bishopMobilityWeights[count], clutterMult);
			bishopAttackMask |= rawMoves;
			
			//penalize bishop for blocking allied pawns on bishop color
			final long squareMask = (PositionMasks.bishopSquareMask[0] & b) != 0?
					PositionMasks.bishopSquareMask[0]: PositionMasks.bishopSquareMask[1];
			final int blockingPawnsCount = (int)BitUtil.getSetBits(blockedAlliedPawns & squareMask);
			final int supportingPawnsCount = (int)BitUtil.getSetBits(chainedAlliedPawns & squareMask & ~blockedAlliedPawns);
			mobScore += S(blockingPawnsCount*-5 + supportingPawnsCount*-1, 0);
		}

		long knightAttackMask = 0;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long k = knights & -knights;
			final long rawMoves = Masks.getRawKnightMoves(k);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			mobScore += Weight.multWeight(knightMobilityWeights[count], clutterMult);
			knightAttackMask |= rawMoves;
		}

		long rookAttackMask = 0;
		final long allPieces = s.pieces[0]|s.pieces[1];
		final int alliedKingIndex = BitUtil.lsbIndex(s.kings[player]);
		final int alliedKingCol = alliedKingIndex%8;
		final int alliedKingRow = alliedKingIndex >>> 3;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks&-rooks;
			final long rawMoves = Masks.getRawRookMoves(agg, r);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int moveCount = (int)BitUtil.getSetBits(moves);
			mobScore += Weight.multWeight(rookMobilityWeights[moveCount], clutterMult);
			rookAttackMask |= rawMoves;
			
			final int rindex = BitUtil.lsbIndex(r);
			final int col = rindex%8;
			if(((allPieces & ~r) & Masks.colMask[col]) == 0){ //tests file open
				mobScore += rookColOpen;
			} else if(isHalfOpen(col, enemyPawns, allPieces & ~r)){ //tests file half open
				mobScore += rookColHalfOpen;
			}

			final int row = rindex >>> 3;
			if(row == alliedKingRow){
				final int backRank = player == 0? 0: 7;
				if(alliedKingRow == backRank && moveCount <= 4){
					if(alliedKingCol >= 4 && col > alliedKingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][1]){
							mobScore += S(-20, -50); //trapped, but can castle right
						} else{
							mobScore += S(-50, -120); //trapped, cannot castle
						}
					}
					if(alliedKingCol <= 3 && col < alliedKingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][0]){
							mobScore += S(-20, -50); //trapped, but can castle left
						} else{
							mobScore += S(-50, -120); //trapped, cannot castle
						}
					}
				}
			}
		}

		long queenAttackMask = 0;
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long q = queens&-queens;
			final long rawMoves = Masks.getRawQueenMoves(agg, q);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			mobScore += Weight.multWeight(queenMobilityWeights[count], clutterMult);
			queenAttackMask |= rawMoves;
		}
		
		final long pawnAttackMask = Masks.getRawPawnAttacks(player, alliedPawns);
		long attackMask = bishopAttackMask | knightAttackMask | rookAttackMask | queenAttackMask | pawnAttackMask;
		
		return new MobilityResult(mobScore, attackMask);
	}

	
	/**
	 * 
	 * @param col
	 * @param enemyPawns
	 * @param pieces pieces excluding the rook to be tested
	 * @return
	 */
	private static boolean isHalfOpen(final int col, final long enemyPawns, final long pieces){
		final long mask = Masks.colMask[col];
		if((mask & pieces) == 0) return true; //column is fully open
		if((mask & (pieces & ~enemyPawns)) == 0){
			//no pieces except for pawns
			final long c = enemyPawns & mask;
			if((c & (c-1)) == 0){
				return true; //only one pawn in the column
			}
		}
		return false;
	}
}
