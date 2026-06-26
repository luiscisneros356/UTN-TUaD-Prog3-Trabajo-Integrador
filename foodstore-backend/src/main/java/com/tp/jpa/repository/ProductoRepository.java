package com.tp.jpa.repository;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Producto. Además del CRUD heredado implementa la consulta
 * de productos activos por categoría.
 */
public class ProductoRepository extends BaseRepository<Producto> {

    public ProductoRepository() {
        super(Producto.class);
    }

    /**
     * Retorna los productos activos que pertenecen a la categoría indicada.
     */
    public List<Producto> buscarPorCategoria(Long categoriaId) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL: navega desde Categoria a través de c.productos y filtra por
            // c.id = :catId y p.eliminado = false (excluye bajas lógicas).
            String jpql = "SELECT p FROM Categoria c JOIN c.productos p "
                    + "WHERE c.id = :catId AND p.eliminado = false";
            return em.createQuery(jpql, Producto.class)
                    .setParameter("catId", categoriaId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Da de alta un producto asociándolo a una categoría. Como la relación es
     * unidireccional (la FK categoria_id la administra Categoria a través de su
     * colección), el alta se hace agregando el producto a la colección de la
     * categoría dentro de una única transacción. Retorna el producto con su ID
     * generado, o null si la categoría no existe o está dada de baja.
     */
    public Producto guardarEnCategoria(Producto producto, Long categoriaId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Categoria categoria = em.find(Categoria.class, categoriaId);
            if (categoria == null || categoria.isEliminado()) {
                tx.rollback();
                return null;
            }
            em.persist(producto);
            // Al agregarlo a la colección, Hibernate setea categoria_id al hacer commit.
            categoria.getProductos().add(producto);
            tx.commit();
            return producto;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Retorna la categoría a la que pertenece un producto (consulta inversa,
     * ya que Producto no conoce su Categoria). Optional.empty() si no tiene.
     */
    public Optional<Categoria> buscarCategoriaPorProducto(Long productoId) {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL: busca la categoría cuya colección de productos contiene el id dado.
            String jpql = "SELECT c FROM Categoria c JOIN c.productos p WHERE p.id = :pid";
            return em.createQuery(jpql, Categoria.class)
                    .setParameter("pid", productoId)
                    .getResultList()
                    .stream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
