package com.dominikwieczynski.meditationtimer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.dominikwieczynski.meditationtimer.*
import com.dominikwieczynski.meditationtimer.databinding.TimerFragmentBinding
import com.dominikwieczynski.meditationtimer.models.MoodEmoji
import com.dominikwieczynski.meditationtimer.models.TimeLeft
import com.dominikwieczynski.meditationtimer.services.TimerService
import com.dominikwieczynski.meditationtimer.viewmodels.TimerViewModel
import com.dominikwieczynski.meditationtimer.viewmodels.TimerViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.dominikwieczynski.meditationtimer.BuildConfig;
import com.dominikwieczynski.meditationtimer.common.Constants
import com.dominikwieczynski.meditationtimer.common.Utils


class TimerFragment : Fragment(), NumberPicker.OnValueChangeListener, MoodChipOnClickListener {
    // https://stackoverflow.com/questions/31205720/two-floating-action-buttons-next-to-each-other
    companion object {
        const val TIMER_START = "${BuildConfig.APPLICATION_ID}.start"
        const val TIMER_STOP = "${BuildConfig.APPLICATION_ID}.timerStop"
        const val TIMER_SERVICE_SEND_SECONDS = "${BuildConfig.APPLICATION_ID}.sendSeconds"

    }


    private val viewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(
            (requireActivity().application as MeditationApplication).meditationRepository,
            (requireActivity().application as MeditationApplication).sharedPrefRepository)
    }
    private var _binding: TimerFragmentBinding? = null
    private val binding get() = _binding!!
    private var secondsLeftLiveData: LiveData<Int> = MutableLiveData(0)
    private lateinit var service: TimerService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            this@TimerFragment.service = binder.getService()
            viewModel.serviceBound = true

            if(wasThereARotationChange()) {
                continueTimer()
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.serviceBound = true

            service.stopSelf()
            service.stopForeground(true)
        }
    }

    fun updateStreak()
    {
        viewModel.updateStreak()
    }
   fun wasThereARotationChange() : Boolean
   {
       return viewModel.isSecondsLeftNotNull()
   }
    fun continueTimer()
    {
        startTimer(viewModel.secondsLeft?.value!!)
        changeFabIcon()

    }
    override fun onDetach() {
        super.onDetach()

        if (viewModel.serviceBound) {
            unbindService()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = TimerFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeBottomNavigationBar()
        initializeNumberPicker()
        initializeCancelButton()

    }


    private fun initializeBottomNavigationBar()
    {
        var bottombar = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        var navController = findNavController()
        bottombar.setOnItemSelectedListener { item ->
            findNavController()
            when(item.itemId) {
                R.id.history_fragment ->{
                    val action = TimerFragmentDirections.actionTimerToDisplayMeditationsFragment()
                    navController.navigate(action)
                }
                R.id.settings_fragment ->{
                    val action = TimerFragmentDirections.actionTimerToSettingsFragment()
                    navController.navigate(action)
                }
                R.id.statistics_fragment ->{
                    val action = TimerFragmentDirections.actionTimerToStatisticsFragment()
                    navController.navigate(action)
                }
            }
            true
        }
    }


    override fun onStart() {
        super.onStart()
        bindToService()
    }

    private fun bindToService() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun unbindService() {
        viewModel.serviceBound = false
        requireActivity().unbindService(connection)
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isTimerStartedAndRunning()) {

            secondsLeftLiveData.removeObservers { viewLifecycleOwner.lifecycle}
        }
    }


    private fun createMoodDialog() {
        val newFragment = MoodDialogFragment(this)
        newFragment.show(childFragmentManager, "time picker")
    }

    private fun startTimer(seconds: Int) {
        addLockScreenFlag()
        secondsLeftLiveData = viewModel.startTimer(seconds, service)
        observeSecondsLeftLiveData()
        hideBottomBar()
        showCancelButton()
    }

    private fun hideBottomBar() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.INVISIBLE
    }

    private fun showCancelButton()
    {
        binding.cancelTimerButton.visibility = View.VISIBLE

    }
    private fun addLockScreenFlag()
    {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    private fun observeSecondsLeftLiveData()
    {
        secondsLeftLiveData.observe(this) { secondsLeft: Int ->
            val timeLeft = TimeLeft(secondsLeft)
            binding.timeLeftTextView.text = timeLeft.toString()

            if (secondsLeft == 0) {
                timerIsFinished()
            }
        }
    }


    private fun timerIsFinished()
    {
        updateStatistics()
        showBottomAppBar()
        removeLockScreenFlag()
        playFinishBell()
        createMoodDialog()

        binding.cancelTimerButton.visibility = View.GONE
        binding.timeLeftTextView.text = getString(R.string.timer_not_set)
        viewModel.cancelTimer(service)
        changeFabIcon()
        viewModel.resetSecondsLeft()

    }

    private fun updateStatistics()
    {
        viewModel.incrementTotalMeditations()
        viewModel.updateMeditationStreak()
    }
    private fun showBottomAppBar() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }


    private fun removeLockScreenFlag()
    {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun playFinishBell()
    {

        var bellResourceID = 0

        when (viewModel.getBellPreference()) {
                Constants.TIBETAN_BELL_PREF -> bellResourceID = R.raw.bells_tibetan_daniel_simon
                Constants.ANALOG_WATCH_BELL_PREF -> bellResourceID = R.raw.analog_watch_alarm_daniel_simion
                Constants.FRONT_DESK_BELL_PREF -> bellResourceID = R.raw.front_desk_bells_daniel_simon
                Constants.CARTOON_TELEPHONE_BELL_PREF -> bellResourceID = R.raw.cartoon_telephone_daniel_simion
            }

        Utils.playBell(requireContext(), bellResourceID)


    }

    private fun initializeCancelButton()
    {
        binding.cancelTimerButton.setOnClickListener{
            showBottomAppBar()
            viewModel.resetSecondsLeft()

            binding.timeLeftTextView.text = resources.getText(R.string.timer_not_set)
            viewModel.cancelTimer(service)
            changeFabIcon()
            binding.cancelTimerButton.visibility = View.INVISIBLE
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.cancelTimer(service)
    }


    private fun onFabPressed()
    {
        if(viewModel.isTimerStartedAndRunning())
        {

            viewModel.pauseTimer(service)
            changeFabIcon()
        }
        else if(viewModel.isTimerStartedAndPaused())
        {

            resumeTimer()
            changeFabIcon()
        }
        else if(viewModel.isTimerNotStartedAndNotRunning())
        {
            val newFragment = ChooseTimeDialogFragment()
            newFragment.setValueChangeListener(this)
            newFragment.show(childFragmentManager, "time picker")
        }


    }
    private fun changeFabIcon()
    {
        if(viewModel.isTimerStartedAndRunning())
        {
            binding.floatingActionButton.setImageResource(R.drawable.pause_icon)
        }
        else if(viewModel.isTimerStartedAndPaused())
        {
            binding.floatingActionButton.setImageResource(R.drawable.play_icon)
        }
        else if(viewModel.isTimerNotStartedAndNotRunning())
        {
            binding.floatingActionButton.setImageResource(R.drawable.ic_baseline_add_alarm_24)
        }
    }
    private fun initializeNumberPicker()
    {
        binding.floatingActionButton.setOnClickListener{
            onFabPressed()

        }
    }

    private fun resumeTimer()
    {
        secondsLeftLiveData = viewModel.resumeTimer(service)
        secondsLeftLiveData.observe(viewLifecycleOwner) { secondsLeft: Int ->
            val timeLeft = TimeLeft(secondsLeft)
            binding.timeLeftTextView.text = timeLeft.toString()

            if (viewModel.isTimerFinished()) {
                timerIsFinished()
            }
        }

    }



    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        if (p0 != null) {

            var minutes = (p0.value + 1) * 5

            startTimer(minutes*60)


            changeFabIcon()
            // Show the cancel button
        }
    }


    override fun onOkButtonPressed(emoji: MoodEmoji, description: String) {
        saveMeditation(emoji, description)
    }

    private fun saveMeditation(emoji: MoodEmoji, description: String) {
        viewModel.insertMeditation(description, emoji)
        viewModel.updateMoodCount()

    }
}