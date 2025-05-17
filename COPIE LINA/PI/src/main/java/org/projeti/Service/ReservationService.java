package org.projeti.Service;

import org.projeti.entites.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ReservationService implements CRUD2<Reservation> {
    private Connection connection;

    public ReservationService(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (status, price_total, modePaiement, user_id, evenement_id ) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reservation.getStatus().toString());
            ps.setFloat(2, reservation.getPrice_total());
            ps.setString(3, reservation.getMode_paiment().toString());
            ps.setInt(4, reservation.getUser().getId()); // Assurez-vous que getId() existe dans User
            ps.setInt(5, reservation.getEvenement().getId_Evenement()); // Assurez-vous que getId_Evenement() existe dans Evenement

            // Exécuter l'insertion
            ps.executeUpdate();

            // Récupérer l'ID généré automatiquement
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                reservation.setId_reservation(generatedKeys.getInt(1)); // Assurez-vous que Reservation a un setter pour setId_reservation(int)
            }
        }
    }


    @Override
    public void update(Reservation reservation) throws SQLException {
        String query = "UPDATE reservation SET status = ?, price_total = ?, modePaiement = ?, user_id = ?, evenement_id  = ? WHERE id_reservation = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, reservation.getStatus().toString());
            ps.setFloat(2, reservation.getPrice_total());
            ps.setString(3, reservation.getMode_paiment().toString());
            ps.setInt(4, reservation.getUser().getId());
            ps.setInt(5, reservation.getEvenement().getId_Evenement());
            ps.setInt(6, reservation.getId_reservation());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Reservation getById(int id) throws SQLException {
        String query = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        }
        return null;
    }

    @Override
    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        int idReservation = rs.getInt("id");
        Status status = Status.valueOf(rs.getString("status").toUpperCase());
        float price = rs.getFloat("price_total");

        String modePaiementString = rs.getString("modePaiement").toUpperCase();
        ModePaiement modePaiement;

        try {
            modePaiement = ModePaiement.valueOf(modePaiementString);
        } catch (IllegalArgumentException e) {
            System.out.println("Mode de paiement invalide : " + modePaiementString);
            modePaiement = ModePaiement.CARTE_BANCAIRE; // valeur par défaut
        }

        Reservation reservation = new Reservation(idReservation, status, price, modePaiement);

        int userId = rs.getInt("user_id");
        User user = new User();
        user.setId(userId);
        reservation.setUser(user);

        int evenementId = rs.getInt("evenement_id");  // <-- corrigé ici
        Evenement evenement = new Evenement();
        evenement.setId_Evenement(evenementId);
        reservation.setEvenement(evenement);

        return reservation;
    }


}