package org.projeti.entites;

import java.util.List;
import java.util.Objects;

public class Category {

    private int idCategory;
    private String nomCategory;
    private String description;
    private List<Publication> publications;  // Liste des publications associées à cette catégorie

    public Category(String nomCategory, String description, List<Publication> publications) {
        this.nomCategory = nomCategory;
        this.description = description;
        this.publications = publications;
    }

    public Category(int idCategory) {
        this.idCategory = idCategory;
    }

    public Category(int idCategory, String nomCategory) {
        this.idCategory = idCategory;
        this.nomCategory = nomCategory;
    }

    public Category() {}

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public String getNomCategory() {
        return nomCategory;
    }

    public void setNomCategory(String nomCategory) {
        this.nomCategory = nomCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    @Override
    public String toString() {
        return "Category{" +
                "idCategory=" + idCategory +
                ", nomCategory='" + nomCategory + '\'' +
                ", description='" + description + '\'' +
                ", publications=" + publications +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Category category = (Category) object;
        return getIdCategory() == category.getIdCategory() &&
                Objects.equals(getNomCategory(), category.getNomCategory()) &&
                Objects.equals(getDescription(), category.getDescription()) &&
                Objects.equals(getPublications(), category.getPublications());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdCategory(), getNomCategory(), getDescription(), getPublications());
    }
}