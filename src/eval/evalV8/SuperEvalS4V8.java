package eval.evalV8;

import static eval.evalV8.EvalWeights.BONUS_BISHOP_PAIR;
import static eval.evalV8.EvalWeights.BONUS_MATERIAL_ADVANTAGE;
import static eval.evalV8.EvalWeights.BONUS_ROOK_HALF_OPEN_FILE;
import static eval.evalV8.EvalWeights.BONUS_ROOK_ON_7TH;
import static eval.evalV8.EvalWeights.BONUS_ROOK_OPEN_FILE;
import static eval.evalV8.EvalWeights.BONUS_TEMPO;
import static eval.evalV8.EvalWeights.DANGER_KING_ATTACKS;
import static eval.evalV8.EvalWeights.DANGER_PAWN_SHIELD_GAP;
import static eval.evalV8.EvalWeights.DANGER_STORMING_PAWN;
import static eval.evalV8.EvalWeights.GRAIN_SIZE;
import static eval.evalV8.EvalWeights.MOBILITY_BONUSES;
import static eval.evalV8.EvalWeights.PENALTY_DOUBLED_PAWNS;
import static eval.evalV8.EvalWeights.PENALTY_ISOLATED_PAWN;
import static eval.evalV8.EvalWeights.PENALTY_TRIPLED_PAWNS;
import static eval.evalV8.EvalWeights.kingDangerSquares;
import static eval.evalV8.EvalWeights.kingDangerValues;
import static eval.evalV8.EvalWeights.pieceSquareTables;
import static eval.evalV8.EvalWeights.pieceValues;
import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;
/*
 * Changes from v7
 * -now always checks for holes in the king's pawn shield and for storming enemy 
 * 	pawns, rather than just checking when the king is castled and on the back rank
 * -optimized the counting of attacked squares around the king
 * -took out unused passedPawnFiles table
 * 
 * 
 * 
 */
public class SuperEvalS4V8 implements Evaluator2<State4>
{
	private final static int[] zeroi = new int[8];

	private int[] positionScore = new int[2];
	private int[] materialScore = new int[2];
	private int[][] pawnOccupiedFiles = new int[2][8];

	public void initialize(State4 s)
	{
		System.arraycopy(zeroi, 0, positionScore, 0, 2);
		System.arraycopy(zeroi, 0, materialScore, 0, 2);
		System.arraycopy(zeroi, 0, pawnOccupiedFiles[0], 0, 8);
		System.arraycopy(zeroi, 0, pawnOccupiedFiles[1], 0, 8);

		initStartingValues(s, 0);
		initStartingValues(s, 1);
		Weight.updateWeightScaling(materialScore[0] + materialScore[1]);
		initPositionValues(s, 0);
		initPositionValues(s, 1);
		updateFileInfo(s);
	}

	private void initStartingValues(State4 s, int player)
	{
		long[] pieces = new long[7];
		pieces[State4.PIECE_TYPE_PAWN] = s.pawns[player];
		pieces[State4.PIECE_TYPE_KNIGHT] = s.knights[player];
		pieces[State4.PIECE_TYPE_BISHOP] = s.bishops[player];
		pieces[State4.PIECE_TYPE_ROOK] = s.rooks[player];
		pieces[State4.PIECE_TYPE_QUEEN] = s.queens[player];
		pieces[State4.PIECE_TYPE_KING] = s.kings[player];

		for (int i = State4.PIECE_TYPE_KING; i <= State4.PIECE_TYPE_PAWN; i++)
		{
			while (pieces[i] != 0)
			{
				pieces[i] &= pieces[i] - 1;
				materialScore[player] += pieceValues[i];
			}
		}
	}

	private void initPositionValues(State4 s, int player)
	{
		long[] pieces = new long[7];
		pieces[State4.PIECE_TYPE_PAWN] = s.pawns[player];
		pieces[State4.PIECE_TYPE_KNIGHT] = s.knights[player];
		pieces[State4.PIECE_TYPE_BISHOP] = s.bishops[player];
		pieces[State4.PIECE_TYPE_ROOK] = s.rooks[player];
		pieces[State4.PIECE_TYPE_QUEEN] = s.queens[player];
		pieces[State4.PIECE_TYPE_KING] = s.kings[player];

		for (int i = State4.PIECE_TYPE_KING; i <= State4.PIECE_TYPE_PAWN; i++)
		{
			while (pieces[i] != 0)
			{
				int index = BitUtil.lsbIndex(pieces[i]);
				pieces[i] &= pieces[i] - 1;
				positionScore[player] += pieceSquareTables[player][i][index].getScore();
			}
		}
	}

	@Override
	public int eval(State4 s, int player)
	{
		Weight.updateWeightScaling(materialScore[player] + materialScore[1 - player]);
		int score = BONUS_TEMPO.getScore();

		// ratio bonus should make trading when ahead attractive
		double materialRatio;
		if (materialScore[1 - player] > 0)
		{
			materialRatio = (double) materialScore[player] / materialScore[1 - player];
			score += (int) (10.0 * (materialRatio - 1.0) * BONUS_MATERIAL_ADVANTAGE);
		}

		score += evalPlayer(s, player);
		score -= evalPlayer(s, 1 - player);

		score = score / GRAIN_SIZE;

		score += lazyEval(s, player);

		return score;
	}

	public void traceEval(State4 s, int player)
	{
		initStartingValues(s, 0);
		initStartingValues(s, 1);
		Weight.updateWeightScaling(materialScore[0] + materialScore[1]);
		initPositionValues(s, 0);
		initPositionValues(s, 1);
		updateFileInfo(s);
		System.out.println("  Eval term     |White|Black|Total  ");
		System.out.println("  Material      |  " + materialScore[State4.WHITE] + "  |  " + materialScore[State4.BLACK] + "  |  " + (materialScore[State4.WHITE] - materialScore[State4.BLACK]));
		System.out.println("  Piece Sq      |  " + positionScore[State4.WHITE] + "  |  " + positionScore[State4.BLACK] + "  |  " + (positionScore[State4.WHITE] - positionScore[State4.BLACK]));

		double whiteRatio = 0;
		int whiteImbalance = 0;
		if (materialScore[State4.BLACK] > 0)
		{
			whiteRatio = (double) materialScore[State4.WHITE] / materialScore[State4.BLACK];
			whiteImbalance = (int) (10.0 * (whiteRatio - 1.0) * BONUS_MATERIAL_ADVANTAGE);
		}
		double blackRatio = 0;
		int blackImbalance = 0;
		if (materialScore[State4.WHITE] > 0)
		{
			blackRatio = (double) materialScore[State4.BLACK] / materialScore[State4.WHITE];
			blackImbalance = (int) (10.0 * (blackRatio - 1.0) * BONUS_MATERIAL_ADVANTAGE);
		}
		System.out.println(" Mat. Imbalance |  " + whiteImbalance + " , " + blackImbalance + " , " + (whiteImbalance - blackImbalance));
		System.out.println("    Mobility    |  " + scorePieceMobility(s, State4.WHITE) + " , " + scorePieceMobility(s, State4.BLACK) + " , "
				+ (scorePieceMobility(s, State4.WHITE) - scorePieceMobility(s, State4.BLACK)));
		System.out.println("     Pawns      |  " + getPawnScore(s, State4.WHITE) + " , " + getPawnScore(s, State4.BLACK) + " , " + (getPawnScore(s, State4.WHITE) - getPawnScore(s, State4.BLACK)));
		for (int i = 0; i < 8; i++)
		{
			if (pawnOccupiedFiles[0][i] > 1)
			{
				System.out.println("doubled pawns for white in column " + i);
			}
		}
		for (int i = 0; i < 8; i++)
		{
			if (pawnOccupiedFiles[1][i] > 1)
			{
				System.out.println("doubled pawns for black in column " + i);
			}
		}
		System.out.println("    Bishops     |  " + getBishopScore(s, State4.WHITE) + " , " + getBishopScore(s, State4.BLACK) + " , "
				+ (getBishopScore(s, State4.WHITE) - getBishopScore(s, State4.BLACK)));
		System.out.println("     Rooks      |  " + getRookScore(s, State4.WHITE) + " , " + getRookScore(s, State4.BLACK) + " , " + (getRookScore(s, State4.WHITE) - getRookScore(s, State4.BLACK)));
		System.out.println("     Kings      |  " + getKingScore(s, State4.WHITE) + " , " + getKingScore(s, State4.BLACK) + " , " + (getKingScore(s, State4.WHITE) - getKingScore(s, State4.BLACK)));
	}

	private double evalPlayer(State4 s, int player)
	{
		double score = 0;

		score += getPawnScore(s, player);
		score += getBishopScore(s, player);
		score += getRookScore(s, player);
		score += getKingScore(s, player);

		score += scorePieceMobility(s, player);

		return score;
	}

	public int lazyEval(State4 s, int player)
	{
		int score = 0;

		score += positionScore[player] / GRAIN_SIZE;
		score += materialScore[player];
		score -= positionScore[1 - player] / GRAIN_SIZE;
		score -= materialScore[1 - player];

		return score;
	}

	private void updateFileInfo(State4 s)
	{
		long whitePawns = s.pawns[State4.WHITE];
		long blackPawns = s.pawns[State4.BLACK];
		while (whitePawns != 0)
		{
			int col = BitUtil.lsbIndex(whitePawns) % 8;
			pawnOccupiedFiles[State4.WHITE][col]++;
			whitePawns &= whitePawns - 1;
		}
		while (blackPawns != 0)
		{
			int col = BitUtil.lsbIndex(blackPawns) % 8;
			pawnOccupiedFiles[State4.BLACK][col]++;
			blackPawns &= blackPawns - 1;
		}
	}

	private int scorePassedPawns(State4 s, int player)
	{
		int score = 0;
		long pawns = s.pawns[player];

		while (pawns != 0)
		{
			int index = BitUtil.lsbIndex(pawns);
			pawns &= pawns - 1;
			if ((Masks.passedPawnMasks[player][index] & s.pawns[1 - player]) == 0)
			{
				int r = player == State4.WHITE ? index / 8 : 7 - (index / 8);
				int rr = r * (r - 1);

				// bonus based on rank
				score += 20 * rr;

				if (rr > 0)
				{
					long blockSq = player == State4.WHITE ? 1L << index + 8 : 1L << index - 8;

					// further bonus if the pawn is free to advance
					if ((blockSq & (s.pieces[player] & s.pieces[1 - player])) == 0)
					{
						score += 10;
					}
				}
			}
		}

		return score;
	}

	private int scorePieceMobility(State4 s, int player)
	{
		int score = 0;

		long knights = s.knights[player];
		while (knights != 0)
		{
			long moves = State4.getKnightMoves(player, s.pieces, knights & -knights);
			knights &= knights - 1;
			score += MOBILITY_BONUSES[State4.PIECE_TYPE_KNIGHT][(int) BitUtil.getSetBits(moves)].getScore();
		}

		long bishops = s.bishops[player];
		while (bishops != 0)
		{
			long moves = State4.getBishopMoves(player, s.pieces, bishops & -bishops);
			bishops &= bishops - 1;
			score += MOBILITY_BONUSES[State4.PIECE_TYPE_BISHOP][(int) BitUtil.getSetBits(moves)].getScore();
		}

		long rooks = s.rooks[player];
		while (rooks != 0)
		{
			long moves = State4.getRookMoves(player, s.pieces, rooks & -rooks);
			rooks &= rooks - 1;
			score += MOBILITY_BONUSES[State4.PIECE_TYPE_ROOK][(int) BitUtil.getSetBits(moves)].getScore();
		}

		return score;
	}

	private int getPawnScore(State4 s, int player)
	{
		int score = 0;

		for (int i = 0; i < 8; i++)
		{
			if (pawnOccupiedFiles[player][i] != 0)
			{
				score -= ((i < 1 || pawnOccupiedFiles[player][i - 1] == 0) && (i > 6 || pawnOccupiedFiles[player][i + 1] == 0)) ? PENALTY_ISOLATED_PAWN.getScore() : 0;
			}
			if (pawnOccupiedFiles[player][i] == 2)
			{
				score -= PENALTY_DOUBLED_PAWNS.getScore();
			}
			else if (pawnOccupiedFiles[player][i] > 2)
			{
				score -= PENALTY_TRIPLED_PAWNS.getScore();
			}
		}

		score += scorePassedPawns(s, player);

		return score;
	}

	private int getBishopScore(State4 s, int player)
	{
		if (s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2)
		{
			return BONUS_BISHOP_PAIR.getScore();
		}
		return 0;
	}

	private int getRookScore(State4 s, int player)
	{
		int score = 0;
		int seventhFile = player == State4.WHITE ? 6 : 1;
		long rooks = s.rooks[player];

		while (rooks != 0)
		{
			int index = BitUtil.lsbIndex(rooks);
			score += (index / 8) == seventhFile ? BONUS_ROOK_ON_7TH.getScore() : 0;
			if (pawnOccupiedFiles[player][index % 8] == 0)
			{
				if (pawnOccupiedFiles[1 - player][index % 8] == 0)
				{
					score += BONUS_ROOK_OPEN_FILE.getScore();
				}
				else
				{
					score += BONUS_ROOK_HALF_OPEN_FILE.getScore();
				}
			}
			rooks &= (rooks - 1);
		}
		return score;
	}

	private int getKingScore(State4 s, int player)
	{
		int score = 0;

		// dont bother with king safety if enemys queen is dead
		if (s.pieceCounts[1 - player][State4.PIECE_TYPE_QUEEN] > 0)
		{
			score -= getKingDanger(s, player);
		}

		return score;
	}

	private int getKingDanger(State4 s, int player)
	{
		int kingSq = BitUtil.lsbIndex(s.kings[player]);
		long kingRing = Masks.kingMoves[kingSq];

		// add the bonus for the square the king is on
		int dangerIndex = kingDangerSquares[player][kingSq];

		int backRank = player == State4.WHITE ? 0 : 7;
		int kingRow = kingSq / 8;
		int kingCol = kingSq % 8;

		if (s.isCastled[player] == 1)
		{
			// evaluate the pawn shield for holes
			int shift = player == State4.WHITE ? 8 : -8;
			long shieldMask = 1L << (kingSq + shift);
			shieldMask |= 1L << (kingSq + (shift * 2));
			if ((shieldMask & s.pawns[player]) == 0)
			{
				// counted twice if pawn in front of the king is missing
				dangerIndex += DANGER_PAWN_SHIELD_GAP * 2;
			}
			if (kingCol > 0)
			{
				shieldMask = shieldMask >> 1;
				if ((shieldMask & s.pawns[player]) == 0)
					dangerIndex += DANGER_PAWN_SHIELD_GAP;
				shieldMask = shieldMask << 1;
			}
			if (kingCol < 7)
			{
				shieldMask = shieldMask << 1;
				if ((shieldMask & s.pawns[player]) == 0)
					dangerIndex += DANGER_PAWN_SHIELD_GAP;
			}
		}

		// check for approaching enemy pawns
		long enemyPawns = s.pawns[1 - player] & (Masks.colMask[kingCol]);
		if (kingCol > 0)
			enemyPawns &= s.pawns[1 - player] & (Masks.colMask[kingCol - 1]);
		else
			enemyPawns &= s.pawns[1 - player] & (Masks.colMask[kingCol + 2]);
		if (kingCol < 7)
			enemyPawns &= s.pawns[1 - player] & (Masks.colMask[kingCol + 1]);
		else
			enemyPawns &= s.pawns[1 - player] & (Masks.colMask[kingCol - 2]);

		while (enemyPawns != 0)
		{
			int rank = BitUtil.lsbIndex(enemyPawns) / 8;
			enemyPawns &= enemyPawns - 1;
			dangerIndex += DANGER_STORMING_PAWN[player][rank];
		}

		// check queen attacks
		if (s.pieceCounts[1 - player][State4.PIECE_TYPE_QUEEN] > 0)
		{
			long attacks = State4.getQueenMoves(1 - player, s.pieces, s.queens[1 - player]);
			long squaresHit = attacks & kingRing;

			while (squaresHit != 0)
			{
				dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_QUEEN];
				squaresHit &= squaresHit - 1;
			}
		}

		// check rook attacks
		if (s.pieceCounts[1 - player][State4.PIECE_TYPE_ROOK] > 0)
		{
			long enemyRooks = s.rooks[1 - player];
			long attacks = State4.getRookMoves(player, s.pieces, enemyRooks);
			long squaresHit = attacks & kingRing;

			dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_ROOK] * BitUtil.getSetBits(squaresHit);

			enemyRooks &= enemyRooks - 1;
			if (enemyRooks != 0)
			{
				attacks = State4.getRookMoves(player, s.pieces, enemyRooks);
				squaresHit = attacks & kingRing;

				dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_ROOK] * BitUtil.getSetBits(squaresHit);
			}
		}

		// check knight attacks
		if (s.pieceCounts[1 - player][State4.PIECE_TYPE_ROOK] > 0)
		{
			long enemyKnights = s.knights[1 - player];
			long attacks = State4.getKnightMoves(1 - player, s.pieces, enemyKnights);
			long squaresHit = attacks & kingRing;

			dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_KNIGHT] * BitUtil.getSetBits(squaresHit);

			enemyKnights &= enemyKnights - 1;

			if (enemyKnights != 0)
			{
				attacks = State4.getKnightMoves(1 - player, s.pieces, enemyKnights);
				squaresHit = attacks & kingRing;

				dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_KNIGHT] * BitUtil.getSetBits(squaresHit);
			}
		}

		// check bishop attacks
		if (s.pieceCounts[1 - player][State4.PIECE_TYPE_BISHOP] > 0)
		{
			long enemyBishops = s.bishops[1 - player];
			long attacks = State4.getBishopMoves(1 - player, s.pieces, enemyBishops);
			long squaresHit = attacks & kingRing;

			dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_BISHOP] * BitUtil.getSetBits(squaresHit);

			enemyBishops &= enemyBishops - 1;

			if (enemyBishops != 0)
			{
				attacks = State4.getBishopMoves(1 - player, s.pieces, enemyBishops);
				squaresHit = attacks & kingRing;

				dangerIndex += DANGER_KING_ATTACKS[State4.PIECE_TYPE_BISHOP] * BitUtil.getSetBits(squaresHit);
			}
		}

		return kingDangerValues[dangerIndex].getScore();
	}

	public void processMove(long encoding)
	{
		int pieceType = MoveEncoder.getMovePieceType(encoding);
		int player = MoveEncoder.getPlayer(encoding);
		int takenType = MoveEncoder.getTakenType(encoding);
		int fromSq = MoveEncoder.getPos1(encoding);
		int toSq = MoveEncoder.getPos2(encoding);

		if (takenType != State4.PIECE_TYPE_EMPTY)
		{
			positionScore[1 - player] -= pieceSquareTables[1 - player][takenType][toSq].getScore();
			materialScore[1 - player] -= pieceValues[takenType];
			if (pieceType == State4.PIECE_TYPE_PAWN)
			{
				pawnOccupiedFiles[player][fromSq % 8]--;
				pawnOccupiedFiles[player][toSq % 8]++;
			}
			if (takenType == State4.PIECE_TYPE_PAWN)
			{
				pawnOccupiedFiles[1 - player][toSq % 8]--;
			}
		}
		if (MoveEncoder.isEnPassanteTake(encoding) != 0)
		{
			long moveMask = 1L << toSq;
			final long takePos = player == 0 ? moveMask >>> 8 : moveMask << 8;

			materialScore[1 - player] -= pieceValues[State4.PIECE_TYPE_PAWN];

			positionScore[1 - player] -= pieceSquareTables[1 - player][State4.PIECE_TYPE_PAWN][BitUtil.lsbIndex(takePos)].getScore();
			positionScore[player] -= pieceSquareTables[player][pieceType][fromSq].getScore();

			pawnOccupiedFiles[player][fromSq % 8]--;
			pawnOccupiedFiles[player][toSq % 8]++;
			pawnOccupiedFiles[1 - player][BitUtil.lsbIndex(takePos) % 8]--;
		}
		else if (MoveEncoder.isPawnPromoted(encoding))
		{
			materialScore[player] -= pieceValues[State4.PIECE_TYPE_PAWN];
			materialScore[player] += pieceValues[State4.PIECE_TYPE_QUEEN];
			positionScore[player] -= pieceSquareTables[player][State4.PIECE_TYPE_PAWN][fromSq].getScore();

			pawnOccupiedFiles[player][fromSq % 8]--;
		}
		else
		{
			positionScore[player] -= pieceSquareTables[player][pieceType][fromSq].getScore();
		}

		Weight.updateWeightScaling(materialScore[player] + materialScore[1 - player]);

		if (MoveEncoder.isEnPassanteTake(encoding) != 0)
		{
			positionScore[player] += pieceSquareTables[player][pieceType][toSq].getScore();
		}
		else if (MoveEncoder.isPawnPromoted(encoding))
		{
			positionScore[player] += pieceSquareTables[player][State4.PIECE_TYPE_QUEEN][toSq].getScore();
		}
		else
		{
			positionScore[player] += pieceSquareTables[player][pieceType][toSq].getScore();
		}
	}

	public void undoMove(long encoding)
	{
		int pieceType = MoveEncoder.getMovePieceType(encoding);
		int player = MoveEncoder.getPlayer(encoding);
		int takenType = MoveEncoder.getTakenType(encoding);
		int fromSq = MoveEncoder.getPos1(encoding);
		int toSq = MoveEncoder.getPos2(encoding);

		if (takenType != State4.PIECE_TYPE_EMPTY)
		{
			materialScore[1 - player] += pieceValues[takenType];

			if (pieceType == State4.PIECE_TYPE_PAWN)
			{
				pawnOccupiedFiles[player][fromSq % 8]++;
				pawnOccupiedFiles[player][toSq % 8]--;
			}
			if (takenType == State4.PIECE_TYPE_PAWN)
			{
				pawnOccupiedFiles[1 - player][toSq % 8]++;
			}
		}
		if (MoveEncoder.isEnPassanteTake(encoding) != 0)
		{
			long moveMask = 1L << toSq;
			final long takePos = player == 0 ? moveMask >>> 8 : moveMask << 8;

			materialScore[1 - player] += pieceValues[State4.PIECE_TYPE_PAWN];
			positionScore[player] -= pieceSquareTables[player][pieceType][toSq].getScore();

			pawnOccupiedFiles[player][fromSq % 8]++;
			pawnOccupiedFiles[player][toSq % 8]--;
			pawnOccupiedFiles[1 - player][BitUtil.lsbIndex(takePos) % 8]++;
		}
		else if (MoveEncoder.isPawnPromoted(encoding))
		{
			materialScore[player] += pieceValues[State4.PIECE_TYPE_PAWN];
			materialScore[player] -= pieceValues[State4.PIECE_TYPE_QUEEN];
			positionScore[player] -= pieceSquareTables[player][State4.PIECE_TYPE_QUEEN][toSq].getScore();

			pawnOccupiedFiles[player][fromSq % 8]++;
		}
		else
		{
			positionScore[player] -= pieceSquareTables[player][pieceType][toSq].getScore();
		}

		Weight.updateWeightScaling(materialScore[player] + materialScore[1 - player]);

		if (takenType != State4.PIECE_TYPE_EMPTY)
		{
			positionScore[1 - player] += pieceSquareTables[1 - player][takenType][toSq].getScore();
		}
		if (MoveEncoder.isEnPassanteTake(encoding) != 0)
		{
			long moveMask = 1L << toSq;
			final long takePos = player == 0 ? moveMask >>> 8 : moveMask << 8;
			positionScore[1 - player] += pieceSquareTables[1 - player][State4.PIECE_TYPE_PAWN][BitUtil.lsbIndex(takePos)].getScore();
			positionScore[player] += pieceSquareTables[player][pieceType][fromSq].getScore();
		}
		else if (MoveEncoder.isPawnPromoted(encoding))
		{
			positionScore[player] += pieceSquareTables[player][State4.PIECE_TYPE_PAWN][fromSq].getScore();
		}
		else
		{
			positionScore[player] += pieceSquareTables[player][pieceType][fromSq].getScore();
		}
	}
}
