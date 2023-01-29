package org.example;

import com.itextpdf.text.pdf.*;
import com.itextpdf.text.*;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {

    public static final String PUBLIC = "resources/misc/test.cer";
    public static final String DEST = "hello.pdf";

    public static void main(String[] args) throws IOException, DocumentException{

        // step 1: Create a Document
        Document document = new Document();
        // step 2: Create a PdfWriter
        PdfWriter writer = PdfWriter.getInstance(
                document, new FileOutputStream(DEST));
        // step 3: Open the Document
        document.open();
        // step 4: Add content
        document.add(new Paragraph("Hello World..."));
        // create a signature form field
        /*PdfFormField field = PdfFormField.createSignature(writer);
        field.setFieldName("huberkl");
        // set the widget properties
        field.setPage();
        field.setWidget(
                new Rectangle(72, 732, 144, 780), PdfAnnotation.HIGHLIGHT_INVERT);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        // add it as an annotation
        writer.addAnnotation(field);
        // maybe you want to define an appearance
        PdfAppearance tp = PdfAppearance.createAppearance(writer, 144, 96);
        tp.setColorStroke(BaseColor.BLUE);
        tp.setColorFill(BaseColor.LIGHT_GRAY);
        tp.rectangle(0.5f, 0.5f, 71.5f, 47.5f);
        tp.fillStroke();
        tp.setColorFill(BaseColor.BLUE);
        ColumnText.showTextAligned(tp, Element.ALIGN_CENTER,
                new Phrase("SIGN HERE"), 36, 24, 25);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);

*/
        // step 5: Close the Document
        document.close();
    }
}