package com.sena.elim.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "fecha_pedido", insertable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "documento_usuario_fk", nullable = false)
    private String documentoUsuarioFk;

    // relaicion con usuarios para traer los datos del cliente
    @ManyToOne
    @JoinColumn(name = "documento_usuario_fk", referencedColumnName = "documento", insertable = false, updatable = false)
    private Usuario usuario;

    @Column(name = "total_pago", nullable = false)
    private Double totalPago;

    @Column(name = "estado_pedido")
    private String estadoPedido = "Pendiente";

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago_fk", referencedColumnName = "id_metodo")
    private MetodoPago metodoPago;

    // Getters y Setters
    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public String getDocumentoUsuarioFk() { return documentoUsuarioFk; }
    public void setDocumentoUsuarioFk(String documentoUsuarioFk) { this.documentoUsuarioFk = documentoUsuarioFk; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Double getTotalPago() { return totalPago; }
    public void setTotalPago(Double totalPago) { this.totalPago = totalPago; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
}