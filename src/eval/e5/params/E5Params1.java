package eval.e5.params;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public class E5Params1 {

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
		
		/*king = 0
		queen = 889
		rook = 428
		bishop = 258
		knight = 153
		pawn = 100*/
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 764;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 470;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 286;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 301;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(14, 5);
		p.bishopPair = new Weight(10, 42);

		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				S(-19,-49), S(-13,-52), S(-6,-27), S(-1,0), S(7,2), S(12,10), S(14,28), S(16,44), S(17,48)
		};
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				S(-13,-30), S(-6,-20), S(1,-18), S(7,-10), S(15,-1), S(24,22), S(28,9), S(24,-23), S(38,31), S(38,23), S(34,41), S(43,31), S(29,32), S(55,37), S(35,36), S(38,38)
		};
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				S(-10,-69), S(-7,-47), S(-4,-43), S(-1,-10), S(2,13), S(5,26), S(7,35), S(10,43), S(11,50), S(12,56), S(11,47), S(13,61), S(14,62), S(15,58), S(15,74), S(17,64)
		};
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				S(-6,-69), S(-4,-45), S(-2,-49), S(-2,-28), S(-1,-9), S(0,10), S(1,15), S(2,20), S(4,25), S(5,30), S(6,30), S(7,30), S(8,30), S(8,30), S(9,30), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(10,35), S(8,35), S(10,35), S(10,35)
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
				{0, 30, 20, 8, 2, 0, 0},
				{0, 75, 38, 20, 5, 0, 0},
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
				S(1,1), S(4,7), S(25,16), S(39,27), S(101,60), S(82,48),
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{S(-19,-33), S(-28,-25), S(-29,-19), S(-24,-23), S(-24,-23), S(-29,-19), S(-28,-25), S(-19,-33)},
				//{W(-37, -45), W(-54, -52), W(-60, -52), W(-60, -52), W(-60, -52), W(-60, -52), W(-54, -52), W(-37, -45)},
				{S(-12,11), S(-21,-18), S(-20,-16), S(-22,-19), S(-22,-19), S(-20,-16), S(-21,-18), S(-12,11)},
				//{W(-25, -30), W(-36, -35), W(-40, -35), W(-40, -35), W(-40, -35), W(-40, -35), W(-36, -35), W(-25, -30)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{{
				W(-13/2, -43/2), W(-20/2, -48/2), W(-23/2, -48/2), W(-23/2, -48/2),
				W(-23/2, -48/2), W(-23/2, -48/2), W(-20/2, -48/2), W(-13/2, -43/2)
			},{
				W(-13/2, -43/2), W(-20/2, -48/2), W(-23/2, -48/2), W(-23/2, -48/2),
				W(-23/2, -48/2), W(-23/2, -48/2), W(-20/2, -48/2), W(-13/2, -43/2)
			},
		};

		//off - on
		//(w0,w1,d) = (154,159,190), d7, (-5,-13) and (-3,-9)
		//(w0,w1,d) = (53,58,89), d7, (-10,-20) and (-5,-14)
		p.backwardPawns = new Weight[][]{
				{S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),S(-10,-20),},
				{S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),S(-6,-13),},
		};
		
		p.pawnChain = new Weight[]{
				S(12,-9), S(15,-10), S(13,-4), S(15,0), S(15,0), S(13,-4), S(15,-10), S(12,-9)
		};
		
		return p;
	}
}