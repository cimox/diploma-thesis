import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.core.TimingUtils;
import moa.streams.generators.*;
import com.yahoo.labs.samoa.instances.Instance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class App {
    public static void main(String[] args) {
        System.out.println("Running experiment");

//        Experiment exp = new Experiment();
        ExperimentConceptDetection exp = new ExperimentConceptDetection();
        exp.run(150001, true);
    }

    private static class Experiment {
        private FileWriter file;
        private BufferedWriter bw;
        private PrintWriter fileWriter;

        public Experiment() {
            try {
                this.file = new FileWriter("tree-training.json", true);
                this.bw = new BufferedWriter(file);
                this.fileWriter = new PrintWriter(this.bw);
                this.fileWriter.println("[");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(int numInstances, boolean isTesting) {
//            Classifier learner = new EnhancedHoeffdingTree(this.conceptFileWriter, numInstances/1000);
            Classifier learner = new HoeffdingAdaptiveTree();
            RandomRBFGenerator stream = new RandomRBFGenerator();
            stream.numClassesOption = new IntOption("numClasses", 'c',
                    "The number of classes to generate.", 3, 3, Integer.MAX_VALUE);
            stream.prepareForUse();

            learner.setModelContext(stream.getHeader());
            learner.prepareForUse();

            int numberSamplesCorrect = 0;
            int numberSamples = 0;
            boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
            long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
            while (stream.hasMoreInstances() && numberSamples < numInstances) {
                Instance trainInst = stream.nextInstance().getData();
                if (isTesting) {
                    if (learner.correctlyClassifies(trainInst)) {
                        numberSamplesCorrect++;
                    }
                }
                numberSamples++;
                if (numberSamples % 100000 == 0) {
                    System.out.println(numberSamples + " samples processed");
                }
                learner.trainOnInstance(trainInst);
            }
            this.fileWriter.println("]");
            this.fileWriter.close();
            double accuracy = 100.0 * (double) numberSamplesCorrect / (double) numberSamples;
            double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread() - evaluateStartTime);
            System.out.println(numberSamples + " instances processed with " + accuracy + "% accuracy in " + time + " seconds.");

            StringBuilder out = new StringBuilder();
            learner.getDescription(out, 4);
            System.out.println(out.toString());
        }
    }

    private static class ExperimentConceptDetection {
        private FileWriter file;
        private BufferedWriter bw;
        private PrintWriter conceptFileWriter;

        public ExperimentConceptDetection() {
            try {
                // Concept file writer
                this.file = new FileWriter("tree-concepts.csv", true);
                this.bw = new BufferedWriter(file);
                this.conceptFileWriter = new PrintWriter(this.bw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(int numInstances, boolean isTesting) {
//            Classifier learner = new EnhancedHoeffdingTree(this.conceptFileWriter, numInstances/1000);
            Classifier learner = new ConceptDetectionTree(this.conceptFileWriter);
//            RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift();
//            stream.numClassesOption = new IntOption("numClasses", 'c',
//                    "The number of classes to generate.", 10, 3, Integer.MAX_VALUE);
//            stream.speedChangeOption = new FloatOption("speedChange", 's',
//                    "Speed of change of centroids in the model.", 100, 10, Float.MAX_VALUE);
            LEDGeneratorDrift stream = new LEDGeneratorDrift();
            stream.numberAttributesDriftOption = new IntOption("numberAttributesDrift",
                    'd', "Number of attributes with drift.", 1, 0, 17);
            stream.prepareForUse();

            learner.setModelContext(stream.getHeader());
            learner.prepareForUse();

            int numberSamplesCorrect = 0;
            int numberSamples = 0;
            boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
            long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
            while (stream.hasMoreInstances() && numberSamples < numInstances) {
                Instance trainInst = stream.nextInstance().getData();
                if (isTesting) {
                    if (learner.correctlyClassifies(trainInst)) {
                        numberSamplesCorrect++;
                    }
                }
                numberSamples++;
                if (numberSamples % 100000 == 0) {
                    System.out.println(numberSamples + " samples processed");
                }
                this.conceptFileWriter.print("\"" + numberSamples + "\", ");
                learner.trainOnInstance(trainInst);
                this.conceptFileWriter.print("\n");
            }
            // Close files
            this.conceptFileWriter.close();


            double accuracy = 100.0 * (double) numberSamplesCorrect / (double) numberSamples;
            double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread() - evaluateStartTime);
            System.out.println(numberSamples + " instances processed with " + accuracy + "% accuracy in " + time + " seconds.");

//            Print resulting tree to console
//            StringBuilder out = new StringBuilder();
//            learner.getDescription(out, 4);
//            System.out.println(out.toString());
        }
    }
}
