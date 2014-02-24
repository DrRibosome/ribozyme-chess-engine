package chess.eval.e9.pipeline;

import chess.eval.PositionMasks;
import chess.eval.e9.Weight;
import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.State4;

public class Stage3 implements LateStage {

	private final static int[] kingDangerTable;

	static {
		kingDangerTable = new int[128];
		final int maxSlope = 30;
		final int maxDanger = 1280;
		for(int x = 0, i = 0; i < kingDangerTable.length; i++){
			x = Math.min(maxDanger, Math.min((int)(i*i*.4), x + maxSlope));
			kingDangerTable[i] = S(-x, 0);
		}
	}

	private final int stage;

	public Stage3(int stage){
		this.stage = stage;
	}

	@Override
	public EvalResult eval(Team allied, Team enemy, AdvancedAttributes adv, EvalContext c, State4 s, int previousScore) {
		if(allied.queens != 0 || enemy.queens != 0){
			int stage3Score = 0;
			stage3Score += evalKingSafety(c.player, s, allied.queens, enemy.queens,
					adv.alliedMobility.attackMask, adv.enemyMobility.attackMask);

			int score = previousScore +
					(Weight.interpolate(stage3Score, c.scale) +
					Weight.interpolate(S((int)(Weight.egScore(stage3Score)*.1), 0), c.scale)) * 8 / 10;

			return new EvalResult(score, 0, 0, stage);
		} else{
			return new EvalResult(previousScore, 0, 0, stage);
		}
	}

	private int evalKingSafety(final int player, final State4 s, final long alliedQueens, final long enemyQueens,
							   long alliedAttackMask, long enemyAttackMask){
		int score = 0;
		if(enemyQueens != 0){
			final long king = s.kings[player];
			final int kingIndex = BitUtil.lsbIndex(king);
			score += evalKingPressure3(kingIndex, player, s, alliedAttackMask);
		}
		if(alliedQueens != 0){
			final long king = s.kings[1-player];
			final int kingIndex = BitUtil.lsbIndex(king);
			score -= evalKingPressure3(kingIndex, 1-player, s, enemyAttackMask);
		}
		return score;
	}

	/**
	 * evaluates king pressure
	 * @param kingIndex index of the players king for whom pressure is to be evaluated
	 * @param player player owning the king for whom pressure is to be evaluated
	 * @param s
	 * @param alliedAttackMask attack mask for allied pieces, excluding the king
	 * @return returns king pressure score
	 */
	private static int evalKingPressure3(final int kingIndex, final int player,
										 final State4 s, final long alliedAttackMask){

		final long king = 1L << kingIndex;
		final long allied = s.pieces[player];
		final long enemy = s.pieces[1-player];
		final long agg = allied | enemy;
		int index = 0;

		final long kingRing = Masks.getRawKingMoves(king);
		final long undefended = kingRing & ~alliedAttackMask;
		final long rookContactCheckMask = kingRing &
				~(PositionMasks.pawnAttacks[0][kingIndex] | PositionMasks.pawnAttacks[1][kingIndex]);
		final long bishopContactCheckMask = kingRing & ~rookContactCheckMask;

		final int enemyPlayer = 1-player;
		final long bishops = s.bishops[enemyPlayer];
		final long rooks = s.rooks[enemyPlayer];
		final long queens = s.queens[enemyPlayer];
		final long pawns = s.pawns[enemyPlayer];
		final long knights = s.knights[enemyPlayer];

		//process enemy queen attacks
		int supportedQueenAttacks = 0;
		for(long tempQueens = queens; tempQueens != 0; tempQueens &= tempQueens-1){
			final long q = tempQueens & -tempQueens;
			final long qAgg = agg & ~q;
			final long queenMoves = Masks.getRawQueenMoves(agg, q) & ~enemy & undefended;
			for(long temp = queenMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
				final long pos = temp & -temp;
				if((pos & undefended) != 0){
					final long aggPieces = qAgg | pos;
					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
					final long knightAttacks = Masks.getRawKnightMoves(pos);
					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);

					if((bishops & bishopAttacks) != 0 |
							(rooks & rookAttacks) != 0 |
							(knights & knightAttacks) != 0 |
							(pawns & pawnAttacks) != 0 |
							(queens & ~q & (bishopAttacks|rookAttacks)) != 0){
						supportedQueenAttacks++;
					}
				}
			}
		}
		//index += supportedQueenAttacks*16;
		index += supportedQueenAttacks*4;

		//process enemy rook attacks
		int supportedRookAttacks = 0;
		int supportedRookContactChecks = 0;
		for(long tempRooks = rooks; tempRooks != 0; tempRooks &= tempRooks-1){
			final long r = tempRooks & -tempRooks;
			final long rAgg = agg & ~r;
			final long rookMoves = Masks.getRawRookMoves(agg, r) & ~enemy & undefended;
			for(long temp = rookMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
				final long pos = temp & -temp;
				if((pos & undefended) != 0){
					final long aggPieces = rAgg | pos;
					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
					final long knightAttacks = Masks.getRawKnightMoves(pos);
					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);

					if((bishops & bishopAttacks) != 0 |
							(rooks & ~r & rookAttacks) != 0 |
							(knights & knightAttacks) != 0 |
							(pawns & pawnAttacks) != 0 |
							(queens & (bishopAttacks|rookAttacks)) != 0){
						if((pos & rookContactCheckMask) != 0) supportedRookContactChecks++;
						else supportedRookAttacks++;
					}
				}
			}
		}
		index += supportedRookAttacks*1;
		index += supportedRookContactChecks*2;

		//process enemy bishop attacks
		int supportedBishopAttacks = 0;
		int supportedBishopContactChecks = 0;
		for(long tempBishops = bishops; tempBishops != 0; tempBishops &= tempBishops-1){
			final long b = tempBishops & -tempBishops;
			final long bAgg = agg & ~b;
			final long bishopMoves = Masks.getRawBishopMoves(agg, b) & ~enemy & undefended;
			for(long temp = bishopMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
				final long pos = temp & -temp;
				if((pos & undefended) != 0){
					final long aggPieces = bAgg | pos;
					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
					final long knightAttacks = Masks.getRawKnightMoves(pos);
					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);

					if((bishops & ~b & bishopAttacks) != 0 |
							(rooks & rookAttacks) != 0 |
							(knights & knightAttacks) != 0 |
							(pawns & pawnAttacks) != 0 |
							(queens & (bishopAttacks|rookAttacks)) != 0){
						if((pos & bishopContactCheckMask) != 0) supportedBishopContactChecks++;
						else supportedBishopAttacks++;
					}
				}
			}
		}
		index += supportedBishopAttacks*1;
		index += supportedBishopContactChecks*2;

		//process enemy knight attacks
		int supportedKnightAttacks = 0;
		for(long tempKnights = knights; tempKnights != 0; tempKnights &= tempKnights-1){
			final long k = tempKnights & -tempKnights;
			final long kAgg = agg & ~k;
			final long knightMoves = Masks.getRawKnightMoves(k) & ~enemy & undefended;
			for(long temp = knightMoves & ~alliedAttackMask; temp != 0; temp &= temp-1){
				final long pos = temp & -temp;
				if((pos & undefended) != 0){
					final long aggPieces = kAgg | pos;
					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
					final long knightAttacks = Masks.getRawKnightMoves(pos);
					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);

					if((bishops & bishopAttacks) != 0 |
							(rooks & rookAttacks) != 0 |
							(knights & ~k & knightAttacks) != 0 |
							(pawns & pawnAttacks) != 0 |
							(queens & (bishopAttacks|rookAttacks)) != 0){
						supportedKnightAttacks++;
					}
				}
			}
		}
		index += supportedKnightAttacks*1;

		return kingDangerTable[index < 128? index: 127];
	}

	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return Weight.encode(start, end);
	}
}
