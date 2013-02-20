package search.exp.searchV32c;


public final class EncoderV1 implements Encoder<StateDataV1>{
	@Override
	public void encode(StateDataV1 t, long[] store) {
		store[0] = t.zkey;
		store[1] = t.move;
		store[2] = Double.doubleToLongBits(t.score);
		store[3] = t.cutoffType;
		store[3] = (long)t.depth << 32;
	}

	@Override
	public void load(StateDataV1 t, long[] store) {
		t.zkey = store[0];
		t.move = store[1];
		t.score = Double.longBitsToDouble(store[2]);
		t.cutoffType = (int)store[3];
		t.depth = (int)(store[3] >>> 32);
		//System.out.println("depth = "+t.depth);
	}

	@Override
	public int requiredStoreSize(){
		return 4;
	}
}
