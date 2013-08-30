package eval.e9;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator3;
import eval.PositionMasks;
import eval.ScoreEncoder;
import eval.e9.pawnEval.PawnEval;

public final class E9v3 implements Evaluator3{
	
	//evaluation stage flags, used to denote which eval stage is complete
	private final static int stage1Flag = 1 << 0;
	private final static int stage2Flag = 1 << 1;
	private final static int stage3Flag = 1 << 2;
	private final static int evalCompleteMask = stage1Flag | stage2Flag | stage3Flag;
	
	/**
	 * gives bonus multiplier to the value of sliding pieces
	 * mobilitiy scores based on how cluttered the board is
	 * <p>
	 * sliding pieces with high movement on a clutterd board
	 * are more valuable
	 * <p>
	 * indexed [num-pawn-attacked-squares]
	 */
	private final static double[] clutterIndex; 
	
	private final static int[] kingDangerTable;
	
	private final static int[] materialWeights = new int[7];
	
	private final static int tempoWeight = S(14, 5);
	private final static int bishopPairWeight = S(10, 42);

	private final static int[][] mobilityWeights = new int[7][];

	private final static int[] zeroi = new int[2];
	
	private final int[] materialScore = new int[2];
	
	/** margin for scaling scores from midgame to endgame
	 * <p> calculated by difference between midgame and endgame material*/
	private final int scaleMargin;
	private final int endMaterial;
	
	/** stores score for non-pawn material*/
	private final int[] nonPawnMaterial = new int[2];
	/** stores attack mask for all pieces for each player, indexed [player][piece-type]*/
	private final long[] attackMask = new long[2];
	
	//cached values
	/** stores total king distance from allied pawns*/
	private final int[] kingPawnDist = new int[2];
	private final PawnHash pawnHash;
	final PawnHashEntry filler = new PawnHashEntry();

	static{
		//clutterIndex calculated by linear interpolation
		clutterIndex = new double[64];
		final double start = .8;
		final double end = 1.2;
		final double diff = end-start;
		for(int a = 0; a < 64; a++){
			clutterIndex[a] = start + diff*(a/63.);
		}
		
		kingDangerTable = new int[128];
		final int maxSlope = 30;
		final int maxDanger = 1280;
		for(int x = 0, i = 0; i < kingDangerTable.length; i++){
			x = Math.min(maxDanger, Math.min((int)(i*i*.4), x + maxSlope));
			kingDangerTable[i] = S(-x, 0);
		}
		
		materialWeights[State4.PIECE_TYPE_QUEEN] = 900;
		materialWeights[State4.PIECE_TYPE_ROOK] = 470;
		materialWeights[State4.PIECE_TYPE_BISHOP] = 306;
		materialWeights[State4.PIECE_TYPE_KNIGHT] = 301;
		materialWeights[State4.PIECE_TYPE_PAWN] = 100;

		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new int[]{
				S(-19,-49), S(-13,-40), S(-6,-27), S(-1,0), S(7,2),
				S(12,10), S(14,28), S(16,44), S(17,48)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new int[]{
				S(-13,-30), S(-6,-20), S(1,-18), S(7,-10), S(15,-1),
				S(24,8), S(28,14), S(24,18), S(30,20), S(34,23),
				S(38,25), S(43,31), S(49,32), S(55,37), S(55,38), S(55,38)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new int[]{
				S(-10,-69), S(-7,-47), S(-4,-43), S(-1,-10), S(2,13), S(5,26),
				S(7,35), S(10,43), S(11,50), S(12,56), S(12,60), S(13,63),
				S(14,66), S(15,69), S(15,74), S(17,74)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new int[]{
				S(-6,-69), S(-4,-49), S(-2,-45), S(-2,-28), S(-1,-9), S(0,10),
				S(1,15), S(2,20), S(4,25), S(5,30), S(6,30), S(7,30), S(8,30),
				S(8,30), S(9,30), S(10,35), S(12,35), S(14,35), S(15,35), S(15,35),
				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35),
				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35)
		};
	}
	
	public E9v3(){
		this(16);
	}
	
	public E9v3(int pawnHashSize){
		int startMaterial = (
				  materialWeights[State4.PIECE_TYPE_PAWN]*8
				+ materialWeights[State4.PIECE_TYPE_KNIGHT]*2
				+ materialWeights[State4.PIECE_TYPE_BISHOP]*2
				+ materialWeights[State4.PIECE_TYPE_ROOK]*2
				+ materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		endMaterial = (
				  materialWeights[State4.PIECE_TYPE_ROOK]
				+ materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		
		scaleMargin = scaleMargin(startMaterial, endMaterial);
		
		pawnHash = new PawnHash(pawnHashSize, 16);
	}
	
	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return Weight.encode(start, end);
	}
	
	/** build a constant, non-scaling weight*/
	private static int S(final int v){
		return Weight.encode(v);
	}
	
	/**  calculates the scale margin to use in {@link #getScale(int, int, int)}*/
	private static int scaleMargin(final int startMaterial, final int endMaterial){
		return endMaterial-startMaterial;
	}
	
	/** gets the interpolatino factor for the weight*/
	private static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
		return min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
	}
	
	@Override
	public int eval(final int player, final State4 s) {
		return refine(player, s, -90000, 90000, 0);
	}

	@Override
	public int eval(final int player, final State4 s, final int lowerBound, final int upperBound) {
		return refine(player, s, lowerBound, upperBound, 0);
	}

	@Override
	public int refine(final int player, final State4 s, final int lowerBound,
			final int upperBound, final int scoreEncoding) {
		
		int score = ScoreEncoder.getScore(scoreEncoding);
		int margin = ScoreEncoder.getMargin(scoreEncoding);
		int flags = ScoreEncoder.getFlags(scoreEncoding);
		
		if((flags != 0 && (score+margin <= lowerBound || score+margin >= upperBound)) ||
				(evalCompleteMask & flags) == evalCompleteMask){
			return scoreEncoding;
		}

		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = getScale(totalMaterialScore, endMaterial, scaleMargin);
		
		final int pawnType = State4.PIECE_TYPE_PAWN;
		final int pawnWeight = materialWeights[pawnType];
		nonPawnMaterial[0] = materialScore[0]-s.pieceCounts[0][pawnType]*pawnWeight;
		nonPawnMaterial[1] = materialScore[1]-s.pieceCounts[1][pawnType]*pawnWeight;
		
		final long alliedQueens = s.queens[player];
		final long enemyQueens = s.queens[1-player];
		final long queens = alliedQueens | enemyQueens;
		
		if(flags == 0){
			flags |= stage1Flag;
			
			//load hashed pawn values, if any
			final long pawnZkey = s.pawnZkey();
			final PawnHashEntry phEntry = pawnHash.get(pawnZkey);
			final PawnHashEntry loader;
			if(phEntry == null){
				filler.passedPawns = 0;
				filler.zkey = 0;
				loader = filler;
			} else{
				loader = phEntry;
			}
			
			//System.out.println("material score = "+(materialScore[player] - materialScore[1-player]));
			int stage1Score = S(materialScore[player] - materialScore[1-player]);
			stage1Score += tempoWeight;
			
			if(s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2){
				stage1Score += bishopPairWeight;
			}
			if(s.pieceCounts[1-player][State4.PIECE_TYPE_BISHOP] == 2){
				stage1Score += -bishopPairWeight;
			}
			
			stage1Score += PawnEval.scorePawns(player, s, loader, enemyQueens, nonPawnMaterial) -
					PawnEval.scorePawns(1-player, s, loader, alliedQueens, nonPawnMaterial);
			
			if(phEntry == null){ //store newly calculated pawn values
				loader.zkey = pawnZkey;
				pawnHash.put(pawnZkey, loader);
			}
			
			final int stage1MarginLower; //margin for a lower cutoff
			final int stage1MarginUpper; //margin for an upper cutoff
			if(alliedQueens != 0 && enemyQueens != 0){
				//both sides have queen, apply even margin
				stage1MarginLower = 82; //margin scores taken from profiled mean score diff, 1.7 std
				stage1MarginUpper = -76;
			} else if(alliedQueens != 0){
				//score will be higher because allied queen, no enemy queen
				stage1MarginLower = 120;
				stage1MarginUpper = -96;
			} else if(enemyQueens != 0){
				//score will be lower because enemy queen, no allied queen
				stage1MarginLower = 92;
				stage1MarginUpper = -128;
			} else{
				stage1MarginLower = 142;
				stage1MarginUpper = -141;
			}
			
			score = Weight.interpolate(stage1Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage1Score)*.1), 0), scale);
			
			if(score+stage1MarginLower <= lowerBound){
				return ScoreEncoder.encode(score, stage1MarginLower, flags);
			}
			if(score+stage1MarginUpper >= upperBound){
				return ScoreEncoder.encode(score, stage1MarginUpper, flags);
			}
		}
		
		if((flags & stage2Flag) == 0){
			flags |= stage2Flag;

			final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
			final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
			final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
			final double clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
			
			final int stage2Score = scoreMobility(player, s, clutterMult, nonPawnMaterial, attackMask) -
					scoreMobility(1-player, s, clutterMult, nonPawnMaterial, attackMask);
			score += Weight.interpolate(stage2Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage2Score)*.1), 0), scale);
			if(queens == 0){
				flags |= stage3Flag;
				return ScoreEncoder.encode(score, 0, flags);
			} else{
				//stage 2 margin related to how much we expect the score to change
				//maximally due to king safety
				final int stage2MarginLower;
				final int stage2MarginUpper;
				if(alliedQueens != 0 && enemyQueens != 0){
					//both sides have queen, apply even margin
					stage2MarginLower = 3;
					stage2MarginUpper = -3;
				} else if(alliedQueens != 0){
					//score will be higher because allied queen, no enemy queen
					stage2MarginLower = 3;
					stage2MarginUpper = -3;
				}  else if(enemyQueens != 0){
					//score will be lower because enemy queen, no allied queen
					stage2MarginLower = 2;
					stage2MarginUpper = -4;
				} else{
					//both sides no queen, aplly even margin
					stage2MarginLower = 0;
					stage2MarginUpper = -0;
				}
				
				if(score+stage2MarginLower <= lowerBound){
					return ScoreEncoder.encode(score, stage2MarginLower, flags);
				} if(score+stage2MarginUpper >= upperBound){
					return ScoreEncoder.encode(score, stage2MarginUpper, flags);
				} else{
					flags |= stage3Flag;
					//margin cutoff failed, calculate king safety scores
					final int stage3Score = evalKingSafety(player, s, alliedQueens, enemyQueens);

					score += Weight.interpolate(stage3Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage3Score)*.1), 0), scale);
					
					return ScoreEncoder.encode(score, 0, flags);
				}
			}
		}
		
		if((flags & stage3Flag) == 0){
			assert queens != 0; //should be caugt by stage 2 eval if queens == 0
			
			flags |= stage3Flag;

			final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
			final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
			final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
			final double clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
			
			//recalculate attack masks
			scoreMobility(player, s, clutterMult, nonPawnMaterial, attackMask);
			scoreMobility(1-player, s, clutterMult, nonPawnMaterial, attackMask);

			final int stage3Score = evalKingSafety(player, s, alliedQueens, enemyQueens);

			score += Weight.interpolate(stage3Score, scale) + Weight.interpolate(S((int)(Weight.egScore(stage3Score)*.1), 0), scale);
			return ScoreEncoder.encode(score, 0, flags);
		}
		
		
		//evaluation should complete in one of the stages above
		assert false;
		return 0;
	}
	
	private int evalKingSafety(final int player, final State4 s, final long alliedQueens, final long enemyQueens){
		int score = 0;
		if(enemyQueens != 0){
			final long king = s.kings[player];
			final int kingIndex = BitUtil.lsbIndex(king);
			score += evalKingPressure3(kingIndex, player, s, attackMask[player]);
		}
		if(alliedQueens != 0){
			final long king = s.kings[1-player];
			final int kingIndex = BitUtil.lsbIndex(king);
			score -= evalKingPressure3(kingIndex, 1-player, s, attackMask[1-player]);
		}
		return score;
	}
	
	private static double min(final double d1, final double d2){
		return d1 < d2? d1: d2;
	}
	
	private static double max(final double d1, final double d2){
		return d1 > d2? d1: d2;
	}
	
	/**
	 * evaluates king pressure
	 * @param kingIndex index of the players king for whom pressure is to be evaluated
	 * @param player player owning the king for whom pressure is to be evaluated
	 * @param s
	 * @param alliedAttackMask attack mask for allied pieces, excluding the king;
	 * generated via {@link #scoreMobility(int, State4, double, int[], long[])}
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
	
	@Override
	public void makeMove(final long encoding) {
		update(encoding, 1);
	}

	@Override
	public void undoMove(final long encoding) {
		update(encoding, -1);
	}
	
	/** incrementally updates the score after a move, dir = undo? -1: 1*/
	private void update(final long encoding, final int dir){
		final int player = MoveEncoder.getPlayer(encoding);
		final int taken = MoveEncoder.getTakenType(encoding);
		if(taken != 0){
			materialScore[1-player] -= dir*materialWeights[taken];
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromotion(encoding)){
			materialScore[player] += dir*(materialWeights[State4.PIECE_TYPE_QUEEN]-
					materialWeights[State4.PIECE_TYPE_PAWN]);
		}
	}

	@Override
	public void initialize(State4 s) {
		System.arraycopy(zeroi, 0, materialScore, 0, 2);
		System.arraycopy(zeroi, 0, kingPawnDist, 0, 2);
		
		for(int a = 0; a < 2; a++){
			final int b = State4.PIECE_TYPE_BISHOP;
			materialScore[a] += s.pieceCounts[a][b] * materialWeights[b];
			
			final int n = State4.PIECE_TYPE_KNIGHT;
			materialScore[a] += s.pieceCounts[a][n] * materialWeights[n];
			
			final int q = State4.PIECE_TYPE_QUEEN;
			materialScore[a] += s.pieceCounts[a][q] * materialWeights[q];
			
			final int r = State4.PIECE_TYPE_ROOK;
			materialScore[a] += s.pieceCounts[a][r] * materialWeights[r];
			
			final int p = State4.PIECE_TYPE_PAWN;
			materialScore[a] += s.pieceCounts[a][p] * materialWeights[p];
		}
	}
	
	@Override
	public void reset(){}
	

	/** calculates mobility and danger to enemy king from mobility*/
	private static int scoreMobility(final int player, final State4 s,
			final double clutterMult, final int[] nonPawnMaterial, final long[] attackMask){
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
			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_BISHOP][count], clutterMult);
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
			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_KNIGHT][count], clutterMult);
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
			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_ROOK][moveCount], clutterMult);
			rookAttackMask |= rawMoves;
			
			final int rindex = BitUtil.lsbIndex(r);
			final int col = rindex%8;
			if(isHalfOpen(col, enemyPawns, allPieces & ~r)){ //tests file half open
				mobScore += S(6, 15);
				if(((allPieces & ~r) & Masks.colMask[col]) == 0){ //tests file open
					mobScore += S(6, 15);
				}
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
							//old, 80, 150
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
			mobScore += Weight.multWeight(mobilityWeights[State4.PIECE_TYPE_QUEEN][count], clutterMult);
			queenAttackMask |= rawMoves;
		}
		
		final long pawnAttackMask = Masks.getRawPawnAttacks(player, alliedPawns);
		attackMask[player] = bishopAttackMask | knightAttackMask | rookAttackMask | queenAttackMask | pawnAttackMask;
		

		//System.out.println(player+" pawn score = ("+mgScore(mobScore)+", "+egScore(mobScore)+")");
		return mobScore;
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
