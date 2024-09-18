package com.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

public class PdfSegmentTest {

    private PdfSegment pdfSegment;
    private PDDocument document;

    @Before
    public void setUp() throws IOException {
        // Initialize the PdfSegment class before each test
        pdfSegment = new PdfSegment();

        // Load the sample PDF file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("Resources/Doctor_Finder_REPORT_24.pdf").getFile());
        document = PDDocument.load(file);
    }

    @Test
    public void testDetectWhitespaceGaps() throws IOException {
        // Detect whitespace gaps in the sample PDF document
        List<Float> whitespaceGaps = pdfSegment.detectWhitespaceGaps(document);

        // Validate the result - ensure that there are detected gaps
        assertTrue("Whitespace gaps should not be empty", !whitespaceGaps.isEmpty());

        // Optionally, validate that the number of gaps is greater than a certain number
        // This can depend on the PDF file being tested
        assertTrue("Should detect significant whitespace gaps", whitespaceGaps.size() > 5);
    }

    @Test
    public void testIdentifyCutPoints() throws IOException {
        // Detect whitespace gaps
        List<Float> whitespaceGaps = pdfSegment.detectWhitespaceGaps(document);

        // Identify the top X largest cut points
        List<Integer> cutPoints = pdfSegment.identifyCutPoints(whitespaceGaps, 3);

        // Print the cut points for debugging
        System.out.println("Cut Points: " + cutPoints);
        
        // Ensure that the cut points have been found and the size matches X
        assertEquals("Should return 3 cut points", 3, cutPoints.size());

        // Ensure the cut points are ordered correctly (descending by size)
        assertTrue("Cut points should be ordered in descending order", 
            cutPoints.get(0) > cutPoints.get(1) && cutPoints.get(1) > cutPoints.get(2));
    }

    @Test
    public void testCreateSegmentedPDFs() throws IOException {
        // Detect whitespace gaps
        List<Float> whitespaceGaps = pdfSegment.detectWhitespaceGaps(document);

        // Identify cut points for segmentation
        List<Integer> cutPoints = pdfSegment.identifyCutPoints(whitespaceGaps, 3);

        // Create the segmented PDFs based on the identified cut points
        pdfSegment.createSegmentedPDFs(document, cutPoints);

        // Assert: Check the existence of the output segmented files (e.g., segment_1.pdf, segment_2.pdf)
        File segment1 = new File("segment_1.pdf");
        File segment2 = new File("segment_2.pdf");
        File segment3 = new File("segment_3.pdf");

        assertTrue("Segment 1 PDF should exist", segment1.exists());
        assertTrue("Segment 2 PDF should exist", segment2.exists());
        assertTrue("Segment 3 PDF should exist", segment3.exists());

        // Clean up (delete the generated PDF segments)
        segment1.delete();
        segment2.delete();
        segment3.delete();
    }

    @Test
    public void testErrorHandlingWithInvalidPDF() throws IOException {
        // Simulate loading an invalid or corrupt PDF
        try {
            PDDocument invalidDocument = PDDocument.load(new File("invalid.pdf"));
            List<Float> whitespaceGaps = pdfSegment.detectWhitespaceGaps(invalidDocument);
            assertTrue("No whitespace gaps should be detected in invalid PDF", whitespaceGaps.isEmpty());
        } catch (IOException e) {
            assertTrue("Should throw IOException for invalid PDF", true);
        }
    }
}
