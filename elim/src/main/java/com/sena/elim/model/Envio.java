package com.sena.elim.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_envio;

    @Column(name = "id_pedido_fk", nullable = false)
    private Integer id_pedido_fk;

    private String empresa_mensajeria;
    private String numero_guia;
    private String estado_envio;
    private LocalDate fecha_envio;

    // Relación con la entidad Pedido
    @ManyToOne
    @JoinColumn(name = "id_pedido_fk", insertable = false, updatable = false)
    private Pedido pedido;

    // metodos para extraer los datos del cliente

    public String getNombres() {
        try {
            if (pedido != null && pedido.getUsuario() != null) {
                return pedido.getUsuario().getNombres();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "Cliente";
    }

    public String getApellidos() {
        try {
            if (pedido != null && pedido.getUsuario() != null) {
                return pedido.getUsuario().getApellidos();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "";
    }

    public String getCorreo() {
        try {
            if (pedido != null && pedido.getUsuario() != null) {
                return pedido.getUsuario().getCorreo();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "Sin correo";
    }

    public Double getTotal_pago() {
        try {
            if (pedido != null && pedido.getTotalPago() != null) {
                return pedido.getTotalPago();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return 0.0;
    }

    // ==========================================
    // GETTERS Y SETTERS TRADICIONALES
    // ==========================================

    public Integer getId_envio() {
        return id_envio;
    }

    public void setId_envio(Integer id_envio) {
        this.id_envio = id_envio;
    }

    public Integer getId_pedido_fk() {
        return id_pedido_fk;
    }

    public void setId_pedido_fk(Integer id_pedido_fk) {
        this.id_pedido_fk = id_pedido_fk;
    }

    public String getEmpresa_mensajeria() {
        return empresa_mensajeria;
    }

    public void setEmpresa_mensajeria(String empresa_mensajeria) {
        this.empresa_mensajeria = empresa_mensajeria;
    }

    public String getNumero_guia() {
        return numero_guia;
    }

    public void setNumero_guia(String numero_guia) {
        this.numero_guia = numero_guia;
    }

    public String getEstado_envio() {
        return estado_envio;
    }

    public void setEstado_envio(String estado_envio) {
        this.estado_envio = estado_envio;
    }

    public LocalDate getFecha_envio() {
        return fecha_envio;
    }

    public void setFecha_envio(LocalDate fecha_envio) {
        this.fecha_envio = fecha_envio;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}