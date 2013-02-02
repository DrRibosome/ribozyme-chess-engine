package uci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.board4.State4;

public class FenParser {
	public static Position parse(String fen){
		Position p = new Position();
		p.s = new State4();
		
		String[] s = fen.split("\\s+");
		
		parsePieces(s[0], p.s);
		p.sideToMove = s[1].toLowerCase().charAt(0) == 'w'? 0: 1;
		parseCastling(s[2], p.s);
		parseEnPassant(s[3], p.s);
		p.halfMoves = s[4].equals("-")? 0: Integer.parseInt(s[4]);
		p.fullMoves = s[5].equals("-")? 0: Integer.parseInt(s[5]);
		
		return p;
	}
	
	private static void parseEnPassant(String p, State4 state){
		if(p.length() == 1 && p.equals("-")){
			return;
		}
		
		p = p.toLowerCase();
		int index = p.charAt(0)-'a'+(p.charAt(1)-'1')*8;
		state.enPassante = 1L << index;
	}
	
	private static void parseCastling(String p, State4 state){
		state.kingMoved[0] = true;
		state.kingMoved[1] = true;
		state.rookMoved[0][0] = true;
		state.rookMoved[0][1] = true;
		state.rookMoved[1][0] = true;
		state.rookMoved[1][1] = true;
		if(p.length() == 1 && p.equals("-")){
			return;
		}
		
		for(int a = 0; a < p.length(); a++){
			char c = p.charAt(a);
			int player = Character.isUpperCase(c)? 0: 1;
			state.kingMoved[player] = false;
			c = Character.toLowerCase(c);
			
			if(c == 'k'){
				state.rookMoved[player][1] = false;
			} else if(c == 'q'){
				state.rookMoved[player][0] = false;
			}
		}
	}
	
	private static void parsePieces(String p, State4 state){
		String[] s = p.split("/");
		for(int a = 0; a < 8; a++){
			
			int off = 0;
			for(int i = 0; i < s[a].length(); i++){
				
				char c = s[a].charAt(i);
				if(Character.isDigit(c)){
					int offset = Integer.parseInt(""+c);
					off += offset-1;
				} else{
					int player = Character.isUpperCase(c)? State4.WHITE: State4.BLACK;
					c = Character.toLowerCase(c);
					long[] pieces = null;
					int type = 0;
					
					if(c == 'q'){
						pieces = state.queens;
						type = State4.PIECE_TYPE_QUEEN;
					} else if(c == 'b'){
						pieces = state.bishops;
						type = State4.PIECE_TYPE_BISHOP;
					} else if(c == 'r'){
						pieces = state.rooks;
						type = State4.PIECE_TYPE_ROOK;
					} else if(c == 'k'){
						pieces = state.kings;
						type = State4.PIECE_TYPE_KING;
					} else if(c == 'n'){
						pieces = state.knights;
						type = State4.PIECE_TYPE_KNIGHT;
					} else if(c == 'p'){
						pieces = state.pawns;
						type = State4.PIECE_TYPE_PAWN;
					}
					
					final int index = (7-a)*8+off+i;
					pieces[player] |= 1L << index;
					state.mailbox[index] = type;
					state.pieceCounts[player][type]++;
					state.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
				}
			}
		}
		
		state.collect();
	}
	
	public static void main(String[] args){
		String fen = "8/8/Q4p1p/3p1K2/5Q2/4k2P/6P1/8 b - - - -";
		System.out.println("fen = "+fen);
		Position p = parse(fen);
		System.out.println(p.s);
		System.out.println("side to move = "+p.sideToMove);
		
		

		String fen2 = "fen rnbqkbnr/1ppp2pp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
		Pattern p2 = Pattern.compile("fen ((.*?\\s+){5}.*?)(\\s+|$)");
		Matcher m = p2.matcher(fen2);
		m.find();
		System.out.println(m.group(1));
	}
}
