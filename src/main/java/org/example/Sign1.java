package org.example;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class Sign1 {

    public static final String SRC = "src/main/resources/hello.pdf";
    public static final String DEST = "test7.pdf";
    public static final String CERT = "src/main/resources/t2g.p7b";

    public static void main(String[] args) throws GeneralSecurityException, IOException, DocumentException, Exception {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Certificate[] chain = loadChain();
        Sign1 app = new Sign1();
        app.sign(SRC, DEST, chain, CryptoStandard.CADES, "", "");
    }

    public static Certificate[] loadChain() throws Exception {
        CertificateFactory fact = CertificateFactory.getInstance("X.509", "BC");
        InputStream inStream = new FileInputStream(CERT);
        ArrayList<X509Certificate> certlist = (ArrayList) fact.generateCertificates(inStream);
        //System.out.println("laenge: "+certlist.size());
        //for (X509Certificate c: certlist) {System.out.println("issuer: " + c.getSubjectX500Principal());}
        Certificate[] ret = new Certificate[certlist.size()];
        for (int n = 0; n < certlist.size(); n++) {
            ret[n] = (Certificate) certlist.get(n);
        }
        return ret;
    }


    public class ServerSignature implements ExternalSignature {     // externe Signatur über T2G

        public String getHashAlgorithm() {
            return DigestAlgorithms.SHA256;
        }

        public String getEncryptionAlgorithm() {
            return "RSA";
        }

        public byte[] sign(byte[] message) throws GeneralSecurityException {
            System.out.println("message.length=" + message.length);
            System.out.println();
            T2G t2g = new T2G();
            System.out.println("auth=" + t2g.setAuth(T2G.USERNAME, T2G.PIN));
            t2g.setCertSN(T2G.CERT_SN);
            String requestID = t2g.generateRequestID();
            System.out.println("requestID=" + requestID);
            System.out.println("Signed hashes received=" + t2g.sign(message, requestID));

            String status = t2g.getStatus();
            System.out.println("status=" + status);
            if (status.equals("OK")) return t2g.getSignedHashBytes();
            else return null;
        }
    }

    public void sign(String src, String dest, Certificate[] chain, CryptoStandard subfilter, String reason, String location) throws GeneralSecurityException, IOException, DocumentException {

        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "Signature");
        // Creating the signature
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new ServerSignature();
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subfilter);
        stamper.close();
        os.close();
    }
}
