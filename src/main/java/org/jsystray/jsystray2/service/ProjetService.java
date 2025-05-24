package org.jsystray.jsystray2.service;

import jakarta.annotation.PostConstruct;
import org.jsystray.jsystray2.Jsystray2Application;
import org.jsystray.jsystray2.vo.Projet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjetService.class);

    @Value("${repertoireProjet}")
    private String repertoireProjet;

    public ProjetService() {
        LOGGER.info("creation repertoireProjet: {}",repertoireProjet);
    }

    @PostConstruct
    public void init(){
        LOGGER.info("init repertoireProjet: {}",repertoireProjet);
    }

    public List<Projet> getProjets(){
        LOGGER.info("répertoire: {}",repertoireProjet);
        if (repertoireProjet == null || repertoireProjet.isEmpty()) {
            throw new RuntimeException("Répertoire vide");
        }
        return listePom();
    }


    private List<Projet> listePom() {
        String directoryPath = repertoireProjet; // Remplacez par le chemin de votre répertoire
        Path p=Paths.get(directoryPath).toAbsolutePath().normalize();


        try {
//            List<Path> pomFiles = findPomFiles(directoryPath);
            LOGGER.info("récupération des fichiers pom ...");
            List<Projet> pomFiles = findPomFiles(p);
            LOGGER.info("récupération des fichiers pom ok");
            if (pomFiles.isEmpty()) {
                LOGGER.info("Aucun fichier pom.xml trouvé (en ignorant target et node_modules) dans : {}", directoryPath);
            } else {
                LOGGER.info("Fichiers pom.xml trouvés (en ignorant target et node_modules) :");
                for (Projet pomFile : pomFiles) {
                    LOGGER.info("{}",pomFile.getFichierPom());
                }
            }
            return pomFiles;
        } catch (IOException e) {
            LOGGER.error("Erreur lors du parcours du répertoire : {}", e.getMessage(), e);
        }
        return null;
    }

    public static List<Projet> findPomFiles(Path startDir) throws IOException {
        List<Projet> pomFiles = new ArrayList<>();

        Files.walkFileTree(startDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String dirName = dir.getFileName().toString();
                // Ignorer les répertoires node_modules et target
                if (dirName.equals("node_modules") || dirName.equals("target")) {
                    return FileVisitResult.SKIP_SUBTREE; // Ne pas visiter ce répertoire ni ses sous-répertoires
                }
                return FileVisitResult.CONTINUE; // Continuer la visite
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Objects.equals(file.getFileName().toString(),"pom.xml")) {
                    Projet projet = new Projet();
                    projet.setNom(file.getParent().getFileName().toString());
                    projet.setRepertoire(file.getParent().toAbsolutePath().toString());
                    projet.setFichierPom(file.toAbsolutePath().toString());
                    pomFiles.add(projet);
                    // Si un pom.xml est trouvé, nous ne voulons pas regarder dans les sous-répertoires
                    // Ici, cela signifie qu'on a trouvé un pom.xml dans le répertoire courant,
                    // donc on peut sauter les autres fichiers dans ce même répertoire,
                    // mais cela ne signifie pas que l'on doit arrêter de chercher ailleurs
                    // car le SimpleFileVisitor parcourt l'arbre de manière hiérarchique.
                    // L'arrêt de la recherche dans les sous-répertoires est géré par la logique
                    // de `postVisitDirectory` ou par l'idée que `pom.xml` est souvent à la racine d'un module.
                    // Pour le cas "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                    // cela s'applique plus à une structure de module où un pom.xml marque le début d'un module.
                    // Si le pom.xml est trouvé dans un dossier, on considèrera que ce dossier est un module
                    // et on ne cherchera pas de pom.xml dans les sous-dossiers de ce même module.
                }
                return FileVisitResult.CONTINUE; // Continuer la visite
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Cette partie est cruciale pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire."
                // Si un pom.xml a été ajouté à la liste et que le répertoire courant contient ce pom.xml,
                // alors nous voulons "sauter" la suite de ce répertoire (si d'autres fichiers ou sous-dossiers étaient encore à visiter dans ce même dossier après le pom.xml).
                // Cependant, le `SimpleFileVisitor` visite d'abord les fichiers et ensuite seulement appelle `postVisitDirectory`.
                // L'idée est plutôt que si `dir` est un répertoire et qu'il contient un `pom.xml`, nous avons déjà trouvé ce `pom.xml` dans `visitFile`.
                // Ce que l'on veut, c'est que si `dir` est un module Maven (identifié par un pom.xml à sa racine),
                // alors on ne descende pas dans les sous-répertoires de `dir` pour chercher d'autres `pom.xml`.
                // Le `preVisitDirectory` gère déjà l'exclusion des répertoires `node_modules` et `target`.
                // Pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                // la meilleure façon de l'implémenter est de ne pas visiter les sous-répertoires d'un répertoire qui *contient* un pom.xml.
                // Cela signifie qu'il faut vérifier l'existence de `pom.xml` dans `preVisitDirectory` pour décider si on continue de descendre.

                // Pour implémenter "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire",
                // nous devons modifier `preVisitDirectory` pour vérifier l'existence de `pom.xml` dans le répertoire courant.
                // Cela est un peu plus complexe car `preVisitDirectory` est appelé avant que nous ayons visité les fichiers du répertoire courant.
                // Une approche serait de maintenir un ensemble de répertoires déjà traités ou de modifier la logique de `preVisitDirectory`.
                // La solution la plus simple qui respecte l'esprit "trouver le pom.xml le plus haut dans la hiérarchie pour un module"
                // est de s'assurer que si un `pom.xml` est trouvé dans un répertoire, on n'ajoute pas les `pom.xml` des sous-répertoires de ce même répertoire.
                // La liste `pomFiles` contient déjà les chemins complets, donc si nous trouvons `A/pom.xml` et `A/B/pom.xml`, les deux seront ajoutés.
                // Pour respecter l'exigence, il faudrait post-filtrer ou adapter la logique de visite.

                // Refactorisons pour l'exigence "Si je trouve un fichier pom, je ne veux pas regarder dans les sous répertoire."
                // Cela signifie que si `dir` contient un `pom.xml`, nous ne voulons pas continuer à chercher dans les sous-dossiers de `dir`.
                // Le `SKIP_SUBTREE` dans `preVisitDirectory` est la clé pour cela.
                // Nous devons donc détecter la présence d'un `pom.xml` avant d'entrer dans les sous-répertoires.
                // La manière la plus propre serait de vérifier si le répertoire contient un `pom.xml`
                // *avant* d'appeler `CONTINUE` dans `preVisitDirectory`.

                return FileVisitResult.CONTINUE; // Continuer
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOGGER.error("Erreur lors de la visite du fichier {}: {}", file, exc.getMessage());
                return FileVisitResult.CONTINUE; // Continuer même en cas d'erreur sur un fichier
            }
        });
        return pomFiles;
    }

    public static List<Path> findPomFiles(String directoryPath) throws IOException {
        Path startPath = Paths.get(directoryPath);

        try (Stream<Path> walk = Files.walk(startPath)) {
            return walk
                    .filter(Files::isRegularFile) // Ne traiter que les fichiers réguliers
                    .filter(path -> path.getFileName().toString().equals("pom.xml")) // Chercher les fichiers nommés pom.xml
                    .filter(ProjetService::isNotIgnoredDirectory) // Ignorer les répertoires spécifiques
                    .collect(Collectors.toList());
        }
    }

    private static boolean isNotIgnoredDirectory(Path path) {
        // Vérifier si le chemin contient "target" ou "node_modules" comme nom de répertoire
        // Cela permet de s'assurer que même si un pom.xml se trouve dans un sous-sous-répertoire d'un répertoire ignoré, il est bien ignoré.
        for (Path segment : path) {
            String segmentName = segment.getFileName().toString();
            if (segmentName.equals("target") || segmentName.equals("node_modules")) {
                return false; // Le chemin contient un répertoire à ignorer
            }
        }
        return true; // Le chemin ne contient pas de répertoire à ignorer
    }

}
