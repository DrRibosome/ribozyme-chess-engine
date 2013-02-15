package search.exp.searchS4V32c;

public interface Encoder <T>{
	/** encode the data in long[] store, first index MUST be the key*/
	public void encode(T t, long[] store);
	public void load(T t, long[] store);
	/** get the target size of the store*/
	public int requiredStoreSize();
}
