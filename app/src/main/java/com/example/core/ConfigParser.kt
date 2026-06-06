package com.example.core

import android.net.Uri
import android.util.Base64
import org.json.JSONObject

data class ProxyConfig(
    val protocol: String,
    val originalUri: String,
    var address: String = "",
    var port: Int = 0,
    var sni: String = "",
    var host: String = "",
    var fragment: String? = null,
    val rawJson: String? = null // For vmess
)

class ConfigParser {

    fun parse(uriString: String): ProxyConfig? {
        return try {
            val uri = Uri.parse(uriString)
            val protocol = uri.scheme?.lowercase() ?: return null

            when (protocol) {
                "vless", "trojan" -> parseStandard(protocol, uri, uriString)
                "vmess" -> parseVmess(uriString)
                "wireguard" -> parseStandard(protocol, uri, uriString)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseStandard(protocol: String, uri: Uri, originalUri: String): ProxyConfig {
        val address = uri.host ?: ""
        val port = uri.port.takeIf { it != -1 } ?: 443
        val sni = uri.getQueryParameter("sni") ?: ""
        val host = uri.getQueryParameter("host") ?: ""
        val fragment = uri.getQueryParameter("fragment")

        return ProxyConfig(
            protocol = protocol,
            originalUri = originalUri,
            address = address,
            port = port,
            sni = sni,
            host = host,
            fragment = fragment
        )
    }

    private fun parseVmess(uriString: String): ProxyConfig? {
        return try {
            // vmess://base64
            val base64Part = uriString.substringAfter("vmess://")
            val decodedBytes = Base64.decode(base64Part, Base64.DEFAULT)
            val jsonString = String(decodedBytes, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)

            val address = jsonObject.optString("add", "")
            val port = jsonObject.optInt("port", 443)
            val sni = jsonObject.optString("sni", "")
            val host = jsonObject.optString("host", "")

            ProxyConfig(
                protocol = "vmess",
                originalUri = uriString,
                address = address,
                port = port,
                sni = sni,
                host = host,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mutates the config to inject dynamic parameters such as fragment lengths
     * and a specific scanner-resolved IP instead of the domain.
     */
    fun mutateConfig(
        config: ProxyConfig,
        newAddress: String? = null,
        fragmentLength: String? = "10-20,10-20,tlshello"
    ): String {
        return try {
            if (config.protocol == "vmess") {
                val jsonObject = JSONObject(config.rawJson ?: "{}")
                if (newAddress != null) {
                    jsonObject.put("add", newAddress)
                }
                // Vmess doesn't standardly use fragment in the vmess:// JSON yet in all clients, 
                // but we simulate adding it to the config if our core supports it.
                if (fragmentLength != null) {
                    jsonObject.put("fragment", fragmentLength)
                }
                val newJsonString = jsonObject.toString()
                val newBase64 = Base64.encodeToString(newJsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                "vmess://$newBase64"
            } else {
                val uri = Uri.parse(config.originalUri)
                val builder = uri.buildUpon()
                
                if (newAddress != null) {
                    // Changing the host without breaking authority
                    builder.encodedAuthority("${uri.userInfo?.let { "$it@" } ?: ""}$newAddress:${config.port}")
                }
                
                if (fragmentLength != null) {
                    builder.appendQueryParameter("fragment", fragmentLength)
                }
                
                builder.build().toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            config.originalUri
        }
    }
}
