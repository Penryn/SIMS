package com.graduate.management.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * SM4国密算法工具类
 * 主要用于身份证号、联系电话、住址等敏感信息加密
 */
@Component
public class SM4Util {
    
    @Value("${system.sm4.key:a123456789012345}")
    private String keyString;
    
    @Value("${system.sm4.iv:1234567890123456}")
    private String ivString;
    
    private Key key;
    private IvParameterSpec iv;
    
    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = ivString.getBytes(StandardCharsets.UTF_8);
            
            key = new SecretKeySpec(keyBytes, "SM4");
            iv = new IvParameterSpec(ivBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM4初始化失败", e);
        }
    }
    
    /**
     * 生成SM4密钥
     *
     * @return 密钥的Base64编码
     */
    public String generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("SM4", BouncyCastleProvider.PROVIDER_NAME);
            kg.init(128, new SecureRandom());
            Key key = kg.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成SM4密钥失败", e);
        }
    }
    
    /**
     * 加密
     *
     * @param plaintext 明文
     * @return 密文的Base64编码
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            return plaintext;
        }
        
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }
    
    /**
     * 解密
     *
     * @param ciphertext 密文的Base64编码
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.trim().isEmpty()) {
            return ciphertext;
        }
        
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] encrypted = Base64.getDecoder().decode(ciphertext);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败", e);
        }
    }
}
