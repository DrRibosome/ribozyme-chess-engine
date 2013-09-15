package eval;




/** encoder for score, margin, and any flags*/
public final class ScoreEncoder {
	public final static int scoreBits = 18;
	public final static int marginBits = 10;
	public final static int flagBits = 3;
	public final static int cutoffTypeBits = 1;
	
	private final static int scoreMask;
	private final static int scoreSignMask;
	
	private final static int marginMask;
	private final static int marginSignMask;
	
	private final static int flagMask;
	
	private final static int cutoffTypeMask;
	
	private final static int scoreMaskOffset;
	private final static int marginMaskOffset;
	private final static int flagMaskOffset;
	private final static int cutoffTypeMaskOffset;
	
	static{
		scoreMaskOffset = 0;
		marginMaskOffset = scoreBits;
		cutoffTypeMaskOffset = scoreBits + marginBits;
		flagMaskOffset = scoreBits + marginBits + cutoffTypeBits;
		
		{
			int temp = 0;
			for(int a = 0; a < scoreBits-1; a++) temp |= 1 << (scoreMaskOffset + a);
			scoreMask = temp;
			scoreSignMask = 1 << scoreBits-1;
		}
		
		{
			int temp = 0;
			for(int a = 0; a < marginBits-1; a++) temp |= 1 << a;
			marginMask = temp;
			marginSignMask = 1 << marginBits-1;
		}
		
		{
			int temp = 0;
			for(int a = 0; a < flagBits; a++) temp |= 1 << (flagMaskOffset + a);
			flagMask = temp;
		}
		
		{
			cutoffTypeMask = 1 << cutoffTypeMaskOffset;
		}
	}
	
	public static int getScore(final int scoreEncoding){
		return (scoreEncoding & scoreMask) - (scoreEncoding & scoreSignMask);
	}
	
	public static int getMargin(final int scoreEncoding){
		final int margin = scoreEncoding >>> marginMaskOffset;
		return (margin & marginMask) - (marginSignMask & margin);
	}
	
	public static int getFlags(final int scoreEncoding){
		return (scoreEncoding & flagMask) >>> flagMaskOffset;
	}
	
	public static boolean isLowerBound(final int score){
		return (score & cutoffTypeMask) != 0;
	}
	
	public static int encode(final int score, final int margin, final int flags, final boolean isLowerBound){
		assert score < 1<<scoreBits;
		assert margin < 1<<marginBits;
		assert flags < 1<<flagBits;
		assert flags >= 0;
		
		final int scoreShift = score >>> 31;
		final int scoreNegative = scoreShift << (scoreBits-1);
		final int scoreValue = scoreShift*(scoreNegative+score) + (1-scoreShift)*score;
		
		final int marginShift = margin >>> 31;
		final int marginNegative = marginShift << (marginBits-1);
		final int marginValue =  marginShift*(marginNegative+margin) + (1-marginShift)*margin;
		
		return (scoreValue | scoreNegative) |
				((marginValue|marginNegative) << marginMaskOffset) |
				((isLowerBound? 1: 0) << cutoffTypeMaskOffset) |
				(flags << flagMaskOffset);
	}
}
