package com.dominikwieczynski.meditationtimer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.dominikwieczynski.meditationtimer.R
import com.dominikwieczynski.meditationtimer.viewmodels.ChooseTimeDialogViewModel
import com.dominikwieczynski.meditationtimer.viewmodels.ChooseTimeDialogViewModelFactory

open class ChooseTimeDialogFragment : DialogFragment() {
    private lateinit var valueChangeListener: OnValueChangeListener

    private val viewModel: ChooseTimeDialogViewModel by viewModels{
        ChooseTimeDialogViewModelFactory()
    }


    private lateinit var builder : AlertDialog.Builder
    private lateinit var numberPicker : NumberPicker
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {



        initializeNumberPicker()
        setMinAndMaxValue()
        setDisplayedValues()

        createDialogBuilder()
        buildDialogTitle()
        buildBuilderMessage()


        buildPositiveButton()
        buildNegativeButton()

        builder.setView(numberPicker)


        return builder.create()
    }


   private fun initializeNumberPicker()
    {
     numberPicker =  NumberPicker(activity)
     numberPicker.id = R.id.dialog_number_picker
    }

    private fun setMinAndMaxValue()
    {
        numberPicker.minValue = 0
        numberPicker.maxValue = viewModel.getListOfIntervalsSize() - 1
    }

    private fun setDisplayedValues()
    {
        val array = viewModel.getListOfIntervalsAsArray()
        numberPicker.displayedValues = array
    }

    private fun createDialogBuilder()
    {
        builder = AlertDialog.Builder(activity)
    }

    private fun buildDialogTitle()
    {
        builder.setTitle(getString(R.string.how_many_minutes_would_you_like_to_meditate_for))
    }

    private fun buildBuilderMessage()
    {
        builder.setMessage(getString(R.string.choose_a_number))
    }

    private fun buildPositiveButton()
    {
        builder.setPositiveButton(
            resources.getString(R.string.okay)
        ) { _, _ ->
            valueChangeListener.onValueChange(
                numberPicker,
                numberPicker.value, numberPicker.value
            )


        }
    }

    private fun buildNegativeButton()
    {
        builder.setNegativeButton(resources.getString(android.R.string.cancel)) {
                _, _ ->
        }
    }


    fun getValueChangeListener(): OnValueChangeListener? {
        return valueChangeListener
    }


     fun setValueChangeListener(valueChangeListener: OnValueChangeListener?) {
        this.valueChangeListener = valueChangeListener!!
    }
}