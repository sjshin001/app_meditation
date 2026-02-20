package com.app.meditation

import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.meditation.databinding.FragmentMeditationDetailBinding
import java.util.concurrent.TimeUnit

enum class MeditationState { PREPARING, RUNNING, PAUSED, FINISHED, EXTRA_TIME }

class MeditationDetailFragment : Fragment() {

    private var _binding: FragmentMeditationDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MeditationDetailFragmentArgs by navArgs()
    private val viewModel: MeditationViewModel by activityViewModels()

    private var mainTimer: CountDownTimer? = null
    private var extraTimer: CountDownTimer? = null
    private var totalTimeInMillis: Long = 0
    private var timeLeftInMillis: Long = 0
    private var extraTimeInMillis: Long = 0

    private var currentState = MeditationState.PREPARING

    private val handler = Handler(Looper.getMainLooper())
    private val preparationRunnables = mutableListOf<Runnable>()

    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isSoundLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalTimeInMillis = TimeUnit.MINUTES.toMillis(args.meditationItem.time.toLong())
        timeLeftInMillis = totalTimeInMillis

        setupSoundPool()
        updateUiForState(MeditationState.PREPARING)
        setupClickListeners()
    }

    private fun setupClickListeners(){
        binding.pauseButton.setOnClickListener { updateUiForState(MeditationState.PAUSED) }
        binding.playButton.setOnClickListener { updateUiForState(MeditationState.RUNNING) }
        binding.closeButton.setOnClickListener { saveRecordAndExit() }
        binding.finishButton.setOnClickListener { saveRecordAndExit() }
    }

    private fun updateUiForState(newState: MeditationState){
        Log.d("MeditationDebug", "Updating UI for state: $newState")
        currentState = newState
        updateScreenLock(newState == MeditationState.RUNNING)

        // Hide all layouts first
        binding.preparationCountdownText.visibility = View.GONE
        binding.countdownText.visibility = View.GONE
        binding.pauseButton.visibility = View.GONE
        binding.pausedControls.visibility = View.GONE
        binding.extraTimeLayout.visibility = View.GONE

        when (currentState) {
            MeditationState.PREPARING -> {
                binding.preparationCountdownText.visibility = View.VISIBLE
                startPreparationTimer()
            }
            MeditationState.RUNNING -> {
                if (isSoundLoaded) { 
                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
                    Log.d("MeditationDebug", "Played start sound")
                } else {
                    Log.d("MeditationDebug", "Start sound not played - sound not loaded")
                }
                binding.countdownText.visibility = View.VISIBLE
                binding.pauseButton.visibility = View.VISIBLE
                startMainTimer()
            }
            MeditationState.PAUSED -> {
                mainTimer?.cancel()
                binding.countdownText.visibility = View.VISIBLE
                binding.pausedControls.visibility = View.VISIBLE
            }
            MeditationState.FINISHED -> { 
                if (isSoundLoaded) { 
                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f) 
                    Log.d("MeditationDebug", "Played end sound")
                } else {
                    Log.d("MeditationDebug", "End sound not played - sound not loaded")
                }
                updateUiForState(MeditationState.EXTRA_TIME)
            }
            MeditationState.EXTRA_TIME -> {
                binding.extraTimeLayout.visibility = View.VISIBLE
                binding.finishedTimeText.text = String.format("%02d:00", args.meditationItem.time)
                startExtraTimer()
            }
        }
    }

    private fun updateScreenLock(keepScreenOn: Boolean) {
        activity?.window?.apply {
            if (keepScreenOn) {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun saveRecordAndExit() {
        var elapsedMillis = totalTimeInMillis - timeLeftInMillis
        if (binding.extraTimeCheckbox.isChecked) {
            elapsedMillis += extraTimeInMillis
        }

        if (elapsedMillis > 0) { 
            val elapsedTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis).toInt()
            viewModel.saveMeditationRecord(elapsedTimeInMinutes)
        }
        findNavController().popBackStack()
    }

    private fun setupSoundPool() {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                isSoundLoaded = true
                Log.d("MeditationDebug", "Sound loaded successfully. Sample ID: $sampleId")
            } else {
                Log.e("MeditationDebug", "Failed to load sound. Status: $status")
            }
        }
        soundId = soundPool?.load(requireContext(), R.raw.singing, 1) ?: 0
    }

    private fun startPreparationTimer() {
        stopPreparationTimer()
        binding.preparationCountdownText.text = "2"
        val runnable1 = Runnable { binding.preparationCountdownText.text = "1" }
        val runnable2 = Runnable { updateUiForState(MeditationState.RUNNING) }

        handler.postDelayed(runnable1, 1000)
        handler.postDelayed(runnable2, 2000)
        preparationRunnables.add(runnable1)
        preparationRunnables.add(runnable2)
    }

    private fun stopPreparationTimer() {
        preparationRunnables.forEach { handler.removeCallbacks(it) }
        preparationRunnables.clear()
    }

    private fun startMainTimer() {
        mainTimer?.cancel()
        mainTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateUiForState(MeditationState.FINISHED)
            }
        }.start()
    }

    private fun startExtraTimer() {
        extraTimer?.cancel()
        val startTime = SystemClock.elapsedRealtime()
        extraTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                extraTimeInMillis = SystemClock.elapsedRealtime() - startTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(extraTimeInMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(extraTimeInMillis) % 60
                binding.extraTimeText.text = String.format("+ %02d:%02d", minutes, seconds)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun updateCountdownText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.countdownText.text = timeFormatted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateScreenLock(false) // Make sure to release the lock
        mainTimer?.cancel()
        stopPreparationTimer()
        extraTimer?.cancel()
        soundPool?.release()
        soundPool = null
        _binding = null
    }
}