package org.jsystray.jsystray2.ui;

import javafx.scene.control.TextArea;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GitStatusUI extends AbstractViewUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitStatusUI.class);

    public GitStatusUI(ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    public void run(Path file) {
        displayTextArea();
        TextArea textArea = getTextArea();

        textArea.setText(getStatus(file));


    }

    private String getStatus(Path file) {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository repository = repositoryBuilder.setGitDir(file.resolve(".git").toFile())
                    .readEnvironment() // Lire GIT_DIR et d'autres variables d'environnement
                    .findGitDir() // Chercher le répertoire .git
                    .build();

            StringBuilder sb = new StringBuilder();
            sb.append("Dépôt Git trouvé à : " + repository.getDirectory().getAbsolutePath());

            try (Git git = new Git(repository)) {

                fetch(git, sb);

                // 1. Branche actuelle
                String currentBranch = repository.getBranch();
                sb.append("\n--- Informations sur la branche ---");
                sb.append("\nBranche actuelle : " + currentBranch);

                // 2. Fichiers à committer (status)
                sb.append("\n--- Statut des fichiers ---");
                Status status = git.status().call();

                boolean hasChanges = false;

                if (!status.getAdded().isEmpty()) {
                    sb.append("\nAjoutés (staged) :");
                    status.getAdded().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getChanged().isEmpty()) {
                    sb.append("\nModifiés (staged) :");
                    status.getChanged().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getRemoved().isEmpty()) {
                    sb.append("\nSupprimés (staged) :");
                    status.getRemoved().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getMissing().isEmpty()) {
                    sb.append("\nManquants (non suivis mais indexés) :");
                    status.getMissing().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getModified().isEmpty()) {
                    sb.append("\nModifiés (non indexés) :");
                    status.getModified().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getUntracked().isEmpty()) {
                    sb.append("\nNon suivis :");
                    status.getUntracked().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }
                if (!status.getConflicting().isEmpty()) {
                    sb.append("\nEn conflit :");
                    status.getConflicting().forEach(f -> sb.append("\n  - " + f));
                    hasChanges = true;
                }

                if (!hasChanges) {
                    sb.append("\nAucun fichier à committer (le répertoire de travail est propre).");
                }

                // 3. Dernier commit
                sb.append("\n--- Dernier commit ---");
                Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
                RevCommit lastCommit = commits.iterator().next();

                if (lastCommit != null) {
                    ObjectId id = lastCommit.getId();
                    String shortHash = id.getName().substring(0, 7); // Hash court (7 premiers caractères)
                    String message = lastCommit.getFullMessage().trim();
                    PersonIdent authorIdent = lastCommit.getAuthorIdent();
                    Date commitDate = authorIdent.getWhen();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    sb.append("\nHash court    : " + shortHash);
                    sb.append("\nMessage       : " + message);
                    sb.append("\nDate du commit: " + sdf.format(commitDate));
                    sb.append("\nAuteur        : " + authorIdent.getName() + " <" + authorIdent.getEmailAddress() + ">");
                } else {
                    sb.append("\nAucun commit trouvé dans ce dépôt.");
                }


                affichageBranches(git, sb);

            }


            return sb.toString();
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'accès au dépôt Git : " + e.getMessage(), e);
        } catch (GitAPIException e) {
            LOGGER.error("Erreur JGit lors de l'obtention du statut : " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Une erreur inattendue est survenue : " + e.getMessage(), e);
        }
        return null;
    }


    private void fetch(Git git, StringBuilder sb) throws GitAPIException {
        sb.append("\nDébut du fetch...");

        // Exécuter la commande fetch
        FetchResult fetchResult = git.fetch()
                .setRemote("origin") // Le nom du dépôt distant (souvent "origin")
                .call();

        sb.append("\nFetch terminé.");

        // Afficher les résultats du fetch
        for (TrackingRefUpdate refUpdate : fetchResult.getTrackingRefUpdates()) {
            sb.append("\nMise à jour de la référence : " + refUpdate.getLocalName() +
                    " de " + refUpdate.getOldObjectId().getName() +
                    " vers " + refUpdate.getNewObjectId().getName());
        }

        // Si vous voulez aussi afficher les messages de la console de fetch
        sb.append("\nmessage fetch: " + fetchResult.getMessages());
    }

    private void affichageBranches(Git git, StringBuilder sb) throws GitAPIException {
// --------------------------------------------------
        // Afficher les branches locales
        // --------------------------------------------------
        sb.append("\n=== Branches Locales ===");
        List<Ref> localBranches = git.branchList().call();
        for (Ref branch : localBranches) {
            sb.append("\n  - " + branch.getName() + " -> " + branch.getObjectId().getName());
        }
        sb.append("\n");

        // --------------------------------------------------
        // Afficher les branches distantes (après un fetch)
        // --------------------------------------------------
        sb.append("\n=== Branches Distantes (après fetch) ===");
        List<Ref> remoteBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        for (Ref branch : remoteBranches) {
            sb.append("\n  - " + branch.getName() + " -> " + branch.getObjectId().getName());
        }
        sb.append("\n");

        // --------------------------------------------------
        // Afficher les branches distantes directement depuis le dépôt distant (ls-remote)
        // Utile si vous n'avez pas encore fait de fetch ou si vous voulez voir toutes les refs du remote
        // --------------------------------------------------
        sb.append("\n=== Branches Distantes (via ls-remote) ===");
        List<RemoteConfig> remotes = git.remoteList().call();
        for (RemoteConfig remote : remotes) {
            sb.append("\n  Dépôt distant : " + remote.getName());
            for (URIish uri : remote.getURIs()) {
                sb.append("\n    URI : " + uri);
            }

            // Pour chaque dépôt distant, lister ses refs
            // C'est l'équivalent d'un 'git ls-remote <remote-name>'
            git.lsRemote()
                    .setRemote(remote.getName())
                    .setHeads(true) // Ne montrer que les branches
                    .setTags(false) // Ne pas montrer les tags
                    .call()
                    .forEach(ref -> {
                        sb.append("\n    - " + ref.getName() + " -> " + ref.getObjectId().getName());
                    });
        }
    }
}
