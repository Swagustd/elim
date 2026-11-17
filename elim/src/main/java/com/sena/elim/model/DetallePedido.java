package com.sena.elim.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "id_pedido_fk", nullable = false)
    private Integer idPedidoFk;

    @Column(name = "id_producto_fk", nullable = false)
    private Integer idProductoFk;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;


    // Getters y Setters
    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }
    public Integer getIdPedidoFk() { return idPedidoFk; }
    public void setIdPedidoFk(Integer idPedidoFk) { this.idPedidoFk = idPedidoFk; }
    public Integer getIdProductoFk() { return idProductoFk; }
    public void setIdProductoFk(Integer idProductoFk) { this.idProductoFk = idProductoFk; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}