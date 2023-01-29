package org.example;


import org.json.*;
import okhttp3.*;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.*;

public class T2G {

    private static final boolean DEBUG = true;

    // zum Testen:
    public static final String USERNAME = "AD46200911";  //"QUQ0NjIwMDkxMTpVcGNfMzE0MQ==";
    public static final String PIN = "Upc_3141";
    public static final String CERT_SN="2153c427c08e2ea306d9f1";

    private static final String CERT_RESP = "[[{\"certificateSerialNumber\":\"2153c427c08e2ea306d9f1\",\"description\":\"\",\"level\":\"USER\",\"certificateType\":\"QUAL\",\"startDateValidity\":\"2023-01-03 12:08:59.000\",\"endDateValidity\":\"2025-01-03 14:08:59.000\",\"force2FA\":true,\"sms2FA\":false,\"certificateString\":\"-----BEGIN CERTIFICATE-----\\r\\nMIIHODCCBSCgAwIBAgILIVPEJ8COLqMG2fEwDQYJKoZIhvcNAQELBQAwWTELMAkG\\r\\nA1UEBhMCQVQxIzAhBgNVBAoTGmUtY29tbWVyY2UgbW9uaXRvcmluZyBHbWJIMSUw\\r\\nIwYDVQQDExxHTE9CQUxUUlVTVCAyMDIwIFFVQUxJRklFRCAxMB4XDTIzMDEwMzEz\\r\\nMDg1OVoXDTI1MDEwMzE1MDg1OVowgcYxCzAJBgNVBAYTAkRFMRIwEAYDVQQHEwlC\\r\\naWVsZWZlbGQxIDAeBgNVBAoTF0xvZXNlbmJlY2sgU3BlY2h0IERhbnR6MRgwFgYD\\r\\nVQQDEw9IdWJlcnR1cyBLbGVpbmUxJjAkBgkqhkiG9w0BCQEWF2tsZWluZUBwYS1s\\r\\nb2VzZW5iZWNrLmRlMRswGQYDVQQFExIxNjA3Nzl3ZHV0eXc0NjUwNjMxDzANBgNV\\r\\nBAQTBktsZWluZTERMA8GA1UEKhMISHViZXJ0dXMwggGiMA0GCSqGSIb3DQEBAQUA\\r\\nA4IBjwAwggGKAoIBgQDhuLsldez3cBYeJSI+92SrFyq2XatQRXtxXHRh5kK+RstG\\r\\nQrBKE7iF9mSKhInej3rKoAm6dhqaR3amxn5Lyain3Etxt6FAoULAkkGsUuBuZbdr\\r\\nu7HCIzAsyrslgr6RJbTrhX/d1nfqNf2J9Ihp7UfcXGFJ3z4begc+g8QVJvONahLm\\r\\nxIfncUThrxJu7oEr9j12zCPf4PeFyIxBYnCXf2+TjaZp19xIOgZsNBqtQLyYexiZ\\r\\nD1J2d0nsjxVjtXwxj9DuUfMRGA2hKp4Zs+aNxVRiZaI/LlF/kf5tqrB7Q7P06ewg\\r\\nAb0EWzpvDSRxgnYWq4sd7xvXFUnZw1pw5x4etd1MXPmHTMNoKE6Xfw0at8kusqbd\\r\\nzr5UfX+wRoGNI/ZRO8zLrHksmmvfTZPQg4hPHWj7jKcFR38MdRbL6zQFWubplCN9\\r\\nnfX6L7KOoDrKj/f8SJLkx1kJnBcCu6Z8P3I8dBm3n8PKpWJ+zVQeMDGUYTTBVjTG\\r\\nNkxyIKk6Wr1z1KPN1VcCAwEAAaOCAhEwggINMA4GA1UdDwEB/wQEAwIGwDAqBgNV\\r\\nHSUEIzAhBgkqhkiG9y8BAQUGCCooACQEAYFJBgorBgEEAYI3CgMMMB0GA1UdDgQW\\r\\nBBTQt9iMAyEIk2LBOaYgRrDSKGJJNDAfBgNVHSMEGDAWgBQlA7qsXAc0ZIN1efsc\\r\\nVIQOqUuTEDCBjgYIKwYBBQUHAQEEgYEwfzAmBggrBgEFBQcwAYYaaHR0cDovL29j\\r\\nc3AuZ2xvYmFsdHJ1c3QuZXUwVQYIKwYBBQUHMAKGSWh0dHA6Ly9zZXJ2aWNlLmds\\r\\nb2JhbHRydXN0LmV1L3N0YXRpYy9nbG9iYWx0cnVzdC0yMDIwLXF1YWxpZmllZC0x\\r\\nLWRlci5jZXIwVgYDVR0fBE8wTTBLoEmgR4ZFaHR0cDovL3NlcnZpY2UuZ2xvYmFs\\r\\ndHJ1c3QuZXUvc3RhdGljL2dsb2JhbHRydXN0LTIwMjAtcXVhbGlmaWVkLTEuY3Js\\r\\nMG0GA1UdIARmMGQwSwYIKigAJAEBCAEwPzA9BggrBgEFBQcCARYxaHR0cDovL3d3\\r\\ndy5nbG9iYWx0cnVzdC5ldS9jZXJ0aWZpY2F0ZS1wb2xpY3kuaHRtbDAJBgcEAIvs\\r\\nQAECMAoGCAQAgZdnAQEDMDcGCCsGAQUFBwEDBCswKTAIBgYEAI5GAQEwCAYGBACO\\r\\nRgEEMBMGBgQAjkYBBjAJBgcEAI5GAQYBMA0GCSqGSIb3DQEBCwUAA4ICAQCIaivf\\r\\n5lEn4jJdKvBBcIo3kN8v4X9J/EysFKuZMdHx2Zf9ImU+T5fBzPZbSB5RTigJLfAQ\\r\\nMg4YbfDyvukMezLjcC3qytk3yrw3Q7D5V7whuvpk+YnLIay1mUvGpnuufEgdertx\\r\\nlRCNIkuvZ0E2lMmBUZHbhQfEWxU4CzIhNIW8Gl8KBiOFFlm2Bc2Fee+4GTjOYzKx\\r\\nGf/ZpHuxGGxTaDVjAmO76jSWiDXWkD4UEjrD+lzi0a/mlX4Iueb7zkf4a32DjocN\\r\\ntzx10S4PMcYme8519AfRLi+NgzKY/YmoytuA21vpxbu0uUqZH0YtwAWsP133jklv\\r\\n3pe5Ve1R5ibgfC7P1HZT0U01aU8pleqDR0PhqfQFAykGJvUQX8IHkVOncP8U3SP9\\r\\nMMV5MPSfGq9P4w5nVDdjNF2GpXzf55LQ8YPBptCxpe/gpHyiMOLz4cdw+jFoP6wx\\r\\nrg74tivIEN0JaonvWdFpQ7mZsK5LicT6n+qiVcuBYsVqJ13INO3bUHhiO0Po6HcP\\r\\ncAFUsXIJl/8/nOiT0YfY4tf4SRfwSIStvxF3G6cCL+VwPJ+f2xjEGCT3SUVP6r8h\\r\\nT+hrpRK8yK7APFCfLgPkeo+qrdASo1NW6jBTBghFAPvx2js4FfKgHP+tpnL9hz9b\\r\\nVe8WR1W+dojKVrQ7XaRbiyMZ4Vm18Q6yMrEWEw==\\r\\n-----END CERTIFICATE-----\\r\\n\",\"contractNumber\":\"T2G202200000125\",\"activated\":true},{\"certificateSerialNumber\":\"-\",\"description\":\"GLOBALTRUST 2020 QUALIFIED 1\",\"level\":\"SUB\",\"certificateType\":\"SIMP\",\"startDateValidity\":\"2020-10-28 23:00:00.000\",\"endDateValidity\":\"2040-06-09 22:00:00.000\",\"force2FA\":false,\"sms2FA\":false,\"certificateString\":\"-----BEGIN CERTIFICATE-----\\r\\nMIIHDjCCBPagAwIBAgILZiXdWzm9pJXwtiQwDQYJKoZIhvcNAQELBQAwTTELMAkG\\r\\nA1UEBhMCQVQxIzAhBgNVBAoTGmUtY29tbWVyY2UgbW9uaXRvcmluZyBHbWJIMRkw\\r\\nFwYDVQQDExBHTE9CQUxUUlVTVCAyMDIwMB4XDTIwMTAyOTAwMDAwMFoXDTQwMDYx\\r\\nMDAwMDAwMFowWTELMAkGA1UEBhMCQVQxIzAhBgNVBAoTGmUtY29tbWVyY2UgbW9u\\r\\naXRvcmluZyBHbWJIMSUwIwYDVQQDExxHTE9CQUxUUlVTVCAyMDIwIFFVQUxJRklF\\r\\nRCAxMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAupe04ASMgKi92Q8o\\r\\nEXMZ0CHJ7Q/Z7++7edt9aHzeHW/cymC/jBRDZ9YBvb702Hg+CBRp0GmWB45fjNja\\r\\nWJxA0hThlv9wZEONpuuvgzZjf8hN2epx/P1cbirccF4OYOJbqFa0OxydMkcxYGgY\\r\\ny4ZnusgAtcMNNvLeRkcXvzUzmv7F7D3uxjZ4ImCtQXGRpuS8IUwIpuJ1OQoqHfCl\\r\\nwPDB6gDdGe8lB3C35TXAWyYybEVr8zDkXq9KluJLq6TveevF5BlYZvkWr19HegV4\\r\\nl0vhj0bm3rQR+fGTBt1Cvlin5F7sJcqekfTpRcKy1PubfYa0t4vDo1pVzRr52QDq\\r\\nurVGJ5JTdU5nHH9VIAX65y/bWHgSx/Mghfr8G4pHoukQ8Vl4sW4UVbgg+Fk6ZgQ2\\r\\nLtIxgFnbaNrFhmWD6yOTp/qIa4sZvrb1bphnIRi/XpLlJ5aF9GVLk9A8EaLGg4lS\\r\\nBg4FtIjWInWz0G1nf1Usbp4ZFcT2lylXE+4FVNUp2EUYop2wjeqXwCt2r1q9LlTp\\r\\nfKdamfZ7eTRP13gwRwUwMZjVwTEyiBGu83I1VXJpWYnnlHkRmtYzf1GYW3BEXa5d\\r\\n5/I7lMyaBjrdmbWWV5WEEdPwy8A3J5ux+c0hBerywBFQ56QLDNS14etZvBKT9xVX\\r\\neCbklcMdQpvpEbzhQpIEvDazw20CAwEAAaOCAeEwggHdMBIGA1UdEwEB/wQIMAYB\\r\\nAf8CAQAwDgYDVR0PAQH/BAQDAgEGMBQGA1UdJQQNMAsGCSqGSIb3LwEBBTAdBgNV\\r\\nHQ4EFgQUJQO6rFwHNGSDdXn7HFSEDqlLkxAwHwYDVR0jBBgwFoAU3C4f0WE3eeSr\\r\\n1dWzEnFoPWponCIwgYEGCCsGAQUFBwEBBHUwczAmBggrBgEFBQcwAYYaaHR0cDov\\r\\nL29jc3AuZ2xvYmFsdHJ1c3QuZXUwSQYIKwYBBQUHMAKGPWh0dHA6Ly9zZXJ2aWNl\\r\\nLmdsb2JhbHRydXN0LmV1L3N0YXRpYy9nbG9iYWx0cnVzdC0yMDIwLWRlci5jZXIw\\r\\nSgYDVR0fBEMwQTA/oD2gO4Y5aHR0cDovL3NlcnZpY2UuZ2xvYmFsdHJ1c3QuZXUv\\r\\nc3RhdGljL2dsb2JhbHRydXN0LTIwMjAuY3JsMIGQBgNVHSAEgYgwgYUwSwYIKigA\\r\\nJAEBCAEwPzA9BggrBgEFBQcCARYxaHR0cDovL3d3dy5nbG9iYWx0cnVzdC5ldS9j\\r\\nZXJ0aWZpY2F0ZS1wb2xpY3kuaHRtbDAJBgcEAIvsQAEAMAkGBwQAi+xAAQEwCQYH\\r\\nBACL7EABAjAJBgcEAIvsQAEDMAoGCAQAgZdnAQEDMA0GCSqGSIb3DQEBCwUAA4IC\\r\\nAQBhXOxTSEtKxN0j3v0rEDqcaHoq4O0kygH3YgGbWZXMdHbyXwJ6O3sBx03ccBJz\\r\\nUuAfGcu5Ctx4VTMr9SXk6KQyc5ugflXpG0SlHvX5maI1SLE80hW4cZ6hwN2MYihp\\r\\n8/jS/H+fDDnqSJkvnOomedAUe71JqQJPuV1zJ5y56RTfWInb6ZgCf8Gx3aduiOyl\\r\\nUQDmCgRZZDSl6HJ4gO/gj901xetTsKqMHDmYVHu7CrTwsQ6z4siTSxNPxXj9+6fh\\r\\nF47njbUPfD4o6M/jqtot9YjNsQBBQ/xptjZBu629WkjQdUvj8sLGLksIFrkRtTBu\\r\\nDBq+rb5qZyPI31F6hY6yruN+H2Zezf4lCR1yx9WV1XiqXFZOV7C1XVLXHOmVhTx5\\r\\nRSwPpI6Cf2Z1WAoM/nXamr75tHxplMouzD88qf2DYN5tGgYKz+g9KHE3ZkRfGUQB\\r\\nZVh4pe5bgvcE8cTgua2+EXZt4T/2zzVIMUlmM6mSAfFBhjborehQcgQDy2NKaRGl\\r\\nmqwYWPwlsC971FtmqixSYljIiMcZ1uCPDWBfthyg5J/ZFZoclVJqYBB7UihI7NKT\\r\\nZnUFPOzNEnSyUWNOZpd6GzH1oix36Kg+NA5cS6L5RcEcpZ7IxW1WX8peP+iOa+VV\\r\\n4emnl/lvw/oJM9uEhHeFZpiyhpZdqqg6M0zk84SGjVqMoQ==\\r\\n-----END CERTIFICATE-----\\r\\n\",\"contractNumber\":\"-\",\"activated\":true},{\"certificateSerialNumber\":\"-\",\"description\":\"GLOBALTRUST 2020\",\"level\":\"ROOT\",\"certificateType\":\"SIMP\",\"startDateValidity\":\"2020-02-09 23:00:00.000\",\"endDateValidity\":\"2040-06-09 22:00:00.000\",\"force2FA\":false,\"sms2FA\":false,\"certificateString\":\"-----BEGIN CERTIFICATE-----\\r\\n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\\r\\nqfrQA71yN2BWHzZ8vTmR9W0Nv3vXkg==\\r\\n-----END CERTIFICATE-----\\r\\n\",\"contractNumber\":\"-\",\"activated\":true}]]";
    private static final String SIGN_RESP = "{\"requestId\":\"oVZyUI\",\"signedHashes\":[{\"hash\":\"RGllc2VzIGlzdCBudXIgZWluIFRlc3Qh\",\"signedHash\":\"OSPYxd3QaPsZ7iC3FR4FDsMPAOgQGqB3QpY1k9pxKTQrlbE64sMzQp8OUe5VRahHwpyQFLPkfqZgHkMgTq8lfQasuIYkgwn9vJ0KFjwToFOB+hfVWOuzhR4yoBSqCnX/0nduHfAuUwoGbO7KFdanzUFp5MUvzj6vFfluTDlwUTJ7bUUjg+W9mhzlEZ6UjWx76qYihFHE5KHnxFaAtfrwfgHw1SbMuz2DoNpq/8TUU/LExMg7Z+5adkC/ktdhs+c3jY3EChVZig0CIY5jaaEz+lCDhDj/maKFR7GTL+bnBN0qlVYLre2maHdiT8zftlfGZugohFxp1Txu90rLudk5lPcceyBLC9XdBLWydUYS9/S061NU9MvgxjI1abXjrsaxltRGdqXigK299TAREn6XCicgDVJnShEiDx7PoFq6W6qkuK98VjRUbe6Y/xlHmFWPfnumws331g/0XReYkqe32DWJm/G3tWWMVKgaV5+1iCpWfxK3ZOEGpuLcr4AB94xv\",\"statusMessage\":\"OK\"}]}";

    // globale Konstanten:
    private static final String CERT_URL= "https://t2g.globaltrust.eu/trust2go/api/signers/usernames/certificates?activeonly=true&language=EN&useronly=false";
    private static final String SIGN_URL="https://t2g.globaltrust.eu/trust2go/api/signers/usernames/sign";

    // globale Variablen:
    private String auth;
    private String certSN;

    private String status;      // Statusmeldung des letzten Signierens
    private String signedhash;  // Ergebnis des letzten Signierens

    public static void main(String[] args) {

        System.out.println("\nT2G API-Test, Version vom 28.01.23");
        System.out.println("-------------------------------------\\n");

        T2G t2g=new T2G();
        System.out.println("auth="+t2g.setAuth(USERNAME, PIN));

        t2g.setCertSN(CERT_SN);
        //t2g.parseCertificates(t2g.getCertificates());

        String requestID=t2g.generateRequestID();
        System.out.println("requestID="+requestID);

        System.out.println(t2g.sign("RGllc2VzIGlzdCBudXIgZWluIFRlc3Qh", requestID));

        System.out.println(t2g.getSignedHash());
    }

    /********************
     setter und getter:
     ********************/

    public String setAuth(String name, String pin) {
        this.auth = "Basic "+Base64.getEncoder().encodeToString((name+":"+pin).getBytes());
        return this.auth;
    }

    public void setCertSN(String certSN) {
        this.certSN = certSN;
    }

    public String getStatus() {
        return this.status;
    }

    public String getSignedHash() {
        return this.signedhash;
    }

    public byte[] getSignedHashBytes() {
        return Base64.getDecoder().decode(this.signedhash);
    }


    /*************************************
     Methoden für das Signieren:
     *************************************/

    public String generateRequestID() { // erstellt eine zufällige RequestID: 6-stellig, erlaubte Ascii-Zeichen: 48-57, 65-90, 97-122
        return RandomStringUtils.random(6, true, true);
    }

    public int sign(byte[] in, String requestID) {     //signiert "in" (ByteArray), sonst s.u.
        return sign(Base64.getEncoder().encodeToString(in), requestID);
    }

    public int sign(String in, String requestID) {     //signiert "in" (Base64String) zu "signedhash" durch t2g der Request-ID "requestID"; gibt Anzahl der signierten Hashes zurück
        System.out.println("T2G.sign");
        int sh=0;
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .build();                                                       // T2G-API-Aufruf "signature-controller"
            MediaType mediaType = MediaType.parse("application/json");
            String reqbody = "{\"language\": \"EN\",\"requestId\":\""+requestID+"\",\"certificateSerialNumber\":\""+certSN+"\",\"hashes\":[\""+in+"\"],\"hashAlgorithm\":\"sha256\"}";
            RequestBody body = RequestBody.create(mediaType, reqbody);
            Request request = new Request.Builder()
                    .url(SIGN_URL)
                    .method("POST", body)
                    .addHeader("Authorization", auth)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject signobj = new JSONObject(response.body().string());
            System.out.println("RequestID: " + signobj.get("requestId"));
            JSONArray signarray = new JSONArray(signobj.get("signedHashes").toString());
            sh=signarray.length();
            JSONObject hashobj = new JSONObject(signarray.get(0).toString());   // erstmal nur ein signierter Hash
            this.status = hashobj.get("statusMessage").toString();
            if (this.status.equals("OK")) {
                this.signedhash = hashobj.get("signedHash").toString();
                System.out.println(this.signedhash);
            }
        }catch (Exception e){this.status=e.toString();sh=0;}
        return sh;
    }

    /******************************************************
     Methoden für die Zertifikate (noch unvollständig!)
     ******************************************************/

    public String getCertificates(){     //lädt Zertifikate von t2g
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            //MediaType mediaType = MediaType.parse("text/plain");
            Request request = new Request.Builder()
                    .url(CERT_URL)
                    .method("GET", null)
                    .addHeader("Authorization", auth)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch(IOException e){return e.toString();}
    }

    public void parseCertificates(String certresp){ // zeigt die geladenen Zertifikate an
        JSONArray chainarray = new JSONArray(certresp);
        int chains=chainarray.length();
        System.out.println("gefundene Zertifikatketten: "+chains);
        //System.out.println(certarray.toString(4));

        if (chains>0) {
            for (int n=0; n<chains; n++){
                System.out.println("\nZertifikatkette "+n+":");
                JSONArray certarray = new JSONArray(chainarray.get(n).toString());
                int certs=certarray.length();
                System.out.println("gefundene Subzertifikate: "+certs);
                for (int c=0; c<certs; c++) {
                    System.out.println("\nZertifikat " + c + ":");
                    JSONObject certobj = new JSONObject(certarray.get(c).toString());
                    //System.out.println(certobj.toString(4));
                    System.out.println(certobj.get("level"));
                    System.out.println(certobj.get("certificateSerialNumber"));
                    System.out.println(certobj.get("certificateString"));
                }
            }
        }
    }
}
