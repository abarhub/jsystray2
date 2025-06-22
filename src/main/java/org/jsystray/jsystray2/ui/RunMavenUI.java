package org.jsystray.jsystray2.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.jsystray.jsystray2.service.RunService;
import org.jsystray.jsystray2.util.Line;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class RunMavenUI extends AbstractViewUI {


    private static final Logger LOGGER = LoggerFactory.getLogger(RunMavenUI.class);

    public RunMavenUI(ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    public void run(Projet projet, List<String> commandes){
        RunService runService=new RunService();
        List<String> list=new CopyOnWriteArrayList<>();

        List<String> commandes2=new ArrayList<>();

        commandes2.addAll(List.of("cmd", "/C", "mvn", "-f", projet.getFichierPom()));
        commandes2.addAll(commandes);

        String tab2[]=commandes2.toArray(new String[0]);

        displayTextArea();
        TextArea textArea=getTextArea();

        Task<Void> tache = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final FluxSink<String> dataSink;
//            Consumer<String> consumer= line->{
//                Platform.runLater(() -> {
//                    textArea.appendText(line + "\n");
//                });
//            };
                    Object[] tab = new Object[1];
                    Flux<String> stringFlux = Flux.create(sink -> {
                        tab[0] = sink;

                        try {
                            Consumer<Line> lineConsumer = line -> {
                                LOGGER.info("run : {}", line);
                                //list.add(line.line());
//                sb.append(line.line()+"\n");
                                sink.next(line.line());
//                Platform.runLater(() -> {
//                    textArea.appendText(line.line() + "\n");
//                });
                            };
                            int res = runService.runCommand(lineConsumer, tab2);

                            LOGGER.info("resultat mvn : {}", res);
                        } catch (Exception e) {
                            sink.error(new RuntimeException("erreur pour executer le programme", e));
                        }

                    });
//            dataSink=(FluxSink<String>)Objects.requireNonNull(tab[0]);
                    stringFlux
                            .buffer(Duration.of(200, ChronoUnit.MILLIS))
                            .subscribe((List<String> lines) -> {
                                        Platform.runLater(() -> {
                                            StringBuilder sb = new StringBuilder();
                                            lines.forEach(line -> {
                                                sb.append(line).append("\n");
                                            });
                                            textArea.appendText(sb.toString());
                                        });
                                    },
                                    (error) -> {
                                        LOGGER.error("Erreur", error);
                                    });

//            int res = runService.runCommand(line -> {
//                LOGGER.info("run : {}", line);
//                //list.add(line.line());
////                sb.append(line.line()+"\n");
//                dataSink.next(line.line());
////                Platform.runLater(() -> {
////                    textArea.appendText(line.line() + "\n");
////                });
//                }, "cmd","/C","mvn", "-f",selectedProduct.getFichierPom(),"dependency:tree");

                    LOGGER.info("resultat mvn : {}", 0);
                } catch (Exception e) {
                    LOGGER.error("erreur mvn", e);
                }
                return null;
            }
        };
        new Thread(tache).start();
    }


}
