package com.onemount.java_spring_boot_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.onemount.java_spring_boot_utils.dto.JsonBaseModel;
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

import java.net.http.HttpClient;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
			KeyPair keyPair = RsaUtil.generateKeyPair(2048);
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();

			String message = objectMapper.writeValueAsString(user1);

			String messageEncode = Base64.getEncoder().encodeToString(message.getBytes());

			System.out.println("message encoded: " + messageEncode);

			String messageDecode = new String(Base64.getDecoder().decode(messageEncode));

			System.out.println("message decoded: " + messageDecode);

			// 2. Encrypt/Decrypt
			String encrypted = RsaUtil.encrypt(message, publicKey);
			String decrypted = RsaUtil.decrypt(encrypted, privateKey);
			System.out.println("Encrypted: " + encrypted);
			System.out.println("Decrypted: " + decrypted);

			// 3. Sign/Verify
			String signature = RsaUtil.sign(message, privateKey);
			boolean isValid = RsaUtil.verify(message, signature, publicKey);
			System.out.println("Signature: " + signature);
			System.out.println("Signature Valid: " + isValid);

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
