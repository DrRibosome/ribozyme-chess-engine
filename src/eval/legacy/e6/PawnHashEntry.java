package eval.legacy.e6;

public final class PawnHashEntry {
	public long zkey;
	public int start;
	public int end;
	
	public void clear(){
		zkey = 0;
		start = 0;
		end = 0;
	}
	
	public void fill(final long zkey, final int lower, int upper){
		this.zkey = zkey;
		this.start = lower;
		this.end = upper;
	}
	
	public void fill(PawnHashEntry t){
		this.zkey = t.zkey;
		this.end = t.end;
		this.start = t.start;
	}
	
	public static void swap(PawnHashEntry s1, PawnHashEntry s2){
		final long temp1 = s1.zkey;
		s1.zkey = s2.zkey;
		s2.zkey = temp1;
		
		final int temp2 = s1.end;
		s1.end = s2.end;
		s2.end = temp2;
		
		final int temp3 = s1.start;
		s1.start = s2.start;
		s2.start = temp3;
	}
}
