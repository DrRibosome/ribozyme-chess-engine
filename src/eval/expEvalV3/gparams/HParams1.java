package eval.expEvalV3.gparams;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public class HParams1 {
	private static Weight S(int start, int end){
		return new Weight(start, end);
	}
	
	public static EvalParameters buildEval(){
		EvalParameters p = new EvalParameters();
		
		//--------------------------------------------------------------
		//general weights
		
		/*
	queen = 1057
	rook = 635
	bishop = 425
	knight = 210
	pawn = 100
mobility weights:
	king = 
	queen = S(-5,-4), S(-4,0), S(-3,-17), S(-2,-12), S(-1,-4), S(0,6), S(1,4), S(2,-1), S(4,12), S(5,30), S(6,17), S(7,24), S(8,12), S(8,20), S(9,17), S(10,18), S(10,17), S(10,21), S(10,13), S(10,17), S(10,17), S(10,17), S(10,17), S(10,18), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), 
	rook = S(-10,-36), S(-7,3), S(-5,-11), S(-2,0), S(3,7), S(4,33), S(6,26), S(8,34), S(12,45), S(14,50), S(13,50), S(13,60), S(13,55), S(16,64), S(15,61), S(16,59), 
	bishop = S(-13,-10), S(-6,-5), S(1,5), S(8,9), S(15,17), S(22,20), S(28,29), S(32,31), S(35,24), S(37,23), S(38,40), S(39,30), S(39,37), S(40,37), S(40,38), S(40,38), 
	knight = S(-19,-18), S(-13,-19), S(-6,-17), S(0,-20), S(6,-1), S(12,19), S(15,-11), S(19,20), S(19,13), 
	pawn = 
danger king attacks:
	king = 0
	queen = 3
	rook = 2
	bishop = 1
	knight = 1
	pawn = 0
contact check, queen = 6
contact check, rook = 4
king danger function:
king danger squares (white perspective):
2	0	2	3	3	2	0	2	
2	2	4	8	8	4	2	2	
7	10	12	12	12	12	10	7	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
passed pawn row weight (white perspective):
S(1,5), S(5,3), S(24,16), S(43,37), S(67,40), S(96,58), 
isolated pawn weight, unopposed (by col):
S(-18,-20), S(-27,-28), S(-29,-22), S(-31,-30), S(-31,-30), S(-29,-22), S(-27,-28), S(-18,-20), 
isolated pawn weight, opposed (by col):
S(-14,-10), S(-18,-18), S(-20,-15), S(-19,-18), S(-19,-18), S(-20,-15), S(-18,-18), S(-14,-10), 
doubled pawn weight, unopposed (by col):
S(-2,-7), S(-4,-20), S(-7,-10), S(-7,-15), S(-7,-15), S(-7,-10), S(-4,-20), S(-2,-7), 
doubled pawn weight, opposed (by col):
S(-3,-11), S(-5,-10), S(-6,-12), S(-6,-11), S(-6,-11), S(-6,-12), S(-5,-10), S(-3,-11), 
backward pawn weight, unopposed (by col):
S(-15,-21), S(-22,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-22,-23), S(-15,-21), 
backward pawn weight, opposed (by col):
S(-10,-14), S(-15,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-15,-16), S(-10,-14), 
pawn chain weight:
S(1,3), S(3,8), S(5,3), S(6,11), S(6,11), S(5,3), S(3,8), S(1,3), 
pawn shelter:
  adj king file: S0, S30, S20, S8, S2, S0, S0, 
  in king file: S0, S70, S45, S20, S5, S0, S0, 
pawn storm:
  no allied: S-20, S-18, S-14, S-10, S-5, S0, 
  allied: S-15, S-13, S-10, S-6, S-5, S0, 
  blocked enemy: S-6, S-5, S-4, S-3, S-1, S0, 
tempo = (3,1)
bishop pair = (5,17)
		 * 
		 */
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 1057;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 635;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 425;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 210;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(5, 12);

		//note, all values here divided by 8
		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				S(-19,-18), S(-13,-19), S(-6,-17), S(0,-20), S(6,-1), S(12,19), S(15,-11), S(19,20), S(19,13)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				S(-13,-10), S(-6,-5), S(1,5), S(8,9), S(15,17), S(22,20), S(28,29), S(32,31), S(35,24), S(37,23), S(38,40), S(39,30), S(39,37), S(40,37), S(40,38), S(40,38)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				S(-10,-36), S(-7,3), S(-5,-11), S(-2,0), S(3,7), S(4,33), S(6,26), S(8,34), S(12,45), S(14,50), S(13,50), S(13,60), S(13,55), S(16,64), S(15,61), S(16,59)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				S(-5,-4), S(-4,0), S(-3,-17), S(-2,-12), S(-1,-4), S(0,6), S(1,4), S(2,-1), S(4,12), S(5,30), S(6,17), S(7,24), S(8,12), S(8,20), S(9,17), S(10,18), S(10,17), S(10,21), S(10,13), S(10,17), S(10,17), S(10,17), S(10,17), S(10,18), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17)
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
			p.kingDangerValues[i] = new Weight(x>>>1, 0);
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
				{0, 30, 20, 8, 2, 0, 0},
				{0, 70, 45, 20, 5, 0, 0},
				//{0, 61, 45, 17, 5, 0, 0},
				//{0, 141, 103, 39, 13, 0, 0},
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
				S(1,5), S(5,3), S(24,16), S(43,37), S(67,40), S(96,58),
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{S(-18,-20), S(-27,-28), S(-29,-22), S(-31,-30), S(-31,-30), S(-29,-22), S(-27,-28), S(-18,-20)},
				{S(-14,-10), S(-18,-18), S(-20,-15), S(-19,-18), S(-19,-18), S(-20,-15), S(-18,-18), S(-14,-10)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{
				{S(-2,-7), S(-4,-20), S(-7,-10), S(-7,-15), S(-7,-15), S(-7,-10), S(-4,-20), S(-2,-7)},
				{S(-3,-11), S(-5,-10), S(-6,-12), S(-6,-11), S(-6,-11), S(-6,-12), S(-5,-10), S(-3,-11)},
		};

		p.backwardPawns = new Weight[][]{
				{S(-30, -42), S(-43, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-43, -46), S(-30, -42)},
				{S(-20, -28), S(-29, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-29, -31), S(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				S(1,3), S(3,8), S(5,3), S(6,11), S(6,11), S(5,3), S(3,8), S(1,3)
		};
		
		return p;
	}
}