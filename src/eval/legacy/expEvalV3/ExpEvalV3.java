package eval.legacy.expEvalV3;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.EvalParameters;
import eval.Evaluator2;
import eval.Weight;

public final class ExpEvalV3 implements Evaluator2{
	private final static class WeightAgg{
		int start;
		int end;
		void add(final Weight w){
			start += w.start;
			end += w.end;
		}
		void clear(){
			start = 0;
			end = 0;
		}
		int score(final double p){
			return start + (int)((end-start)*p);
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
	
	private final int[] materialScore = new int[2];
	/** current max number of moves by piece type*/
	private final int[][] maxMobility = new int[2][7];
	private final EvalParameters p;
	/** weight aggregator*/
	private final WeightAgg agg = new WeightAgg();
	
	private final int margin;
	private final int endMaterial;
	
	public ExpEvalV3(EvalParameters p){
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
	}
	
	public ExpEvalV3(){
		this(DefaultEvalWeights.defaultEval());
	}
	
	@Override
	public int eval(State4 s, int player) {
		final int totalMaterialScore = materialScore[0]+materialScore[1];
		final double scale = Weight.getScale(totalMaterialScore, endMaterial, margin);
		return score(player, s, scale) - score(1-player, s, scale);
	}

	@Override
	public int lazyEval(State4 state, int player) {
		return lazyScore(player)-lazyScore(1-player);
	}
	
	private int score(final int player, final State4 s, final double scale){
		int score = materialScore[player];
		scoreMobility(player, s, agg, p);
		scorePawns(player, s, agg, p);
		
		if(s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2){
			agg.add(p.bishopPair);
		}
		
		if(s.queens[1-player] != 0){
			getKingDanger(player, s, agg, p);
		}
		
		return score + agg.score(scale);
	}
	
	private static void scorePawns(final int player, final State4 s, final WeightAgg agg, final EvalParameters p){
		final long enemyPawns = s.pawns[1-player];
		for(long pawns = s.pawns[player]; pawns != 0; pawns &= pawns-1){
			final int index = BitUtil.lsbIndex(pawns);
			if((Masks.passedPawnMasks[player][index] & enemyPawns) == 0){ //pawn passed
				agg.add(p.passedPawnRowWeight[player][index >> 3]); //index >>> 3 == index/8 == row
			}
		}
	}
	
	/** gets the king danger for the passed player*/
	private void getKingDanger(final int player, final State4 s, final WeightAgg w, final EvalParameters p){
		final long kingRing = State4.getKingMoves(player, s.pieces, s.kings[player]);
		final long king = s.kings[player];
		final int kingSq = BitUtil.lsbIndex(s.kings[player]);
		int dindex = p.kingDangerSquares[player][kingSq]; //danger index
		final long agg = s.pieces[0]|s.pieces[1];
		
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
		final int taken = MoveEncoder.getTakenType(encoding);
		if(taken != 0){
			materialScore[1-player] -= dir*p.materialWeights[taken];
			maxMobility[1-player][taken] -= dir*maxPieceMobility[taken];
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*p.materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(p.materialWeights[State4.PIECE_TYPE_QUEEN]-
					p.materialWeights[State4.PIECE_TYPE_PAWN]);
			maxMobility[1-player][State4.PIECE_TYPE_QUEEN] +=
					dir*maxPieceMobility[State4.PIECE_TYPE_QUEEN];
		}
	}

	@Override
	public void initialize(State4 s) {
		//initialize raw material scores
		materialScore[0] = 0;
		materialScore[1] = 0;
		System.arraycopy(zeroi7, 0, maxMobility[0], 0, 7);
		System.arraycopy(zeroi7, 0, maxMobility[1], 0, 7);
		
		agg.clear();
		
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
		
		/*mobilityScore[0] = initializeMobilityScore(0, s, c);
		mobilityScore[1] = initializeMobilityScore(1, s, c);*/
	}
	
	private static void scoreMobility(final int player, final State4 s, final WeightAgg agg, final EvalParameters c){
		final long enemyPawnAttaks = Masks.getRawPawnAttacks(1-player, s.pawns[1-player]);
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long moves = State4.getBishopMoves(player, s.pieces, bishops&-bishops) & ~enemyPawnAttaks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_BISHOP][count]);
		}
		
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long moves = State4.getKnightMoves(player, s.pieces, knights&-knights) & ~enemyPawnAttaks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_KNIGHT][count]);
		}
		
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long moves = State4.getQueenMoves(player, s.pieces, queens&-queens) & ~enemyPawnAttaks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_QUEEN][count]);
		}
		
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long moves = State4.getRookMoves(player, s.pieces, rooks&-rooks) & ~enemyPawnAttaks;
			final int count = (int)BitUtil.getSetBits(moves);
			agg.add(c.mobilityWeights[State4.PIECE_TYPE_ROOK][count]);
		}
	}
}
