package com.kontranik.koguitartuner


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.kontranik.koguitartuner.databinding.ActivityTunerBinding


class GuitarTunerActivity : AppCompatActivity() {

    companion object {
        const val PREF_TUNING_ID_KEY  = "tuning_id"
        const val PREF_AUTO_TUNING_KEY  = "auto_tuning"
    }

    private var currentTuningId = 0
    private var autoTuning = false

    private var selectedNoteId = 0

    private var isInitailized = false

    private lateinit var binding: ActivityTunerBinding

    private val buttons = mutableListOf<ImageView>()
    private val buttonsText = mutableListOf<TextView>()

    private var pitchDetectorThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_KoGuitarTuner)
        super.onCreate(savedInstanceState)

        binding = ActivityTunerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStart() {
        super.onStart()

        initGui()
    }

    override fun onResume() {
        super.onResume()

        loadPrefs()
        checkPerm()
    }

    override fun onPause() {
        super.onPause()

        pitchDetectorThread?.interrupt()
    }

    override fun onDestroy() {
        super.onDestroy()

        savePrefs()
    }

    private fun savePrefs() {
        val pref = getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor?.putInt(PREF_TUNING_ID_KEY, currentTuningId)
        editor?.putBoolean(PREF_AUTO_TUNING_KEY, autoTuning)
        editor?.apply()
    }

    private fun loadPrefs() {
        val pref = getPreferences(Context.MODE_PRIVATE)

        autoTuning = pref.getBoolean(PREF_AUTO_TUNING_KEY, false)
        binding.switchAutoTuning.isChecked = autoTuning

        changeTuning(pref.getInt(PREF_TUNING_ID_KEY, 0))
    }

    private fun checkPerm() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                lateinit var dialog: AlertDialog

                // Initialize a new instance of alert dialog builder object
                val builder = AlertDialog.Builder(this)

                // Set a title for alert dialog
                builder.setTitle(title)

                // Set a message for alert dialog
                builder.setMessage("Without this permission, the app is unable to detect the tuning of your instrument.")

                // On click listener for dialog buttons
                val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
                    when(which){
                        DialogInterface.BUTTON_POSITIVE -> {
                            ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                        }
                        DialogInterface.BUTTON_NEUTRAL -> {}
                    }
                }
                // Set the alert dialog positive/yes button
                builder.setPositiveButton("RE-TRY",dialogClickListener)
                // Set the alert dialog neutral/cancel button
                builder.setNeutralButton("CANCEL",dialogClickListener)
                // Initialize the AlertDialog using builder object
                dialog = builder.create()
                // Finally, display the alert dialog
                dialog.show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1)
            }
        } else {
            // Permission has already been granted
            initAudio()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        for(ixt in tunings.indices) {
            menu.add(0, ixt, 0, tunings[ixt].desc)
        }

        return true
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean{
        changeTuning(item.itemId)
        return true
    }

    private fun changeTuning(id: Int) {
        currentTuningId = id
        supportActionBar?.title = tunings[currentTuningId].title
        setUiToTuning()
        savePrefs()
    }

    private fun setUiToTuning() {
        val currentTuning = tunings[currentTuningId]

        for (i in buttonsText.indices) {
            buttonsText[i].text = currentTuning.notes[i].getNote()
        }

        toggleNoteButton(selectedNoteId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initAudio()
                }
            }
        }
    }

    private fun initAudio() {
            val pitchDifferences: MutableList<PitchDifference> = mutableListOf()

            val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                AudioHelper.SAMPLE_RATE, AudioHelper.BUFFER_SIZE, AudioHelper.RECORD_OVERLAPS
            )

            val pitchDetector = PitchDetectionHandler { pitchDetectionResult, audioEvent ->
                val pitchInHz = pitchDetectionResult.pitch
                //if (pitchInHz != -1F) Log.d("PITCH", pitchInHz.toString())
                runOnUiThread {
                    isInitailized = true

                    if (pitchInHz == -1f) binding.tunerView.addFrequency(null)
                    else {
                        binding.tunerView.addFrequency(pitchInHz)
                    }

                    if (pitchInHz != -1F) {
                        val pitchDifference =
                            AudioHelper.retrieveNote(tunings[currentTuningId], pitchInHz)
                        pitchDifferences.add(pitchDifference)
                        if (pitchDifferences.size >= AudioHelper.MIN_ITEMS_COUNT) {
                            val average: PitchDifference? =
                                AudioHelper.calculateAverageDifference(pitchDifferences)

                            if (average?.closest != null) {
                                Log.d(
                                    "NOTE",
                                    average.closest.getNote() + ", freg: " + average.closest.frequency.toString() + ", deviation: " + average.deviation
                                )
                                if (autoTuning) {
                                    for (idx in tunings[currentTuningId].notes.indices) {
                                        if (average.closest.frequency == tunings[currentTuningId].notes[idx].frequency) {
                                            toggleNoteButton(idx)
                                            break
                                        }
                                    }
                                }
                            }
                            pitchDifferences.clear()
                        }
                    }
                    if (pitchInHz.toInt() == tunings[currentTuningId].notes[selectedNoteId].frequency.toInt()) {
                        tunings[currentTuningId].notes[selectedNoteId].tuned = true
                        buttonsText[selectedNoteId].setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.button_text_perfect
                            )
                        )
                    }
                }
            }

            val pitchProcessor: AudioProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                AudioHelper.SAMPLE_RATE.toFloat(),
                AudioHelper.BUFFER_SIZE,
                pitchDetector
            )

            dispatcher.addAudioProcessor(pitchProcessor)

            pitchDetectorThread = Thread(dispatcher, "Audio Dispatcher")
            pitchDetectorThread?.start()
    }

    private fun initGui() {
        buttons.add(binding.btn1)
        buttons.add(binding.btn2)
        buttons.add(binding.btn3)
        buttons.add(binding.btn4)
        buttons.add(binding.btn5)
        buttons.add(binding.btn6)

        buttonsText.add(binding.btn1Text)
        buttonsText.add(binding.btn2Text)
        buttonsText.add(binding.btn3Text)
        buttonsText.add(binding.btn4Text)
        buttonsText.add(binding.btn5Text)
        buttonsText.add(binding.btn6Text)

        for (i in buttons.indices) {
            buttons[i].setOnClickListener { toggleNoteButton(i) }
        }

        binding.switchAutoTuning.setOnCheckedChangeListener { buttonView, isChecked ->
            autoTuning = isChecked
            savePrefs()
        }
    }

    private fun toggleNoteButton(notesIndex: Int) {
        val colorNeutral = ContextCompat.getColor(this, R.color.button_text_normal)
        val colorPerfect = ContextCompat.getColor(this, R.color.button_text_perfect)
        val colorHighlighted = ContextCompat.getColor(this, R.color.button_text_selected)

        val colorTintNeutral = ContextCompat.getColor(this, R.color.button_tint_normal)
        val colorTintHighlighted= ContextCompat.getColor(this, R.color.button_tint_selected)

        checkNote(notesIndex)

        for (i in buttonsText.indices) {
            when {
                tunings[currentTuningId].notes[i].tuned -> buttonsText[i].setTextColor(colorPerfect)
                i == selectedNoteId -> buttonsText[i].setTextColor(colorHighlighted)
                else -> buttonsText[i].setTextColor(colorNeutral)
            }
        }

        for (i in buttons.indices) {
            if ( i == selectedNoteId)
                buttons[i].setColorFilter(colorTintHighlighted)
            else
                buttons[i].setColorFilter(colorTintNeutral)
        }
    }

    private fun checkNote(index: Int) {
        if (index >= tunings[currentTuningId].notes.size) return
        selectedNoteId = index
        binding.tunerTitle.text = tunings[currentTuningId].notes[selectedNoteId].getNote()
        binding.tunerView.setNote(tunings[currentTuningId].notes[selectedNoteId])
    }
}

