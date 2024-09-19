# PDF Content Segmenter

## Overview

The PDF Content Segmenter is a Java application that segments PDF documents into separate sections based on whitespace between text blocks. This application detects whitespace gaps, identifies significant cut points, and creates new PDF documents based on these points.

## Project Structure

- `PdfSegment.java`: The core class for PDF segmentation.
 It includes methods for detecting whitespace gaps, identifying cut points, and creating segmented PDFs.
- `PdfSegmentTest.java`: JUnit test class that validates the functionality of the `PdfSegment` class.

## Requirements

- Java 8 or higher
- Apache PDFBox library (included in project dependencies)
- Maven (for build and dependency management)

## Setup Instructions

- Build the Project
- Add Sample PDF

## Running the Application

- Run the Application
- Output

## Testing

- Unit Test 
- Test Details

## Usage Examples

- Detecting White Space Gaps
- Identifying Cut Points
- Creating Segmented PDFs

## Notes

- Ensure that the PDF file in src/main/resources/ is properly formatted for the segmentation logic.
- Modify the file path in PdfSegment if using a different file name or location. 

