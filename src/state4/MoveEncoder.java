package state4;

public final class MoveEncoder {
	private final static long posMask = 0x3FL;
	private final static long pieceTypeMask = 7;
	
	public static int getPos1(long encoding){
		return (int)(encoding & posMask);
	}
	
	public static int getPos2(long encoding){
		return (int)((encoding & (posMask<<6)) >>> 6);
	}
	
	/** gets the type of piece taken*/
	public static int getTakenType(long encoding){
		return (int)((encoding & (pieceTypeMask<<15)) >>> 15);
	}
	
	/** gets the type of the piece that moved*/
	public static int getMovePieceType(long encoding){
		return (int)((encoding & (pieceTypeMask<<12)) >>> 12);
	}
	
	public static int getPlayer(long encoding){
		final long q = 1;
		return (int)((encoding & (q<<18)) >>> 18);
	}
	
	public static boolean isFirstKingMove(int player, long encoding){
		return (encoding & (1L<<(19+player))) != 0;
	}
	
	/** sets the position of a previous en passante (that will be destroyed by this move)*/
	public static long setPrevEnPassantePos(long pos, long encoding){
		return encoding | ((1L*BitUtil.lsbIndex(pos))<<26);
	}
	
	/** returns a mask of the previous move's en passante position (0 if no pos)*/
	public static int getPrevEnPassantePos(long encoding){
		return (int)((encoding & (posMask<<26)) >>> 26);
	}
	
	/** sets that the move encoded was an en passante take*/
	public static long setEnPassanteTake(long encoding){
		return encoding | (1L << 32);
	}
	
	/** return 0 if false, returns non-zero if true*/
	public static long isEnPassanteTake(long encoding){
		//return (encoding & (1L<<32)) != 0;
		return encoding & (1L<<32);
	}
	
	public static long setCastle(long encoding){
		return encoding | 1L<<33;
	}

	/** return 0 if false, returns non-zero if true*/
	public static long isCastle(long encoding){
		return encoding & (1L<<33);
	}
	
	public static void undoCastleProps(long encoding, State4 s){
		long castleCode = (encoding & (posMask << 19)) >>> 19;
		if(castleCode != 0){
			int player = getPlayer(encoding);
			
			if((castleCode & 1<<player) != 0){
				s.kingMoved[player] = false;
			} else if((castleCode & 3<<(player*2+2)) != 0){
				//rook was moved for first time
				int rook = (int)((castleCode & (3<<(player*2+2))) >>> (player*2+2));
				s.rookMoved[player][rook-1] = false;
			}
			
			if((castleCode & 3<<((1-player)*2+2)) != 0){
				//must have taken a rook that had not moved
				int rook = (int)((castleCode & 3<<((1-player)*2+2)) >>> ((1-player)*2+2));
				s.rookMoved[1-player][rook-1] = false;
			}
		}
	}
	
	/**
	 * encodes passed move and sets castling properties on the passed state object
	 * @param pos1
	 * @param pos2
	 * @param pieceMovingType
	 * @param pieceTakenType
	 * @param player
	 * @param s
	 * @return
	 */
	public static long encode(int pos1, int pos2, int pieceMovingType, int pieceTakenType,
			int player, State4 s){
		long castleCode = 0;
		if(!s.kingMoved[player] && pieceMovingType == State4.PIECE_TYPE_KING){
			castleCode |= 1<<player;
			s.kingMoved[player] = true;
		} else if(pieceMovingType == State4.PIECE_TYPE_ROOK){
			boolean left = (player == 0 && pos1 == 0) ||(player == 1 && pos1 == 56);
			boolean right = (player == 0 && pos1 == 7) ||(player == 1 && pos1 == 63);
			if(!s.rookMoved[player][0] && left){
				castleCode |= 1<<(2+player*2);
				s.rookMoved[player][0] = true;
			} else if(!s.rookMoved[player][1] && right){
				castleCode |= 1<<(2+player*2+1);
				s.rookMoved[player][1] = true;
			}
		}
		
		if(pieceTakenType == State4.PIECE_TYPE_ROOK){
			//System.out.println("rook taken");
			boolean left = (1-player == 0 && pos2 == 0) || (1-player == 1 && pos2 == 56);
			boolean right = (1-player == 0 && pos2 == 7) || (1-player == 1 && pos2 == 63);
			if(!s.rookMoved[1-player][0] && left){
				castleCode |= 1<<(2+(1-player)*2);
				s.rookMoved[1-player][0] = true;
			} else if(!s.rookMoved[1-player][1] && right){
				castleCode |= 1<<(2+(1-player)*2+1);
				s.rookMoved[1-player][1] = true;
			}
		}
		
		
		return pos1 | (pos2 << 6) | (pieceMovingType << 12) |
				(pieceTakenType << 15) | (player << 18) | (castleCode << 19);
	}
	
	/** given the encoding, set pawn promotion flag and return new encoding*/
	public static long setPawnPromotion(long encoding){
		return encoding | (1L<<25);
	}
	
	public static boolean isPawnPromoted(long encoding){
		return (encoding & (1L<<25)) != 0;
	}
	
	public static String getString(long encoding){
		int pos1 = getPos1(encoding);
		int pos2 = getPos2(encoding);
		return posString(pos1)+" -> "+posString(pos2);
	}
	
	private static String posString(int pos){
		return ""+(char)(pos%8+'A')+(char)(pos/8+'1');
	}
}
