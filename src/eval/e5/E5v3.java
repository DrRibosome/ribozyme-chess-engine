package eval.e5;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.EvalParameters;
import eval.Evaluator2;
import eval.PositionMasks;
import eval.Weight;

public final class E5v3 implements Evaluator2{
	private final static class WeightAgg{
		int start;
		int end;
		void add(final Weight w){
			start += w.start;
			end += w.end;
		}
		void add(final int start, final int end){
			this.start += start;
			this.end += end;
		}
		void add(WeightAgg w){
			this.start += w.start;
			this.end += w.end;
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
	}

	/** stores max number of moves by piece type (major minor pieces only)*/
	public final static int[] maxPieceMobility;
	static{
		maxPieceMobility = new int[7];
		maxPieceMobility[State4.PIECE_TYPE_BISHOP] = 14;
		maxPieceMobility[State4.PIECE_TYPE_KNIGHT] = 8;
		maxPieceMobility[State4.PIECE_TYPE_ROOK] = 15;
		maxPieceMobility[State4.PIECE_TYPE_QUEEN] = 28;
	}
	
	private final static int[] zeroi7 = new int[7];
	private final static int[] zeroi8 = new int[8];
	
	/** counts pawns in each column*/
	private final int[][] pawnCount = new int[2][8];
	
	private final int[] materialScore = new int[2];
	/** current max number of moves by piece type*/
	private final int[][] maxMobility = new int[2][7];
	private final EvalParameters p;
	/** weight aggregator*/
	private final WeightAgg agg = new WeightAgg();
	/** stores whether king has moved since last eval*/
	private boolean kingMoved;
	/** stores whether a pawn has moved since last eval*/
	private boolean pawnMoved;

	private final int margin;
	private final int endMaterial;
	private final int granularity;
	
	//cached values
	private final int[] pawnShieldStorm = new int[2];
	private final WeightAgg[] pawnScore = new WeightAgg[]{new WeightAgg(), new WeightAgg()};
	
	
	public E5v3(EvalParameters p){
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
	
	public E5v3(){
		this(E5Params3.buildEval());
	}

	final WeightAgg endgameBonus = new WeightAgg();
	@Override
	public int eval(State4 s, int player) {
		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = Weight.getScale(totalMaterialScore, endMaterial, margin);
		
		score(player, s, scale, true);
		final int p1 = granulate(agg.score(scale), granularity);
		final int p1End = agg.score(1);
		
		score(1-player, s, scale, false);
		final int p2 = granulate(agg.score(scale), granularity);
		final int p2End = agg.score(1);
		
		final int score = p1-p2;
		
		//E5 - E4
		//(w0,w1,d) = (28,27,18), .2
		//(w0,w1,d) = (110,83,91), .1
		//(w0,w1,d) = (23,23,14), .05
		endgameBonus.start = (int)(.1*(p1End-p2End)+.5);
		endgameBonus.end = 0;
		
		kingMoved = false;
		pawnMoved = false;
		
		return score + endgameBonus.score(scale) + p.tempo.score(scale);
	}

	@Override
	public int lazyEval(State4 state, int player) {
		return lazyScore(player)-lazyScore(1-player);
	}
	
	private void score(final int player, final State4 s, final double scale, final boolean tempo){
		
		agg.clear();
		
		int score = materialScore[player];
		scoreMobility(player, s, agg, p);
		if(pawnMoved || kingMoved){
			pawnScore[player].clear();
			scorePawns(player, s, pawnScore[player], p, pawnCount[player]);
			agg.add(pawnScore[player]);
		} else{
			agg.add(pawnScore[player]);
		}
		
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
	
	private static void scoreRooks(final int player, final long rook, final State4 s, final WeightAgg agg){
		//test for trapped rook
		
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		final int kCol = kingIndex%8;
		final int kRow = kingIndex/8;
		
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final int rIndex = BitUtil.lsbIndex(rooks);
			final int rCol = rIndex%8;
			final int rRow = rIndex/8;
			if(kCol >= 4 && rCol >= 4 && kRow == rRow && player == 0? kRow == 0: kRow == 7){
				
			}
		}
	}
	
	private static int max(final int a1, final int a2){
		return a1 > a2? a1: a2;
	}
	
	private void analyzePassedPawn(final int player, final long p, final State4 s, final WeightAgg agg){
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
		if(pawnDist < enemyKingDist &&
				materialScore[1-player] - s.pieceCounts[1-player][State4.PIECE_TYPE_PAWN]
					*this.p.materialWeights[State4.PIECE_TYPE_PAWN] == 0){
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
			//agg.add(-10/pawnDist, -30/pawnDist);
		}
		
		//checks to see whether we have a non-pawn material disadvantage,
		//its very hard to keep a passed pawn when behind
		final int pawnType = State4.PIECE_TYPE_PAWN;
		final int pawnWeight = this.p.materialWeights[pawnType];
		final int nonPawnMaterialDiff = 
				(materialScore[player]-s.pieceCounts[player][pawnType]*pawnWeight) - 
				(materialScore[1-player]-s.pieceCounts[1-player][pawnType]*pawnWeight);
		if(nonPawnMaterialDiff < 0){
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
	
	private void scorePawns(final int player, final State4 s, final WeightAgg agg,
			final EvalParameters p, final int[] pawnCount){
		final long enemyPawns = s.pawns[1-player];
		final long alliedPawns = s.pawns[player];
		final long all = alliedPawns | enemyPawns;
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		
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
				analyzePassedPawn(player, pawns&-pawns, s, agg);
			}
			if(isolated){
				agg.add(p.isolatedPawns[opposedFlag][col]);
			}
			if(doubled){
				agg.add(p.doubledPawns[opposedFlag][col]);
			}
			if(chain){
				agg.add(p.pawnChain[col]);
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
					agg.add(p.backwardPawns[opposedFlag][col]);
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
	
	/** gets the king danger for the passed player*/
	private void getKingDanger(final int player, final State4 s, final WeightAgg w, final EvalParameters p){
		final long kingRing = State4.getKingMoves(player, s.pieces, s.kings[player]);
		final long king = s.kings[player];
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		final long agg = s.pieces[0]|s.pieces[1];
		
		int dindex = p.kingDangerSquares[player][kingIndex]; //danger index
		
		//pawn wall, storm
		if(kingMoved || pawnMoved){
			int pawnWallBonus = pawnShelterStormDanger(player, s, kingIndex, p);
			final long cmoves = State4.getCastleMoves(player, s);
			if(cmoves != 0){
				//if we can castle, count the pawn wall/storm weight as best available after castle
				if((player == 0 && (cmoves & 1L<<2) != 0) || (player == 1 && (cmoves & 1L<<58) != 0)){
					final int left = pawnShelterStormDanger(player, s, player == 0? 2: 58, p);
					pawnWallBonus = left > pawnWallBonus? left: pawnWallBonus;
				}
				if((player == 0 && (cmoves & 1L<<6) != 0) || (player == 1 && (cmoves & 1L<<62) != 0)){
					final int right = pawnShelterStormDanger(player, s, player == 0? 6: 62, p);
					pawnWallBonus = right > pawnWallBonus? right: pawnWallBonus;
				}
			}
			w.add(pawnWallBonus, 0);
			pawnShieldStorm[player] = pawnWallBonus;
		} else{
			w.add(pawnShieldStorm[player], 0);
		}
		
		w.add(0, centerDanger[kingIndex]);
		
		//case that checking piece not defended should be handled by qsearch
		//(ie, it will just be taken by king, etc, and a better move will be chosen)
		
		for(long queens = s.queens[1-player]; queens != 0; queens &= queens-1){
			final long q = queens&-queens;
			final long moves = Masks.getRawQueenMoves(agg, q);
			if((q & kingRing) != 0){ //contact check
				dindex += p.contactCheckQueen;
			} else if((moves & king) != 0){ //non-contact check
				dindex += p.queenCheck;
			}
			dindex += p.dangerKingAttacks[State4.PIECE_TYPE_QUEEN] * BitUtil.getSetBits(moves & kingRing);
		}
		
		for(long rooks = s.rooks[1-player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks&-rooks;
			final long moves = Masks.getRawRookMoves(agg, r);
			 if((moves & king) != 0){
				if((r & kingRing) != 0){ //contact check
					dindex += p.contactCheckRook;
				} else{ //non-contact check
					dindex += p.rookCheck;
				}
			}
			dindex += p.dangerKingAttacks[State4.PIECE_TYPE_ROOK] * BitUtil.getSetBits(moves & kingRing);
		}
		
		for(long bishops = s.bishops[1-player]; bishops != 0; bishops &= bishops-1){
			final long moves = Masks.getRawBishopMoves(agg, bishops);
			if((moves & king) != 0){ //non-contact check
				dindex += p.bishopCheck;
			}
			dindex += p.dangerKingAttacks[State4.PIECE_TYPE_BISHOP] * BitUtil.getSetBits(moves & kingRing);
		}
		
		for(long knights = s.knights[1-player]; knights != 0; knights &= knights-1){
			final long moves = Masks.getRawKnightMoves(knights);
			if((moves & king) != 0){ //non-contact check
				dindex += p.knightCheck;
			}
			dindex += p.dangerKingAttacks[State4.PIECE_TYPE_KNIGHT] * BitUtil.getSetBits(moves & kingRing);
		}
		
		w.add(p.kingDangerValues[dindex]);
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
		final int moveType = MoveEncoder.getMovePieceType(encoding);
		final int taken = MoveEncoder.getTakenType(encoding);
		final int pos1Col = MoveEncoder.getPos1(encoding)%8;
		final int pos2Col = MoveEncoder.getPos2(encoding)%8;
		if(taken != 0){
			materialScore[1-player] -= dir*p.materialWeights[taken];
			maxMobility[1-player][taken] -= dir*maxPieceMobility[taken];
			if(taken == State4.PIECE_TYPE_PAWN){
				pawnCount[1-player][pos2Col] -= dir;
			}
			if(MoveEncoder.getMovePieceType(encoding) == State4.PIECE_TYPE_PAWN){
				pawnCount[player][pos1Col] -= dir;
				pawnCount[player][pos2Col] += dir;
			}
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*p.materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(p.materialWeights[State4.PIECE_TYPE_QUEEN]-
					p.materialWeights[State4.PIECE_TYPE_PAWN]);
			maxMobility[1-player][State4.PIECE_TYPE_QUEEN] +=
					dir*maxPieceMobility[State4.PIECE_TYPE_QUEEN];
			
			pawnCount[player][pos1Col] -= dir;
		}
		
		pawnMoved |= moveType == State4.PIECE_TYPE_PAWN || taken == State4.PIECE_TYPE_PAWN;
		kingMoved |= moveType == State4.PIECE_TYPE_KING || taken == State4.PIECE_TYPE_KING;
	}

	@Override
	public void initialize(State4 s) {
		//initialize raw material scores
		materialScore[0] = 0;
		materialScore[1] = 0;
		System.arraycopy(zeroi7, 0, maxMobility[0], 0, 7);
		System.arraycopy(zeroi7, 0, maxMobility[1], 0, 7);
		
		for(int a = 0; a < 2; a++){
			final int b = State4.PIECE_TYPE_BISHOP;
			materialScore[a] += s.pieceCounts[a][b] * p.materialWeights[b];
			maxMobility[a][b] += s.pieceCounts[a][b] * maxPieceMobility[b];
			
			final int n = State4.PIECE_TYPE_KNIGHT;
			materialScore[a] += s.pieceCounts[a][n] * p.materialWeights[n];
			maxMobility[a][n] += s.pieceCounts[a][n] * maxPieceMobility[n];
			
			final int q = State4.PIECE_TYPE_QUEEN;
			materialScore[a] += s.pieceCounts[a][q] * p.materialWeights[q];
			maxMobility[a][q] += s.pieceCounts[a][q] * maxPieceMobility[q];
			
			final int r = State4.PIECE_TYPE_ROOK;
			materialScore[a] += s.pieceCounts[a][r] * p.materialWeights[r];
			maxMobility[a][r] += s.pieceCounts[a][r] * maxPieceMobility[r];
			
			final int p = State4.PIECE_TYPE_PAWN;
			materialScore[a] += s.pieceCounts[a][p] * this.p.materialWeights[p];
		}
		
		for(int a = 0; a < 2; a++){
			System.arraycopy(zeroi8, 0, pawnCount[a], 0, 8);
			for(long p = s.pawns[a]; p != 0; p &= p-1){
				final int index = BitUtil.lsbIndex(p);
				pawnCount[a][index%8]++;
			}
		}
		
		kingMoved = true;
		pawnMoved = true;
	}
	
	private static void scoreMobility(final int player, final State4 s, final WeightAgg agg, final EvalParameters c){
		final long enemyPawnAttacks = Masks.getRawPawnAttacks(1-player, s.pawns[1-player]);
		
		long enemyAttacks = enemyPawnAttacks;
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long moves = State4.getBishopMoves(player, s.pieces, bishops&-bishops) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_BISHOP][count]);
		}
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long moves = State4.getKnightMoves(player, s.pieces, knights&-knights) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_KNIGHT][count]);
		}
		
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
		
		
		final long allPieces = s.pieces[0]|s.pieces[1];
		final long enemyPawns = s.pawns[1-player];
		final int kingIndex = BitUtil.lsbIndex(s.kings[player]);
		final int kingCol = kingIndex%8;
		final int kingRow = kingIndex >>> 3;
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long r = rooks&-rooks;
			final long moves = State4.getRookMoves(player, s.pieces, r) & ~enemyAttacks;
			final int moveCount = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_ROOK][moveCount]);
			
			final int rindex = BitUtil.lsbIndex(r);
			final int col = rindex%8;
			if(isHalfOpen(col, enemyPawns, allPieces & ~r)){ //tests file half open
				agg.add(6, 15);
				if(((allPieces & ~r) & Masks.colMask[col]) == 0){ //tests file open
					agg.add(6, 15);
				}
			}

			final int row = rindex >>> 3;
			if(row == kingRow){
				final int backRank = player == 0? 0: 7;
				if(kingRow == backRank && moveCount <= 4){
					if(kingCol >= 4 && col > kingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][1]){
							agg.add(-20, -50); //trapped, but can castle right
						} else{
							agg.add(-50, -120); //trapped, cannot castle
							//old, 80, 150
						}
					}
					if(kingCol <= 3 && col < kingCol){
						if(!s.kingMoved[player] && !s.rookMoved[player][0]){
							agg.add(-20, -50); //trapped, but can castle left
						} else{
							agg.add(-50, -120); //trapped, cannot castle
						}
					}
				}
			}
		}
		
		//add enemy rook attacks
		//----------------------------------------
		/*for(long rooks = s.rooks[1-player]; rooks != 0; rooks &= rooks-1){
			final long moves = State4.getRookMoves(1-player, s.pieces, rooks&-rooks);
			enemyAttacks |= moves;
		}*/
		//----------------------------------------
		
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long moves = State4.getQueenMoves(player, s.pieces, queens&-queens) & ~enemyAttacks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_QUEEN][count]);
		}
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
