package org.projeti.entites;

public class Offre {
    private int idOffre;
    private String titre;
    private String description;
    private float prix;
    private String destination;
    private String image;  // Chemin de l'image
    private double rating;
    private int rating_count;

    public Offre() {}

    public Offre(int idOffre, String titre, String description, float prix, String destination, String image) {
        this.idOffre = idOffre;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.destination = destination;
        this.image = image;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRating_count() {
        return rating_count;
    }

    public void setRating_count(int rating_count) {
        this.rating_count = rating_count;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "idOffre=" + idOffre +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", destination='" + destination + '\'' +
                ", image='" + image + '\'' +
                ", rating=" + rating +
                ", rating_count=" + rating_count +
                '}';
    }
}
