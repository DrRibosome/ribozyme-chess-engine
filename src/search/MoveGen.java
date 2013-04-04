package search;

/** incremental move gen*/
public final class MoveGen {
	/** controls whether */
	private final boolean quiescentGen;
	
	private long ttMove;
	
	private final long[] killer = new long[4];
	private int killerIndex;

	/** index to piece */
	private int moveIndex;
	private final long[] tempMoves = new long[3];
	private int tempMoveIndex;
	
	MoveGen(final boolean quiescentGen){
		this.quiescentGen = quiescentGen;
		if(!quiescentGen){
			
		}
	}
	
	public void loadTTMove(final long ttMove){
		this.ttMove = ttMove;
	}
	
	public void loadKiller(final long kMove){
		killer[killerIndex++] = kMove;
	}
	
	public void reset(){
		ttMove = 0;
		killerIndex = 0;
		moveIndex = 0;
		tempMoveIndex = 0;
	}
	
	/** checks that another move exist, must be called before {@link #getPiece()} and {@link #getMoves()}*/
	public boolean hasNext(){
		
	}
	
	/** gets the piece that is to move*/
	public long getPiece(){
		
	}
	
	/** gets the moves available for the piece returned by {@link #getPiece()}*/
	public long getMoves(){
		
	}
}
