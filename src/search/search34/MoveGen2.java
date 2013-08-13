package search.search34;

import search.MoveSet;
import state4.BitUtil;
import state4.Masks;
import state4.State4;

final class MoveGen2 {

	public final static int tteMoveRank = -7;
	public final static int promoteTakeRank = -6;
	public final static int promoteRank = -5;
	public final static int upTakeRank = -4;
	public final static int downTakeRank = -3;
	/** rank set to the first of the non takes*/
	public final static int killerMoveRank = -2;
	
	private final static int maxWeight = 1 << 10;
	
	private final FeatureSet[] f;
	
	MoveGen2(){
		f = new FeatureSet[2];
		for(int a = 0; a < f.length; a++) f[a] = new FeatureSet();
	}
	
	private final static class FeatureSet{
		int pawnPromotionWeight;
		int passedPawnWeight;
		/** position weights, indexed [piece-type][start position][end position]*/
		final int[][][] posWeight = new int[7][64][64];
	}

	private final static long pawnLeftShiftMask = Masks.colMaskExc[7];
	private final static long pawnRightShiftMask = Masks.colMaskExc[0];
	
	private static int recordMoves(final int player, final int pieceMovingType, final long pieceMask,
			final long moves, final long enemyPawnAttacks, final long enemyPieces, final long upTakeMask, final MoveSet[] mset,
			final int msetIndex, final State4 s, final boolean quiesce, final FeatureSet f){
		
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
					//final boolean goodNonTake = (move & ~enemyPawnAttacks) != 0;
					//final boolean badNonTake = (move & enemyPawnAttacks) != 0;
					
					assert !quiesce || (upTake || downTake);
					
					final int rank;
					if(upTake){
						rank = upTakeRank;
					} else if (downTake){
						rank = downTakeRank;
					} else if(f == null){
						rank = downTakeRank;
					} else {
						rank = maxWeight - getMoveWeight(player, pieceMovingType, piece, move, f, s);
					}
					temp.rank = rank;
				}
			}
		}
		
		return w;
	}
	
	public void reset(){
		for(int a = 0; a < f.length; a++){
			f[a].passedPawnWeight = 0;
			f[a].pawnPromotionWeight = 0;
			for(int q = 0; q < 7; q++){
				for(int w = 0; w < 64; w++){
					for(int s = 0; s < 64; s++){
						f[a].posWeight[q][w][s] = 0;
					}
				}
			}
		}
	}
	
	public void alphaRaised(final int player, final int pieceType, final int startPos,
			final int movePos, final State4 s, final int depth){
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
		
		if(f.posWeight[pieceType][startPos][index] >= maxWeight ||
				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight){
			dampen(f);
		}
	}
	
	public void betaCutoff(final int player, final int pieceType, final int startPos,
			final int movePos, final State4 s, final int depth){
		final FeatureSet f = this.f[player];

		final int offset = (depth*depth) >>> 1;
		
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
		
		if(f.posWeight[pieceType][startPos][index] >= maxWeight ||
				f.passedPawnWeight >= maxWeight || f.pawnPromotionWeight >= maxWeight){
			dampen(f);
		}
	}
	
	public void dampen(){
		dampen(f[0]);
		dampen(f[1]);
	}
	
	/**
	 * Lowers all weights
	 * <p> Called when a weight gets too high. Helps the history heuristic
	 * stay in line with the game tree being searched
	 * @param f
	 */
	private void dampen(final FeatureSet f){
		f.passedPawnWeight >>>= 1;
		f.pawnPromotionWeight >>>= 1;
		for(int q = 1; q < 7; q++){
			for(int a = 0; a < 64; a++){
				for(int z = 0; z < 64; z++){
					f.posWeight[q][a][z] >>>= 1;
				}
			}
		}
	}
	
	private static int getMoveWeight(final int player, final int pieceType,
			final long piece, final long move, final FeatureSet f, final State4 s){
		
		int rank = 0;
		
		final int startIndex = BitUtil.lsbIndex(piece);
		final int index = BitUtil.lsbIndex(move);
		
		if(pieceType == State4.PIECE_TYPE_PAWN){
			final long ppMask = Masks.passedPawnMasks[player][index];
			if((ppMask & s.pawns[1-player]) == 0){
				rank += f.passedPawnWeight;
				
				final long pomotionMask = Masks.pawnPromotionMask[player];
				if((move & pomotionMask) != 0){
					rank += f.pawnPromotionWeight;
				}
			}
		}
		
		rank += f.posWeight[pieceType][startIndex][index];
		
		return rank;
	}
	
	/**
	 * genereates moves
	 * @param player
	 * @param s
	 * @param alliedKingAttacked
	 * @param mset
	 * @param msetInitialIndex initial index to start recording moves at
	 * @param quiesce
	 * @return returns length of move set array after move generation
	 */
	public int genMoves(final int player, final State4 s, final boolean alliedKingAttacked,
			final MoveSet[] mset, final int msetInitialIndex, final boolean quiesce, final int stackIndex){
		
		final FeatureSet f = quiesce? null: this.f[player];
		
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
					enemyPawnAttacks, enemy, kingUpTakes, mset, w, s, false, f);
		}
		
		final long queenUpTakes = s.queens[1-player];
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			w = recordMoves(player, State4.PIECE_TYPE_QUEEN, queens,
					Masks.getRawQueenMoves(agg, queens&-queens) & ~allied,
					enemyPawnAttacks, enemy, queenUpTakes, mset, w, s, quiesce, f);
		}

		final long rookUpTakes = s.rooks[1-player] | queenUpTakes;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			w = recordMoves(player, State4.PIECE_TYPE_ROOK, rooks,
					Masks.getRawRookMoves(agg, rooks&-rooks) & ~allied,
					enemyPawnAttacks, enemy, rookUpTakes, mset, w, s, quiesce, f);
		}
		
		final long minorPieceUpTakes = s.bishops[1-player] | s.knights[1-player] | rookUpTakes;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			w = recordMoves(player, State4.PIECE_TYPE_KNIGHT, knights,
					Masks.getRawKnightMoves(knights&-knights) & ~allied,
					enemyPawnAttacks, enemy, minorPieceUpTakes, mset, w, s, quiesce, f);
		}
		
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			w = recordMoves(player, State4.PIECE_TYPE_BISHOP, bishops,
					Masks.getRawBishopMoves(agg, bishops&-bishops) & ~allied,
					enemyPawnAttacks, enemy, minorPieceUpTakes, mset, w, s, quiesce, f);
		}

		
		final long open = ~agg;
		final long promotionMask = Masks.pawnPromotionMask[player];
		//final long[] passedPawnMasks = Masks.passedPawnMasks[player];
		//final long enemyPawns = s.pawns[1-player];
		final long enPassant = s.enPassante;
		for(long pawns = s.pawns[player]; pawns != 0; pawns &= pawns-1){
			final long p = pawns & -pawns;
			final long attacks;
			final long l1move;
			final long l2move;
			if(player == 0){
				attacks = (((p << 7) & pawnLeftShiftMask) | ((p << 9) & pawnRightShiftMask)) & (enemy | enPassant);
				l1move = (p << 8) & open;
				l2move = ((((p & 0xFF00L) << 8) & open) << 8) & open;
			} else{
				attacks = (((p >>> 9) & pawnLeftShiftMask) | ((p >>> 7) & pawnRightShiftMask)) & (enemy | enPassant);
				l1move = (p >>> 8) & open;
				l2move = ((((p & 0xFF000000000000L) >>> 8) & open) >>> 8) & open;
			}
			
			for(long moves = attacks | l1move | l2move; moves != 0; moves &= moves-1){
				final long m = moves & -moves;
				final boolean take = (m & (enemy|enPassant)) != 0;
				final boolean promote = (m & promotionMask) != 0;
				
				if(!quiesce || take || promote){
					final MoveSet temp = mset[w++];
					temp.piece = p;
					temp.moves = m;
					
					
					final int rank;
					if(take && promote){
						rank = promoteTakeRank;
					} else if(promote){
						rank = promoteRank;
					} else if(take){
						rank = upTakeRank;
					} else{
						rank = maxWeight - getMoveWeight(player, State4.PIECE_TYPE_PAWN, p, m, f, s);
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
					enemyPawnAttacks, enemy, kingUpTakes, mset, w, s, quiesce, f);
		}

		return w;
	}
}
