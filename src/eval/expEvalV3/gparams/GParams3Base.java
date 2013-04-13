package eval.expEvalV3.gparams;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;


public class GParams3Base {

	private static Weight W(int start, int end){
		return new Weight(start, end);
	}
	
	public static EvalParameters buildEval(){
		EvalParameters p = new EvalParameters();
		
		//--------------------------------------------------------------
		//general weights
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 954;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 468;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 297;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 290;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(4, 14);

		//note, all values here divided by 8
		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				W(-2,1), W(0,1), W(0,0), W(0,0), W(6,3), W(11,9), W(15,11), W(21,13), W(19,14)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				W(0,1), W(0,0), W(1,0), W(8,6), W(16,14), W(18,21), W(30,32), W(33,31), W(34,33), W(33,31), W(38,37), W(42,34), W(41,40), W(40,38), W(39,42), W(39,40)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				W(0,1), W(0,0), W(0,0), W(0,6), W(2,15), W(5,21), W(7,35), W(10,34), W(9,46), W(14,51), W(13,48), W(15,53), W(14,59), W(15,50), W(16,60), W(14,59)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				W(0,0), W(0,0), W(0,0), W(0,0), W(0,1), W(0,4), W(1,5), W(2,9), W(5,10), W(5,15), W(6,16), W(7,14), W(8,15), W(8,17), W(9,19), W(11,17), W(10,19), W(11,18), W     (10,15), W(9,18), W(10,19), W(10,16), W(9,19), W(10,17), W(10,18), W(11,16), W(10,18), W(10,17), W(10,16), W(10,17), W(11,19), W(10,18)
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
		
		p.pawnShelter = new int[][]{ //only need 7 indeces, pawn cant be on last row
				{0, 55, 35, 20, 8, 0, 0},
				{0, 85, 45, 25, 15, 0, 0},
				//{0, 60, 40, 25, 10, 0, 0},
				//{0, 130, 70, 35, 15, 0, 0},
		};
		
		p.pawnStorm = new int[][]{ //indexed [type][distance]
				{-20, -18, -14, -10, -5, 0}, //no allied pawn
				{-15, -13, -10, -6, -5, 0}, //has allied pawn, enemy not blocked
				{-6, -5, -4, -3, -1, 0}, //enemy pawn blocked by allied pawn
		};
		
		//-------------------------------------------------------------------------
		//pawn values
		
		final Weight[][] passedPawnRowWeight = new Weight[][]{
				{
				new Weight(-999, -999), //unused
				W(5,6), W(13,13), W(31,21), W(49,35), W(70,44), W(105,77),
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{W(0,-4), W(-3,-10), W(-5,-4), W(-11,-2), W(-11,-2), W(-5,-4), W(-3,-10), W(0,-4)},
				{W(0,2), W(1,0), W(1,0), W(-1,1), W(-1,1), W(1,0), W(1,0), W(0,2)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{
				{W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0)},
				{W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0), W(-5,0)},
		};

		p.backwardPawns = new Weight[][]{
				{W(-30, -42), W(-43, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-43, -46), W(-30, -42)},
				{W(-20, -28), W(-29, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-29, -31), W(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				W(5,0), W(6,0), W(8,0), W(10,0),
			    W(10,0), W(8,0), W(6,0), W(5,0)
		};
		
		return p;
	}
}