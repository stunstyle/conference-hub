package io.jprime.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class QrCodeServiceTest {

    @Inject
    QrCodeService qrCodeService;

    @Test
    public void testQrCodeGeneration() {
        String base64Qr = qrCodeService.generateQrCodeBase64("JP26-TEST", 200, 200);
        assertNotNull(base64Qr);
        assertTrue(base64Qr.length() > 100);
    }
}
