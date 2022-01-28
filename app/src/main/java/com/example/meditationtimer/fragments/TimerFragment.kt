package com.example.meditationtimer.fragments

import android.media.MediaPlayer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.meditationtimer.MeditationApplication
import com.example.meditationtimer.R
import com.example.meditationtimer.databinding.TimerFragmentBinding
import com.example.meditationtimer.models.Meditation
import com.example.meditationtimer.models.TimeLeft
import com.example.meditationtimer.viewmodels.TimerViewModel
import com.example.meditationtimer.viewmodels.TimerViewModelFactory
import kotlinx.coroutines.*
import java.util.*

class TimerFragment : Fragment(), NumberPicker.OnValueChangeListener {

    companion object {
        fun newInstance() = TimerFragment()
    }

    private val timerVM: TimerViewModel by viewModels{
        TimerViewModelFactory((activity!!.application as MeditationApplication).repository)
    }
    private var _binding : TimerFragmentBinding? = null
    private val binding get() = _binding!!
    private var secondsLeftLiveData : LiveData<Int> = MutableLiveData(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = TimerFragmentBinding.inflate(inflater, container, false)


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




            timerVM.insertMeditation(Meditation(1, "", 5, Date(2020, 12, 11, 14, 15).toString()))



           initializeViewModel()
           initializeNumberPicker()
           initializeCancelButton()

    }

    // When the TIMER is running, add this flag to activity to keep the timer when the screen is locked

    private fun addLockScreenFlag()
    {
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    // When the Timer ends, use this to clear the flag so that the app isnt running when the screen is locked
    private fun removeLockScreenFlag()
    {
        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    }

    /*
    Start the timer
     */
    private fun startTimer(minutes : Int)
    {
        addLockScreenFlag()
        secondsLeftLiveData =  timerVM.startTimer(minutes)


        //Hide the button so that another timer cant be started
        _binding!!.floatingActionButton.hide()
        secondsLeftLiveData.observe(this, { secondsLeft: Int ->
            val timeLeft = TimeLeft(secondsLeft)
            _binding!!.timeLeftTextView.text = timeLeft.toString()

            // If the timer is finished, play the sound, show the FAB button again and hide the cancel button
            if(secondsLeft == 0)
            {
                timerIsFinished()
            }
        })

    }

    /*

     */
    private fun timerIsFinished()
    {
        removeLockScreenFlag()
        playFinishSound()

        _binding!!.floatingActionButton.show()
        _binding!!.cancelTimerButton.visibility=View.GONE
        _binding!!.timeLeftTextView.text = getString(R.string.timer_not_set)
        timerVM.cancelTimer()
    }

    /*
    Play a sound when the timer is finished
     */
    private fun playFinishSound()
    {
        // Add a way to change the sounds. Maybe add a CurrentSound variable somewhere?
        var mp = MediaPlayer.create(context, R.raw.old_fashioned_school_bell_daniel_simon)

        // This is only ran when the sound is finished, which takes too long
        mp.setOnCompletionListener {
            it.reset()
            it.release()
            mp = null
        }

        mp.start()
        // Pause the sound after two seconds
        Handler(Looper.getMainLooper()).postDelayed({
            mp.reset()
            mp.release()
            mp = null
        }, 2000)
    }

    // When the cancel button is pressed, cancel the timer, change the text in the textview,
    //  show the FAB again, and hide the cancel button
    private fun initializeCancelButton()
    {
        _binding!!.cancelTimerButton.setOnClickListener{
            _binding!!.timeLeftTextView.text = resources.getText(R.string.timer_not_set)
            timerVM.cancelTimer()
            _binding!!.floatingActionButton.show()
            _binding!!.cancelTimerButton.visibility = View.INVISIBLE
        }

    }
    private fun initializeViewModel()
    {
        //timerVM = ViewModelProvider(this).get(TimerViewModel::class.java)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timerVM.cancelTimer()
    }
    private fun initializeNumberPicker()
    {
        _binding!!.floatingActionButton.setOnClickListener{
            val newFragment = ChooseTimeDialog()
            newFragment.setValueChangeListener(this)
            newFragment.show(childFragmentManager, "time picker")
        }
    }

    /*
     When save the meditation object on the device
     */
    private fun saveMeditation()
    {

    }

    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        if (p0 != null) {

            //Add one because the list postion started at 0, and multiply by 5 becausue positions were increments of 5
            var minutes = (p0.value + 1) * 5

            startTimer(minutes)
            // Show the cancel button
            _binding!!.cancelTimerButton.visibility = View.VISIBLE
        };
    }
}