package edu.pucp.plg_back.service.fileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class MantenimientoService {

    public Map<String, LocalDate> cargarMantenimiento(String filePath) throws IOException {
        Map<String, LocalDate> camionesEnMantenimiento = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    String fechaStr = partes[0];
                    String codCamion = partes[1];

                    LocalDate fecha = LocalDate.parse(fechaStr, formatter);

                    camionesEnMantenimiento.put(codCamion, fecha);                
                }
            }
        }

        return camionesEnMantenimiento;
    }
}
