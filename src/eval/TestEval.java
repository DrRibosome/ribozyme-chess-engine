package eval;

import util.board4.BitUtil;
import util.board4.State4;
import ai.modularAI2.Evaluator2;

public final class TestEval implements Evaluator2<State4>{

	private final static int[] maxPotentials = new int[7];
	
	/** max endgame potential*/
	private final int[] maxPotential = new int[2];
	private final int[] currentPotential = new int[2];
	
	static{
		maxPotentials[State4.PIECE_TYPE_QUEEN] = 15+14;
		maxPotentials[State4.PIECE_TYPE_ROOK] = 15;
		maxPotentials[State4.PIECE_TYPE_BISHOP] = 14;
		maxPotentials[State4.PIECE_TYPE_KNIGHT] = 8;
	}
	
	@Override
	public double eval(State4 s, int player) {
		return score(s, player)-score(s, 1-player);
		/*double s1 = score(s, player);
		double s2 = score(s, 1-player);
		return (s1-s2)/s1;*/
	}
	
	private void calcPotential(State4 s, int player){
		currentPotential[player] = 0;
		maxPotential[player] = 0;
		
		long[] queens = s.queens;
		for(long q = queens[player]; q != 0; q&=q-1){
			long moves = State4.getQueenMoves(player, s.pieces, q);
			currentPotential[player] += BitUtil.getSetBitsCount(moves);
			maxPotential[player] += maxPotentials[State4.PIECE_TYPE_QUEEN];
		}
		
		long[] rooks = s.rooks;
		for(long q = rooks[player]; q != 0; q&=q-1){
			long moves = State4.getRookMoves(player, s.pieces, q);
			currentPotential[player] += BitUtil.getSetBitsCount(moves);
			maxPotential[player] += maxPotentials[State4.PIECE_TYPE_ROOK];
		}
		
		long[] knights = s.knights;
		for(long q = knights[player]; q != 0; q&=q-1){
			long moves = State4.getKnightMoves(player, s.pieces, q);
			currentPotential[player] += BitUtil.getSetBitsCount(moves) + 2;
			maxPotential[player] += maxPotentials[State4.PIECE_TYPE_KNIGHT] + 2;
		}
		
		long[] bishops = s.bishops;
		for(long q = bishops[player]; q != 0; q&=q-1){
			long moves = State4.getBishopMoves(player, s.pieces, q);
			currentPotential[player] += BitUtil.getSetBitsCount(moves);
			maxPotential[player] += maxPotentials[State4.PIECE_TYPE_BISHOP];
		}
	}
	
	private double score(State4 s, int player){
		calcPotential(s, player);
		double score = currentPotential[player]*1./maxPotential[player] + maxPotential[player];
		score += s.pieceCounts[player][State4.PIECE_TYPE_PAWN];
		
		
		return score;
	}

	@Override
	public double lazyEval(State4 state, int player) {
		return eval(state, player);
	}

	@Override
	public void processMove(long encoding) {
	}

	@Override
	public void undoMove(long encoding) {
	}

	@Override
	public void initialize(State4 state) {
	}

}
