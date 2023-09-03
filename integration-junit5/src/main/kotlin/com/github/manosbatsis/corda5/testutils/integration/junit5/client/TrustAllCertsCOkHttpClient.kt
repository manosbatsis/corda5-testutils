package com.github.manosbatsis.corda5.testutils.integration.junit5.client

import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object TrustAllCertsCOkHttpClient {
    @get:Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    val trustAllCertsClient: OkHttpClient
        get() {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) { /*NO-OP*/
                    }

                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) { /*NO-OP*/
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val newBuilder = Builder()
            newBuilder.sslSocketFactory(sslContext.socketFactory, (trustAllCerts[0] as X509TrustManager))
            newBuilder.hostnameVerifier { hostname: String?, session: SSLSession? -> true }
            return newBuilder.build()
        }
}
