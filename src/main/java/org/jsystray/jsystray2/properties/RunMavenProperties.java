package org.jsystray.jsystray2.properties;

import java.util.List;

public class RunMavenProperties {

    private String nom;
    private List<String> commande;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<String> getCommande() {
        return commande;
    }

    public void setCommande(List<String> commande) {
        this.commande = commande;
    }
}
