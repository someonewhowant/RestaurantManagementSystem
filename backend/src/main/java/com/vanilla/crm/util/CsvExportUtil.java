package com.vanilla.crm.util;

import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for CSV export operations.
 * Handles UTF-8 BOM encoding required for proper Cyrillic display in Excel.
 */
public final class CsvExportUtil {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvExportUtil() {
        // Utility class — no instantiation
    }

    /**
     * Wraps CSV content string with a UTF-8 BOM marker for proper display in Excel.
     *
     * @param csvContent the raw CSV string
     * @return byte array with BOM prefix + CSV content
     */
    public static byte[] wrapWithBom(String csvContent) {
        byte[] textBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[textBytes.length + UTF8_BOM.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(textBytes, 0, result, UTF8_BOM.length, textBytes.length);
        return result;
    }

    /**
     * Escapes a field value for safe inclusion in a semicolon-delimited CSV.
     *
     * @param value the raw field value (may be null)
     * @return sanitized string with semicolons replaced by spaces
     */
    public static String escapeField(Object value) {
        if (value == null) return "";
        return value.toString().replace(";", " ");
    }

    /**
     * Builds a standardized ResponseEntity for CSV file downloads.
     *
     * @param data     the CSV byte array
     * @param filename the name of the file to be downloaded
     * @return a properly configured ResponseEntity
     */
    public static ResponseEntity<byte[]> buildCsvResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
