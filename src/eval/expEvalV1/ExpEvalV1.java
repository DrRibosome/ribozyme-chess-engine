package eval.expEvalV1;

import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;

public final class ExpEvalV1 implements Evaluator2<State4>{
	private final static int[] zeroi7 = new int[7];
	
	private final int[] materialScore = new int[2];
	/** max number of moves by piece type*/
	private final int[][] maxMobility = new int[2][7];
	private final FeatureExtractor.FeatureSet fset = new FeatureExtractor.FeatureSet();
	
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
		FeatureExtractor.loadFeatures(fset, player, s, true);
		
		double score = materialScore[player];
		score += scoreMobility(player);
		score += scorePawns(player, fset, s);
		score += s.isCastled[player]*EvalConstantsV1.canCastleWeight;
		score += s.pieceCounts[player][State4.PIECE_TYPE_BISHOP] == 2?
				EvalConstantsV1.bishopPairWeight: 0;
		score += knightEntropy(player, s);
		
		return score;
	}
	
	private static double scorePawns(int player, FeatureExtractor.FeatureSet fset, State4 s){
		final int len = s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		double m = 0;
		for(int a = 0; a < len; a++){
			m += EvalConstantsV1.unopposedPawnWeight*fset.pawnUnopposed[a];
			m += EvalConstantsV1.passedPawnWeight*fset.pawnPassed[a];
			if(fset.pawnPassed[a] != 0){
				m += EvalConstantsV1.pawnRowBonus[player][fset.pawnRow[a]];
				m += EvalConstantsV1.supportedPassedPawn * fset.supportedPawn[a];
			} else{
				//m += EvalConstantsV1.supportedPawn * fset.supportedPawn[a];
			}
		}
		
		m += EvalConstantsV1.doubledPawnsWeight*fset.doubledPawns;
		m += EvalConstantsV1.tripledPawnsWeight*fset.tripledPawns;
		
		return m;
	}
	
	private static double knightEntropy(int player, State4 s){
		double m = 0;
		for(long knights = s.knights[player]; knights != 0; knights &= knights-1){
			final long k = knights & -knights;
			final int index = BitUtil.lsbIndex(k);
			final long attacked = s.pieces[1-player] & Masks.knightMoves[index];
			final long count = BitUtil.getSetBits(attacked);
			if(count <= 2){
				m += EvalConstantsV1.knightEntropyWeight[0];
			} else if(count <= 4){
				m += EvalConstantsV1.knightEntropyWeight[1];
			} else{
				m += EvalConstantsV1.knightEntropyWeight[2];
			}
		}
		return m;
	}
	
	private double scoreMobility(int player){
		double m = 0;
		for(int a = 1; a < 7; a++){
			double p = fset.mobility[a]*1./maxMobility[player][a];
			if(p < .3){ //low mobility
				m += EvalConstantsV1.mobilityBonus[a][0];
			} else if(p <= .66){
				m += EvalConstantsV1.mobilityBonus[a][1];
			} else{
				m += EvalConstantsV1.mobilityBonus[a][2];
			}
		}
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
	
	private void update(long encoding, boolean undo){
		final int dir = undo? -1: 1;
		final int player = MoveEncoder.getPlayer(encoding);
		final int taken = MoveEncoder.getTakenType(encoding);
		if(taken != 0){
			materialScore[1-player] -= dir*EvalConstantsV1.materialWeights[taken];
			maxMobility[1-player][taken] -= dir*EvalConstantsV1.pieceMobility[taken];
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*EvalConstantsV1.materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(EvalConstantsV1.materialWeights[State4.PIECE_TYPE_QUEEN]-
					EvalConstantsV1.materialWeights[State4.PIECE_TYPE_PAWN]);
			maxMobility[1-player][State4.PIECE_TYPE_QUEEN] +=
					dir*EvalConstantsV1.pieceMobility[State4.PIECE_TYPE_QUEEN];
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
			materialScore[a] += s.pieceCounts[a][b] * EvalConstantsV1.materialWeights[b];
			maxMobility[a][b] += s.pieceCounts[a][b] * EvalConstantsV1.pieceMobility[b];
			
			final int n = State4.PIECE_TYPE_KNIGHT;
			materialScore[a] += s.pieceCounts[a][n] * EvalConstantsV1.materialWeights[n];
			maxMobility[a][n] += s.pieceCounts[a][n] * EvalConstantsV1.pieceMobility[n];
			
			final int q = State4.PIECE_TYPE_QUEEN;
			materialScore[a] += s.pieceCounts[a][q] * EvalConstantsV1.materialWeights[q];
			maxMobility[a][q] += s.pieceCounts[a][q] * EvalConstantsV1.pieceMobility[q];
			
			final int r = State4.PIECE_TYPE_ROOK;
			materialScore[a] += s.pieceCounts[a][r] * EvalConstantsV1.materialWeights[r];
			maxMobility[a][r] += s.pieceCounts[a][r] * EvalConstantsV1.pieceMobility[r];
			
			final int p = State4.PIECE_TYPE_PAWN;
			materialScore[a] += s.pieceCounts[a][p] * EvalConstantsV1.materialWeights[p];
		}
	}

}
