package com.example.dataviewer.util;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvroGeneratorUtilTest {

    @Test
    void testGenerateSampleAvro() throws IOException {
        File sampleFile = new File("sample_records.avro");
        AvroGeneratorUtil.generateSampleAvro(sampleFile, 100);

        assertTrue(sampleFile.exists(), "Sample Avro file should exist");

        int count = 0;
        try (DataFileReader<GenericRecord> reader = new DataFileReader<>(sampleFile, new GenericDatumReader<>())) {
            while (reader.hasNext()) {
                GenericRecord record = reader.next();
                count++;
                assertTrue(record.get("id") != null);
                assertTrue(record.get("name") != null);
                assertTrue(record.get("status") != null);
                assertTrue(record.get("metadataJson").toString().startsWith("{\"source\":"));
                assertTrue(record.get("payloadJson").toString().startsWith("{\"userId\":"));
            }
        }

        assertEquals(100, count, "Should have written exactly 100 records");
    }
}
