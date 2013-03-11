package eval.expEvalV2;

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
	public final int[][] mobilityWeight = new int[7][3];
	
	//bishop weights
	public final int bishopPairWeight;
	
	//pawn weights
	/** weights for pawns in given row, indexed by player,row*/
	public final int[][] passedPawnRowWeight = new int[2][8];
	public final int unopposedPawnWeight;
	public final int doubledPawnsWeight;
	public final int tripledPawnsWeight;
	public final int supportedPassedPawn;
	
	static{
		maxPieceMobility = new int[7];
		maxPieceMobility[State4.PIECE_TYPE_BISHOP] = 14;
		maxPieceMobility[State4.PIECE_TYPE_KNIGHT] = 8;
		maxPieceMobility[State4.PIECE_TYPE_ROOK] = 15;
		maxPieceMobility[State4.PIECE_TYPE_QUEEN] = 28;
	}
	
	public static EvalConstantsV2 defaultEval(){
		final int[] materialWeights = new int[7];
		materialWeights[State4.PIECE_TYPE_BISHOP] = 310;
		materialWeights[State4.PIECE_TYPE_KNIGHT] = 300;
		materialWeights[State4.PIECE_TYPE_ROOK] = 500;
		materialWeights[State4.PIECE_TYPE_QUEEN] = 900;
		materialWeights[State4.PIECE_TYPE_PAWN] = 100;

		final int s = 1; //scale
		final int[][] mobilityWeight = new int[7][3];
		mobilityWeight[State4.PIECE_TYPE_BISHOP] = new int[]{	-10/s,	8/s,	30/s};
		mobilityWeight[State4.PIECE_TYPE_KNIGHT] = new int[]{	-20/s,	8/s,	30/s};
		mobilityWeight[State4.PIECE_TYPE_ROOK] = new int[]{		-15/s,	20/s,	30/s};
		mobilityWeight[State4.PIECE_TYPE_QUEEN] = new int[]{	0/s,	15/s,	20/s};
		
		final int[] passedPawnRowWeight = new int[]{0, 0, 10, 20, 35, 80, 130, 0};
		
		final int unopposedPawnWeight = 5;
		final int doubledPawnsWeight = -10;
		final int tripledPawnsWeight = -15;
		final int supportedPassedPawn = 5;
		final int bishopPairWeight = 20;
		
		return new EvalConstantsV2(
				materialWeights,
				mobilityWeight,
				passedPawnRowWeight,
				unopposedPawnWeight,
				supportedPassedPawn,
				doubledPawnsWeight,
				tripledPawnsWeight,
				bishopPairWeight
				);
	}
	
	public EvalConstantsV2(final int[] materialWeights,
			final int[][] mobilityWeight,
			final int[] passedPawnRowWeight,
			final int unopposedPawnWeight,
			final int supportedPassedPawnWeight,
			final int doubledPawnsWeight,
			final int tripledPawnWeight,
			final int bishopPairWeight){
		assert materialWeights.length == 7;
		assert mobilityWeight.length == 7 && mobilityWeight[0].length == 3;
		assert passedPawnRowWeight.length == 8; //interpreted symmetrically for each player
		
		System.arraycopy(materialWeights, 0, this.materialWeights, 0, 7);
		for(int a = 0; a < 7; a++){
			System.arraycopy(mobilityWeight[a], 0, this.mobilityWeight[a], 0, 3);
		}
		for(int a = 0; a < 8; a++){
			this.passedPawnRowWeight[0][a] = passedPawnRowWeight[a];
			this.passedPawnRowWeight[1][a] = passedPawnRowWeight[7-a];
		}
		
		this.unopposedPawnWeight = unopposedPawnWeight;
		this.supportedPassedPawn = supportedPassedPawnWeight;
		this.doubledPawnsWeight = doubledPawnsWeight;
		this.tripledPawnsWeight = tripledPawnWeight;
		this.bishopPairWeight = bishopPairWeight;
	}
	
	/** write weight values to passed output stream*/
	public static void saveWeights(EvalConstantsV2 e, OutputStream os) throws IOException{
		DataOutputStream dos = new DataOutputStream(os);
		for(int a = 0; a < 7; a++){
			dos.writeShort(e.materialWeights[a]);
		}
		for(int a = 0; a < 7; a++){
			for(int q = 0; q < 3; q++){
				dos.writeShort(e.mobilityWeight[a][q]);
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
		
		return new EvalConstantsV2(
				materialWeights,
				mobilityWeight,
				passedPawnRowWeight,
				unopposedPawnWeight,
				supportedPassedPawn,
				doubledPawnsWeight,
				tripledPawnsWeight,
				bishopPairWeight
				);
	}
}
