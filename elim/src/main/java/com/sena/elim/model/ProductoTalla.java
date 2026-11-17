package com.sena.elim.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_tallas")
public class ProductoTalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProductoTalla;

    private String talla;
    private Integer stock;

    // mapeo con la clase de producto
    @ManyToOne
    @JoinColumn(name = "id_producto_fk")
    private Producto producto;

    //getters y setters


    public Integer getIdProductoTalla() {
        return idProductoTalla;
    }

    public void setIdProductoTalla(Integer idProductoTalla) {
        this.idProductoTalla = idProductoTalla;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}