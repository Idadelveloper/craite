package com.example.craite.utils

import android.os.Bundle

class CommonUtils {
    companion object {
        // Helper function to convert bundle to string
        fun bundleToString(bundle: Bundle): String {
            val sb = StringBuilder()
            for (key in bundle.keySet()) {
                if (sb.isNotEmpty()) {
                    sb.append("&")
                }
                sb.append(key).append("=").append(bundle.get(key))
            }
            return sb.toString()
        }

        fun stringToBundle(queryString: String): Bundle {
            val bundle = Bundle()
            val params = queryString.split("&")
            for (param in params) {
                val parts = param.split("=")
                if (parts.size == 2) {
                    bundle.putString(parts[0], parts[1])
                }
            }
            return bundle
        }
//            val bundle = backStackEntry.arguments?.getString("bundle")?.let { stringToBundle(it) }
    }
}