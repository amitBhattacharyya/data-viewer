package com.example.dataviewer.util;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;

public class AvroGeneratorUtil {

    public static final String SCHEMA_JSON = "{\n"
            + "  \"type\": \"record\",\n"
            + "  \"name\": \"SampleRecord\",\n"
            + "  \"namespace\": \"com.example.dataviewer\",\n"
            + "  \"fields\": [\n"
            + "    {\"name\": \"id\", \"type\": \"long\"},\n"
            + "    {\"name\": \"name\", \"type\": \"string\"},\n"
            + "    {\"name\": \"status\", \"type\": \"string\"},\n"
            + "    {\"name\": \"metadataJson\", \"type\": \"string\"},\n"
            + "    {\"name\": \"payloadJson\", \"type\": \"string\"}\n"
            + "  ]\n"
            + "}";

    public static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    /**
     * Generates an Avro file with sample records.
     * Schema has 5 columns: id (long), name (string), status (string),
     * and 2 JSON string columns: metadataJson (string) and payloadJson (string).
     *
     * @param destinationFile Output Avro file
     * @param recordCount     Number of records to generate
     * @throws IOException on write error
     */
    public static void generateSampleAvro(File destinationFile, int recordCount) throws IOException {
        if (destinationFile.getParentFile() != null && !destinationFile.getParentFile().exists()) {
            destinationFile.getParentFile().mkdirs();
        }

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(SCHEMA);
        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {
            dataFileWriter.create(SCHEMA, destinationFile);

            for (int i = 1; i <= recordCount; i++) {
                GenericRecord record = new GenericData.Record(SCHEMA);
                record.put("id", (long) i);
                record.put("name", "User_" + i);
                record.put("status", (i % 2 == 0) ? "ACTIVE" : "PENDING");

                // JSON Column 1: metadataJson (consistent schema across all records)
                String metadataJson = String.format(
                        "{\"source\":\"WEB_PORTAL\",\"version\":\"1.0.0\",\"retryCount\":%d,\"tags\":[\"avro\",\"test\",\"user_%d\"]}",
                        (i % 5), i
                );
                record.put("metadataJson", metadataJson);

                // JSON Column 2: payloadJson (consistent schema across all records)
                String payloadJson = String.format(
                        "{\"userId\":%d,\"department\":\"Engineering\",\"score\":%.2f,\"preferences\":{\"newsletter\":%b,\"theme\":\"%s\"}}",
                        i,
                        70.0 + (i % 30) + 0.5,
                        (i % 3 == 0),
                        (i % 2 == 0 ? "dark" : "light")
                );
                record.put("payloadJson", payloadJson);

                dataFileWriter.append(record);
            }
        }
    }

    public static void main(String[] args) {
        try {
            File outputFile = new File("sample_records.avro");
            generateSampleAvro(outputFile, 100);
            System.out.println("Successfully generated Avro file with 100 records: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
