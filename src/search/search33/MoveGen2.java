package search.search33;

import state4.BitUtil;
import state4.Masks;
import state4.State4;

final class MoveGen2 {

	private final static long pawnLeftShiftMask = Masks.colMaskExc[7];
	private final static long pawnRightShiftMask = Masks.colMaskExc[0];
	
	/** record moves as blocks*/
	private static int recordMoves(final int player, final int pieceMovingType, final long pieceMask,
			final long moves, final long enemyPawnAttacks, final long enemyPieces, final long upTakeMask, final MoveSet[] mset,
			final int msetIndex, final State4 s, final boolean quiesce){
		
		int w = msetIndex;
		
		if(pieceMask != 0 && moves != 0){
			final long piece = pieceMask&-pieceMask;

			//final long moveCount = BitUtil.getSetBits(moves);
			//long m = moves;
			for(long m = moves; m != 0; m &= m-1){
				final long move = m&-m;
				if(!quiesce || (move & enemyPieces) != 0){
					final MoveSet temp = mset[w++];
					temp.piece = piece;
					temp.moves = move;
					
					final boolean upTake = (move & enemyPieces & upTakeMask) != 0;
					final boolean downTake = (move & enemyPieces & ~upTakeMask) != 0;
					final boolean goodNonTake = (move & ~enemyPawnAttacks) != 0;
					final boolean badNonTake = (move & enemyPawnAttacks) != 0;
					
					final int rank;
					if(upTake){
						rank = 3;
					} else if(downTake){
						rank = 4;
					} else if(goodNonTake){
						rank = 5;
					} else{
						rank = 6;
					}
					temp.rank = rank;
				}
			}
		}
		
		return w;
	}
	
	/** genereates moves, returns length after move generation*/
	public static int genMoves(final int player, final State4 s, final boolean alliedKingAttacked,
			final MoveSet[] mset, final int msetInitialIndex, final boolean quiece){
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, s.pawns[1-player]) & ~s.pieces[1-player];
		
		final long allied = s.pieces[player];
		final long enemy = s.pieces[1-player];
		//final long enemyKing = s.kings[1-player];
		final long agg = allied | enemy;
		int w = msetInitialIndex;
		
		final long kingUpTakes = s.pieces[1-player];
		if(alliedKingAttacked){
			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player]);
			w = recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves,
					enemyPawnAttacks, enemy, kingUpTakes, mset, w, s, false);
		}
		
		final long queenUpTakes = s.queens[1-player];
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			w = recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
					Masks.getRawQueenMoves(agg, queens&-queens) & ~allied,
					enemyPawnAttacks, enemy, queenUpTakes, mset, w, s, quiece);
		}

		final long rookUpTakes = s.rooks[1-player] | queenUpTakes;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			w = recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
					Masks.getRawRookMoves(agg, rooks&-rooks) & ~allied,
					enemyPawnAttacks, enemy, rookUpTakes, mset, w, s, quiece);
		}
		
		final long minorPieceUpTakes = s.bishops[1-player] | s.knights[1-player] | rookUpTakes;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			w = recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
					Masks.getRawKnightMoves(knights&-knights) & ~allied,
					enemyPawnAttacks, enemy, minorPieceUpTakes, mset, w, s, quiece);
		}
		
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			w = recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
					Masks.getRawBishopMoves(agg, bishops&-bishops) & ~allied,
					enemyPawnAttacks, enemy, minorPieceUpTakes, mset, w, s, quiece);
		}

		
		final long open = ~agg;
		final long promotionMask = Masks.pawnPromotionMask[player];
		final long[] passedPawnMasks = Masks.passedPawnMasks[player];
		final long enemyPawns = s.pawns[1-player];
		for(long pawns = s.pawns[player]; pawns != 0; pawns &= pawns-1){
			final long p = pawns & -pawns;
			final long attacks;
			final long l1move;
			final long l2move;
			if(player == 0){
				attacks = (((p << 7) & pawnLeftShiftMask) | ((p << 9) & pawnRightShiftMask)) & enemy;
				l1move = (p << 8) & open;
				l2move = ((((p & 0xFF00L) << 8) & open) << 8) & open;
			} else{
				attacks = (((p >>> 9) & pawnLeftShiftMask) | ((p >>> 7) & pawnRightShiftMask)) & enemy;
				l1move = (p >>> 8) & open;
				l2move = ((((p & 0xFF000000000000L) >>> 8) & open) >>> 8) & open;
			}
			
			for(long moves = attacks | l1move | l2move; moves != 0; moves &= moves-1){
				final long m = moves & -moves;
				final boolean take = (m & enemy) != 0;
				final boolean promote = (m & promotionMask) != 0;
				
				if(!quiece || take || promote){
					final MoveSet temp = mset[w++];
					temp.piece = p;
					temp.moves = m;
					
					
					final int rank;
					if(!take && !promote){
						rank = 5;
					} else if(!promote && take){
						rank = 3;
					} else if(promote && !take){
						rank = 2;
					} else{ //promote && take
						rank = 1;
					}
					temp.rank = rank;
					temp.promotionType = State4.PROMOTE_QUEEN;
					
					if(promote){
						final MoveSet temp2 = mset[w++];
						temp2.piece = p;
						temp2.moves = m;
						
						final int rank2 = take? 8: 9;
						temp2.rank = rank2;
						temp2.promotionType = State4.PROMOTE_KNIGHT;
					}
				}
			}
		}

		if(!alliedKingAttacked){
			long kingMoves = State4.getKingMoves(player, s.pieces, s.kings[player])|State4.getCastleMoves(player, s);
			w = recordMoves(player, State4.PIECE_TYPE_KING, s.kings[player], kingMoves,
					enemyPawnAttacks, enemy, kingUpTakes, mset, w, s, quiece);
		}

		return w;
	}
}
