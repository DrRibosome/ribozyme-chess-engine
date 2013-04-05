package util.genetic.mutatorV1;

final class IntArrayMutator implements MutatorPoint{
	private final int[] w;
	private final int[] excludedIndeces;
	IntArrayMutator(int[] w, int[] excludedIndeces){
		this.w = w;
		this.excludedIndeces = excludedIndeces;
	}
	public void mutate() {
		int index;
		while(contains(index = (int)(Math.random()*w.length), excludedIndeces));
		
		int value = w[index];
		final double offset = -value*MutatorV1.mDist + value*MutatorV1.mDist*2*Math.random();
		value += max((int)offset, 1);
		w[index] = value;
	}
	
	private static int max(final int i1, final int i2){
		return i1 > i2? i1: i2;
	}
	
	private static boolean contains(int i, int[] l){
		for(int a = 0; a < l.length; a++){
			if(l[a] == i){
				return true;
			}
		}
		return false;
	}
}
