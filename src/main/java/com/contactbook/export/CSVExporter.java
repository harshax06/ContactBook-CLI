package com.contactbook.export;

import com.contactbook.config.DatabaseConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class CSVExporter {

    /**
     * Exports contacts to CSV using MySQL cursor-based streaming.
     * Requires useCursorFetch=true in JDBC URL.
     */
    public void exportToCSV(Path outputPath, int batchSize)
            throws SQLException, IOException {

        String sql = "SELECT * FROM contacts ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
             // MySQL streaming: set fetch size to enable cursor-based reading
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_FORWARD_ONLY,
                     ResultSet.CONCUR_READ_ONLY)) {

            ps.setFetchSize(batchSize);

            try (ResultSet rs = ps.executeQuery();
                 BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // Dynamic header from metadata
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    header.append(escapeCSV(meta.getColumnName(i)));
                    if (i < colCount) header.append(",");
                }
                writer.write(header.toString());
                writer.newLine();

                // Stream rows with batch progress reporting
                long rowCount = 0;
                while (rs.next()) {
                    StringBuilder line = new StringBuilder();
                    for (int i = 1; i <= colCount; i++) {
                        String value = rs.getString(i);
                        line.append(escapeCSV(value != null ? value : ""));
                        if (i < colCount) line.append(",");
                    }
                    writer.write(line.toString());
                    writer.newLine();
                    rowCount++;

                    if (rowCount % batchSize == 0) {
                        System.out.printf("  ... exported %d rows%n", rowCount);
                    }
                }

                System.out.printf("%nExport complete: %d rows → %s%n",
                        rowCount, outputPath.toAbsolutePath());
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") ||
                value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}