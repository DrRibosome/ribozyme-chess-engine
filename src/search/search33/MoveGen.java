package search.search33;

import state4.Masks;
import state4.State4;

final class MoveGen {
	final static class MoveSet{
		long piece;
		long moves;
		int rank;
		
		void fill(final long piece, final long moves, final int rank){
			this.piece = piece;
			this.moves = moves;
			this.rank = rank;
			if(moves == 0) this.rank = 99999;
		}
		
		private void load(final MoveSet mset){
			this.piece = mset.piece;
			this.moves = mset.moves;
			this.rank = mset.rank;
		}
	}
	
	final static class MoveList{
		final MoveSet[] mset;
		int length;
		
		MoveList(int size){
			mset = new MoveSet[size];
			for(int a = 0; a < size; a++){
				mset[a] = new MoveSet();
			}
		}
		
		void clear(){
			length = 0;
		}
		
		void add(final MoveSet[] mset, final int length){
			for(int a = 0; a < length; a++){
				this.mset[this.length+a].load(mset[a]);
			}
			this.length += length;
		}
	}
	
	private static void isort(final MoveSet[] mset, final int length){
		for(int i = 1; i < length; i++){
			for(int a = i; a > 0 && mset[a-1].rank > mset[a].rank; a--){
				final MoveSet temp = mset[a];
				mset[a] = mset[a-1];
				mset[a-1] = temp;
			}
		}
	}
	
	public static void genMoves(final int player, final State4 s, final MoveList list,
			final boolean kingAttacked, final boolean quiesce){
		
		assert list != null;
		
		final long king = s.kings[player];
		final long queens = s.queens[player];
		final long rooks = s.rooks[player];
		final long knights = s.knights[player];
		final long bishops = s.bishops[player];
		final long pawns = s.pawns[player];
		final long allied = s.pieces[player];
		
		final long enemyQueens = s.queens[1-player];
		final long enemyRooks = s.rooks[1-player];
		final long enemyKnights = s.knights[1-player];
		final long enemyBishops = s.bishops[1-player];
		final long enemyPawns = s.pawns[1-player];
		final long enemies = s.pieces[1-player];
		final long enemyMinorPieces = enemyKnights | enemyBishops;
		
		final long agg = allied | enemies;
		
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, enemyPawns) & ~enemies;
		
		final MoveSet[] mset = list.mset;
		
		int w = list.length;
		if(kingAttacked){
			final long kingMoves = Masks.getRawKingMoves(king) & ~allied;
			mset[w++].fill(king, kingMoves & enemies, 0);
			if(!quiesce) mset[w++].fill(king, kingMoves & ~enemies, 1);
		}
		
		for(long temp = queens; temp != 0; temp &= temp-1){
			final long q = temp & -temp;
			final long moves = Masks.getRawQueenMoves(agg, q) & ~allied;
			mset[w++].fill(q, moves & enemyQueens, 9);
			mset[w++].fill(q, moves & enemyRooks, 5);
			mset[w++].fill(q, moves & enemyMinorPieces, 12);
			mset[w++].fill(q, moves & enemyPawns, 8);
			if(!quiesce){
				mset[w++].fill(q, moves & ~enemies & ~enemyPawnAttacks, 14);
				mset[w++].fill(q, moves & ~enemies & enemyPawnAttacks, 15);
			}
		}
		
		for(long temp = bishops; temp != 0; temp &= temp-1){
			final long b = temp & -temp;
			final long moves = Masks.getRawBishopMoves(agg, b) & ~allied;
			mset[w++].fill(b, moves & enemyQueens, 3);
			mset[w++].fill(b, moves & enemyRooks, 4);
			mset[w++].fill(b, moves & enemyMinorPieces, 9);
			mset[w++].fill(b, moves & enemyPawns, 8);
			if(!quiesce){
				mset[w++].fill(b, moves & ~enemies & ~enemyPawnAttacks, 14);
				mset[w++].fill(b, moves & ~enemies & enemyPawnAttacks, 15);
			}
		}
		
		for(long temp = knights; temp != 0; temp &= temp-1){
			final long k = temp & -temp;
			final long moves = Masks.getRawKnightMoves(k) & ~allied;
			mset[w++].fill(k, moves & enemyQueens, 3);
			mset[w++].fill(k, moves & enemyRooks, 4);
			mset[w++].fill(k, moves & enemyMinorPieces, 9);
			mset[w++].fill(k, moves & enemyPawns, 8);
			if(!quiesce){
				mset[w++].fill(k, moves & ~enemies & ~enemyPawnAttacks, 14);
				mset[w++].fill(k, moves & ~enemies & enemyPawnAttacks, 15);
			}
		}
		
		for(long temp = rooks; temp != 0; temp &= temp-1){
			final long r = temp & -temp;
			final long moves = Masks.getRawRookMoves(agg, r) & ~allied;
			mset[w++].fill(r, moves & enemyQueens, 3);
			mset[w++].fill(r, moves & enemyRooks, 9);
			mset[w++].fill(r, moves & enemyMinorPieces, 8);
			mset[w++].fill(r, moves & enemyPawns, 7);
			if(!quiesce){
				mset[w++].fill(r, moves & ~enemies & ~enemyPawnAttacks, 14);
				mset[w++].fill(r, moves & ~enemies & enemyPawnAttacks, 15);
			}
		}
		
		final long pawnEnemies = enemies | s.enPassante;
		final long promotionMask = Masks.pawnPromotionMask[player];
		for(long temp = pawns; temp != 0; temp &= temp-1){
			final long p = temp & -temp;
			final long moves;
			if(player == 0){
				final long l1 = (p << 8) & ~agg;
				final long l2 = ((((p & 0xFF00L) << 8) & ~agg) << 8) & ~agg;
				final long takes = ((p<<7) | (p<<9)) & pawnEnemies;
				moves = l1 | l2 | takes;
			} else{
				final long l1 = (p >>> 8) & ~agg;
				final long l2 = ((((p & 0xFF000000000000L) >>> 8) & ~agg) >>> 8) & ~agg;
				final long takes = ((p>>>7) | (p>>>9)) & pawnEnemies;
				moves = l1 | l2 | takes;
			}
			mset[w++].fill(p, moves & promotionMask, 2);
			final long nonPromote = moves & ~promotionMask;
			mset[w++].fill(p, nonPromote & enemyQueens, 3);
			mset[w++].fill(p, nonPromote & enemyRooks, 4);
			mset[w++].fill(p, nonPromote & enemyMinorPieces, 5);
			mset[w++].fill(p, nonPromote & enemyPawns, 9);
			if(!quiesce){
				mset[w++].fill(p, nonPromote & ~enemies, 14);
			}
		}
		
		if(!kingAttacked){
			final long kingMoves = (Masks.getRawKingMoves(king) & ~allied) | State4.getCastleMoves(player, s);
			mset[w++].fill(king, kingMoves & enemies, 15);
			if(!quiesce) mset[w++].fill(king, kingMoves & ~enemies, 14);
		}
		
		list.length = w;
		isort(mset, w);
	}
}
