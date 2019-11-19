/*
@author Arthur Godet <arth.godet@gmail.com>
@since 07/03/2019
*/
package bench;

import constraint.PMSPAURModel;
import data.Factory;
import data.input.Instance;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import org.chocosolver.solver.Solution;

public class Bench {

    // returns all instances contained in the folder described by path, all instances being sorted by increasing number of jobs
    private static Instance[] getAllInstances(String path) {
        File[] files = Factory.listAllFiles(path, ".json", true);
        return Arrays.stream(files).map(f -> Factory.fromFile(f.getAbsolutePath(), Instance.class)).sorted(Comparator.comparingInt(i -> i.getJobs().size())).toArray(Instance[]::new);
    }

    private static String existingInstance(String fileName, String instanceName) throws IOException {
        Scanner scanner = new Scanner(new FileReader(fileName));
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            if (str.split(";")[0].equals(instanceName)) {
                scanner.close();
                return str;
            }
        }
        scanner.close();
        return null;
    }

    public static void bench(int nbMinutes, Configuration... configs) throws IOException {
        FileWriter[] writers = new FileWriter[configs.length];
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new FileWriter(configs[i].getPath(), true);
            if (existingInstance(configs[i].getPath(), "Instance") == null) {
                writers[i].write("Instance;Time to Proof (ms);Time to Best (ms);BestFound;nbNodes;nbBacktracks;nbFails;\n");
            }
        }

        Instance[] instances = getAllInstances("data/");
        for (int j = 0; j < instances.length; j++) {
            Instance inst = instances[j];
            System.out.println(inst.getName() + " --> " + (1.0 * (j + 1) / instances.length));
            for (int i = 0; i < configs.length; i++) {
                System.out.println(configs[i].toString());
                exec(inst, writers[i], configs[i], nbMinutes);
            }
        }

        for (FileWriter fw : writers) {
            fw.close();
        }
    }

    private static void exec(Instance inst, FileWriter writer, Configuration config, int nbMinutes) throws IOException {
        String str = existingInstance(config.getPath(), inst.getName());
        if (str == null) {
            str = exeInst(inst, config, nbMinutes);
            writer.write(str + "\n");
            writer.flush();
        }
        System.out.println(str);
    }

    private static Instance buildInstance(String path) {
        return Factory.fromFile(path, Instance.class);
    }

    private static String exeInst(Instance inst, Configuration config, int nbMinutes) {
        PMSPAURModel model = new PMSPAURModel(inst, config.getParameters()[0][0],
            config.getParameters()[0][1], config.getParameters()[1], config.getSearch());
        Object[] res = model.solve(nbMinutes);
        Solution sol = (Solution) res[0];
        long timeToBestSolution = (long) res[1];
        long timeToProof = (long) res[2];

        return inst.getName() + ";"
            + timeToProof / 1000000 + ";"
            + timeToBestSolution / 1000000 + ";"
            + (sol == null ? "noSolution" : sol.getIntVal(model.getC())) + ";"
            + model.getModel().getSolver().getNodeCount() + ";"
            + model.getModel().getSolver().getBackTrackCount() + ";"
            + model.getModel().getSolver().getFailCount() + ";";
    }

    public static void main(String[] args) throws IOException {
        /*
        Configuration configuration = Configuration.valueOf(args[0]);
        File[] files = Factory.listAllFiles("data/", ".json", true);
        Instance[] instances = Arrays.stream(files).sorted().map(f -> Factory.fromFile(f.getAbsolutePath(), Instance.class)).toArray(Instance[]::new);

        PrintStream systemOut = System.out;

        String str = (PMSPAURModel.CHOCO_CUMULATIVE ? "choco30_" : "sota30_");

        File folder = new File("results/"+configuration.name()+"/");
        if(!folder.exists()) {
            folder.mkdirs();
        }

        for(int k = 0; k<instances.length; k++) {
            systemOut.println(configuration.toString()+" : "+instances[k].getName()+" : "+str+k);
            File file = new File("results/"+configuration.name()+"/"+str+k+".out");
            if(!file.exists()) {
                System.setOut(new PrintStream(new FileOutputStream("results/"+configuration.name()+"/"+str+k+".out"), true));
                System.out.println(exeInst(instances[k], configuration, 30));
            }
        }
        //*/

        //* args = NB_MINUTES Configuration.name pathToInst
        Configuration config = Configuration.valueOf(args[1]);
        Instance inst = buildInstance(args[2]);
        System.out.println(exeInst(inst, config, Integer.parseInt(args[0])));
        //*/
    }
}
