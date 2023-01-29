package org.example;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nullable;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Sign2 {
    static Certificate[] chain;
    static byte[] toSign;
    static byte[] hash;

    static class MyExternalSignatureContainer implements ExternalSignatureContainer {
        protected byte[] sig;
        public MyExternalSignatureContainer(byte[] sig) {
            this.sig = sig;
        }
        public byte[] sign(InputStream is) {
            return sig;
        }

        @Override
        public void modifySigningDictionary(PdfDictionary signDic) {
        }
    }

    static class EmptyContainer implements ExternalSignatureContainer {
        public EmptyContainer() {
        }
        public byte[] sign(InputStream is) {
            try {
                ExternalDigest digest = getDigest();
                String hashAlgorithm = getHashAlgorithm();

                Certificate[] certs = {null};

                hash = DigestAlgorithms.digest(is, digest.getMessageDigest(hashAlgorithm));
                PdfPKCS7 sgn = getPkcs(certs);

                toSign = sgn.getAuthenticatedAttributeBytes(hash, getOscp(), null,
                        MakeSignature.CryptoStandard.CMS);

                return new byte[0];
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void modifySigningDictionary(PdfDictionary pdfDictionary) {
            pdfDictionary.put(PdfName.FILTER, PdfName.ADOBE_PPKMS);
            pdfDictionary.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_DETACHED);
        }
    }

    public static String getHashAlgorithm() {
        return "SHA256";
    }

    public static byte[] getOscp() {
        byte[] ocsp = null;
        OcspClient ocspClient = new OcspClientBouncyCastle(new OCSPVerifier(null, null));

        if (chain.length >= 2) {
            ocsp = ocspClient.getEncoded((X509Certificate)chain[0], (X509Certificate)chain[1], null);
        }

        return ocsp;
    }

    public static PdfPKCS7 getPkcs() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        return new PdfPKCS7(null, chain, getHashAlgorithm(), null, getDigest(), false);
    }

    public static PdfPKCS7 getPkcs(@Nullable  Certificate[] certChain) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        //noinspection ConstantConditions
        return new PdfPKCS7(null, certChain, getHashAlgorithm(), null, getDigest(), false);
    }

    public static void emptySignature(String src, String dest, String fieldname) throws IOException, DocumentException, GeneralSecurityException {
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.MINUTE, 10);

        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, fieldname);
        appearance.setReason("Nice");
        appearance.setLocation("Delhi");
        appearance.setSignDate(cal);

        ExternalSignatureContainer external = new EmptyContainer();
        MakeSignature.signExternalContainer(appearance, external, 8192);

        os.close();
        reader.close();
    }

    public static void setChain() throws CertificateException {
        String cert = ""; // the cert we get from client
        ByteArrayInputStream userCertificate = new ByteArrayInputStream(Base64.decodeBase64(cert));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        chain = new Certificate[]{cf.generateCertificate(userCertificate)};
    }

    private static ExternalDigest getDigest() {
        return new ExternalDigest() {
            public MessageDigest getMessageDigest(String hashAlgorithm)
                    throws GeneralSecurityException {
                return DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
            }
        };
    }

    public static TSAClient getTsa() {
        return new TSAClientBouncyCastle("http://timestamp.digicert.com", null, null, 4096, "SHA-512");
    }

    public static void createSignature(String src, String dest, String fieldname, byte[] hash, byte[] signature) throws IOException, DocumentException, GeneralSecurityException {
        PdfPKCS7 sgn = getPkcs();
        sgn.setExternalDigest(signature, null, "RSA");

        byte[] encodedSig = sgn.getEncodedPKCS7(hash, getTsa(), getOscp(), null,
                MakeSignature.CryptoStandard.CMS);

        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        ExternalSignatureContainer external = new MyExternalSignatureContainer(encodedSig);
        MakeSignature.signDeferred(reader, fieldname, os, external);

        reader.close();
        os.close();
    }

    public static void main(String[] args) throws Exception {
        setChain();

        String src = "resources/hello.pdf";
        String between = "between.pdf";
        String dest = "test21.pdf";
        String fieldName = "sign";

        emptySignature(src, between, fieldName);
        System.out.println(Hex.encodeHexString(toSign));

        String signature = "";  // signed hash signature we get from client
        byte[] signatureBytes = Hex.decodeHex(signature.toCharArray());

        createSignature(between, dest, fieldName, hash, signatureBytes);
    }
}
