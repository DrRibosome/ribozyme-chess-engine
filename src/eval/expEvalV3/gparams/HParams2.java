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
max ply = 12
processing game 421...
material weights:
	king = 0
	queen = 810
	rook = 548
	bishop = 1463
	knight = 28
	pawn = 100
mobility weights:
	king = 
	queen = S(-5,78), S(-4,-1), S(-3,-83), S(-2,-35), S(-1,-9), S(0,10), S(1,42), S(2,4), S(4,32), S(5,1), S(6,18), S(7,14), S(8,3), S(8,13), S(9,29), S(10,15), S(10,11), S(10,17), S(10,17), S(10,17), S(10,18), S(10,21), S(10,17), S(10,16), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), S(10,17), 
	rook = S(-10,-2420), S(-7,1714), S(-4,-45), S(-1,-10), S(2,13), S(5,27), S(7,32), S(10,51), S(11,53), S(12,46), S(12,68), S(14,64), S(14,59), S(15,56), S(15,65), S(16,59), 
	bishop = S(-13,40), S(-6,2), S(1,18), S(7,-10), S(15,-1), S(23,23), S(29,9), S(35,-22), S(35,32), S(39,23), S(36,39), S(39,36), S(39,37), S(40,37), S(40,38), S(40,38), 
	knight = S(-19,-17), S(-13,-47), S(-6,-30), S(-1,-1007), S(7,2), S(12,324), S(14,27), S(19,42), S(17,17), 
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
S(1,1), S(4,7), S(25,16), S(45,40), S(71,60), S(96,54), 
isolated pawn weight, unopposed (by col):
S(-19,-26), S(-26,-25), S(-31,-20), S(-28,-28), S(-28,-28), S(-31,-20), S(-26,-25), S(-19,-26), 
isolated pawn weight, opposed (by col):
S(-12,11), S(-18,-18), S(-20,-16), S(-22,-21), S(-22,-21), S(-20,-16), S(-18,-18), S(-12,11), 
doubled pawn weight, unopposed (by col):
S(-2,-4), S(-4,-30), S(-7,-25), S(-5,6), S(-5,6), S(-7,-25), S(-4,-30), S(-2,-4), 
doubled pawn weight, opposed (by col):
S(-3,-10), S(-5,-16), S(-6,1), S(-6,-11), S(-6,-11), S(-6,1), S(-5,-16), S(-3,-10), 
backward pawn weight, unopposed (by col):
S(-15,-21), S(-22,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-25,-23), S(-22,-23), S(-15,-21), 
backward pawn weight, opposed (by col):
S(-10,-14), S(-15,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-17,-16), S(-15,-16), S(-10,-14), 
pawn chain weight:
S(2,-9), S(5,-10), S(3,-4), S(5,-2), S(5,-2), S(3,-4), S(5,-10), S(2,-9), 
pawn shelter:
  adj king file: S0, S30, S20, S8, S2, S0, S0, 
  in king file: S0, S70, S45, S20, S5, S0, S0, 
pawn storm:
  no allied: S-20, S-18, S-14, S-10, S-5, S0, 
  allied: S-15, S-13, S-10, S-6, S-5, S0, 
  blocked enemy: S-6, S-5, S-4, S-3, S-1, S0, 
tempo = (3,1)
bishop pair = (5,53)


		 * 
		 */
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 810;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 548;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 340;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 290;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(5, 53);

		//note, all values here divided by 8
		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				S(-19,-50), S(-13,-45), S(-6,-30), S(-1,0), S(7,2), S(12,10), S(14,27), S(15,42), S(17,45)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				S(-13,-30),	S(-6,-20), 	S(1,-15), 	S(7,-10), 	S(15,-1), 	S(23,23),
				S(29,9), 	S(35,-22), 	S(35,32), 	S(39,23), 	S(36,39), 	S(39,36),
				S(39,37), 	S(40,37), 	S(40,38), 	S(40,38)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				S(-10,-70),	S(-7,-50),	S(-4,-45),	S(-1,-10),	S(2,13),	S(5,27),
				S(7,32),	S(10,51), 	S(11,53), 	S(12,55),	S(12,60), 	S(14,64),
				S(14,65),	S(15,66), 	S(15,67), 	S(16,68)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				S(-5,-70),	S(-4,-50),	S(-3,-40),	S(-2,-35),	S(-1,-9),	S(0,10),
				S(1,42),	S(2,4),		S(4,32),	S(5,1),		S(6,18),	S(7,14),
				S(8,3),		S(8,13),	S(9,29),	S(10,35),	S(10,40),	S(10,40),
				S(10,40),	S(10,40),	S(10,40),	S(10,40),	S(10,40),	S(10,40),
				S(10,40),	S(10,40), 	S(10,40), 	S(10,40), 	S(10,40), 	S(10,40), 
				S(10,40), 	S(10,40)
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
				S(1,1), S(4,7), S(25,16), S(45,40), S(71,60), S(96,54), 
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{S(-19,-26), S(-26,-25), S(-31,-20), S(-28,-28), S(-28,-28), S(-31,-20), S(-26,-25), S(-19,-26)},
				{S(-12,11), S(-18,-18), S(-20,-16), S(-22,-21), S(-22,-21), S(-20,-16), S(-18,-18), S(-12,11)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{
				{S(-2,-4), S(-4,-30), S(-7,-25), S(-5,6), S(-5,6), S(-7,-25), S(-4,-30), S(-2,-4)},
				{S(-3,-10), S(-5,-16), S(-6,1), S(-6,-11), S(-6,-11), S(-6,1), S(-5,-16), S(-3,-10)},
		};

		p.backwardPawns = new Weight[][]{
				{S(-30, -42), S(-43, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-49, -46), S(-43, -46), S(-30, -42)},
				{S(-20, -28), S(-29, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-33, -31), S(-29, -31), S(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				S(2,-9), S(5,-10), S(3,-4), S(5,-2), S(5,-2), S(3,-4), S(5,-10), S(2,-9)
		};
		
		return p;
	}
}