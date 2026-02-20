package com.app.meditation

import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.meditation.databinding.FragmentBowBinding
import kotlin.system.exitProcess

class BowFragment : Fragment() {

    private var _binding: FragmentBowBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BowViewModel by activityViewModels()

    private var mainTimer: CountDownTimer? = null
    private var displayTimer: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val preparationRunnables = mutableListOf<Runnable>()

    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var finishSoundId: Int = 0
    private var soundsLoaded = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSoundPool()
        observeViewModel()
        setupClickListeners()

        binding.timeChronometer.setOnChronometerTickListener { 
            val time = SystemClock.elapsedRealtime() - it.base
            val h = (time / 3600000).toInt()
            val m = ((time - h * 3600000) / 60000).toInt()
            val s = ((time - h * 3600000 - m * 60000) / 1000).toInt()
            val formattedTime = String.format("%02d:%02d:%02d", h, m, s)
            it.text = formattedTime
        }
        binding.timeChronometer.text = "00:00:00"
    }

    private fun setupSoundPool() {
        soundPool = SoundPool.Builder().setMaxStreams(2).build()
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) soundsLoaded++
        }
        tickSoundId = soundPool?.load(requireContext(), R.raw.jukbit, 1) ?: 0
        finishSoundId = soundPool?.load(requireContext(), R.raw.jukbitclose, 1) ?: 0
    }

    private fun observeViewModel() {
        viewModel.count.observe(viewLifecycleOwner) { count ->
            binding.countText.text = count.toString()
        }

        viewModel.uiSettings.observe(viewLifecycleOwner) { settings ->
             if (settings != null) {
                binding.intervalText.text = String.format("%.1f", settings.interval)
                binding.planText.text = settings.planCount.toString()
                activity?.window?.apply {
                    if (settings.keepScreenOn) {
                        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }

        viewModel.todayRecord.observe(viewLifecycleOwner) { record ->
            binding.todayRecordText.text = (record ?: 0).toString()
        }
        
        viewModel.workoutState.observe(viewLifecycleOwner) { state ->
            updateUiForState(state)
        }
    }

    private fun setupClickListeners() {
        binding.startButton.setOnClickListener { viewModel.onStartClicked() }
        binding.stopButton.setOnClickListener { 
            viewModel.setTimeWhenStopped(SystemClock.elapsedRealtime() - binding.timeChronometer.base)
            viewModel.onPauseClicked()
        }
        binding.resumeButton.setOnClickListener { viewModel.onResumeClicked() }
        binding.resetButton.setOnClickListener { viewModel.onResetClicked() }
        binding.exitButtonInitial.setOnClickListener { 
            viewModel.onResetClicked()
            requireActivity().finishAffinity()
        }
        binding.endButtonPaused.setOnClickListener { showEndDialog() }
        
        binding.planCard.setOnClickListener {
            findNavController().navigate(R.id.action_bowFragment_to_editPlanFragment)
        }
        binding.todayRecordCard.setOnClickListener {
            findNavController().navigate(R.id.action_bowFragment_to_todayRecordFragment)
        }

        binding.intervalMinus10Button.setOnClickListener { viewModel.updateIntervalInMemory(-1.0) }
        binding.intervalMinus1Button.setOnClickListener { viewModel.updateIntervalInMemory(-0.1) }
        binding.intervalPlus1Button.setOnClickListener { viewModel.updateIntervalInMemory(0.1) }
        binding.intervalPlus10Button.setOnClickListener { viewModel.updateIntervalInMemory(1.0) }
    }

    private fun updateUiForState(newState: WorkoutState) {
        // Default visibility states
        binding.initialButtons.visibility = View.GONE
        binding.runningButtonLayout.visibility = View.GONE
        binding.pausedButtonsLayout.visibility = View.GONE
        binding.preparationCountdownText.visibility = View.GONE
        binding.intervalCountdownText.visibility = View.INVISIBLE
        binding.intervalSettingControls.visibility = View.VISIBLE
        binding.intervalText.visibility = View.VISIBLE

        when (newState) {
            WorkoutState.INITIAL -> {
                binding.initialButtons.visibility = View.VISIBLE
                binding.timeChronometer.stop()
                binding.timeChronometer.base = SystemClock.elapsedRealtime()
                binding.timeChronometer.text = "00:00:00"
                mainTimer?.cancel()
                displayTimer?.cancel()
                stopPreparationTimer()
            }
            WorkoutState.PREPARING -> {
                binding.preparationCountdownText.visibility = View.VISIBLE
                startPreparationTimer()
            }
            WorkoutState.RUNNING -> {
                binding.runningButtonLayout.visibility = View.VISIBLE
                binding.intervalCountdownText.visibility = View.VISIBLE

                val timeWhenStopped = viewModel.timeWhenStopped.value ?: 0L
                binding.timeChronometer.base = SystemClock.elapsedRealtime() - timeWhenStopped
                binding.timeChronometer.start()
                startTimer()
            }
            WorkoutState.PAUSED -> {
                binding.pausedButtonsLayout.visibility = View.VISIBLE
                binding.timeChronometer.stop()
                mainTimer?.cancel()
                displayTimer?.cancel()
                val timeWhenStopped = viewModel.timeWhenStopped.value ?: 0L
                binding.timeChronometer.base = SystemClock.elapsedRealtime() - timeWhenStopped
            }
        }
    }

    private fun startPreparationTimer() {
        stopPreparationTimer()
        binding.preparationCountdownText.text = "2"
        val runnable1 = Runnable { binding.preparationCountdownText.text = "1" }
        val runnable2 = Runnable { viewModel.workoutStarted() }

        handler.postDelayed(runnable1, 1000)
        handler.postDelayed(runnable2, 2000)
        preparationRunnables.add(runnable1)
        preparationRunnables.add(runnable2)
    }

    private fun stopPreparationTimer() {
        preparationRunnables.forEach { handler.removeCallbacks(it) }
        preparationRunnables.clear()
    }

    private fun startTimer() {
        val intervalMillis = ((viewModel.uiSettings.value?.interval ?: 10.1) * 1000).toLong()

        mainTimer?.cancel()
        mainTimer = object : CountDownTimer(Long.MAX_VALUE, intervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                val isGoalReached = viewModel.incrementCount()
                if (soundsLoaded >= 2) {
                    if (isGoalReached) {
                        soundPool?.play(finishSoundId, 1f, 1f, 0, 0, 1f)
                        mainTimer?.cancel() 
                        val duration = SystemClock.elapsedRealtime() - binding.timeChronometer.base
                        handler.postDelayed({ viewModel.endWorkout(duration) }, 2000)
                    } else {
                        soundPool?.play(tickSoundId, 1f, 1f, 0, 0, 1f)
                    }
                }
                displayTimer?.cancel()
                if (!isGoalReached) {
                    startDisplayTimer()
                }
            }
            override fun onFinish() {}
        }.start()
        
        startDisplayTimer()
    }
    
    private fun startDisplayTimer(){
        val intervalMillis = ((viewModel.uiSettings.value?.interval ?: 10.1) * 1000).toLong()
        displayTimer?.cancel()
        displayTimer = object : CountDownTimer(intervalMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                 binding.intervalCountdownText.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() { 
                binding.intervalCountdownText.text = "0"
            }
        }.start()
    }

    private fun showEndDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("종료")
            .setMessage("운동을 종료하고 기록을 저장하시겠습니까?")
            .setPositiveButton("저장") { _, _ ->
                val duration = SystemClock.elapsedRealtime() - binding.timeChronometer.base
                viewModel.endWorkout(duration)
                viewModel.saveSettingsToDatabase()
                requireActivity().finishAffinity()
            }
            .setNegativeButton("취소", null)
            .setNeutralButton("저장 안함") { _, _ -> 
                val duration = SystemClock.elapsedRealtime() - binding.timeChronometer.base
                viewModel.endWorkout(duration, saveRecord = false)
                viewModel.saveSettingsToDatabase()
                requireActivity().finishAffinity()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mainTimer?.cancel()
        displayTimer?.cancel()
        stopPreparationTimer()
        soundPool?.release()
        soundPool = null
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.workoutState.value == WorkoutState.RUNNING) {
            viewModel.setTimeWhenStopped(SystemClock.elapsedRealtime() - binding.timeChronometer.base)
            viewModel.onPauseClicked()
        }
    }
}
