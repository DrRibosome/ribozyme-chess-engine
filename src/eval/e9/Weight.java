package eval.e9;

/** handles evaluator weight encoding and interpoation*/
final class Weight {
	private final static int weightValueMask = 0x7FFF;
	private final static int weightSignMask = 1<<15;
	
	/** build a weight scaling from passed start,end values*/
	static int encode(final int start, final int end){
		return (start&0xFFFF) + (end<<16);
	}
	
	/** build a constant, non-scaling weight*/
	static int encode(final int v){
		return encode(v, v);
	}
	
	/** mid game score*/
	static int mgScore(final int weight){
		return  (weight & weightValueMask) - (weight & weightSignMask);
	}
	
	/** end game score*/
	static int egScore(final int weight){
		final int shifted = weight >>> 16;
		return (shifted & weightValueMask) - (shifted & weightSignMask);
	}
	
	/** interpolate a passed weight value, scale in [0,1]*/
	static int interpolate(final int weight, final double scale){
		final int start = mgScore(weight);
		final int end = egScore(weight);
		return (int)(start + (end-start)*scale);
	}
	
	static int multWeight(final int weight, final double mult){
		final int start = (weight & weightValueMask) - (weight & weightSignMask);
		final int shifted = weight >>> 16;
		final int end = (shifted & weightValueMask) - (shifted & weightSignMask);
		return encode((int)(start*mult), (int)(end*mult));
	}
}
