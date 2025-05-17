package org.projeti.entites;

public class Reservation {
    private int id;
    private Status status;
    private float priceTotal;
    private ModePaiement modePaiement ;
    private User user;
    private Evenement evenement; // Référence à l'événement auquel cette réservation appartient

// Référence à l'utilisateur auquel la réservation appartient


    public Reservation(int id, Status status, float priceTotal, ModePaiement modePaiement) {
        this.id = id;
        this.status = status;  // Status directement
        this.priceTotal = priceTotal;
        this.modePaiement = modePaiement;
    }
    public Reservation(int idReservation, String status, float price, String modePaiment){};

    public int getId_reservation() {
        return id;
    }

    public void setId_reservation(int id_reservation) {
        this.id = id_reservation;
    }



    public Status getStatus() {
        return status;
    }

    public float getPrice_total() {
        return priceTotal;
    }

    public void setPrice_total(float price_total) {
        this.priceTotal = price_total;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public float getPrice() {
        return priceTotal;
    }

    public void setPrice(float price) {
        this.priceTotal = price;
    }

    public ModePaiement getMode_paiment() {
        return modePaiement;
    }

    public void setMode_paiment(ModePaiement mode_paiment) {
        this.modePaiement = mode_paiment;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id +
                ", status='" + status + '\'' +
                ", price=" + priceTotal +
                ", Mode_paiment='" + modePaiement + '\'' +
                '}';
    }
}

