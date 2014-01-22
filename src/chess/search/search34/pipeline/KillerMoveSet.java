package chess.search.search34.pipeline;

/** set of killer moves detected for the node*/
public class KillerMoveSet {
	final long l1killer1;
	final long l1killer2;
	final long l2killer1;
	final long l2killer2;

	KillerMoveSet(long l1killer1, long l1killer2, long l2killer1, long l2killer2){
		this.l1killer1 = l1killer1;
		this.l1killer2 = l1killer2;
		this.l2killer1 = l2killer1;
		this.l2killer2 = l2killer2;
	}

	public boolean contains(long encoding){
		final long rawEn = encoding & 0xFFFL; //raw encoding
		return rawEn == l1killer1 || rawEn == l1killer2 ||
				rawEn == l2killer1 || rawEn == l2killer2;
	}
}
