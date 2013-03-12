package eval.expEvalV2;

import state4.Masks;
import state4.State4;

public final class EvalUtil {
	/** gets attacks, including attacks that would take friendly pieces (ie, defend them)*/
	public static long getRawAttacks(final int player, final State4 s){
		long attacked = 0;
		final long[] pieceMasks = s.pieces;
		final long agg = s.pieces[0]|s.pieces[1];
		
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			attacked |= Masks.getRawQueenMoves(agg, queens);
		}
		
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			attacked |= Masks.getRawKnightMoves(knights);
		}
		
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			attacked |= Masks.getRawBishopMoves(agg, bishops);
		}
		
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			attacked |= Masks.getRawRookMoves(agg, rooks);
		}

		attacked |= Masks.getRawKingMoves(s.kings[player]);
		attacked |= Masks.getRawPawnAttacks(player, s.pawns[player]);

		attacked |= s.pieces[player];
		
		return attacked;
	}
}
