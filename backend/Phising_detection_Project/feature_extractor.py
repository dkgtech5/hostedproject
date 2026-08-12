# ============================================================
# feature_extractor.py
# AI-Based Phishing Detection
# ============================================================

from urllib.parse import urlparse
import re
import socket
import time
import ssl

import requests
import dns.resolver
import whois


# ============================================================
# Characters used by the dataset
# ============================================================

CHARACTERS = {
    "dot": ".",
    "hyphen": "-",
    "underline": "_",
    "slash": "/",
    "questionmark": "?",
    "equal": "=",
    "at": "@",
    "and": "&",
    "exclamation": "!",
    "space": " ",
    "tilde": "~",
    "comma": ",",
    "plus": "+",
    "asterisk": "*",
    "hashtag": "#",
    "dollar": "$",
    "percent": "%"
}


# ============================================================
# Character counting
# ============================================================

def count_char(text, character):
    return text.count(character)


def count_features(text, prefix):

    features = {}

    for name, character in CHARACTERS.items():

        features[f"qty_{name}_{prefix}"] = count_char(
            text,
            character
        )

    return features


# ============================================================
# IP address check
# ============================================================

def is_ip_address(hostname):

    if not hostname:
        return False

    pattern = r"^(?:\d{1,3}\.){3}\d{1,3}$"

    return bool(
        re.match(pattern, hostname)
    )


# ============================================================
# URL shortener
# ============================================================

def is_shortened_url(url):

    shorteners = [
        "bit.ly",
        "tinyurl.com",
        "t.co",
        "goo.gl",
        "is.gd",
        "buff.ly",
        "ow.ly",
        "cutt.ly"
    ]

    parsed = urlparse(url)

    hostname = (
        parsed.hostname or ""
    ).lower()

    return int(
        any(
            hostname == domain
            or hostname.endswith("." + domain)
            for domain in shorteners
        )
    )


# ============================================================
# TLD extraction
# ============================================================

def get_tld(hostname):

    parts = hostname.split(".")

    if len(parts) >= 2:
        return parts[-1].lower()

    return ""


# ============================================================
# DNS: IP addresses
# ============================================================

def get_ip_addresses(hostname):

    try:

        addresses = socket.getaddrinfo(
            hostname,
            None
        )

        ips = set()

        for item in addresses:
            ip = item[4][0]
            ips.add(ip)

        return ips

    except Exception:
        return set()


# ============================================================
# DNS: Nameservers
# ============================================================

def get_nameservers(hostname):

    if is_ip_address(hostname):
        return -1

    try:

        answers = dns.resolver.resolve(
            hostname,
            "NS"
        )

        return len(answers)

    except Exception:

        return -1


# ============================================================
# DNS: MX servers
# ============================================================

def get_mx_servers(hostname):

    if is_ip_address(hostname):
        return -1

    try:

        answers = dns.resolver.resolve(
            hostname,
            "MX"
        )

        return len(answers)

    except Exception:

        return -1


# ============================================================
# DNS: SPF
# ============================================================

def get_domain_spf(hostname):

    if is_ip_address(hostname):
        return -1

    try:
        answers = dns.resolver.resolve(
            hostname,
            "TXT"
        )

        for answer in answers:

            text = "".join(
                part.decode()
                if isinstance(part, bytes)
                else part
                for part in answer.strings
            )

            if text.lower().startswith(
                "v=spf1"
            ):

                return 1

        return 0

    except Exception:

        return -1


# ============================================================
# DNS: TTL
# ============================================================

def get_ttl_hostname(hostname):

    if is_ip_address(hostname):
        return -1

    try:
        answer = dns.resolver.resolve(
            hostname,
            "A"
        )

        return answer.rrset.ttl

    except Exception:

        return -1


# ============================================================
# TLS / SSL certificate
# ============================================================

def check_ssl_certificate(hostname):

    try:

        context = ssl.create_default_context()

        with socket.create_connection(
            (hostname, 443),
            timeout=5
        ) as sock:

            with context.wrap_socket(
                sock,
                server_hostname=hostname
            ):

                return 1

    except Exception:

        return 0


# ============================================================
# HTTP response time + redirects
# ============================================================

def get_http_information(url):

    start = time.perf_counter()

    try:

        response = requests.get(
            url,
            timeout=5,
            allow_redirects=True,
            headers={
                "User-Agent":
                "Mozilla/5.0"
            }
        )

        end = time.perf_counter()

        response_time = (
            end - start
        )

        redirects = len(
            response.history
        )

        return (
            response_time,
            redirects
        )

    except Exception:

        return (
            -1,
            -1
        )


# ============================================================
# Domain age / expiration
# ============================================================

def get_domain_dates(hostname):

    if is_ip_address(hostname):
        return -1, -1

    activation = -1
    expiration = -1

    try:

        domain_info = whois.whois(
            hostname
        )

        creation_date = (
            domain_info.creation_date
        )

        expiration_date = (
            domain_info.expiration_date
        )

        # WHOIS may return a list
        if isinstance(
            creation_date,
            list
        ):

            creation_date = (
                creation_date[0]
            )

        if isinstance(
            expiration_date,
            list
        ):

            expiration_date = (
                expiration_date[0]
            )

        # Calculate age in days
        if creation_date:

            activation = (
                time.time()
                - creation_date.timestamp()
            ) / 86400

        # Calculate remaining days
        if expiration_date:

            expiration = (
                expiration_date.timestamp()
                - time.time()
            ) / 86400

    except Exception:

        pass

    return (
        activation,
        expiration
    )


# ============================================================
# MAIN FEATURE EXTRACTION
# ============================================================

def extract_url_features(url):

    # --------------------------------------------------------
    # Validate URL
    # --------------------------------------------------------

    if not isinstance(url, str):

        raise ValueError(
            "URL must be a string."
        )

    url = url.strip()

    if not url:

        raise ValueError(
            "URL cannot be empty."
        )


    # --------------------------------------------------------
    # Add scheme
    # --------------------------------------------------------

    if not url.startswith(
        ("http://", "https://")
    ):

        url = "http://" + url


    # --------------------------------------------------------
    # Parse URL
    # --------------------------------------------------------

    parsed = urlparse(url)

    hostname = (
        parsed.hostname or ""
    )

    path = (
        parsed.path or ""
    )

    parameters = (
        parsed.query or ""
    )


    # ========================================================
    # 1. URL FEATURES
    # ========================================================

    features = count_features(
        url,
        "url"
    )

    features["length_url"] = len(url)


    # ========================================================
    # 2. DOMAIN FEATURES
    # ========================================================

    features.update(
        count_features(
            hostname,
            "domain"
        )
    )

    features["qty_vowels_domain"] = sum(
        hostname.lower().count(vowel)
        for vowel in "aeiou"
    )

    features["domain_length"] = len(
        hostname
    )

    features["domain_in_ip"] = int(
        is_ip_address(hostname)
    )

    features["server_client_domain"] = int(
        "server" in hostname.lower()
        or "client" in hostname.lower()
    )


    # ========================================================
    # 3. DIRECTORY FEATURES
    # ========================================================

    if "/" in path:

        directory = path.rsplit(
            "/",
            1
        )[0]

    else:

        directory = ""

    features.update(
        count_features(
            directory,
            "directory"
        )
    )

    features["directory_length"] = len(
        directory
    )


    # ========================================================
    # 4. FILE FEATURES
    # ========================================================

    filename = ""

    if path:

        filename = path.rsplit(
            "/",
            1
        )[-1]

    features.update(
        count_features(
            filename,
            "file"
        )
    )

    features["file_length"] = len(
        filename
    )


    # ========================================================
    # 5. PARAMETERS
    # ========================================================

    features.update(
        count_features(
            parameters,
            "params"
        )
    )

    features["params_length"] = len(
        parameters
    )

    if parsed.query:

        features["qty_params"] = len(
            parsed.query.split("&")
        )

    else:

        features["qty_params"] = -1


    # ========================================================
    # 6. EMAIL
    # ========================================================

    email_pattern = (
        r"[\w\.-]+@[\w\.-]+\.\w+"
    )

    features["email_in_url"] = int(
        re.search(
            email_pattern,
            url
        ) is not None
    )


    # ========================================================
    # 7. URL SHORTENED
    # ========================================================

    features["url_shortened"] = (
        is_shortened_url(url)
    )


    # ========================================================
    # 8. TLD
    # ========================================================

    tld = get_tld(hostname)

    features["qty_tld_url"] = int(
        bool(tld)
    )


    # ========================================================
    # 9. TLD PRESENT IN PARAMETERS
    # ========================================================

    features["tld_present_params"] = int(
        bool(tld)
        and tld in parameters.lower()
    )


    # ========================================================
    # 10. HTTP RESPONSE TIME + REDIRECTS
    # ========================================================

    response_time, redirects = (
        get_http_information(url)
    )

    features["time_response"] = (
        response_time
    )

    features["qty_redirects"] = (
        redirects
    )


    # ========================================================
    # 11. DNS FEATURES
    # ========================================================

    ips = get_ip_addresses(
        hostname
    )

    if ips:

        features["qty_ip_resolved"] = (
            len(ips)
        )

    else:

        features["qty_ip_resolved"] = -1


    features["qty_nameservers"] = (
        get_nameservers(hostname)
    )

    features["qty_mx_servers"] = (
        get_mx_servers(hostname)
    )

    features["ttl_hostname"] = (
        get_ttl_hostname(hostname)
    )

    features["domain_spf"] = (
        get_domain_spf(hostname)
    )


    # ========================================================
    # 12. ASN IP
    # ========================================================
    #
    # ASN requires an external ASN database/service.
    # We keep the dataset's unavailable value (-1)
    # rather than inventing a number.
    # ========================================================

    features["asn_ip"] = -1


    # ========================================================
    # 13. DOMAIN AGE
    # ========================================================

    activation, expiration = (
        get_domain_dates(hostname)
    )

    features[
        "time_domain_activation"
    ] = activation

    features[
        "time_domain_expiration"
    ] = expiration


    # ========================================================
    # 14. SSL CERTIFICATE
    # ========================================================

    if parsed.scheme == "https":

        features[
            "tls_ssl_certificate"
        ] = check_ssl_certificate(
            hostname
        )

    else:

        features[
            "tls_ssl_certificate"
        ] = 0


    # ========================================================
    # 15. GOOGLE INDEX
    # ========================================================
    #
    # We do NOT scrape Google.
    # -1 represents unavailable information,
    # consistent with the dataset convention.
    # ========================================================

    features[
        "url_google_index"
    ] = -1

    features[
        "domain_google_index"
    ] = -1


    # ========================================================
    # RETURN
    # ========================================================

    return features