package com.onemount.java_spring_boot_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.onemount.java_spring_boot_utils.dto.JsonBaseModel;
import com.onemount.java_spring_boot_utils.utils.AesUtil;
import com.onemount.java_spring_boot_utils.utils.RsaUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@EnableCaching   // bật Spring Cache
@SpringBootApplication
public class JavaSpringBootUtilsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSpringBootUtilsApplication.class, args);
	}

	@Bean
	public CommandLineRunner testRsa(ObjectMapper objectMapper) {
		return args -> {
			@Getter
			@Setter
			class User extends JsonBaseModel {
				private String name;
				private Integer age;
				private Boolean isActive;
				private Address address;

				@Getter
				@Setter
				public static class Address extends JsonBaseModel {
					private String street;
					private String city;
					private String stateName;
				}
			}
			User user1 = new User();
			user1.setName("John");
			user1.setAge(23);
			user1.setIsActive(true);
			User.Address address = new User.Address();
			address.setStreet("street 1");
			address.setCity("city 1");
			address.setStateName("state 1");
			user1.setAddress(address);

			log.info("======================== Start Rsa ========================");
			// 1. Generate RSA key pair
			KeyPair keyPairAlice = RsaUtil.generateKeyPair(2048);
			PublicKey publicKeyAlice = keyPairAlice.getPublic();
			PrivateKey privateKeyAlice = keyPairAlice.getPrivate();

			KeyPair keyPairBob = RsaUtil.generateKeyPair(2048);
			PublicKey publicKeyBob = keyPairBob.getPublic();
			PrivateKey privateKeyBob = keyPairBob.getPrivate();

			/*
			* Tao chu ky so va verify
			* Bước 1 message -> sha256(meesage) = hash
			* Bước 2 hash -> base64Encode(hash) = hashEncoded
			* Bước 3 hashEncoded -> sign(hashEncoded, privateKey) = signature
			* Bước 4 verify(hashEncoded, signature, publicKey)
			*/

			/*
			* Ma hoa va giai ma
			* Bước 1 message -> base64Encode(message) = encodedMessage
			* Bước 2 encodeMessage -> rsaEncrypt(encodeMeesage, publicKey) = cipherText
			* Bước 3 cipherText -> rsaDecrypt(cipherText, privateKey) = encodeMessage
			* Bước 4 encodeMessage -> base64Decode(encodeMessage) = message
			* */

			// Alice chuẩn bị message
			User message = user1;
			byte[] messageBytes = objectMapper.writeValueAsBytes(message);

			// Tạo AES key
			SecretKey aesKey = AesUtil.generateAESKey(256);
			byte[] iv = new byte[16];
			new SecureRandom().nextBytes(iv);

			// AES encrypt message
			byte[] cipherTextAES = AesUtil.encrypt(messageBytes, aesKey, iv);

			// Alice hash ciphertext
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashCiphertext = digest.digest(cipherTextAES);

			// Alice sign hash
			byte[] signatureBytes = RsaUtil.sign(hashCiphertext, privateKeyAlice);
			String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

			// Alice encrypt AES key bằng RSA publicKeyBob
			byte[] encryptedAESKeyBytes = RsaUtil.encrypt(aesKey.getEncoded(), publicKeyBob);
			String encryptedAESKeyBase64 = Base64.getEncoder().encodeToString(encryptedAESKeyBytes);
			String ivBase64 = Base64.getEncoder().encodeToString(iv);
			String cipherTextBase64 = Base64.getEncoder().encodeToString(cipherTextAES);

			System.out.println("Alice encrypted AES key: " + encryptedAESKeyBase64);
			System.out.println("Alice IV: " + ivBase64);
			System.out.println("Alice cipherText: " + cipherTextBase64);
			System.out.println("Alice signature: " + signatureBase64);

			// Bob nhận encryptedAESKeyBase64, ivBase64, cipherTextBase64, signatureBase64

			// Bob decrypt AES key
			byte[] encryptedAESKeyBytesBob = Base64.getDecoder().decode(encryptedAESKeyBase64);
			byte[] aesKeyBytes = RsaUtil.decrypt(encryptedAESKeyBytesBob, privateKeyBob);
			SecretKey aesKeyBob = new SecretKeySpec(aesKeyBytes, "AES");

			// Bob decrypt ciphertext
			byte[] ivBob = Base64.getDecoder().decode(ivBase64);
			byte[] cipherTextAESBob = Base64.getDecoder().decode(cipherTextBase64);
			byte[] plainBytes = AesUtil.decrypt(cipherTextAESBob, aesKeyBob, ivBob);

			// Bob hash ciphertext
			MessageDigest digestBob = MessageDigest.getInstance("SHA-256");
			byte[] hashCiphertextBob = digestBob.digest(cipherTextAESBob);

			// Bob verify signature
			byte[] signatureBytesBob = Base64.getDecoder().decode(signatureBase64);
			boolean valid = RsaUtil.verify(hashCiphertextBob, signatureBytesBob, publicKeyAlice);

			System.out.println("Bob verify Alice signature valid: " + valid);
			System.out.println("Bob plaintext JSON: " + new String(plainBytes, StandardCharsets.UTF_8));


			log.info("======================== End Rsa ========================");
		};
	}

	@Bean
	public CommandLineRunner demo(ObjectMapper objectMapper) {
		return args -> {
			@Getter
			@Setter
			class User extends JsonBaseModel {
				private String name;
				private Integer age;
				private Boolean isActive;
				private Address address;

				@Getter
				@Setter
				public static class Address extends JsonBaseModel {
					private String street;
					private String city;
					private String stateName;
				}
			}
			User user1 = new User();
			user1.setName("John");
			user1.setAge(23);
			user1.setIsActive(true);
			User.Address address = new User.Address();
			address.setStreet("street 1");
			address.setCity("city 1");
			address.setStateName("state 1");
			user1.setAddress(address);

			String dataStr  = objectMapper.writeValueAsString(user1);
			byte[] dataByte = objectMapper.writeValueAsBytes(user1);

			log.info("Application started {}", dataStr);
			log.info("Application started 2 {}", user1);
			log.info(Base64.getEncoder().withoutPadding().encodeToString(dataByte));
			log.info(Base64.getUrlEncoder().encodeToString(dataByte));
		};
	}

	@Bean
	public CommandLineRunner caffeineTester(@Qualifier("caffeineCache") Cache<String, Object> cache) {
		return args -> {
			log.info("➤ Put key = foo, value = bar");
			cache.put("foo", "bar");

			log.info("➤ Retrieve immediately: {}", cache.getIfPresent("foo")); // bar

			Thread.sleep(500);
			log.info("➤ After 0.5s (still within TTL): {}", cache.getIfPresent("foo")); // bar

			Thread.sleep(1_500); // total 1.5s since the initial put
			log.info("➤ After a total of 1.5s (expired after 1s): {}", cache.getIfPresent("foo")); // null

			log.info("➤ Cache stats: {}", cache.stats());
		};
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper(); // customize if necessary
	}

	@Bean
	public RestTemplate getRestTemplate() {
		final Duration timeout = Duration.ofSeconds(5);
		// Executor dùng virtual threads (Java 21) – phù hợp IO‑bound
		Executor executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(timeout)
				.executor(executor)          // giới hạn maxThread = poolSize nếu cần
				.version(HttpClient.Version.HTTP_2)
				.build();

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
		factory.setReadTimeout(timeout);
		return new RestTemplate(factory);
	}

	@Bean(name = "caffeineCache")
	public Cache<String, Object> caffeineCache() {
		return Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofSeconds(2))
				.recordStats()
				.maximumSize(5_000)
				.build();
	}

	@Bean
	public CacheManager cacheManager() {
		// 1. Tùy biến builder của Caffeine
		Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
				.maximumSize(10_000)                     // giới hạn số entry
				.expireAfterWrite(Duration.ofMinutes(1))// TTL 2 phút
				.recordStats();                          // thống kê hit/miss

		// 2. Tạo CaffeineCacheManager
		CaffeineCacheManager manager = new CaffeineCacheManager();
		manager.setCaffeine(caffeine);

		// 3. Khai báo sẵn tên cache (không bắt buộc)
		manager.setCacheNames(List.of("users", "orders"));

		// 4. Không lưu giá trị null (tùy chọn)
		manager.setAllowNullValues(false);

		return manager;
	}

}
