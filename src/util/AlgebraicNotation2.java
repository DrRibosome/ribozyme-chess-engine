package util;

import java.util.HashMap;
import java.util.Map;

import util.board4.BitUtil;
import util.board4.State4;

public final class AlgebraicNotation2 {
	private final static boolean debug = false;
	private final static Map<Character, Integer> m;
	
	static{
		m = new HashMap<Character, Integer>();
		m.put('N', State4.PIECE_TYPE_KNIGHT);
		m.put('K', State4.PIECE_TYPE_KING);
		m.put('Q', State4.PIECE_TYPE_QUEEN);
		m.put('B', State4.PIECE_TYPE_BISHOP);
		m.put('R', State4.PIECE_TYPE_ROOK);
	}
	
	/** returns a length 2 array specifying the start and end coordinates for the move*/
	public static int[] getPos(int player, String move, State4 s){
		if(debug) System.out.println("processing '"+move+"'");
		if(move.charAt(move.length()-1) == '+' || move.charAt(move.length()-1) == '#'){
			move = move.substring(0, move.length()-1);
		}
		//Rae1, R2e1
		if(move.equals("O-O-O")){
			if(debug) System.out.println("castle queen side");
			return player == 0? new int[]{4,2}: new int[]{60,58};
		} else if(move.equals("O-O")){
			if(debug) System.out.println("castle king side");
			return player == 0? new int[]{4,6}: new int[]{60,62};
		} else if(Character.isLowerCase(move.charAt(0))){
			//pawn move, "fxg1=Q+"
			if(move.charAt(1) != 'x'){
			//if(move.length() == 2 || (move.length() == 4 && move.charAt(2) == '=')){
				//pawn move
				if(debug) System.out.println("pawn move");
				int target = convert(move);
				
				long mask = 1L<<target;
				long pawns = s.pawns[player];
				while((pawns & mask) == 0){
					mask = player == 0? mask >>> 8: mask << 8;
				}
				return new int[]{BitUtil.lsbIndex(mask), target};
			} //else if(move.length() == 4 && move.charAt(2) != '='){
			else if(move.charAt(1) == 'x'){
				//pawn take
				if(debug) System.out.println("pawn take");
				int target = convert(move.substring(2));
				int offset = player == 0? -1: 1;
				int startCol = move.charAt(0)-'a';
				return new int[]{startCol+(target/8+offset)*8, target};
			}
		} else{
			int pieceType = m.get(move.charAt(0));
			long[] p = new long[7];
			p[State4.PIECE_TYPE_BISHOP] = s.bishops[player];
			p[State4.PIECE_TYPE_QUEEN] = s.queens[player];
			p[State4.PIECE_TYPE_ROOK] = s.rooks[player];
			p[State4.PIECE_TYPE_KING] = s.kings[player];
			p[State4.PIECE_TYPE_KNIGHT] = s.knights[player];
			
			//other piece movement
			if(move.length() == 3){
				
				//no differentiation if two pieces can move and one in check
				
				
				if(debug) System.out.println("piece movement ("+move.charAt(0)+")");
				
				int pos = convert(move.substring(1));
				long moves = getMoves(pos, 1-player, pieceType, s);
				//System.out.println(Masks.getString(moves));
				//System.out.println("end pos = "+pos);
				//System.out.println(Masks.getString(1L<<pos));
				//System.out.println(Masks.getString(moves));
				long piece = moves & p[pieceType];
				//System.out.println(Masks.getString(piece));
				//System.out.println(BitUtil.lsbIndex(piece)+" -> "+pos);
				if((piece&(piece-1)) == 0){
					return new int[]{BitUtil.lsbIndex(piece), pos};
				} else{
					//one of the pieces is held by check
					/*System.out.println("====================================================================");
					System.out.println("guessing randomly at which piece is held by check (restart if wrong)");
					System.out.println("====================================================================");*/
					long p1 = piece&-piece;
					piece &= piece-1;
					long p2 = piece;
					
					long movingPiece = p1;
					s.executeMove(player, p1, 1L<<pos);
					if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
						movingPiece = p2;
					}
					s.undoMove();

					return new int[]{BitUtil.lsbIndex(movingPiece), pos};
				}
			} else if(move.length() == 4 && move.charAt(1) == 'x'){
				if(debug) System.out.println("piece take ("+move.charAt(0)+")");
				int pos = convert(move.substring(2));
				long moves = getMoves(pos, 1-player, pieceType, s);
				long piece = moves & p[pieceType];
				return new int[]{BitUtil.lsbIndex(piece), pos};
			} else if(move.length() == 4 && move.charAt(1) != 'x'){
				if(debug) System.out.println("specified move ("+move.charAt(0)+")");
				int pos = convert(move.substring(2));
				long moves = getMoves(pos, 1-player, pieceType, s);
				long pieces = moves & p[pieceType];
				
				char spec = move.charAt(1);
				if(Character.isLetter(spec)){
					int col = spec-'a';
					long colMask = 0x101010101010101L;
					//System.out.println(Masks.getString(colMask));
					pieces &= colMask << col;
				} else{
					int row = spec-'1';
					long rowMask = 0xFFL;
					pieces &= rowMask<<row*8;
				}
				
				return new int[]{BitUtil.lsbIndex(pieces), pos};
			}  else if(move.length() == 5 && move.charAt(1) != 'x' && move.charAt(2) == 'x'){
				//fails on R3xe4
				
				if(debug) System.out.println("specified take ("+move.charAt(0)+")");
				int pos = convert(move.substring(3));
				long moves = getMoves(pos, 1-player, pieceType, s);
				long pieces = moves & p[pieceType];
				
				char spec = move.charAt(1);
				if(Character.isLetter(spec)){
					int col = spec-'a';
					long colMask = 0x101010101010101L;
					//System.out.println(Masks.getString(colMask));
					pieces &= colMask << col;
				} else{
					int row = spec-'1';
					long rowMask = 0xFFL;
					pieces &= rowMask<<row;
				}
				
				return new int[]{BitUtil.lsbIndex(pieces), pos};
			}
		}
		
		return new int[]{};
	}
	
	private static long getMoves(int pos, int player, int pieceType, State4 s){
		long[] temp = new long[2];
		temp[0] = s.pieces[0];
		temp[1] = s.pieces[1];
		temp[0] |= 1L<<pos;
		
		if(pieceType == State4.PIECE_TYPE_BISHOP){
			return State4.getBishopMoves(player, temp, 1L<<pos);
		} else if(pieceType == State4.PIECE_TYPE_ROOK){
			return State4.getRookMoves(player, temp, 1L<<pos);
		} else if(pieceType == State4.PIECE_TYPE_QUEEN){
			return State4.getQueenMoves(player, temp, 1L<<pos);
		} else if(pieceType == State4.PIECE_TYPE_KING){
			return State4.getKingMoves(player, temp, 1L<<pos);
		} else if(pieceType == State4.PIECE_TYPE_KNIGHT){
			return State4.getKnightMoves(player, temp, 1L<<pos);
		}
		return 0;
	}
	
	/** convert string position (like A4,B6, etc) to int position*/
	private static int convert(String pos){
		pos = pos.toLowerCase();
		return pos.charAt(0)-'a' + (pos.charAt(1)-'1')*8;
	}
	
	/*public static void main(String[] args){
		State4 s = new State4();
		char[][] c = new char[][]{			
				{'R', ' ', ' ', 'Q', 'K', ' ', ' ', 'R'},
				{'P', ' ', 'P', ' ', 'B', 'P', 'P', ' '},
				{' ', ' ', ' ', ' ', 'P', 'N', ' ', 'P'},
				{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
				{' ', ' ', ' ', ' ', 'n', ' ', ' ', ' '},
				{'p', ' ', ' ', ' ', ' ', 'q', ' ', ' '},
				{' ', 'p', 'p', 'b', ' ', 'p', 'p', 'p'},
				{'r', ' ', ' ', ' ', 'k', ' ', ' ', 'r'},
				{'0', '0', '0', '0', '0', '0'}
		};
		s = Debug.loadConfig(c);
		//s.initialize();
		
		System.out.println(s);

		int player = 0;
		String test = "Bdxh6";
		int[] move = getPos(player, test, s);
		System.out.println(move[0]+" -> "+move[1]);
	}*/
}
