package chess.eval;


import chess.state4.Masks;

/** encoder for score, margin, and any flags*/
public final class ScoreEncoder {
	public final static int scoreBits = 18;
	public final static int marginBits = 11;
	public final static int flagBits = 3;
	
	private final static long scoreMask;
	private final static long scoreSignMask;
	
	private final static long marginMask;
	private final static long marginSignMask;
	
	private final static long flagMask;
	
	private final static int scoreMaskOffset;
	private final static int lowerMarginMaskOffset;
	private final static int upperMarginMaskOffset;
	private final static int flagMaskOffset;
	
	static{
		scoreMaskOffset = 0;
		lowerMarginMaskOffset = scoreBits;
		upperMarginMaskOffset = scoreBits + marginBits;
		flagMaskOffset = scoreBits + marginBits + marginBits;
		
		{
			long temp = 0;
			for(int a = 0; a < scoreBits-1; a++) temp |= 1L << (scoreMaskOffset + a);
			scoreMask = temp;
			scoreSignMask = 1 << scoreBits-1;
		}
		
		{
			long temp = 0;
			for(int a = 0; a < marginBits-1; a++) temp |= 1L << a;
			marginMask = temp;
			marginSignMask = 1 << marginBits-1;
		}
		
		{
			long temp = 0;
			for(int a = 0; a < flagBits; a++) temp |= 1L << (flagMaskOffset + a);
			flagMask = temp;
		}
	}
	
	public static long getScore(final long scoreEncoding){
		return (scoreEncoding & scoreMask) - (scoreEncoding & scoreSignMask);
	}
	
	public static long getLowerMargin(final long scoreEncoding){
		final long margin = scoreEncoding >>> lowerMarginMaskOffset;
		return (margin & marginMask) - (marginSignMask & margin);
	}

	public static long getUpperMargin(final long scoreEncoding){
		final long margin = scoreEncoding >>> upperMarginMaskOffset;
		return (margin & marginMask) - (marginSignMask & margin);
	}
	
	public static long getFlag(final long scoreEncoding){
		return (scoreEncoding & flagMask) >>> flagMaskOffset;
	}

	public static long encode(final int score, final int lowerMargin, final int upperMargin, final long flag){
		return shrink(score, scoreBits) |
				(shrink(lowerMargin, marginBits) << lowerMarginMaskOffset) |
				(shrink(upperMargin, marginBits) << upperMarginMaskOffset) |
				(flag << flagMaskOffset);
	}

	/** shrink passed target value to twos complement value in X bits*/
	private static long shrink(int value, int targetBits){
		int shift = value >>> 31;
		long negativeMask = shift << (targetBits-1);
		long valueMask =  shift*(negativeMask+value) + (1-shift)*value;
		return negativeMask | valueMask;
	}
}
