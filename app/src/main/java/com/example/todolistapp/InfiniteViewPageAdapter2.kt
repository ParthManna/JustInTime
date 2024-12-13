import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.todolistapp.WeekFragment
import java.time.LocalDate

class InfiniteViewPageAdapter2(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private lateinit var baseDate: LocalDate

    /**
     * Set the base date to calculate the offset of months.
     */
    fun setBaseDate(date: LocalDate) {
        baseDate = date
    }

    /**
     * Returns a fragment representing a month based on position offset.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getItem(position: Int): Fragment {
        val offset = position - START_POSITION
        val dateForMonth = baseDate.plusWeeks(offset.toLong())
        return WeekFragment.newInstance(dateForMonth)
    }

    /**
     * Large number to simulate "infinite" scrolling.
     */
    override fun getCount(): Int {
        return MAX_PAGES
    }

    /**
     * Start position set in the middle of the range to allow both forward and backward navigation.
     */
    fun getMiddlePosition(): Int {
        return START_POSITION
    }

    companion object {
        private const val MAX_PAGES = 1200 // Simulate 100 years (100 years forward and back)
        private const val START_POSITION = MAX_PAGES / 2
    }
}
