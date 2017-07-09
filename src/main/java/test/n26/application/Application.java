package test.n26.application;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 
 * @author Denys Nikolskyy
 *
 *         Start application class for test task
 *
 */

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan({ "test.n26.controllers", "test.n26.managers", "test.n26.data.storage" })
public class Application {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}

	/**
	 * Bean defining method for realization of asynchronous approach
	 * @return defined bean
	 */
	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(100);
		executor.setMaxPoolSize(500);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("SBT-");
		executor.initialize();
		return executor;
	}

	/**
	 * Bean defining method for realization of scheduling possibility
	 * @return defined bean
	 */
	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(500);
		return scheduler;
	}

}
