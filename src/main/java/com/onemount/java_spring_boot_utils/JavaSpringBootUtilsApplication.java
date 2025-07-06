package com.onemount.java_spring_boot_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
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
	public CommandLineRunner demo(ObjectMapper objectMapper) {
		return args -> {
			@Data
			class User {
				private String name;
				private Integer age;
				private Boolean isActive;
			}
			User user1 = new User();
			user1.setName("John");
			user1.setAge(23);
			user1.setIsActive(true);

			String dataStr  = objectMapper.writeValueAsString(user1);
			byte[] dataByte = objectMapper.writeValueAsBytes(user1);

			log.info("Application started {}", dataStr);
			log.info("Application started 2 {}", user1);
			log.info(Base64.getEncoder().encodeToString(dataByte));
			log.info(Base64.getUrlEncoder().encodeToString(dataByte));
		};
	}

	@Bean
	public CommandLineRunner caffeineTester(@Qualifier("caffeineCache") Cache<String, Object> cache) {
		return args -> {
			log.info("➤ Put key = foo, value = bar");
			cache.put("foo", "bar");

			log.info("➤ Retrieve immediately: {}", cache.getIfPresent("foo")); // bar

			Thread.sleep(1_500);
			log.info("➤ After 1.5s (still within TTL): {}", cache.getIfPresent("foo")); // bar

			Thread.sleep(2_500); // total 2.5s since the initial put
			log.info("➤ After a total of 2.5s (expired after 2s): {}", cache.getIfPresent("foo")); // null

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
				.expireAfterWrite(Duration.ofMinutes(2))// TTL 2 phút
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
