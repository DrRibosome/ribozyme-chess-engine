package eval.e7;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;
import eval.PositionMasks;

/** speed improvements in mobility*/
public final class E7v8 implements Evaluator2{
	
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
	
	private final static int[][] isolatedPawns = new int[][]{{
		S(-15,-10), S(-18,-15), S(-20,-19), S(-22,-20), S(-22,-20), S(-20,-19), S(-18,-15), S(-15,-10)},
		//{S(-6,-8), S(-8,-8), S(-12,-10), S(-14,-12), S(-14,-12), S(-12,-10), S(-8,-8), S(-6,-8)},
		//{S(-17, -20), S(-18, -18), S(-20, -23), S(-25, -25), S(-25, -25), S(-20, -23), S(-18, -18), S(-17, -20)},
		{S(-10, -14), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-10, -14)},
	};

	private final static int[] pawnChain = new int[]{
		S(13,0), S(15,0), S(18,1), S(22,5), S(22,5), S(18,1), S(15,0), S(13,0)
	};

	private final static int[][] doubledPawns = new int[][]{
		{S(-9,-18), S(-12,-19), S(-13,-19), S(-13,-19), S(-13,-19), S(-13,-19), S(-12,-19), S(-12,-18)},
		{S(-6,-13), S(-8,-16), S(-9,-17), S(-9,-17), S(-9,-17), S(-9,-17), S(-8,-16), S(-6,-13)},
	};

	private final static int[][] backwardPawns = new int[][]{
		{S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),},
		{S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),},
	};
	
	private final static int[][] pawnShelter = new int[][]{ //only need 7 indeces, pawn cant be on last row
			{0, 30, 20, 8, 2, 0, 0},
			{0, 75, 38, 20, 5, 0, 0},
			//{0, 61, 45, 17, 5, 0, 0},
			//{0, 141, 103, 39, 13, 0, 0},
	};
	
	private final static int[][] pawnStorm = new int[][]{ //indexed [type][distance]
			{-25, -20, -18, -14, -8, 0}, //no allied pawn
			{-20, -18, -14, -10, -6, 0}, //has allied pawn, enemy not blocked
			{-10, -8, -6, -4, -1, 0}, //enemy pawn blocked by allied pawn
	};

	private final static int[][] kingDangerSquares = {
			{
				2,  0,  2,  3,  3,  2,  0,  2,
				2,  2,  4,  8,  8,  4,  2,  2,
				7, 10, 12, 12, 12, 12, 10,  7,
				15, 15, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15,
				15, 15, 15, 15, 15, 15, 15, 15
			}, new int[64]
	};

	private final static boolean[] allFalse = new boolean[2];
	private final static boolean[] allTrue = new boolean[]{true, true};
	private final static int[] zeroi = new int[2];
	
	private final int[] materialScore = new int[2];
	
	private final int margin;
	private final int endMaterial;
	private final int granularity = 8;
	
	/** multiplier for active pieces on a cluttered board*/
	private double clutterMult;
	/** stores score for non-pawn material*/
	private final int[] nonPawnMaterial = new int[2];
	private final boolean[] kingMoved = new boolean[2];
	private final boolean[] pawnMoved = new boolean[2];
	/** stores attack mask for all pieces for each player, indexed [player][piece-type]*/
	private final long[] attackMask = new long[2];
	
	//cached values
	private final int[] pawnWallScore = new int[2];
	/** stores total king distance from allied pawns*/
	private final int[] kingPawnDist = new int[2];

	static{
		//linear interpolation
		clutterIndex = new double[64];
		final double start = .9;
		final double end = 1.1;
		final double diff = end-start;
		for(int a = 0; a < 64; a++){
			clutterIndex[a] = start + diff*(a/64.);
		}
		
		kingDangerTable = new int[128];
		final int maxSlope = 30;
		final int maxDanger = 1280;
		for(int x = 0, i = 0; i < kingDangerTable.length; i++){
			x = Math.min(maxDanger, Math.min((int)(i*i*.4), x + maxSlope));
			kingDangerTable[i] = S(-x, 0);
		}

		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
		
		materialWeights[State4.PIECE_TYPE_QUEEN] = 850;
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
				S(7,35), S(10,43), S(11,50), S(12,56), S(11,47), S(13,61),
				S(14,62), S(15,58), S(15,74), S(17,74)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new int[]{
				S(-6,-69), S(-4,-45), S(-2,-49), S(-2,-28), S(-1,-9), S(0,10),
				S(1,15), S(2,20), S(4,25), S(5,30), S(6,30), S(7,30), S(8,30),
				S(8,30), S(9,30), S(10,35), S(12,35), S(14,35), S(15,35), S(15,35),
				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35), S(15,35),
				S(15,35), S(15,35), S(15,35), S(15,35), S(15,35)
		};
	}
	
	public E7v8(){
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
		
		margin = margin(startMaterial, endMaterial);
	}
	
	private final static int weightValueMask = 0x7FFF;
	private final static int weightSignMask = 1<<15;
	/** interpolate a passed weight value, scale in [0,1]*/
	private static int interpolate(final int weight, final double scale){
		final int start = (weight & weightValueMask) - (weight & weightSignMask);
		final int shifted = weight >>> 16;
		final int end = (shifted & weightValueMask) - (shifted & weightSignMask);
		return (int)(start + (end-start)*scale);
	}
	
	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return (start&0xFFFF) + (end<<16);
	}
	
	/** build a constant, non-scaling weight*/
	private static int S(final int v){
		return S(v, v);
	}
	
	private static int egScore(final int weight){
		final int shifted = weight >>> 16;
		return (shifted & weightValueMask) - (shifted & weightSignMask);
	}
	
	/**  calculates the margin to use in {@link #getScale(int, int, int)}*/
	private static int margin(final int startMaterial, final int endMaterial){
		return endMaterial-startMaterial;
	}
	
	/** gets the interpolatino factor for the weight*/
	private static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
		return min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
	}

	@Override
	public int eval(State4 s, int player) {
		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = getScale(totalMaterialScore, endMaterial, margin);
		
		

		final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
		final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
		final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
		clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
		
		final int pawnType = State4.PIECE_TYPE_PAWN;
		final int pawnWeight = materialWeights[pawnType];
		nonPawnMaterial[0] = materialScore[0]-s.pieceCounts[0][pawnType]*pawnWeight; 
		nonPawnMaterial[1] = materialScore[1]-s.pieceCounts[1][pawnType]*pawnWeight;
		
		
		final int p1Weight = score(player, s);
		final int p1 = granulate(interpolate(p1Weight, scale), granularity);
		final int p1End = egScore(p1Weight);
		
		final int p2Weight = score(1-player, s);
		final int p2 = granulate(interpolate(p2Weight, scale), granularity);
		final int p2End = egScore(p2Weight);
		
		final int score = p1-p2;
		final int endgameBonus = S((int)(.1*(p1End-p2End)+.5), 0);
		
		System.arraycopy(allFalse, 0, kingMoved, 0, 2);
		System.arraycopy(allFalse, 0, pawnMoved, 0, 2);
		return score + interpolate(endgameBonus, scale) + interpolate(tempoWeight, scale);
	}

	@Override
	public int lazyEval(State4 state, int player) {
		return lazyScore(player)-lazyScore(1-player);
	}
	
	private int score(final int player, final State4 s){
		
		int score = S(materialScore[player]);
		score += scoreMobility(player, s, clutterMult, nonPawnMaterial, attackMask);
		score += scorePawns(player, s, nonPawnMaterial);
		
		if(s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2){
			score += bishopPairWeight;
		}
		
		if(s.queens[1-player] != 0){
			score += getKingDanger(player, s);
		}
		
		return score;
	}
	
	/** for grainSize a power of 2, returns passed score inside specified granularity,
	 * helps prevent hopping around to different PVs on low score differences*/
	private static int granulate(final int score, final int grainSize){
		return (score+grainSize>>1) & ~(grainSize-1);
	}
	
	private static int max(final int a1, final int a2){
		return a1 > a2? a1: a2;
	}
	
	private static int analyzePassedPawn(final int player, final long p, final State4 s, final int[] nonPawnMaterial){
		
		int passedPawnSore = 0;
		
		final int pawnIndex = BitUtil.lsbIndex(p);
		//agg.add(this.p.passedPawnRowWeight[player][index >>> 3]);s
		
		final int row = player == 0? pawnIndex>>>3: 7-(pawnIndex>>>3);
		final int pawnDist = 7-row; //distance of pawn from promotion square

		assert (Masks.passedPawnMasks[player][pawnIndex] & s.pawns[1-player]) == 0;
		
		//calculate king distance to promote square
		final int kingIndex = BitUtil.lsbIndex(s.kings[1-player]);
		final int kingXDist = Math.abs(kingIndex%8 - pawnIndex%8);
		final int promoteRow = 1-player == 0? 7: 0; 
		final int kingYDist = Math.abs((kingIndex>>>3) - promoteRow);
		final int enemyKingDist = kingXDist > kingYDist? kingXDist: kingYDist;
		
		//our pawn closer to promotion than enemy king
		if(pawnDist < enemyKingDist){
			final int diff = enemyKingDist-pawnDist;
			assert diff < 8;
			passedPawnSore += S(0, max(diff*diff, 15));
		}
		
		//pawn closer than enemy king and no material remaining
		if(pawnDist < enemyKingDist && nonPawnMaterial[1-player] == 0){
			passedPawnSore += S(500);
		}
		
		//checks for support by same color bishop
		//performs badly by several indications
		/*final int endIndex = index%8 + (player == 0? 56: 0);
		final int endColor = PositionMasks.squareColor(endIndex);
		final int enemyBishopSupport = -25;
		final long squareMask = PositionMasks.bishopSquareMask[endColor];
		if((s.bishops[1-player] & squareMask) != 0){ //allied supporting bishop
			agg.add(0, enemyBishopSupport/(pawnDist*pawnDist));
		}*/

		final int rr = row*(row-1);
		final int start = 18*rr/2;
		final int end = 10*(rr+row+1)/2;
		passedPawnSore += S(start, end);
		
		
		//checks for pawn advancement blocked
		final long nextPos = player == 0? p << 8: p >>> 8;
		final long allPieces = s.pieces[0]|s.pieces[1];
		if((nextPos & allPieces) != 0){ //pawn adancement blocked
			passedPawnSore += S(-start/6/pawnDist, -end/6/pawnDist);
			
			//slight bonus for causing a piece to keep blocking a pawn,
			//(w0,w1,d) = (111,134,126) without-with, depth=3
			if((nextPos & s.queens[1-player]) != 0) passedPawnSore += S(45/pawnDist);
			else if((nextPos & s.rooks[1-player]) != 0) passedPawnSore += S(35/pawnDist);
			else if((nextPos & s.bishops[1-player]) != 0) passedPawnSore += S(25/pawnDist);
			else if((nextPos & s.knights[1-player]) != 0) passedPawnSore += S(25/pawnDist);
		}
		
		//checks to see whether we have a non-pawn material disadvantage,
		//its very hard to keep a passed pawn when behind
		//final int nonPawnMaterialDiff = nonPawnMaterial[player]-nonPawnMaterial[1-player];
		final double npDisMult = max(min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
		passedPawnSore += multWeight(S(-start*2/3, -end*2/3), npDisMult);
		
		//passed pawn supported by rook bonus
		if((s.rooks[player] & PositionMasks.opposedPawnMask[1-player][pawnIndex]) != 0){
			//agg.add(10/pawnDist);
		}
		
		final boolean chain = (PositionMasks.pawnChainMask[player][pawnIndex] & s.pawns[player]) != 0;
		if(chain){
			passedPawnSore += S(35/pawnDist);
		}
		
		return passedPawnSore;
	}
	
	private static double min(final double d1, final double d2){
		return d1 < d2? d1: d2;
	}
	
	private static double max(final double d1, final double d2){
		return d1 > d2? d1: d2;
	}
	
	private static int scorePawns(final int player, final State4 s, final int[] nonPawnMaterial){
		
		int pawnScore = 0;
		
		final long enemyPawns = s.pawns[1-player];
		final long alliedPawns = s.pawns[player];
		final long all = alliedPawns | enemyPawns;
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		
		final boolean nonPawnDisadvantage = nonPawnMaterial[player]-nonPawnMaterial[1-player]+20 < 0;
		final double npDisMult = max(min(nonPawnMaterial[1-player]-nonPawnMaterial[player], 300), 0)/300.;
		
		int kingDistAgg = 0; //king distance aggregator
		for(long pawns = alliedPawns; pawns != 0; pawns &= pawns-1){
			final int index = BitUtil.lsbIndex(pawns);
			final int col = index%8;
			
			final boolean passed = (Masks.passedPawnMasks[player][index] & enemyPawns) == 0;
			final boolean isolated = (PositionMasks.isolatedPawnMask[col] & alliedPawns) == 0;
			final boolean opposed = (PositionMasks.opposedPawnMask[player][index] & enemyPawns) != 0;
			final int opposedFlag = opposed? 1: 0;
			final boolean chain = (PositionMasks.pawnChainMask[player][index] & alliedPawns) != 0;
			final boolean doubled = (PositionMasks.opposedPawnMask[player][index] & alliedPawns) != 0;
			
			if(passed){
				pawnScore += analyzePassedPawn(player, pawns&-pawns, s, nonPawnMaterial);
			}
			if(isolated){
				pawnScore += isolatedPawns[opposedFlag][col];
				if(nonPawnDisadvantage) pawnScore += multWeight(S(-10, -20), npDisMult);
			}
			if(doubled){
				pawnScore += doubledPawns[opposedFlag][col];
				if(nonPawnDisadvantage) pawnScore += multWeight(S(-10, -10), npDisMult);
			}
			if(chain){
				pawnScore += pawnChain[col];
			}
			
			//backward pawn checking
			final long attackSpan = PositionMasks.isolatedPawnMask[col] & Masks.passedPawnMasks[player][index];
			if(!passed && !isolated && !chain &&
					(attackSpan & enemyPawns) != 0 && //enemy pawns that can attack our pawns
					(PositionMasks.pawnAttacks[player][index] & enemyPawns) == 0){ //not attacking enemy pawns
				long b = PositionMasks.pawnAttacks[player][index];
				while((b & all) == 0){
					b = player == 0? b << 8: b >>> 8;
					assert b != 0;
				}
				
				final boolean backward = ((b | (player == 0? b << 8: b >>> 8)) & enemyPawns) != 0;
				if(backward){
					pawnScore += backwardPawns[opposedFlag][col];
					
					//(w0,w1,d) = (116,95,73), with-without, depth=3
					if(nonPawnDisadvantage) pawnScore += multWeight(S(-10, -10), npDisMult);
				}
			}
			
			//allied king distance, used to encourage king supporting pawns in endgame
			final int kingXDist = Math.abs(kingIndex%8 - index%8);
			final int kingYDist = Math.abs((kingIndex>>>3) - (index>>>3));
			final int alliedKingDist = kingXDist > kingYDist? kingXDist: kingYDist;
			assert alliedKingDist < 8;
			kingDistAgg += alliedKingDist-1;
		}
		
		//minimize avg king dist from pawns in endgame
		final double n = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		if(n > 0) pawnScore += S(0, (int)(-kingDistAgg/n*5+.5));
		
		return pawnScore;
	}
	
	/** calculates danger associated with pawn wall weaknesses or storming enemy pawns*/
	private int pawnShelterStormDanger(final int player, final State4 s, final int kingIndex){
		final int kc = kingIndex%8; //king column
		final int kr = player == 0? kingIndex >>> 3: 7-(kingIndex>>>3); //king rank
		final long mask = Masks.passedPawnMasks[player][kingIndex];
		final long wallPawns = s.pawns[player] & mask; //pawns in front of the king
		final long stormPawns = s.pawns[1-player] & mask; //pawns in front of the king
		final int f = kc == 0? 1: kc == 7? 6: kc; //file, eval as if not on edge
		
		int pawnWallDanger = 0;
		
		for(int a = -1; a <= 1; a++){
			final long colMask = Masks.colMask[f+a];
			
			final long allied = wallPawns & colMask;
			final int rankAllied;
			if(allied != 0){
				rankAllied = player == 0? BitUtil.lsbIndex(allied)>>>3: 7-(BitUtil.msbIndex(allied)>>>3);
				pawnWallDanger += pawnShelter[f != kc? 0: 1][rankAllied];
			} else{
				rankAllied = 0;
			}
			
			final long enemy = stormPawns & colMask;
			if(enemy != 0){
				final int rankEnemy = player == 0? BitUtil.lsbIndex(enemy)>>>3: 7-(BitUtil.msbIndex(enemy)>>>3);
				final int type = allied == 0? 0: rankAllied+1 != rankEnemy? 1: 2;
				assert rankEnemy > kr;
				pawnWallDanger += pawnStorm[type][rankEnemy-kr-1];
			}
		}
		
		return pawnWallDanger;
	}
	
	private static int[] centerDanger = new int[]{
		-30, -15, -10, -10, -10, -10, -15, -30,
		-15, -10, -10, -10, -10, -10, -10, -15,
		-10, -10, -8, -8, -8, -8, -10, -10,
		-10, -10, -8, -4, -4, -8, -10, -10,
		-10, -10, -8, -4, -4, -8, -10, -10,
		-10, -10, -8, -8, -8, -8, -10, -10,
		-15, -10, -10, -10, -10, -10, -10, -15,
		-30, -15, -10, -10, -10, -10, -15, -30,
	};
	
	/** stores castling positions indexed [side = left? 0: 1][player]*/
	private final static long[][] castleOffsets = new long[][]{
		{1L<<2, 1L<<58}, //castle left mask
		{1L<<6, 1L<<62}, //castle right mask
	};
	
	/** gives the final index of the king after castling, index [side = left? 0: 1][player]*/
	private final static int[][] castleIndex = new int[][]{
		{2, 58}, //castle left index
		{6, 62}, //castle right index
	};
	
	/** gets the king danger for the passed player*/
	private int getKingDanger(final int player, final State4 s){
		
		int kingDangerScore = 0;
		
		final long king = s.kings[player];
		final int kingIndex = BitUtil.lsbIndex(king);
		final long cmoves = State4.getCastleMoves(player, s);
		
		/*final long kingRing = State4.getKingMoves(player, s.pieces, s.kings[player]);
		final long agg = s.pieces[0]|s.pieces[1];
		int dindex = p.kingDangerSquares[player][kingIndex]; //danger index
		*/
		
		//pawn wall, storm
		if(pawnMoved[0] || pawnMoved[1] || kingMoved[player]){
			int pawnWallBonus = pawnShelterStormDanger(player, s, kingIndex);
			if(cmoves != 0){
				//if we can castle, count the pawn wall/storm weight as best available after castle
				if((castleOffsets[0][player] & cmoves) != 0){
					final int leftIndex = castleIndex[0][player];
					final int leftScore = pawnShelterStormDanger(player, s, leftIndex);
					pawnWallBonus = leftScore > pawnWallBonus? leftScore: pawnWallBonus;
				}
				if((castleOffsets[1][player] & cmoves) != 0){
					final int rightIndex = castleIndex[1][player];
					final int rightScore = pawnShelterStormDanger(player, s, rightIndex);
					pawnWallBonus = rightScore > pawnWallBonus? rightScore: pawnWallBonus;
				}
			}
			kingDangerScore += S(pawnWallBonus, 0);
			this.pawnWallScore[player] = pawnWallBonus;
		} else{
			kingDangerScore += pawnWallScore[player];
		}
		
		kingDangerScore += S(-kingDangerSquares[player][kingIndex], 0);
		kingDangerScore += S(0, centerDanger[kingIndex]);
		
		/*int kingPressureScore = evalKingPressureDanger(kingIndex, player, s);
		if(cmoves != 0){
			if((castleOffsets[0][player] & cmoves) != 0){
				final int leftIndex = castleIndex[0][player];
				final int leftScore = evalKingPressureDanger(leftIndex, player, s);
				kingPressureScore = leftScore < kingPressureScore? leftScore: kingPressureScore;
			}
			if((castleOffsets[1][player] & cmoves) != 0){
				final int rightIndex = castleIndex[1][player];
				final int rightScore = evalKingPressureDanger(rightIndex, player, s);
				kingPressureScore = rightScore < kingPressureScore? rightScore: kingPressureScore;
			}
		}
		w.add(-kingPressureScore, 0);*/
		
		kingDangerScore += evalKingPressureDanger3(kingIndex, player, s, attackMask[player]);
		
		return kingDangerScore;
	}
	
	private static int evalKingPressureDanger3(final int kingIndex, final int player,
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
		
		final long bishops = s.bishops[1-player];
		final long rooks = s.rooks[1-player];
		final long queens = s.queens[1-player];
		final long pawns = s.pawns[1-player];
		final long knights = s.knights[1-player];
		
		//process queen attacks
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

		//process rook attacks
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
		
		//process bishop attacks
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
		
		//process knight attacks
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
	
	private int lazyScore(int player){
		return materialScore[player];
	}

	@Override
	public void processMove(long encoding) {
		update(encoding, false);
	}

	@Override
	public void undoMove(long encoding) {
		update(encoding, true);
	}
	
	/** incrementally updates the score after a move*/
	private void update(long encoding, boolean undo){
		final int dir = undo? -1: 1;
		final int player = MoveEncoder.getPlayer(encoding);
		final int taken = MoveEncoder.getTakenType(encoding);
		if(taken != 0){
			materialScore[1-player] -= dir*materialWeights[taken];
			if(taken == State4.PIECE_TYPE_PAWN) pawnMoved[1-player] = true;
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*materialWeights[State4.PIECE_TYPE_PAWN];
			if(taken == State4.PIECE_TYPE_PAWN) pawnMoved[1-player] = true;
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(materialWeights[State4.PIECE_TYPE_QUEEN]-
					materialWeights[State4.PIECE_TYPE_PAWN]);
			if(taken == State4.PIECE_TYPE_PAWN) pawnMoved[player] = true;
		}
		
		final int moveType = MoveEncoder.getMovePieceType(encoding);
		if(moveType == State4.PIECE_TYPE_KING) kingMoved[player] = true;
		else if(moveType == State4.PIECE_TYPE_PAWN) pawnMoved[player] = true;
	}

	@Override
	public void initialize(State4 s) {
		System.arraycopy(zeroi, 0, materialScore, 0, 2);
		System.arraycopy(zeroi, 0, kingPawnDist, 0, 2);
		System.arraycopy(allTrue, 0, kingMoved, 0, 2);
		System.arraycopy(allTrue, 0, pawnMoved, 0, 2);
		
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
	
	private static int multWeight(final int weight, final double mult){
		final int start = (weight & weightValueMask) - (weight & weightSignMask);
		final int shifted = weight >>> 16;
		final int end = (shifted & weightValueMask) - (shifted & weightSignMask);
		return S((int)(start*mult), (int)(end*mult));
	}

	/** calculates mobility and danger to enemy king from mobility*/
	private static int scoreMobility(final int player, final State4 s,
			final double clutterMult, final int[] nonPawnMaterial, final long[] attackMask){
		int mobScore = 0;
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, s.pawns[1-player]);
		
		final long allied = s.pieces[player];
		final long enemy = s.pieces[1-player];
		final long agg = allied | enemy;
		
		long bishopAttackMask = 0;
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long b = bishops & -bishops;
			final long rawMoves = Masks.getRawBishopMoves(agg, b);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			mobScore += multWeight(mobilityWeights[State4.PIECE_TYPE_BISHOP][count], clutterMult);
			bishopAttackMask |= moves;
		}

		long knightAttackMask = 0;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long k = knights & -knights;
			final long rawMoves = Masks.getRawKnightMoves(k);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			mobScore += multWeight(mobilityWeights[State4.PIECE_TYPE_KNIGHT][count], clutterMult);
			knightAttackMask |= moves;
		}

		long rookAttackMask = 0;
		final long allPieces = s.pieces[0]|s.pieces[1];
		final long enemyPawns = s.pawns[1-player];
		final int alliedKingIndex = BitUtil.lsbIndex(s.kings[player]);
		final int alliedKingCol = alliedKingIndex%8;
		final int alliedKingRow = alliedKingIndex >>> 3;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks&-rooks;
			final long rawMoves = Masks.getRawRookMoves(agg, r);
			final long moves = rawMoves & ~allied & ~enemyPawnAttacks;
			final int moveCount = (int)BitUtil.getSetBits(moves);
			mobScore += multWeight(mobilityWeights[State4.PIECE_TYPE_ROOK][moveCount], clutterMult);
			rookAttackMask |= moves;
			
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
			mobScore += multWeight(mobilityWeights[State4.PIECE_TYPE_QUEEN][count], clutterMult);
			queenAttackMask |= moves;
		}
		
		final long pawnAttackMask = Masks.getRawPawnAttacks(player, s.pawns[player]) & ~allied;
		attackMask[player] = bishopAttackMask | knightAttackMask | rookAttackMask | queenAttackMask | pawnAttackMask;
		
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
