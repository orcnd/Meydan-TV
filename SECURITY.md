# Security

Do not report credentials or private signing files in public issues. Use GitHub
private vulnerability reporting if enabled by the repository owner, otherwise
contact the owner privately before sharing details.

The application sends fixed Alanya/Konya coordinates to Open-Meteo over HTTPS.
It does not collect device location, accounts, analytics or advertising IDs.
Weather is cached in application-private SharedPreferences; backups/transfers
are disabled. IP addresses may still be visible to the weather service as part
of normal HTTPS traffic. The app is a visual forecast, not a safety alert system.

Release passwords must not enter Git or CI logs. Local release credentials use
Windows DPAPI. To migrate machines, securely transfer the keystore and a separately
recoverable password; an encrypted password.xml alone is not portable.
