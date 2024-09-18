package com.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PdfSegment extends PDFTextStripper {
	private static final int X = 3;
	private List<TextPosition> textPositions; // Store text positions

	public PdfSegment() throws IOException {
		super();
		textPositions = new ArrayList<>();
	}

	public static void main(String[] args) {
		try {
			// Load the PDF file using the ClassLoader
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			File file = new File(classLoader.getResource("Resources/Doctor_Finder_REPORT_24.pdf").getFile());
			PDDocument document = PDDocument.load(file);

			// Create an instance of the segmenter
			PdfSegment segmenter = new PdfSegment();

			// Extract whitespace gaps between text blocks
			List<Float> whitespaceGaps = segmenter.detectWhitespaceGaps(document);

			// Identify the top X largest gaps for segmentation
			List<Integer> cutPoints = segmenter.identifyCutPoints(whitespaceGaps, X);

			// Create new PDFs based on the cut points
			segmenter.createSegmentedPDFs(document, cutPoints);

			// Close the original PDF document
			document.close();

			System.out.println("PDF segmented successfully.");
		} catch (IOException e) {
			System.err.println("Error processing PDF: " + e.getMessage());
		}
	}

	// Override writeString to collect TextPosition objects
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		this.textPositions.addAll(textPositions); // Collect text positions
		super.writeString(string, textPositions); // Keep default behavior
	}

	List<Float> detectWhitespaceGaps(PDDocument document) throws IOException {
		textPositions.clear(); // Clear any previous text positions

		// Set the text stripper to sort text by position on the page
		this.setSortByPosition(true);
		this.setStartPage(0);
		this.setEndPage(document.getNumberOfPages());

		// Extract text positions from the PDF document
		this.getText(document); // Use PDFTextStripper to extract text

		// Analyze the Y-axis spacing between consecutive text blocks
		List<Float> whitespaceGaps = new ArrayList<>();
		for (int i = 1; i < textPositions.size(); i++) {
			float currentY = textPositions.get(i).getY();
			float previousY = textPositions.get(i - 1).getY();

			// Calculate vertical whitespace gap
			float whitespaceGap = currentY - previousY;
			if (whitespaceGap > 0) { // Only consider positive gaps (downward vertical movement)
				whitespaceGaps.add(whitespaceGap);
			}
		}

		return whitespaceGaps;
	}

	List<Integer> identifyCutPoints(List<Float> whitespaceGaps, int cuts) {
		// Ensure there are whitespace gaps to process
		if (whitespaceGaps.isEmpty()) {
			return Collections.emptyList();
		}

		// Map gaps to their corresponding Y positions
		List<Integer> yPositions = new ArrayList<>();
		for (int i = 1; i < textPositions.size(); i++) {
			float currentY = textPositions.get(i).getY();
			float previousY = textPositions.get(i - 1).getY();
			float gap = currentY - previousY;
			if (gap > 0) {
				yPositions.add(Math.round(currentY)); 
			}
		}

		// Sort Y positions by the size of the corresponding gap
		List<Float> uniqueGaps = new ArrayList<>(new HashSet<>(whitespaceGaps)); 
		Collections.sort(uniqueGaps, Collections.reverseOrder());

		// Select the largest gaps as cut points (limit to 'cuts')
		List<Integer> cutPoints = new ArrayList<>();
		for (int i = 0; i < cuts && i < uniqueGaps.size(); i++) {
			float largestGap = uniqueGaps.get(i);
			// Find the Y position corresponding to the largest gap
			for (int j = 1; j < textPositions.size(); j++) {
				float currentY = textPositions.get(j).getY();
				float previousY = textPositions.get(j - 1).getY();
				if ((currentY - previousY) == largestGap) {
					cutPoints.add(Math.round(currentY));
					// Move to next largest gap after finding a match
					break; 
				}
			}
		}

		// Sort cut points in descending order
		Collections.sort(cutPoints, Collections.reverseOrder());

		return cutPoints;
	}

	void createSegmentedPDFs(PDDocument document, List<Integer> cutPoints) throws IOException {
		int segmentCount = 1;

		for (int i = 0; i <= cutPoints.size() - 1; i++) {
			// Create a new PDF for each segment
			PDDocument segment = new PDDocument();
			segment.addPage(document.getPage(i));
			segment.save("segment_" + segmentCount + ".pdf");
			segment.close();
			segmentCount++;
		}
	}
}
