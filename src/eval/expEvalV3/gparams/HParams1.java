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
max ply = 5
processing game 473...
material weights:
	king = 0
	queen = 961
	rook = 508
	bishop = 1758
	knight = 20
	pawn = 100
mobility weights:
	king = 
	queen = 0:(-5,36), 1:(-4,56), 2:(-3,-47), 3:(-2,-14), 4:(-1,-23), 5:(0,15), 6:(1,13), 7:(2,-26), 8:(4,15), 9:(5,-2), 10:(6,20), 11:(7,12), 12:(8,12), 13:(8,10), 14:(9,15), 15:(10,8), 16:(10,16), 17:(10,9), 18:(10,16), 19:(10,20), 20:(10,17), 21:(10,16), 22:(10,17), 23:(10,17), 24:(10,17), 25:(10,17), 26:(10,17), 27:(10,17), 28:(10,17), 29:(10,17), 30:(10,17), 31:(10,17), 
	rook = 0:(-10,-5068), 1:(-7,2157), 2:(-4,-18), 3:(-1,12), 4:(2,9), 5:(4,29), 6:(8,33), 7:(8,60), 8:(11,53), 9:(12,53), 10:(13,64), 11:(14,63), 12:(14,59), 13:(15,61), 14:(15,52), 15:(16,59), 
	bishop = 0:(-13,159), 1:(-6,-4), 2:(1,-4), 3:(7,-4), 4:(16,8), 5:(22,36), 6:(28,-3), 7:(36,0), 8:(35,28), 9:(39,12), 10:(39,30), 11:(39,38), 12:(39,37), 13:(40,37), 14:(40,38), 15:(40,38), 
	knight = 0:(-19,-17), 1:(-13,-20), 2:(-7,-49), 3:(1,-1933), 4:(5,16), 5:(11,907), 6:(15,21), 7:(20,3), 8:(19,28), 
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
1:(1,-1), 2:(4,4), 3:(24,21), 4:(48,30), 5:(67,51), 6:(96,60), 
isolated pawn weight, unopposed (by col):
0:(-19,-18), 1:(-26,-27), 2:(-31,-23), 3:(-29,-37), 4:(-29,-37), 5:(-31,-23), 6:(-26,-27), 7:(-19,-18), 
isolated pawn weight, opposed (by col):
0:(-13,5), 1:(-20,-26), 2:(-20,-16), 3:(-20,-15), 4:(-20,-15), 5:(-20,-16), 6:(-20,-26), 7:(-13,5), 
doubled pawn weight, unopposed (by col):
0:(-4,24), 1:(-4,-29), 2:(-6,-33), 3:(-6,-13), 4:(-6,-13), 5:(-6,-33), 6:(-4,-29), 7:(-4,24), 
doubled pawn weight, opposed (by col):
0:(-3,-10), 1:(-5,-9), 2:(-6,1), 3:(-6,-9), 4:(-6,-9), 5:(-6,1), 6:(-5,-9), 7:(-3,-10), 
backward pawn weight, unopposed (by col):
0:(-15,-21), 1:(-22,-23), 2:(-25,-23), 3:(-25,-23), 4:(-25,-23), 5:(-25,-23), 6:(-22,-23), 7:(-15,-21), 
backward pawn weight, opposed (by col):
0:(-10,-14), 1:(-15,-16), 2:(-17,-16), 3:(-17,-16), 4:(-17,-16), 5:(-17,-16), 6:(-15,-16), 7:(-10,-14), 
pawn chain weight:
0:(1,30), 1:(3,-2), 2:(4,-24), 3:(5,-26), 4:(5,-26), 5:(4,-24), 6:(3,-2), 7:(1,30), 
pawn shelter:
  adj king file: 0:0, 1:30, 2:20, 3:8, 4:2, 5:0, 6:0, 
  in king file: 0:0, 1:70, 2:45, 3:20, 4:5, 5:0, 6:0, 
pawn storm:
  no allied: 0:-20, 1:-18, 2:-14, 3:-10, 4:-5, 5:0, 
  allied: 0:-15, 1:-13, 2:-10, 3:-6, 4:-5, 5:0, 
  blocked enemy: 0:-6, 1:-5, 2:-4, 3:-3, 4:-1, 5:0, 
tempo = (3,1)
bishop pair = (5,83)
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