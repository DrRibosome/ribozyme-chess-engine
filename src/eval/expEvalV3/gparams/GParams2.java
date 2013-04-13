package eval.expEvalV3.gparams;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public class GParams2 {

	private static Weight W(int start, int end){
		final int shift = 1;
		return new Weight(start >> shift, end >> shift);
	}
	
	private static Weight S(int start, int end){
		return new Weight(start, end);
	}
	
	public static EvalParameters buildEval(){
		EvalParameters p = new EvalParameters();
		
		//--------------------------------------------------------------
		//general weights
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 960;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 520;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 344;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 281;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(5, 12);

		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				S(-8,-7), S(-1,0), S(0,0), S(0,0), S(6,3), S(12,8), S(13,11), S(21,14), S(19,12)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				S(-2,-2), S(0,0), S(1,0), S(9,7), S(16,16), S(26,19), S(28,22), S(28,35), S(31,28), S(46,31), S(41,27), S(33,41), S(42,37), S(42,35), S(35,35), S(42,44)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				S(0,-9), S(0,-3), S(0,0), S(0,7), S(2,16), S(5,24), S(9,27), S(9,37), S(11,52), S(12,58), S(9,58), S(15,49), S(14,59), S(15,55), S(12,71), S(18,60)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				S(0,0), S(0,0), S(0,0), S(0,0), S(0,1), S(0,4),S(1,6), S(2,9), S(3,10), S(5,14), S(7,14), S(7,15), S(8,19), S(8,17), S(7,18), S(11,14), S(9,17), S(11,17), S(11,15), S(11,17), S(9,16), S(10,14), S(10,17), S(9,16), S(9,16), S(11,16), S(11,20), S(11,19), S(12,18), S(12,19), S(10,15), S(10,19)
		};
		mobilityWeights[State4.PIECE_TYPE_EMPTY] = new Weight[0];
		mobilityWeights[State4.PIECE_TYPE_KING] = new Weight[0];
		mobilityWeights[State4.PIECE_TYPE_PAWN] = new Weight[0];
		p.mobilityWeights = mobilityWeights;
		
		//---------------------------------------------------------------------
		//king values

		final int[][] kingDangerSquares = {
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
		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
		p.kingDangerSquares = kingDangerSquares;
		
		p.contactCheckQueen = 6;
		p.contactCheckRook = 4;
		p.queenCheck = 3;
		p.rookCheck = 2;
		p.knightCheck = 1;
		p.bishopCheck = 1;
		
		p.kingDangerValues = new Weight[128];
		final int maxSlope = 25;
		final int maxDanger = 800;
		for(int x = 0, i = 0; i < p.kingDangerValues.length; i++){
			x = Math.min(maxDanger, Math.min((i * i) / 2, x + maxSlope));
			p.kingDangerValues[i] = W(x, 0);
		}
		
		p.dangerKingAttacks = new int[]{
				0,
				0, 		//king
				3,   	//queen
				2,	    //rook
				1,	    //bishop
				1,	    //knight
				0		//pawn
		};
		
		//-------------------------------------------------------------------------
		//pawn values
		
		final Weight[][] passedPawnRowWeight = new Weight[][]{
				{
				new Weight(-999, -999), //unused
				S(5,6),
				S(15,10),
				S(32,21),
				S(52,34),
				S(77,50),
				S(113,69),
				new Weight(-999, -999)}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{S(-7,-19), S(-18,-10), S(-9,-23), S(-12,-11), S(-12,-11), S(-9,-23), S(-18,-10), S(-7,-19)},
				{S(-2,-2), S(-6,-4), S(-14,-4), S(-9,-7), S(-9,-7), S(-14,-4), S(-6,-4), S(-2,-2)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{
				{S(0,0), S(0,-1), S(0,-1), S(0,-2), S(0,-2), S(0,-1), S(0,-1), S(0,0)},
				{S(0,-2), S(0,-2), S(0,-4), S(0,-3), S(0,-3), S(0,-4), S(0,-2), S(0,-2)},
		};

		p.backwardPawns = new Weight[][]{
				{W(-30, -42), W(-43, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-43, -46), W(-30, -42)},
				{W(-20, -28), W(-29, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-29, -31), W(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				S(4,0), S(6,0), S(7,0), S(8,0),
			    S(8,0), S(7,0), S(6,0), S(4,0)
		};
		
		return p;
	}
}