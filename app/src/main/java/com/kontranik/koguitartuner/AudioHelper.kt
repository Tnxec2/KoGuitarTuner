package com.kontranik.koguitartuner

import java.util.*
import kotlin.math.abs
import kotlin.math.ln


class AudioHelper {

    companion object {
        const val SAMPLE_RATE = 22050
        const val BUFFER_SIZE = 1024
        const val RECORD_OVERLAPS = 0
        const val MIN_ITEMS_COUNT = 15

        const val MAX_FREQUENCY = 800
        const val MIN_FREQUENCY = 50

        fun retrieveNote(tuning: Tuning, pitch: Float): PitchDifference {
            val notes = tuning.notes

            notes.sortedBy { it.frequency }
            var minCentDifference = Float.POSITIVE_INFINITY.toDouble()
            var closest: Note = notes[0]
            for (note in notes) {
                val frequency = note.frequency.toDouble()
                val centDifference = 1200.0 * log2(pitch / frequency)
                if ( abs(centDifference) < abs(minCentDifference) ) {
                    minCentDifference = centDifference
                    closest = note
                }
            }
            return PitchDifference(closest, minCentDifference)
        }

        private fun log2(number: Double): Double {
            return ln(number) / ln(2.0)
        }

        fun calculateAverageDifference(samples: List<PitchDifference>): PitchDifference? {
            val mostFrequentNote = extractMostFrequentNote(samples)
            val filteredSamples = filterByNote(samples, mostFrequentNote)
            var deviationSum = 0.0
            var sameNoteCount = 0
            for (pitchDifference in filteredSamples) {
                deviationSum += pitchDifference.deviation
                sameNoteCount++
            }
            if (sameNoteCount > 0) {
                val averageDeviation = deviationSum / sameNoteCount
                return PitchDifference(mostFrequentNote, averageDeviation)
            }
            return null
        }

        private fun filterByNote(samples: List<PitchDifference>, note: Note?): List<PitchDifference> {
            val filteredSamples: MutableList<PitchDifference> = ArrayList()
            for (sample in samples) {
                if (sample.closest == note) {
                    filteredSamples.add(sample)
                }
            }
            return filteredSamples
        }

        private fun extractMostFrequentNote(samples: List<PitchDifference>): Note? {
            val noteFrequencies: MutableMap<Note?, Int> = HashMap()
            for (pitchDifference in samples) {
                val closest = pitchDifference.closest
                if (noteFrequencies.containsKey(closest)) {
                    val count = noteFrequencies[closest]
                    noteFrequencies[closest] = count!! + 1
                } else {
                    noteFrequencies[closest] = 1
                }
            }
            var mostFrequentNote: Note? = null
            var mostOccurrences = 0
            for (note in noteFrequencies.keys) {
                val occurrences = noteFrequencies[note]
                if (occurrences!! > mostOccurrences) {
                    mostFrequentNote = note
                    mostOccurrences = occurrences
                }
            }
            return mostFrequentNote
        }
    }


}


