import org.jgap.*;
import org.jgap.impl.*;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

import java.io.IOException;

@SuppressWarnings("serial")
public class runGA extends FitnessFunction {
    public static final int MAX_GENERATIONS = 10;
    public static final int POPULATION_SIZE = 30;
    public static final int CHROMOSOME_AMOUNT = 6;
    public static final int NUMBER_OF_ROUNDS = 50;
    public static int robotScore,enemyScore;
    public static final String robotToEnvolve = "edo.FatorIntegrante 1.0*";
    public static final String trainingRobots = "FatorIntegrante 1.0,edo.FatorIntegrante*";
    public static final String robocodePath = "C:/robocode";

    public void run() throws Exception {

        Configuration conf = new DefaultConfiguration();
        conf.addGeneticOperator(new MutationOperator(conf, 100));
        conf.setPreservFittestIndividual(true);
        conf.setFitnessFunction(this);

        Gene[] sampleGenes = new Gene[ CHROMOSOME_AMOUNT ];
        sampleGenes[0] = new DoubleGene(conf, 15, 360); // corpo direita
        sampleGenes[1] = new DoubleGene(conf, 15, 360); // corpo esquerda
        sampleGenes[2] = new DoubleGene(conf, 0, 90); // virar canhao p direita
        sampleGenes[3] = new DoubleGene(conf, 0, 90); // virar canhao p esquerda
        sampleGenes[4] = new DoubleGene(conf, 0, 360); // angulo de virada qdo perto de parede
        sampleGenes[5] = new DoubleGene(conf, 50, 250); // quao perto detectar colisao da parede

        IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome(sampleChromosome);
        conf.setPopulationSize(POPULATION_SIZE);

        Genotype population = Genotype.randomInitialGenotype(conf);
        IChromosome fittestSolution = null;

        for ( int gen = 0; gen<MAX_GENERATIONS; gen++ ) {
            population.evolve();
            fittestSolution = population.getFittestChromosome(); // find fittest of population
            System.out.printf("\nafter %d generations the best solution is %s \n",gen + 1,fittestSolution);
        }

        buildRobot(fittestSolution);
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        new runGA().run();
    }

    public boolean isRobot(String name){
        return name.equals(robotToEnvolve);
    }

    private void buildRobot(IChromosome chromosome) {
        double[] chromo = new double[CHROMOSOME_AMOUNT];
        int i = 0;
        for (Gene gene : chromosome.getGenes() ) {
            chromo[i] += (Double) gene.getAllele();
            i++;
        }
        String[] s = new String[1];
        s[0] = chromo[0] + " " + chromo[1] + " " + chromo[2] + " " + chromo[3] + " " + chromo[4] + " " + chromo[5] + "\n";
        GenesWriter.main(s);
        createRobot.create(chromo);
    }

    @Override
    protected double evaluate(IChromosome chromosome) {
        buildRobot(chromosome);
        RobocodeEngine engine = new RobocodeEngine(new java.io.File(robocodePath));
        engine.addBattleListener(new battleObserver());
        engine.setVisible(false);

        BattlefieldSpecification battlefield = new BattlefieldSpecification(800, 600);
        RobotSpecification[] selectedRobots = engine.getLocalRepository(trainingRobots);
        BattleSpecification battleSpec = new BattleSpecification(NUMBER_OF_ROUNDS, battlefield, selectedRobots);

        engine.runBattle(battleSpec, true);
        engine.close();
        return robotScore > 0 ? robotScore : 0;
    }

    public void setScore(int roboScore,int enemScore){
        robotScore = roboScore;
        enemyScore = enemScore;
    }
}