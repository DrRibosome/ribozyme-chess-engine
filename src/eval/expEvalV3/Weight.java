package eval.expEvalV3;

import java.nio.ByteBuffer;

public final class Weight {
	public final int start, end;
	
	public Weight(final int start, final int end){
		this.start = start;
		this.end = end;
	}
	
	/**  calculates the margin to use in {@link #getScale(int, int, int)}*/
	public static int margin(final int startMaterial, final int endMaterial){
		return endMaterial-startMaterial;
	}
	
	/** gets the interpolatino factor for the weight*/
	public static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
		return min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
	}
	
	/** calculates score, interpolating with scale calculated from {@link #getScale(int, int, int)}*/
	public int score(final double p){
		return start + (int)((end-start)*p);
	}
	
	private static double min(final double d1, final double d2){
		return d1 < d2? d1: d2;
	}
	
	public void writeWeight(final ByteBuffer buff){
		buff.putShort((short)start);
		buff.putShort((short)end);
	}
	
	public static Weight readWeight(final ByteBuffer buff){
		int start = buff.getShort();
		int end = buff.getShort();
		return new Weight(start, end);
	}
	
	@Override
	public String toString(){
		return "("+start+","+end+")";
	}
}
