package org.example;

import com.itextpdf.text.pdf.*;
import com.itextpdf.text.*;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Main {

    public static final String DEST = "unsigned.pdf";

    public static void main(String[] args) throws IOException, DocumentException{
        //createDocument(DEST);
        checkDocument(DEST);
    }

    private static void createDocument(String name)throws IOException, DocumentException{
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(
                document, new FileOutputStream(name));
        document.open();
        document.add(new Paragraph("Hello World..."));
        document.close();
    }

    private static void checkDocument(String name)throws IOException, DocumentException{
        PdfReader reader = new PdfReader(new FileInputStream(name));
        AcroFields acroFields = reader.getAcroFields();
        ArrayList<String> signatureNames = acroFields.getSignatureNames();
        for (String s:signatureNames) System.out.println(s);
    }
}