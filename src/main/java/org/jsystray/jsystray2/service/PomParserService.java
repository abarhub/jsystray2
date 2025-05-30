package org.jsystray.jsystray2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class PomParserService {


    private static final Logger LOGGER = LoggerFactory.getLogger(PomParserService.class);

    public static final List<String> PROJET_VERSION = List.of("project","version");
    public static final List<String> PROJET_GROUPEID = List.of("project","groupId");
    public static final List<String> PROJET_ARTIFACTID = List.of("project","artifactId");

    private XmlParserService xmlParserService;

    public void parsePom(Path file) throws Exception {
        List<List<String>> balisesRecherche = List.of(List.of("project","version"));
        var resultat=xmlParserService.parse(file, List.of(PROJET_VERSION,PROJET_ARTIFACTID,PROJET_GROUPEID));
    }
}
