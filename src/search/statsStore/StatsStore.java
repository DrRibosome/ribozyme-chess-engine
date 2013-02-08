package search.statsStore;

import state4.State4;

public class StatsStore {
	/**
	 * records scores found at each pv level of the search
	 * @param s
	 * @param player
	 * @param maxPly max ply searched to (ie, number of scores stored
	 * @param scores array storing seen scores
	 * @return
	 */
	public static String recordPVScores(State4 s, int player, int maxPly, double[] scores){
		String pieceCounts = "";
		for(int q = 0; q < 2; q++){
			pieceCounts += "<piece-counts-"+q+"=";
			for(int a = 0; a < s.pieceCounts[0].length; a++){
				pieceCounts += s.pieceCounts[q][a];
				if(a != s.pieceCounts[0].length-1)
					pieceCounts+=",";
			}
			pieceCounts += ">";
		}
		String scorestr = "<scores=";
		for(int a = 0; a < maxPly && a < scores.length; a++){
			scorestr += scores[a];
			if(a != maxPly-1)
				scorestr += ",";
		}
		scorestr += ">";
		String record = "<pv-score <turn="+player+">"+pieceCounts+scorestr+">";
		return record;
	}
	
	public static String recordBranchingFactor(double branchingFactor){
		return "<branching-factor = "+branchingFactor+">";
	}
}
