package com.example.demo;

import org.springframework.boot.SpringApplication;
//追加したインポート
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


//2. (exclude = ...) を追加して、DB接続エラーを回避する
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MonsterTamerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonsterTamerApplication.class, args);
	}

}
