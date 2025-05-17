package org.projeti.entites;

import java.time.LocalDate;

public class Tutorial {
    private int id_tutorial;
    private int offre_id;
    private String nom_tutorial;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private float prix_tutorial;
    private String url;

    // Constructeurs
    public Tutorial() {}

    public Tutorial(int id_tutorial,int offre_id, String nom_tutorial, LocalDate dateDebut, LocalDate dateFin, float prix_tutorial, String url) {
        this.id_tutorial = id_tutorial;
        this.offre_id = offre_id;
        this.nom_tutorial = nom_tutorial;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix_tutorial = prix_tutorial;
        this.url = url;
    }

    // Getters et Setters
    public int getId_tutorial() {
        return id_tutorial;
    }

    public void setId_tutorial(int id_tutorial) {
        this.id_tutorial = id_tutorial;
    }

    public String getNom_tutorial() {
        return nom_tutorial;
    }

    public void setNom_tutorial(String nom_tutorial) {
        this.nom_tutorial = nom_tutorial;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public float getPrix_tutorial() {
        return prix_tutorial;
    }

    public void setPrix_tutorial(float prix_tutorial) {
        this.prix_tutorial = prix_tutorial;
    }

   public String getUrl() {
        return url;
   }
   public void setUrl(String url) {
        this.url = url;

   }
   public int getOffre_id() {
        return offre_id;
   }
    public void setOffre_id(int offre_id) {
                this.offre_id = offre_id;
    }
}