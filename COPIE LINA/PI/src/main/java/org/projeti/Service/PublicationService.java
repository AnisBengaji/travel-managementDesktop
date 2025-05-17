package org.projeti.Service;

import org.projeti.entites.Publication;
import org.projeti.entites.Category;
import org.projeti.entites.User;
import org.projeti.entites.Comment;
import org.projeti.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationService {

    private Connection cnx = Database.getInstance().getCnx();
    private Statement st;
    private PreparedStatement ps;

    public int insert(Publication publication) throws SQLException {
        User currentUser = publication.getAuthor();
        if (currentUser == null) {
            throw new SQLException("No logged-in user found. Publication cannot be inserted without an author.");
        }

        String req = "INSERT INTO publication (title, contenu, date_publication, author_id, visibility, image, idCategory) VALUES (?, ?, ?, ?, ?, ?, ?)";
        ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, publication.getTitle());
        ps.setString(2, publication.getContenu());

        // Use current date from the Publication object
        // The controller now sets this automatically to the current date
        ps.setDate(3, publication.getDate_publication());

        ps.setInt(4, currentUser.getId());
        ps.setString(5, publication.getVisibility());
        ps.setString(6, publication.getImage());
        ps.setInt(7, publication.getCategory() != null ? publication.getCategory().getIdCategory() : 0);

        int affectedRows = ps.executeUpdate();
        if (affectedRows > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                publication.setId_publication(rs.getInt(1));
            }
        }
        return affectedRows;
    }
    public Publication findById(int id) throws SQLException {
        String req = "SELECT p.id_publication, p.title, p.contenu, p.date_publication, p.user_id, p.visibility, p.image, p.id_category, c.nom_category " +
                "FROM publication p LEFT JOIN category c ON p.id_category = c.id_category WHERE p.id_publication = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Publication p = new Publication();
            p.setId_publication(rs.getInt("id_publication"));
            p.setTitle(rs.getString("title"));
            p.setContenu(rs.getString("contenu"));
            p.setDate_publication(rs.getDate("date_publication"));

            // Set author
            int userId = rs.getInt("user_id");


            p.setVisibility(rs.getString("visibility"));
            p.setImage(rs.getString("image"));

            // Set category
            int categoryId = rs.getInt("id_category");
            if (!rs.wasNull()) {
                Category category = new Category();
                category.setIdCategory(categoryId);
                category.setNomCategory(rs.getString("nom_category"));
                p.setCategory(category);
            }

            return p;
        }

        return null;
    }

    public int update(Publication publication) throws SQLException {
        String req = "UPDATE publication SET title = ?, contenu = ?, date_publication = ?, author_id = ?, visibility = ?, image = ?, idCategory = ? WHERE id_publication = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, publication.getTitle());
        ps.setString(2, publication.getContenu());
        ps.setDate(3, publication.getDate_publication());
        ps.setInt(4, publication.getAuthor() != null ? publication.getAuthor().getId() : 0);
        ps.setString(5, publication.getVisibility());
        ps.setString(6, publication.getImage());
        ps.setInt(7, publication.getCategory() != null ? publication.getCategory().getIdCategory() : 0);
        ps.setInt(8, publication.getId_publication());
        return ps.executeUpdate();
    }

    public int delete(Publication publication) throws SQLException {
        String req = "DELETE FROM publication WHERE id_publication = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, publication.getId_publication());
        return ps.executeUpdate();
    }

    public int deletePublication(int publicationId) throws SQLException {
        String req = "DELETE FROM publication WHERE id_publication = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, publicationId);
        return ps.executeUpdate();
    }

    public List<Publication> showAll() throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT p.*, u.id AS user_id, u.nom, u.prenom, u.email, c.idCategory, c.nom_category " +
                "FROM publication p " +
                "LEFT JOIN user u ON p.author_id = u.id " +
                "LEFT JOIN category c ON p.idCategory = c.idCategory";
        st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Publication p = new Publication();
            p.setId_publication(rs.getInt("id_publication"));
            p.setTitle(rs.getString("title"));
            p.setContenu(rs.getString("contenu"));
            p.setDate_publication(rs.getDate("date_publication"));
            User author = new User();
            author.setId(rs.getInt("user_id"));
            author.setNom(rs.getString("nom"));
            author.setPrenom(rs.getString("prenom"));
            author.setEmail(rs.getString("email"));
            p.setAuthor(author);
            p.setVisibility(rs.getString("visibility"));
            p.setImage(rs.getString("image"));
            Category category = new Category();
            category.setIdCategory(rs.getInt("idCategory"));
            category.setNomCategory(rs.getString("nom_category"));
            p.setCategory(category);
            p.setComments(loadComments(p.getId_publication()));
            publications.add(p);
        }
        return publications;
    }

    private List<Comment> loadComments(int publicationId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String req = "SELECT id_comment, content, author, date_comment, id_publication FROM comment WHERE id_publication = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, publicationId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Comment comment = new Comment();
            comment.setId_comment(rs.getInt("id_comment"));
            comment.setContent(rs.getString("content"));
            comment.setAuthor(rs.getString("author"));
            comment.setDate_comment(rs.getDate("date_comment"));
            comment.setPublication_id(rs.getInt("id_publication"));
            comments.add(comment);
        }
        return comments;
    }

    public List<Publication> getPublicationsByCategory(String categoryName) throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT p.*, u.id AS user_id, u.nom, u.prenom, u.email, c.idCategory, c.nom_category " +
                "FROM publication p " +
                "LEFT JOIN user u ON p.author_id = u.id " +
                "LEFT JOIN category c ON p.idCategory = c.idCategory " +
                "WHERE c.nom_category = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, categoryName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Publication p = new Publication();
            p.setId_publication(rs.getInt("id_publication"));
            p.setTitle(rs.getString("title"));
            p.setContenu(rs.getString("contenu"));
            p.setDate_publication(rs.getDate("date_publication"));
            User author = new User();
            author.setId(rs.getInt("user_id"));
            author.setNom(rs.getString("nom"));
            author.setPrenom(rs.getString("prenom"));
            author.setEmail(rs.getString("email"));
            p.setAuthor(author);
            p.setVisibility(rs.getString("visibility"));
            p.setImage(rs.getString("image"));
            Category category = new Category();
            category.setIdCategory(rs.getInt("idCategory"));
            category.setNomCategory(rs.getString("nom_category"));
            p.setCategory(category);
            p.setComments(loadComments(p.getId_publication()));
            publications.add(p);
        }
        return publications;
    }

    public List<Publication> getPublicationsByAuthor(String authorEmail) throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT p.*, u.id AS user_id, u.nom, u.prenom, u.email, c.idCategory, c.nom_category " +
                "FROM publication p " +
                "LEFT JOIN user u ON p.author_id = u.id " +
                "LEFT JOIN category c ON p.idCategory = c.idCategory " +
                "WHERE u.email = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, authorEmail);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Publication p = new Publication();
            p.setId_publication(rs.getInt("id_publication"));
            p.setTitle(rs.getString("title"));
            p.setContenu(rs.getString("contenu"));
            p.setDate_publication(rs.getDate("date_publication"));
            User author = new User();
            author.setId(rs.getInt("user_id"));
            author.setNom(rs.getString("nom"));
            author.setPrenom(rs.getString("prenom"));
            author.setEmail(rs.getString("email"));
            p.setAuthor(author);
            p.setVisibility(rs.getString("visibility"));
            p.setImage(rs.getString("image"));
            Category category = new Category();
            category.setIdCategory(rs.getInt("idCategory"));
            category.setNomCategory(rs.getString("nom_category"));
            p.setCategory(category);
            p.setComments(loadComments(p.getId_publication()));
            publications.add(p);
        }
        return publications;
    }

    public List<Publication> getPublicationsByVisibility(String visibility) throws SQLException {
        List<Publication> publications = new ArrayList<>();
        String req = "SELECT p.*, u.id AS user_id, u.nom, u.prenom, u.email, c.idCategory, c.nom_category " +
                "FROM publication p " +
                "LEFT JOIN user u ON p.author_id = u.id " +
                "LEFT JOIN category c ON p.idCategory = c.idCategory " +
                "WHERE p.visibility = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, visibility);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Publication p = new Publication();
            p.setId_publication(rs.getInt("id_publication"));
            p.setTitle(rs.getString("title"));
            p.setContenu(rs.getString("contenu"));
            p.setDate_publication(rs.getDate("date_publication"));
            User author = new User();
            author.setId(rs.getInt("user_id"));
            author.setNom(rs.getString("nom"));
            author.setPrenom(rs.getString("prenom"));
            author.setEmail(rs.getString("email"));
            p.setAuthor(author);
            p.setVisibility(rs.getString("visibility"));
            p.setImage(rs.getString("image"));
            Category category = new Category();
            category.setIdCategory(rs.getInt("idCategory"));
            category.setNomCategory(rs.getString("nom_category"));
            p.setCategory(category);
            p.setComments(loadComments(p.getId_publication()));
            publications.add(p);
        }
        return publications;
    }
}