package eval.expEvalV2;

import state4.BitUtil;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;

public final class ExpEvalV2 implements Evaluator2<State4>{
	private final static int[] zeroi7 = new int[7];
	
	private final int[] materialScore = new int[2];
	/** current max number of moves by piece type*/
	private final int[][] maxMobility = new int[2][7];
	private final FeatureExtractor2.FeatureSet fset = new FeatureExtractor2.FeatureSet();
	private final EvalConstantsV2 c;
	
	public ExpEvalV2(){
		this(EvalConstantsV2.defaultEval());
	}
	
	public ExpEvalV2(EvalConstantsV2 c){
		this.c = c;
	}
	
	@Override
	public double eval(State4 s, int player) {
		final double s1 = score(player, s);
		return s1-score(1-player, s);
	}

	@Override
	public double lazyEval(State4 state, int player) {
		return lazyScore(player)-lazyScore(1-player);
	}
	
	private double score(int player, State4 s){
		FeatureExtractor2.loadFeatures(fset, player, s, true);
		
		double score = materialScore[player];
		score += scoreMobility(player, s, c);
		score += scorePawns(player, fset, s, c);
		score += s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2?
				c.bishopPairWeight: 0;
		
		return score;
	}
	
	private static double scorePawns(final int player, final FeatureExtractor2.FeatureSet fset, final State4 s, final EvalConstantsV2 c){
		final int len = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		double m = 0;
		for(int a = 0; a < len; a++){
			m += c.unopposedPawnWeight*fset.pawnUnopposed[a];
			if(fset.pawnPassed[a] != 0){
				m += c.supportedPassedPawn * fset.supportedPawn[a];
				m += c.passedPawnRowWeight[player][fset.pawnRow[a]];
			}
		}
		
		m += c.doubledPawnsWeight*fset.doubledPawns;
		m += c.tripledPawnsWeight*fset.tripledPawns;
		
		return m;
	}
	
	private double lazyScore(int player){
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
			materialScore[1-player] -= dir*c.materialWeights[taken];
			maxMobility[1-player][taken] -= dir*EvalConstantsV2.maxPieceMobility[taken];
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*c.materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(c.materialWeights[State4.PIECE_TYPE_QUEEN]-
					c.materialWeights[State4.PIECE_TYPE_PAWN]);
			maxMobility[1-player][State4.PIECE_TYPE_QUEEN] +=
					dir*EvalConstantsV2.maxPieceMobility[State4.PIECE_TYPE_QUEEN];
		}
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
			materialScore[a] += s.pieceCounts[a][b] * c.materialWeights[b];
			maxMobility[a][b] += s.pieceCounts[a][b] * EvalConstantsV2.maxPieceMobility[b];
			
			final int n = State4.PIECE_TYPE_KNIGHT;
			materialScore[a] += s.pieceCounts[a][n] * c.materialWeights[n];
			maxMobility[a][n] += s.pieceCounts[a][n] * EvalConstantsV2.maxPieceMobility[n];
			
			final int q = State4.PIECE_TYPE_QUEEN;
			materialScore[a] += s.pieceCounts[a][q] * c.materialWeights[q];
			maxMobility[a][q] += s.pieceCounts[a][q] * EvalConstantsV2.maxPieceMobility[q];
			
			final int r = State4.PIECE_TYPE_ROOK;
			materialScore[a] += s.pieceCounts[a][r] * c.materialWeights[r];
			maxMobility[a][r] += s.pieceCounts[a][r] * EvalConstantsV2.maxPieceMobility[r];
			
			final int p = State4.PIECE_TYPE_PAWN;
			materialScore[a] += s.pieceCounts[a][p] * c.materialWeights[p];
		}
		
		/*mobilityScore[0] = initializeMobilityScore(0, s, c);
		mobilityScore[1] = initializeMobilityScore(1, s, c);*/
	}
	
	private static int scoreMobility(int player, State4 s, EvalConstantsV2 c){
		int mScore = 0;
		
		final long enemyPawns = s.pawns[1-player];
		final long enemyPawnAttaks = State4.getLeftPawnAttacks(1-player, s.pieces, 0, enemyPawns) | 
				State4.getRightPawnAttacks(1-player, s.pieces, 0, enemyPawns);
		
		for(long bishops = s.bishops[player]; bishops != 0; bishops &= bishops-1){
			final long p = State4.getBishopMoves(player, s.pieces, bishops&-bishops) & ~enemyPawnAttaks;
			mScore += c.mobilityWeight[State4.PIECE_TYPE_BISHOP][mobilityIndex((int)BitUtil.getSetBits(p), 14)];
		}
		
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long p = State4.getKnightMoves(player, s.pieces, knights&-knights) & ~enemyPawnAttaks;
			mScore += c.mobilityWeight[State4.PIECE_TYPE_KNIGHT][mobilityIndex((int)BitUtil.getSetBits(p), 8)];
		}
		
		for(long queens = s.queens[player]; queens != 0; queens &= queens-1){
			final long p = State4.getQueenMoves(player, s.pieces, queens&-queens) & ~enemyPawnAttaks;
			mScore += c.mobilityWeight[State4.PIECE_TYPE_QUEEN][mobilityIndex((int)BitUtil.getSetBits(p), 28)];
		}
		
		for(long rooks = s.rooks[player]; rooks != 0; rooks &= rooks-1){
			final long p = State4.getRookMoves(player, s.pieces, rooks&-rooks) & ~enemyPawnAttaks;
			mScore += c.mobilityWeight[State4.PIECE_TYPE_ROOK][mobilityIndex((int)BitUtil.getSetBits(p), 15)];
		}
		
		return mScore;
	}

	private static int mobilityIndex(final int movement, final int maxMovement){
		final double d = movement*1./maxMovement;
		if(d < .33){
			return 0;
		} else if(d < .66){
			return 1;
		} else{
			return 2;
		}
	}
}
