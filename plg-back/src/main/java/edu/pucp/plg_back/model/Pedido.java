package edu.pucp.plg_back.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String clienteId;
    private Integer volumen;
    private Integer posX;
    private Integer posY;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaLimiteEntrega;

    public Pedido(Integer id,
            String clienteId,
            Integer volumen,
            Integer posX,
            Integer posY,
            LocalDateTime fechaPedido,
            Integer horasLimite) {
        this.id = id;
        this.clienteId = clienteId;
        this.volumen = volumen;
        this.posX = posX;
        this.posY = posY;
        this.fechaPedido = fechaPedido;
        this.fechaLimiteEntrega = fechaPedido.plusHours(horasLimite);
    }
}
