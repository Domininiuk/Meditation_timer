package com.dominikwieczynski.meditationtimer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.dominikwieczynski.meditationtimer.SharedPrefRepository
import com.dominikwieczynski.meditationtimer.models.Meditation
import com.dominikwieczynski.meditationtimer.room.MeditationRepository


//TODO: Create a universal viewmodel factory class for viewmodels that depend on the depository?
class DisplayMeditationsViewModel(private val meditationRepository: MeditationRepository,
private val sharedPrefRepository: SharedPrefRepository) : ViewModel() {

    val allMeditations = meditationRepository.allMeditations.asLiveData()



    fun groupByDate(meditations : List<Meditation>): Map<String, List<Meditation>> {

        return meditations.groupBy { it.convertToMeditationDate().date }
    }

     fun getTotalMeditations() : Int
    {
        return sharedPrefRepository.getTotalMeditations()
    }

    fun getLongestDaysInARow(): Int {
        return sharedPrefRepository.getLongestDaysInARow()
    }

    fun getCurrentDaysInARow(): Int {
        return sharedPrefRepository.getDaysInARow()
    }

}
    class DisplayMeditationsViewModelFactory(private val meditationRepository: MeditationRepository,
    private val sharedPrefRepository: SharedPrefRepository) : ViewModelProvider.Factory
    {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(DisplayMeditationsViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return DisplayMeditationsViewModel(meditationRepository, sharedPrefRepository ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

    }


