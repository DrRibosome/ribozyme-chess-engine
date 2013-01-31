package debug;

import search.Search3;
import search.SearchS4V29;
import search.SearchS4V30;
import time.TimerThread2;
import util.OldPositions;
import util.board4.BitUtil;
import util.board4.Masks;
import util.board4.State4;
import eval.SuperEvalS4V8;


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

		State4 s = loadConfig(c);
		//State4 s = loadConfig(c1);
		System.out.println(s);
		
		SuperEvalS4V8 e = new SuperEvalS4V8();
		//e.traceEval(s, State4.WHITE);
		
		final int maxDepth = 15;
		Search3 search = new SearchS4V29(maxDepth, s, e, 20, false);
		int[] move = new int[2];
		search.search(State4.BLACK, move, maxDepth);
		System.out.println("\n"+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
		
		TimerThread2.search(new SearchS4V30(maxDepth, s, e, 20, false), s, State4.WHITE, 1000*60*3, 0, move);
		
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
		return util.debug.Debug.loadConfig(c);
	}
}
