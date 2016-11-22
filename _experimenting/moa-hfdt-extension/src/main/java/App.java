import com.github.javacliparser.IntOption;
import moa.classifiers.Classifier;
import moa.core.TimingUtils;
import moa.streams.generators.RandomRBFGenerator;
import com.yahoo.labs.samoa.instances.Instance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class App {
    public static void main(String[] args) {
        System.out.println("Running experiment");

        Experiment exp = new Experiment();
        exp.run(1501, true);
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
            Classifier learner = new EnhancedHoeffdingTree(this.fileWriter, numInstances/1000);
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
}
