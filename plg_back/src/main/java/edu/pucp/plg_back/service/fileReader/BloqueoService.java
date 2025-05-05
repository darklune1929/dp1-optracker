package edu.pucp.plg_back.service.fileReader;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Nodo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BloqueoService {

    private List<Bloqueo> bloqueos = new ArrayList<>();

    public void cargarBloqueos(String filePath, String periodo) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd:HH:mm");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Separar la línea en el período y los tramos bloqueados
                String[] partes = linea.split(":");
                String rango_fechas = partes[0];
                String coordenadas = partes[1];

                // Parsear el período de tiempo de cada tramo bloqueado
                String[] tiempos = rango_fechas.split("-");
                LocalDateTime inicio = parsearFecha(tiempos[0], formatter);
                LocalDateTime fin = parsearFecha(tiempos[1], formatter);

                // Parsear los tramos bloqueados (nodos)
                List<Nodo> tramos = new ArrayList<>();
                String[] puntos = coordenadas.split(",");
                for (int i = 0; i < puntos.length; i += 2) {
                    int x = Integer.parseInt(puntos[i]);
                    int y = Integer.parseInt(puntos[i + 1]);
                    Nodo nodo = new Nodo(x, y);
                    nodo.setInicioBloqueo(inicio);
                    nodo.setFinBloqueo(fin);
                    nodo.setEstaBloqueado(true); // Marcar el nodo como bloqueado
                    tramos.add(nodo);
                }

                // Crear el objeto Bloqueo y agregarlo a la lista
                bloqueos.add(new Bloqueo(tramos, periodo, "Bloqueo de ruta", inicio, fin));
            }
        }
    }

    private LocalDateTime parsearFecha(String tiempo, DateTimeFormatter formatter) {
        // Convertir el formato ##d##h##m a LocalDateTime
        String dias = tiempo.substring(0, 2);
        String horas = tiempo.substring(3, 5);
        String minutos = tiempo.substring(6, 8);

        int dia = Integer.parseInt(dias);
        int hora = Integer.parseInt(horas);
        int minuto = Integer.parseInt(minutos);

        // Asumimos que el mes y año son constantes (puedes ajustarlo según tu contexto)
        return LocalDateTime.of(2025, 1, dia, hora, minuto);
    }
}
