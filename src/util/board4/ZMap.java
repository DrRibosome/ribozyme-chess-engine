package util.board4;

public final class ZMap {
	public final static int CUTOFF_TYPE_EXACT = 0;
	public final static int CUTOFF_TYPE_UPPER = 1;
	public final static int CUTOFF_TYPE_LOWER = 2;
	public final static int CUTOFF_TYPE_NONE = 3;
	
	private final Entry[] alwaysReplace;
	private final Entry[] depthReplace;
	private final int size;
	private int seq = 0;
	
	public final static class Entry{
		public long zkey;
		/** move encoding for best move, if found (0 otherwise)*/
		public long encoding;
		public double score;
		/** depth the score was computed with*/
		public double depth;
		/** cutoff type of the score*/
		public int cutoffType;
		/** search sequenec that filled the entry*/
		public int sequence;
	}
	
	/**
	 * creates a new ZMap
	 * @param size hash map size, in powers of 2
	 */
	public ZMap(int size){
		this.size = size;
		alwaysReplace = new Entry[1<<size];
		for(int i = 0; i < alwaysReplace.length; i++){
			alwaysReplace[i] = new Entry();
		}
		depthReplace = new Entry[1<<size];
		for(int i = 0; i < depthReplace.length; i++){
			depthReplace[i] = new Entry();
		}
	}
	
	/** increment the sequence, should be run every time a new search is started*/
	public void incSeq(){
		seq++;
	}
	
	public Entry get(long zkey){
		final int index = (int)(zkey >>> (64-size));
		Entry e = alwaysReplace[index];
		if(e.zkey == zkey){
			return e;
		} else{
			Entry d = depthReplace[index];
			if(d.zkey == zkey){
				return d;
			}
		}
		
		
		return null;
	}
	
	/** puts a new entry in*/
	public void put(long zkey, long encoding, double score, double depth, int cutoffType){
		final int index = (int)(zkey >>> (64-size));
		final Entry d = depthReplace[index];
		if(d.sequence != seq || depth > d.depth){
			fill(d, zkey, encoding, score, depth, cutoffType);
		} else{
			fill(alwaysReplace[index], zkey, encoding, score, depth, cutoffType);
		}
	}
	
	public void put2(long zkey, long encoding, double score, double depth, int cutoffType){
		final int index = (int)(zkey >>> (64-size));
		final Entry d = depthReplace[index];
		if(d.sequence != seq || depth < d.depth){
			fill(d, zkey, encoding, score, depth, cutoffType);
		} else{
			final Entry a = alwaysReplace[index];
			fill(a, zkey, encoding, score, depth, cutoffType);
		}
	}
	
	private void fill(Entry e, long zkey, long encoding, double score, double depth, int cutoffType){
		e.zkey = zkey;
		e.encoding = encoding;
		e.score = score;
		e.depth = depth;
		e.cutoffType = cutoffType;
		e.sequence = seq;
	}
}
