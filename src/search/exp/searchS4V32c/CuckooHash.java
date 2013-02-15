package search.exp.searchS4V32c;

public abstract class CuckooHash <T> {
	public final static class BasicCuckooHash <T> extends CuckooHash<T>{
		public BasicCuckooHash(Encoder<T> e, int size){
			super(e, size);
		}
		
		@Override
		protected int h1(long key) {
			final int z = (int)(key ^ (key >>> 32));
			return z >>> (32-size);
		}

		@Override
		protected int h2(long key) {
			final int z = (int)(key ^ (key >>> 32));
			final int remaining = 32-size;
			return (z << remaining) >>> remaining; //possibly and with ~-1?
		}
	}
	
	private final Encoder<T> e;
	private final long[][] store;
	protected final int size;
	
	public CuckooHash(Encoder<T> e, int size){
		this.e = e;
		store = new long[1<<size][e.requiredStoreSize()];
		this.size = size;
	}
	
	public void put(final long key, final T t){
		final int index1 = h1(key);
		if(store[index1][0] == key){
			e.encode(t, store[index1]);
		} else{
			final int index2 = h2(key);
			if(store[index2][0] == key){
				e.encode(t, store[index2]);
			} else{
				final long[] temp = store[index2];
				store[index2] = store[index1];
				store[index1] = temp;
				e.encode(t, store[index1]);
			}
		}
	}
	
	public boolean get(final long key, final T t){
		final int index1 = h1(key);
		if(store[index1][0] == key){
			e.load(t, store[index1]);
			return true;
		} else{
			final int index2 = h2(key);
			if(store[index2][0] == key){
				e.load(t, store[index2]);
				return true;
			}
		}
		return false;
	}
	
	/** hash function 1*/
	protected abstract int h1(long key);
	/** hash function 2*/
	protected abstract int h2(long key);
}
