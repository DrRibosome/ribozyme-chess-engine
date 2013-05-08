package search.search33.prof;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class SearchLogger{
	public final static class Entry{
		public int predictedScore;
		public int currentAlpha;
		public int currentBeta;
		public int depth;
		public int moveCount;
		public int pieceType;
		public int lazyEval;
		public boolean pv;
		public boolean quiesce;
		public boolean inCheck;
		public boolean isCapture;
		public boolean isDangerous;
		public boolean isTTEMove;
	}
	
	private final static int pvFlag = 1 << 0;
	private final static int quiesceFlag = 1 << 1;
	private final static int checkFlag = 1 << 2;
	private final static int captureFlag = 1 << 3;
	private final static int dangerousFlag = 1 << 4;
	private final static int tteFlag = 1 << 5;
	
	private final DataOutputStream dos;
	
	SearchLogger(File f) throws IOException{
		dos = new DataOutputStream(new FileOutputStream(f));
	}
	
	public void log(final int predictedScore, final int currentAlpha, final int currentBeta, final int depth, final int moveCount,
			final boolean pv, final boolean quiesce, final boolean inCheck, final boolean isCapture, final boolean isDangerous,
			final boolean isTTEMove, final int pieceType, final int lazyEval){
		try{
			dos.writeShort(predictedScore);
			dos.writeShort(currentAlpha);
			dos.writeShort(currentBeta);
			dos.writeShort(depth);
			dos.writeShort(moveCount);
			dos.writeShort(pieceType);
			dos.writeShort(lazyEval);
			
			int flags = 0;
			if(pv) flags |= pvFlag;
			if(quiesce) flags |= quiesceFlag;
			if(inCheck) flags |= checkFlag;
			if(isCapture) flags |= captureFlag;
			if(isDangerous) flags |= dangerousFlag;
			if(isTTEMove) flags |= tteFlag;
			
			dos.writeShort(flags);
			
			
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static Entry load(DataInputStream dis) throws IOException{
		Entry e = new Entry();
		
		e.predictedScore = dis.readShort();
		e.currentAlpha = dis.readShort();
		e.currentBeta = dis.readShort();
		e.depth = dis.readShort();
		e.moveCount = dis.readShort();
		e.pieceType = dis.readShort();
		e.lazyEval = dis.readShort();
		
		final int flags = dis.readShort();
		e.pv = (flags & pvFlag) != 0;
		e.quiesce = (flags & quiesceFlag) != 0;
		e.inCheck = (flags & checkFlag) != 0;
		e.isCapture = (flags & captureFlag) != 0;
		e.isDangerous = (flags & dangerousFlag) != 0;
		e.isTTEMove = (flags & tteFlag) != 0;
		
		return e;
	}
}
