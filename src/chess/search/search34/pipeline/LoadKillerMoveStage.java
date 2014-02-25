package chess.search.search34.pipeline;

import chess.search.search34.MoveGen;
import chess.search.search34.StackFrame;
import chess.state4.Masks;
import chess.state4.MoveEncoder;
import chess.state4.State4;

/** loads legal killer moves to pass on to a final pipeline stage component;
 * additionally, adds killer moves to move list */
public final class LoadKillerMoveStage implements MidStage {

	private final StackFrame[] stack;
	private final FinalStage next;

	public LoadKillerMoveStage(StackFrame[] stack, FinalStage next){
		this.stack = stack;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {
		final long l1killer1;
		final long l1killer2;
		final long l2killer1;
		final long l2killer2;

		final StackFrame.MoveList mlist = stack[c.stackIndex].mlist;

		if(c.stackIndex-1 >= 0 && !c.skipNullMove){
			final StackFrame prev = stack[c.stackIndex-1];
			final long l1killer1Temp = prev.killer[0];
			if(l1killer1Temp != 0 && isPseudoLegal(c.player, l1killer1Temp, s)){
				l1killer1 = l1killer1Temp & 0xFFFL;
				mlist.add(l1killer1, MoveGen.killerMoveRank);
			} else{
				l1killer1 = 0;
			}

			final long l1killer2Temp = prev.killer[1];
			if(l1killer2Temp != 0 && isPseudoLegal(c.player, l1killer2Temp, s)){
				l1killer2 = l1killer2Temp & 0xFFFL;
				mlist.add(l1killer2, MoveGen.killerMoveRank);
			} else{
				l1killer2 = 0;
			}

			if(c.stackIndex-3 >= 0){
				final StackFrame prev2 = stack[c.stackIndex-3];
				final long l2killer1Temp = prev2.killer[0];
				if(l2killer1Temp != 0 && isPseudoLegal(c.player, l2killer1Temp, s)){
					l2killer1 = l2killer1Temp & 0xFFFL;
					mlist.add(l2killer1, MoveGen.killerMoveRank);
				} else{
					l2killer1 = 0;
				}

				final long l2killer2Temp = prev2.killer[1];
				if(l2killer2Temp != 0 && isPseudoLegal(c.player, l2killer2Temp, s)){
					l2killer2 = l2killer2Temp & 0xFFFL;
					mlist.add(l2killer2, MoveGen.killerMoveRank);
				} else{
					l2killer2 = 0;
				}
			} else{
				l2killer1 = 0;
				l2killer2 = 0;
			}
		} else{
			l1killer1 = 0;
			l1killer2 = 0;
			l2killer1 = 0;
			l2killer2 = 0;
		}

		return next.eval(c, props, new KillerMoveSet(l1killer1, l1killer2, l2killer1, l2killer2), s);
	}

	/**
	 * checks too see if a move is legal, assumming we do not start in check,
	 * moving does not yield self check, we are not castling, and if moving a pawn
	 * we have chosen a non take move that could be legal if no piece is
	 * blocking the target square
	 *
	 * <p> used to check that killer moves are legal
	 * @param player
	 * @param encoding
	 * @param s
	 * @return
	 */
	private static boolean isPseudoLegal(final int player, final long encoding, final State4 s){
		final int pos1 = MoveEncoder.getPos1(encoding);
		final int pos2 = MoveEncoder.getPos2(encoding);
		final int takenType = MoveEncoder.getTakenType(encoding);
		final long p = 1L << pos1;
		final long m = 1L << pos2;
		final long[] pieces = s.pieces;
		final long agg = pieces[0] | pieces[1];
		final long allied = pieces[player];
		final long open = ~allied;

		if((allied & p) != 0 && takenType == s.mailbox[pos2]){
			final int type = s.mailbox[pos1];
			switch(type){
				case State4.PIECE_TYPE_BISHOP:
					final long tempBishopMoves = Masks.getRawBishopMoves(agg, p) & open;
					return (m & tempBishopMoves) != 0;
				case State4.PIECE_TYPE_KNIGHT:
					final long tempKnightMoves = Masks.getRawKnightMoves(p) & open;
					return (m & tempKnightMoves) != 0;
				case State4.PIECE_TYPE_QUEEN:
					final long tempQueenMoves = Masks.getRawQueenMoves(agg, p) & open;
					return (m & tempQueenMoves) != 0;
				case State4.PIECE_TYPE_ROOK:
					final long tempRookMoves = Masks.getRawRookMoves(agg, p) & open;
					return (m & tempRookMoves) != 0;
				case State4.PIECE_TYPE_KING:
					final long tempKingMoves = (Masks.getRawKingMoves(p) & open) | State4.getCastleMoves(player, s);
					return (m & tempKingMoves) != 0;
				case State4.PIECE_TYPE_PAWN:
					final long tempPawnMoves = Masks.getRawAggPawnMoves(player, agg, s.pawns[player]);
					return (m & tempPawnMoves) != 0;
			}
		}

		return false;
	}
}
