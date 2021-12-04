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
import com.example.meditationtimer.databinding.TimerFragmentBinding
import com.example.meditationtimer.dialogs.ChooseTimeDialog
import com.example.meditationtimer.models.TimerCoroutine
import com.example.meditationtimer.viewmodels.TimerViewModel

class Timer : Fragment(), NumberPicker.OnValueChangeListener {

    companion object {
        fun newInstance() = Timer()
    }

    private lateinit var timerVM: TimerViewModel
    private var _binding : TimerFragmentBinding? = null
    private val binding get() = _binding!!

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
        timerVM.startTimer(minutes)

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
        _binding?.setTimerButton?.setOnClickListener{
            val newFragment = ChooseTimeDialog()
            newFragment.setValueChangeListener(this)
            newFragment.show(childFragmentManager, "time picker")
        }

    }

    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        if (p0 != null) {

            var minutes = (p0.value + 1) * 5
            //Add one because the list postion started at 0, and multiply by 5 becausue positions were increments of 5
                p0.value += 1
            startTimer(minutes)
            Toast.makeText(context,
                "selected number " + minutes, Toast.LENGTH_SHORT).show()
        };
    }
}