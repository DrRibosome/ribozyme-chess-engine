package util.analysis.searchAnalysis;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import search.search33.prof.SearchLogger;
import state4.State4;

/** 
 * gives analysis for quiet move score gain
 * <p> aggregates and displays ~95 quantile for the difference between
 * the predicted score and alpha for candidate moves
 * 
 * <p>
 * results seem to indicate that best quiet moves can be goods
 * (~98 quantile returns ~200 points), but most quiet moves
 * are bad (average loss ~200 points)
 */
public class AnalysisQuietMoveGain {
	public static void main(String[] args) throws IOException{
		
		final File f = new File("search-test.log");
		
		final DataInputStream dis = new DataInputStream(new FileInputStream(f));
		
		final int[][][] meanDiff = new int[7][128][128]; //indexed [depth][move-count]
		final int[][][] variance = new int[7][128][128];
		final int[][][] n = new int[7][128][128]; //number of occurrences, indexed [depth][move-count]
		
		int positions = 0;
		while(dis.available() > 0){
			SearchLogger.Entry temp = SearchLogger.load(dis);
			positions++;
			
			if(!temp.inCheck && !temp.isCapture && !temp.isDangerous && !temp.isTTEMove &&
					Math.abs(temp.predictedScore) < 3000 && Math.abs(temp.currentAlpha) < 3000 &&
					!temp.pv && !temp.quiesce){
				
				n[0][temp.depth][temp.moveCount]++;
				n[temp.pieceType][temp.depth][temp.moveCount]++;
				
				final int k = temp.currentAlpha-temp.predictedScore;
				meanDiff[0][temp.depth][temp.moveCount] += k;
				variance[0][temp.depth][temp.moveCount] += Math.pow(k, 2);
				meanDiff[temp.pieceType][temp.depth][temp.moveCount] += k;
				variance[temp.pieceType][temp.depth][temp.moveCount] += Math.pow(k, 2);
				
				
				
			}
		}
		System.out.println("positions under analysis = "+positions);
		
		for(int a = 0; a < 7; a++){
			for(int d = 0; d < meanDiff[0].length; d++){
				for(int mc = 0; mc < meanDiff[0][0].length; mc++){
					if(n[a][d][mc] != 0){
						meanDiff[a][d][mc] /= n[a][d][mc];
						variance[a][d][mc] = (int)(variance[a][d][mc]/n[a][d][mc] - Math.pow(meanDiff[a][d][mc], 2));
					}
				}
			}
		}
		
		String[] pieceTypes = new String[7];
		pieceTypes[State4.PIECE_TYPE_EMPTY] = "all";
		pieceTypes[State4.PIECE_TYPE_BISHOP] = "bishop";
		pieceTypes[State4.PIECE_TYPE_KNIGHT] = "knight";
		pieceTypes[State4.PIECE_TYPE_QUEEN] = "queen";
		pieceTypes[State4.PIECE_TYPE_ROOK] = "rook";
		pieceTypes[State4.PIECE_TYPE_PAWN] = "pawn";
		pieceTypes[State4.PIECE_TYPE_KING] = "king";
		final int depthToDisplay = 9; 
		final int moveCountToDisplay = 60;
		
		for(int a = 0; a < 7; a++){
			System.out.println("------------------------------------");
			System.out.println("piece type = "+pieceTypes[a]);
			System.out.println();
			
			System.out.println("mean difference between current alpha (before prediction) and predicted score");
			for(int d = 0; d < meanDiff[0].length; d++){
				if(d <= depthToDisplay){
					System.out.print("d="+d+":\t");
					for(int mc = 0; mc < meanDiff[0][0].length; mc++){
						if(mc <= moveCountToDisplay){
							System.out.print(meanDiff[a][d][mc]+",\t");
						}
					}
					System.out.println();
				}
			}
			
			System.out.println("~98% quantile");
			for(int d = 0; d < meanDiff[0].length; d++){
				if(d <= depthToDisplay){
					System.out.print("d="+d+":\t");
					for(int mc = 0; mc < meanDiff[0][0].length; mc++){
						if(mc <= moveCountToDisplay){
							System.out.print((int)(meanDiff[a][d][mc]-2*Math.sqrt(variance[a][d][mc]))+",\t");
						}
					}
					System.out.println();
				}
			}
			
			System.out.println("number of samples");
			for(int d = 0; d < meanDiff[0].length; d++){
				if(d <= depthToDisplay){
					System.out.print("d="+d+":\t");
					for(int mc = 0; mc < meanDiff[0][0].length; mc++){
						if(mc <= moveCountToDisplay){
							System.out.print(n[a][d][mc]+",\t");
						}
					}
					System.out.println();
				}
			}
		}
	}
}
