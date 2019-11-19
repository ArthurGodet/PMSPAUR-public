/*
@author Arthur Godet <arth.godet@gmail.com>
@since 07/03/2019
*/
package data;

import bench.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.input.Instance;
import data.input.Job;
import data.input.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Factory {

    private static Random RAND = new Random(0);

    public static <T> T fromFile(String path, Class<T> valueType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(path), valueType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void toFile(String path, Object toWrite) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File f = new File(path);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            String s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toWrite);
            FileWriter fw = new FileWriter(path);
            fw.write(s);
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static void createDZN(Instance instance, String path) throws IOException {
        int horizon = instance.getJobs().stream().mapToInt(Job::getProcTime).sum();

        LinkedList<Resource> resourcesList = instance.getListResources();
        int nJobsPerResource = resourcesList.stream().mapToInt(r -> r.getJobs().size()).max().getAsInt();

        int maxProcTimeSumResource = resourcesList.stream().mapToInt(Resource::getProcSum).max().getAsInt();
        int LB = Math.max(horizon / instance.getNbMachines(), maxProcTimeSumResource);

        FileWriter fw = new FileWriter(path);
        fw.write("n_machines = " + instance.getNbMachines() + ";\n");
        fw.write("n_resources = " + resourcesList.size() + ";\n");
        fw.write("n_jobs_per_resource = " + nJobsPerResource + ";\n");
        fw.write("n_jobs = " + (resourcesList.size() * nJobsPerResource) + ";\n");
        fw.write("horizon = " + horizon + ";\n");
        fw.write("LB = " + LB + ";\n");

        StringBuilder sb = new StringBuilder();
        sb.append("duration = [");
        for (int j = 0; j < resourcesList.size(); j++) {
            Resource r = resourcesList.get(j);
            for (int i = 0; i < nJobsPerResource; i++) {
                if (i < r.getJobs().size()) {
                    sb.append(r.getJobs().get(i).getProcTime());
                } else {
                    sb.append(0);
                }
                if (j + 1 != resourcesList.size() || i + 1 != nJobsPerResource) {
                    sb.append(", ");
                }
            }
        }
        sb.append("];\n");
        fw.write(sb.toString());
        fw.close();
    }

    private static void addFiles(List<File> list, File file, String fileType, boolean includeSubFolders) {
        if (file.isDirectory() && includeSubFolders) {
            for (File f : file.listFiles()) {
                addFiles(list, f, fileType, true);
            }
        } else if (file.getName().contains(fileType)) {
            list.add(file);
        }
    }

    public static File[] listAllFiles(String folderPath, String fileType, boolean includeSubFolders) {
        List<File> list = new LinkedList<>();
        File folder = new File(folderPath);
        for (File f : folder.listFiles()) {
            addFiles(list, f, fileType, includeSubFolders);
        }
        return list.toArray(new File[list.size()]);
    }

    public static void createAllDZN() throws IOException {
        File[] files = listAllFiles("data/", ".json", true);
        for (File f : files) {
            Instance instance = fromFile(f.getPath(), Instance.class);
            String str = f.getPath().substring(0, f.getPath().length() - 5);
            createDZN(instance, str + ".dzn");
        }
    }

    // nbJobs indicates the number of jobs per resource
    private static Instance generateInstance(String name, int nbMachines, int[] nbJobs, int maxJobProcTime) {
        int id = 0;
        LinkedList<Job> jobs = new LinkedList<>();
        for (int idResource = 0; idResource < nbJobs.length; idResource++) {
            for (int job = 0; job < nbJobs[idResource]; job++) {
                jobs.add(new Job(id++, noZeroRandomInteger(maxJobProcTime), idResource));
            }
        }
        return new Instance(name, jobs, nbMachines);
    }

    private static int[] generateNbJobs(String str, int nbResources, int maxNbJob) {
        int[] nbJobs = new int[nbResources];
        if (str.equals("UNIFORM")) {
            Arrays.fill(nbJobs, maxNbJob);
        } else {
            nbJobs = Arrays.stream(nbJobs).map(i -> noZeroRandomInteger(maxNbJob)).toArray();
        }
        return nbJobs;
    }

    private static int noZeroRandomInteger(int max) {
        int n;
        do {
            n = RAND.nextInt(max);
        } while (n == 0);
        return n;
    }

    private static void generateBenchmark() {
        int[] maxJobProcTimeArray = new int[]{10, 100, 1000};
        int[] nbMachinesArray = new int[]{2, 3, 5, 10};
        String[] typeGen = new String[]{"UNIFORM", "RANDOM", "RANDOM", "RANDOM", "RANDOM"};
        double[] nbResCompMachines = new double[]{1.25, 1.5, 1.75, 2.0};
        int[] maxNbJobArray = new int[]{5, 10, 20};

        for (int maxJobProcTime : maxJobProcTimeArray) {
            for (int nbMachines : nbMachinesArray) {
                for (double d : nbResCompMachines) {
                    int nbResources = (int) (d * nbMachines);
                    if (nbResources > nbMachines) { // if nbResources<=nbMachines, instance is trivial
                        for (int maxNbJob : maxNbJobArray) {
                            for (String type : typeGen) {
                                int[] nbJobs;
                                do {
                                    nbJobs = generateNbJobs(type, nbResources, maxNbJob);
                                } while (Arrays.stream(nbJobs).sum() <= 5); // we want at least 5 jobs in the instance
                                Instance instance = generateInstance(
                                    nbMachines + "_" + nbResources + "_" +
                                        type + "_" + maxNbJob + "_" + maxJobProcTime,
                                    nbMachines, nbJobs, maxJobProcTime);
                                toFile("data/" + nbMachines + "_" + nbResources + "/" + instance.getName() + ".json", instance);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void createCommandLines(int nbMinutes, Configuration... configs) throws IOException {
        FileWriter fw = new FileWriter("commandsLine.txt");
        for(File file : listAllFiles("data/", ".json", true)) {
            for(Configuration c : configs) {
                fw.write("java -jar PMSPAUR.jar "+nbMinutes+" "+c.name()+" "+file.getPath().replace("\\", "/")+"\n");
            }
        }
        fw.close();
    }

    public static void main(String[] args) throws IOException {
//        generateBenchmark();
//        createAllDZN();
        createCommandLines(30, Configuration.Smallest, Configuration.SetTimes, Configuration.Order, Configuration.OrderA, Configuration.OrderAM);
    }
}
