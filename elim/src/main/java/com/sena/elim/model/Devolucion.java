package com.sena.elim.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Integer id_devolucion;

    @Column(name = "id_pedido_fk", nullable = false)
    private Integer id_pedido;

    // mapeo por si devuelve el pedido
    @Column(name = "id_detalle_fk", nullable = true)
    private Integer id_detalle_fk;

    @Column(name = "motivo", columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Column(name = "estado_devolucion", nullable = false)
    private String estado_devolucion = "Solicitada";

    @Column(name = "fecha_solicitud")
    private LocalDate fecha_devolucion;

    @Column(name = "nombre_producto")
    private String nombre_producto;

    @Column(name = "cantidad_devuelta")
    private Integer cantidad_devuelta = 1;

    @ManyToOne
    @JoinColumn(name = "id_pedido_fk", insertable = false, updatable = false)
    private Pedido pedido;

    public Devolucion() {}

    // Getters y Setters
    public Integer getId_devolucion() { return id_devolucion; }
    public void setId_devolucion(Integer id_devolucion) { this.id_devolucion = id_devolucion; }

    public Integer getId_pedido() { return id_pedido; }
    public void setId_pedido(Integer id_pedido) { this.id_pedido = id_pedido; }

    public Integer getId_detalle_fk() { return id_detalle_fk; }
    public void setId_detalle_fk(Integer id_detalle_fk) { this.id_detalle_fk = id_detalle_fk; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado_devolucion() { return estado_devolucion; }
    public void setEstado_devolucion(String estado_devolucion) { this.estado_devolucion = estado_devolucion; }

    public LocalDate getFecha_devolucion() { return fecha_devolucion; }
    public void setFecha_devolucion(LocalDate fecha_devolucion) { this.fecha_devolucion = fecha_devolucion; }

    public String getNombre_producto() { return nombre_producto; }
    public void setNombre_producto(String nombre_producto) { this.nombre_producto = nombre_producto; }

    public Integer getCantidad_devuelta() { return cantidad_devuelta; }
    public void setCantidad_devuelta(Integer cantidad_devuelta) { this.cantidad_devuelta = cantidad_devuelta; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    //metodos de ayuda para el admin
    public String getNombres() {
        if (this.pedido != null && this.pedido.getUsuario() != null) {
            return this.pedido.getUsuario().getNombres();
        }
        return null;
    }

    public String getApellidos() {
        if (this.pedido != null && this.pedido.getUsuario() != null) {
            return this.pedido.getUsuario().getApellidos();
        }
        return null;
    }

    public String getCorreo() {
        if (this.pedido != null && this.pedido.getUsuario() != null) {
            return this.pedido.getUsuario().getCorreo();
        }
        return null;
    }
}