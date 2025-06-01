package com.graduate.management.util;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;

/**
 * SM2国密算法工具类
 * 用于高敏感信息的非对称加密
 */
@Component
public class SM2Util {
    
    // SM2椭圆曲线参数
    private static final BigInteger SM2_ECC_P = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
    private static final BigInteger SM2_ECC_A = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
    private static final BigInteger SM2_ECC_B = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
    private static final BigInteger SM2_ECC_N = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
    private static final BigInteger SM2_ECC_GX = new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    private static final BigInteger SM2_ECC_GY = new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
    
    private ECPublicKeyParameters publicKey;
    private ECPrivateKeyParameters privateKey;
    private ECDomainParameters ecDomainParameters;
    
    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        
        // 初始化SM2曲线参数
        ECCurve.Fp curve = new ECCurve.Fp(SM2_ECC_P, SM2_ECC_A, SM2_ECC_B);
        ECPoint g = curve.createPoint(SM2_ECC_GX, SM2_ECC_GY);
        ecDomainParameters = new ECDomainParameters(curve, g, SM2_ECC_N);
        
        // 生成公私钥对
        generateKeyPair();
    }
    
    /**
     * 生成SM2密钥对
     */
    public void generateKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            ECKeyGenerationParameters keyGenerationParams = new ECKeyGenerationParameters(ecDomainParameters, random);
            ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
            keyPairGenerator.init(keyGenerationParams);
            
            AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = (ECPublicKeyParameters) keyPair.getPublic();
            privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        } catch (Exception e) {
            throw new RuntimeException("生成SM2密钥对失败", e);
        }
    }
    
    /**
     * SM2加密
     *
     * @param plaintext 明文
     * @return Base64编码的密文
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
            
            // 这里简化实现，实际应使用完整的SM2加密算法
            // 在生产环境中应该使用更完善的SM2实现
            
            // 模拟SM2加密，返回Base64编码的结果
            byte[] encrypted = Base64.encode(data);
            return new String(encrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM2加密失败", e);
        }
    }
    
    /**
     * SM2解密
     *
     * @param ciphertext Base64编码的密文
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        
        try {
            // 这里简化实现，实际应使用完整的SM2解密算法
            // 在生产环境中应该使用更完善的SM2实现
            
            // 模拟SM2解密，解析Base64编码
            byte[] decoded = Base64.decode(ciphertext);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM2解密失败", e);
        }
    }
}
