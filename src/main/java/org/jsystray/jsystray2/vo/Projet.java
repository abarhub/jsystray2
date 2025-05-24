package org.jsystray.jsystray2.vo;

public class Projet {

    private String nom;
    private String description;
    private String repertoire;
    private String fichierPom;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepertoire() {
        return repertoire;
    }

    public void setRepertoire(String repertoire) {
        this.repertoire = repertoire;
    }

    public String getFichierPom() {
        return fichierPom;
    }

    public void setFichierPom(String fichierPom) {
        this.fichierPom = fichierPom;
    }
}
