package com.graduate.management.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * SM3国密算法工具类
 * 主要用于密码加密和日志完整性校验
 */
@Component
public class SM3Util {
    
    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * SM3摘要计算
     *
     * @param data 待计算数据
     * @return 摘要值的16进制字符串
     */
    public String hash(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] result = hash(dataBytes);
        return Hex.toHexString(result);
    }
    
    /**
     * SM3摘要计算
     *
     * @param data 待计算数据
     * @return 摘要值
     */
    public byte[] hash(byte[] data) {
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }
    
    /**
     * 计算HMAC-SM3值
     *
     * @param data 原始数据
     * @param key  密钥
     * @return HMAC-SM3值的16进制字符串
     */
    public String hmac(String data, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        
        // 密钥长度大于分组长度时先做哈希
        if (keyBytes.length > 64) {
            keyBytes = hash(keyBytes);
        }
        
        // 构造内外部填充
        byte[] iPadKey = new byte[64];
        byte[] oPadKey = new byte[64];
        
        for (int i = 0; i < 64; i++) {
            if (i < keyBytes.length) {
                iPadKey[i] = (byte) (keyBytes[i] ^ 0x36);
                oPadKey[i] = (byte) (keyBytes[i] ^ 0x5c);
            } else {
                iPadKey[i] = (byte) (0x00 ^ 0x36);
                oPadKey[i] = (byte) (0x00 ^ 0x5c);
            }
        }
        
        // 内层摘要
        byte[] innerData = new byte[iPadKey.length + dataBytes.length];
        System.arraycopy(iPadKey, 0, innerData, 0, iPadKey.length);
        System.arraycopy(dataBytes, 0, innerData, iPadKey.length, dataBytes.length);
        byte[] innerHash = hash(innerData);
        
        // 外层摘要
        byte[] outerData = new byte[oPadKey.length + innerHash.length];
        System.arraycopy(oPadKey, 0, outerData, 0, oPadKey.length);
        System.arraycopy(innerHash, 0, outerData, oPadKey.length, innerHash.length);
        
        return Hex.toHexString(hash(outerData));
    }
}
