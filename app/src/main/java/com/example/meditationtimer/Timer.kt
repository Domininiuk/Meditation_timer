package com.example.meditationtimer

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.meditationtimer.databinding.TimerFragmentBinding
import com.example.meditationtimer.dialogs.ChooseTimeDialog
import com.example.meditationtimer.models.TimeLeft
import com.example.meditationtimer.models.TimerCoroutine
import com.example.meditationtimer.viewmodels.TimerViewModel

class Timer : Fragment(), NumberPicker.OnValueChangeListener {

    companion object {
        fun newInstance() = Timer()
    }

    private lateinit var timerVM: TimerViewModel
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

        initializeViewModel()
        initializeNumberPicker()
    }

    private fun startTimer(minutes : Int)
    {
        secondsLeftLiveData =  timerVM.startTimer(minutes)


        //Hide the button so that another timer cant be started
        _binding!!.floatingActionButton.hide()
        secondsLeftLiveData.observe(this, { secondsLeft: Int ->
            var timeLeft = TimeLeft(secondsLeft)
            _binding!!.timeLeftTextView.text = timeLeft.toString()

            // If the timer is finished, show the button again
            if(secondsLeft == 0)
            {
                _binding!!.floatingActionButton.show()
            }
        })

    }
    private fun initializeViewModel()
    {
        timerVM = ViewModelProvider(this).get(TimerViewModel::class.java)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timerVM.cancelTimer()
    }
    private fun initializeNumberPicker()
    {
      //  binding.numberPicker.minValue = 1
       // binding.numberPicker.maxValue = 50


        _binding!!.floatingActionButton.setOnClickListener{
            val newFragment = ChooseTimeDialog()
            newFragment.setValueChangeListener(this)
            newFragment.show(childFragmentManager, "time picker")


        }
        // When the cancel button is pressed, cancel the timer, change the text in the textview,
        //  show the FAB again, and hide the cancel button
        _binding!!.cancelTimerButton.setOnClickListener{
            _binding!!.timeLeftTextView.text = "Timer not set"
            timerVM.cancelTimer()
            _binding!!.floatingActionButton.show()
            _binding!!.cancelTimerButton.visibility = View.INVISIBLE
        }

    }

    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        if (p0 != null) {

            var minutes = (p0.value + 1) * 5
            //Add one because the list postion started at 0, and multiply by 5 becausue positions were increments of 5
                p0.value += 1
            startTimer(minutes)
            // Show the cancel button
            _binding!!.cancelTimerButton.visibility = View.VISIBLE
        };
    }
}