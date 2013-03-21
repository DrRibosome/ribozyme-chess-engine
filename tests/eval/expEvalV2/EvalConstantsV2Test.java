package eval.expEvalV2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

public class EvalConstantsV2Test {

	@Test
	public void testSaveWeights() {
		try{
			EvalConstantsV2 t1 = EvalConstantsV2.defaultEval();
			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream();
			pis.connect(pos);
			EvalConstantsV2.saveWeights(t1, pos);
			EvalConstantsV2 t2 = EvalConstantsV2.loadWeights(pis);

			assertEquals(t1.unopposedPawnWeight, t2.unopposedPawnWeight);
			assertEquals(t1.doubledPawnsWeight, t2.doubledPawnsWeight);
			assertEquals(t1.tripledPawnsWeight, t2.tripledPawnsWeight);
			assertEquals(t1.supportedPassedPawn, t2.supportedPassedPawn);
			assertEquals(t1.bishopPairWeight, t2.bishopPairWeight);
			for(int a = 0; a < 7; a++){
				assertEquals(t1.materialWeights[a], t2.materialWeights[a]);
				assertEquals(t1.dangerKingAttacks[a], t2.dangerKingAttacks[a]);
				
				for(int q = 0; q < 3; q++){
					assertEquals(t1.mobilityWeight[a][q], t2.mobilityWeight[a][q]);
				}
			}
			
			for(int a = 0; a < 8; a++){
				assertEquals(t1.passedPawnRowWeight[0][a], t2.passedPawnRowWeight[0][a]);
				assertEquals(t1.passedPawnRowWeight[1][a], t2.passedPawnRowWeight[1][a]);
			}
		} catch(IOException e){
			fail();
		}
	}

}
