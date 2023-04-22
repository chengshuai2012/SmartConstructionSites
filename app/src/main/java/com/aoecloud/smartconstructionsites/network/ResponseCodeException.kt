
package com.aoecloud.smartconstructionsites.network

import java.io.InterruptedIOException


class ResponseCodeException(val responseCode: Int,val mes:String) : InterruptedIOException("Http request failed with response code $responseCode")
