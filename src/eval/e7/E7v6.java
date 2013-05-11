package eval.e7;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.EvalParameters;
import eval.Evaluator2;
import eval.PositionMasks;
import eval.Weight;

public final class E7v6 implements Evaluator2{
	private final static class WeightAgg{
		int start;
		int end;
		void add(final Weight w){
			start += w.start;
			end += w.end;
		}
		void add(final Weight w, final double mult){
			start += w.start*mult+.5;
			end += w.end*mult+.5;
		}
		void add(final int start, final int end){
			this.start += start;
			this.end += end;
		}
		void add(final int v){
			this.start += v;
			this.end += v;
		}
		void clear(){
			start = 0;
			end = 0;
		}
		int score(final double p){
			return start + (int)((end-start)*p);
		}
		public String toString(){
			return "("+start+","+end+")";
		}
		public void set(int start, int end){
			this.start = start;
			this.end = end;
		}
	}
	
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
	
	private final static Weight[] kingDangerTable;
	
	private static Weight S(int start, int end){
		return new Weight(start, end);
	}
	
	private final static Weight[][] isolatedPawns = new Weight[][]{
		{S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25), S(-25, -25)},
		{S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17)},
		//{S(-17, -20), S(-18, -18), S(-20, -23), S(-25, -25), S(-25, -25), S(-20, -23), S(-18, -18), S(-17, -20)},
		//{S(-10, -14), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-17, -17), S(-10, -14)},
	};

	private final static Weight[] pawnChain = new Weight[]{
		S(10,0), S(13,0), S(15,1), S(20,5), S(20,5), S(15,1), S(13,0), S(10,0)
	};

	private final static Weight[][] doubledPawns = new Weight[][]{
		{S(-6,-15), S(-9,-16), S(-10,-16), S(-10,-16), S(-10,-16), S(-10,-16), S(-9,-16), S(-6,-15)},
		{S(-3,-10), S(-5,-14), S(-6,-10), S(-6,-11), S(-6,-11), S(-6,-10), S(-5,-14), S(-3,-10)},
	};

	private final static Weight[][] backwardPawns = new Weight[][]{
		{S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),},
		{S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),},
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
	private final EvalParameters p;
	/** weight aggregator*/
	private final WeightAgg agg = new WeightAgg();
	
	private final int margin;
	private final int endMaterial;
	private final int granularity;
	
	/** multiplier for active pieces on a cluttered board*/
	private double clutterMult;
	/** stores score for non-pawn material*/
	private final int[] nonPawnMaterial = new int[2];
	private final boolean[] kingMoved = new boolean[2];
	private final boolean[] pawnMoved = new boolean[2];
	/** stores attack mask for all pieces for each player, indexed [player][piece-type]*/
	private final long[][] attackMask = new long[2][7];
	
	//cached values
	private final WeightAgg[] pawnWallScore = new WeightAgg[2];
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
		
		kingDangerTable = new Weight[128];
		final int maxSlope = 30;
		final int maxDanger = 1280;
		for(int x = 0, i = 0; i < kingDangerTable.length; i++){
			x = Math.min(maxDanger, Math.min((int)(i*i*.4), x + maxSlope));
			kingDangerTable[i] = new Weight(-x/2, 0);
		}

		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
	}
	
	public E7v6(EvalParameters p){
		this.p = p;
		int startMaterial = (
				  p.materialWeights[State4.PIECE_TYPE_PAWN]*8
				+ p.materialWeights[State4.PIECE_TYPE_KNIGHT]*2
				+ p.materialWeights[State4.PIECE_TYPE_BISHOP]*2
				+ p.materialWeights[State4.PIECE_TYPE_ROOK]*2
				+ p.materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		endMaterial = (
				  p.materialWeights[State4.PIECE_TYPE_ROOK]
				+ p.materialWeights[State4.PIECE_TYPE_QUEEN]
				) * 2;
		
		margin = Weight.margin(startMaterial, endMaterial);
		granularity = p.granularity;
		
		for(int a = 0; a < 2; a++){
			pawnWallScore[a] = new WeightAgg();
		}
	}
	
	public void traceEval(final State4 s){
		initialize(s);
		
		System.out.println("------------------------------");
		System.out.println("note, scores do not take into account tempo="+p.tempo);
		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = Weight.getScale(totalMaterialScore, endMaterial, margin);
		System.out.println("total material score = "+totalMaterialScore);
		System.out.println("weight scaling = "+scale);
		
		for(int a = 0; a < 2; a++){
			System.out.println("------------------------------");
			System.out.println("player "+a);
			System.out.println("material = "+materialScore[a]);
			
			agg.clear();
			int pawnWallBonus = pawnShelterStormDanger(a, s, BitUtil.lsbIndex(s.kings[a]), p);
			System.out.println("current pawn shelter bonus = "+pawnWallBonus);
			final long cmoves = State4.getCastleMoves(a, s);
			if(cmoves != 0){
				//if we can castle, count the pawn wall/storm weight as best available after castle
				if((a == 0 && (cmoves & 1L<<2) != 0) || (a == 1 && (cmoves & 1L<<58) != 0)){
					final int left = pawnShelterStormDanger(a, s, a == 0? 2: 58, p);
					System.out.println("castle left pawn shelter bonus = "+left);
					pawnWallBonus = left > pawnWallBonus? left: pawnWallBonus;
				}
				if((a == 0 && (cmoves & 1L<<6) != 0) || (a == 1 && (cmoves & 1L<<62) != 0)){
					final int right = pawnShelterStormDanger(a, s, a == 0? 6: 62, p);
					System.out.println("castle right pawn shelter bonus = "+right);
					pawnWallBonus = right > pawnWallBonus? right: pawnWallBonus;
				}
			}
			agg.add(pawnWallBonus, 0);
			System.out.println("scaled pawn shelter bonus = "+agg.score(scale));
			
			score(a, s, scale, false);
			final int scaledScore = agg.score(scale);
			System.out.println("total scaled score = "+scaledScore);
		}
	}
	
	public E7v6(){
		this(E7Params.buildEval());
	}

	final WeightAgg endgameBonus = new WeightAgg();
	@Override
	public int eval(State4 s, int player) {
		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = Weight.getScale(totalMaterialScore, endMaterial, margin);
		
		

		final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
		final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
		final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
		clutterMult = clutterIndex[(int)BitUtil.getSetBits(pawnAttacks)];
		
		final int pawnType = State4.PIECE_TYPE_PAWN;
		final int pawnWeight = p.materialWeights[pawnType];
		nonPawnMaterial[0] = materialScore[0]-s.pieceCounts[0][pawnType]*pawnWeight; 
		nonPawnMaterial[1] = materialScore[1]-s.pieceCounts[1][pawnType]*pawnWeight;
		
		
		score(player, s, scale, true);
		final int p1 = granulate(agg.score(scale), granularity);
		final int p1End = agg.score(1);
		
		score(1-player, s, scale, false);
		final int p2 = granulate(agg.score(scale), granularity);
		final int p2End = agg.score(1);
		
		final int score = p1-p2;
		
		endgameBonus.start = (int)(.1*(p1End-p2End)+.5);
		endgameBonus.end = 0;
		
		System.arraycopy(allFalse, 0, kingMoved, 0, 2);
		System.arraycopy(allFalse, 0, pawnMoved, 0, 2);
		return score + endgameBonus.score(scale) + p.tempo.score(scale);
	}

	@Override
	public int lazyEval(State4 state, int player) {
		return lazyScore(player)-lazyScore(1-player);
	}
	
	private void score(final int player, final State4 s, final double scale, final boolean tempo){
		
		agg.clear();
		
		int score = materialScore[player];
		scoreMobility(player, s, agg, p, clutterMult, nonPawnMaterial, attackMask);
		scorePawns(player, s, agg, nonPawnMaterial);
		
		if(s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2){
			agg.add(p.bishopPair);
		}
		
		if(s.queens[1-player] != 0){
			getKingDanger(player, s, agg, p);
		}
		
		agg.start += score;
		agg.end += score;
	}
	
	/** for grainSize a power of 2, returns passed score inside specified granularity,
	 * helps prevent hopping around to different PVs on low score differences*/
	private static int granulate(final int score, final int grainSize){
		return (score+grainSize>>1) & ~(grainSize-1);
	}
	
	private static int max(final int a1, final int a2){
		return a1 > a2? a1: a2;
	}
	
	private static void analyzePassedPawn(final int player, final long p, final State4 s,
			final WeightAgg agg, final int[] nonPawnMaterial){
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
			agg.add(0, max(diff*diff, 15));
		}
		
		//pawn closer than enemy king and no material remaining
		if(pawnDist < enemyKingDist && nonPawnMaterial[1-player] == 0){
			agg.add(500);
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
		agg.add(start, end);
		
		
		//checks for pawn advancement blocked
		final long nextPos = player == 0? p << 8: p >>> 8;
		final long allPieces = s.pieces[0]|s.pieces[1];
		if((nextPos & allPieces) != 0){ //pawn adancement blocked
			agg.add(-start/6/pawnDist, -end/6/pawnDist);
			
			//slight bonus for causing a piece to keep blocking a pawn,
			//(w0,w1,d) = (111,134,126) without-with, depth=3
			if((nextPos & s.queens[1-player]) != 0) agg.add(45/pawnDist);
			else if((nextPos & s.rooks[1-player]) != 0) agg.add(35/pawnDist);
			else if((nextPos & s.bishops[1-player]) != 0) agg.add(25/pawnDist);
			else if((nextPos & s.knights[1-player]) != 0) agg.add(25/pawnDist);
		}
		
		//checks to see whether we have a non-pawn material disadvantage,
		//its very hard to keep a passed pawn when behind
		final int nonPawnMaterialDiff = nonPawnMaterial[player]-nonPawnMaterial[1-player];
		if(nonPawnMaterialDiff+20 < 0){ //+20 to ensure actually down a minor piece (not just 2 bishops vs 2 knights)
			agg.add(-start*2/3, -end*2/3);
		}
		
		//passed pawn supported by rook bonus
		if((s.rooks[player] & PositionMasks.opposedPawnMask[1-player][pawnIndex]) != 0){
			//agg.add(10/pawnDist);
		}
		
		final boolean chain = (PositionMasks.pawnChainMask[player][pawnIndex] & s.pawns[player]) != 0;
		if(chain){
			agg.add(35/pawnDist);
		}
	}
	
	private static void scorePawns(final int player, final State4 s, final WeightAgg agg, final int[] nonPawnMaterial){
		final long enemyPawns = s.pawns[1-player];
		final long alliedPawns = s.pawns[player];
		final long all = alliedPawns | enemyPawns;
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		
		final boolean nonPawnDisadvantage = nonPawnMaterial[player]-nonPawnMaterial[1-player]+20 < 0;
		
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
				//agg.add(p.passedPawnRowWeight[player][index >>> 3]); //index >>> 3 == index/8 == row
				analyzePassedPawn(player, pawns&-pawns, s, agg, nonPawnMaterial);
			}
			if(isolated){
				//agg.add(p.isolatedPawns[opposedFlag][col]);
				agg.add(isolatedPawns[opposedFlag][col]);
				if(nonPawnDisadvantage) agg.add(-5, -10);
			}
			if(doubled){
				agg.add(doubledPawns[opposedFlag][col]);
				//agg.add(doubledPawns[opposedFlag][col]);
				if(nonPawnDisadvantage) agg.add(-5, -10);
			}
			if(chain){
				//agg.add(p.pawnChain[col]);
				agg.add(pawnChain[col]);
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
					agg.add(backwardPawns[opposedFlag][col]);
					
					//(w0,w1,d) = (116,95,73), with-without, depth=3
					if(nonPawnDisadvantage) agg.add(-5, -10);
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
		if(n > 0) agg.add(0, (int)(-kingDistAgg/n*5+.5));
	}
	
	/** calculates danger associated with pawn wall weaknesses or storming enemy pawns*/
	private int pawnShelterStormDanger(final int player, final State4 s, final int kingIndex, final EvalParameters p){
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
				pawnWallDanger += p.pawnShelter[f != kc? 0: 1][rankAllied];
			} else{
				rankAllied = 0;
			}
			
			final long enemy = stormPawns & colMask;
			if(enemy != 0){
				final int rankEnemy = player == 0? BitUtil.lsbIndex(enemy)>>>3: 7-(BitUtil.msbIndex(enemy)>>>3);
				final int type = allied == 0? 0: rankAllied+1 != rankEnemy? 1: 2;
				assert rankEnemy > kr;
				pawnWallDanger += p.pawnStorm[type][rankEnemy-kr-1];
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
	private void getKingDanger(final int player, final State4 s, final WeightAgg w, final EvalParameters p){
		final long king = s.kings[player];
		final int kingIndex = BitUtil.lsbIndex(king);
		final long cmoves = State4.getCastleMoves(player, s);
		
		/*final long kingRing = State4.getKingMoves(player, s.pieces, s.kings[player]);
		final long agg = s.pieces[0]|s.pieces[1];
		int dindex = p.kingDangerSquares[player][kingIndex]; //danger index
		*/
		
		//pawn wall, storm
		if(pawnMoved[0] || pawnMoved[1] || kingMoved[player]){
			int pawnWallBonus = pawnShelterStormDanger(player, s, kingIndex, p);
			if(cmoves != 0){
				//if we can castle, count the pawn wall/storm weight as best available after castle
				if((castleOffsets[0][player] & cmoves) != 0){
					final int leftIndex = castleIndex[0][player];
					final int leftScore = pawnShelterStormDanger(player, s, leftIndex, p);
					pawnWallBonus = leftScore > pawnWallBonus? leftScore: pawnWallBonus;
				}
				if((castleOffsets[1][player] & cmoves) != 0){
					final int rightIndex = castleIndex[1][player];
					final int rightScore = pawnShelterStormDanger(player, s, rightIndex, p);
					pawnWallBonus = rightScore > pawnWallBonus? rightScore: pawnWallBonus;
				}
			}
			w.add(pawnWallBonus, 0);
			this.pawnWallScore[player].set(pawnWallBonus, 0);
		} else{
			final WeightAgg temp = pawnWallScore[player];
			w.add(temp.start, temp.end);
		}
		
		w.add(-kingDangerSquares[player][kingIndex], 0);
		w.add(0, centerDanger[kingIndex]);
		
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
		
		evalKingPressureDanger3(kingIndex, player, s, attackMask, w);
	}
	
	private static void evalKingPressureDanger3(final int kingIndex, final int player,
			final State4 s, final long[][] attackMask, final WeightAgg w){
		
		final long king = 1L << kingIndex;
		final long agg = s.pieces[0] | s.pieces[1];
		int index = 0;
		
		final long kingRing = Masks.getRawKingMoves(king);
		final long alliedAttacks = attackMask[player][State4.PIECE_TYPE_EMPTY];
		final long undefended = kingRing & ~alliedAttacks;
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
			final long queenMoves = State4.getQueenMoves(1-player, s.pieces, q) & undefended;
			for(long temp = queenMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
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
		index += supportedQueenAttacks*6;

		//process rook attacks
		int supportedRookAttacks = 0;
		int supportedRookContactChecks = 0;
		for(long tempRooks = rooks; tempRooks != 0; tempRooks &= tempRooks-1){
			final long r = tempRooks & -tempRooks;
			final long rAgg = agg & ~r;
			final long rookMoves = State4.getRookMoves(1-player, s.pieces, r) & undefended;
			for(long temp = rookMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
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
		index += supportedRookContactChecks*3;
		
		//process bishop attacks
		int supportedBishopAttacks = 0;
		int supportedBishopContactChecks = 0;
		for(long tempBishops = bishops; tempBishops != 0; tempBishops &= tempBishops-1){
			final long b = tempBishops & -tempBishops;
			final long bAgg = agg & ~b;
			final long bishopMoves = State4.getBishopMoves(1-player, s.pieces, b) & undefended;
			for(long temp = bishopMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
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
			final long knightMoves = State4.getKnightMoves(1-player, s.pieces, k) & undefended;
			for(long temp = knightMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
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
		
		
		//(w0,w1,d) = (31,20,27)
		index /= 2;
		w.add(kingDangerTable[index < 128? index: 127]);
		//w.add(-index/4, 0); //(w0,w1,d) = (31,19,13)
		//w.add(-index/4, 0);
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
			materialScore[1-player] -= dir*p.materialWeights[taken];
			if(taken == State4.PIECE_TYPE_PAWN) pawnMoved[1-player] = true;
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*p.materialWeights[State4.PIECE_TYPE_PAWN];
			if(taken == State4.PIECE_TYPE_PAWN) pawnMoved[1-player] = true;
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(p.materialWeights[State4.PIECE_TYPE_QUEEN]-
					p.materialWeights[State4.PIECE_TYPE_PAWN]);
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
			materialScore[a] += s.pieceCounts[a][b] * p.materialWeights[b];
			
			final int n = State4.PIECE_TYPE_KNIGHT;
			materialScore[a] += s.pieceCounts[a][n] * p.materialWeights[n];
			
			final int q = State4.PIECE_TYPE_QUEEN;
			materialScore[a] += s.pieceCounts[a][q] * p.materialWeights[q];
			
			final int r = State4.PIECE_TYPE_ROOK;
			materialScore[a] += s.pieceCounts[a][r] * p.materialWeights[r];
			
			final int p = State4.PIECE_TYPE_PAWN;
			materialScore[a] += s.pieceCounts[a][p] * this.p.materialWeights[p];
		}
	}
	
	@Override
	public void reset(){}
	
	/** without - with: (w0,w1,d) = (46,90,37), prob <= 0.003715*/
	private static void evalKingPressureDanger2(final int kingIndex, final int player,
			final State4 s, final long[][] attackMask, final WeightAgg w){
		
		
		final long king = 1L << kingIndex;
		final long agg = s.pieces[0] | s.pieces[1];
		//final int kc = kingIndex%8; //king column
		//final long k = kc == 0? king<<1: kc == 7? king>>>1: king; //adjusted king position
		//final long mask = Masks.passedPawnMasks[1-player][BitUtil.lsbIndex(k)];
		//long pressureSquares = Masks.getRawKingMoves(k) & ~mask; //king squares in front and on sides of king
		//final long alliedPawns = s.pawns[player];
		int index = 0;
		
		//index += kingDangerSquares[player][kingIndex];
		
		final long kingRing = Masks.getRawKingMoves(king);
		final long alliedAttacks = attackMask[player][State4.PIECE_TYPE_EMPTY];
		final long undefended = kingRing & ~alliedAttacks;
	
		final long support = attackMask[1-player][State4.PIECE_TYPE_BISHOP] |
				attackMask[1-player][State4.PIECE_TYPE_KNIGHT] |
				attackMask[1-player][State4.PIECE_TYPE_PAWN];
		final long bishopSupport = attackMask[1-player][State4.PIECE_TYPE_PAWN] |
				attackMask[1-player][State4.PIECE_TYPE_QUEEN] |
				attackMask[1-player][State4.PIECE_TYPE_ROOK] |
				attackMask[1-player][State4.PIECE_TYPE_KNIGHT];
		final long knightSupport = attackMask[1-player][State4.PIECE_TYPE_PAWN] |
				attackMask[1-player][State4.PIECE_TYPE_QUEEN] |
				attackMask[1-player][State4.PIECE_TYPE_ROOK] |
				attackMask[1-player][State4.PIECE_TYPE_BISHOP];
		
		final long enemy = s.pieces[1-player];
		//final long alliedPawnAttacks = attackMask[player][State4.PIECE_TYPE_PAWN];
		
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
			final long queenMoves = State4.getQueenMoves(1-player, s.pieces, q) & undefended;
			for(long temp = queenMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
				final long pos = temp & -temp;
				if((pos & undefended) != 0){
					final long aggPieces = qAgg | pos;
					final long bishopAttacks = Masks.getRawBishopMoves(aggPieces, pos);
					final long knightAttacks = Masks.getRawKnightMoves(pos);
					final long rookAttacks = Masks.getRawRookMoves(aggPieces, pos);
					final long pawnAttacks = Masks.getRawPawnAttacks(player, pawns);
					
					/*final long supportMask = 
							bishopAttacks |
							knightAttacks |
							rookAttacks |
							pawnAttacks;*/
					
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
		index += supportedQueenAttacks*4;
		
		//process rook attacks
		/*long rookMoves = 0;
		long rookAttacks = 0;
		int rookContactChecks = 0;
		final long rookSupport = support | attackMask[1-player][State4.PIECE_TYPE_QUEEN];
		for(long rooks = s.rooks[1-player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks & -rooks;
			rookMoves |= State4.getRookMoves(1-player, s.pieces, r);
			final long rAgg = agg & ~r;
			for(long temp = rookMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
				final long pos = temp&-temp;
				final long l2moves = Masks.getRawRookMoves(rAgg, pos) & ~enemy;
				rookAttacks |= l2moves;
				if((l2moves & king & pos & undefended & rookSupport) != 0) rookContactChecks++;
			}
		}
		index += BitUtil.getSetBits(rookAttacks & rookSupport & undefended) * 2; //rook supported attacks
		index += rookContactChecks * 4; //rook supported contact attacks
		index += BitUtil.getSetBits(rookAttacks & king & ~alliedAttacks) * 1; //rook supported checks
		
		//process bishop attacks
		long bishopAttacks = 0;
		for(long bishops = s.bishops[1-player]; bishops != 0; bishops &= bishops-1){
			final long b = bishops & -bishops;
			final long bishopMoves = State4.getBishopMoves(1-player, s.pieces, b);
			final long bAgg = agg & ~b;
			for(long temp = bishopMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
				bishopAttacks |= Masks.getRawBishopMoves(bAgg, temp&-temp) & ~enemy;
			}
		}
		index += BitUtil.getSetBits(bishopAttacks & bishopSupport & undefended) * 1; //bishop supported attacks
		index += BitUtil.getSetBits(bishopAttacks & king & ~alliedAttacks) * 1; //bishop supported checks
	
		//process knight attacks
		long knightAttacks = 0;
		for(long knights = s.knights[1-player]; knights != 0; knights &= knights-1){
			final long n = knights & -knights;
			final long knightMoves = State4.getKnightMoves(1-player, s.pieces, n);
			for(long temp = knightMoves & ~alliedAttacks; temp != 0; temp &= temp-1){
				knightAttacks |= Masks.getRawKnightMoves(temp&-temp) & ~enemy;
			}
		}
		index += BitUtil.getSetBits(knightAttacks & knightSupport & undefended) * 1; //knight supported attacks
		index += BitUtil.getSetBits(knightAttacks & king & ~alliedAttacks) * 1; //knight checks*/
	
		//div 2, (w0,w1,d) = (17,13,13)
		
		//System.out.println("score = "+index);
		//return score;
		//w.add(kingDangerTable[index < 128? index: 127]);
		w.add(-index, 0);
		//return (int)(Math.log1p(score) * 20);
	}

	/** calculates mobility and danger to enemy king from mobility*/
	private static void scoreMobility(final int player, final State4 s, final WeightAgg agg,
			final EvalParameters c, final double clutterMult, final int[] nonPawnMaterial, final long[][] attackMask){
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, s.pawns[1-player]);
		/*final long enemyKing = s.kings[1-player];
		final long enemyKingRing = Masks.getRawKingMoves(enemyKing);
		int kingRingAttacks = c.kingDangerSquares[1-player][BitUtil.lsbIndex(enemyKing)]; //danger index*/
		
		attackMask[player][State4.PIECE_TYPE_EMPTY] = 0;

		attackMask[player][State4.PIECE_TYPE_BISHOP] = 0;
		long enemyAttacks = enemyPawnAttacks;
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long moves = State4.getBishopMoves(player, s.pieces, bishops&-bishops) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_BISHOP][count], clutterMult);
			attackMask[player][State4.PIECE_TYPE_BISHOP] |= moves;
		}
		attackMask[player][State4.PIECE_TYPE_EMPTY] |= attackMask[player][State4.PIECE_TYPE_BISHOP];

		attackMask[player][State4.PIECE_TYPE_KNIGHT] = 0;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long moves = State4.getKnightMoves(player, s.pieces, knights&-knights) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_KNIGHT][count], clutterMult);
			attackMask[player][State4.PIECE_TYPE_KNIGHT] |= moves;
		}
		attackMask[player][State4.PIECE_TYPE_EMPTY] |= attackMask[player][State4.PIECE_TYPE_KNIGHT];
		
		//===============================
		//note: even though commented out, slight indication not an improvement
		//results for testing (without - with): (w0,w1,d) = (21,16,29)
		//===============================
		
		//add enemy minor piece attacks
		//----------------------------------------
		/*for(long bishops = s.bishops[1-player]; bishops != 0; bishops &= bishops-1){
			final long moves = State4.getBishopMoves(1-player, s.pieces, bishops&-bishops);
			enemyAttacks |= moves;
		}
		for(long knights = s.knights[1-player]; knights != 0; knights &= knights-1){
			final long moves = State4.getKnightMoves(1-player, s.pieces, knights&-knights);
			enemyAttacks |= moves;
		}*/
		//----------------------------------------
		

		attackMask[player][State4.PIECE_TYPE_ROOK] = 0;
		final long allPieces = s.pieces[0]|s.pieces[1];
		final long enemyPawns = s.pawns[1-player];
		final int alliedKingIndex = BitUtil.lsbIndex(s.kings[player]);
		final int alliedKingCol = alliedKingIndex%8;
		final int alliedKingRow = alliedKingIndex >>> 3;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks&-rooks;
			final long moves = State4.getRookMoves(player, s.pieces, r) & ~enemyAttacks;
			final int moveCount = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_ROOK][moveCount], clutterMult);
			attackMask[player][State4.PIECE_TYPE_ROOK] |= moves;
			
			final int rindex = BitUtil.lsbIndex(r);
			final int col = rindex%8;
			if(isHalfOpen(col, enemyPawns, allPieces & ~r)){ //tests file half open
				agg.add(6, 15);
				if(((allPieces & ~r) & Masks.colMask[col]) == 0){ //tests file open
					agg.add(6, 15);
				}
			}

			final int row = rindex >>> 3;
			if(row == alliedKingRow){
				final int backRank = player == 0? 0: 7;
				if(alliedKingRow == backRank && moveCount <= 4){
					if(alliedKingCol >= 4 && col > alliedKingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][1]){
							agg.add(-20, -50); //trapped, but can castle right
						} else{
							agg.add(-50, -120); //trapped, cannot castle
							//old, 80, 150
						}
					}
					if(alliedKingCol <= 3 && col < alliedKingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][0]){
							agg.add(-20, -50); //trapped, but can castle left
						} else{
							agg.add(-50, -120); //trapped, cannot castle
						}
					}
				}
			}
		}
		attackMask[player][State4.PIECE_TYPE_EMPTY] |= attackMask[player][State4.PIECE_TYPE_ROOK];
		
		//add enemy rook attacks
		//----------------------------------------
		/*for(long rooks = s.rooks[1-player]; rooks != 0; rooks &= rooks-1){
			final long moves = State4.getRookMoves(1-player, s.pieces, rooks&-rooks);
			enemyAttacks |= moves;
		}*/
		//----------------------------------------

		attackMask[player][State4.PIECE_TYPE_QUEEN] = 0;
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long q = queens&-queens;
			final long moves = State4.getQueenMoves(player, s.pieces, q) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_QUEEN][count], clutterMult);
			attackMask[player][State4.PIECE_TYPE_QUEEN] |= moves;
		}
		attackMask[player][State4.PIECE_TYPE_EMPTY] |= attackMask[player][State4.PIECE_TYPE_QUEEN];
		
		attackMask[player][State4.PIECE_TYPE_PAWN] = Masks.getRawPawnAttacks(player, s.pawns[player]) & ~s.pieces[player];
		attackMask[player][State4.PIECE_TYPE_EMPTY] |= attackMask[player][State4.PIECE_TYPE_PAWN];
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
