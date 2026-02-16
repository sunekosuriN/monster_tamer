package com.example.demo.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

/**
 * メッセージ管理サービス
 * messages.properties から文言を取得します。
 */
@Service
public class MessageService {

    @Autowired
    private MessageSource messageSource;

    /**
     * メッセージIDとパラメータからメッセージを取得する
     * * @param code messages.propertiesのキー
     * @param args パラメータ（{0}, {1} などに対応）
     * @return 整形されたメッセージ。キーが見つからない場合はコード名を返します。
     */
    public String getMessage(String code, Object... args) {
        try {
            // 日本語（Locale.JAPANESE）でメッセージを取得を試みる
            return messageSource.getMessage(code, args, Locale.JAPANESE);
        } catch (NoSuchMessageException e) {
            // ★重要：メッセージキーが定義されていない場合にエラーで落とさず、
            // ログに原因が表示されるようにコード名をそのまま返します。
            System.err.println("警告: メッセージキーが見つかりません: " + code);
            return "Log: " + code;
        }
    }
}