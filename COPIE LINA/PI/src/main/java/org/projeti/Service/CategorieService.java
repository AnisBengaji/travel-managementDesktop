package org.projeti.Service;

import org.projeti.entites.Category;
import org.projeti.entites.Publication;
import org.projeti.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements CRUD<Category> {
    private final Connection cnx = Database.getInstance().getCnx();

    @Override
    public int insert(Category category) throws SQLException {
        String req = "INSERT INTO category (nom_category, description) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getNomCategory());
            ps.setString(2, category.getDescription());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setIdCategory(rs.getInt(1));
                    }
                }
            }
            return rowsAffected;
        }
    }

    @Override
    public int update(Category category) throws SQLException {
        String req = "UPDATE category SET nom_category = ?, description = ? WHERE idCategory = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, category.getNomCategory());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getIdCategory());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No category found with idCategory: " + category.getIdCategory());
            }
            return rowsAffected;
        }
    }

    @Override
    public int delete(Category category) throws SQLException {
        System.out.println("Attempting to delete category ID: " + category.getIdCategory());
        String req = "DELETE FROM category WHERE idCategory = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, category.getIdCategory());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No category found with idCategory: " + category.getIdCategory());
            }
            return rowsAffected;
        } catch (SQLException e) {
            throw new SQLException("Failed to delete category ID: " + category.getIdCategory() + ". Error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Category> showAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String req = "SELECT idCategory, nom_category, description FROM category";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Category category = new Category();
                category.setIdCategory(rs.getInt("idCategory"));
                category.setNomCategory(rs.getString("nom_category"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        }
        return categories;
    }

    public boolean exists(String nomCategory, int excludeId) throws SQLException {
        String req = "SELECT 1 FROM category WHERE nom_category = ? AND idCategory != ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, nomCategory);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getPublicationCount(int categoryId) throws SQLException {
        String req = "SELECT COUNT(*) FROM publication WHERE idCategory = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    public void loadPublications(Category category) throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT id_publication, title, contenu, date_publication, user_id, visibility, image, idCategory " +
                "FROM publication WHERE idCategory = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, category.getIdCategory());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Publication p = new Publication();
                    p.setId_publication(rs.getInt("id_publication"));
                    p.setTitle(rs.getString("title"));
                    p.setContenu(rs.getString("contenu"));
                    p.setDate_publication(rs.getDate("date_publication"));
                    p.setVisibility(rs.getString("visibility"));
                    p.setImage(rs.getString("image"));
                    publications.add(p);
                }
            }
        }
        category.setPublications(publications);
    }
}