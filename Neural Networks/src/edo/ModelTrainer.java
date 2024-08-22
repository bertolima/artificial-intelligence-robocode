package edo;

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.training.propagation.sgd.StochasticGradientDescent;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.TrainingSetUtil;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationReLU;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.io.File;

import static org.encog.persist.EncogDirectoryPersistence.*;

public class ModelTrainer {

    public static void main(final String args[]) {

        final MLDataSet df = TrainingSetUtil.loadCSVTOMemory(CSVFormat.ENGLISH,
                "/home/luis/workspace/java/artificial-intelligence-robocode/Neural Networks/normalized_df.csv", false, 5, 2);
//
//         create a neural network, without using a factory
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(null,true,5));
        network.addLayer(new BasicLayer(new ActivationReLU(),true,60));
        network.addLayer(new BasicLayer(new ActivationReLU(),true,6));
        network.addLayer(new BasicLayer(null,true,2));
        network.getStructure().finalizeStructure();
        network.reset();

        // train the neural network
        final StochasticGradientDescent  train = new StochasticGradientDescent (network, df);
        train.setLearningRate(0.0001d);

        int epoch = 1;

        do {
            train.iteration();
            if(epoch % 1000 == 0)System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            Writer.main("loss_vector.csv", String.format("%.12f", train.getError()) + "\n");
            epoch++;
        } while (train.getError() > 0.00000001);
        train.finishTraining();
//
        String filename = "network.eg";
//
// save network...
        saveObject(new File(filename), network);

// load network...
//        BasicNetwork network = (BasicNetwork) loadObject(new File("/home/luis/workspace/java/artificial-intelligence-robocode/Neural Networks/network.eg"));
////        double input []= {400, 500, 150, 14, 200, 130};
////
////        MLData data = new BasicMLData(input);
////
////        final MLData output = network.compute(data);
////
////        System.out.println(output);
//
//        CreateFile.main("comparison.csv");
//        System.out.println("Neural Network Results:");
//        for(MLDataPair pair: df ) {
//            final MLData output = network.compute(pair.getInput());
//            Writer.main("comparison.csv",output.getData(0)+ "," + output.getData(1) + "," + pair.getIdeal().getData(0) + "," + pair.getIdeal().getData(1) + "\n");
//        }

        Encog.getInstance().shutdown();
    }
}
