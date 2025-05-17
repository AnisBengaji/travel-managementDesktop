package org.projeti.Service;

import org.projeti.entites.Evenement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements CRUD2<Evenement> {
    private final Connection conn;

    public EvenementService(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void add(Evenement evenement) {
        String query = "INSERT INTO evenement (nom, date_evenement_depart, date_evenement_arriver, lieu, description, price, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, evenement.getNom());
            stmt.setDate(2, Date.valueOf(evenement.getDate_EvenementDepart()));
            stmt.setDate(3, Date.valueOf(evenement.getDate_EvenementArriver()));
            stmt.setString(4, evenement.getLieu());
            stmt.setString(5, evenement.getDescription());
            stmt.setFloat(6, evenement.getPrice());
            stmt.setDouble(7, evenement.getLatitude());  // Ajout de la latitude
            stmt.setDouble(8, evenement.getLongitude()); // Ajout de la longitude

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evenement.setId_Evenement(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void update(Evenement evenement) {
        String query = "UPDATE evenement SET nom = ?, date_evenement_depart = ?, date_evenement_arriver = ?, "
                + "lieu = ?, description = ?, price = ?, latitude = ?, longitude = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, evenement.getNom());
            stmt.setDate(2, Date.valueOf(evenement.getDate_EvenementDepart()));
            stmt.setDate(3, Date.valueOf(evenement.getDate_EvenementArriver()));
            stmt.setString(4, evenement.getLieu());
            stmt.setString(5, evenement.getDescription());
            stmt.setFloat(6, evenement.getPrice());
            stmt.setDouble(7, evenement.getLatitude()); // Mise à jour de la latitude
            stmt.setDouble(8, evenement.getLongitude()); // Mise à jour de la longitude
            stmt.setInt(9, evenement.getId_Evenement());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Aucun événement trouvé avec l'ID : " + evenement.getId_Evenement());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String deleteReservations = "DELETE FROM reservation WHERE evenement_id = ?";
        String deleteEvenement = "DELETE FROM evenement WHERE id = ?";

        try (
                PreparedStatement stmtReservations = conn.prepareStatement(deleteReservations);
                PreparedStatement stmtEvenement = conn.prepareStatement(deleteEvenement)
        ) {
            // Supprimer les réservations liées
            stmtReservations.setInt(1, id);
            stmtReservations.executeUpdate();

            // Supprimer l'événement
            stmtEvenement.setInt(1, id);
            int rowsDeleted = stmtEvenement.executeUpdate();

            if (rowsDeleted == 0) {
                throw new RuntimeException("Aucun événement trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de suppression : " + e.getMessage());
        }
    }


    @Override
    public Evenement getById(int id) {
        String query = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Evenement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getDate("date_evenement_depart").toLocalDate(),
                            rs.getDate("date_evenement_arriver").toLocalDate(),
                            rs.getString("lieu"),
                            rs.getString("description"),
                            rs.getFloat("price"),
                            rs.getDouble("latitude"),   // Récupération de la latitude
                            rs.getDouble("longitude")   // Récupération de la longitude
                    );
                }
                throw new RuntimeException("Aucun événement trouvé avec l'ID : " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de récupération : " + e.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT * FROM evenement";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                evenements.add(new Evenement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDate("date_evenement_depart").toLocalDate(),
                        rs.getDate("date_evenement_arriver").toLocalDate(),
                        rs.getString("lieu"),
                        rs.getString("description"),
                        rs.getFloat("price"),
                        rs.getDouble("latitude"),  // Récupération de la latitude
                        rs.getDouble("longitude")  // Récupération de la longitude
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de récupération : " + e.getMessage());
        }
        return evenements;
    }
}
