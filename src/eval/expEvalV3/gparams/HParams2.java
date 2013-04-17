package eval.expEvalV3.gparams;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public class HParams2 {
	private static Weight S(int start, int end){
		return new Weight(start, end);
	}
	
	public static EvalParameters buildEval(){
		EvalParameters p = new EvalParameters();
		
		//--------------------------------------------------------------
		//general weights
		
		/*

	queen = 1034
	rook = 493
	bishop = 422
	knight = 296
	pawn = 100
mobility weights:
	king = 
	queen = S(-5,-7), S(-4,-10), S(-3,-14), S(-2,-20), S(-1,7), S(0,13), S(1,-1), S(2,11), S(4,17), S(5,21), S(6,19), S(7,19), S(8,16), S(8,19), S(9,15), S(10,12), S(10,18), S(10,21), S(10,14), S(10,19), S(10,16), S(10,15), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), 
	rook = S(-10,-29), S(-7,-9), S(-4,-11), S(-1,5), S(2,14), S(5,29), S(7,40), S(9,43), S(11,50), S(13,54), S(13,49), S(14,63), S(14,52), S(15,62), S(15,61), S(16,59), 
	bishop = S(-13,-9), S(-6,-9), S(1,-1), S(8,15), S(15,15), S(22,12), S(28,29), S(32,33), S(35,31), S(37,32), S(38,33), S(39,32), S(39,37), S(40,37), S(40,38), S(40,38), 
	knight = S(-19,-20), S(-13,-13), S(-6,-11), S(0,-2), S(6,7), S(12,2), S(15,19), S(19,19), S(19,14), 
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
S(0,0), S(0,0), S(1,0), S(2,0), S(4,0), S(6,0), S(9,0), S(12,0), S(16,0), S(20,0), S(25,0), S(30,0), S(36,0), S(42,0), S(49,0), S(56,0), S(64,0), S(72,0), S(81,0), S(90,0), S(100,0), S(110,0), S(121,0), S(132,0), S(144,0), S(156,0), S(168,0), S(181,0), S(193,0), S(206,0), S(218,0), S(231,0), S(243,0), S(256,0), S(268,0), S(281,0), S(293,0), S(306,0), S(318,0), S(331,0), S(343,0), S(356,0), S(368,0), S(381,0), S(393,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), S(400,0), 
king danger squares (white perspective):
2	0	2	3	3	2	0	2	
2	2	4	8	8	4	2	2	
7	10	12	12	12	12	10	7	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
15	15	15	15	15	15	15	15	
passed pawn row weight (white perspective):
S(0,0), S(5,3), S(23,15), S(44,37), S(67,43), S(96,64), 
isolated pawn weight, unopposed (by col):
S(-19,-16), S(-27,-25), S(-30,-25), S(-30,-29), S(-30,-29), S(-30,-25), S(-27,-25), S(-19,-16), 
isolated pawn weight, opposed (by col):
S(-13,-19), S(-18,-20), S(-20,-18), S(-20,-20), S(-20,-20), S(-20,-18), S(-18,-20), S(-13,-19), 
doubled pawn weight, unopposed (by col):
S(-3,-4), S(-5,-15), S(-6,-9), S(-6,-14), S(-6,-14), S(-6,-9), S(-5,-15), S(-3,-4), 
doubled pawn weight, opposed (by col):
S(-3,-11), S(-5,-12), S(-6,-14), S(-6,-11), S(-6,-11), S(-6,-14), S(-5,-12), S(-3,-11), 
backward pawn weight, unopposed (by col):
S(-15,-21), S(-22,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-22,-23), S(-15,-21), 
backward pawn weight, opposed (by col):
S(-10,-14), S(-15,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-15,-16), S(-10,-14), 
pawn chain weight:
S(2,-5), S(3,8), S(4,-5), S(5,6), S(5,6), S(4,-5), S(3,8), S(2,-5), 
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
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 1034;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 493;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 422;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 296;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(5, 12);

		//note, all values here divided by 8
		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				S(-19,-20), S(-13,-13), S(-6,-11), S(0,-2), S(6,7), S(12,2), S(15,19), S(19,19), S(19,14)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				S(-13,-9), S(-6,-9), S(1,-1), S(8,15), S(15,15), S(22,12), S(28,29), S(32,33), S(35,31), S(37,32), S(38,33), S(39,32), S(39,37), S(40,37), S(40,38), S(40,38)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				S(-10,-29), S(-7,-9), S(-4,-11), S(-1,5), S(2,14), S(5,29), S(7,40), S(9,43), S(11,50), S(13,54), S(13,49), S(14,63), S(14,52), S(15,62), S(15,61), S(16,59)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				S(-5,-7), S(-4,-10), S(-3,-14), S(-2,-20), S(-1,7), S(0,13), S(1,-1), S(2,11), S(4,17), S(5,21), S(6,19), S(7,19), S(8,16), S(8,19), S(9,15), S(10,12), S(10,18), S(10,21), S(10,14), S(10,19), S(10,16), S(10,15), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17)
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
				S(0,0), S(5,3), S(23,15), S(44,37), S(67,43), S(96,64), 
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{S(-19,-16), S(-27,-25), S(-30,-25), S(-30,-29), S(-30,-29), S(-30,-25), S(-27,-25), S(-19,-16)},
				{S(-13,-19), S(-18,-20), S(-20,-18), S(-20,-20), S(-20,-20), S(-20,-18), S(-18,-20), S(-13,-19)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{
				{S(-3,-4), S(-5,-15), S(-6,-9), S(-6,-14), S(-6,-14), S(-6,-9), S(-5,-15), S(-3,-4)},
				{S(-3,-11), S(-5,-12), S(-6,-14), S(-6,-11), S(-6,-11), S(-6,-14), S(-5,-12), S(-3,-11)},
		};

		p.backwardPawns = new Weight[][]{
				{S(-30, -42), S(-43, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-43, -46), S(-30, -42)},
				{S(-20, -28), S(-29, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-29, -31), S(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				S(2,-5), S(3,8), S(4,-5), S(5,6), S(5,6), S(4,-5), S(3,8), S(2,-5)
		};
		
		return p;
	}
}