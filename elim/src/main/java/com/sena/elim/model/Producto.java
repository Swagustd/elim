package com.sena.elim.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio;
    private Integer stock;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "estado")
    private String estado;

    // relacion con la tabla de categorias
    @ManyToOne
    @JoinColumn(name = "id_categoria_fk")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto")
    private List<ProductoTalla> productoTallas;

    // getters y setters

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<ProductoTalla> getProductoTallas() {
        return productoTallas;
    }

    public void setProductoTallas(List<ProductoTalla> productoTallas) {
        this.productoTallas = productoTallas;
    }
}