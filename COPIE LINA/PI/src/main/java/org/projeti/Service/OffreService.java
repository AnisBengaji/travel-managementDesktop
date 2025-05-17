package org.projeti.Service;

import org.projeti.entites.Offre;
import org.projeti.utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreService implements CRUD<Offre> {

    private Connection cnx;
    
    public OffreService() {
        try {
            cnx = Database.getInstance().getCnx();
            if (cnx == null) {
                throw new SQLException("Database connection is null");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing OffreService: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public int insert(Offre offre) throws SQLException {
        if (cnx == null) {
            cnx = Database.getInstance().getCnx();
            if (cnx == null) {
                throw new SQLException("Database connection is null");
            }
        }

        String req = "INSERT INTO offre (titre, description, prix, destination, image, rating, rating_count) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setFloat(3, offre.getPrix());
            ps.setString(4, offre.getDestination());
            ps.setString(5, offre.getImage());
            ps.setDouble(6, offre.getRating());
            ps.setInt(7, offre.getRating_count());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error inserting offre: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(Offre offre) throws SQLException {
        String req = "UPDATE offre SET titre=?, description=?, prix=?, destination=?, image=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setFloat(3, offre.getPrix());
            ps.setString(4, offre.getDestination());
            ps.setString(5, offre.getImage());
            ps.setInt(6, offre.getIdOffre());

            return ps.executeUpdate();
        }
    }

    @Override
    public int delete(Offre offre) throws SQLException {
        String req = "DELETE FROM offre WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, offre.getIdOffre());
            return ps.executeUpdate();
        }
    }

    @Override
    public List<Offre> showAll() throws SQLException {
        List<Offre> offres = new ArrayList<>();
        String query = "SELECT * FROM offre";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                offres.add(new Offre(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getFloat("prix"),
                        rs.getString("destination"),
                        rs.getString("image")
                ));
            }
        }
        return offres;
    }

    public List<Offre> getAll() throws SQLException {
        List<Offre> offres = new ArrayList<>();
        String req = "SELECT * FROM offre ORDER BY id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                offres.add(new Offre(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getFloat("prix"),
                        rs.getString("destination"),
                        rs.getString("image")
                ));
            }
        }
        return offres;
    }

    public Offre getById(int idOffre) throws SQLException {
        String req = "SELECT * FROM offre WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idOffre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Offre(
                        idOffre,
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getFloat("prix"),
                        rs.getString("destination"),
                        rs.getString("image")
                );
            }
        }
        return null;
    }
}
