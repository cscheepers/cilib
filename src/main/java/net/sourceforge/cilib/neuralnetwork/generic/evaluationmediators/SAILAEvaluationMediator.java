/*
 * Created on 2004/11/30
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.sourceforge.cilib.neuralnetwork.generic.evaluationmediators;

import java.util.ArrayList;

import net.sourceforge.cilib.neuralnetwork.generic.datacontainers.SAILARealData;
import net.sourceforge.cilib.neuralnetwork.generic.errorfunctions.MSEErrorFunction;
import net.sourceforge.cilib.neuralnetwork.foundation.EvaluationMediator;
import net.sourceforge.cilib.neuralnetwork.foundation.NNError;
import net.sourceforge.cilib.neuralnetwork.foundation.NNPattern;
import net.sourceforge.cilib.neuralnetwork.foundation.NeuralNetworkDataIterator;
import net.sourceforge.cilib.type.types.MixedVector;


/**
 * @author stefanv
 *
 */
public class SAILAEvaluationMediator extends EvaluationMediator {
	
	protected NeuralNetworkDataIterator iteratorDt = null;
	protected NeuralNetworkDataIterator iteratorDg = null;
	
	//termination criterion 1
	int maxSubsetEpochs;
	int subsetEpochCounter;
	
	//termination criterion 2  -- select first element of base array as reference error to clone
	NNError subsetBeginErrorDt = null;
	NNError subsetBeginErrorDg = null;
	
	//termination criterion 3
	double minimumErrorDecrease;
	NNError errorDtPrevious;
	
	//termination criterion 4
	double averageErrorDg = 0.0;
	int epochNumber = 0;
	ArrayList<NNError> history = null;
	
	
	public SAILAEvaluationMediator() {
		super();	
		this.minimumErrorDecrease = 0.001;
		this.maxSubsetEpochs = 100;
		this.subsetEpochCounter = 0;
	}
			
	
	@Override
	public void initialize() {
		
		if (this.data == null)  {			
			throw new IllegalArgumentException("Evaluation Strategy error: required data object was null");
		}
		if (this.topology == null)  {			
			throw new IllegalArgumentException("Evaluation Strategy error: required topology object was null");
		}
		if (this.prototypeError == null)  {			
			throw new IllegalArgumentException("Evaluation Strategy error: required prototypeError object was null");
		}
		if (this.trainer == null)  {			
			throw new IllegalArgumentException("Evaluation Strategy error: required trainer object was null");
		}
		
		//Initialize objects, depth first via Chain of Responsibility pattern.
		data.initialize();
		topology.initialize();
		trainer.setTopology(this.topology);
		trainer.initialize();
		
		this.data.activeLearningUpdate(null);		
		
		this.errorDg = new NNError[prototypeError.length];
		this.errorDt = new NNError[prototypeError.length];
		this.errorDv = new NNError[prototypeError.length];
		
		for (int i=0; i < prototypeError.length; i++){
			
			this.errorDg[i] = prototypeError[i].clone();
			this.errorDg[i].setNoPatterns(this.data.getGeneralisationSetSize());
			this.errorDt[i] = prototypeError[i].clone();
			this.errorDt[i].setNoPatterns(this.data.getTrainingSetSize());
			this.errorDv[i] = prototypeError[i].clone();
			this.errorDv[i].setNoPatterns(this.data.getValidationSetSize());
		}
		
		//Always select the first error in the array as the reference for Active learning comparions.
		subsetBeginErrorDt = prototypeError[0].clone();
		subsetBeginErrorDt.setNoPatterns(((SAILARealData)data).getNrUpdates());
		subsetBeginErrorDg = prototypeError[0].clone();
		subsetBeginErrorDg.setNoPatterns(1);
		
		iteratorDt = data.getTrainingSetIterator();
		iteratorDg = data.getGeneralisationSetIterator();
		
		//set the initial error to the error on Dt = 1 pattern.
		MixedVector tmpO = topology.evaluate(iteratorDt.value());
		subsetBeginErrorDt.computeIteration(tmpO, iteratorDt.value());
		subsetBeginErrorDg.computeIteration(tmpO, iteratorDg.value());
		subsetBeginErrorDt.finaliseError();
		subsetBeginErrorDg.finaliseError();
		
		//set the recorded error for criterion 3 to initial error as well
		errorDtPrevious = this.prototypeError[0].clone();
		errorDtPrevious.setValue(new Double(9999999));
		
		//criterion 4:
		history = new ArrayList<NNError>();
		
		
	}




	public void learningEpoch(){
					
		//reset errors
		this.resetError(this.errorDt);
		this.setErrorNoPatterns(this.errorDt, this.data.getTrainingSetSize());
		
		this.resetError(this.errorDg);
		this.setErrorNoPatterns(this.errorDg, this.data.getGeneralisationSetSize());		
		
		//determine training error, perform learning steps
		//================================================
		iteratorDt = data.getTrainingSetIterator();
		iteratorDg = data.getGeneralisationSetIterator();
			
		trainer.preEpochActions(null);
		
		//iterate over training set
		while(iteratorDt.hasMore()){
						
			MixedVector outputDt = topology.evaluate(iteratorDt.value());
						
			//compute the per pattern error, use it to train the topology stochastically by default
			this.computeErrorIteration(this.errorDt,outputDt, iteratorDt.value());
						
			trainer.invokeTrainer(iteratorDt.value());
							
			iteratorDt.next();
		}
		
		trainer.postEpochActions(null);
				
		//determine generalization error
		//==========================
		iteratorDg = data.getGeneralisationSetIterator();
		
		while(iteratorDg.hasMore()){
			
			MixedVector outputDg = topology.evaluate(iteratorDg.value());
						
			//compute the per pattern error, use it to train the topology stochastically be default
			this.computeErrorIteration(this.errorDg, outputDg, iteratorDg.value());
									
			iteratorDg.next();
		}
				
		//finalise errors
		this.finaliseErrors(this.errorDt);
		this.finaliseErrors(this.errorDg);
		
		
		
		
		
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//%%  Determine one of the subset termination criteria has been reached.  %%
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		
		boolean terminate = false;		
		
		
		
		//Criterion 1: maximum epochs per subset reached
		//----------------------------------------------
		subsetEpochCounter++;
		if(subsetEpochCounter >= maxSubsetEpochs){
			terminate = true;
			System.out.println("___________SAILA Termination criterion 1: Maximum epochs reached");
		}
		
		//criterion 2: error decreased by more than 80%
		//---------------------------------------------
		double decreasePercentageDt = ((Double)this.errorDt[0].getValue() / (Double)subsetBeginErrorDt.getValue()) * 100;
		double decreasePercentageDg = ((Double)this.errorDg[0].getValue() / (Double)subsetBeginErrorDg.getValue()) * 100;
		
		if ((decreasePercentageDt < 20.0) || (decreasePercentageDg < 20.0)){
			terminate = true;
			System.out.println("___________SAILA Termination criterion 2: error decreased by 80%.  Begin = " + (Double)subsetBeginErrorDt.getValue() + ", end = " +
			(Double)this.errorDt[0].getValue() );
		}
		
		
		//criterion 3: average decrease per epoch too small		
		//-------------------------------------------------
		
		//update minimum decrease factor
		if ( (Double)this.errorDt[0].getValue() < (subsetBeginErrorDt.getValue().doubleValue() / 10.0)){
			minimumErrorDecrease /= 10.0;
		}
		
		if( ((errorDtPrevious.getValue() - (Double)this.errorDt[0].getValue()) < minimumErrorDecrease) 
			&& (errorDtPrevious.getValue() - (Double)this.errorDt[0].getValue() > 0)  ){
			terminate = true;
			System.out.println("___________SAILA Termination criterion 3: average error too small. min = " + minimumErrorDecrease 
					          + ", actual = " + (errorDtPrevious.getValue() - (Double)this.errorDt[0].getValue()) + ", errorPrev = " + errorDtPrevious.getValue().doubleValue());
		}
				
		//save the local var this.errorDt[0] as previous
		errorDtPrevious.setValue(((Double)this.errorDt[0].getValue()).doubleValue());
				
		
		//criterion 4: Error on Dg increases too much
		//----------------------------------------------
		
		//averageNew = (averageOld * periodsOld + new_sample) / (periodsOld + 1)
		epochNumber++;
		averageErrorDg = ((double)(averageErrorDg * (epochNumber - 1)) + ((Double)this.errorDg[0].getValue()).doubleValue())
						  / (double)epochNumber;
		
		history.add(this.errorDt[0]);
		double errorDgStdDev = 0;
		
		for(int i = 0; i < history.size(); i++){
			errorDgStdDev += Math.pow(((Double)history.get(i).getValue()).doubleValue() - averageErrorDg, 2);
		}
		
		errorDgStdDev = Math.sqrt(errorDgStdDev / (double)history.size());		
		
		
		if(((Double)this.errorDg[0].getValue()).doubleValue() > (averageErrorDg + errorDgStdDev)){
			terminate = true;
			System.out.println("___________SAILA Termination criterion 4: Validation error rising too much, overfitting detected");
		}
		
		
		//===================================================
		//=  if termination criteria --> update Dt from Dc  =
		//===================================================
		
		if(terminate){
			
			data.activeLearningUpdate(null);
						
			//criterion 1 reset
			subsetEpochCounter = 0;
			
			//criterion 2 reset
			((MSEErrorFunction)subsetBeginErrorDt).setValue(((Double)this.errorDt[0].getValue()).doubleValue());
			((MSEErrorFunction)subsetBeginErrorDg).setValue(((Double)this.errorDg[0].getValue()).doubleValue());
			
			
		}
		
		data.shuffleTrainingSet();
		
	}


	
	public MixedVector evaluate(NNPattern p) {
		return topology.evaluate(p);
	}
	
}
