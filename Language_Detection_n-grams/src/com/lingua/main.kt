package com.lingua

import com.github.pemistahl.lingua.api.*
import com.github.pemistahl.lingua.api.Language.*
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import java.io.BufferedReader
import java.io.File

class Language_detector {
    init {
        val detector: LanguageDetector = LanguageDetectorBuilder.fromAllSpokenLanguages().build()
        //val detector = LanguageDetectorBuilder.fromLanguages(Language.SERBIAN, Language.GREEK, Language.SLOVENE,Language.MACEDONIAN).build()
        //val detectedLanguage = detector.detectLanguageOf("Parles vous francais")
        val bufferedReader: BufferedReader = File("Language_Detection_n-grams/Dialect_korpus/Strumicki.txt").bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        val confidenceValues = detector.computeLanguageConfidenceValues(inputString)
        println(confidenceValues)
        //.values.elementAt(1)
    }//0.7948293564567115 Kosovian 0.8154272908931579 Prizrenski
     //0.9848844732239616 Kumanov 0.9469716115293692 Ohridski 0.9484093027085897 Strumicki
}

fun main() {
    Language_detector()
}