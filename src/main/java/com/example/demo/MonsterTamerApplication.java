package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// ★重要：exclude設定を削除しました。
// これにより application.properties の設定に基づいてDB接続が有効になります。
@EnableJpaRepositories("com.example.demo.repository")
@EntityScan("com.example.demo.entity")
public class MonsterTamerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonsterTamerApplication.class, args);
    }

}