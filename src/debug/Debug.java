package debug;

import search.Search4;
import search.search33.Search33v3;
import state4.BitUtil;
import state4.Masks;
import state4.State4;
import state4.StateUtil;
import uci.Position;
import util.FenParser;
import eval.Evaluator2;
import eval.e7.E7v3;


public class Debug {
	public static void main(String[] args){
		char[][] c = new char[][]{
				{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
				{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
				{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
				{'0', '0', '1', '1', '1', '1', 'w'}
		};
		char[][] c2 = new char[][]{
				{'R', ' ', ' ', ' ', ' ', 'Q', ' ', ' '},
				{'P', ' ', ' ', 'B', 'r', ' ', 'K', 'P'},
				{' ', ' ', 'P', ' ', ' ', ' ', ' ', 'n'},
				{' ', ' ', ' ', ' ', ' ', 'P', ' ', ' '},
				{' ', ' ', ' ', ' ', ' ', 'P', ' ', ' '},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{'p', 'p', ' ', ' ', ' ', 'p', 'p', 'p'},
				{'r', ' ', ' ', ' ', ' ', ' ', 'k', ' '},
				{'0', '0', '1', '1', '1', '1', 'w'}
		};

		//int player = State4.BLACK;
		//State4 s = loadConfig(c);
		//State4 s = loadConfig(c2);
		
		//Position p = FenParser.parse("7r/p1pk4/5p2/1p1r2p1/7p/P3PN1P/1P3KPB/2R5 b - - - -");
		//Position p = FenParser.parse("8/8/2p3p1/2pp1b2/1n1k4/1P3P2/1P1K4/R7 w - - - -");
		//Position p = FenParser.parse("2r1r1k1/p4ppp/3Bp3/2P5/1p2P2P/6Q1/qb3PP1/1R1R2K1 w - - - -"); //bm Rb2
		//Position p = FenParser.parse("1q1rkb1r/pp2pppp/2n2n2/1N1P4/5P2/4BB2/PP3P1P/R2Q1RK1 b - - - -");
		//Position p = FenParser.parse("r1b1kb1r/ppp1qppp/2n5/1B1n4/3Q4/2P2N2/PP3PPP/RNB2K1R b - - - -");
		//Position p = FenParser.parse("2kr1b1r/ppp3pp/4qp2/3P4/3Q3B/5N2/PP1N1PPP/5K1R b - - - -");
		//Position p = FenParser.parse("7r/p1pk4/5p2/1p1r2p1/7p/P3PN1P/1P3KPB/2R5 b - - - -");
		//Position p = FenParser.parse("1r5k/1P3pp1/B3pn1p/8/R7/1r3P2/5P1P/R4K2 w - - - -");
		//Position p = FenParser.parse("2r2bk1/pp3p2/1n2q2B/1P3N1Q/2p5/4P3/P4PP1/3R2K1 b - - - -");
		//Position p = FenParser.parse("8/4kp2/p2b1r2/2p1Q3/P1P2p1P/1P6/5PP1/1R4K1 b - - - -");
		//Position p = FenParser.parse("r1qnk2r/1ppb1ppp/4p2n/p2PP1NP/2P1B1P1/P1B2P2/8/1R1QK2R b KQkq - 0 20");
		Position p = FenParser.parse("2k5/pp1r2b1/2p5/7P/2P2r1q/5pN1/PPb2P1P/2Q1RRK1 w - - 0 27"); //c1c2 leads to loss by checkmate
		//Position p = FenParser.parse("r1bq1rk1/p1pp1ppp/2p5/3nP3/8/2B5/PPPQ1PPP/R3KB1R w - - - -"); //c4d3 blunder
		//Position p = FenParser.parse("1r2r2k/p1b2pp1/Q1p5/2P5/P2Pp2p/4BqP1/R4P1P/5RK1 w - - 0 24"); //missed mate threat on low depths
		//Position p = FenParser.parse("2r2rkn/pp3p1p/1q2p1pP/3pP1N1/b1nP4/P2B1QP1/1PN2P2/1R2K2R b - - - -"); //missed mate threat on depth 10, choose c4b2
		//Position p = FenParser.parse("5rk1/2pbQp1p/n3p1p1/4P3/r3B3/5P2/3B2PP/5RK1 b - - 0 27");
		
		System.out.println(StateUtil.fen(p.sideToMove, p.s));
		State4 s = p.s;
		int player = p.sideToMove;
		
		System.out.println(s);
		Evaluator2 e = new E7v3();
		//Evaluator2<State4> e = new IncrementalPieceScore();
		
		//e.initialize(s);
		//e.traceEval(s);
		
		System.out.println("\n");
		
		final int maxDepth = 40;
		Search4 search = new Search33v3(e, 20, true);
		int[] move = new int[2];
		search.search(player, s, move, maxDepth);
		System.out.println("\n"+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
		System.out.println("nodes searched = "+search.getStats().nodesSearched);
		System.out.println("hash hit rate = "+search.getStats().hashHits*1./search.getStats().nodesSearched);
		System.out.println("branching factor = "+search.getStats().empBranchingFactor);
		
		
		System.out.println(s);
		
		//TimerThread3.search(new SearchS4V30(maxDepth, s, e, 20, false), s, State4.WHITE, 1000*60*3, 0, move);
		
		//a.getMove(move);
		//System.out.println(getMoveString(move, 0)+" -> "+getMoveString(move, 2));
		
		/*int player = State4.BLACK;
		System.out.println("is attacked = "+State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s));*/
		
		//test en passant
		/*s.executeMove(1, 0, 1, 0, 0);
		System.out.println(s);
		System.out.println(Masks.getString(s.enPassante));*/
		
		//printPawnMoves(s, 1);
		//printRookMoves(s, 1);
		//printBishopMoves(s, 1);
		//printQueenMoves(s, 1);
		//printKingMoves(s, 0);
		//printKnightMoves(s, 1);
	}
	
	private static void printKnightMoves(State4 s, int player){
		System.out.println("knight moves");
		long knights = s.knights[player];
		char piece = player == 0? 'n': 'N';
		while(knights != 0){
			int start = BitUtil.lsbIndex(knights);
			//long moves = Masks.knightMoves[start] & (~(s.pieces[0]|s.pieces[1]) | s.pieces[1-player]);
			long moves = State4.getKnightMoves(player, s.pieces, knights);
			
			//System.out.println(Masks.getString(moves));
			knights = knights & (knights-1);
			while(moves != 0){
				int index = BitUtil.lsbIndex(moves);
				moves = moves & (moves-1);
				System.out.println(piece+": "+posStr(start)+" -> "+posStr(index));
			}
		}
	}
	
	private static void printKingMoves(State4 s, int player){
		System.out.println("king moves");
		long kings = s.kings[player];
		char piece = player == 0? 'k': 'K';
		while(kings != 0){
			int start = BitUtil.lsbIndex(kings);
			//long moves = Masks.kingMoves[start] & (~(s.pieces[0]|s.pieces[1]) | s.pieces[1-player]);
			long moves = State4.getKingMoves(player, s.pieces, kings);
			moves |= State4.getCastleMoves(player, s);
			
			//System.out.println(Masks.getString(moves));
			kings = kings & (kings-1);
			while(moves != 0){
				int index = BitUtil.lsbIndex(moves);
				moves = moves & (moves-1);
				System.out.println(piece+": "+posStr(start)+" -> "+posStr(index));
			}
		}
	}
	
	private static void printQueenMoves(State4 s, int player){
		System.out.println("queen moves");
		long queens = s.queens[player];
		char piece = player == 0? 'q': 'Q';
		while(queens != 0){
			long moves = State4.getBishopMoves(player, s.pieces, queens) |
					State4.getRookMoves(player, s.pieces, queens);
			//System.out.println("after xor-ing friendly pieces\n"+Masks.getString(moves));
			int start = BitUtil.lsbIndex(queens);
			queens = queens & (queens-1);
			while(moves != 0){
				int index = BitUtil.lsbIndex(moves);
				moves = moves & (moves-1);
				System.out.println(piece+": "+posStr(start)+" -> "+posStr(index));
			}
		}
	}
	
	private static void printBishopMoves(State4 s, int player){
		System.out.println("bishop moves");
		long bishops = s.bishops[player];
		char piece = player == 0? 'b': 'B';
		while(bishops != 0){
			long moves = State4.getBishopMoves(player, s.pieces, bishops);
			//System.out.println("after xor-ing friendly pieces\n"+Masks.getString(moves));
			int start = BitUtil.lsbIndex(bishops);
			bishops = bishops & (bishops-1);
			while(moves != 0){
				int index = BitUtil.lsbIndex(moves);
				moves = moves & (moves-1);
				System.out.println(piece+": "+posStr(start)+" -> "+posStr(index));
			}
		}
	}
	
	private static void printRookMoves(State4 s, int player){
		System.out.println("rook moves");
		long rooks = s.rooks[player];
		char piece = player == 0? 'r': 'R';
		while(rooks != 0){
			long moves = State4.getRookMoves(player, s.pieces, rooks);
			int start = BitUtil.lsbIndex(rooks);
			rooks = rooks & (rooks-1);
			while(moves != 0){
				int index = BitUtil.lsbIndex(moves);
				moves = moves & (moves-1);
				System.out.println(piece+": "+posStr(start)+" -> "+posStr(index));
			}
		}
	}
	
	private static void printPawnTakes(char piece, int player, int offset, long colMask, State4 s){
		long pawns = s.pawns[player];
		/*long enemy = s.pieces[1-player];
		long moves = offset >= 0? (pawns << offset) & colMask & enemy:
			(pawns >>> -offset) & colMask & enemy;*/
		System.out.println("left pawn attacks:");
		long moves = State4.getLeftPawnAttacks(player, s.pieces, s.enPassante, pawns);
		//System.out.println(Masks.getString(moves));
		while(moves != 0){
			int index = BitUtil.lsbIndex(moves);
			moves = moves&(moves-1);
			System.out.println(piece+": "+posStr(index-offset)+" -> "+posStr(index));
		}
		System.out.println();
		System.out.println("right pawn attacks:");
		moves = State4.getRightPawnAttacks(player, s.pieces, s.enPassante, pawns);
		//System.out.println(Masks.getString(moves));
		offset = player == 0? 9: -9;
		while(moves != 0){
			int index = BitUtil.lsbIndex(moves);
			moves = moves&(moves-1);
			System.out.println(piece+": "+posStr(index-offset)+" -> "+posStr(index));
		}
		System.out.println();
	}
	
	private static void printPawnNoTake(char piece, int player, State4 s){
		final int offset = player == 0? 8: -8;
		//printPawnTakes(piece, player, offset, ~(0L), s);
		long pawns = s.pawns[player];
		final long open = ~(s.pieces[0] | s.pieces[1]);
		long forwardMoves = offset >= 0? (pawns << offset) & open: (pawns >>> -offset) & open;
		while(forwardMoves != 0){
			int index = BitUtil.lsbIndex(forwardMoves);
			forwardMoves = forwardMoves&(forwardMoves-1);
			System.out.println(piece+": "+posStr(index-offset)+" -> "+posStr(index));
		}
		long forward2Moves = State4.getPawnMoves2(player, s.pieces, pawns);
		/*long forward2Moves = offset >= 0?
				(((pawns << offset) & open) << offset & open):
				(((pawns >>> -offset) & open) >>> -offset & open);*/
		while(forward2Moves != 0){
			int index = BitUtil.lsbIndex(forward2Moves);
			forward2Moves = forward2Moves&(forward2Moves-1);
			System.out.println(piece+": "+posStr(index-offset*2)+" -> "+posStr(index));
		}
	}
	
	private static void printPawnMoves(State4 s, int player){
		char piece = player == 0? 'p': 'P';
		
		long colMaskLeft = player == 0? Masks.colMaskExc[7]: Masks.colMaskExc[0];
		printPawnTakes(piece, player, player == 0? 7: -7, colMaskLeft, s);
		
		System.out.println("pawn move, no take:");
		printPawnNoTake(piece, player, s);
		System.out.println();
	}
	
	private static String posStr(int index){
		return ""+(char)('A'+index%8)+(char)('1'+index/8);
	}
	
	private static String getMoveString(int[] l, int i){
		return ""+(char)('A'+l[i]%8)+(l[i]/8+1);
	}
	
	public static State4 loadConfig(char[][] c){
		State4 s = new State4();
		final long q = 1;
		for(int i = 0; i < 64; i++){
			int x = i%8;
			int y = 7-i/8;
			if(c[y][x] != ' '){
				char piece = c[y][x];
				int player = Character.toLowerCase(piece) == piece? 0: 1;
				piece = Character.toLowerCase(piece);
				switch (piece){
				case 'p':
					s.pawns[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_PAWN;
					s.pieceCounts[player][State4.PIECE_TYPE_PAWN]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				case 'q':
					s.queens[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_QUEEN;
					s.pieceCounts[player][State4.PIECE_TYPE_QUEEN]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				case 'b':
					s.bishops[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_BISHOP;
					s.pieceCounts[player][State4.PIECE_TYPE_BISHOP]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				case 'r':
					s.rooks[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_ROOK;
					s.pieceCounts[player][State4.PIECE_TYPE_ROOK]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				case 'n':
					s.knights[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_KNIGHT;
					s.pieceCounts[player][State4.PIECE_TYPE_KNIGHT]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				case 'k':
					s.kings[player] |= q<<i;
					s.mailbox[i] = State4.PIECE_TYPE_KING;
					s.pieceCounts[player][State4.PIECE_TYPE_KING]++;
					s.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
					break;
				}
			}
		}
		
		if(c.length == 9){
			s.kingMoved[0] = c[8][0] == '1';
			s.kingMoved[1] = c[8][1] == '1';
			s.rookMoved[0][0] = c[8][2] == '1';
			s.rookMoved[0][1] = c[8][3] == '1';
			s.rookMoved[1][0] = c[8][4] == '1';
			s.rookMoved[1][1] = c[8][5] == '1';
		}
		
		s.collect();
		return s;
	}
}
