package eval.expEvalV3;

import static eval.evalV8.Weight.W;

import java.awt.dnd.DnDConstants;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import state4.State4;

public final class EvalConstantsV2 {
	//general weights
	public final int[] materialWeights = new int[7];
	/** stores max number of moves by piece type (major minor pieces only)*/
	public final static int[] maxPieceMobility;
	/** mobility bonus for 3 levels of mobility, by piece type*/
	public final Weight[][] mobilityWeight;
	
	//bishop weights
	public final Weight bishopPairWeight;
	
	//pawn weights
	/** weights for pawns in given row, indexed [player][row]*/
	public final Weight[][] passedPawnRowWeight = new Weight[2][8];
	public final Weight[][] unopposedPawnWeight = new Weight[2][8];
	public final Weight doubledPawnsWeight;
	public final Weight tripledPawnsWeight;
	public final Weight[][] supportedPassedPawn = new Weight[2][8];
	
	//king danger
	/** danger index increment associated with each piece type*/
	public final int[] dangerKingAttacks = new int[7];
	public final static int contactCheckQueen = 6;
	public final static int contactCheckRook = 4;
	public final static int queenCheck = 3;
	public final static int rookCheck = 2;
	public final static int knightCheck = 1;
	public final static int bishopCheck = 1;
	public final Weight[] kingDangerValues = new Weight[64];
	
	static{
		maxPieceMobility = new int[7];
		maxPieceMobility[State4.PIECE_TYPE_BISHOP] = 14;
		maxPieceMobility[State4.PIECE_TYPE_KNIGHT] = 8;
		maxPieceMobility[State4.PIECE_TYPE_ROOK] = 15;
		maxPieceMobility[State4.PIECE_TYPE_QUEEN] = 28;
	}
	
	
	
	public EvalConstantsV2(final EvalParameters p){
		assert materialWeights.length == 7;
		assert passedPawnRowWeight.length == 8; //interpreted symmetrically for each player
		
		System.arraycopy(materialWeights, 0, this.materialWeights, 0, 7);
		this.mobilityWeight = mobilityWeight;
		for(int a = 0; a < 8; a++){
			this.passedPawnRowWeight[0][a] = passedPawnRowWeight[a];
			this.passedPawnRowWeight[1][a] = passedPawnRowWeight[7-a];
		}
		
		this.unopposedPawnWeight = unopposedPawnWeight;
		this.supportedPassedPawn = supportedPassedPawnWeight;
		this.doubledPawnsWeight = doubledPawnsWeight;
		this.tripledPawnsWeight = tripledPawnWeight;
		this.bishopPairWeight = bishopPairWeight;
		
		System.arraycopy(dangerKingAttacks, 0, this.dangerKingAttacks, 0, 7);
		

		
		final int maxSlope = 25;
		final int maxDanger = 800;
		for(int x = 0, i = 0; i < kingDangerValues.length; i++){
			x = Math.min(maxDanger, Math.min((i * i) / 2, x + maxSlope));
			kingDangerValues[i] = new Weight(x, 0, materialWeights);
		}
	}
	
	/** write weight values to passed output stream*/
	public static void saveWeights(EvalConstantsV2 e, OutputStream os) throws IOException{
		DataOutputStream dos = new DataOutputStream(os);
		for(int a = 0; a < 7; a++){
			dos.writeShort(e.materialWeights[a]);
		}
		for(int a = 0; a < 7; a++){
			for(int q = 0; q < e.mobilityWeight[a].length; q++){
				Weight.writeWeight(e.mobilityWeight[a][q], dos);
			}
		}
		for(int a = 0; a < 8; a++){
			dos.writeShort(e.passedPawnRowWeight[0][a]);
		}
		dos.writeShort(e.unopposedPawnWeight);
		dos.writeShort(e.supportedPassedPawn);
		dos.writeShort(e.doubledPawnsWeight);
		dos.writeShort(e.tripledPawnsWeight);
		dos.writeShort(e.bishopPairWeight);
		
		for(int a = 0; a < e.dangerKingAttacks.length; a++){
			dos.writeShort(e.dangerKingAttacks[a]);
		}
	}
	
	public static EvalConstantsV2 loadWeights(InputStream is) throws IOException{
		final int[] materialWeights = new int[7];
		final int[][] mobilityWeight = new int[7][3];
		final int[] passedPawnRowWeight = new int[8];
		
		DataInputStream dis = new DataInputStream(is);
		for(int a = 0; a < 7; a++){
			materialWeights[a] = dis.readShort();
		}
		for(int a = 0; a < 7; a++){
			for(int q = 0; q < 3; q++){
				mobilityWeight[a][q] = dis.readShort();
			}
		}
		for(int a = 0; a < 8; a++){
			passedPawnRowWeight[a] = dis.readShort();
		}
		final int unopposedPawnWeight = dis.readShort();
		final int supportedPassedPawn = dis.readShort();
		final int doubledPawnsWeight = dis.readShort();
		final int tripledPawnsWeight = dis.readShort();
		final int bishopPairWeight = dis.readShort();
		
		final int[] dangerKingAttacks = new int[7];
		for(int a = 0; a < dangerKingAttacks.length; a++){
			dangerKingAttacks[a] = dis.readShort();
		}
		
		return new EvalConstantsV2(
				materialWeights,
				mobilityWeight,
				passedPawnRowWeight,
				unopposedPawnWeight,
				supportedPassedPawn,
				doubledPawnsWeight,
				tripledPawnsWeight,
				bishopPairWeight,
				dangerKingAttacks
				);
	}
}
