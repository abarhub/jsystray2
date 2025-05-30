package org.jsystray.jsystray2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class PomPaserService {


    private static final Logger LOGGER = LoggerFactory.getLogger(PomPaserService.class);

    private static final List<String> PROJET_VERSION = List.of("project","version");
    private static final List<String> PROJET_GROUPEID = List.of("project","groupId");
    private static final List<String> PROJET_ARTIFACTID = List.of("project","artifactId");

    private XmlParserService xmlParserService;

    public void parsePom(Path file) throws Exception {
        List<List<String>> balisesRecherche = List.of(List.of("project","version"));
        var resultat=xmlParserService.parse(file, List.of(PROJET_VERSION,PROJET_ARTIFACTID,PROJET_GROUPEID));
    }
}
