package chess.search.search34.moveGen;

import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.State4;

public final class MoveGen {

	public final static int tteMoveRank = 9999;
	/** rank set to the first of the non takes */
	public final static int killerMoveRank = 90;

	private final static int maxWeight = 1 << 10;

	private final FeatureSet[] f;

	private final static int[] pieceValue = new int[7];

	public MoveGen() {
		f = new FeatureSet[2];
		for (int a = 0; a < f.length; a++) f[a] = new FeatureSet();

		//note, piece values below don't necessarily correspond exactly to
		//piece values used in eval, and are instead only used to roughly
		//gauge the value of a move for move ordering purposes
		pieceValue[State4.PIECE_TYPE_PAWN] = 100;
		pieceValue[State4.PIECE_TYPE_BISHOP] = 300;
		pieceValue[State4.PIECE_TYPE_KNIGHT] = 300;
		pieceValue[State4.PIECE_TYPE_ROOK] = 470;
		pieceValue[State4.PIECE_TYPE_QUEEN] = 900;
	}

	private final static class FeatureSet {
		int pawnPromotionWeight;
		int passedPawnWeight;
		/**
		 * position weights, indexed [piece-type][start position][end position]
		 */
		final int[][][] posWeight = new int[7][64][64];
	}

	private final static long pawnLeftShiftMask = Masks.colMaskExc[7];
	private final static long pawnRightShiftMask = Masks.colMaskExc[0];

	/**
	 *
	 * @param player
	 * @param pieceMovingType
	 * @param pieceMask
	 * @param moves
	 * @param enemyAttacks positions defended by enemy pieces of lesser value
	 * @param enemyPieces
	 * @param mlist
	 * @param promotionMask promotion mask for pawns
	 * @param s
	 * @param f
	 */
	private static void recordMoves(final int player, final int pieceMovingType,
									final long pieceMask, final long moves,
									final long enemyAttacks, final long enemyPieces,
									final long promotionMask,
									final MoveList mlist, final State4 s, final FeatureSet f) {

		if (pieceMask != 0 && moves != 0) {

			final int[] mailbox = s.mailbox;
			final long piece = pieceMask & -pieceMask;

			for (long m = moves; m != 0; m &= m - 1) {
				final long move = m & -m;
				final int moveIndex = BitUtil.lsbIndex(move);

				//bonus for direct material gain from the move
				int gain;
				if((move & enemyPieces) != 0){
					final int takenValue = pieceValue[mailbox[moveIndex]];
					final int movingPieceValue = pieceValue[pieceMovingType];

					gain = takenValue - movingPieceValue/10;

					if(takenValue < movingPieceValue && (enemyAttacks & move) != 0){
						//apply penalty because in the short term there is possibility
						//of retaking the piece on opp. next turn for opp. material gain
						gain -= 50;
					}
				} else{
					gain = 0;
				}

				int historyWeight = f != null? getMoveWeight(player, pieceMovingType, piece, move, f, s) * 50 / maxWeight: 0;

				int baseRank = gain + historyWeight;

				if(pieceMovingType == State4.PIECE_TYPE_PAWN && (move & promotionMask) != 0){
					mlist.add(piece, move, baseRank + 900, State4.PROMOTE_QUEEN);
					mlist.add(piece, move, baseRank + 300, State4.PROMOTE_KNIGHT);
				} else{
					//non-pawn movement
					mlist.add(piece, move, baseRank);
				}
			}
		}
	}

	public void reset() {
		for (int a = 0; a < f.length; a++) {
			f[a].passedPawnWeight = 0;
			f[a].pawnPromotionWeight = 0;
			for (int q = 0; q < 7; q++) {
				for (int w = 0; w < 64; w++) {
					for (int s = 0; s < 64; s++) {
						f[a].posWeight[q][w][s] = 0;
					}
				}
			}
		}
	}

	public void alphaRaised(final int player, final int pieceType, final int startPos,
							final int movePos, final State4 s, final int depth) {
		final FeatureSet f = this.f[player];

		final int offset = depth;

		final int index = movePos;
		/*final long move = 1L << movePos;
		if(pieceType == State4.PIECE_TYPE_PAWN){
			final long ppMask = Masks.passedPawnMasks[player][index];
			if((ppMask & s.pawns[1-player]) == 0){
				//f.passedPawnWeight += offset;
				final long pomotionMask = Masks.pawnPromotionMask[player];
				if((move & pomotionMask) != 0){
					//f.pawnPromotionWeight += offset;
				}
			}
		}*/

		f.posWeight[pieceType][startPos][index] += offset;
		//f.pieceTypeWeight[pieceType] += offset;

		if (f.posWeight[pieceType][startPos][index] >= maxWeight ||
				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight) {
			dampen(f);
		}
	}

	public void betaCutoff(final int player, final int pieceType, final int startPos,
						   final int movePos, final State4 s, final int depth) {
		final FeatureSet f = this.f[player];

		final int offset = (depth * depth) >>> 1;

		final int index = movePos;
		/*final long move = 1L << movePos;
		if(pieceType == State4.PIECE_TYPE_PAWN){
			final long ppMask = Masks.passedPawnMasks[player][index];
			if((ppMask & s.pawns[1-player]) == 0){
				//f.passedPawnWeight += offset;
				final long pomotionMask = Masks.pawnPromotionMask[player];
				if((move & pomotionMask) != 0){
					//f.pawnPromotionWeight += offset;
				}
			}
		}*/

		f.posWeight[pieceType][startPos][index] += offset;
		//f.pieceTypeWeight[pieceType] += offset;

		if (f.posWeight[pieceType][startPos][index] >= maxWeight ||
				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight) {
			dampen(f);
		}
	}

	public void dampen() {
		dampen(f[0]);
		dampen(f[1]);
	}

	/**
	 * Lowers all weights
	 * <p> Called when a weight gets too high. Helps the history heuristic
	 * stay in line with the game tree being searched
	 *
	 * @param f
	 */
	private void dampen(final FeatureSet f) {
		f.passedPawnWeight >>>= 1;
		f.pawnPromotionWeight >>>= 1;
		for (int q = 1; q < 7; q++) {
			for (int a = 0; a < 64; a++) {
				for (int z = 0; z < 64; z++) {
					f.posWeight[q][a][z] >>>= 1;
				}
			}
		}
	}

	private static int getMoveWeight(final int player, final int pieceType,
									 final long piece, final long move, final FeatureSet f, final State4 s) {

		int rank = 0;

		final int startIndex = BitUtil.lsbIndex(piece);
		final int index = BitUtil.lsbIndex(move);

		if (pieceType == State4.PIECE_TYPE_PAWN) {
			final long ppMask = Masks.passedPawnMasks[player][index];
			if ((ppMask & s.pawns[1 - player]) == 0) {
				rank += f.passedPawnWeight;

				final long pomotionMask = Masks.pawnPromotionMask[player];
				if ((move & pomotionMask) != 0) {
					rank += f.pawnPromotionWeight;
				}
			}
		}

		rank += f.posWeight[pieceType][startIndex][index];

		return rank;
	}

	/**
	 * genereates moves
	 *
	 * @param player
	 * @param s
	 * @param alliedKingAttacked
	 * @param mlist
	 * @param quiesce
	 */
	public void genMoves(final int player, final State4 s, final boolean alliedKingAttacked,
						 final MoveList mlist, final boolean quiesce) {

		final FeatureSet f = quiesce ? null : this.f[player];

		final long allied = s.pieces[player];
		final long enemy = s.pieces[1 - player];
		final long agg = allied | enemy;

		final long enemyAttacks = genAttacks(1-player, s);

		final long quiesceMask = quiesce? enemy: ~0;
		final long promotionMask = Masks.pawnPromotionMask[player];

		if (alliedKingAttacked) {
			long kingMoves = Masks.getRawKingMoves(s.kings[player]) & ~allied;
			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		for (long queens = s.queens[player]; queens != 0; queens &= queens - 1) {
			recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
					Masks.getRawQueenMoves(agg, queens & -queens) & ~allied & quiesceMask,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		for (long rooks = s.rooks[player]; rooks != 0; rooks &= rooks - 1) {
			recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
					Masks.getRawRookMoves(agg, rooks & -rooks) & ~allied & quiesceMask,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		for (long knights = s.knights[player]; knights != 0; knights &= knights - 1) {
			recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
					Masks.getRawKnightMoves(knights & -knights) & ~allied & quiesceMask,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		for (long bishops = s.bishops[player]; bishops != 0; bishops &= bishops - 1) {
			recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
					Masks.getRawBishopMoves(agg, bishops & -bishops) & ~allied & quiesceMask,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		final long open = ~agg;
		final long enPassant = s.enPassante;
		for (long pawns = s.pawns[player]; pawns != 0; pawns &= pawns - 1) {
			final long p = pawns & -pawns;
			final long attacks;
			final long l1move;
			final long l2move;
			if (player == 0) {
				attacks = (((p << 7) & pawnLeftShiftMask) | ((p << 9) & pawnRightShiftMask)) & (enemy | enPassant);
				l1move = (p << 8) & open;
				l2move = ((((p & 0xFF00L) << 8) & open) << 8) & open;
			} else {
				attacks = (((p >>> 9) & pawnLeftShiftMask) | ((p >>> 7) & pawnRightShiftMask)) & (enemy | enPassant);
				l1move = (p >>> 8) & open;
				l2move = ((((p & 0xFF000000000000L) >>> 8) & open) >>> 8) & open;
			}

			long moves = attacks | l1move | l2move;
			recordMoves(player, State4.PIECE_TYPE_PAWN, p, moves & (quiesceMask | promotionMask), enemyAttacks, enemy, promotionMask, mlist, s, f);
		}

		if (!alliedKingAttacked) {
			long kingMoves = ((Masks.getRawKingMoves(s.kings[player]) & ~allied) | State4.getCastleMoves(player, s)) & quiesceMask;
			recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player],
					kingMoves,
					enemyAttacks, enemy, promotionMask, mlist, s, f);
		}
	}

	private static long genAttacks(final int player, final State4 s){
		return genAttacks(player, s.pieces[0]|s.pieces[1],
				s.pawns[player], s.knights[player], s.bishops[player],
				s.rooks[player], s.queens[player], s.kings[player]);
	}

	private static long genAttacks(final int player, final long agg,
								   final long pawns, final long knights, final long bishops,
								   final long rooks, final long queens, final long king) {

		final long kingAttacks = Masks.getRawKingMoves(king);

		long queenAttacks = 0;
		for (long q = queens; q != 0; q &= q - 1) {
			queenAttacks |= Masks.getRawQueenMoves(agg, q & -q);
		}

		long rookAttacks = 0;
		for (long r = rooks; r != 0; r &= r - 1) {
			rookAttacks |= Masks.getRawRookMoves(agg, r & -r);
		}

		long knightAttacks = 0;
		for (long k = knights; k != 0; k &= k - 1) {
			knightAttacks |= Masks.getRawKnightMoves(k & -k);
		}

		long bishopAttacks = 0;
		for (long b = bishops; b != 0; b &= b - 1) {
			bishopAttacks |= Masks.getRawBishopMoves(agg, b & -b);
		}

		final long pawnAttacks;
		if (player == 0) {
			pawnAttacks = (((pawns << 7) & pawnLeftShiftMask) | ((pawns << 9) & pawnRightShiftMask));
		} else {
			pawnAttacks = (((pawns >>> 9) & pawnLeftShiftMask) | ((pawns >>> 7) & pawnRightShiftMask));
		}

		return pawnAttacks | knightAttacks | bishopAttacks | rookAttacks | queenAttacks | kingAttacks;
	}
}
