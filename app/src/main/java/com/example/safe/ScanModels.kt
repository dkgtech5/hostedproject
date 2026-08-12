package com.example.safe

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("url") val url: String
)

data class ScanResponse(
    @SerializedName("url") val url: String,
    @SerializedName("prediction") val prediction: String,
    @SerializedName("legitimate_probability") val legitimateProbability: Double,
    @SerializedName("phishing_probability") val phishingProbability: Double,
    @SerializedName("security_checks") val securityChecks: SecurityChecksResponse? = null
)

data class SecurityChecksResponse(
    @SerializedName("https_enabled") val httpsEnabled: Boolean,
    @SerializedName("no_ip_in_url") val noIpInUrl: Boolean,
    @SerializedName("no_suspicious_redirect") val noSuspiciousRedirect: Boolean,
    @SerializedName("shortened_url") val shortenedUrl: Boolean
)

// OTP Related Models
data class OtpRequest(
    @SerializedName("email") val email: String
)

data class OtpVerifyRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class OtpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
