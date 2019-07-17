package com.openshift.evg.roadshow.rest.gateway;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.security.cert.CertificateException;

import feign.Client;

class CustomFeignClient {
  private static final Logger logger = LoggerFactory.getLogger(CustomFeignClient.class);
    /**
   * This method should not be used in production!! It is only in a poof of
   * concept!
   * 
   * @return
   */
  private static SSLSocketFactory getSSLSocketFactory() {
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, new X509TrustManager[] { new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          logger.info("checkClientTrusted");
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          logger.info("checkClientTrusted");
        }

        public X509Certificate[] getAcceptedIssuers() {
          logger.info("checkClientTrusted");
          return new X509Certificate[0];
        }
      } }, new SecureRandom());
      logger.warn("Ignoring certification errors! Don't use in production!");
      return context.getSocketFactory();
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  public static Client getClient() {
    return new Client.Default(getSSLSocketFactory(), getHostNameVerifier());
  }

  private static HostnameVerifier getHostNameVerifier() {
    HostnameVerifier hostnameVerifier= new HostnameVerifier(){

      public boolean verify(String hostname,
              javax.net.ssl.SSLSession sslSession) {
                logger.warn("Ignoring hostname verification errors! Don't use in production!");
          return true;
      }
    };
    return hostnameVerifier;
  }
}