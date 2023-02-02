package com.chatbot.service;


import com.darkprograms.speech.translator.GoogleTranslate;

import java.io.IOException;

public class TranslationService {

    String SPACE = " ";
    public String translate(String toTranslate){
        try{

            System.out.println(toTranslate);
            String result = GoogleTranslate.translate("gb",toTranslate);
            return result;

        } catch (IOException e){
            e.printStackTrace();
        }
        return toTranslate;
    }

    public String translateLongMessage(String toTranslate){
        try{
            System.out.println(toTranslate);
            String[] parts = toTranslate.split("-");
            String result = GoogleTranslate.translate("de","en",parts[0]);
            String result2 = GoogleTranslate.translate("de","en",parts[1]).toLowerCase();
            System.out.println(result);
            System.out.println(result2);
            return result + SPACE + result2;
        } catch (IOException e){
            e.printStackTrace();
        }
        return toTranslate;
    }

}

