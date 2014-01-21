package chess.state4;

public final class StateUtil {
	private final static char[] p;
	
	static{
		p = new char[7];
		p[State4.PIECE_TYPE_BISHOP] = 'b';
		p[State4.PIECE_TYPE_KING] = 'k';
		p[State4.PIECE_TYPE_QUEEN] = 'q';
		p[State4.PIECE_TYPE_ROOK] = 'r';
		p[State4.PIECE_TYPE_KNIGHT] = 'n';
		p[State4.PIECE_TYPE_PAWN] = 'p';
	}
	
	public static String fen(int turn, State4 s){
		String f = "";
		int space = 0;
		for(int a = 7; a >= 0; a--){
			for(int q = 0; q < 8; q++){
				final int type = s.mailbox[a*8+q];
				if(type != State4.PIECE_TYPE_EMPTY){
					final int player = (s.pieces[0] & (1L << a*8+q)) != 0? 0: 1;
					char c = p[type];
					if(player == 0){
						c = Character.toUpperCase(c);
					}
					if(space != 0){
						f += space;
						space = 0;
					}
					f += c;
					
				} else{
					space++;
				}
			}
			if(space != 0){
				f += space;
				space = 0;
			}
			if(a != 0){
				f += '/';
			}
		}
		
		f += " "+(turn == 0? 'w': 'b');
		
		String castle = "";
		for(int a = 0; a < 2; a++){
			if(!s.kingMoved[a]){
				if(!s.rookMoved[a][0]){
					castle += a==0? 'Q': 'q';
				}
				if(!s.rookMoved[a][1]){
					castle += a==0? 'K': 'k';
				}
			}
		}
		f += ' '+(castle.length() == 0? "-": castle);
		
		if(s.enPassante != 0){
			final int index = BitUtil.lsbIndex(s.enPassante);
			f += " "+(char)('a'+index%8)+(""+(index/8+1));
		} else{
			f += " -";
		}
		
		f += " "+s.drawCount+" -"; //for moves and half moves
		
		return f;
	}
}
