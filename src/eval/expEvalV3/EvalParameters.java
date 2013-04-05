package eval.expEvalV3;

import java.io.IOException;
import java.nio.ByteBuffer;

import state4.State4;

public final class EvalParameters {

	//-------------------------------------------------
	//general weights
	
	public int[] materialWeights;
	/** mobility weights, indexed [piece-type][movement]*/
	public Weight[][] mobilityWeights;
	public Weight tempo;
	public Weight bishopPair;
	
	//-------------------------------------------------
	//king danger
	
	/** danger for attacks on the king, indexed [piece-type]*/
	public int[] dangerKingAttacks;
	/** king danger index, indexed [danger]*/
	public Weight[] kingDangerValues;
	/** king danger based off location of the king*/
	public int[][] kingDangerSquares;
	
	//-------------------------------------------------
	//pawns weights
	
	/** passed pawn weight, indexed [row] from white perspective*/
	public Weight[][] passedPawnRowWeight;
	public Weight doubledPawnsWeight;
	public Weight tripledPawnsWeight;
	
	
	
	
	public void write(final ByteBuffer b){

		//general values
		
		for(int a = 0; a < 7; a++) b.putShort((short)materialWeights[a]);
		
		writeMatrix(mobilityWeights, b);
		
		tempo.writeWeight(b);
		bishopPair.writeWeight(b);
		
		//king values

		for(int a = 0; a < 7; a++) b.putShort((short)dangerKingAttacks[a]);
		
		b.putShort((short)kingDangerValues.length);
		for(int a = 0; a < kingDangerValues.length; a++) kingDangerValues[a].writeWeight(b);
		
		for(int a = 0; a < 2; a++){
			for(int q = 0; q < 64; q++){
				b.putShort((short)kingDangerSquares[a][q]);
			}
		}
		
		//pawn values
		
		writeMatrix(passedPawnRowWeight, b);
		doubledPawnsWeight.writeWeight(b);
		tripledPawnsWeight.writeWeight(b);
		
	}
	
	public void read(final ByteBuffer b){

		materialWeights = new int[7];
		for(int a = 0; a < 7; a++) materialWeights[a] = b.getShort();

		mobilityWeights = readMatrix(b);

		tempo = Weight.readWeight(b);
		bishopPair = Weight.readWeight(b);

		//king values

		dangerKingAttacks = new int[7];
		for(int a = 0; a < 7; a++) dangerKingAttacks[a] = b.getShort();

		kingDangerValues = new Weight[b.getShort()];
		for(int a = 0; a < kingDangerValues.length; a++) kingDangerValues[a] = Weight.readWeight(b);
		
		kingDangerSquares = new int[2][64];
		for(int a = 0; a < 2; a++){
			for(int q = 0; q < 64; q++){
				kingDangerSquares[a][q] = b.getShort();
			}
		}
		
		
		//pawn values
		
		passedPawnRowWeight = readMatrix(b);
		doubledPawnsWeight = Weight.readWeight(b);
		tripledPawnsWeight = Weight.readWeight(b);
		
	}
	
	private static void writeMatrix(final Weight[][] w, final ByteBuffer b){
		b.putShort((short)w.length);
		for(int a = 0; a < w.length; a++){
			b.putShort((short)w[a].length);
			for(int q = 0; q < w[a].length; q++){
				w[a][q].writeWeight(b);
			}
		}
	}
	
	private static Weight[][] readMatrix(ByteBuffer b){
		Weight[][] w = new Weight[b.getShort()][];
		for(int a = 0; a < w.length; a++){
			w[a] = new Weight[b.getShort()];
			for(int q = 0; q < w[a].length; q++){
				w[a][q] = Weight.readWeight(b);
			}
		}
		return w;
	}
}
