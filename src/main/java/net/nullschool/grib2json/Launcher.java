package net.nullschool.grib2json;

import ch.qos.logback.classic.LoggerContext;
import com.lexicalscope.jewel.JewelRuntimeException;
import com.lexicalscope.jewel.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;


/**
 * 2013-10-24<p/>
 *
 * Execution shim for the grib2json utility. Parses command line options and invokes the {@link Grib2Json} converter.
 *
 * @author Cameron Beccario
 */
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    private static void printUsage() {
//        System.out.println(CliFactory.createCli(Options.class).getHelpMessage());
    }

    private static <T> T[] merge(T[] a, T[] b) {
        T[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static String[] splitArgs(String line) {
        List<String> args = new ArrayList<>();
        for (String arg : line.split("\\s+")) {
            if (!arg.isEmpty()) {
                args.add(arg);
            }
        }
        return args.toArray(new String[args.size()]);
    }

    private static List<Options> readRecipeFile(String[] mainArgs, File recipe) throws IOException {
        List<Options> groups = new ArrayList<>();
        for (String line : Files.readAllLines(recipe.toPath(), Charset.forName("UTF-8"))) {
            String[] args = merge(splitArgs(line), mainArgs);
            log.info(Arrays.toString(args));
            groups.add(CliFactory.parseArguments(Options.class, args));
        }
        return groups;
    }

    /**
     * 
     * @param inputFile
     * @param outputFile
     * @param dataField
     */
   	public void transform(String inputFile, String outputFile, String dataField) {
    	String [] args = new String[13];
    	args[0] = "--compact";
    	args[1] = "-c";
    	args[2] = "--names";
    	args[3] = "--data";
    	args[4] = "--fp";
    	args[5] = dataField;
    	args[6] = "--fs";
    	args[7] = "103";
    	args[8] = "--fv";
    	args[9] = "10.0";
    	args[10] = "--output";
//    	args[11] = "C:\\wind-data.json";
    	args[11] = outputFile;
//    	args[12] = "C:\\2022040606gfs.t06z.sfluxgrbf006.grib2";
    	args[12] = inputFile;
    	
        try {
            Options options = CliFactory.parseArguments(Options.class, args);
            if (options.getShowHelp() || options.getFile() == null) {
                printUsage();
                System.exit(options.getShowHelp() ? 0 : 1);
                return;
            }

            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            if (!options.getEnableLogging()) {
                lc.stop();
            }

            List<Options> optionGroups = options.getRecipe() != null ?
                readRecipeFile(args, options.getRecipe()) :
                Collections.singletonList(options);

            new Grib2Json(options.getFile(), optionGroups).write();
        }
        catch (JewelRuntimeException t) {
            printUsage();
            System.out.println();
            System.err.println(t.getMessage());
            System.exit(1);
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(2);
        }
    }
}
