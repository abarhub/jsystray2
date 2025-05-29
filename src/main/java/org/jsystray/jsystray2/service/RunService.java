package org.jsystray.jsystray2.service;

import org.jsystray.jsystray2.util.Line;
import org.jsystray.jsystray2.util.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RunService {


    private static final Logger LOGGER = LoggerFactory.getLogger(RunService.class);

    private int runCommand(Consumer<Line> consumer, String... commandes) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder();
        List<String> liste = new ArrayList<>();
        for (String s : commandes) {
            if (s.contains(" ")) {
                liste.add("\"" + s + "\"");
            } else {
                liste.add(s);
            }
        }
        LOGGER.info("run {}", liste);
        builder.command(liste);
        Process process = builder.start();
        try (ExecutorService executorService = Executors.newCachedThreadPool()) {
            //LOGGER.info("output: {}",x);
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), x -> consumer.accept(new Line(false, x)));
            executorService.submit(streamGobbler);
            StreamGobbler streamGobblerErrur =
                    new StreamGobbler(process.getErrorStream(), (x) -> {
                        LOGGER.error("error: {}", x);
                        consumer.accept(new Line(true, x));
                    });
            executorService.submit(streamGobblerErrur);
            return process.waitFor();
        }
    }
}
