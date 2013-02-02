package eval.incrPieceScore;

import eval.Evaluator2;
import util.board4.MoveEncoder;
import util.board4.State4;

/** raw piece score eval, appears to have very slight performance increase over
 * non-incremental piece score eval*/
public final class IncrementalPieceScore implements Evaluator2<State4>{
	private final int[] materialScore = new int[2];
	final static int[] materialWeights;
	
	static{
		materialWeights = new int[7];
		materialWeights[State4.PIECE_TYPE_BISHOP] = 3;
		materialWeights[State4.PIECE_TYPE_KNIGHT] = 3;
		materialWeights[State4.PIECE_TYPE_ROOK] = 5;
		materialWeights[State4.PIECE_TYPE_QUEEN] = 9;
		materialWeights[State4.PIECE_TYPE_PAWN] = 1;
	}
	
	@Override
	public double eval(State4 state, int player) {
		return materialScore[player]-materialScore[1-player];
	}

	@Override
	public double lazyEval(State4 state, int player) {
		return materialScore[player]-materialScore[1-player];
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
			materialScore[1-player] -= dir*materialWeights[taken];
		} else if(MoveEncoder.isEnPassanteTake(encoding) != 0){
			materialScore[1-player] -= dir*materialWeights[State4.PIECE_TYPE_PAWN];
		}
		if(MoveEncoder.isPawnPromoted(encoding)){
			materialScore[player] += dir*(materialWeights[State4.PIECE_TYPE_QUEEN]-
					materialWeights[State4.PIECE_TYPE_PAWN]);
		}
	}

	@Override
	public void initialize(State4 s) {
		//initialize raw material scores
		materialScore[0] = 0;
		materialScore[1] = 0;
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

}
