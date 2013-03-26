package state4;


public final class MoveEncoder {
	/** 6-bit position mask*/
	private final static long posMask = 0x3FL;
	private final static long pieceTypeMask = 7;
	/** 7-bit draw count mask*/
	private final static long drawCountMask = 0x7F;
	
	/**
	 * stores offsets for king/rook first moves
	 * <p>
	 * bit 0: king moved
	 * bit 1: rook moved/taken, player 0
	 * bit 2: rook moved/taken, player 1
	 * bit 3: which rook was moved/taken for player 0
	 * bit 4: which rook was moved/taken for player 1
	 * bit 5: unused
	 */
	private final static int castleCodeOffset = 19; //new castle code uses 5 bits, reserves 6
	private final static int isEnPassanteTakeOffset = 32;
	private final static int isCastleOffset = 33;
	private final static int prevDrawCountOffset = 34;
	
	public static int getPos1(final long encoding){
		return (int)(encoding & posMask);
	}
	
	public static int getPos2(final long encoding){
		return (int)((encoding & (posMask<<6)) >>> 6);
	}
	
	/** gets the type of piece taken*/
	public static int getTakenType(final long encoding){
		return (int)((encoding & (pieceTypeMask<<15)) >>> 15);
	}
	
	/** gets the type of the piece that moved*/
	public static int getMovePieceType(final long encoding){
		return (int)((encoding & (pieceTypeMask<<12)) >>> 12);
	}
	
	public static int getPlayer(final long encoding){
		final long q = 1;
		return (int)((encoding & (q<<18)) >>> 18);
	}
	
	public static boolean isFirstKingMove(final int player, final long encoding){
		return (encoding & (1L<<(19+player))) != 0;
	}
	
	/** sets the position of a previous en passante (that will be destroyed by this move)*/
	public static long setPrevEnPassantePos(final long pos, final long encoding){
		return encoding | ((1L*BitUtil.lsbIndex(pos))<<26);
	}
	
	/** returns a mask of the previous move's en passante position (0 if no pos)*/
	public static int getPrevEnPassantePos(final long encoding){
		return (int)((encoding & (posMask<<26)) >>> 26);
	}
	
	/** sets that the move encoded was an en passante take*/
	public static long setEnPassanteTake(final long encoding){
		return encoding | 1L << isEnPassanteTakeOffset;
	}
	
	/** return 0 if false, returns non-zero if true*/
	public static long isEnPassanteTake(final long encoding){
		//return (encoding & (1L<<32)) != 0;
		return encoding & 1L<<isEnPassanteTakeOffset;
	}
	
	public static long setCastle(final long encoding){
		return encoding | 1L<<isCastleOffset;
	}

	/** return 0 if false, returns non-zero if true*/
	public static long isCastle(final long encoding){
		return encoding & (1L<<isCastleOffset);
	}
	
	/** sets the number of moves since last pawn move or capture*/
	public static long setPrevDrawCount(final long encoding, final long count){
		return encoding | count << prevDrawCountOffset;
	}
	
	/** gets the number of moves since the last pawn move or capture*/
	public static int getPrevDrawCount(final long encoding){
		return (int)(drawCountMask & encoding >>> prevDrawCountOffset);
	}
	
	public static void undoCastleProps(final long encoding, final State4 s){
		long castleCode = (encoding & (posMask << 19)) >>> 19;
		if(castleCode != 0){
			int player = getPlayer(encoding);
			
			if((castleCode & 1<<player) != 0){
				s.kingMoved[player] = false;
			} else if((castleCode & 3<<(player*2+2)) != 0){
				//rook was moved for first time
				final int rook = (int)((castleCode & (3<<(player*2+2))) >>> (player*2+2));
				s.rookMoved[player][rook-1] = false;
			}
			
			if((castleCode & 3<<((1-player)*2+2)) != 0){
				//must have taken a rook that had not moved
				final int rook = (int)((castleCode & 3<<((1-player)*2+2)) >>> ((1-player)*2+2));
				s.rookMoved[1-player][rook-1] = false;
			}
		}
	}
	
	/** record that king has moved for first time*/
	public static long setFirstKingMove(final long encoding){
		return encoding | 1L<<castleCodeOffset;
	}
	
	/** true if move was first king move, false otherwise*/
	public static boolean isFirstKingMove(final long encoding){
		return (encoding & 1L<<castleCodeOffset) != 0;
	}

	/** sets that a players rook has moved for the first time, rook==left? 0: 1*/
	public static long setFirstRookMove(final int player, final int rook, final long encoding){
		final long playerMask = 1L << 1+player;
		final long rookMask = 1L << 3+player;
		return encoding | (playerMask | rookMask) << castleCodeOffset;
	}
	
	/** returns true if rook has moved/taken for first time*/
	public static boolean isFirstRookMove(final int player, final long encoding){
		final long playerMask = 1L << 1+player;
		return (encoding & playerMask << castleCodeOffset) != 0;
	}
	
	/** gets the side the first rook moved on, returns correct values if {@link #isFirstRookMove(int, long)} returns true*/
	public static int getFirstRookMoveSide(final int player, final long encoding){
		final int rookOffset = 3+player+castleCodeOffset;
		return (int)((encoding & 1L << rookOffset) >>> rookOffset);
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
	public static long encode(final int pos1, final int pos2, final int pieceMovingType, final int pieceTakenType,
			final int player, final State4 s){
		long castleCode = 0;
		if(!s.kingMoved[player] && pieceMovingType == State4.PIECE_TYPE_KING){
			castleCode |= 1<<player;
			s.kingMoved[player] = true;
		} else if(pieceMovingType == State4.PIECE_TYPE_ROOK){
			final boolean left = (player == 0 && pos1 == 0) || (player == 1 && pos1 == 56);
			final boolean right = (player == 0 && pos1 == 7) || (player == 1 && pos1 == 63);
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
			final boolean left = (1-player == 0 && pos2 == 0) || (1-player == 1 && pos2 == 56);
			final boolean right = (1-player == 0 && pos2 == 7) || (1-player == 1 && pos2 == 63);
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
	public static long setPawnPromotion(final long encoding){
		return encoding | (1L<<25);
	}
	
	public static boolean isPawnPromoted(final long encoding){
		return (encoding & (1L<<25)) != 0;
	}
	
	public static String getString(final long encoding){
		int pos1 = getPos1(encoding);
		int pos2 = getPos2(encoding);
		return posString(pos1)+" -> "+posString(pos2);
	}
	
	private static String posString(final int pos){
		return ""+(char)(pos%8+'A')+(char)(pos/8+'1');
	}
}
