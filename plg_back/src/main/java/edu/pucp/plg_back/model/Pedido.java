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
    private int id;

    private String clienteId;
    private int volumen;
    private int X;
    private int Y;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaLimiteEntrega;

    @Override
    public String toString() {
        return String.format("[%d,%d]", this.getX(), this.getY());
    }
}
