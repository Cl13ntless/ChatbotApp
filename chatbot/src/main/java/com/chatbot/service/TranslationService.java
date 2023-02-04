package com.chatbot.service;


import com.darkprograms.speech.translator.GoogleTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private static final String SPACE = " ";

    public String translate(String toTranslate) {
        try {

            logger.info(toTranslate);
            return GoogleTranslate.translate("gb", toTranslate);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return toTranslate;
    }

    public String translateLongMessage(String toTranslate) {
        try {
            logger.info(toTranslate);
            String[] parts = toTranslate.split("-");
            String result = GoogleTranslate.translate("de", "en", parts[0]);
            String result2 = GoogleTranslate.translate("de", "en", parts[1]).toLowerCase();
            logger.info(result);
            logger.info(result2);
            return result + SPACE + result2;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toTranslate;
    }

}

