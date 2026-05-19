package eu.urbreathdsjobs.util;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.List;

public class MeasurementUpdaterBatch {

    private static final String URL = "jdbc:postgresql://188.34.141.13:57708/urbreath_dev";
    private static final String USER = "external_user";
    private static final String PASSWORD = "external_pass";

    @Test
    public void updateMeasurementsInBatches() {
    	
        long startRecord = 0;
        long endRecord = 225300479;
        long batchSize = 1000000; // batch di 1 milione
        List<Long> excludedIdMeasures = new java.util.ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connesso al database.");

            for (long batchStart = startRecord; batchStart <= endRecord; batchStart += batchSize) {
                long batchEnd = Math.min(batchStart + batchSize - 1, endRecord);
                System.out.println("\nElaborando batch: " + batchStart + " - " + batchEnd);

                // 1. Trova id_measure con record fuori range del batch
                String findExcludedQuery = String.format("""
                    SELECT DISTINCT ma.id_measure
                    FROM measurement_attribute ma
                    WHERE ma.id_record NOT BETWEEN %d AND %d
                      AND ma.id_measure IN (
                          SELECT DISTINCT id_measure
                          FROM measurement_attribute
                          WHERE id_record BETWEEN %d AND %d
                      );
                    """, batchStart, batchEnd, batchStart, batchEnd);

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(findExcludedQuery)) {

                    System.out.println("Id_measure con record fuori batch:");
                    boolean hasExcluded = false;
                    while (rs.next()) {
                        long idMeasure = rs.getLong("id_measure");
                        excludedIdMeasures.add(idMeasure);
                        System.out.println(idMeasure);
                        hasExcluded = true;
                    }

                    if (!hasExcluded) {
                        System.out.println("Nessun id_measure escluso in questo batch.");
                    }
                }

                // 2. Aggiorna measurement per il batch
                String updateQuery = String.format("""
                    UPDATE measurement m
                    SET metadata = tmp.jsonUpdate
                    FROM (
                        SELECT 
                            ma.id_measure,
                            jsonb_object_agg(
                                at.attr_code,
                                COALESCE(
                                    to_jsonb(ma.attr_value_string),
                                    to_jsonb(ma.attr_value_number)
                                )
                            ) AS jsonUpdate
                        FROM measurement_attribute ma
                        JOIN attribute at 
                          ON ma.id_attribute = at.id_attribute
                        WHERE ma.id_record BETWEEN %d AND %d
                        GROUP BY ma.id_measure
                    ) AS tmp
                    WHERE tmp.id_measure = m.id_measure;
                    """, batchStart, batchEnd);

                try (Statement stmt = conn.createStatement()) {
                    int rowsUpdated = stmt.executeUpdate(updateQuery);
                    System.out.println("Aggiornate " + rowsUpdated + " righe nella tabella measurement.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        writeIdMeasuresToFile(excludedIdMeasures, "C:\\Users\\salespos\\Documents\\excluded_id_measures.txt");
    }
    
    public void writeIdMeasuresToFile(List<Long> idMeasures, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Long id : idMeasures) {
                writer.write(String.valueOf(id));
                writer.newLine();
            }
            System.out.println("Scritti " + idMeasures.size() + " id_measure su file: " + filename);
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura su file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
