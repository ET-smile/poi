/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that POIDocument correctly loads and saves the common
 *  (hspf) Document Properties.
 *
 * This is part 1 of 2 of the tests - it only does the POIDocuments
 *  which are part of the Main (not scratchpad)
 */
public final class TestPOIDocumentMain {
    // The POI Documents to work on
    private POIDocument doc;
    private POIDocument doc2;

    /**
     * Set things up, two spreadsheets for our testing
     */
    @Before
    public void setUp() {
        doc = HSSFTestDataSamples.openSampleWorkbook("DateFormats.xls");
        doc2 = HSSFTestDataSamples.openSampleWorkbook("StringFormulas.xls");
    }

    @Test
    public void readProperties() {
        // We should have both sets
        assertNotNull(doc.getDocumentSummaryInformation());
        assertNotNull(doc.getSummaryInformation());

        // Check they are as expected for the test doc
        assertEquals("Administrator", doc.getSummaryInformation().getAuthor());
        assertEquals(0, doc.getDocumentSummaryInformation().getByteCount());
    }

    @Test
    public void readProperties2() {
        // Check again on the word one
        assertNotNull(doc2.getDocumentSummaryInformation());
        assertNotNull(doc2.getSummaryInformation());

        assertEquals("Avik Sengupta", doc2.getSummaryInformation().getAuthor());
        assertEquals(null, doc2.getSummaryInformation().getKeywords());
        assertEquals(0, doc2.getDocumentSummaryInformation().getByteCount());
    }

    @Test
    public void writeProperties() throws Exception {
        // Just check we can write them back out into a filesystem
        NPOIFSFileSystem outFS = new NPOIFSFileSystem();
        doc.readProperties();
        doc.writeProperties(outFS);

        // Should now hold them
        assertNotNull(
                outFS.createDocumentInputStream("\005SummaryInformation")
        );
        assertNotNull(
                outFS.createDocumentInputStream("\005DocumentSummaryInformation")
        );
    }

    @Test
    public void WriteReadProperties() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Write them out
        NPOIFSFileSystem outFS = new NPOIFSFileSystem();
        doc.readProperties();
        doc.writeProperties(outFS);
        outFS.writeFilesystem(baos);

        // Create a new version
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        OPOIFSFileSystem inFS = new OPOIFSFileSystem(bais);

        // Check they're still there
        doc.directory = inFS.getRoot();
        doc.readProperties();

        // Delegate test
        readProperties();
    }

    @Test
    public void createNewProperties() throws IOException {
        POIDocument doc = new HSSFWorkbook();

        // New document won't have them
        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Add them in
        doc.createInformationProperties();
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());

        // Write out and back in again, no change
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        ((HSSFWorkbook)doc).close();

        doc = new HSSFWorkbook(bais);

        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());
        
        ((HSSFWorkbook)doc).close();
    }

    @Test
    public void createNewPropertiesOnExistingFile() throws IOException {
        POIDocument doc = new HSSFWorkbook();

        // New document won't have them
        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Write out and back in again, no change
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);
        
        ((HSSFWorkbook)doc).close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        doc = new HSSFWorkbook(bais);

        assertNull(doc.getSummaryInformation());
        assertNull(doc.getDocumentSummaryInformation());

        // Create, and change
        doc.createInformationProperties();
        doc.getSummaryInformation().setAuthor("POI Testing");
        doc.getDocumentSummaryInformation().setCompany("ASF");

        // Save and re-load
        baos = new ByteArrayOutputStream();
        doc.write(baos);
        
        ((HSSFWorkbook)doc).close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        doc = new HSSFWorkbook(bais);

        // Check
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());
        assertEquals("POI Testing", doc.getSummaryInformation().getAuthor());
        assertEquals("ASF", doc.getDocumentSummaryInformation().getCompany());

        // Asking to re-create will make no difference now
        doc.createInformationProperties();
        assertNotNull(doc.getSummaryInformation());
        assertNotNull(doc.getDocumentSummaryInformation());
        assertEquals("POI Testing", doc.getSummaryInformation().getAuthor());
        assertEquals("ASF", doc.getDocumentSummaryInformation().getCompany());

        ((HSSFWorkbook)doc).close();
    }
}
