package com.kontranik.koguitartuner

import kotlin.math.pow
import kotlin.math.round


const val SHARP_SIGN = "#"

class Note(
    private val letter: String = "",
    private val number: Int = 0,
    private val sharp: Boolean = false,

    ) {
    var tuned: Boolean = false
    val frequency: Float =  round(calculateFrequency(letter, number) * 100f ) / 100f

    fun getNote(): String {
        return letter + number.toString() + if ( sharp ) SHARP_SIGN else ""
    }
}

fun calculateFrequency(noteLetter: String, noteNumber: Int): Float{
    val baseFrequency = 440
    val exp = (1.0/12.0).toFloat()
    val step = 2.toFloat().pow(exp)
    val scaleDiff = noteNumber - 4
    //convert letter to number; C -> 1
    var noteLetterNumber = 0
    when (noteLetter.first()){
        'C' -> noteLetterNumber = 1
        'D' -> noteLetterNumber = 3
        'E' -> noteLetterNumber = 5
        'F' -> noteLetterNumber = 6 //only half step from E to F
        'G' -> noteLetterNumber = 8
        'A' -> noteLetterNumber = 10
        'B' -> noteLetterNumber = 12
    }
    var stepDiff = scaleDiff * 12 + noteLetterNumber - 10 //considers the fact that there is only half step difference between B & C
    if (noteLetter.length > 1){
        stepDiff += 1
    }
    return (baseFrequency * (step.pow(stepDiff)))
}
