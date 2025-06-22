package org.jsystray.jsystray2.vo;

public class Projet {

    private String nom;
    private String description;
    private String repertoire;
    private String fichierPom;
    private String packageJson;
    private String goMod;
    private String cargoToml;

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

    public String getPackageJson() {
        return packageJson;
    }

    public void setPackageJson(String packageJson) {
        this.packageJson = packageJson;
    }

    public String getGoMod() {
        return goMod;
    }

    public void setGoMod(String goMod) {
        this.goMod = goMod;
    }

    public String getCargoToml() {
        return cargoToml;
    }

    public void setCargoToml(String cargoToml) {
        this.cargoToml = cargoToml;
    }
}
