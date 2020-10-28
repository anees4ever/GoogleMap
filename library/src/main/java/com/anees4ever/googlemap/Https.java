package com.anees4ever.googlemap;

public class Https {
    public interface ProgressListener {
        void onProgress(long size, long received);
    }
    public static boolean saveHttpsUrlEx(String url, String path, ProgressListener progressListener) {
        return true;
    }

    /*
    public static boolean saveHttpsUrl(String url, String path, ProgressListener progressListener) {
        InputStream input= null;
        OutputStream output= null;
        long size=0;
        try {
            HttpPost hPost= new HttpPost(url);
            CustomHttpsClient hClient= new CustomHttpsClient();
            HttpParams httpParameters = new BasicHttpParams();
            //hPost.addHeader("Referer", "https://smebusinesssoftware.com");

            HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);
            hClient.setParams(httpParameters);

            //fetch
            HttpResponse hResult;
            try {
                hResult= hClient.execute(hPost);
                HttpEntity entity= hResult.getEntity();
                size= entity.getContentLength();
                input= entity.getContent();
            } catch(Exception e) {
                Log.d("Http.connect.execute", e.toString());
                return false;
            }

            if(input==null) {
                return false;
            }
            try {
                byte data[] = new byte[1024*1024];
                long total= 0;
                int count;

                output= new FileOutputStream(path);
                while ((count = input.read(data)) != -1) {
                    total+= count;
                    if(progressListener!=null) {
                        progressListener.onProgress(size, total);
                    }
                    output.write(data, 0, count);
                }
                output.flush();
            } catch(Exception e){
                Log.d("Http.connect.response", e.toString());
                return false;
            }

            return true;
        } catch(Exception e) {
            Log.d("Http.connect", e.toString());
            return false;
        } finally {
            if(output!=null) {
                try {
                    output.close();
                } catch (Exception e) {
                    //ignore
                }
            }
            if(input!=null) {
                try {
                    input.close();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }
    public static class CustomHttpsClient extends DefaultHttpClient {
        public CustomHttpsClient() {
            super();
            SSLSocketFactory socketFactory= SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier(new CustomHostnameVerifier());
            Scheme scheme= (new Scheme("https", socketFactory, 443));
            getConnectionManager().getSchemeRegistry().register(scheme);
        }

        public static CustomHostnameVerifier getVerifier() {
            return new CustomHostnameVerifier();
        }
        public static class CustomHostnameVerifier implements X509HostnameVerifier {
            @Override
            public boolean verify(String host, SSLSession session) {
                HostnameVerifier hv= HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify(host, session);
            }

            @Override
            public void verify(String host, SSLSocket ssl) throws IOException {
            }

            @Override
            public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
            }

            @Override
            public void verify(String arg0, X509Certificate arg1) throws SSLException {
            }
        }
    }
    public static JSONObject fetchJSONFromFile(File file) {
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            StringBuffer output= new StringBuffer();
            BufferedReader br= new BufferedReader(new FileReader(file));
            String line= "";
            while((line=br.readLine())!=null) {
                output.append(line + "\n");
            }
            br.close();
            return new JSONObject(output.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    */
}