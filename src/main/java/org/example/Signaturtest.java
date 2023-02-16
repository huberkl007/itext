package org.example;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class Signaturtest {

    public static final String SRC = "hello.pdf";
    public static final String DEST = "hello_signiert.pdf";
    public static final String CERT = "t2g.p7b";
    private static final String SIGN_URL="https://t2g.globaltrust.eu/trust2go/api/signers/usernames/sign";
    public static final String USERNAME = "AD46200911";
    public static final String PIN = "Upc_3141";
    public static final String CERT_SN="2153c427c08e2ea306d9f1";


    public static void main(String[] args) throws GeneralSecurityException, IOException, DocumentException, Exception {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Certificate[] chain = loadChain();  // Zertifikat aus Datei laden - später direkt von T2G laden
        Signaturtest app = new Signaturtest();
        app.sign(SRC, DEST, chain, MakeSignature.CryptoStandard.CADES, "", "");
    }

    public static Certificate[] loadChain() throws Exception {
        CertificateFactory fact = CertificateFactory.getInstance("X.509", "BC");
        InputStream inStream = new FileInputStream(CERT);
        ArrayList<X509Certificate> certlist = (ArrayList) fact.generateCertificates(inStream);
        Certificate[] ret = new Certificate[certlist.size()];
        for (int n = 0; n < certlist.size(); n++) {
            ret[n] = certlist.get(n);
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

            String base64Message=Base64.getEncoder().encodeToString(message);

            String signedMessage=getSignature(base64Message);   // externe Signatur durch T2G, s.u.

            return Base64.getDecoder().decode(signedMessage);

        }
    }

    public void sign(String src, String dest, Certificate[] chain, MakeSignature.CryptoStandard subfilter, String reason, String location) throws GeneralSecurityException, IOException, DocumentException {

        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "Signature");

        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new ServerSignature();
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subfilter);
        stamper.close();
        os.close();
    }

    private String getSignature(String in) {     //signiert "in" (Base64String), gibt signierten Wert zurück (auch Base64String)
        String auth= "Basic "+Base64.getEncoder().encodeToString((USERNAME+":"+PIN).getBytes());
        String requestID="ABCDEF";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .build();                                                       // T2G-API-Aufruf "signature-controller"
            MediaType mediaType = MediaType.parse("application/json");
            String reqbody = "{\"language\": \"EN\",\"requestId\":\""+requestID+"\",\"certificateSerialNumber\":\""+CERT_SN+"\",\"hashes\":[\""+in+"\"],\"hashAlgorithm\":\"sha256\"}";
            RequestBody body = RequestBody.create(mediaType, reqbody);
            Request request = new Request.Builder()
                    .url(SIGN_URL)
                    .method("POST", body)
                    .addHeader("Authorization", auth)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String resp=response.body().string();
            //System.out.println("Resp="+resp);
            JSONObject signobj = new JSONObject(resp);
            //System.out.println("RequestID=" + signobj.get("requestId"));
            JSONArray signarray = new JSONArray(signobj.get("signedHashes").toString());
            JSONObject hashobj = new JSONObject(signarray.get(0).toString());   // erstmal nur ein signierter Hash

            if (hashobj.get("statusMessage").toString().equals("OK")) {
                return hashobj.get("signedHash").toString();
            }
        }catch (Exception e){System.out.println("Exception in getSignature: "+e);}
        return null;
    }
}
