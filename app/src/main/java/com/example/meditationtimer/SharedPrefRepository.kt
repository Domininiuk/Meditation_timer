package com.example.meditationtimer

import android.content.Context
import android.content.SharedPreferences
import com.example.meditationtimer.models.MoodCount
import com.example.meditationtimer.models.MoodEmoji
import com.example.meditationtimer.models.Statistics
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

// Could be worthwile to create to Repositories, Settings and Statistics?
class SharedPrefRepository(val context : Context) {

    private val  sharedPref : SharedPreferences =  context.getSharedPreferences(Constants.USER_PREFERENCES,
        Context.MODE_PRIVATE)!!
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    public fun putBellPreference(string: String)
    {

        editor.putString(Constants.BELL_PREF, string).apply()

    }

    fun getBellPreference()  : String
    {
        return sharedPref.getString(Constants.BELL_PREF, Constants.ANALOG_WATCH_BELL_PREF)!!
    }


    fun updateMoodCount(mood : MoodEmoji)
    {
        when(mood){
            MoodEmoji.VERY_BAD -> incrementVeryBadCount()
            MoodEmoji.BAD ->incrementBadCount()
            MoodEmoji.NEUTRAL -> incrementNeutralCount()
            MoodEmoji.GOOD -> incrementGoodCount()
            MoodEmoji.GREAT -> incrementGreatCount()
        }
    }

    private fun incrementVeryBadCount()
    {
        val count = sharedPref.getInt(Constants.VERY_BAD_MOOD_COUNT_PREF, 0) + 1;
        editor.putInt(Constants.VERY_BAD_MOOD_COUNT_PREF, count).apply()

    }

    private fun incrementBadCount()
    {
        val count = sharedPref.getInt(Constants.BAD_MOOD_COUNT_PREF, 0) + 1;
        editor.putInt(Constants.BAD_MOOD_COUNT_PREF, count).apply()
    }

    private fun incrementNeutralCount()
    {
        val count = sharedPref.getInt(Constants.NEUTRAL_MOOD_COUNT_PREF, 0) + 1;
        editor.putInt(Constants.NEUTRAL_MOOD_COUNT_PREF, count).apply()
    }

    private fun incrementGoodCount()
    {
        val count = sharedPref.getInt(Constants.GOOD_MOOD_COUNT_PREF, 0) + 1;
        editor.putInt(Constants.GOOD_MOOD_COUNT_PREF, count).apply()
    }

    private fun incrementGreatCount()
    {
        val count = sharedPref.getInt(Constants.GREAT_MOOD_COUNT_PREF, 0) + 1;
        editor.putInt(Constants.GREAT_MOOD_COUNT_PREF, count).apply()
    }
    //TODO
    // THATS A LOT OF ARGUMENTS, DEPENDENCY INJECTION?
    public fun getMoodCount() : MoodCount
    {
        val veryBadCount = sharedPref.getInt(Constants.VERY_BAD_MOOD_COUNT_PREF, 0)
        val badCount = sharedPref.getInt(Constants.BAD_MOOD_COUNT_PREF, 0)
        val neutralCount = sharedPref.getInt(Constants.NEUTRAL_MOOD_COUNT_PREF, 0)
        val goodCount = sharedPref.getInt(Constants.GOOD_MOOD_COUNT_PREF, 0)
        val greatCount = sharedPref.getInt(Constants.GREAT_MOOD_COUNT_PREF, 0)

        return MoodCount(greatCount, goodCount, neutralCount, badCount, veryBadCount)

    }

    //TODO
    // THATS A LOT OF ARGUMENTS, DEPENDENCY INJECTION?
    // make the getter privates, create the statistics here and return it to the vm
    fun getStatistics() : Statistics
    {

        return Statistics(getDaysInARow(), getLongestDaysInARow(), getTotalMeditations(), getMoodCount())
    }
    public fun getDaysInARow() : Int
    {
        return sharedPref.getInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, 0)
    }

    public fun getLongestDaysInARow() : Int
    {
        return sharedPref.getInt(Constants.LONGEST_STREAK_PREF, 0)
    }
    public fun incrementTotalMeditations()
    {
        val newTotal = getTotalMeditations() + 1
        editor.putInt(Constants.TOTAL_MEDITATIONS_PREF, newTotal).apply()
    }
    public fun getTotalMeditations() : Int
    {
        return sharedPref.getInt(Constants.TOTAL_MEDITATIONS_PREF, 0)
    }
    public fun resetTotalMeditations()
    {
        editor.putInt(Constants.TOTAL_MEDITATIONS_PREF, 0).apply()
    }

    fun resetCurrentStreak() {
        editor.putInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, 0).apply()
    }

    fun resetLongestStreak() {
        editor.putInt(Constants.LONGEST_STREAK_PREF, 0).apply()
    }

    fun updateStreak() {


        val daysDiff = getDayDifference()
        if(daysDiff >= 2)
        {
            editor.putInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, 1).apply()
        }
        else if(daysDiff == 1){
            var currentStreak = sharedPref.getInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, 1)

            editor.putInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, currentStreak + 1).apply()
        }
        else if(daysDiff == 0)
        {
        }

        compareToBestStreak()


    }

    private fun getDayDifference() : Int
    {
        val dateFormat = SimpleDateFormat("dd MM yyyy")
        //Format doesnt work for soem reaosn
        val currentDate = dateFormat.parse(dateFormat.format(System.currentTimeMillis()))

        var lastDayMeditated = Date()
        if(sharedPref.getString(Constants.LAST_DAY_MEDITATED_PREF,
                "") == "")
        {
            return 2
        }
        else{
            lastDayMeditated = dateFormat.parse(
                sharedPref.getString(Constants.LAST_DAY_MEDITATED_PREF,
                    "01 12 1999"
                ))
        }


        val timeDiff = currentDate?.time?.minus(lastDayMeditated?.time!!)
        val daysDiff = TimeUnit.MILLISECONDS.toDays(timeDiff!!).toInt()

        return daysDiff
    }

    private fun compareToBestStreak()
    {
        val currentStreak = sharedPref.getInt(Constants.DAYS_IN_A_ROW_STREAK_PREF, 1)
        val currentBestStreak = sharedPref.getInt(Constants.LONGEST_STREAK_PREF, 1)
        if(currentStreak >= currentBestStreak)
        {
            editor.putInt(Constants.LONGEST_STREAK_PREF, currentStreak).apply()
        }
    }
}