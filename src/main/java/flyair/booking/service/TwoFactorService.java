package flyair.booking.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.exceptions.TimeProviderException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {
    
    @Value("${app.totp.issuer}")
    private String issuer;
    
    @Value("${app.totp.app-name}")
    private String appName;
    
    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final CodeGenerator codeGenerator;
    private final TimeProvider timeProvider;
    
    public TwoFactorService() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }
    
    /**
     * Generate a new secret key for 2FA
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }
    
    /**
     * Generate QR code data URL for the user to scan
     */
    public String generateQrCodeDataUrl(String userEmail, String secret) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(userEmail)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        
        byte[] qrCodeImage = qrGenerator.generate(data);
        return convertToDataUrl(qrCodeImage);
    }
    
    /**
     * Verify the TOTP code provided by the user
     */
    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
    
    /**
     * Generate a TOTP code for testing purposes
     */
    public String generateCode(String secret) {
        try {
            return codeGenerator.generate(secret, Math.floorDiv(timeProvider.getTime(), 30));
        } catch (TimeProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CodeGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return secret;
    }
    
    /**
     * Convert QR code byte array to data URL
     */
    private String convertToDataUrl(byte[] qrCodeImage) {
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(qrCodeImage);
    }
    
    /**
     * Validate secret format
     */
    public boolean isValidSecret(String secret) {
        return secret != null && secret.length() >= 16;
    }
}